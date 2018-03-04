package com.stovokor.domain

import com.jme3.math.Vector3f

trait CanBeHit {
  def receiveHit(damage: Int, direction: Vector3f) = {
    if (!isDead) {
      val newHealth = decreaseHealth(damage)
      println("ouch " + newHealth)
      if (newHealth == 0) die
      else reactToHit(direction)

    }
  }
  def decreaseHealth(i: Int) = {
    val hp = getHealth
    val ar = getArmor
    val arHit = (armorFactor * i) toInt
    val hpHit = (healthFactor * i + Math.min(0, arHit)) toInt
    val newHp = Math.max(0, hp - hpHit)
    val newAr = Math.max(0, ar - arHit)

    setHealth(newHp)
    setArmor(newAr)
    newHp
  }

  val armorFactor = 0.67f
  val healthFactor = 1f - armorFactor

  def getHealth: Int
  def setHealth(h: Int)

  def getArmor: Int = 0
  def setArmor(a: Int) {}

  def die
  def isDead: Boolean

  def reactToHit(direction: Vector3f) {}
}

