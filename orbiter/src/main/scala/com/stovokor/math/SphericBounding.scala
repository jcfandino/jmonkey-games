package com.stovokor.math

import com.jme3.math.Vector3f

class SphericBounding(val pos: Vector3f, val rad: Float) extends SimpleBounding {

  def collidesWith(other: SimpleCollidable): Boolean = {
    if (other == this) true
    else {
      val otherBounding = other.getBounding()
      val centerDistance = otherBounding.getPosition().distance(pos)
      centerDistance <= otherBounding.getRadius() + rad
    }
  }

  def getBounding = SphericBounding.this

  def getRadius = rad
  def getPosition = pos
}