package com.stovokor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.scene.Geometry

class ExplosionControl(sheetSizeX: Int, sheetSizeY: Int, maxTtl: Float) extends AbstractControl {

  var ttl = maxTtl

  def controlUpdate(tpf: Float) {
    ttl = ttl - tpf
    if (ttl <= 0f) {
      getSpatial.removeFromParent()
    }
    updateTextureCoordinates
  }

  val stepX = 1f / sheetSizeX
  val stepY = 1f / sheetSizeY

  def updateTextureCoordinates = {
    val (i, j) = textureIndex
    val m = getSpatial.asInstanceOf[Geometry].getMesh
    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(i * stepX, j * stepY),
      new Vector2f(i * stepX + stepX, j * stepY),
      new Vector2f(i * stepX, j * stepY + stepY),
      new Vector2f(i * stepX + stepX, j * stepY + stepY)))
  }

  def textureIndex = {
    val n = ((sheetSizeX * sheetSizeY / maxTtl) * (maxTtl - ttl)).toInt
    (n % sheetSizeX, -1 + sheetSizeY - n / sheetSizeX)
  }

  def controlRender(x1: RenderManager, x2: ViewPort): Unit = {
  }
}