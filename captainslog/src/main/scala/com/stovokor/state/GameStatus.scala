package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector2f
import com.stovokor.K
import com.stovokor.control.EnemyControl
import com.stovokor.control.ShipControl
import com.stovokor.control.TorpedoControl
import com.stovokor.factory.EnemyFactory
import com.stovokor.factory.ExplosionFactory
import com.stovokor.factory.TorpedoFactory
import scala.util.Random

object GameStatus extends AbstractAppState {

  var highScore = 0
  var score = 0
  var lives = 0
  var multiplier = 0
  //  var combo = 0
  //  val combos = List(5, 10, 20, 50)
  var notice = ""
  var noticeTimeout = 0f
  var playingDangerAlarm = false

  var waitingTime = 0f
  var timeToWait = 0f
  var timeOfGame = 0f
  var timeToBreak = K.breaksEvery

  var enemies: List[EnemyControl] = List()
  var ship: Option[ShipControl] = None

  var app: SimpleApplication = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    app = simpleApp.asInstanceOf[SimpleApplication]
    super.initialize(appStateManager, simpleApp)
    enemies = List()
    ship = None
  }

  override def update(tpf: Float) {
    if (!isGameOver) updateGameLogic(tpf)
  }

  def updateGameLogic(tpf: Float) {
    timeOfGame += tpf
    waitingTime += tpf
    timeToBreak = timeToBreak - tpf

    if (false || waitingTime > timeToWait && timeToBreak > 0f) {
      val enemies = EnemyFactory.multipleWarbirds(app.getAssetManager, timeOfGame, enemiesToAdd)
      enemies.foreach(enemy => {
        register(enemy.getControl(classOf[EnemyControl]))
        app.getRootNode.attachChild(enemy)
      })
      // TODO accelerate linearly
      if (timeOfGame > K.easyTime) {
        timeToWait = Math.max(K.minTimeToWait, timeToWait - K.timeAcceleration)
      }
      waitingTime = 0f;
    }
    if (timeToBreak < -K.breakTime) {
      timeToBreak = K.breaksEvery
      CaptainMoodState.set(CaptainMood.Energized())
      SoundsState.playWave
    }

    checkEnemies(tpf)

    if (noticeTimeout == 0f) {
      notice = ""
    } else {
      noticeTimeout = Math.max(0, noticeTimeout - tpf)
    }
    if (isInDangerZone()) {
      if (!playingDangerAlarm) {
        CaptainMoodState.set(CaptainMood.Dissapointed())
        playingDangerAlarm = true
        SoundsState.playDanger(true)
      }
    } else if (playingDangerAlarm) {
      playingDangerAlarm = false
      SoundsState.playDanger(false)
    }
  }

  def enemiesToAdd = {
    def choose(tmin: Float, tmax: Float, nmax: Int, p: Float, n: Float): Int = {
      if (nmax == 1) 1
      else if (n < p * (timeOfGame - tmin) / tmin) nmax
      else choose(tmin, tmax, nmax - 1, p, n)
    }

    val timeBeforeTwo = 20f
    val timeBeforeThree = 40f
    val timeBeforeFour = 60f
    val n = Random.nextFloat()
    if (timeOfGame < timeBeforeTwo) {
      1
    } else if (timeOfGame < timeBeforeThree) {
      choose(timeBeforeTwo, timeBeforeThree, 2, 0.4f, n)
    } else if (timeOfGame < timeBeforeFour) {
      choose(timeBeforeThree, timeBeforeFour, 3, 0.3f, n)
    } else {
      choose(timeBeforeFour, timeBeforeFour + timeBeforeTwo, 4, 0.2f, n)
    }
  }

  def isGameOver = lives <= 0

  def stop { lives = 0 }

  def checkEnemies(tpf: Float) {
    enemies.foreach(b => {
      // take some precautions
      if (b.getSpatial != null && b.getSpatial.getParent != null) {
        checkEnemy(b, tpf)
      }
    })
  }

  def restart {
    enemies = List()
    torpedos = List()
    highScore = Math.max(score, highScore)
    score = 0
    lives = K.initLives
    multiplier = 1
    //    combo = 0
    waitingTime = 0
    timeToWait = K.initTimeToWait
    timeOfGame = 0f
    showNotice("Engage!")
    CaptainMoodState.set(CaptainMood.Energized())
  }

  def showNotice(msg: String, timeout: Float = 1f) {
    notice = msg
    noticeTimeout = timeout
  }

  var torpedos: List[TorpedoControl] = List()

  def torpedoShot(x: Float, y: Float, friendly: Boolean) {
    var tor = TorpedoFactory.create(app.getAssetManager, x, y, friendly)
    app.getRootNode.attachChild(tor)
    register(tor.getControl(classOf[TorpedoControl]))
  }

  def showExplosion(x: Float, y: Float) {
    var exp = ExplosionFactory.create(app.getAssetManager, x, y)
    app.getRootNode.attachChild(exp)
  }

  def register(t: TorpedoControl) {
    torpedos = torpedos ++ List(t)
  }
  def register(p: ShipControl) {
    ship = Some(p)
  }

  def register(enemy: EnemyControl) {
    enemies = enemies ++ List(enemy)
  }

  def checkEnemy(enemy: EnemyControl, tpf: Float) {
    if (isAtShipHeight(enemy) && isCrashing(enemy)) {
      ship.forall(_.hit(tpf * K.crashDamage))
      enemy.hit(tpf * K.crashDamage)
      CaptainMoodState.set(CaptainMood.Mad())
      SoundsState.playCrash
    }
    if (isTooFarBehind(enemy)) {
      missEnemy(enemy)
      CaptainMoodState.set(CaptainMood.Confused())
    }
    //TODO doesnt mean is actually shooting
    if (ship.map(_.isShootingPhaser).getOrElse(false) &&
      isEnemyTargetedByPhaser(enemy)) {
      enemy.hit(tpf * K.phaserDamage)
    }
    if (enemy.isShootingPhaser &&
      isShipTargetedByPhaser(enemy)) {
      ship.foreach(_.hit(tpf * K.phaserDamage))
      CaptainMoodState.set(CaptainMood.Sad())
    }
    if (checkHitByTorpedo(enemy)) {

    }
    if (enemy.health <= 0f) {
      removeShip(enemy)
      showExplosion(enemyX(enemy), enemyY(enemy))
      earnScore()
      SoundsState.playWarbirdExplosion
      if (Random.nextInt(2) == 0) {
        CaptainMoodState.set(CaptainMood.Happy())
      } else {
        CaptainMoodState.set(CaptainMood.Surprised())
      }
    }

    if (ship.map(_.health == 0f).getOrElse(false)) {
      looseALife()
    }
  }

  def checkHitByTorpedo(enemy: EnemyControl) = {
    val any = torpedos.find(t => isEnemyHitByTorpedo(enemy, t))
    any.foreach(torpedo => {
      torpedos = torpedos.filterNot(t => t == torpedo)
      torpedo.getSpatial.removeFromParent()
      enemy.hit(K.torpedoDamage)
    })
    any.isDefined
  }

  def missEnemy(enemy: EnemyControl) {
    //    combo = 0
    multiplier = Math.max(1, multiplier - 1)
    //    showNotice("Missed")
    SoundsState.playMissed
    removeShip(enemy)
  }

  def removeShip(enemy: EnemyControl) {
    enemies = enemies.filterNot(b => b == enemy)
    enemy.getSpatial.removeFromParent
  }

  def earnScore() {
    score += 10 * multiplier
  }

  def looseALife() {
    for (i <- 1 to 8) {
      showExplosion(shipX - K.shipWidth / 2f + K.shipWidth * Random.nextFloat(),
        shipY - K.shipHeight / 2f + K.shipHeight * Random.nextFloat())
    }
    // decrease difficulty a little
    timeToWait = Math.min(K.initTimeToWait, timeToWait + 25f * K.timeAcceleration)
    // partial reset
    lives = lives - 1
    //    combo = 0
    multiplier = 1
    enemies.foreach(b => b.getSpatial.removeFromParent())
    enemies = List()
    SoundsState.playEnterpriseExplosion
    CaptainMoodState.set(CaptainMood.Dissapointed())
    if (isGameOver) {
      ship.foreach(s => s.getSpatial.removeFromParent())
      showNotice("Game over")
    } else {
      ship.foreach(_.respawn)
      showNotice("Life lost")
    }
  }

  def shipY = ship.map(p => p.getSpatial.getLocalTranslation.y).getOrElse(0f)
  def shipX = ship.map(p => p.getSpatial.getLocalTranslation.x).getOrElse(0f)
  def enemyX(enemy: EnemyControl) = enemy.getSpatial.getLocalTranslation.x
  def enemyY(enemy: EnemyControl) = enemy.getSpatial.getLocalTranslation.y

  def isAtShipHeight(enemy: EnemyControl) = {
    val enemyY = enemy.getSpatial.getLocalTranslation.y
    enemyY - K.enemyHeight / 2f < shipY + K.shipHeight / 2f &&
      enemyY + K.enemyHeight > shipY - K.shipHeight / 2f
  }

  val v1 = new Vector2f
  val v2 = new Vector2f

  def isNear(x1: Float, y1: Float, x2: Float, y2: Float, r: Float) = {
    val radius = K.shipWidth // check for distance
    v1.setX(x1)
    v1.setY(y1)
    v2.setX(x2)
    v2.setY(y2)
    v1.distance(v2) < r
  }

  def isCrashing(enemy: EnemyControl) = {
    isNear(
      enemy.getSpatial.getLocalTranslation.x,
      enemy.getSpatial.getLocalTranslation.y,
      shipX,
      shipY,
      K.shipWidth) // check for distance
  }

  def isTooFarBehind(enemy: EnemyControl) = {
    val enemyY = enemy.getSpatial.getLocalTranslation.y
    enemyY < -K.enemyHeight / 2f
  }

  def isEnemyTargetedByPhaser(enemy: EnemyControl) = {
    enemyY(enemy) > K.shipPosY &&
      enemyX(enemy) - K.enemyWidth / 2f < shipX &&
      enemyX(enemy) + K.enemyWidth / 2f > shipX
  }
  def isShipTargetedByPhaser(enemy: EnemyControl) = {
    enemyY(enemy) > K.shipPosY &&
      shipX - K.shipWidth / 2f < enemyX(enemy) &&
      shipX + K.shipWidth / 2f > enemyX(enemy)
  }
  def isEnemyHitByTorpedo(enemy: EnemyControl, torpedo: TorpedoControl) = {
    isNear(
      enemyX(enemy),
      enemyY(enemy),
      torpedo.getSpatial.getLocalTranslation.x,
      torpedo.getSpatial.getLocalTranslation.y,
      K.enemyWidth / 2f)
  }

  def isInDangerZone() = {
    ship.map(s => s.shield < 1f).getOrElse(false)
  }

}