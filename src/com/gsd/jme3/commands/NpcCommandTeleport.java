package com.gsd.jme3.commands;

import com.gsd.jme3.character.Player;
import com.jme3.math.Vector3f;

public class NpcCommandTeleport extends NpcCommand {
	
	Vector3f position;
	
	public NpcCommandTeleport(String message, Vector3f position) {
		super(message);
		this.position = position;
	}

	@Override
	protected void executeInternalsActions(Player player) {
		player.setPhysicsLocation(position);
	}	
}
