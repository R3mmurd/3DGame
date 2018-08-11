package com.gsd.jme3.commands;

import com.gsd.jme3.character.Player;

public class NpcCommandTalk extends NpcCommand {
	
	public NpcCommandTalk(String message) {
		super(message);
	}

	@Override
	protected void executeInternalsActions(Player player) {
		// empty
	}
}
