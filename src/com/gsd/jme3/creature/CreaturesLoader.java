package com.gsd.jme3.creature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class CreaturesLoader {

	private CreaturesLoader() {
		// Empty
	}

	public static List<Creature> load(Node node, PhysicsSpace physicsSpace,
			AssetManager assetManager, String fileName) throws IOException {
		List<Creature> list = new ArrayList<Creature>();

		BufferedReader rd = new BufferedReader(new FileReader(fileName));
		String line;
		String[] parsedLine;
		Pattern p = Pattern.compile("(;+)");

		while ((line = rd.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}

			parsedLine = p.split(line);
			
			if (parsedLine[0].trim().equals("//")) {
				continue;
			}

			String name = parsedLine[0];
			String model = parsedLine[1];

			String[] animWalk = {parsedLine[2]};
			String[] animIdle = {parsedLine[3]};
			String[] animAttack = {parsedLine[4]};

			Vector3f position = new Vector3f();
			position.setX(Float.parseFloat(parsedLine[5]));
			position.setY(100f);
			position.setZ(Float.parseFloat(parsedLine[6]));

			float widthBox = Float.parseFloat(parsedLine[7]);
			float heightBox = Float.parseFloat(parsedLine[8]);

			float scale = Float.parseFloat(parsedLine[9]);
			float radius = Float.parseFloat(parsedLine[10]);
			float height = Float.parseFloat(parsedLine[11]);

			float speed = Float.parseFloat(parsedLine[12]);

			int life = Integer.parseInt(parsedLine[13]);


			list.add(new Creature(node, physicsSpace, assetManager, //
					name,model, animWalk, animIdle, animAttack, //
					position, widthBox, heightBox, scale, //
					radius, height, speed, life));
		}

		return list;
	}

}
