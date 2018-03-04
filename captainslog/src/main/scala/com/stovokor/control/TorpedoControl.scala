package com.stovokor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.stovokor.K

class TorpedoControl(dir:Float) extends AbstractControl {  
  
  def controlUpdate(tpf: Float) {
    getSpatial.move(0f, dir * tpf * K.torpedoSpeed, 0f)
    val y = getSpatial.getLocalTranslation.x
    if (y > K.enemyStart) {
      getSpatial.removeFromParent()
    }
  }
  
  def controlRender(r: RenderManager, v: ViewPort) {}
}