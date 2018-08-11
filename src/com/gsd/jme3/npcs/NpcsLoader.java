package com.gsd.jme3.npcs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.gsd.jme3.commands.NpcCommand;
import com.gsd.jme3.commands.NpcCommandModifyLife;
import com.gsd.jme3.commands.NpcCommandTalk;
import com.gsd.jme3.commands.NpcCommandTeleport;
import com.gsd.jme3.commands.NpcCommandWorldChange;
import com.gsd.jme3.main.Main;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class NpcsLoader {
	
	private static Main main;

	private NpcsLoader() {
		// Empty
	}
	
	public static void setMain(Main main) {
		NpcsLoader.main = main;
	}

	public static List<Npc> load(Node node, PhysicsSpace physicsSpace,
			AssetManager assetManager, String fileName) throws IOException {
		List<Npc> list = new ArrayList<Npc>();

		BufferedReader rd = new BufferedReader(new FileReader(fileName));
		String line;
		String[] parsedLine;
		Pattern p = Pattern.compile("(;+)");

		Npc npc = null;

		while ((line = rd.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}

			parsedLine = p.split(line);

			String key = parsedLine[0].trim();

			if (key.equals("//")) {
				continue;
			} else if (key.equals("New")) {
				npc = createNewNpc(node, physicsSpace, assetManager, parsedLine);
				list.add(npc);
			} else if (key.equals("Talk")) {
				addActionTalk(npc, parsedLine);
			} else if (key.equals("Teleport")) {
				addActionTeleport(npc, parsedLine);
			} else if (key.equals("ModifyLife")) {
				addActionModifyLife(npc, parsedLine);
			} else if (key.equals("ChangeWorld")) {
				addActionChangeWorld(npc, parsedLine);
			}

		}

		return list;
	}

	private static Npc createNewNpc(Node node, PhysicsSpace physicsSpace,
			AssetManager assetManager, String[] parsedLine) {

		String name = parsedLine[1];
		String model = parsedLine[2];

		int count = Integer.parseInt(parsedLine[3]);

		String[] animWalk = new String[count];
		String[] animIdle = new String[count];
		String[] animAttack = new String[count];

		int i;
		for (i = 0; i < count; i++) {
			animWalk[i] = parsedLine[i + 4];
			animIdle[i] = parsedLine[i + 4 + count];
			animAttack[i] = parsedLine[i + 4 + count * 2];
		}
		i--;
		
		for (int k = 0; k < count; k++) {
			System.out.println("animWalk[" + k + "] = " + animWalk[k]);
			System.out.println("animIdle[" + k + "] = " + animIdle[k]);
			System.out.println("animattack[" + k + "] = " + animAttack[k]);
		}

		Vector3f initPosition = new Vector3f();
		initPosition.setX(Float.parseFloat(parsedLine[i + 5 + count * 2]));
		initPosition.setY(Float.parseFloat(parsedLine[i + 6 + count * 2]));
		initPosition.setZ(Float.parseFloat(parsedLine[i + 7 + count * 2]));

		float widthBox = Float.parseFloat(parsedLine[i + 8 + count * 2]);
		float heightBox = Float.parseFloat(parsedLine[i + 9 + count * 2]);
		
		float scale = Float.parseFloat(parsedLine[i + 10 + count * 2]);
		float radius = Float.parseFloat(parsedLine[i + 11 + count * 2]);
		float height = Float.parseFloat(parsedLine[i + 12 + count * 2]);

		float speed = Float.parseFloat(parsedLine[i + 13 + count * 2]);

		for (int j = 0; j < parsedLine.length; j++) {
			System.out.println("parsedLine[" + j + "] = " + parsedLine[j]);
		}
		
		String defaultMessage = parsedLine[i + 14 + count * 2];

		return new Npc(node, physicsSpace, assetManager, //
				name, model, animWalk, animIdle, animAttack, //
				initPosition, widthBox, heightBox, //
				scale, radius, height, //
				speed, defaultMessage);

	}
	
	private static void addActionTalk(Npc npc, String[] parsedLine) {
		String key = parsedLine[1];
		String res = "NPC " + npc.getName() + ": " + parsedLine[2];
		NpcCommand command = new NpcCommandTalk(res);
		npc.addCommand(key, command);
	}
	
	private static void addActionTeleport(Npc npc, String[] parsedLine) {
		String key = parsedLine[1];
		String res = "NPC " +npc.getName() + ": " +parsedLine[2];
		
		float x = Float.parseFloat(parsedLine[3]);
		float y = Float.parseFloat(parsedLine[4]);
		float z = Float.parseFloat(parsedLine[5]);
		
		Vector3f position = new Vector3f(x, y, z);
		
		NpcCommand command = new NpcCommandTeleport(res, position);
		npc.addCommand(key, command);
	}
	
	private static void addActionModifyLife(Npc npc, String[] parsedLine) {
		String key = parsedLine[1];
		String res = "NPC " +npc.getName() + ": " +parsedLine[2];
		int life = Integer.parseInt(parsedLine[3]);
		NpcCommand command = new NpcCommandModifyLife(res, life);
		npc.addCommand(key, command);
	}

	private static void addActionChangeWorld(Npc npc, String[] parsedLine) {
		String key = parsedLine[1];
		String res = "NPC " + npc.getName() + ": " + parsedLine[2];
		NpcCommand command = new NpcCommandWorldChange(res, main);
		npc.addCommand(key, command);
	}

}
