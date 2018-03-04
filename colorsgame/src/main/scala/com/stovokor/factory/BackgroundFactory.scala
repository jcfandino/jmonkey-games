package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.scene.shape.Box
import com.jme3.scene.Geometry
import com.jme3.material.Material
import scala.util.Random
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.texture.Texture.WrapMode
import com.jme3.math.Vector2f
import com.stovokor.K

object BackgroundFactory {

  val borderWidth = 0.2f
  val lowerExtent = 0.5f

  val box = new Box((K.padMaxPos - K.padMinPos + K.padWidth) / 2f, K.brickStart / 2f + lowerExtent / 2f, 1f)
  val boxBig = new Box(box.xExtent + borderWidth, box.yExtent + borderWidth, 1f)

  val backgroundCount = 6

  def randomBackground(assetManager: AssetManager) = {

    val node = new Node("scene")
    node.attachChild(back(assetManager))
    node.attachChild(border(assetManager))
    node
  }

  def back(assetManager: AssetManager) = {
    val back = new Geometry("background", box)
    val pos = new Vector3f(K.middle, K.brickStart / 2f - lowerExtent, -1f);
    back.setLocalTranslation(pos)

    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    val index = 1 + Random.nextInt(backgroundCount)
    val file = s"Textures/Backgrounds/Background-${index}.jpg"
    val texture = assetManager.loadTexture(file)
    mat.setTexture("ColorMap", texture)
    back.setMaterial(mat)
    back
  }

  def border(assetManager: AssetManager) = {
    boxBig.scaleTextureCoordinates(new Vector2f(5f, 5f))
    val border = new Geometry("border", boxBig)
    val pos = new Vector3f(K.middle, K.brickStart / 2f - lowerExtent, -2f)
    border.setLocalTranslation(pos)

    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    val index = 1 + Random.nextInt(backgroundCount)
    val texture = assetManager.loadTexture(s"Textures/Border-1.jpg")
    mat.setTexture("ColorMap", texture)
    mat.getTextureParam("ColorMap").getTextureValue().setWrap(WrapMode.MirroredRepeat)

    border.setMaterial(mat)
    border
  }
}