package com.stovokor.state

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.scene.Node
import com.stovokor.K

class IndicatorBarControl(max: Float, f: => Float) extends AbstractControl {

  def controlUpdate(tpf: Float) {
    setLevel(f)
  }

  def setLevel(value: Float) {
    var g = getSpatial.asInstanceOf[Node].getChild("bar")
    val t = g.getLocalTransform
    t.setScale(value / max, 1f, 1f)
    t.setTranslation(-(1f - value / max) * K.barWidth / 2f, 0, 0)
    g.setLocalTransform(t)
  }

  def controlRender(r: RenderManager, v: ViewPort) {
  }

}