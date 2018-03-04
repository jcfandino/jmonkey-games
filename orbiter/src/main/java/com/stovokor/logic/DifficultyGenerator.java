package com.stovokor.logic;

import com.stovokor.domain.K;
import com.stovokor.logic.sequence.ColorSequence;
import com.stovokor.logic.sequence.TrajectoryFactory;
import com.stovokor.logic.sequence.ColorSequenceFactory;
import com.stovokor.logic.sequence.TrajectoryFactory2;
import com.stovokor.math.Trajectory;

public class DifficultyGenerator {
	
	private float diff;
	private ColorSequenceFactory colorSeqFactory;
	private TrajectoryFactory2 trajectoryFactory;
	
	public DifficultyGenerator(float initialDifficulty) {
		this.diff = initialDifficulty;
		colorSeqFactory = new ColorSequenceFactory();
		trajectoryFactory = new TrajectoryFactory2();
	}
	
	public float getBallProbability() {
		return K.getBallProbability() * diff;
	}

	public float getBallInterval() {
		float i = K.getMaxBallInterval() / (diff);
		return Math.max(i, K.getMinBallInterval());
	}
	
	public void increase(float tpf) {
		diff += tpf * K.getDifficultyIncSpeed();
	}
	
	public float getDifficulty() {
		return diff;
	}
	
	public ColorSequence getColorSequence() {
		return colorSeqFactory.create(diff);
	}
	
	public Trajectory getTrajectory() {
		return trajectoryFactory.create(diff);
	}

}
