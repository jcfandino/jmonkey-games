package com.stovokor.control

import com.jme3.math.ColorRGBA

trait HasColor {

  def color: ColorRGBA

  def sameColor(other: HasColor) = other.color == color
}