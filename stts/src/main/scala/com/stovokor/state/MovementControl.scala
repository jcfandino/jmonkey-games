package com.stovokor.state

import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.input.InputManager
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.math.Vector3f
import com.jme3.renderer.Camera
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.Control
import com.stovokor.domain.WeaponStashControl
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.PlayerInteracts
import com.stovokor.util.jme.PlayerShoot
import com.stovokor.domain.SwitchId
import com.stovokor.util.jme.SwitchPushed
import com.jme3.scene.Node
import com.stovokor.domain.NodeId
import com.jme3.math.FastMath
import com.jme3.math.FastMath.sin
import com.jme3.math.FastMath.cos
import com.jme3.input.controls.MouseAxisTrigger
import scala.collection.JavaConversions._
import com.stovokor.util.jme.LevelChange
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.LevelChange
import com.jme3.math.Quaternion
import com.stovokor.domain.PlayerCharacterControl

trait MovementControl extends Control {
  def positionAndDirection: (Vector3f, Vector3f)
}
/**
 * Player Movement Control - Moves a Player using the keyboard
 */
object PlayerMovementControl {
  def apply(cam: Camera, input: InputManager) = {
    val ctrl = new PlayerMovementControl(cam, input)
    ctrl.setupKeys
    EventHub.subscribeByType(ctrl, classOf[LevelChange])
    ctrl
  }
}
class PlayerMovementControl(val cam: Camera, inputManager: InputManager)
    extends AbstractControl
    with MovementControl
    with LogicEventListener
    with ActionListener {

  var up: Boolean = false
  var down: Boolean = false
  var left: Boolean = false
  var right: Boolean = false

  val walkFactor: Float = 60f
  val strafeFactor: Float = 40f
  val inertiaTime = .2f // in seconds
  val camHeight = 7f
  val camVerticalAmplitud = .2f
  val airControlFactor = .2f

  def controlUpdate(tpf: Float) = {
    setWalkDirection(tpf)
    setCameraLocationAndRotation
  }

  val walkDirection = new Vector3f() // Cache a vector

  def setWalkDirection(tpf: Float) {
    walkDirection.set(0, 0, 0)

    val camDir = cam.getDirection.clone.setY(0f).normalizeLocal.multLocal(walkFactor)
    val camLeft = cam.getLeft.mult(strafeFactor)

    if (left) walkDirection.addLocal(camLeft)
    if (right) walkDirection.addLocal(camLeft.negate())
    if (up) walkDirection.addLocal(camDir)
    if (down) walkDirection.addLocal(camDir.negate())

    // Apply inertia
    walkDirection.subtractLocal(physics.getWalkDirection)
    walkDirection.multLocal(Math.min(tpf / inertiaTime, 1f))

    // Air control: If not on ground, apply movement but dampened
    if (!physics.isOnGround()) {
      walkDirection.multLocal(airControlFactor)
    }
    physics.setWalkDirection(physics.getWalkDirection.add(walkDirection))
  }

  def setCameraLocationAndRotation {
    limitCameraRotation
    val camNode = spatial.asInstanceOf[Node].getChild(NodeId.camera)
    camNode.getLocalRotation().set(cam.getRotation())
    camNode.setLocalTranslation(0f, verticalCamOffset, 0f)
    cam.setLocation(camNode.getWorldTranslation)
  }

  val tmpQuat = new Quaternion
  val maxCameraAngle = FastMath.HALF_PI * .9f
  val camAngles = new Array[Float](3)

  def limitCameraRotation {
    cam.getRotation().toAngles(camAngles);
    if (camAngles(0) > maxCameraAngle) {
      camAngles(0) = maxCameraAngle
      cam.setRotation(tmpQuat.fromAngles(camAngles))
    } else if (camAngles(0) < -maxCameraAngle) {
      camAngles(0) = -maxCameraAngle
      cam.setRotation(tmpQuat.fromAngles(camAngles))
    }
  }

  def verticalCamOffset = {
    val pos = spatial.getLocalTranslation()
    camHeight + camVerticalAmplitud * sin(pos.x / 4f) + camVerticalAmplitud * cos(pos.z / 4f)
  }

  def physics = spatial.getControl(classOf[PlayerCharacterControl])

  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  def setupKeys() = {
    inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W))
    inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S))
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE))
    inputManager.addMapping("Use", new KeyTrigger(KeyInput.KEY_E))
    inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("NextWeapon", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true))
    inputManager.addMapping("PrevWeapon", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false))
    inputManager.addMapping("Weapon1", new KeyTrigger(KeyInput.KEY_1))
    inputManager.addMapping("Weapon2", new KeyTrigger(KeyInput.KEY_2))
    inputManager.addMapping("Weapon3", new KeyTrigger(KeyInput.KEY_3))
    inputManager.addMapping("Weapon4", new KeyTrigger(KeyInput.KEY_4))
    inputManager.addMapping("Weapon5", new KeyTrigger(KeyInput.KEY_5))
    inputManager.addMapping("Weapon6", new KeyTrigger(KeyInput.KEY_6))
    inputManager.addMapping("Weapon7", new KeyTrigger(KeyInput.KEY_7))
    inputManager.addMapping("Weapon8", new KeyTrigger(KeyInput.KEY_8))
    inputManager.addMapping("Weapon9", new KeyTrigger(KeyInput.KEY_9))
    inputManager.addMapping("Weapon0", new KeyTrigger(KeyInput.KEY_0))
    inputManager.addMapping("F1", new KeyTrigger(KeyInput.KEY_F1))
    inputManager.addMapping("F2", new KeyTrigger(KeyInput.KEY_F2))

    inputManager.addListener(this, "Lefts")
    inputManager.addListener(this, "Rights")
    inputManager.addListener(this, "Ups")
    inputManager.addListener(this, "Downs")
    inputManager.addListener(this, "Jump")
    inputManager.addListener(this, "Use")
    inputManager.addListener(this, "Shoot")
    inputManager.addListener(this, "NextWeapon")
    inputManager.addListener(this, "PrevWeapon")
    inputManager.addListener(this, "Weapon1")
    inputManager.addListener(this, "Weapon2")
    inputManager.addListener(this, "Weapon3")
    inputManager.addListener(this, "Weapon4")
    inputManager.addListener(this, "Weapon5")
    inputManager.addListener(this, "Weapon6")
    inputManager.addListener(this, "Weapon7")
    inputManager.addListener(this, "Weapon8")
    inputManager.addListener(this, "Weapon9")
    inputManager.addListener(this, "Weapon0")
    inputManager.addListener(this, "F1")
    inputManager.addListener(this, "F2")
  }

  def onEvent(e: GameEvent) = e match {
    case LevelChange(_, _) => inputManager.removeListener(this)
  }

  val weaponPattern = "Weapon([0-9])".r

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Lefts", _) => left = value
    case ("Rights", _) => right = value
    case ("Ups", _) => up = value
    case ("Downs", _) => down = value
    case ("Jump", true) => physics.jump
    case ("Shoot", _) => EventHub.trigger(PlayerShoot(value))
    case ("NextWeapon", true) => weaponNext
    case ("PrevWeapon", true) => weaponPrev
    case ("Use", true) => EventHub.trigger(PlayerInteracts())
    case ("F1", false) => EventHub.trigger(SwitchPushed(SwitchId.levelExitA))
    case ("F2", false) => EventHub.trigger(SwitchPushed(SwitchId.levelExitB))
    case (weaponPattern(d), false) => weaponNumber(d.toInt)
    case _ =>
  }

  def positionAndDirection = (cam.getLocation, cam.getDirection)

  def weaponNext = weaponStash.switchWeaponNext
  def weaponPrev = weaponStash.switchWeaponPrev
  def weaponNumber(number: Int) = {
    println(s"Weapon $number")
    weaponStash.switchWeaponNext
  }
  def weaponStash = spatial.getControl(classOf[WeaponStashControl])
}
