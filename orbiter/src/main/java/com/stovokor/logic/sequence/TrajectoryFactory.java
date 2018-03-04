package com.stovokor.logic.sequence;

import com.jme3.math.Vector3f;
import com.stovokor.domain.K;
import com.stovokor.math.BallSequenceValueGenerator;
import com.stovokor.math.CircularTrajectory;
import com.stovokor.math.OblicValueGenerator;
import com.stovokor.math.RangedValue;
import com.stovokor.math.SinoidalValueGenerator;
import com.stovokor.math.Trajectory;
import com.stovokor.math.ValueGenerator;

/**
 * Replaced by TrajectoryFactory2<br>
 * TODO Remove and rename the newer one
 */
@Deprecated
public class TrajectoryFactory {
	
	public Trajectory create(float level) {
		float radius = getRadius();
		float latitude = getLatitude();
		Factory factory = new OblicCircularTrajectoryFactory(radius, latitude);
//		Factory factory = new PlainCircularTrajectoryFactory(radius, latitude);
//		Factory factory = new SinoidalCircularTrajectoryFactory(radius, latitude);
		return factory.create();
	}

	public float getLatitude() {
		float latPercent = (float) Math.random();
		float latitude = new RangedValue( - K.getMaxShipLean(), K.getMaxShipLean()).setFromPercent(latPercent).value();
		return latitude;
	}

	public float getRadius() {
		float altPercent = (float) Math.random();
		float altitude = new RangedValue(K.getMinShipAlt(), K.getMaxShipAlt()).setFromPercent(altPercent).value();
		float radius = K.getPlanetRadius() + altitude;
		return radius;
	}
	
	private interface Factory {
		public Trajectory create();
	}
	
	private class PlainCircularTrajectoryFactory implements Factory {
		private CircularTrajectory trajectory;
		public PlainCircularTrajectoryFactory(float radius, float latitude) {
			Vector3f normal = K.getPlanetAxis().normalize();
			Vector3f center = K.getCenter().add(normal.mult(latitude));
			this.trajectory = new CircularTrajectory(center, radius, normal);
		}
		@Override
		public Trajectory create() {
			return trajectory;
		}
	}
	
	private class OblicCircularTrajectoryFactory implements Factory {
		private CircularTrajectory trajectory;
		public OblicCircularTrajectoryFactory(float radius, float latitude) {
			Vector3f normal = K.getPlanetAxis().normalize();
			Vector3f center = K.getCenter();
			ValueGenerator latitudeGen = getLatitudeGenerator();
			ValueGenerator radiusGen = getRadiusGenerator();
			this.trajectory = new CircularTrajectory(center, radiusGen, latitudeGen, normal);
		}
		private ValueGenerator getRadiusGenerator() {
			float minRad = K.getPlanetRadius() + K.getMinShipAlt();
			float maxRad = K.getPlanetRadius() + K.getMaxShipAlt();
			RangedValue radRange = new RangedValue(minRad, maxRad);
			float minRadPercent = (float) Math.random();
			float maxRadPercent = (float) Math.random();
			float minRadius = radRange.setFromPercent(minRadPercent).value();
			float maxRadius = radRange.setFromPercent(maxRadPercent).value();
			ValueGenerator radiusGen = new BallSequenceValueGenerator(
				new OblicValueGenerator(minRadius, maxRadius)
			);
			return radiusGen;
		}
		private ValueGenerator getLatitudeGenerator() {
			RangedValue latRange = new RangedValue( - K.getMaxShipLean(), K.getMaxShipLean());
			float minLatPercent = (float) Math.random();
			float maxLatPercent = (float) Math.random();
			float minLatitude = latRange.setFromPercent(minLatPercent).value();
			float maxLatitude = latRange.setFromPercent(maxLatPercent).value();
			ValueGenerator latitudeGen = new BallSequenceValueGenerator(
				new OblicValueGenerator(minLatitude, maxLatitude)
			);
			return latitudeGen;
		}
		@Override
		public Trajectory create() {
			return trajectory;
		}
	}
	
	private class SinoidalCircularTrajectoryFactory implements Factory {
		private CircularTrajectory trajectory;
		public SinoidalCircularTrajectoryFactory(float radius, float latitude) {
			Vector3f normal = K.getPlanetAxis().normalize();
			Vector3f center = K.getCenter();
			ValueGenerator latitudeGen = getLatitudeGenerator();
			ValueGenerator radiusGen = getRadiusGenerator();
			this.trajectory = new CircularTrajectory(center, radiusGen, latitudeGen, normal);
		}
		private ValueGenerator getRadiusGenerator() {
			float minRad = K.getPlanetRadius() + K.getMinShipAlt();
			float maxRad = K.getPlanetRadius() + K.getMaxShipAlt();
			RangedValue radRange = new RangedValue(minRad, maxRad);
			float minRadPercent = (float) Math.random();
			float maxRadPercent = (float) Math.random();
			float minRadius = radRange.setFromPercent(minRadPercent).value();
			float maxRadius = radRange.setFromPercent(maxRadPercent).value();
			ValueGenerator radiusGen = new BallSequenceValueGenerator(
				new SinoidalValueGenerator(minRadius, maxRadius)
			);
			return radiusGen;
		}
		private ValueGenerator getLatitudeGenerator() {
			RangedValue latRange = new RangedValue( - K.getMaxShipLean(), K.getMaxShipLean());
			float minLatPercent = (float) Math.random();
			float maxLatPercent = (float) Math.random();
			float minLatitude = latRange.setFromPercent(minLatPercent).value();
			float maxLatitude = latRange.setFromPercent(maxLatPercent).value();
			ValueGenerator latitudeGen = new BallSequenceValueGenerator(
				new SinoidalValueGenerator(minLatitude, maxLatitude)
			);
			return latitudeGen;
		}
		@Override
		public Trajectory create() {
			return trajectory;
		}
	}
}
