package com.stovokor.control

import com.jme3.input.InputManager
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.math.ColorRGBA
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Geometry
import com.jme3.scene.control.AbstractControl
import com.stovokor.K
import com.jme3.input.controls.AnalogListener
import com.jme3.scene.Node
import com.jme3.input.controls.MouseButtonTrigger

object ShipControl {

  def apply(inputManager: InputManager) = {
    val ctrl = new ShipControl(inputManager)
    ctrl.setupKeys
    ctrl
  }

}

class ShipControl(inputManager: InputManager) extends AbstractControl
    with HasShield
    with HasWeapons
    with ActionListener
    with AnalogListener {

  var left, right = false

  var vertPos = K.shipPosY

  def c(b: Boolean, max: Float = 1f, min: Float = 0f) = if (b) max else min

  def controlUpdate(tpf: Float) {
    val pos = getSpatial.getLocalTranslation
    var posX =
      if (left) {
        Math.max(K.shipMinPos, pos.x - K.shipSpeed * tpf)
      } else if (right) {
        Math.min(K.shipMaxPos, pos.x + K.shipSpeed * tpf)
      } else {
        pos.x
      }
    getSpatial.setLocalTranslation(posX, interpolate(pos.y, vertPos), pos.z)
    updateWeapons(tpf)
    updateShield(tpf)
  }

  def interpolate(a: Float, b: Float) = {
    if ((a - b).abs < 0.01f) b
    else (a + b) / 2f
  }

  def respawn() {
    respawnHealth
    phaserEnergy = K.phaserMaxEnergy
    torpedoes = K.initialTorpedos
    getSpatial.setLocalTranslation(K.middle, K.shipPosY, 10f)
    getSpatial.addControl(new ShipRespawning)
    getSpatial.setLocalTranslation(K.middle, -K.shipWidth, 0f)
  }

  def controlRender(r: RenderManager, vp: ViewPort) {}

  def setupKeys() = {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE),
      new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("Enter", new KeyTrigger(KeyInput.KEY_RETURN),
      new KeyTrigger(KeyInput.KEY_F),
      new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))

    inputManager.addMapping("MouseLeft",
      new MouseAxisTrigger(MouseInput.AXIS_X, true))
    inputManager.addMapping("MouseRight",
      new MouseAxisTrigger(MouseInput.AXIS_X, false))

    inputManager.addListener(this, "Left")
    inputManager.addListener(this, "Right")
    inputManager.addListener(this, "Space")
    inputManager.addListener(this, "Enter")
    inputManager.addListener(this, "MouseLeft")
    inputManager.addListener(this, "MouseRight")
  }

  def moveHorizontally(delta: Float) {
    val oldPos = getSpatial.getLocalTranslation
    val newPos = Math.max(K.shipMinPos, Math.min(oldPos.x + delta, K.shipMaxPos))
    getSpatial.setLocalTranslation(newPos, oldPos.y, oldPos.z)
  }

  var phaserEnergy = K.phaserMaxEnergy

  def changePhaserEnery(d: Float) {
    phaserEnergy = Math.max(0f, Math.min(K.phaserMaxEnergy, phaserEnergy + d))
  }

  var torpedoes = K.initialTorpedos

  def changeTorpedos(n: Int) {
    torpedoes = Math.max(0, Math.min(K.initialTorpedos, torpedoes + n))
  }

  def getNode: Node = {
    getSpatial.asInstanceOf[Node]
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Left", _)  => left = value
    case ("Right", _) => right = value
    case ("Space", _) => shootingPhaser = value
    case ("Enter", _) => {
      shootingTorpedo = value
      if (!value) readyToShootTorpedo = true
    }
    case _ =>
  }

  def onAnalog(name: String, value: Float, tpf: Float) {
    name match {
      case "MouseLeft"  => moveHorizontally(-value * K.shipSpeed)
      case "MouseRight" => moveHorizontally(value * K.shipSpeed)
      case _            =>
    }
  }

  def initialHealth = K.shipInitialHealth
  def initialShield = K.shipInitialShield

}