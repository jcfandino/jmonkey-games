package com.stovokor.domain;

import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.stovokor.logic.animation.Animation;

public class Planet implements WorldObject {

	private Spatial planet;

	public Planet(AssetManager assetManager) {
		Sphere sph = new Sphere(100, 100, K.getPlanetRadius());
		planet = new Geometry("planet", sph);
		Material sphMat = new Material(assetManager,
				"Common/MatDefs/Light/Lighting.j3md");
		// sphMat.getAdditionalRenderState().setWireframe(true);
		sphMat.setTexture("DiffuseMap",
				assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
		sphMat.setTexture("NormalMap",
				assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
		planet.setMaterial(sphMat);
		planet.setLocalTranslation(K.getCenter());
	}

	@Override
	public Spatial getSpatial() {
		return planet;
	}

	@Override
	public void configureInputs(InputManager inputManager) {
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
	}

	@Override
	public void onAnalog(String name, float isPressed, float tpf) {
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
