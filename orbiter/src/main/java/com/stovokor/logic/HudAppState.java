package com.stovokor.logic;

import com.jme3.app.state.AbstractAppState;
import com.stovokor.GameContext;
import com.stovokor.GameHud;

public class HudAppState extends AbstractAppState {

	private GameContext context;
	
	public HudAppState(GameContext context) {
		this.context = context;
	}
	
	@Override
	public void update(float tpf) {
//		context.getScore().setScore( (int)(context.getParam().getValue() * 1000f));
		
		GameHud hud = context.getHud();
		hud.setScore(context.getScore());
	}
	
}
