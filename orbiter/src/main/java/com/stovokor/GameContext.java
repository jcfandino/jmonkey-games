package com.stovokor;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.stovokor.domain.Planet;
import com.stovokor.domain.Ship;
import com.stovokor.logic.GameScore;
import com.stovokor.math.Param;

public class GameContext {
	private Planet planet;
	private Ship ship;
	private Param param;
	private Camera camera;
	private AssetManager assetManager;
	private Node rootNode;
	private GameScore score;
	private Node guiNode;
	private GameHud hud;
	private ViewPort viewPort;
	
	public GameContext() {
		this.param = new Param(0);
	}
	

	public Planet getPlanet() {
		return planet;
	}

	public void setPlanet(Planet planet) {
		this.planet = planet;
	}

	public Ship getShip() {
		return ship;
	}

	public void setShip(Ship ship) {
		this.ship = ship;
	}

	public Param getParam() {
		return param;
	}

	public void setParam(Param param) {
		this.param = param;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}
	
	public AssetManager getAssetManager() {
		return assetManager;
	}
	
	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
	
	public Node getRootNode() {
		return rootNode;
	}
	
	public void setScore(GameScore score) {
		this.score = score;
	}
	
	public GameScore getScore() {
		return score;
	}
	
	public void setGuiNode(Node guiNode) {
		this.guiNode = guiNode;
	}
	
	public Node getGuiNode() {
		return guiNode;
	}
	
	public void setHud(GameHud gameHud) {
		this.hud = gameHud;
	}
	
	public GameHud getHud() {
		return hud;
	}
	
	public void setViewPort(ViewPort viewPort) {
		this.viewPort = viewPort;
	}
	
	public ViewPort getViewPort() {
		return viewPort;
	}
}