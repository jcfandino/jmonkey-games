package com.stovokor.state

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.stovokor.K
import com.stovokor.control.FallingBrickControl
import com.stovokor.control.PadControl
import com.stovokor.factory.BrickFactory
import com.jme3.app.SimpleApplication

object GameStatus extends AbstractAppState {

  var highScore = 0
  var score = 0
  var lives = 0
  var multiplier = 0
  var combo = 0
  val combos = List(5, 10, 20, 50)
  var notice = ""
  var noticeTimeout = 0f
  var playingDangerAlarm = false

  var waitingTime = 0f
  var timeToWait = 0f
  var timeOfGame = 0f
  var brickSpeed = 0f

  var bricks: List[FallingBrickControl] = List()
  var pad: Option[PadControl] = None

  var app: SimpleApplication = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    app = simpleApp.asInstanceOf[SimpleApplication]
    super.initialize(appStateManager, simpleApp)
    bricks = List()
    pad = None
  }

  override def update(tpf: Float) {
    if (!isGameOver) updateGameLogic(tpf)
  }

  def updateGameLogic(tpf: Float) {
    timeOfGame += tpf
    waitingTime += tpf

    if (timeOfGame > K.easyTime) {
      pad.foreach(_.moveUp(tpf * K.padUpSpeed))
    }
    if (waitingTime > timeToWait) {
      val brick = BrickFactory.nextBrick(app.getAssetManager, timeOfGame, brickSpeed)
      register(brick.getControl(classOf[FallingBrickControl]))
      app.getRootNode.attachChild(brick)
      waitingTime = 0f;
      // TODO accelerate linearly
      if (timeOfGame > K.easyTime) {
        timeToWait = Math.max(K.minTimeToWait, timeToWait - K.timeAcceleration)
        brickSpeed = Math.min(K.brickMaxSpeed, brickSpeed + K.brickSpeedAcceleration)
      }
    }

    checkBricks

    if (noticeTimeout == 0f) {
      notice = ""
    } else {
      noticeTimeout = Math.max(0, noticeTimeout - tpf)
    }
    if (isInDangerZone()) {
      if (!playingDangerAlarm) {
        playingDangerAlarm = true
        SoundsState.playDanger(true)
      }
    } else if (playingDangerAlarm) {
      playingDangerAlarm = false
      SoundsState.playDanger(false)
    }
  }

  def isGameOver = lives <= 0

  def stop { lives = 0 }

  def checkBricks {
    bricks.foreach(b => {
      // take some precautions
      if (b.getSpatial != null && b.getSpatial.getParent != null) {
        checkBrick(b)
      }
    })
  }

  def restart {
    bricks = List()
    highScore = Math.max(score, highScore)
    score = 0
    lives = K.initLives
    multiplier = 1
    combo = 0
    waitingTime = 0
    timeToWait = K.initTimeToWait
    timeOfGame = 0f
    brickSpeed = K.brickInitSpeed
    showNotice("Go!")
  }

  def showNotice(msg: String, timeout: Float = 1f) {
    notice = msg
    noticeTimeout = timeout
  }

  def register(p: PadControl) {
    pad = Some(p)
  }

  def register(brick: FallingBrickControl) {
    bricks = bricks ++ List(brick)
  }

  def checkBrick(brick: FallingBrickControl) {
    if (isAtPadHeight(brick) && isOverPad(brick)) {
      if (matchesPadColor(brick)) {
        // color OK, earn points and move down a little
        grabBrick(brick)
      } else {
        // wrong color, loose a life, restart
        looseALife()
      }
    } else if (isUnderPad(brick)) {
      // remove brick, pump the pad up
      missBrick(brick)
    } else if (isTooHigh()) {
      looseALife()
    }
  }

  def grabBrick(brick: FallingBrickControl) {
    // color OK, earn points and move down a little
    pad.foreach(_.moveDown(K.grabbedHelp))
    if (isInDangerZone()) { // be generous in danger zone
      pad.foreach(p => p.moveDown((p.vertPos - K.padDangerZone) + K.grabbedHelp))
    }
    killBrick(brick)
    earnScore()
  }

  def missBrick(brick: FallingBrickControl) {
    combo = 0
    multiplier = Math.max(1, multiplier - 1)
    pad.foreach(_.moveUp(K.missedPenalty))
    showNotice("Missed")
    SoundsState.playMissed
    killBrick(brick)
  }
  
  def killBrick(brick:FallingBrickControl) {
    bricks = bricks.filterNot(b => b == brick)
    brick.getSpatial.removeFromParent
  }

  def earnScore() {
    score += 10 * multiplier
    combo += 1
    if (combo == nextCombo) {
      showNotice("Combo!")
      multiplier += 1
      combo = 0
      SoundsState.playCombo
    } else {
      SoundsState.playGrabbed
    }
  }

  def nextCombo() = {
    combos(Math.min(combos.length - 1, multiplier - 1))
  }

  def looseALife() {
    // decrease difficulty a little
    timeToWait = Math.min(K.initTimeToWait, timeToWait + 25f * K.timeAcceleration)
    brickSpeed = Math.max(K.brickInitSpeed, brickSpeed - 10f * K.brickInitSpeed)
    // partial reset
    lives = lives - 1
    combo = 0
    multiplier = 1
    pad.foreach(_.resetPos)
    bricks.foreach(b => b.getSpatial.removeFromParent())
    bricks = List()
    showNotice("Life lost")
    SoundsState.playLifeLost
  }

  def padY = pad.map(p => p.getSpatial.getLocalTranslation.y).getOrElse(0f)
  def padX = pad.map(p => p.getSpatial.getLocalTranslation.x).getOrElse(0f)

  def isAtPadHeight(brick: FallingBrickControl) = {
    val brickY = brick.getSpatial.getLocalTranslation.y
    brickY - K.brickHeight / 2f < padY + K.padHeight / 2f &&
      brickY + K.brickHeight > padY - K.padHeight / 2f
  }

  def isOverPad(brick: FallingBrickControl) = {
    val brickX = brick.getSpatial.getLocalTranslation.x
    (brickX + K.brickWidth / 2f) > (padX - K.padWidth / 2f) && // right brick side right of left pad side
      (brickX - K.brickWidth / 2f) < (padX + K.padWidth / 2f) // left brick side left of right pad side
  }

  def isUnderPad(brick: FallingBrickControl) = {
    val brickY = brick.getSpatial.getLocalTranslation.y
    brickY < -K.brickHeight / 2f
  }
  def isTooHigh() = {
    padY > K.padMaxY
  }
  def isInDangerZone() = {
    padY > K.padDangerZone
  }

  def matchesPadColor(brick: FallingBrickControl) = {
    pad.map(_.sameColor(brick)).getOrElse(false)
  }
}