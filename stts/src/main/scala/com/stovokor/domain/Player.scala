package com.stovokor.domain

import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.Control
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.PlayerDied
import com.stovokor.util.jme.PlayerDied
import com.stovokor.util.jme.PlayerDied

import com.stovokor.util.jme.HasSpatialState
import com.stovokor.util.jme.HealthChange
import com.stovokor.util.jme.ArmorChange
import com.stovokor.state.CollisionGroups
import com.jme3.bullet.PhysicsSpace
import com.jme3.util.TempVars
import scala.collection.JavaConversions._
import com.stovokor.util.math.TimedChecker
import com.stovokor.state.PhysicsSchedulingAppState
import com.jme3.bullet.collision.PhysicsRayTestResult
import java.util.concurrent.Future
import com.stovokor.jme.PortedBetterCharacterControl
import java.util.ArrayList

class PlayerCharacter(
  model: Node,
  control: PlayerControl,
  val stash: WeaponStashControl,
  val ghosts: List[Control])
    extends Entity(model, control, Some(model)) {

  def physics = control.physics

}

class PlayerControl extends AbstractControl with CanBeHit with HasSpatialState {

  def controlUpdate(tpf: Float) = {
  }
  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  def physics = spatial.getControl(classOf[PlayerCharacterControl])
  def weaponStash = spatial.getControl(classOf[WeaponStashControl])

  override def getHealth = getOr("health", 0)
  override def setHealth(h: Int) = {
    set("health", h)
    EventHub.trigger(HealthChange(h))
  }
  override def getArmor = getOr("armor", 0)
  override def setArmor(a: Int) = {
    set("armor", a)
    EventHub.trigger(ArmorChange(a))
  }

  override def die = {
    println("player is dead!!!!! game over dude")
    EventHub.trigger(PlayerDied())
    //    setHealth(0)
  }
  override def isDead = getHealth <= 0

  def pickupWeapon(weapon: Weapon): Boolean = {
    println(s"Pickup weapon ${weapon.id}")
    weaponStash.store(weapon)
  }

  def pickupArmor(armor: Int): Boolean = {
    if (getArmor < 100) {
      println(s"Pickup armor $armor")
      setArmor(Math.min(100, getArmor + armor))
      true
    } else false
  }
  def pickupHealth(health: Int): Boolean = {
    if (getHealth < 100) {
      println(s"Pickup health $health")
      setHealth(Math.min(100, getHealth + health))
      true
    } else false
  }

  def pickupAmmo(ammoType: Ammo, amount: Int) = {
    weaponStash.increaseAmmo(ammoType, amount)
  }
}

object PlayerCharacterControl {
  def apply(radius: Float, height: Float, mass: Float) = {
    val control = new PlayerCharacterControl(radius, height, mass)
    control.getRigidBody.setCollisionGroup(CollisionGroups.level)
    control.getRigidBody.setCollideWithGroups(CollisionGroups.level)
    control.setGravity(new Vector3f(0, -4000, 0)) //-9.81f // doesnt work???
    control.setPhysicsDamping(0.9f)
    control.setJumpForce(new Vector3f(0f, 8000f, 0f))
    control
  }
}

class PlayerCharacterControl(radius: Float, height: Float, mass: Float)
//    extends BetterCharacterControl(radius, height, mass) {
    extends PortedBetterCharacterControl(radius, height, mass) {
  
  override def cloneForSpatial(spatial:Spatial) = {
        val control =
                new PlayerCharacterControl(radius, height, mass);
        control.setJumpForce(jumpForce)
        control
  }
  
  override def jmeClone() = new PlayerCharacterControl(radius, height, mass)

  override def setPhysicsSpace(aSpace: PhysicsSpace) = {
    if (aSpace == null && space != null)
      removePhysics(space)
    else
      super.setPhysicsSpace(aSpace)
  }

  def getRigidBody = rigidBody

  override def prePhysicsTick(space: PhysicsSpace, tpf: Float) {
    if (isOnGround) rigidBody.setFriction(0.6f)
    else rigidBody.setFriction(0f)
    super.prePhysicsTick(space, tpf)
  }

  var checkOnGroundFuture: Option[Future[List[PhysicsRayTestResult]]] = None

  override def checkOnGround() {
        onGround = checkOnGroundChecker.checkValue
//    checkOnGroundAsync
  }
  
  var onGroundFound = !false
  
  def checkOnGroundAsync() {
    //    onGround = checkOnGroundChecker.checkValue
    if (checkOnGroundFuture.isEmpty || checkOnGroundFuture.get.isDone) {
      //    val vars = TempVars.get()
      val location = new Vector3f
      val rayVector = new Vector3f
      val height = getFinalHeight()
      location.set(localUp).multLocal(height).addLocal(this.location)
      rayVector.set(localUp).multLocal(-height - 0.1f).addLocal(location)
      checkOnGroundFuture =
        Some(PhysicsSchedulingAppState.scheduleTest(location, rayVector, handleRayTestResult))
    }
    onGround = onGroundFound
  }

  def handleRayTestResult(results: List[PhysicsRayTestResult]) {
    println(s"handling ray test: ${results.size}")
    onGroundFound = results.exists(result => {
      val levelGroup = result.getCollisionObject.getCollisionGroup == CollisionGroups.level
      val notMe = !result.getCollisionObject().equals(rigidBody)
      levelGroup && notMe
    })
  }

  val checkOnGroundChecker = new TimedChecker(doCheckOnGround, 20L)

  def doCheckOnGround(): Boolean = {
    val vars = TempVars.get()
    val location = vars.vect1
    val rayVector = vars.vect2
    val height = getFinalHeight()
    location.set(localUp).multLocal(height).addLocal(this.location)
    rayVector.set(localUp).multLocal(-height - 0.1f).addLocal(location)
    val results = space.rayTest(location, rayVector, new ArrayList())
    vars.release()
    for (result <- results) {
      val levelGroup = result.getCollisionObject.getCollisionGroup == CollisionGroups.level
      val notMe = !result.getCollisionObject().equals(rigidBody)
      if (levelGroup && notMe) {
        return true
      }
    }
    return false
  }
}