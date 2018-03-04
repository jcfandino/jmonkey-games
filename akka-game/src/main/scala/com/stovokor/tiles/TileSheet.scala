package com.stovokor.tiles

class TileSheet(val file: String,
                val width: Int,
                val height: Int,
                val tileWidth: Int,
                val tileHeight: Int) {

  val tilesX = width / tileWidth
  val tilesY = height / tileHeight
  val percX = 1f / tilesX
  val percY = 1f / tilesY
  val numberOfTiles = tilesX / tilesY

  def getCoords(idx: Int): (Float, Float, Float, Float) = {
    val xo = percX * (idx % tilesX)
    val xe = xo + percX
    val yo = percY * (idx / tilesX)
    val ye = yo + percY
    (xo, xe, yo, ye)
  }
}

object TileCoordinatesResolver {
  val sheets = List(
    new TileSheet("Textures/Sheets/Street.png", 800, 608, 32, 32))

  def resolve(idx: Int) = {
    sheet(idx).getCoords(idx)
  }
  def sheet(idx: Int) = sheets(0) // TODO support many

}