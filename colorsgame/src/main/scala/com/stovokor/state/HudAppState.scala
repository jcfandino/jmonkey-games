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

class HudAppState(niftyDisplay: NiftyJmeDisplay) extends AbstractAppState with ScreenController {

  var nifty: Nifty = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    val nifty = niftyDisplay.getNifty()
    nifty.fromXml("Interface/Nifty/nifty.xml", "hud", this);
  }

  override def update(tpf: Float) {
    print("score-label", s"Score: ${GameStatus.score}")
    print("lives-label", s"Lives: ${GameStatus.lives}")
    print("highscore-label", s"Highscore: ${GameStatus.highScore}")
    print("multiplier-label", s"Mult: ${GameStatus.multiplier}")
    print("nextcombo-label", s"Next in: ${GameStatus.nextCombo - GameStatus.combo}")
    print("notice-label", GameStatus.notice)

    showDanger()
  }

  def print(field: String, text: String) {
    if (nifty != null) {
      val niftyElement = nifty.getCurrentScreen().findElementById(field)
      niftyElement.getRenderer(classOf[TextRenderer]).setText(text)
    }
  }

  def showDanger() {
    if (nifty != null) {
      val niftyElement = nifty.getCurrentScreen().findElementById("danger-label")
      val label = niftyElement.getRenderer(classOf[TextRenderer])
      val alpha = if (GameStatus.isInDangerZone()) {
        0.75f + 0.25f * Math.sin(System.currentTimeMillis.toDouble / 500.0).toFloat
      } else {
        0f
      }
      label.setColor(label.getColor.setAlpha(alpha))
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

