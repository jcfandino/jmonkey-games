package com.stovokor.domain;

import java.util.ArrayList;
import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.stovokor.logic.animation.Animation;
import com.stovokor.logic.animation.JewelAnimation;
import com.stovokor.math.SimpleBounding;
import com.stovokor.math.SimpleCollidable;
import com.stovokor.math.SphericBounding;

public class ColorBall implements WorldObject, SimpleCollidable {

	private Node node;
	private ShieldColor color;
	private List<Animation> anims;
	
	public ColorBall(AssetManager assetManager, ShieldColor color) {
		node = new Node();
		
		this.color = color;
		ColorRGBA colorRgba = color.getColor().clone();
		colorRgba.a = 0.1f;
		Sphere sphere = new Sphere(12,12,1);
		Spatial shield = new Geometry("shield", sphere);
		ColorRGBA gColor = color.getColor().clone();
		gColor.r = gColor.r * 0.75f + 0.25f;
		gColor.g = gColor.g * 0.75f + 0.25f;
		gColor.b = gColor.b * 0.75f + 0.25f;
		gColor.a = 0.8f;
		
		Material shieldMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		shieldMaterial.setColor("Color", colorRgba);
		shieldMaterial.setColor("GlowColor", gColor);
		shieldMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		shield.setMaterial(shieldMaterial);
		
		shield.setQueueBucket(Bucket.Transparent);
		shield.setLocalTranslation(0, -1f, 0);
		shield.setLocalScale(0.8f, 0.8f, 0.8f);
		
		
//        ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 200);
//        emit.setShape(new EmitterPointShape(new Vector3f(0, -1f, 0)));
//        emit.setGravity(0);
//        emit.setLowLife(1);
//        emit.setHighLife(5);
//        emit.setInitialVelocity(new Vector3f(0, 0, 0));
//        emit.setImagesX(15);
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
//        mat.setTexture("GlowMap", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
//		mat.setColor("GlowColor", colorRgba);
//        emit.setMaterial(mat);
		
		
		Sphere shape = new Sphere(3,4,1);
		Spatial jewel = new Geometry("jewel", shape);
//		Material jewelMaterial = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
		Material jewelMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture texture = assetManager.loadTexture("Effects/Explosion/flame.png");
		jewelMaterial.setTexture("ColorMap", texture);
		
//		ColorRGBA jewelColor = colorRgba.clone();
//		jewelColor.r = 0f;//jewelColor.r * 0.4f;
//		jewelColor.g = 0f;//jewelColor.g * 0.4f;
//		jewelColor.b = 0f;//jewelColor.b * 0.4f;
//		jewelColor.a = 0.1f;
//		jewelMaterial.setColor("Color", colorRgba);
//		jewelMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Color);
		
		jewel.setMaterial(jewelMaterial);
		
		jewel.setQueueBucket(Bucket.Transparent);
		jewel.setLocalTranslation(0, -1f, 0);
		jewel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
		jewel.setLocalScale(0.4f, 0.7f, 0.4f);
		
		node.attachChild(jewel);
		node.attachChild(shield);
		
		anims = new ArrayList<Animation>();
		anims.add(new JewelAnimation(jewel));
		
		
	}
	
	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
	}
	
	@Override
	public void onAnalog(String name, float isPressed, float tpf) {
	}
	
	@Override
	public Spatial getSpatial() {
		return node;
	}
	
	@Override
	public void configureInputs(InputManager inputManager) {
	}
	
	public ShieldColor getColor() {
		return color;
	}
	
	public void setPosition(Vector3f point) {
		node.setLocalTranslation(point);
	}
	
	@Override
	public boolean collidesWith(SimpleCollidable other) {
		return getBounding().collidesWith(other);
	}

	@Override
	public SimpleBounding getBounding() {
		return new SphericBounding(node.getWorldTranslation(), node.getWorldScale().getX());
	}

	@Override
	public List<Animation> getAnimations() {
		return anims;
	}

	@Override
	public void kill() {
		for(Animation anim : anims) {
			anim.kill();
		}
		anims.clear();
	}

}


