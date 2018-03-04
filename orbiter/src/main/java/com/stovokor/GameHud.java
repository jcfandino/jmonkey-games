package com.stovokor;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.stovokor.logic.GameScore;

public class GameHud {

	private Node guiNode;
	private BitmapText scoreText;
	private BitmapText diffText;
	
	public GameHud(AssetManager assetManager) {
		guiNode = new Node();
		BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		
		scoreText = new BitmapText(guiFont, false);
		scoreText.setSize(guiFont.getCharSet().getRenderedSize());
		scoreText.setLocalTranslation(300, scoreText.getLineHeight(), 0);
		
		diffText = new BitmapText(guiFont, false);
		diffText.setSize(guiFont.getCharSet().getRenderedSize());
		diffText.setLocalTranslation(300, 2.5f * diffText.getLineHeight(), 0);
		
		guiNode.attachChild(scoreText);
		guiNode.attachChild(diffText);
	}
	
	public Node getGuiNode() {
		return guiNode;
	}
	
	public void setScore(GameScore score) {
		scoreText.setText("Score:" + String.valueOf(score.getScore()));
		diffText.setText("Level:" + String.valueOf(score.getDiff()));
	}
	
}
