package com.stovokor.bomber.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.stovokor.bomber.state.GameStatus
import com.jme3.math.Vector3f

object FlakControl {
  def apply(attitude: Float) = new FlakControl(attitude)
}

class FlakControl(attitude: Float) extends AbstractControl {

  def controlUpdate(tpf: Float) {
    if (getSpatial != null && getSpatial.getLocalTranslation.y > attitude) {
      explode()
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def explode() {
    GameStatus.explode(getSpatial.getLocalTranslation, friendly = false)
    GameStatus.recycleShot(getSpatial)
  }
}
