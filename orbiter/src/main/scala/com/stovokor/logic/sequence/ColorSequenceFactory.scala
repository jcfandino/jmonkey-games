package com.stovokor.logic.sequence

import scala.util.Random

class ColorSequenceFactory {
  private val TWO_EASY_ALT = 1.5f;
  private val TWO_HARD_ALT = 2f;
  private val ALL_RANDOM = 3f;

  val twoEasyAlt = new TwoEasyAlternatedColorSequenceFactory
  val twoHardAlt = new TwoHardAlternatedColorSequenceFactory

  def create(level: Float): ColorSequence = {
    val r = new Random().nextFloat * level;
    if (r < TWO_EASY_ALT) new PlainColorSequence()
    else if (r < TWO_HARD_ALT) twoEasyAlt.create
    else if (r < ALL_RANDOM) twoHardAlt.create
    else new AllRandomColorSequence()
  }
}