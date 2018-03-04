package com.stovokor.logic.animation

trait Animation {
  def updated(tpf: Float): Animation
  def isAlive: Boolean = true
  def kill: Animation = KilledAnimation
}

object KilledAnimation extends Animation {
  def updated(f: Float) = KilledAnimation
  override def isAlive = false
}
