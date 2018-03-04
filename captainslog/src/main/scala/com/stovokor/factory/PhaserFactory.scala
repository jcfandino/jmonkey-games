package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.input.InputManager
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture
import com.stovokor.K
import com.stovokor.control.ShipControl
import com.jme3.math.ColorRGBA
import com.jme3.scene.Spatial.CullHint
import com.jme3.math.Vector3f

object PhaserFactory {

  val box = new Box(
    new Vector3f(-0.015f, 0f, -1f),
    new Vector3f(0.015f, 10f, 0f))

  def create(implicit assetManager: AssetManager) = {
    val geom = new Geometry("phaser", box)
    val mat = MaterialFactory.create(assetManager, "Textures/phaser.png")
    geom.setMaterial(mat)
    geom.setCullHint(CullHint.Always) // Hide it at first
    geom.setLocalTranslation(0, 0, 0)
    geom
  }
}