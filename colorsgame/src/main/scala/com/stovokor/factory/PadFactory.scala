package com.stovokor.factory

import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.scene.shape.Box
import com.jme3.math.ColorRGBA
import com.jme3.asset.AssetManager
import com.jme3.input.InputManager
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.material.RenderState.BlendMode
import com.stovokor.K
import com.stovokor.control.PadControl

object PadFactory {

  val box = new Box(K.padWidth / 2f, K.padHeight / 2f, 1f)

  def create(implicit assetManager: AssetManager, inputManager: InputManager) = {
    val geom = new Geometry("box", box)
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", ColorRGBA.DarkGray)

    val texture = assetManager.loadTexture("Textures/Glossy07.png")
    mat.setTexture("ColorMap", texture)
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);  // !
    geom.setQueueBucket(Bucket.Transparent);
    geom.setMaterial(mat)

    geom.setLocalTranslation((K.padMinPos + K.padMaxPos) / 2, K.padPosY, 1)
    geom.addControl(PadControl(inputManager))
    geom
  }
}