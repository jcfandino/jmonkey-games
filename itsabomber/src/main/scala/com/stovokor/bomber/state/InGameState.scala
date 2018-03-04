package com.stovokor.bomber.state

import scala.collection.JavaConversions.asScalaBuffer

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.math.Vector3f
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.scene.Node
import com.stovokor.bomber.control.PlaneControl
import com.stovokor.bomber.factory.PlayerFactory
import com.stovokor.bomber.factory.SkyFactory
import com.stovokor.bomber.tiles.MapLoader
import scala.util.Random
import com.jme3.bullet.control.RigidBodyControl

class InGameState(val niftyDisplay: NiftyJmeDisplay) extends SimpleAppState with ActionListener {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    space.setGravity(new Vector3f(0f, -5f, 0f))
    app.getFlyByCamera.setEnabled(false)
    cam.setLocation(new Vector3f(5.55f, 4.15f, 10f))
    //    cam.setLocation(new Vector3f(20.55f, 4.15f, 100f))

    app.getInputManager.setCursorVisible(false)
    setupKeys

    //sky
    val sky = SkyFactory.create(assetManager)
    rootNode.attachChild(sky)

    // player
    val plane = PlayerFactory.create(assetManager, inputManager)
    rootNode.attachChild(plane)
    space.add(plane)

    // map
    val map = new MapLoader(assetManager).load()
    rootNode.attachChild(map)

    map.getChild("enemies").asInstanceOf[Node]
      .getChildren
      .map(_.asInstanceOf[Node].getChild("enemy-body"))
      .foreach(space.add)

    GameStatus.reset()
    stateManager.attach(new SceneManagementState(10f)) // see map builder
    stateManager.attach(GameStatus)
    stateManager.attach(new HudAppState(niftyDisplay))
  }

  var gameOverAt = -1L

  override def update(tpf: Float) {
    if (gameOverAt > 0L && System.currentTimeMillis - gameOverAt > 1000) {
      gameOver()
    }
    if (gameOverAt < 0L && GameStatus.lives <= 0) {
      gameOverAt = System.currentTimeMillis
      val plane = rootNode.getChild("plane-node")
      app.getInputManager.removeListener(plane.getControl(classOf[PlaneControl]))
      val pos = plane.getWorldTranslation // enemies are attached to ground
      for (i <- 1 to 6) {
        val expPos = pos.add(-PlayerFactory.planeLength / 2f + (0.2f * i * PlayerFactory.planeLength),
          -PlayerFactory.planeTall / 2f + PlayerFactory.planeTall * Random.nextFloat(), 0f)
        GameStatus.explode(expPos)
      }

      //      GameStatus.stop

    }
  }

  override def cleanup {
    val plane = rootNode.getChild("plane-node")
    app.getInputManager.removeListener(plane.getControl(classOf[PlaneControl]))
    app.getInputManager.clearMappings
    space.removeAll(rootNode)
    val sm = app.getStateManager
    sm.detach(sm.getState(classOf[HudAppState]))
    sm.detach(GameStatus)
    rootNode.detachAllChildren()
  }

  def setupKeys {
    app.getInputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE), new KeyTrigger(KeyInput.KEY_Q))
    app.getInputManager.addListener(this, "Escape")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Escape", _) => gameCancelled()
    case _             =>
  }

  def gameCancelled() {
    println("game cancelled")
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new TitleScreenState(niftyDisplay, true))
  }

  def gameOver() {
    println("game over")
    val sm = app.getStateManager
    sm.detach(this)
    sm.attach(new GameOverState(niftyDisplay))
  }

}