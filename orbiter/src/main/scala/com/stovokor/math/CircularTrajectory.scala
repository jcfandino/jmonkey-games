package com.stovokor.math

import com.jme3.math.Vector3f
import com.jme3.math.FastMath
import com.jme3.math.Quaternion

class CircularTrajectory(
    center: Vector3f,
    radGen: ValueGenerator,
    offsetGen: ValueGenerator,
    nor: Vector3f) extends Trajectory {

  def this(center: Vector3f, radius: Float, nor: Vector3f) =
    this(center, new ConstantValueGenerator(radius), new ConstantValueGenerator(0), nor)

  val normal = nor.normalize()
  val yAxis = normal.cross(new Vector3f(1, 0, 0)).normalize()
  val xAxis = yAxis.cross(normal).normalize()

  def getPoint(param: Param) = {
    val ang = FastMath.TWO_PI * param.value;
    val radius = radGen.getValue(param);
    val x = radius * FastMath.cos(ang);
    val y = radius * FastMath.sin(ang);
    center
      .add(xAxis.mult(x))
      .add(yAxis.mult(y))
      .add(normal.mult(offsetGen.getValue(param)));
  }
  
  def getRotation(param:Param) = 
    new Quaternion().fromAngleAxis(FastMath.TWO_PI * param.value, normal);
}