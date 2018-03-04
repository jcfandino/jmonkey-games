package com.stovokor.domain;

import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.stovokor.logic.animation.Animation;
import com.stovokor.math.RangedValue;

public class Ship implements WorldObject {

	private RangedValue shipAltitude = new RangedValue(K.getMinShipAlt(),
			K.getMaxShipAlt());
	private RangedValue rotationSpeed = new RangedValue(K.getMinRotSpeed(),
			K.getMaxRotSpeed());
	private RangedValue shipLean = new RangedValue(-K.getMaxShipLean(),
			K.getMaxShipLean());

	private RangedValue camAltitude = new RangedValue(K.getMinCamAlt(),
			K.getMaxCamAlt());
	private RangedValue camLean = new RangedValue(-K.getMaxCamLean(),
			K.getMaxCamLean());

	private Node node;
	private Spatial theShip;
	private ShipShield shield;

	private boolean redEnabled;

	private boolean greenEnabled;
	private boolean blueEnabled;
	private ParticleEmitter emit;

	public Ship(AssetManager assetManager) {

		node = new Node();
		theShip = assetManager.loadModel("Models/Teapot/Teapot.obj");
		Material mat_default = new Material(assetManager,
				"Common/MatDefs/Misc/ShowNormals.j3md");
		theShip.setMaterial(mat_default);
		theShip.setLocalTranslation(0, -1f, 0);
		theShip.setLocalRotation(new Quaternion().fromAngleAxis(
				-FastMath.HALF_PI, new Vector3f(0, 0, 1)));

		shield = new ShipShield(assetManager);

		// emit = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle,
		// 1400);
		// emit.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
		// emit.setGravity(4);
		// emit.setLowLife(1);
		// emit.setHighLife(2);
		// emit.setInitialVelocity(new Vector3f(0, 0, 0));
		// emit.setImagesX(15);
		// Material mat = new Material(assetManager,
		// "Common/MatDefs/Misc/Particle.j3md");
		// mat.setTexture("Texture",
		// assetManager.loadTexture("Effects/Smoke/Smoke.png"));
		// emit.setMaterial(mat);
		// emit.setIgnoreTransform(false);
		// emit.setInWorldSpace(false);
		//
		// ParticleEmitter fire = new ParticleEmitter("fireball",
		// ParticleMesh.Type.Triangle, 20);
		// Sphere sphere = new Sphere(20, 20, 1f);
		// fire.setMesh(sphere);
		// Material mat_red = new Material(assetManager,
		// "Common/MatDefs/Misc/Particle.j3md");
		// mat_red.setTexture("Texture",assetManager.loadTexture("Effects/Explosion/flame.png"));
		// fire.setMaterial(mat_red);
		// fire.setImagesX(2);
		// fire.setImagesY(2); // 2x2 texture animation
		// fire.setEndColor(new ColorRGBA(0.4f, 0.4f, 1f, 1f)); // red
		// fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
		// fire.setInitialVelocity(new Vector3f(0, 0, 0));
		// fire.setStartSize(1f*1.5f);
		// fire.setEndSize(1.2f*1.5f);
		// fire.setGravity(0);
		// fire.setLowLife(100f);
		// fire.setHighLife(100f);
		// fire.setVelocityVariation(0f);
		// fire.setRotateSpeed(5f);
		// fire.setIgnoreTransform(false);
		// fire.setInWorldSpace(false);

		// node.attachChild(fire);
		node.attachChild(theShip);
		// node.attachChild(emit);
		node.attachChild(shield.getSpatial());

	}

	@Override
	public Spatial getSpatial() {
		return node;
	}

	@Override
	public void configureInputs(InputManager inputManager) {

		inputManager.addMapping("Altitude+", new MouseAxisTrigger(
				MouseInput.AXIS_Y, false));
		inputManager.addMapping("Altitude-", new MouseAxisTrigger(
				MouseInput.AXIS_Y, true));
		inputManager.addMapping("Lean+", new MouseAxisTrigger(
				MouseInput.AXIS_X, true));
		inputManager.addMapping("Lean-", new MouseAxisTrigger(
				MouseInput.AXIS_X, false));
		inputManager.addMapping("Speed+", new MouseAxisTrigger(
				MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping("Speed-", new MouseAxisTrigger(
				MouseInput.AXIS_WHEEL, true));

		inputManager.addMapping("Red", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Green", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Blue", new KeyTrigger(KeyInput.KEY_D));

		inputManager.addListener(this, "Altitude+", "Altitude-", "Lean+",
				"Lean-", "Speed+", "Speed-");
		inputManager.addListener(this, "Red", "Green", "Blue");

	}

	public RangedValue getLean() {
		return shipLean;
	}

	public RangedValue getAltitude() {
		return shipAltitude;
	}

	public RangedValue getRotationSpeed() {
		return rotationSpeed;
	}

	public RangedValue getCamLean() {
		return camLean;
	}

	public RangedValue getCamAltitude() {
		return camAltitude;
	}

	public boolean isRedEnabled() {
		return redEnabled;
	}

	public boolean isGreenEnabled() {
		return greenEnabled;
	}

	public boolean isBlueEnabled() {
		return blueEnabled;
	}

	public ShipShield getShield() {
		return shield;
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("Red")) {
			redEnabled = isPressed;
		}
		if (name.equals("Green")) {
			greenEnabled = isPressed;
		}
		if (name.equals("Blue")) {
			blueEnabled = isPressed;
		}
	}

	@Override
	public void onAnalog(String name, float isPressed, float tpf) {
		if (name.equals("Altitude+")) {
			shipAltitude = shipAltitude.increase(K.getAltSpeed());
			camAltitude = camAltitude.setFromPercent(shipAltitude.getPercent());
		}
		if (name.equals("Altitude-")) {
			shipAltitude = shipAltitude.decrease(K.getAltSpeed());
			camAltitude = camAltitude.setFromPercent(shipAltitude.getPercent());
		}
		if (name.equals("Lean+")) {
			shipLean = shipLean.increase(K.getLeanSpeed());
			camLean = camLean.setFromPercent(shipLean.getPercent());
		}
		if (name.equals("Lean-")) {
			shipLean = shipLean.decrease(K.getLeanSpeed());
			camLean = camLean.setFromPercent(shipLean.getPercent());
		}
		if (name.equals("Speed+")) {
			rotationSpeed = rotationSpeed.increase(K.getRotAcceleration());
		}
		if (name.equals("Speed-")) {
			rotationSpeed = rotationSpeed.decrease(K.getRotAcceleration());
		}
	}

	@Override
	public List<Animation> getAnimations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub

	}

}
