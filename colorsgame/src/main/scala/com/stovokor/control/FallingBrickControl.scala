package com.stovokor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.ColorRGBA
import com.stovokor.K

object FallingBrickControl {
  def apply(color: ColorRGBA, brickSpeed: Float) = new FallingBrickControl(color, brickSpeed)
}

class FallingBrickControl(val color: ColorRGBA, val brickSpeed: Float) extends AbstractControl with HasColor {

  def controlUpdate(tpf: Float) {
    getSpatial.move(0, -brickSpeed * tpf, 0)
  }

  def controlRender(r: RenderManager, vp: ViewPort) {}
}
