package com.stovokor.bomber.tiles

import com.jme3.math.ColorRGBA

object Tile {
  def apply(textureIndex: Int, height: Int) =
    new Tile(textureIndex, height)
}

class Tile(val textureIndex: Int, val height: Int) {
  // debug
  val color = ColorRGBA.randomColor()
}
