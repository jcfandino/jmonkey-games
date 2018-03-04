package com.stovokor.bomber.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.font.BitmapText
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.scene.Node
import com.stovokor.bomber.control.PlaneControl

object PlayerHealthDisplayState extends SimpleAppState {

  var plane: Option[PlaneControl] = None

  var messageBox: BitmapText = null

  var screenSize: Vector2f = null

  val barLengh = 100
  val barHeight = 10

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    screenSize = new Vector2f(settings.getWidth, settings.getHeight)
    messageBox = createMessageBox(assetManager)
    guiNode.attachChild(messageBox)
  }

  override def update(tpf: Float) {
    getPlane.foreach(cc => {
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

  def getPlane = {
    if (plane.isEmpty) {
      val node = rootNode.getChild("plane-node")
      if (node != null && node.getControl(classOf[PlaneControl]) != null) {
        plane = Some(node.getControl(classOf[PlaneControl]))
      }
    }
    plane
  }
}
