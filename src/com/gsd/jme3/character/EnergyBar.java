package com.gsd.jme3.character;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;

public class EnergyBar {
	
	private float height;
	private Picture background;
	private Picture front;
	private Node guiNode;
	
	
	public EnergyBar(float height, AssetManager assetManager, Node guiNode) {
		this.height = height;
		this.guiNode = guiNode;
		
		background = new Picture("Back");
		background.setImage(assetManager, "com/gsd/jme3/images/barra.png", true);
		
		front = new Picture("Front");
		front.setImage(assetManager, "com/gsd/jme3/images/barra2.png", true);
		
		guiNode.attachChild(background);
		guiNode.attachChild(front);
		
	}
	
	public void paint(float actualLife, float totalLife, float width, Vector2f pos){
		
		
		background.setWidth((totalLife * width) / totalLife);
		background.setHeight(height);
		background.setPosition(pos.x, pos.y);

		
		front.setWidth((actualLife * width) / totalLife);
		front.setHeight(height-3);
		front.setPosition(pos.x + 1, pos.y+1);
		
	}


	public Picture getFront() {
		return front;
	}


	public void setFront(Picture front) {
		this.front = front;
	}


	public Picture getBackground() {
		return background;
	}


	public void setBackground(Picture background) {
		this.background = background;
	}


	public float getHeight() {
		return height;
	}


}
