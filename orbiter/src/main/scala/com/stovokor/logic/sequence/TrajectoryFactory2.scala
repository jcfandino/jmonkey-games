package com.stovokor.logic.sequence

import com.stovokor.domain.K
import com.stovokor.math.CircularTrajectory
import com.stovokor.math.ValueGenerator
import com.stovokor.math.RangedValue
import com.stovokor.math.BallSequenceValueGenerator
import com.stovokor.math.OblicValueGenerator
import com.stovokor.math.SinoidalValueGenerator
import com.stovokor.math.Trajectory

// TODO Fix SinoidalCircularTrajectoryFactory
class TrajectoryFactory2 {
  def create(level: Float): Trajectory = {
    val radius = this.getRadius
    val latitude = this.getLatitude
//    new OblicCircularTrajectoryFactory(radius, latitude).create
//    new SinoidalCircularTrajectoryFactory(radius,latitude).create
    new PlainCircularTrajectoryFactory(radius,latitude).create
  }

  def getLatitude: Float = {
    val latPercent = scala.math.random.toFloat
    new RangedValue(-K.getMaxShipLean(), K.getMaxShipLean()).setFromPercent(latPercent).value
  }

  def getRadius: Float = {
    val altPercent = scala.math.random.toFloat
    val altitude = new RangedValue(K.getMinShipAlt(), K.getMaxShipAlt()).setFromPercent(altPercent).value
    K.getPlanetRadius() + altitude;
  }

  trait Factory {
    def create: CircularTrajectory
  }

  class PlainCircularTrajectoryFactory(radius: Float, latitude: Float) extends Factory {
    lazy val create = {
      val normal = K.getPlanetAxis().normalize();
      val center = K.getCenter().add(normal.mult(latitude));
      new CircularTrajectory(center, radius, normal);
    }
  }

  class OblicCircularTrajectoryFactory(radius: Float, latitude: Float) extends Factory {
    lazy val create = {
      val normal = K.getPlanetAxis().normalize();
      val center = K.getCenter();
      def latitudeGen = this.getLatitudeGenerator
      def radiusGen = this.getRadiusGenerator
      new CircularTrajectory(center, radiusGen, latitudeGen, normal)

    }
    def getRadiusGenerator: ValueGenerator = {
      val minRad = K.getPlanetRadius() + K.getMinShipAlt();
      val maxRad = K.getPlanetRadius() + K.getMaxShipAlt();
      val radRange = new RangedValue(minRad, maxRad);
      val minRadPercent = scala.math.random.toFloat
      val maxRadPercent = scala.math.random.toFloat
      val minRadius = radRange.setFromPercent(minRadPercent).value
      val maxRadius = radRange.setFromPercent(maxRadPercent).value
      val radiusGen = new BallSequenceValueGenerator(
        new OblicValueGenerator(minRadius, maxRadius));
      return radiusGen;
    }
    def getLatitudeGenerator: ValueGenerator = {
      val latRange = new RangedValue(-K.getMaxShipLean(), K.getMaxShipLean());
      val minLatPercent = scala.math.random.toFloat
      val maxLatPercent = scala.math.random.toFloat
      val minLatitude = latRange.setFromPercent(minLatPercent).value
      val maxLatitude = latRange.setFromPercent(maxLatPercent).value
      val latitudeGen = new BallSequenceValueGenerator(
        new OblicValueGenerator(minLatitude, maxLatitude));
      return latitudeGen;
    }
  }

  class SinoidalCircularTrajectoryFactory(radius: Float, latitude: Float) extends Factory {
    lazy val create = {
      val normal = K.getPlanetAxis().normalize();
      val center = K.getCenter();
      val latitudeGen = this.getLatitudeGenerator
      val radiusGen = this.getRadiusGenerator
      new CircularTrajectory(center, radiusGen, latitudeGen, normal);
    }
    def getRadiusGenerator: ValueGenerator = {
      val minRad = K.getPlanetRadius() + K.getMinShipAlt();
      val maxRad = K.getPlanetRadius() + K.getMaxShipAlt();
      val radRange = new RangedValue(minRad, maxRad);
      val minRadPercent = scala.math.random.toFloat
      val maxRadPercent = scala.math.random.toFloat
      val minRadius = radRange.setFromPercent(minRadPercent).value
      val maxRadius = radRange.setFromPercent(maxRadPercent).value
      new BallSequenceValueGenerator(
        new SinoidalValueGenerator(minRadius, maxRadius));
    }
    def getLatitudeGenerator: ValueGenerator = {
      val latRange = new RangedValue(-K.getMaxShipLean(), K.getMaxShipLean());
      val minLatPercent = scala.math.random.toFloat
      val maxLatPercent = scala.math.random.toFloat
      val minLatitude = latRange.setFromPercent(minLatPercent).value
      val maxLatitude = latRange.setFromPercent(maxLatPercent).value
      new BallSequenceValueGenerator(
        new SinoidalValueGenerator(minLatitude, maxLatitude));
    }
  }
}