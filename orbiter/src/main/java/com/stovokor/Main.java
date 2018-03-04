package com.stovokor;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.stovokor.domain.Planet;
import com.stovokor.domain.Ship;
import com.stovokor.logic.GameScore;
import com.stovokor.logic.HudAppState;
import com.stovokor.logic.InGameAppState;

/**
 * test
 * @author xuan
 */
public class Main extends SimpleApplication {

	private GameContext context;
	private DirectionalLight sun;
	
	public static void main(String[] args) {
		Main app = new Main();
		app.start();
	}
	
	@Override
	public void simpleInitApp() {
		context = new GameContext();
		
		context.setShip(new Ship(assetManager));
		context.setPlanet(new Planet(assetManager));
		context.setCamera(cam);
		context.setAssetManager(assetManager);
		context.setRootNode(rootNode);
		context.setScore(new GameScore());
		context.setGuiNode(guiNode);
		context.setHud(new GameHud(assetManager));
		context.setViewPort(viewPort);
		
		rootNode.attachChild(context.getShip().getSpatial());
		rootNode.attachChild(context.getPlanet().getSpatial());
		
		guiNode.attachChild(context.getHud().getGuiNode());
		
		InGameAppState gameState = new InGameAppState(context);
		gameState.setEnabled(true);
		stateManager.attach(gameState);
		
		HudAppState hudState = new HudAppState(context);
		hudState.setEnabled(true);
		stateManager.attach(hudState);
		
		DirectionalLight sun1 =new DirectionalLight();
		sun1.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		rootNode.addLight(sun1);
		
		DirectionalLight sun2 =new DirectionalLight();
		sun1.setDirection(new Vector3f(-0.1f, 0.7f, 1.0f));
		rootNode.addLight(sun2);
		
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
		fpp.addFilter(bloom);
		context.getViewPort().addProcessor(fpp);

		
		//Controls
		inputManager.clearMappings();
		context.getShip().configureInputs(inputManager);
	}

	@Override
	public void simpleUpdate(float tpf) {
		rootNode.updateGeometricState();
		guiNode.updateGeometricState();
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}
}
