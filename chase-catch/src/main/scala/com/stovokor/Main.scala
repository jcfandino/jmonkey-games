package com.stovokor

import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.app.SimpleApplication
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture
import com.stovokor.tiles.TileMatrix
import com.stovokor.tiles.Tile
import com.stovokor.tiles.MapBuilder
import scala.util.Random
import com.jme3.bullet.BulletAppState
import com.jme3.math.Vector3f
import com.jme3.bullet.BulletAppState.ThreadingType
import com.stovokor.state.InGameState
import com.jme3.system.AppSettings

object Main extends SimpleApplication {

  def main(args: Array[String]) {
    val sets = new AppSettings(true)
    sets.setSettingsDialogImage("/Textures/the-chase-logo.png")
    sets.setGammaCorrection(true)
    sets.setWidth(1024)
    sets.setHeight(768)
    setSettings(sets)
    setDisplayFps(false)
    setDisplayStatView(false)
    
    Main.start
  }

  def simpleInitApp() = {
    val bulletAppState = new BulletAppState
    bulletAppState.setDebugEnabled(false)
    bulletAppState.setThreadingType(ThreadingType.PARALLEL)
    stateManager.attach(bulletAppState)
    stateManager.attach(new InGameState)
  }

}