package com.stovokor.math

case class Param(val value: Float) {

  def increase(delta: Float): Param = {
    val dt = delta - delta.toInt
    val next = value + dt
    if (next > 1.0) Param(next - next.toInt)
    else if (next < 0) Param(1F + next)
    else Param(next)
  }

  def decrease(delta: Float): Param = increase(-delta)

}
