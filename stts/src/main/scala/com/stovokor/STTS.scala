package com.stovokor

import java.util.logging.Level
import java.util.logging.Logger
import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.BulletAppState.ThreadingType
import com.jme3.input.controls.ActionListener
import com.jme3.scene.Node
import com.stovokor.state.PathFindScheduler
import com.stovokor.domain.GameStatus
import com.stovokor.domain.PlayerCharacter
import com.stovokor.domain.Weapon
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.state.HudGameState
import com.stovokor.state.InGameState
import com.stovokor.state.ProximityDetectionState
import com.stovokor.util.debug.NavigationDebug
import com.stovokor.state.PhysicsSchedulingAppState

object STTS extends STTSApp with ActionListener {

  def main(args: Array[String]): Unit = STTS.start

  def simpleInitApp = {
//    val seed = 17L
    val seed = 1L
//        val seed = System.currentTimeMillis()//1L
    val gameStatus = new GameStatus(seed)
    val bulletState = new BulletAppState
    bulletState.setThreadingType(threadingType)
    stateManager.attach(bulletState)
    stateManager.attach(new InGameState(gameStatus))
    stateManager.attach(new HudGameState(gameStatus))
    stateManager.attach(new ProximityDetectionState())
    stateManager.attach(PathFindScheduler)
    stateManager.attach(PhysicsSchedulingAppState)
    //    stateManager.attach(SceneManager)
    bulletState.getPhysicsSpace().setMaxSubSteps(Settings.Physics.maxSubSteps)
    setDisplayStatView(true)

    Logger.getLogger("com.jme3.util.TangentBinormalGenerator").setLevel(Level.OFF)
  }

  override def destroy() {
    super.destroy()
  }
  def threadingType =
    if (Settings.Physics.parallel) ThreadingType.PARALLEL
    else ThreadingType.SEQUENTIAL

  def setupKeys() = {}

  def onAction(binding: String, value: Boolean, tpf: Float) = {}

  override def simpleUpdate(tpf: Float) = {}

  lazy val navigationDebug = new NavigationDebug(rootNode, assetManager)

}

abstract class STTSApp extends SimpleApplication {
  def getSettings = settings
  def getGuiFont = guiFont

}