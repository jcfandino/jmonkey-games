package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import com.stovokor.K
import com.stovokor.control.FallingBrickControl
import com.stovokor.control.PadControl
import com.stovokor.factory.BackgroundFactory
import com.stovokor.factory.BrickFactory
import com.stovokor.factory.PadFactory
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.ActionListener
import com.jme3.scene.Node
import com.jme3.asset.AssetManager
import com.jme3.audio.AudioNode
import com.jme3.scene.Node

class InGameState(niftyDisplay: NiftyJmeDisplay) extends AbstractAppState with ActionListener {

  var app: SimpleApplication = null
  var pad: Spatial = null

  implicit def assetManager = app.getAssetManager
  def rootNode = app.getRootNode
  def cam = app.getCamera

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    app.getFlyByCamera.setEnabled(false)

    cam.setParallelProjection(true)
    val aspect = cam.getWidth() / cam.getHeight()
    val frustumSize = K.brickStart / 2f
    val margin = 1f
    cam.setFrustum(-100, 100,
      -frustumSize * aspect, frustumSize * aspect,
      frustumSize + margin, -frustumSize - margin);
    cam.setLocation(new Vector3f(K.middle, frustumSize, 10))
    cam.lookAt(new Vector3f(K.middle, frustumSize, 0), Vector3f.UNIT_Y)
    cam.update

    app.getInputManager.setCursorVisible(false)
    setupKeys()

    pad = PadFactory.create(assetManager, app.getInputManager)
    rootNode.attachChild(pad)
    GameStatus.register(pad.getControl(classOf[PadControl]))

    rootNode.attachChild(BackgroundFactory.randomBackground(assetManager))

    appStateManager.attach(new HudAppState(niftyDisplay))

    GameStatus.restart
    SoundsState.playGameStart
  }


  override def update(tpf: Float) {
    if (GameStatus.isGameOver) {
      gameOver()
    }
  }

  override def cleanup {
    app.getInputManager.removeListener(pad.getControl(classOf[PadControl]))
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
    GameStatus.stop
    SoundsState.playGameOver
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new GameOverState(niftyDisplay))
  }

}