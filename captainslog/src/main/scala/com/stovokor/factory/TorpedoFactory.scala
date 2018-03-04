package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.Vector2f
import com.stovokor.K
import com.jme3.scene.shape.Box
import com.jme3.math.ColorRGBA
import com.stovokor.control.TorpedoControl
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.material.RenderState.BlendMode
import com.jme3.texture.Texture

object TorpedoFactory {

  val radius = 0.15f
  val box = new Box(radius, radius, 2f)

  def create(assetManager: AssetManager, posX: Float, posY: Float, friendly: Boolean) = {
    val torpedo = new Geometry("torpedo", box)
    torpedo.setLocalTranslation(new Vector3f(posX, posY, 2f))
    val mat = MaterialFactory.create(assetManager, "Textures/torpedo.png")
    torpedo.setMaterial(mat)
    torpedo.setQueueBucket(Bucket.Transparent);
    torpedo.addControl(new TorpedoControl(if (friendly) 1f else -1f))
    torpedo
  }
}