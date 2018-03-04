package com.stovokor.factory

import scala.util.Random
import com.jme3.asset.AssetManager
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.ColorRGBA
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.stovokor.K
import com.stovokor.control.EnemyControl
import com.flowpowered.noise.module.source.Perlin
import java.time.Instant
import com.flowpowered.noise.NoiseQuality
import com.jme3.texture.Texture
import com.jme3.scene.Node
import com.jme3.math.FastMath

object EnemyFactory {

  val box = new Box(K.enemyWidth / 2f, K.enemyHeight / 2f, 0.2f)

  def multipleWarbirds(assetManager: AssetManager, gameTime: Float, count: Int) = {
    val base = nextColumn(gameTime)
    0.to(count - 1).map(i => {
      val cand = base - i
      val col = if (cand < K.minCol) K.minCol + i else cand
      nextWarbird(assetManager, gameTime, col)
    })
  }

  def oneWarbird(assetManager: AssetManager, gameTime: Float) = {
    val col = nextColumn(gameTime)
    nextWarbird(assetManager, gameTime, col)
  }
  def nextWarbird(assetManager: AssetManager, gameTime: Float, col: Int) = {
    val geom = new Geometry("ship", box)

    val mat = MaterialFactory.create(assetManager, "Textures/warbird.png")
    geom.setQueueBucket(Bucket.Transparent)
    geom.setMaterial(mat)

    val node = new Node("warbird")
    node.attachChild(geom)
    node.attachChild(createPhaser(assetManager))
    node.addControl(EnemyControl())
    node.setLocalTranslation(col * K.enemyWidth, K.enemyStart, 0)
    node
  }

  def createPhaser(assetManager: AssetManager) = {
    val phaser = PhaserFactory.create(assetManager)
    phaser.rotate(0, 0, FastMath.PI)
    phaser
  }
  def nextColumn(gameTime: Float): Int = {
    K.minCol + Random.nextInt(K.columns)
  }

}