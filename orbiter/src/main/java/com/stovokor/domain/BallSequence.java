package com.stovokor.domain;

import java.util.Iterator;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.stovokor.logic.DifficultyGenerator;
import com.stovokor.logic.sequence.ColorSequence;
import com.stovokor.math.Param;
import com.stovokor.math.Trajectory;

public class BallSequence implements Iterator<ColorBall> {

	
	private Param param;
	private int left;
	private float interval;
	private Trajectory trajectory;
	private AssetManager assetManager;
	private ColorSequence colorSequence;
	
	public BallSequence(Param shipPos, DifficultyGenerator difficultyGenerator, AssetManager assetManager) {
		this.assetManager = assetManager;
		param = shipPos.increase(FastMath.ONE_THIRD / 2f);
		
//		trajectory = new CircularTrajectory(center, K.getPlanetRadius() + altitude, K.getPlanetAxis());
		trajectory = difficultyGenerator.getTrajectory();
		left = K.getBallsPerSquence();
		interval =  K.getBallSequenceInterval();
		
		
//		colorSequence = new PlainColorSequence();
		colorSequence = difficultyGenerator.getColorSequence();
		
	}
	
	
	@Override
	public boolean hasNext() {
		return left > 0;
	}

	@Override
	public ColorBall next() {
		ColorBall colorBall = new ColorBall(assetManager, colorSequence.next());
//		colorBall.setPosition(trajectory.getPoint(param));
		trajectory.updateSpatial(colorBall.getSpatial(), param);
		param = param.increase(interval);
		left--;
		return colorBall;
	}

	@Override
	public void remove() {
	}

}
