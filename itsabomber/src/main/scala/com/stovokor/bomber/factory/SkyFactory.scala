package com.stovokor.bomber.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.stovokor.bomber.tiles.SpriteSheet
import com.jme3.scene.Node

object SkyFactory {
  val width = 12f
  val height = 10f
  def create(assetManager: AssetManager) = {
    val sheet = SpriteSheet("Textures/sky05.png", 1, 1)

    val sprite1 = SpriteFactory.create(assetManager, "sky", sheet,
      width, height, new Vector3f(3f, height / 2f, -0.1f), false)
    val sprite2 = SpriteFactory.create(assetManager, "sky", sheet,
      width, height, new Vector3f(15f, height / 2f, -0.1f), false)

    val node = new Node("sky")
    node.attachChild(sprite1)
    node.attachChild(sprite2)
    node
  }
}