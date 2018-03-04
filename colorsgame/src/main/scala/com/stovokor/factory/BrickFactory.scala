package com.stovokor.factory

import scala.util.Random
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.ColorRGBA
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.stovokor.K
import com.stovokor.control.FallingBrickControl
import com.flowpowered.noise.module.source.Perlin
import java.time.Instant
import com.flowpowered.noise.NoiseQuality

object BrickFactory {

  val box = new Box(K.brickWidth / 2f, K.brickHeight / 2f, 0.2f)
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

  val colors = List(ColorRGBA.Red, ColorRGBA.Green, ColorRGBA.Blue,
    ColorRGBA.Magenta, ColorRGBA.Cyan, ColorRGBA.Yellow,
    ColorRGBA.White)

  val easyColors = List(ColorRGBA.Red, ColorRGBA.Green, ColorRGBA.Blue)

  def nextBrick(implicit assetManager: AssetManager, gameTime: Float, brickSpeed: Float) = {
    val geom = new Geometry("brick", box)
    val pos = nextPos(gameTime)
    geom.setLocalTranslation(pos, K.brickStart, 0)

    val color = nextColor(gameTime)
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)

    val texture = assetManager.loadTexture("Textures/Glossy07.png")
    mat.setTexture("ColorMap", texture)
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
    geom.setQueueBucket(Bucket.Transparent)

    geom.setMaterial(mat)
    geom.addControl(FallingBrickControl(color, brickSpeed))
    geom
  }

  def nextColor(gameTime: Float) = {
    val list = if (gameTime < K.easyTime) easyColors else colors
    list(Random.nextInt(list.size))
  }


  def nextPos(gameTime: Float): Float = {
    val factor = perlin.getValue(1.1f * gameTime, 0.1f * gameTime, 0).toFloat // -1 ~ 1 ??
    val column = Math.max(K.minCol, Math.min(K.maxCol, (0.5f * factor * K.columns.toFloat).toInt))
    K.brickWidth * column
  }

}