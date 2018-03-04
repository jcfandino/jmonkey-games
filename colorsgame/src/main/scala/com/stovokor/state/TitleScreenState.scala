package com.stovokor.state

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.app.SimpleApplication
import com.jme3.app.Application
import com.jme3.input.controls.ActionListener
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import de.lessvoid.nifty.screen.ScreenController
import com.jme3.niftygui.NiftyJmeDisplay
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.elements.render.TextRenderer

class TitleScreenState(niftyDisplay: NiftyJmeDisplay, var escBounced: Boolean = false)
    extends AbstractAppState with ActionListener with ScreenController {

  var app: SimpleApplication = null

  implicit def assetManager = app.getAssetManager
  def rootNode = app.getRootNode
  def cam = app.getCamera
  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    app = simpleApp.asInstanceOf[SimpleApplication]
    setupKeys

    val nifty = niftyDisplay.getNifty()
    nifty.fromXml("Interface/Nifty/nifty.xml", "title", this);
    SoundsState.playWelcome

  }
  var nifty: Nifty = null

  override def update(tpf: Float) {
    if (nifty != null) {
      val text = if (System.currentTimeMillis / 1000 % 2 == 0) "" else "Press Space to Start"
      val niftyElement = nifty.getCurrentScreen().findElementById("presskey-label")
      niftyElement.getRenderer(classOf[TextRenderer]).setText(text)

      if (GameStatus.highScore > 0) {
        val niftyElement = nifty.getCurrentScreen().findElementById("title-highscore-label")
        niftyElement.getRenderer(classOf[TextRenderer]).setText(s"Highscore: ${GameStatus.highScore}")
      }
    }
    if (pressed) {
      app.getInputManager.removeListener(this)
      app.getInputManager.clearMappings
      app.getStateManager.detach(this)
      app.getStateManager.attach(new InGameState(niftyDisplay))
    }
  }

  override def cleanup {
    app.getInputManager.removeListener(this)
  }

  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = nifty
  }

  def onEndScreen() {
  }

  def onStartScreen() {
  }
  var pressed = false

  def setupKeys() = {
    app.getInputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN))
    app.getInputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE))
    app.getInputManager.addListener(this, "Space")
    app.getInputManager.addListener(this, "Escape")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Space", _)  => pressed = value
    case ("Escape", _) => if (escBounced) escBounced = value else app.stop()
    case _             =>
  }

}