package com.stovokor

import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.app.SimpleApplication
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture
import com.stovokor.state.InGameState
import com.stovokor.state.TitleScreenState
import com.jme3.niftygui.NiftyJmeDisplay
import com.stovokor.state.GameStatus
import com.stovokor.state.SoundsState

// Captain's log
object Main extends SimpleApplication {

  def main(args: Array[String]) {
    Main.start
  }

  def simpleInitApp() = {
    val niftyDisplay = new NiftyJmeDisplay(getAssetManager,
      getInputManager,
      getAudioRenderer,
      getGuiViewPort)
    getGuiViewPort.addProcessor(niftyDisplay);
    
    stateManager.attach(SoundsState)
    stateManager.attach(GameStatus)
    stateManager.attach(new TitleScreenState(niftyDisplay))

    setDisplayFps(false)
    setDisplayStatView(false)
  }

}
