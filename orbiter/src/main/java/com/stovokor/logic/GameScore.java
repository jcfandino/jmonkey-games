package com.stovokor.logic;

public class GameScore {

	private int score;
	private float diff;

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}
	
	public void ballGrabbedRightColor() {
		score += 10;
	}
	
	public void ballGrabbedWrongColor() {
		score -= 15;
	}
	
	public void ballMissed() {
		score -= 2;
	}

	public void setDiff(float diff) {
		this.diff = diff;
	}

	public float getDiff() {
		return diff;
	}
	
}
