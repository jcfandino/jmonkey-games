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

object PadControl {
  def apply(inputManager: InputManager) = {
    val ctrl = new PadControl(inputManager)
    ctrl.setupKeys
    ctrl
  }
}

class PadControl(inputManager: InputManager) extends AbstractControl
    with ActionListener
    with AnalogListener
    with HasColor {

  var red, green, blue: Boolean = false
  var left, right = false

  val color = new ColorRGBA
  val matColor = new ColorRGBA

  var vertPos = K.padPosY

  def c(b: Boolean, max: Float = 1f, min: Float = 0f) = if (b) max else min

  def controlUpdate(tpf: Float) {
    color.set(c(red), c(green), c(blue), 1f)
    updateMaterial
    getSpatial.asInstanceOf[Geometry].getMaterial.setColor("Color", matColor)

    val pos = getSpatial.getLocalTranslation
    var posX =
      if (left) {
        Math.max(K.padMinPos, pos.x - K.padSpeed * tpf)
      } else if (right) {
        Math.min(K.padMaxPos, pos.x + K.padSpeed * tpf)
      } else {
        pos.x
      }
    getSpatial.setLocalTranslation(posX, interpolate(pos.y, vertPos), pos.z)
  }

  def interpolate(a: Float, b: Float) = {
    if ((a - b).abs < 0.01f) b
    else (a + b) / 2f
  }

  def updateMaterial {
    if (red || green || blue) {
      matColor.set(c(red, min = 0.0f), c(green, min = 0.0f), c(blue, min = 0.0f), 1f)
    } else {
      matColor.set(ColorRGBA.Gray)
    }
  }

  def moveUp(d: Float = K.padHeight) {
    vertPos = vertPos + d
  }

  def moveDown(d: Float = K.padHeight) {
    vertPos = Math.max(vertPos - d, K.padPosY)
  }

  def resetPos() {
    vertPos = K.padPosY
  }

  def controlRender(r: RenderManager, vp: ViewPort) {}

  def setupKeys() = {
    inputManager.addMapping("Red", new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("Green", new KeyTrigger(KeyInput.KEY_S))
    inputManager.addMapping("Blue", new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT))

    inputManager.addMapping("MouseLeft",
      new MouseAxisTrigger(MouseInput.AXIS_X, true))
    inputManager.addMapping("MouseRight",
      new MouseAxisTrigger(MouseInput.AXIS_X, false))

    inputManager.addListener(this, "Red")
    inputManager.addListener(this, "Green")
    inputManager.addListener(this, "Blue")
    inputManager.addListener(this, "Left")
    inputManager.addListener(this, "Right")
    inputManager.addListener(this, "MouseLeft")
    inputManager.addListener(this, "MouseRight")
  }

  def onAnalog(name: String, value: Float, tpf: Float) {
    name match {
      case "MouseLeft"  => moveHorizontally(-value * K.padSpeed)
      case "MouseRight" => moveHorizontally(value * K.padSpeed)
      case _            =>
    }
  }

  def moveHorizontally(delta: Float) {
    val oldPos = getSpatial.getLocalTranslation
    val newPos = Math.max(K.padMinPos, Math.min(oldPos.x + delta, K.padMaxPos))
    getSpatial.setLocalTranslation(newPos, oldPos.y, oldPos.z)
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Red", _)   => red = value
    case ("Green", _) => green = value
    case ("Blue", _)  => blue = value
    case ("Left", _)  => left = value
    case ("Right", _) => right = value
    case _            =>
  }

}