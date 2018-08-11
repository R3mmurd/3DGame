package com.gsd.jme3.commands;

import com.gsd.jme3.character.Player;
import com.gsd.jme3.main.Main;

public class NpcCommandWorldChange extends NpcCommand {

	private Main main;

	public NpcCommandWorldChange(String message, Main main) {
		super(message);
		this.main = main;
	}

	@Override
	protected void executeInternalsActions(Player player) {
		main.changeWorld();
	}

}
