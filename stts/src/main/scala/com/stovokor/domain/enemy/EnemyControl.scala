package com.stovokor.domain.enemy

import scala.collection.JavaConversions.asScalaBuffer
import com.jme3.ai.navmesh.NavMeshPathfinder
import com.jme3.bullet.collision.PhysicsRayTestResult
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.bullet.control.GhostControl
import com.jme3.math.FastMath
import com.jme3.math.Plane
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.Control
import com.stovokor.ai.Navigation
import com.stovokor.domain.CanBeHit
import com.stovokor.domain.Container
import com.stovokor.domain.EnemyWeaponControl
import com.stovokor.domain.NodeId
import com.stovokor.domain.Weapon
import com.stovokor.domain.Entity
import com.stovokor.util.jme.HasSpatialState
import com.stovokor.util.math.TimedUpdate
import com.stovokor.util.math.TimedChecker
import com.stovokor.state.PhysicsSchedulingAppState
import com.jme3.bullet.PhysicsSpace
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.ItemDropped
import scala.collection.JavaConversions._
import java.util.ArrayList

class EnemyCharacter(model: Node, control: EnemyControl, val weapon: Weapon, val point: Control)
    extends Entity(model, control, Some(model)) {
}

object EnemyControl {
  def apply(weapon: Weapon, charModel: Spatial, nav: Navigation, specs: EnemySpecs) = {

    // setup animation:
    try {

      val animChannels = new AnimChannels(charModel)
      val anim = animChannels.getAnimControl
      //      anim.addListener(new AnimEventListener() {
      //        def onAnimCycleDone(control: AnimControl, channel: AnimChannel, name: String) {}
      //        def onAnimChange(control: AnimControl, channel: AnimChannel, name: String) {}
      //      })
      anim.getNumChannels()
      val channel = anim.createChannel()
      //      animChannels.setBodyDefaultAnimation("Idle")
      //      channel.setAnim("Idle")

      val legsChannel = anim.createChannel
    } catch {
      case e: NullPointerException => println("No anim control for character");
    }
    //      legsChannel.setAnim("IdleBase")
    //      attackChannel.addBone(anim.getSkeleton.getBone("uparm.right"))
    //      attackChannel.addBone(anim.getSkeleton.getBone("arm.right"))
    //      attackChannel.addBone(anim.getSkeleton.getBone("hand.right"))

    new EnemyControl(weapon, charModel, nav, specs)
  }
}

class EnemySpecs(val health: Int, val speed: Int, val tactic: EnemyTactic) {
  def this(c: Container) = this(c.health, c.speed, c.tactic)

}

