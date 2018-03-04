package com.stovokor.domain

trait IsInteractive {
  def interact = {
    if (ready) {
      receiveInteraction
      setInteractionTime(System.currentTimeMillis)
    }
  }

  def ready = {
    val lastTime = getInteractionTime
    val currentTime = System.currentTimeMillis
    lastTime < 0 || currentTime - lastTime > minTimeBetweenInteractions
  }

  def setInteractionTime(t: Long)
  def getInteractionTime: Long
  def minTimeBetweenInteractions: Long
  def receiveInteraction
}