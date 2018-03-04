package com.stovokor;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class CollisionTest extends SimpleApplication {

	public static void main(String[] args) {
		new CollisionTest().start();
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletState = new BulletAppState();
		bulletState.setDebugEnabled(true);
		stateManager.attach(bulletState);
		stateManager.attach(new State());
	}

	private class State extends AbstractAppState {
		private Spatial ball1Spatial;
		private Spatial ball2Spatial;
		private Float counter = 0f;

		@Override
		public void initialize(AppStateManager stateManager, Application app) {
			super.initialize(stateManager, app);
			space().addCollisionListener(new CollisionListener());
			ball1Spatial = createBall();
			ball2Spatial = createBall();
			resetBalls();
		}

		@Override
		public void update(float tpf) {
			counter += tpf;
			if (counter > 1) {
				counter = 0f;
				resetBalls();
			}
		}

		public Spatial createBall() {
			Spatial ball = new Geometry("ball", new Sphere(8, 8, 1));
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", ColorRGBA.Blue);
			ball.setMaterial(mat);
			// The Sphere doesn't register collisions?
			ball.addControl(new RigidBodyControl(new SphereCollisionShape(1), 1));
			// But Box works just fine!
			// ball.addControl(new RigidBodyControl(new BoxCollisionShape(Vector3f.UNIT_XYZ)));
			rootNode.attachChild(ball);
			space().add(ball);
			return ball;
		}

		public void resetBalls() {
			moveTo(ball1Spatial, new Vector3f(-5, 2, 0));
			push(ball1Spatial, Vector3f.UNIT_X);
			moveTo(ball2Spatial, new Vector3f(5, 2, 0));
			push(ball2Spatial, Vector3f.UNIT_X.negate());

		}

		public void moveTo(Spatial ball, Vector3f loc) {
			RigidBodyControl body = ball.getControl(RigidBodyControl.class);
			body.setPhysicsLocation(loc);
			body.clearForces();
			body.setLinearVelocity(Vector3f.ZERO);
		}

		public void push(Spatial ball, Vector3f dir) {
			ball.getControl(RigidBodyControl.class).applyImpulse(dir.mult(50f), Vector3f.ZERO);
		}

		public PhysicsSpace space() {
			return stateManager.getState(BulletAppState.class).getPhysicsSpace();
		}

	}

	private class CollisionListener implements PhysicsCollisionListener {

		@Override
		public void collision(PhysicsCollisionEvent event) {
			System.out.println("When two worlds collide!");
		}

	}
}