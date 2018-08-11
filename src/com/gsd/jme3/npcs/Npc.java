package com.gsd.jme3.npcs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.gsd.jme3.character.Player;
import com.gsd.jme3.character.PhysicalCharacter;
import com.gsd.jme3.commands.NpcCommand;
import com.gsd.jme3.screens.ScreenControler;
import com.gsd.jme3.utils.Utils;
import com.gsd.jme3.utils.Utils.CharacterStatus;
import com.gsd.jme3.utils.Utils.NpcStatus;
import com.gsd.jme3.utils.Utils.PhysicalCharacterType;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Npc extends PhysicalCharacter {

	private HashMap<String, NpcCommand> actions = new HashMap<String, NpcCommand>();

	Utils.NpcStatus status = Utils.NpcStatus.WAITING;

	// Caja delimitante del movimiento
	private float minX;
	private float minZ;

	private float maxX;
	private float maxZ;

	private long adsTime = 10000;
	private long contAdsTime = 0;

	private final String defaultMessage;

	public Npc(Node node, PhysicsSpace physicsSpace, AssetManager assetManager, //
			String name, String model, String[] animWalk, String[] animIdle, String[] animAttack, //
			Vector3f initPosition, float widthBox, float heightBox, //
			float scale, float radius, float height, //
			float speed, String defaultMessage) {

		super(node, physicsSpace, assetManager, //
				name, model, animWalk, animIdle, animAttack, //
				initPosition, scale, radius, height, speed);
		this.defaultMessage = defaultMessage;

		src.set(getPhysicsLocation());
		minX = initPosition.getX() - widthBox / 2;
		minZ = initPosition.getZ() - heightBox / 2;

		maxX = initPosition.getX() + widthBox / 2;
		maxZ = initPosition.getZ() + heightBox / 2;

		computeNextPosition();
	}

	@Override
	public void computeNextPosition() {
		if (status == Utils.NpcStatus.WAITING) {
			src.set(getPhysicsLocation());

			tgt.setX((float) Math.random() * (maxX - minX) + minX);
			tgt.setZ((float) Math.random() * (maxZ - minZ) + minZ);

			Vector3f v = tgt.subtract(src);
			v.setY(0);
			distanceToMove = v.length();
			currentDirection.set(v.normalize().divide(speed));
			currentDirection.setY(0);
			viewDirection.set(currentDirection);
			setWalkDirection(currentDirection);
			viewDirection.set(viewDirection);
		}
	}

	public void listen(String sentence, PhysicalCharacter player) {
		switch (status) {
		case WAITING:
			if (sentence.equals("HOLA")) {
				characterTarget = player;
				ScreenControler.instance.sendMessage("NPC " +name + ": Hola amigo");

				status = NpcStatus.TALKING;

				src.set(getPhysicsLocation());
				tgt.set(src);

				Vector3f v = tgt.subtract(src);
				v.setY(0);
				distanceToMove = v.length();
				currentDirection.set(v.normalize().divide(speed));
				currentDirection.setY(0);

				setWalkDirection(currentDirection);
			}
			break;
		case TALKING:
			if (sentence.equals("ADIOS")) {
				characterTarget = null;
				status = NpcStatus.WAITING;
				ScreenControler.instance.sendMessage("NPC " +name + ": " +"Adios amigo");
				computeNextPosition();
			} else {
				NpcCommand command = actions.get(sentence);
				if (command != null) {
					command.execute((Player) player);
				} else {
					ScreenControler.instance.sendMessage("NPC " +name + ": " + "No puedo entender lo que me dices");
				}
			}
			break;
		}
	}

	@Override
	public void actionForPlayer(PhysicalCharacter player, long dTime) {

		Vector3f v = player.getPhysicsLocation().subtract(getPhysicsLocation());

		contAdsTime += dTime;

		switch (status) {
		case WAITING:
			if (contAdsTime >= adsTime && v.length() <= 30) {
				ScreenControler.instance.sendMessage("NPC " +name + ": " + defaultMessage);
				contAdsTime = 0;
			}
			animationUpdate((float) dTime);
			break;
		case TALKING:

			viewDirection.set(characterTarget.getPhysicsLocation().subtract(getPhysicsLocation()).normalize());
			viewDirection.setY(0);
			viewDirection.set(viewDirection);
			setViewDirection(viewDirection);

			if (v.length() >= 30) {
				characterTarget = null;
				ScreenControler.instance.sendMessage("NPC " +name + ": " +"Nos vemos en otro momento");
				status = NpcStatus.WAITING;
				computeNextPosition();
			}
			animationUpdate((float) dTime);
			break;
		}

	}

	@Override
	public void setupAttackChannel() {
		/* empty */
	}

	@Override
	public CharacterStatus getStatus() {

		switch (status) {
		case TALKING:
			return Utils.CharacterStatus.WAITING;
		case WAITING:
			return Utils.CharacterStatus.WALKING;
		default:
			return Utils.CharacterStatus.WALKING;
		}

	}

	public void addCommand(String key, NpcCommand command) {
		actions.put(key, command);
	}

	@Override
	public PhysicalCharacterType getType() {
		return Utils.PhysicalCharacterType.NPC;
	}

	public String getSentencesList() {
		String ret = new String();

		Iterator<Entry<String, NpcCommand>> it = actions.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, NpcCommand> entry = it.next();
			ret += entry.getKey() + "\n";
		}


		return ret;
	}

}
