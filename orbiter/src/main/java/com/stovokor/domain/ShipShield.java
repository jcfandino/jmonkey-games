package com.stovokor.domain;

import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.stovokor.logic.animation.Animation;
import com.stovokor.math.SimpleBounding;
import com.stovokor.math.SimpleCollidable;
import com.stovokor.math.SphericBounding;

public class ShipShield implements WorldObject, SimpleCollidable {

	private Geometry theShield;
	private ShieldColor color;
	private Node node;

	public ShipShield(AssetManager assetManager) {
		node = new Node();
		color = ShieldColor.none;
		Sphere shieldSphere = new Sphere(16,16,1);
		theShield = new Geometry("shield", shieldSphere);
		Material shieldMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		shieldMaterial.setColor("Color", color.getColor());
		shieldMaterial.setColor("GlowColor", new ColorRGBA(0.8f, 0.1f, 1f, 1f));
		shieldMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		theShield.setMaterial(shieldMaterial);
		theShield.setQueueBucket(Bucket.Transparent);
		theShield.setLocalTranslation(0, -1f, 0);
		theShield.setLocalScale(0.9f, 1.3f, 0.9f);
	
		// TODO make the texture for the shield
		if(false) {
			Sphere inSphere = new Sphere(16,16,1);
			Geometry inShield = new Geometry("shield", inSphere);
			Material inShieldMat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
			Texture texture = assetManager.loadTexture("Textures/ColoredTex/Monkey.png");
			inShieldMat.setTexture("ColorMap", texture);
			inShieldMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			inShield.setMaterial(inShieldMat);
			inShield.setQueueBucket(Bucket.Transparent);
			inShield.setLocalTranslation(0, -1f, 0);
			inShield.setLocalScale(0.9f * 0.9f, 1.3f * 0.9f, 0.9f * 0.9f);
			node.attachChild(inShield);
		}
		
		node.attachChild(theShield);
	}

	@Override
	public Spatial getSpatial() {
		return node;
	}

	@Override
	public void configureInputs(InputManager inputManager) {
	}
	
	public void setColor(ShieldColor sc) {
		color = sc;
		Material shieldMaterial = theShield.getMaterial();
		
		ColorRGBA color = sc.getColor().clone();
		color.a = 0.1f;
		shieldMaterial.setColor("Color", color);
		
		ColorRGBA gcolor = sc.getColor().clone();
		gcolor.r = gcolor.r * 0.75f + 0.25f;
		gcolor.g = gcolor.g * 0.75f + 0.25f;
		gcolor.b = gcolor.b * 0.75f + 0.25f;
		gcolor.a = 0.8f;
		shieldMaterial.setColor("GlowColor", gcolor);
	}
	
	public ShieldColor getColor() {
		return color;
	}
	
	
	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
	}

	@Override
	public void onAnalog(String name, float isPressed, float tpf) {
	}

	@Override
	public boolean collidesWith(SimpleCollidable other) {
		return getBounding().collidesWith(other);
	}

	@Override
	public SimpleBounding getBounding() {
		return new SphericBounding(theShield.getWorldTranslation(), theShield.getWorldScale().getX());
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
