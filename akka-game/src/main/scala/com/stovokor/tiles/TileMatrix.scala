package com.stovokor.tiles

object TileMatrix {
  def apply(tiles: Array[Array[Tile]]) = new TileMatrix(tiles)
}

class TileMatrix(tiles: Array[Array[Tile]]) {
  
  def zipWithIndex() = {
     tiles.map(a => a.zipWithIndex).zipWithIndex 
  }
}