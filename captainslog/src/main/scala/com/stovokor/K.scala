package com.stovokor

object K {

  // Gameplay
  val columns = 4
  val easyTime = 2f
  val initLives = 3
  val initTimeToWait = 5f
  val timeAcceleration = 0.1f
  val minTimeToWait = 2f

  val phaserDamage = 10f
  val torpedoDamage = 20f
  val crashDamage = 50f
  val breaksEvery = 30f
  val breakTime = 8f

  val shieldDangerZoneFactor = 0.2f

  // Enemies
  val enemyInitSpeed = 1f
  val enemySpeedAcceleration = 0.00f
  val enemyMaxSpeed = 1f
  val enemyStart = 6f

  val enemyWidth = 1.1f
  val enemyHeight = 1.34f

  val enemyHealth = 6f

  val enemyFastSpeed = 5f
  val enemySlowSpeed = 0.75f
  val enemyFastZone = .85f * enemyStart

  // Ship
  val shipWidth = 1f
  val shipHeight = 1.6f

  val shieldWidth = 1.2f
  val shieldHeight = 1.8f

  val shipPosY = .8f
  val shipMaxY = enemyStart - 2f

  val shipSpeed = 10f
  val shipUpSpeed = 0.1f

  val shipInitialHealth = 100f
  val shipInitialShield = 20f
  val shieldRechargeSpeed = 1f

  // Phaser
  val phaserMinEnergyToShoot = 10f
  val phaserMaxEnergy = 100f
  val phaserRechargeSpeed = 5f
  val phaserDischargeSpeed = 10f

  // Torpedo
  val torpedoSpeed = 1.5f
  val initialTorpedos = 10

  // indicator bars
  val barHeight = 0.2f
  val barWidth = 1.2f

  // handy calculations
  val minCol = -columns / 2
  val maxCol = if (columns % 2 == 0) -minCol - 1 else -minCol

  val shipMinPos = enemyWidth * minCol
  val shipMaxPos = enemyWidth * maxCol

  val middle = (shipMinPos + shipMaxPos) / 2f
  val xOffset = 0.8f
}