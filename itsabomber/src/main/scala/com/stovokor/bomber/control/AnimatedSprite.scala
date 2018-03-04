package com.stovokor.bomber.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.scene.Geometry
import com.stovokor.bomber.state.GameStatus
import com.jme3.scene.Spatial

trait AnimatedSprite {

  def sheetSizeX: Int
  def sheetSizeY: Int
  def maxTtl: Float
  def getSpriteSpatial: Spatial

  var ttl = maxTtl

  def decreseTtl(tpf: Float) {
    ttl = ttl - tpf
  }

  def reset {
    ttl = maxTtl
  }

  lazy val stepX = 1f / sheetSizeX
  lazy val stepY = 1f / sheetSizeY

  def updateTextureCoordinates = {
    val (i, j) = textureIndex
    val m = getSpriteSpatial.asInstanceOf[Geometry].getMesh
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

}