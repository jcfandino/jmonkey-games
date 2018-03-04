package com.stovokor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.scene.Geometry

class ExplosionControl extends AbstractControl {

  val frames = 7
  val maxTtl = 1f
  var ttl = maxTtl

  def controlUpdate(tpf: Float) {
    ttl = ttl - tpf
    if (ttl <= 0f) {
      getSpatial.removeFromParent()
    }
    updateTextureCoordinates
  }

  val step = maxTtl / frames

  def updateTextureCoordinates = {
    val i = textureIndex
    val m = getSpatial.asInstanceOf[Geometry].getMesh
    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(i * step, 0f),
      new Vector2f(i * step + step, 0f),
      new Vector2f(i * step, 1f),
      new Vector2f(i * step + step, 1f)))
  }

  def textureIndex = {
    ((maxTtl - ttl) / step).toInt
  }

  def controlRender(x1: RenderManager, x2: ViewPort): Unit = {
  }
}