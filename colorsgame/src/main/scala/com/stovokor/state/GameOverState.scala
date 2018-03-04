package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.niftygui.NiftyJmeDisplay
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.elements.render.TextRenderer
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.ActionListener

class GameOverState(niftyDisplay: NiftyJmeDisplay)
    extends AbstractAppState with ScreenController with ActionListener {

  var nifty: Nifty = null
  var app: SimpleApplication = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    app = simpleApp.asInstanceOf[SimpleApplication]
    nifty = niftyDisplay.getNifty()
    nifty.fromXml("Interface/Nifty/nifty.xml", "gameover", this)
    setupKeys()
  }

  override def update(tpf: Float) {
    if (pressedSpace || pressedEsc) {
      goToTitleScreen()
    }
  }

  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = nifty
  }

  def onEndScreen() {
  }

  def onStartScreen() {
    val niftyElement = nifty.getCurrentScreen().findElementById("go-score-label")
    niftyElement.getRenderer(classOf[TextRenderer]).setText(s"Score: ${GameStatus.score}")
  }

  override def cleanup {
    app.getInputManager.removeListener(this)
  }

  def goToTitleScreen() {
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new TitleScreenState(niftyDisplay, pressedEsc))
  }

  var pressedSpace = false
  var pressedEsc = false

  def setupKeys() = {
    app.getInputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN))
    app.getInputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE))
    app.getInputManager.addListener(this, "Space")
    app.getInputManager.addListener(this, "Escape")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Space", _)  => pressedSpace = value
    case ("Escape", _) => pressedEsc = value
    case _             =>
  }
}
