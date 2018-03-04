package com.stovokor.util.math

import scala.compat.Platform

class Random(seed: Long) extends scala.util.Random(seed) {
}

object Dist {
  def normalInt(random: Random, min: Int, max: Int) =
    random.nextInt(max - min) + min // TODO This is linear

  def normalFloat(random: Random, min: Float, max: Float) =
    random.nextGaussian.toFloat * (max - min) + min

  def lineal(min: Float, max: Float, top: Float): Float => Float = {
    val delta = max / top
    t => if (t > top) max else Math.max(t * delta, min)
  }
}

object Random {

  val singleton = new Random(Platform.currentTime)

  def apply() = singleton

  def apply(seed: Long, number: Int) = {
    (for (r <- Seq(new Random(seed)); n <- 0 to number) yield { r.nextBoolean(); r }).head
  }
  def apply(seed: Long, number: Int, index: Int) = {
    (for (r <- Seq(new Random(seed)); i <- 0 to number; j <- 0 to index) yield { r.nextBoolean(); r }).head
    //new Random(seed * number * (index + 1))
  }
}
