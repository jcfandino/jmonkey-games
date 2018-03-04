package com.stovokor.math

import com.jme3.math.FastMath
import com.stovokor.domain.K

trait ValueGenerator {

  def getValue(param: Param): Float



}

  // Implementations
  class ConstantValueGenerator(r: Float) extends ValueGenerator {
    def getValue(param: Param) = r
  }

  class OblicValueGenerator(min: Float, max: Float) extends ValueGenerator {
    val lon = max - min

    def getValue(param: Param) = min + (lon * param.value)
  }

  class SinoidalValueGenerator(min: Float, amp: Float) extends ValueGenerator {
    def getValue(param: Param) =
      min + amp * FastMath.sin(param.value * FastMath.TWO_PI)

  }

  class BallSequenceValueGenerator(colab: ValueGenerator) extends ValueGenerator {
    //TODO Remove mutable var
    var parameter: Param = Param(0)

    def getValue(param: Param) = {
      val retval = colab.getValue(parameter)
      parameter = parameter.increase(1f / (K.getBallsPerSquence()))
      retval
    }

  }