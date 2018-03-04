package com.stovokor.domain

import scala.collection.JavaConversions._
import scala.compat.Platform
import com.jme3.bullet.BulletAppState
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.stovokor.STTS
import com.stovokor.util.jme.GameEvent
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.audio.AudioNode
import com.jme3.light.PointLight
import com.jme3.math.ColorRGBA
import com.jme3.bullet.control.PhysicsControl
import com.jme3.bullet.control.AbstractPhysicsControl
import com.jme3.collision.CollisionResults
import com.jme3.math.Ray
import com.stovokor.util.math.Random
import com.stovokor.util.math.Dist
import com.jme3.math.FastMath.sin
import com.jme3.math.FastMath.cos
import com.stovokor.util.jme.LogicEventListener
import com.jme3.effect.ParticleEmitter
import com.jme3.material.Material
import com.jme3.effect.ParticleMesh.Type
import com.jme3.effect.shapes.EmitterSphereShape
import com.stovokor.domain.item.CanBePickedUp
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.LoudSound
import com.stovokor.util.jme.JmeExtensions._

class Weapon(
    val id: String,
    model: Node,
    control: WeaponControl,
    val tech: Technology,
    val spec: TechSpecs,
    val sounds: WeaponSounds) extends Entity(model, control) {

  //  def this(id: String, model: Node, tech: Technology, spec: TechSpecs, sounds: WeaponSounds) = {
  //    this(id, model, new NullWeaponControl, tech, spec, sounds)
  //  }

  override def clone = {
    val snds = sounds.clone
    val m = model.clone().asNode
    snds.attachTo(m)
    val c = m.getControl(classOf[WeaponControl])
    new Weapon(id, m, c, tech, spec, snds)
  }

  // workaround because the control changes after created
  def weaponControl = spatial.getControl(classOf[WeaponControl])
}

abstract class WeaponControl(var pointingAt: (() => (Vector3f, Vector3f)))
    extends AbstractControl {

  var lastShot = 0L
  var inRecoil = false
  var triggerPulled = false
  var triggerPulledTime = 0f

  def controlUpdate(tpf: Float) = {
    val time = Platform.currentTime
    if (enoughTimePassed(time, lastShot) && inRecoil) {
      inRecoil = false
      if (triggerPulled && weapon.spec.automatic) {
        triggerPulledTime = triggerPulledTime + tpf
        shootIfNeeded
      }
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  def pullTheTrigger = {
    triggerPulled = true
    triggerPulledTime = 0f
    shootIfNeeded
  }

  def looseTheTrigger {
    triggerPulled = false
  }

  def shootIfNeeded {
    val time = Platform.currentTime
    if (enoughTimePassed(time, lastShot) && hasEnoughAmmo) {
      lastShot = time
      inRecoil = true
      checkHit
      decreaseAmmo
      flash
      playSound
    }
  }
  val rnd = Random()
  def checkHit = {
    val (location, direction) = pointingAt()
    val deviated = deviatedDir(direction)
    // TODO reference to stts. Replace by using an Event and handle in the InAppState
    val physicsState = STTS.getStateManager().getState(classOf[BulletAppState])
    weapon.tech.checkHit(physicsState, location, deviated, weapon.spec)
  }

  def deviatedDir(direction: Vector3f) = if (triggerPulledTime == 0f) direction else {
    val dev = weapon.spec.precision(triggerPulledTime)
    direction.add(Dist.normalFloat(rnd, -dev, dev),
      Dist.normalFloat(rnd, -dev, dev),
      Dist.normalFloat(rnd, -dev, dev))
  }

  def flash {
    if (!spatial.hasControl(classOf[FlashControl])) {
      spatial.addControl(new FlashControl)
    }
    val ctrl = spatial.getControl(classOf[FlashControl])
    ctrl.flash
  }

  def playSound {
    weapon.sounds.shoot.playInstance
    val (location, _) = pointingAt()
    EventHub.trigger(LoudSound(location))
  }

  def ammoType = weapon.tech.ammoType

  def enoughTimePassed(time: Long, lastShot: Long) = {
    time - lastShot > weapon.spec.delayTime
  }

  def setPositionAndDirectionFunction(f: () => (Vector3f, Vector3f)) {
    pointingAt = f
  }

  def weapon: Weapon
  def hasEnoughAmmo: Boolean
  def decreaseAmmo: Unit

}

class NullWeaponControl extends WeaponControl((() => (Vector3f.UNIT_Y, Vector3f.UNIT_Y))) {
  def hasEnoughAmmo = false
  def decreaseAmmo {}
  def weapon = null
}

class PlayerWeaponControl(stash: WeaponStashControl, pointingAt: (() => (Vector3f, Vector3f)))
    extends WeaponControl(pointingAt) {

  override def controlUpdate(tpf: Float) = {
    super.controlUpdate(tpf)
    setWeaponOffset
  }

  def setWeaponOffset {
    val pos = spatial.getWorldTranslation()
    val offset = -3f + .05f * (sin(pos.x / 8f) + cos(pos.z / 8f))
    spatial.setLocalTranslation(offset, -1.5f, -1f)
  }

  def hasEnoughAmmo = stash.getAmmo(ammoType) >= weapon.spec.ammoPerShot
  def decreaseAmmo { stash.decreaseAmmo(ammoType, weapon.spec.ammoPerShot) }

  def weapon = stash.current
}
class EnemyWeaponControl(val weapon: Weapon, pointingAt: (() => (Vector3f, Vector3f)))
    extends WeaponControl(pointingAt) with CanBePickedUp {

  val hasEnoughAmmo = true
  def decreaseAmmo {}

  def doPickup(player: PlayerControl): Boolean = {
    player.pickupWeapon(weapon)
  }
}

class WeaponSounds(val shoot: AudioNode) {

  override def clone = new WeaponSounds(shoot.clone)

  def attachTo(node: Node) {
    node.attachChild(shoot)
  }
}

class FlashControl extends AbstractControl {

  val point = {
    val p = new PointLight()
    p.setName("flashingLight")
    p.setColor(new ColorRGBA(1.0f, 1.0f, 0.9f, 1f) mult 2)
    p.setRadius(20)
    p
  }

  var time: Float = 0f

  def node = {
    def root(s: Node): Node = s.getParent match {
      case null => s
      case n => root(n)
    }
    root(spatial.getParent())
  }

  def flash {
    time = 0f
  }

  def controlUpdate(tpf: Float) {
    if (time == 0f) {
      println(s"Adding flash in ${spatial.getWorldTranslation()}")
      point.setPosition(spatial.getWorldTranslation())
      node.addLight(point)
      getFlashEmitter.emitAllParticles()
    }
    if (time > 0.06f) {
      node.removeLight(point)
    }
    time += tpf
  }

  def getFlashEmitter = {
    spatial.asInstanceOf[Node].getChild("flash").asInstanceOf[ParticleEmitter]
  }

  override def clone = {
    new FlashControl
  }

  def controlRender(rm: RenderManager, vp: ViewPort) = {}
}
