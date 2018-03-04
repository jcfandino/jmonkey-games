package com.stovokor.bomber.state

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

class HudAppState(niftyDisplay: NiftyJmeDisplay) extends AbstractAppState with ScreenController {

  var nifty: Nifty = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    val nifty = niftyDisplay.getNifty()
    nifty.fromXml("Interface/nifty.xml", "hud", this);
  }

  override def update(tpf: Float) {
    print("score-label", s"Score: ${GameStatus.score}")
//    print("lives-label", s"${GameStatus.lives}")
    print("health-label", s"Health: ${GameStatus.health}")
//    print("notice-label", GameStatus.notice)

  }

  def print(field: String, text: String) {
    if (nifty != null) {
      val niftyElement = nifty.getCurrentScreen().findElementById(field)
      niftyElement.getRenderer(classOf[TextRenderer]).setText(text)
    }
  }

  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = nifty
  }

  def onEndScreen() {
  }

  def onStartScreen() {
  }
}

