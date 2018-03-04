package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import com.stovokor.K
import com.stovokor.control.EnemyControl
import com.stovokor.control.ShipControl
import com.stovokor.factory.BackgroundFactory
import com.stovokor.factory.EnemyFactory
import com.stovokor.factory.ShipFactory
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.ActionListener
import com.jme3.scene.Node
import com.jme3.asset.AssetManager
import com.jme3.audio.AudioNode
import com.jme3.scene.Node
import com.stovokor.factory.IndicatorBarFactory
import com.stovokor.factory.IndicatorBarFactory
import com.stovokor.factory.IndicatorBarFactory

class InGameState(niftyDisplay: NiftyJmeDisplay) extends AbstractAppState with ActionListener {

  var app: SimpleApplication = null
  var ship: Spatial = null

  implicit def assetManager = app.getAssetManager
  def rootNode = app.getRootNode
  def cam = app.getCamera

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    // camera
    app.getFlyByCamera.setEnabled(false)
    cam.setParallelProjection(true)
    val aspect = cam.getWidth() / cam.getHeight()
    val frustumSize = K.enemyStart / 2f
    val margin = .1f
    cam.setFrustum(-100, 100,
      -frustumSize * aspect, frustumSize * aspect,
      frustumSize + margin, -frustumSize - margin);
    cam.setLocation(new Vector3f(K.middle + K.xOffset, frustumSize, 10))
    cam.lookAt(new Vector3f(K.middle + K.xOffset, frustumSize, 0), Vector3f.UNIT_Y)
    cam.update

    // input
    app.getInputManager.setCursorVisible(false)
    setupKeys()

    // background
    rootNode.attachChild(BackgroundFactory.randomBackground(assetManager))
    // hud    
    appStateManager.attach(new HudAppState(niftyDisplay))

    // indicator bars
    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, 1f, 6, "score",  0f))
    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, 1f, 5, "lives", 0f))

    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, K.shipInitialShield, 4, "shield", shipControl.shield))
    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, K.shipInitialHealth, 3, "health", shipControl.health))
    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, K.phaserMaxEnergy, 2, "phaser", shipControl.phaserEnergy))
    rootNode.attachChild(IndicatorBarFactory.create(
        assetManager, K.initialTorpedos, 1, "torpedoes", shipControl.torpedoes))

    // ship
    ship = ShipFactory.create(assetManager, app.getInputManager)
    rootNode.attachChild(ship)
    GameStatus.register(ship.getControl(classOf[ShipControl]))

    GameStatus.restart
    SoundsState.playGameStart
  }
  
  
  var gameOverAt = -1L
  
  override def update(tpf: Float) {
    if (gameOverAt > 0L && System.currentTimeMillis - gameOverAt > 3000) {
      gameOver()
    }
    if (gameOverAt < 0L && GameStatus.isGameOver) {
      gameOverAt = System.currentTimeMillis
      GameStatus.stop
    }
  }

  override def cleanup {
    app.getInputManager.removeListener(ship.getControl(classOf[ShipControl]))
    app.getInputManager.clearMappings
    val sm = app.getStateManager
    sm.detach(sm.getState(classOf[HudAppState]))
    rootNode.detachAllChildren()
  }

  def setupKeys() = {
    app.getInputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE))
    app.getInputManager.addListener(this, "Escape")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Escape", _) => gameCancelled()
    case _             =>
  }

  def gameCancelled() {
    GameStatus.stop
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new TitleScreenState(niftyDisplay, true))
  }

  def gameOver() {
    SoundsState.playGameOver
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new GameOverState(niftyDisplay))
  }

  def shipControl = ship.getControl(classOf[ShipControl])
}