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
import com.jme3.scene.Node
import com.jme3.math.ColorRGBA

object ShipFactory {

  val box = new Box(K.shipWidth / 2f, K.shipHeight / 2f, 1f)
  val shieldBox = new Box(K.shieldWidth / 2f, K.shieldHeight / 2f, 2f)

  def create(implicit assetManager: AssetManager, inputManager: InputManager) = {
    val node = new Node("enterprise")
    node.attachChild(ship(assetManager))
    node.attachChild(shield(assetManager))
    node.attachChild(PhaserFactory.create(assetManager))
    node.addControl(ShipControl(inputManager))
    node.setLocalTranslation(K.middle, -K.shipWidth, 0f)
    node
  }

  def ship(assetManager: AssetManager) = {
    val geom = new Geometry("ship", box)
    val mat = MaterialFactory.create(assetManager, "Textures/enterprise-d.png")
    geom.setQueueBucket(Bucket.Transparent);
    geom.setMaterial(mat)
    geom
  }

  def shield(assetManager: AssetManager) = {
    val geom = new Geometry("shield", shieldBox)
    val mat = MaterialFactory.create(assetManager, "Textures/shield.png")
    geom.setQueueBucket(Bucket.Transparent);
    geom.setMaterial(mat)
    geom
  }
}