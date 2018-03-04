package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.shape.Box
import com.stovokor.control.CarControl
import com.jme3.ui.Picture
import com.jme3.system.AppSettings
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import java.time.Duration
import com.jme3.font.BitmapText
import com.jme3.font.BitmapFont

object CarHealthDisplayState extends AbstractAppState {

  var car: Option[CarControl] = None
  var rootNode: Node = null
  var assetManager: AssetManager = null
  var guiNode: Node = null

  var messageBox: BitmapText = null

  var screenSize: Vector2f = null

  val barLengh = 100
  val barHeight = 10

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    rootNode = simpleApp.asInstanceOf[SimpleApplication].getRootNode
    assetManager = simpleApp.asInstanceOf[SimpleApplication].getAssetManager
    guiNode = app.getGuiNode
    val settings = app.getContext.getSettings
    screenSize = new Vector2f(settings.getWidth, settings.getHeight)

    messageBox = createMessageBox(assetManager)
    guiNode.attachChild(messageBox)

  }

  override def update(tpf: Float) {
    getCar.foreach(cc => {
      val pc = Math.max(0f, 100f * cc.health / cc.initialHealth).toInt
      messageBox.setText(s"Car health: $pc%")
      messageBox.setLocalTranslation(screenSize.x * .95f - messageBox.getLineWidth,
        messageBox.getLocalTranslation.y, messageBox.getLocalTranslation.z)
    })
  }

  def createMessageBox(assetManager: AssetManager) = {
    val font = assetManager.loadFont("Interface/Fonts/Default.fnt")
    val text = new BitmapText(font, false)
    text.setSize(font.getCharSet().getRenderedSize())
    text.setColor(ColorRGBA.Green)
    text.setLocalTranslation(screenSize.x - 20f * text.getLineWidth, screenSize.y - 2f * text.getLineHeight(), 0)
    text
  }

  def getCar = {
    if (car.isEmpty) {
      val node = rootNode.getChild("car")
      if (node != null && node.getControl(classOf[CarControl]) != null) {
        car = Some(node.getControl(classOf[CarControl]))
      }
    }
    car
  }
}
