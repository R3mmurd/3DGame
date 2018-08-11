package com.gsd.jme3.commands;

import com.gsd.jme3.character.Player;

public class NpcCommandModifyLife extends NpcCommand {

	int life;
	
	// Parametro negativo si quiere que el npc haga dano
	public NpcCommandModifyLife(String message, int life) {
		super(message);
		this.life = life;
	}

	@Override
	protected void executeInternalsActions(Player player) {
		player.setCurrentLife(player.getCurrentLife() + life);
	}
}
