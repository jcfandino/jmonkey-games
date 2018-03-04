package com.stovokor.domain.enemy

import com.jme3.animation.AnimChannel
import com.jme3.animation.AnimControl
import com.jme3.animation.AnimEventListener
import com.jme3.animation.LoopMode
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.stovokor.util.math.TimedUpdate
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.EnemyDied
import com.jme3.animation.SkeletonControl
import com.jme3.scene.Spatial.CullHint

object States {
  sealed case class State(val s: String)
  object Idle extends State("Idle")
  object Chase extends State("Chase")
  object Dead extends State("Dead")
  object Attack extends State("Attack")
}

class EnemyFSM(val self: EnemyControl) {

  def tactic = self.tactic

  var state: Option[State] = None

  def toState(s: States.State): State = {
    val behavior = tactic.behaviorFor(s)
    s match {
      case States.Idle => new Idle(behavior)
      case States.Chase => new Chase(behavior)
      case States.Dead => new Dead
      case States.Attack => new Attack(behavior)
      case States.State(x) => throw new IllegalArgumentException(x)
    }
  }
  def setState(s: States.State): State = {
    def st = toState(s)
    setState(st)
  }

  def setState(st: State): State = {
    state = Some(st)
    self.setState(st.s)
    st.enterState
    st
  }

  def update(tpf: Float) {
    if (!state.isDefined) {
      println("Initing FSM")
      state = Some(toState(States.State(self.getState)))
    }
    state.get.update(tpf)
  }

  abstract class State(val s: States.State) {

    def update(tpf: Float)
    def enterState
  }
  abstract class FsmState(val name: States.State, val behavior: Behavior) extends State(name) {

    var timeAccumulated = 0F

    def update(tpf: Float) {
      timeAccumulated += tpf
      behavior.update(self)
      val transition = next
      if (transition != name) {
        setState(transition)
      }
    }
    def next: States.State
  }

  class Idle(behavior: Behavior) extends FsmState(States.Idle, behavior) with TimedUpdate {
    val checkSpan = 200L
    def next = {
      // If I saw the player chase him!
      if (canCheck && self.hasSeenPlayer &&
        (self.isPlayerInFront && self.canSeePlayer || self.isPlayerTooClose)) {
        States.Chase
      } else name
    }

    def enterState {
      println("Entering Idle")
      self.animChannels.getBodyChannel.setAnim("Idle")
      self.animChannels.getArmChannel.setAnim("Idle")
      self.animChannels.getBodyChannel.setLoopMode(LoopMode.Loop)
      self.animChannels.getArmChannel.setLoopMode(LoopMode.Loop)
    }
  }
  class Dead extends FsmState(States.Dead, new StandStill) {
    def enterState {
      self.animChannels.getBodyChannel.setAnim("Die")
      self.animChannels.getBodyChannel.setLoopMode(LoopMode.DontLoop);

      self.weapon.weaponControl.looseTheTrigger
      val phys = self.getPhysicsControl
      val space = phys.getPhysicsSpace
      val spat = self.getSpatial
      val skeletonCtrl = self.animChannels.getSkeletonControl
      phys.setWalkDirection(Vector3f.ZERO)
      space.remove(phys)
      while (spat.getNumControls > 0) spat.removeControl(spat.getControl(0))

      val l = new AnimEventListener() {
        def onAnimCycleDone(ctrl: AnimControl, chn: AnimChannel, n: String) {
          println("Triggering EnemyDied")
          if (n == "Die") {
            // Disable culling so mesh is updated off camera
            spat.setCullHint(CullHint.Never)
            EventHub.trigger(new EnemyDied(spat))
            ctrl.removeListener(this)
          }
        }
        def onAnimChange(ctrl: AnimControl, chn: AnimChannel, n: String) {}
      }
      self.animChannels.getAnimControl.addListener(l)
      spat.setName("dead-body")
    }
    val next = name
  }

  class Chase(behavior: Behavior) extends FsmState(States.Chase, behavior) {
    def pFinder = self.pathFinder
    def pcon = self.getPhysicsControl
    def goto = self.getLastSeenPlayer

    def enterState {
      if (self.animChannels.getArmChannel.getAnimationName != "Shoot") {
        self.animChannels.getBodyChannel.setAnim("Walk")
        self.animChannels.getBodyChannel.setLoopMode(LoopMode.Loop)
        self.animChannels.getArmChannel.setAnim("Walk")
      }
    }

    def closeToLastWaypoint(implicit self: EnemyControl) = {
      val v1 = pFinder.getPath().getLast().getPosition()
      val v2 = self.getPosition
      (new Vector2f(v1.x, v1.z) distance new Vector2f(v2.x, v2.z)) < 2f
    }

    def next: States.State = {
      if (goto.distance(self.getPosition) < self.distanceToReach) {
        // I'm close, can I shoot?
        //        pcon.setViewDirection(goto.subtract(self.getPosition).setY(0f).normalize)
        //        if (self.isPointingToThePlayer)
        return States.Attack //new Attack(new StrafeAround) //(new StandStill)
      }
      // TODO Concurrency problem here, can't access pathFinder is used from the other thread
      if (self.lastPosCalculation.distance(self.getPosition) < 2f) {
        //      if (pFinder.getNextWaypoint() != null) { //A path was traced
        //        if (closeToLastWaypoint(self)) {
        println("Arrived to last known player position, idling.")
        pcon.setWalkDirection(Vector3f.ZERO)
        return States.Idle //new Idle(new StandStill())
        //        }
      } else {
        //        println("No path to follow, keep chasing?")
      }
      return name
    }

  }

  class Attack(behavior: Behavior) extends FsmState(States.Attack, behavior) {

    def weaponControl = self.weapon.weaponControl

    def enterState {
      if (self.animChannels.getArmChannel.getAnimationName != "Shoot") {
        self.animChannels.getBodyChannel.setLoopMode(LoopMode.DontLoop)
        self.animChannels.getBodyChannel.setAnim("Shoot", 1)
        self.animChannels.getBodyChannel.setSpeed(4f)
      }
      weaponControl.pullTheTrigger
    }

    def next: States.State = {
      self.turnToPlayer
      if (self.isPointingToThePlayer || timeAccumulated < 1f) {
        if (!weaponControl.inRecoil) {
          weaponControl.pullTheTrigger
        }
        name
      } else {
        weaponControl.looseTheTrigger
        States.Chase
      }
    }
  }
}