class EnemyControl(
  val weapon: Weapon,
  val charModel: Spatial,
  val nav: Navigation,
  val specs: EnemySpecs)

    extends AbstractControl with CanBeHit with HasSpatialState {

  val animChannels = new AnimChannels(charModel)
  val fsm = new EnemyFSM(this)
  val pathFinder = new NavMeshPathfinder(nav.navMesh)

  // Mutable vector for path finding
  val lastPosCalculation = new Vector3f

  // Specs
  val distanceToReach = specs.tactic.parameters.distanceToReach
  val speed = specs.speed
  val tactic = specs.tactic

  val turnTarget = new Vector3f(1, 0, 0)

  def isAwake = getOr("awake", false)
  def setAwake(b: Boolean) = set("awake", b)

  def seePlayer(player: Spatial) = {
    val lastSeen = player.getLocalTranslation.clone
    setLastSeenPlayer(lastSeen)
  }

  def controlUpdate(tpf: Float) = {
    if (isAwake) fsm.update(tpf)
    updateTurningAngle(tpf)
  }

  def updateTurningAngle(tpf: Float) {
    val pcon = getPhysicsControl
    val turnVelocity = FastMath.PI * tpf //100f // TODO Depends of FPS, need tpf
    val angle = turnTarget.angleBetween(pcon.getViewDirection)
    //    println(s"angle is $angle")
    if (angle > -turnVelocity && angle < turnVelocity) {
      pcon.setViewDirection(turnTarget)
    } else {
      //      println("interpolating")
      pcon.setViewDirection(pcon.getViewDirection.clone.interpolateLocal(turnTarget, 0.1f))
    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort) = {}
  override def getHealth = getOr[Int]("health", specs.health)
  override def setHealth(i: Int) = set("health", i)
  override def isDead = getState == States.Dead

  def getPhysicsControl = spatial.getControl(classOf[EnemyCharacterControl])
  def getPosition = spatial.getLocalTranslation
  def getDirection = getPhysicsControl.getViewDirection()

  def state = fsm.state
  def getState = {
    if (!hasState) {
      set("state", States.Idle.s)
    }
    get[String]("state")
  }

  def setState(s: States.State) = {
    println(s"State change: ${getOr("state", "-")} -> ${s.s}")
    set("state", s.s)
  }
  def hasState = has("state")

  def getLastSeenPlayer = getOr("lastSeenPlayer", Vector3f.ZERO)
  def setLastSeenPlayer(pos: Vector3f) = set("lastSeenPlayer", pos)
  def hasSeenPlayer = has("lastSeenPlayer")

  override def reactToHit(dir: Vector3f) {
    turn(dir.negate())
  }

  def reactToNoise(pos: Vector3f) {
    if (getState == States.Idle.s) {
      println("I heard you...")
      turn((getPosition subtract pos) negateLocal)
    }
  }

  def die = {
    if (!isDead) {
      println("enemy: me muero")
      dropWeapon
      fsm.setState(States.Dead)
    }
  }

  def dropWeapon = {
    weapon.spatial.setLocalTranslation(weapon.spatial.getWorldTranslation)
    weapon.spatial.setLocalRotation(spatial.getWorldRotation())
    weapon.spatial.rotate(0, FastMath.HALF_PI, 0)
    weapon.spatial.getControl(classOf[EnemyWeaponControl])
      .dropItem(weapon.spatial, spatial.getParent, getPhysicsControl.getPhysicsSpace)
    //    EventHub.trigger(ItemDropped(weapon.spatial))
  }

  def playerIsNearby(player: Spatial) = {
    val ghost = spatial.getControl(classOf[GhostControl])
    if (ghost.getPhysicsSpace() == null) {
      println(s"Player is nearby enemy, activating ghost")
      getPhysicsControl.getPhysicsSpace().add(ghost)
      ghost.setEnabled(true)
    }
  }

  def canSeePlayer = canSeePlayerChecker.checkValue

  val canSeePlayerChecker = new TimedChecker(doCanSeePlayer, 100L)
  def doCanSeePlayer() = {
    val height = new Vector3f(0, 4, 0)
    def addHeight(p: Vector3f) = p.add(height)
    val rayTest = getPhysicsControl.getPhysicsSpace.rayTest(
      addHeight(getPosition),
      addHeight(getLastSeenPlayer),
      new ArrayList())
    !rayTest.isEmpty && getClosest(rayTest.toList) == NodeId.player
  }

  def isPlayerTooClose() = {
    getPosition.distance(getLastSeenPlayer) < 7f
  }

  def isPlayerInFront() = {
    val pos = getPosition
    val normal = getDirection.normalize

    //D = -(P1.x*N.x + P1.y*N.y + P1.z*N.z)
    val d = -(pos dot normal)
    val plane = new Plane(normal, d)
    val dist = (normal dot getLastSeenPlayer) + d
    dist > 0
  }

  def turn(direction: Vector3f) {
    turnTarget.set(direction)
    turnTarget.normalizeLocal()
  }

  def turnToPlayer {
    val direction = getLastSeenPlayer.subtract(getPosition).normalizeLocal
    turn(direction)
  }

  def isPointingToThePlayer(): Boolean = {
    val pcon = getPhysicsControl
    val pos = getPosition.add(0, 5, 0)
    val direction = pcon.getViewDirection.normalize
    val rayTest = pcon.getPhysicsSpace.rayTest(
      pos.add(direction),
      pos.add(direction.mult(100f)),
      new ArrayList())
    if (rayTest.size() > 0) {
      getClosest(rayTest.toList) match {
        case NodeId.player => {
          pcon.setWalkDirection(Vector3f.ZERO)
          return true
        }
        case _ => // Do nothing
      }
    } else {
      println("raytest gave no results")
      //        setLastSeenPlayer(Vector3f.ZERO)
      //        setState(Idle)
    }
    false
  }
  def getClosest(list: List[PhysicsRayTestResult]): String = {
    val sorted = list.filter(r => {
      r.getCollisionObject.getUserObject.isInstanceOf[Node] && r.getCollisionObject.getUserObject != spatial
    }).sortBy(_.getHitFraction)
    if (sorted.isEmpty) "nothing"
    else sorted.head.getCollisionObject.getUserObject.asInstanceOf[Node].getName
  }

}

