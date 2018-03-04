package com.stovokor.factory

import com.stovokor.tiles.MapBuilder
import scala.util.Random
import com.jme3.asset.AssetManager
import com.stovokor.tiles.Tile

object TestMapFactory {

  def create(assetManager: AssetManager, w: Int, h: Int) =
    new TestMapFactory(assetManager, w, h).create
}

class TestMapFactory(assetManager: AssetManager, val mapWidth: Int, val mapHeight: Int) {

  val block = 20
  val tiles9 = List(
    60, 61, 62,
    35, 36, 37,
    10, 11, 12)
  val asphalts = List(
    226, 226, 226, 226, 226, 226, 226, 226, 150, 151, 152)
  val streetLines = List(
    228, 228 + 25, 228 - 25)
  val roofs = List(50, 50, 50, 50, 50, 50, 50, 50, 50, 0, 25, 27, 30, 33, 101)
  val walls = 1 to 23 toList

  def create = {
    // textures

    val builder = MapBuilder(assetManager).init(mapWidth, mapHeight)
    val tiles = for (i <- 0 to mapWidth - 1; j <- 0 to mapHeight - 1) {
      val h =
        if (i == 0 || i == mapWidth - 1 || j == 0 || j == mapHeight - 1) 50
        else if (i % block > 4 && j % block > 4) buildingHeight(i, j)
        else r

      val tex =
        // wall
        if (i == 0 || i == mapWidth - 1 || j == 0 || j == mapHeight - 1) 50
        // roof
        else if (i % block > 4 && j % block > 4) randomOf(roofs)
        // sidewalk
        else if (i % block == 4 && j % block == 0) tiles9(0) // NW  
        else if (i % block == 4 && j % block > 4) tiles9(3) // W  
        else if (i % block == 4 && j % block == 4) tiles9(6) // SW  
        else if (i % block == 0 && j % block == 0) tiles9(2) // NE  
        else if (i % block == 0 && j % block > 4) tiles9(5) // E  
        else if (i % block == 0 && j % block == 4) tiles9(8) // SE  
        else if (i % block > 4 && j % block == 0) tiles9(1) // N  
        else if (i % block > 4 && j % block == 4) tiles9(7) // S  
        // street
        else if (i % block == 2 && j % block != 2) streetLines(1) // |  
        else if (j % block == 2 && i % block != 2) streetLines(0) // -  
        else if (j % block == 2 && i % block == 2) streetLines(2) // +
        else randomOf(asphalts)

      val wtex =
        if (i == 0 || i >= mapWidth - 1 || j == 0 || j >= mapHeight - 1) 9
        else buildingWallTex(i, j)

      builder.setTile(i, j, Tile(tex, wtex, h))
    }
    builder.build()
  }

  def buildingHeight(x: Int, y: Int) = {
    val bx = x / block
    val by = y / block
    val r = new Random(bx + 1000 * by)
    20 + r.nextInt(50)
  }
  def buildingWallTex(x: Int, y: Int) = {
    val bx = x / block
    val by = y / block
    val r = new Random(bx + 1000 * by)
    walls(r.nextInt(walls.size))
  }
  def randomOf(l: List[Int]): Int = l(Random.nextInt(l.size))

  def r = 1 //Random.nextInt(2) + 1

}