package com.stovokor.bomber

import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.app.SimpleApplication
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture
import scala.util.Random
import com.jme3.bullet.BulletAppState
import com.jme3.math.Vector3f
import com.jme3.bullet.BulletAppState.ThreadingType
import com.jme3.system.AppSettings
import com.stovokor.bomber.state.InGameState
import com.jme3.niftygui.NiftyJmeDisplay
import com.stovokor.bomber.state.HudAppState
import com.stovokor.bomber.state.TitleScreenState

object Main extends SimpleApplication {

  def main(args: Array[String]) {
    val sets = new AppSettings(true)
    sets.setSettingsDialogImage("/Interface/bomber-logo.png")
    sets.setGammaCorrection(true)
    sets.setWidth(1024)
    sets.setHeight(768)
    sets.setGammaCorrection(false)
    setSettings(sets)
    setDisplayFps(false)
    setDisplayStatView(false)

    Main.start
  }

  def simpleInitApp() = {
    val niftyDisplay = new NiftyJmeDisplay(getAssetManager,
      getInputManager,
      getAudioRenderer,
      getGuiViewPort)
    getGuiViewPort.addProcessor(niftyDisplay);

    val bulletAppState = new BulletAppState
    //    bulletAppState.setDebugEnabled(true)
    bulletAppState.setThreadingType(ThreadingType.PARALLEL)
    stateManager.attach(bulletAppState)
    stateManager.attach(new TitleScreenState(niftyDisplay))

  }

}
