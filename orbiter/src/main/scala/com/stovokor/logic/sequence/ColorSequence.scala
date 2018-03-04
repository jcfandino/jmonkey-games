package com.stovokor.logic.sequence

import com.stovokor.domain.ShieldColor
import scala.util.Random

trait ColorSequence {
  def next: ShieldColor
}

class AllRandomColorSequence extends ColorSequence {
  def next = ShieldColor.random
}

class PlainColorSequence extends ColorSequence {
  lazy val next = ShieldColor.random
}
class TwoAlternatedColorSequence(color1: ShieldColor, color2: ShieldColor) extends ColorSequence {
  var flag = false // TODO Mutable variable

  def next = {
    flag = !flag
    if (flag) color1 else color2
  }
}

class TwoEasyAlternatedColorSequenceFactory {

  def create: TwoAlternatedColorSequence = {
    val rnd = new Random
    val g = rnd.nextBoolean()
    val b = rnd.nextBoolean()
    val r = rnd.nextBoolean() | !(g | b)

    val color1 = ShieldColor.fromMask(r, g, b)

    val rgb2 = if (rnd.nextBoolean()) {
      (!r, g, b)
    } else if (rnd.nextBoolean()) {
      (r, !g, b)
    } else {
      (r, g, !b)
    }

    val color2 = if (!(rgb2._1 | rgb2._2 | rgb2._3)) {
      ShieldColor.blue
    } else {
      ShieldColor.fromMask(rgb2._1, rgb2._2, rgb2._3)
    }
    new TwoAlternatedColorSequence(color1, color2)
  }

}

class TwoHardAlternatedColorSequenceFactory {
  def create: TwoAlternatedColorSequence = {
    val rnd = new Random
    val g = rnd.nextBoolean()
    val b = rnd.nextBoolean()
    val r = rnd.nextBoolean()

    val color1 = 
    if (!(r & g) | !(g & b) | !(r & b)) {
      ShieldColor.fromMask(true, true, b)
    } else {
      ShieldColor.fromMask(r, g, b)
    }
    val color2 = ShieldColor.random();
    
    new TwoAlternatedColorSequence(color1, color2)
  }
}
