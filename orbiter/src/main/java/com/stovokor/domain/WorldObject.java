package com.stovokor.domain;

import java.util.List;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Spatial;
import com.stovokor.logic.animation.Animation;

public interface WorldObject extends ActionListener, AnalogListener {

	public Spatial getSpatial();
	
	
	public List<Animation> getAnimations();
	public void kill();
//	public Collidable getCollidable();
	
	public void configureInputs(InputManager inputManager);
}
