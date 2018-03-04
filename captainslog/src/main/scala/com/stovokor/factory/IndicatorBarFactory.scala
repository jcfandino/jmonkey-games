package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.Vector2f
import com.stovokor.K
import com.jme3.math.ColorRGBA
import com.jme3.scene.shape.Box
import com.stovokor.state.IndicatorBarControl
import com.jme3.scene.Node

object IndicatorBarFactory {

  val box = new Box(K.barWidth / 2f, K.barHeight / 2f, 1f)

  def create(assetManager: AssetManager, max: Float, pos: Int, title: String, f: => Float) = {
    var titleGeom = titleText(assetManager, title)
    titleGeom.move(0f, K.barHeight, 0f)

    var barGeom = bar(assetManager)

    val x = K.shipMaxPos + K.barWidth + 0.1f
    val y = -2f * K.barHeight + 3 * pos * K.barHeight
    println(s"Title $title coords ($x, $y) -> vertical perc. ${y / K.enemyStart}")
    val node = new Node("hud-bar")
    node.attachChild(titleGeom)
    node.attachChild(barGeom)
    node.addControl(new IndicatorBarControl(max, f))
    node.setLocalTranslation(new Vector3f(x, y, 1f))

    node
  }

  def titleText(assetManager: AssetManager, text: String) = {
    val title = new Geometry("title", box)
    val mat = MaterialFactory.create(assetManager, s"Textures/hud-${text}.png")
    title.setMaterial(mat)
    title
  }
  def bar(assetManager: AssetManager) = {
    val bar = new Geometry("bar", box)
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", ColorRGBA.White)
    bar.setMaterial(mat)
    bar

  }
}