package com.stovokor

object K {

  // Gameplay
  val columns = 10
  val easyTime = 20f
  val initLives = 3
  val initTimeToWait = 4f
  val timeAcceleration = 0.1f
  val minTimeToWait = 1f

  val padDangerZoneFactor = 0.75f
  val missedPenalty = 1f
  val grabbedHelp = 0.5f

  // Bricks
  val brickInitSpeed = 4f
  val brickSpeedAcceleration = 0.1f
  val brickMaxSpeed = 10f
  val brickStart = 20f

  val brickWidth = 1.5f
  val brickHeight = .6f

  // Pad
  val padWidth = 1.8f
  val padHeight = .6f
  val padPosY = 0.2f
  val padMaxY = brickStart - 2f

  val padSpeed = 10f
  val padUpSpeed = 0.1f

  // handy calculations
  val minCol = -columns / 2
  val maxCol = if (columns % 2 == 0) -minCol - 1 else -minCol

  val padMinPos = brickWidth * minCol
  val padMaxPos = brickWidth * maxCol
  val padDangerZone = padMaxY * padDangerZoneFactor

  val middle = (padMinPos + padMaxPos) / 2f
}