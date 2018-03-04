package com.stovokor.math

case class RangedValue(min: Float, max: Float, val value: Float) {

  def this(min: Float, max: Float) = this(min, max, min + max / 2)

  def increase(delta: Float) = {
    val newValue = value + delta
    if (newValue < min) RangedValue(min, max, min)
    else if (newValue > max) RangedValue(min, max, max)
    else RangedValue(min, max, newValue)
  }

  def decrease(delta: Float) = increase(-delta)

  def setFromPercent(p: Float) =
    RangedValue(min, max, min + p * (max - min))

  def getPercent = (value - min) / (max - min)

  override def toString = value.toString
  
}