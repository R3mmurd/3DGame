package com.gsd.jme3.commands;

import com.gsd.jme3.character.Player;
import com.gsd.jme3.screens.ScreenControler;

public abstract class NpcCommand {

	String message;

	// TODO: Pasarle el parametro de la pantalla a la que va a escribir el mensaje
	protected NpcCommand(String message) {
		this.message = message;
	}

	public void execute(Player player) {
		ScreenControler.instance.sendMessage(message);
		executeInternalsActions(player);
	}
	
	protected abstract void executeInternalsActions(Player player);

}
