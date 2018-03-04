package com.stovokor.domain;

import com.jme3.math.Vector3f;

public class K {
	private static final float CAMERA_PARAM_DELTA = 0.01f;
	private static final float LEAN_SPEED = 0.2f;
	private static final float PLANET_RADIUS = 100f;
	private static final float MAX_SHIP_LEAN = 10f;
	private static final float MAX_CAM_LEAN = 8f;
	private static final Vector3f CENTER = new Vector3f(0,0,0);
	private static final Vector3f PLANET_AXIS = Vector3f.UNIT_Z.negate();

	
	private static final float MIN_SHIP_ALT = 0.2f;
	private static final float MAX_SHIP_ALT = 6f;
	private static final float ALT_SPEED = 0.1f;//100f;
	
	private static final float MIN_CAM_ALT = MIN_SHIP_ALT + 2f;
	private static final float MAX_CAM_ALT = MAX_SHIP_ALT + 1f;
	
	//in revolutions per minute
	private static final float MIN_ROT_SPEED = 0;//4f;
	private static final float MAX_ROT_SPEED = 8f;
	private static final float ROT_ACCELERATION = 2f;
	
	private static final float INITIAL_DIFFICULTY = 1f; // TODO Has to be a setting
	private static final float DIFFICULTY_INC_SPEED = 0.01f; // units per second
	// prob of one sequence in a second
	private static final float BALL_PROBABILITY = 1.0f;
	private static final float MIN_BALL_INTERVAL = 1f / 16f;
	private static final float MAX_BALL_INTERVAL = 3f / 16f;
	//math constants
	private static final float ONE_OVER_SIXTY = 1f / 60f;
	
	//ball sequences
	private static final float BALL_SEQ_INTERVAL = 1f / 64f; // of 2 pi
	private static final int BALLS_PER_SEQUENCE = 5;
	
	
	public static float getLeanSpeed() {
		return LEAN_SPEED;
	}
	public static float getAltSpeed() {
		return ALT_SPEED;
	}
	public static float getPlanetRadius() {
		return PLANET_RADIUS;
	}
	public static float getMinShipAlt() {
		return MIN_SHIP_ALT;
	}
	public static float getMaxShipAlt() {
		return MAX_SHIP_ALT;
	}
	public static float getMaxShipLean() {
		return MAX_SHIP_LEAN;
	}
	public static float getMaxCamLean() {
		return MAX_CAM_LEAN;
	}
	public static float getMinCamAlt() {
		return MIN_CAM_ALT;
	}
	public static float getMaxCamAlt() {
		return MAX_CAM_ALT;
	}
	public static float getMinRotSpeed() {
		return MIN_ROT_SPEED;
	}
	public static float getMaxRotSpeed() {
		return MAX_ROT_SPEED;
	}
	public static float getRotAcceleration() {
		return ROT_ACCELERATION;
	}
	public static Vector3f getCenter() {
		return CENTER;
	}
	public static Vector3f getPlanetAxis() {
		return PLANET_AXIS;
	}
	public static float getCameraParamDelta() {
		return CAMERA_PARAM_DELTA;
	}
	public static float getOneOverSixty() {
		return ONE_OVER_SIXTY;
	}
	public static float getBallProbability() {
		return BALL_PROBABILITY;
	}
	public static float getMinBallInterval() {
		return MIN_BALL_INTERVAL;
	}
	public static float getMaxBallInterval() {
		return MAX_BALL_INTERVAL;
	}
	public static float getInitialDifficulty() {
		return INITIAL_DIFFICULTY;
	}
	public static float getDifficultyIncSpeed() {
		return DIFFICULTY_INC_SPEED;
	}
	public static float getBallSequenceInterval() {
		return BALL_SEQ_INTERVAL;
	}
	public static int getBallsPerSquence() {
		return BALLS_PER_SEQUENCE;
	}

}
