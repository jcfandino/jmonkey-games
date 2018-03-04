package com.stovokor.bomber.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.scene.Geometry
import com.stovokor.bomber.state.GameStatus
import com.jme3.bullet.control.GhostControl

class ExplosionControl(val sheetSizeX: Int, val sheetSizeY: Int, val maxTtl: Float)
    extends AbstractControl
    with AnimatedSprite {

  def getSpriteSpatial = getSpatial

  var friendly = true

  def controlUpdate(tpf: Float) {
    decreseTtl(tpf)
    if (ttl <= 0f) {
      GameStatus.recycleExplosion(getSpatial)
    }
    val ghost = getSpatial.getControl(classOf[GhostControl])
    if (ghost != null) {
      ghost.setEnabled(ttl > maxTtl - 0.1f)
    }
    updateTextureCoordinates
  }

  def setFriendly(is: Boolean) {
    friendly = is
  }

  def controlRender(x1: RenderManager, x2: ViewPort): Unit = {
  }
}