package com.stovokor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.scene.Geometry
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils

class StarfieldControl(val speed: Float) extends AbstractControl {

  var t = 0f
  def controlUpdate(tpf: Float) {
    t += tpf
    updateTextureCoordinates()
  }

  def updateTextureCoordinates() = {
    val m = getSpatial.asInstanceOf[Geometry].getMesh

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(0f, t / speed),
      new Vector2f(1f, t / speed),
      new Vector2f(0f, t / speed + 1f),
      new Vector2f(1f, t / speed + 1f)))
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}
}