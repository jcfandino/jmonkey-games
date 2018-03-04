package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.audio.AudioNode

object SoundsState extends AbstractAppState {

  var missed: AudioNode = null
  var grabbed: AudioNode = null
  var combo: AudioNode = null
  var lifeLost: AudioNode = null
  var gameOver: AudioNode = null
  var gameStart: AudioNode = null
  var danger: AudioNode = null
  var welcome: AudioNode = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    val assetManager = app.getAssetManager
    missed = new AudioNode(assetManager, "Sounds/Effects/missed.wav", false)
    grabbed = new AudioNode(assetManager, "Sounds/Effects/grabbed.wav", false)
    combo = new AudioNode(assetManager, "Sounds/Effects/combo.wav", false)
    lifeLost = new AudioNode(assetManager, "Sounds/Effects/lifelost.wav", false)
    gameOver = new AudioNode(assetManager, "Sounds/Effects/gameover.wav", false)
    gameStart = new AudioNode(assetManager, "Sounds/Effects/start.wav", false)
    danger = new AudioNode(assetManager, "Sounds/Effects/danger.wav", false)
    danger.setLooping(true)
    welcome = new AudioNode(assetManager, "Sounds/Effects/welcome.wav", false)

    val node = app.getRootNode
    List(missed, grabbed, combo, lifeLost, gameOver, danger).foreach(node.attachChild)
  }

  def playMissed { missed.play() }

  def playGrabbed { grabbed.play() }

  def playCombo { combo.play() }

  def playLifeLost { lifeLost.play() }

  def playGameOver { gameOver.play() }

  def playGameStart { gameStart.play() }

  def playWelcome { welcome.play() }

  def playDanger(b: Boolean) {
    if (b) danger.play()
    else danger.stop()
  }
}