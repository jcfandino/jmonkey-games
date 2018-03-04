package com.stovokor.tiles

import com.jme3.math.ColorRGBA

object Tile {
  def apply(textureIndex: Int, wallTextureIndex: Int, height: Int) =
    new Tile(textureIndex, wallTextureIndex, height)
}

class Tile(val textureIndex: Int, val wallTextureIndex: Int = 2, val height: Int) {
  // debug
  val color = ColorRGBA.randomColor()
}
