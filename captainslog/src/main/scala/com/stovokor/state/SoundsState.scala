package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.audio.AudioNode

object SoundsState extends AbstractAppState {

  var missed: AudioNode = null
  var crash: AudioNode = null
  var danger: AudioNode = null
  var lifeLost: AudioNode = null
  var gameOver: AudioNode = null
  var gameStart: AudioNode = null
  var welcome: AudioNode = null
  var enterpriseExplosion: AudioNode = null
  var warbirdExplosion: AudioNode = null
  var phaser: AudioNode = null
  var respawn: AudioNode = null
  var torpedo: AudioNode = null
  var wave: AudioNode = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    val assetManager = app.getAssetManager
    missed = new AudioNode(assetManager, "Sounds/Effects/missed.wav", false)
    crash = new AudioNode(assetManager, "Sounds/Effects/crash.wav", false)
    danger = new AudioNode(assetManager, "Sounds/Effects/danger.wav", false)
    lifeLost = new AudioNode(assetManager, "Sounds/Effects/lifelost.wav", false)
    gameOver = new AudioNode(assetManager, "Sounds/Effects/gameover.wav", false)
    gameStart = new AudioNode(assetManager, "Sounds/Effects/start.wav", false)
    danger.setLooping(true)
    welcome = new AudioNode(assetManager, "Sounds/Effects/welcome.wav", false)
    enterpriseExplosion = new AudioNode(assetManager, "Sounds/Effects/enterprise-explosion.wav", false)
    warbirdExplosion = new AudioNode(assetManager, "Sounds/Effects/warbird-explosion.wav", false)
    phaser = new AudioNode(assetManager, "Sounds/Effects/phaser.wav", false)
    respawn = new AudioNode(assetManager, "Sounds/Effects/respawn.wav", false)
    torpedo = new AudioNode(assetManager, "Sounds/Effects/torpedo.wav", false)
    wave = new AudioNode(assetManager, "Sounds/Effects/wave.wav", false)

    val node = app.getRootNode
    List(missed, crash, danger, lifeLost, gameOver, gameStart, welcome, enterpriseExplosion,
      warbirdExplosion, phaser, respawn, torpedo, wave).foreach(node.attachChild)
  }

  def playMissed { missed.play() }

  def playCrash { crash.play() }

  def playEnterpriseExplosion { enterpriseExplosion.play() }

  def playWarbirdExplosion { warbirdExplosion.play() }

  def playPhaser { phaser.play() }

  def playTorpedo { torpedo.play() }

  def playWave { wave.play() }

  def playLifeLost { lifeLost.play() }

  def playGameOver { gameOver.play() }

  def playGameStart { gameStart.play() }

  def playWelcome { welcome.play() }

  def playDanger(b: Boolean) {
    if (b) danger.play()
    else danger.stop()
  }
}