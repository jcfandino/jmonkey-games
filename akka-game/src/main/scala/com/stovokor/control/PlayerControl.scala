package com.stovokor.control

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
import com.jme3.bullet.control.BetterCharacterControl

object PlayerControl {

  def apply(inputManager: InputManager) = {
    val ctrl = new PlayerControl(inputManager)
    ctrl.setupKeys
    ctrl
  }

}

class PlayerControl(inputManager: InputManager)
    extends AbstractControl
    with ActionListener {

  def controlUpdate(tpf: Float) {
    val direction = new Vector3f()
    if (left) direction.addLocal(-1, 0, 0)
    if (right) direction.addLocal(1, 0, 0)
    if (up) direction.addLocal(0, 1, 0)
    if (down) direction.addLocal(0, -1, 0)
    direction.multLocal(10 * tpf)
    if (direction.length() > 0f) {
      println(s"moving $direction")
      spatial.move(direction)
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def setupKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP))
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN))
    inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_H))
    inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R), new KeyTrigger(KeyInput.KEY_RETURN))
    inputManager.addListener(this, "Left")
    inputManager.addListener(this, "Right")
    inputManager.addListener(this, "Up")
    inputManager.addListener(this, "Down")
    inputManager.addListener(this, "Space")
    inputManager.addListener(this, "Reset")
  }

  var (up, down, left, right) = (false, false, false, false)

  def onAction(binding: String, value: Boolean, tpf: Float) {
    (binding, value) match {
      case ("Left", _)  => left = value
      //      case ("Left", false)  => direction.add(1, 0, 0)
      case ("Right", _) => right = value
      //      case ("Right", false) => direction.add(-1, 0, 0)
      case ("Up", _)    => up = value
      //      case ("Up", false)    => direction.add(0, -1, 0)
      case ("Down", _)  => down = value
      //      case ("Down", false)  => direction.add(0, 1, 0)
      case ("Space", true) => {
      }
      case ("Space", false) => {
      }
      case ("Reset", false) => {
      }
      case _ =>
    }
  }

  def body = getSpatial.getControl(classOf[BetterCharacterControl])
  //  def player = getSpatial.getControl(classOf[VehicleControl])
}