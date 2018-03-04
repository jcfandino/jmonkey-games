package com.stovokor.math

trait SimpleCollidable {
  def collidesWith(other: SimpleCollidable): Boolean
  def getBounding(): SimpleBounding
}