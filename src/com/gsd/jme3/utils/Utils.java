package com.gsd.jme3.utils;

public class Utils {

	private Utils() {
		// Empty
	}
	
	public static final int QUAD_WIDTH = 10;
	public static final int QUAD_HEIGHT = 1;
	
	public static enum CreatureStatus {
		WALKING,
		FOLLOWING,
		ATTACKING
	}
	
	public static enum CharacterStatus{
		WALKING,
		WAITING,
		FOLLOWING,
		ATTACKING
	}
	
	public static enum NpcStatus {
		WAITING,
		TALKING
	}
	
	public static enum PhysicalCharacterType {
		PLAYER,
		CREATURE,
		NPC
	}
	
}