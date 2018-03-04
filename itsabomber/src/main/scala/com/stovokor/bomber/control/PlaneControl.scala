package com.stovokor.bomber.control

import com.jme3.bullet.control.VehicleControl
import com.jme3.input.InputManager
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.math.Matrix3f
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.bullet.control.RigidBodyControl
import com.stovokor.bomber.state.GameStatus
import com.stovokor.bomber.factory.ShotType

object PlaneControl {

  def apply(inputManager: InputManager) = {
    val ctrl = new PlaneControl(inputManager)
    ctrl.setupKeys
    ctrl
  }

}

class PlaneControl(inputManager: InputManager)
    extends AbstractControl
    with ActionListener
    with Shoots {

  val initialHealth = 1000f
  var health = initialHealth

  var up, down, forward, backward = false
  var drop, shoot = false

  val shootDirection = Vector3f.UNIT_X
  val timeBetweenShots = 0.1f
  val shootingOffset = new Vector3f(1f, 0f, 0f)
  val shotType = ShotType.Light

  var dropFlipFlop = true

  def controlUpdate(tpf: Float) {
    val pos = getSpatial.getLocalTranslation
    val move = new Vector3f()
    if (up) move.addLocal(0, tpf * 1f, 0)
    if (down) move.addLocal(0, tpf * -1f, 0)
    if (forward) move.addLocal(tpf * 1f, 0, 0)
    if (backward) move.addLocal(tpf * -1f, 0, 0)
    if (drop) {
      if (dropFlipFlop) GameStatus.dropBomb(pos)
      dropFlipFlop = false
    }
    updateShooting(tpf)
    getSpatial.setLocalTranslation(
      Math.max(1.5f, Math.min(10f, pos.x + move.x)),
      Math.max(0f, Math.min(8f, pos.y + move.y)),
      pos.z)
    getPhysicBody.setPhysicsLocation(getSpatial.getLocalTranslation)
  }

  def hit(points: Float) {
    health = Math.max(0, health - points)
  }

  def getPhysicBody = getSpatial.getControl(classOf[RigidBodyControl])

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def setupKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP))
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN))
    inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_H))
    inputManager.addMapping("Ctrl", new KeyTrigger(KeyInput.KEY_LCONTROL), new KeyTrigger(KeyInput.KEY_RCONTROL))
    inputManager.addListener(this, "Left")
    inputManager.addListener(this, "Right")
    inputManager.addListener(this, "Up")
    inputManager.addListener(this, "Down")
    inputManager.addListener(this, "Space")
    inputManager.addListener(this, "Ctrl")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) {
    (binding, value) match {
      case ("Left", _)  => backward = value
      case ("Right", _) => forward = value
      case ("Up", _)    => up = value
      case ("Down", _)  => down = value
      case ("Ctrl", _)  => shoot = value
      case ("Space", _) => {
        dropFlipFlop = !drop || dropFlipFlop
        drop = value
      }
      case _ =>
    }
  }

  def resetPosition() {
    val pos = getSpatial.getLocalTranslation
    getSpatial.setLocalTranslation(2f, 6f, pos.z)
  }
}