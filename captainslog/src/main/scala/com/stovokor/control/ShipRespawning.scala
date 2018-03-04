package com.stovokor.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.jme3.scene.Spatial.CullHint

class ShipRespawning extends AbstractControl {

  var age = 0F

  def controlUpdate(tpf: Float) {
    if ((1000F * age / 250F).toInt % 2 == 0) {
      getSpatial.setCullHint(CullHint.Always)
    } else {
      getSpatial.setCullHint(CullHint.Never)
    }
    age = age + tpf
    if (age > 3f) {
      getSpatial.setCullHint(CullHint.Never)
      getSpatial.removeControl(this)
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}
}