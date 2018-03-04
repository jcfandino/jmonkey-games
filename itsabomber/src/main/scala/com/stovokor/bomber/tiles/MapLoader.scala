package com.stovokor.bomber.tiles

import com.jme3.asset.AssetManager
import com.flowpowered.noise.module.source.Perlin
import com.flowpowered.noise.NoiseQuality
import java.time.Instant
import scala.util.Random
import com.jme3.scene.Geometry
import com.stovokor.bomber.factory.EnemyFactory
import com.stovokor.bomber.factory.EnemyFactory
import com.jme3.scene.Node

class MapLoader(val assetManager: AssetManager) {

  val mapLength = 1000
  val mapHeight = 17

  def load() = {
    val builder = new MapBuilder(assetManager).init(mapLength, mapHeight)
    var enemies = new Node("enemies")
    for (i <- 0 to mapLength - 1; j <- 0 to mapHeight - 1) {
      val ground = nextHeight(i)
      val h = if (j < ground || j >= mapHeight + 1 || i > mapLength - 5) 1 else 0
      builder.setTile(i, j, Tile(2, h))
      if (i > 40 && j == mapHeight - 1) {
        // soldier
        if (Random.nextFloat() < 0.1f) {
          val enemy = EnemyFactory.create(assetManager, 0)
          enemies.attachChild(enemy)
          enemy.setLocalTranslation((i + .5f) * builder.tileWidth, (ground + 1) * builder.tileHeight, builder.tileThickness / 2f)
          println(s"placing soldier at ${enemy.getLocalTranslation}")
          // tank
        } else if (i % 8 == 0 && Random.nextFloat() < Math.min(1.0f, i * 0.01f)) {
          val g1 = nextHeight(i + 1)
          val g2 = nextHeight(i + 2)
          if (ground == g1 && ground == g2) {
            val enemy = EnemyFactory.create(assetManager, 2)
            enemies.attachChild(enemy)
            enemy.setLocalTranslation((i + 1.5f) * builder.tileWidth, (ground + 1) * builder.tileHeight, builder.tileThickness / 2f)
            println(s"placing tank at ${enemy.getLocalTranslation}")
          }
          // turret
        } else if (Random.nextFloat() < Math.min(.1f, i * 0.001f)) {
          val enemy = EnemyFactory.create(assetManager, 3)
          enemies.attachChild(enemy)
          enemy.setLocalTranslation((i + .5f) * builder.tileWidth, (ground + 1) * builder.tileHeight, builder.tileThickness / 2f)
          println(s"placing turrent at ${enemy.getLocalTranslation}")
        }
        // plane
        if (h < mapHeight - 6 && i % 20 == 0 && Random.nextFloat() < Math.min(0.6f, i * .002f)) {
          val enemy = EnemyFactory.create(assetManager, 1)
          val att = mapHeight - 6 + Random.nextInt(5)
          enemies.attachChild(enemy)
          enemy.setLocalTranslation((i + .5f) * builder.tileWidth, att * builder.tileHeight, builder.tileThickness / 2f)
          println(s"placing spitfire at ${enemy.getLocalTranslation}")
        }
      }
    }
    val mapRoot = builder.build()

    mapRoot.attachChild(enemies)
    mapRoot
  }

  def nextHeight(i: Int): Int = {
    val max = if (i < 20) 5 else mapHeight - 3
    val factor = perlin.getValue(i * .1f, 0, 0).toFloat // -1 ~ 1 ??
    Math.max(1, Math.min(mapHeight - 2, (factor.abs * max).toInt))
  }

  val perlin = {
    val p = new Perlin
    p.setSeed(Instant.now.getNano)
    p.setNoiseQuality(NoiseQuality.FAST)
    // no idea what I'm doing
    p.setFrequency(0.5)
    p.setLacunarity(4.0)
    p.setOctaveCount(2)
    p
  }
}