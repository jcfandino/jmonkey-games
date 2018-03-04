package com.stovokor.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Plane;
import com.jme3.math.Plane.Side;
import com.jme3.math.Vector3f;
import com.stovokor.GameContext;
import com.stovokor.domain.BallSequence;
import com.stovokor.domain.ColorBall;
import com.stovokor.domain.K;
import com.stovokor.domain.ShieldColor;
import com.stovokor.domain.Ship;
import com.stovokor.domain.ShipShield;
import com.stovokor.logic.animation.Animation;
import com.stovokor.logic.animation.BallExplodeAnimation;
import com.stovokor.logic.animation.BallImplodeAnimation;
import com.stovokor.math.CircularTrajectory;
import com.stovokor.math.Param;
import com.stovokor.math.Trajectory;

public class InGameAppState extends AbstractAppState {
	
	private GameContext context;
	private DifficultyGenerator difficultyGenerator;
	private List<Animation> animations;
	
	public InGameAppState(GameContext context) {
		this.context = context;
		this.activeBalls = new ArrayList<ColorBall>();
		this.difficultyGenerator = new DifficultyGenerator(K.getInitialDifficulty());
		this.animations = new ArrayList<Animation>();
	}
	
	@Override
	public void update(float tpf) {
		Ship ship = context.getShip();
		
		//Move the ship
		Trajectory shipTrajectory = new CircularTrajectory(
			K.getCenter().add(K.getPlanetAxis().mult(ship.getLean().value())),
			K.getPlanetRadius() + ship.getAltitude().value(),
			K.getPlanetAxis()
		);
		Param param = context.getParam();
		context.setParam(param.increase(K.getOneOverSixty() * ship.getRotationSpeed().value() * tpf));
		Vector3f pos = shipTrajectory.getPoint(param);
		shipTrajectory.updateSpatial(ship.getSpatial(), param);
		
		//Move the camera
		Trajectory cameraTrajectory = new CircularTrajectory(
			K.getCenter().add(K.getPlanetAxis().mult(ship.getCamLean().value())),
			K.getPlanetRadius() + ship.getCamAltitude().value(),
			K.getPlanetAxis()
		);
		context.getCamera().setLocation(cameraTrajectory.getPoint(context.getParam().decrease(K.getCameraParamDelta())));
		context.getCamera().lookAt(pos, pos.add(K.getCenter().negate()).normalize());
		
		//Set the shield color
		ShieldColor sc = ShieldColor.fromMask(ship.isRedEnabled(), ship.isGreenEnabled(), ship.isBlueEnabled());
		ship.getShield().setColor(sc);
		
		//Add balls
		addBalls(tpf);
		
		//Check balls
		checkBalls();
		
		//Increase difficulty
		difficultyGenerator.increase(tpf);
		
		//update animations
		List<Animation> newList= new ArrayList<Animation>();
		Iterator<Animation> it = animations.iterator();
		while(it.hasNext()) {
			Animation anim = it.next();
			Animation updated = anim.updated(tpf);
			if(updated.isAlive()) {
				newList.add(updated);
			}
		}
		animations = newList;
		
		//Temp
		context.getScore().setDiff(difficultyGenerator.getDifficulty());
	}

	
	
	//check balls logic
	private Collection<ColorBall> activeBalls;
	
	/**
	 * TODO Optimize adding some ordering. <br>
	 * eg: checking the orbit angle
	 */
	private void checkBalls() {
		Iterator<ColorBall> it = activeBalls.iterator();
		ShipShield shield = context.getShip().getShield();
		
		CircularTrajectory ct = new CircularTrajectory(K.getCenter(), K.getPlanetRadius(), K.getPlanetAxis());
		Param cameraDelta = context.getParam().decrease(K.getCameraParamDelta());
		Plane pi = new Plane();
		Vector3f p1 = K.getCenter();
		Vector3f p2 = ct.getPoint(cameraDelta);
		Vector3f p3 = K.getPlanetAxis().add(K.getCenter());
		pi.setPlanePoints(p1, p2, p3);
		
		while(it.hasNext()) {
			ColorBall ball = it.next();
			if(ball.collidesWith(shield)) {
				if(ball.getColor().equals(shield.getColor())) {
					context.getScore().ballGrabbedRightColor();
					implodeBall(it,ball);
				} else {
					context.getScore().ballGrabbedWrongColor();
					explodeBall(it, ball);
				}
			} else if(pi.whichSide(ball.getSpatial().getWorldTranslation()).equals(Side.Positive)) {
				removeBall(it, ball);
				context.getScore().ballMissed();
			}
			
		}
	}

	private void explodeBall(Iterator<ColorBall> it, ColorBall ball) {
		animations.add(new BallExplodeAnimation(ball, context.getRootNode()));
		it.remove();
	}
	
	private void implodeBall(Iterator<ColorBall> it, ColorBall ball) {
		animations.add(new BallImplodeAnimation(ball, context.getRootNode(), context));
		it.remove();
	}

	public void removeBall(Iterator<ColorBall> it, ColorBall ball) {
		it.remove();
		ball.kill();
		context.getRootNode().detachChild(ball.getSpatial());
	}

	
	//Add balls logic
	private float lastParamValueWhenAddedBalls = 0;
	
	private void addBalls(float tpf) {
		float r = (float) Math.random();
		Param param = context.getParam();

		if(r < difficultyGenerator.getBallProbability() * tpf) {
			float passed = param.value() >= lastParamValueWhenAddedBalls ?
					param.value() - lastParamValueWhenAddedBalls :
					param.value() + (1f - lastParamValueWhenAddedBalls);
			
			if(passed > difficultyGenerator.getBallInterval()) {
				lastParamValueWhenAddedBalls = param.value();
				BallSequence sequence = new BallSequence(param, difficultyGenerator, context.getAssetManager());
				float caca = (float)Math.random();
				int i = 0;
				while(sequence.hasNext()) {
					System.out.println(caca + "-" + i++);
					ColorBall ball = sequence.next();
					context.getRootNode().attachChild(ball.getSpatial());
					activeBalls.add(ball);
					animations.addAll(ball.getAnimations());
				}
			}
		}
	}
	
}
