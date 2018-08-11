/*
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsd.jme3.creature;

import com.gsd.jme3.character.PhysicalCharacter;
import com.gsd.jme3.screens.ScreenControler;
import com.gsd.jme3.utils.Utils;
import com.gsd.jme3.utils.Utils.CharacterStatus;
import com.gsd.jme3.utils.Utils.CreatureStatus;
import com.gsd.jme3.utils.Utils.PhysicalCharacterType;
import com.jme3.animation.AnimChannel;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Creature extends PhysicalCharacter {

	// Atributos de la criatura

	private float lookRatio = 150;
	private float lookAngle = 60;

	private float listenRatio = 50;

	private float minDistanceWithPlayer = 10;
	private long attackTime = 2000;// segundos
	private long contAttack = 0;

	// Caja delimitante del movimiento
	private float minX;
	private float minZ;

	private float maxX;
	private float maxZ;

	private Utils.CreatureStatus status = CreatureStatus.WALKING;

	boolean isOto = false;

	public Creature(Node node, PhysicsSpace physicsSpace, //
			AssetManager assetManager, String name, String model, //
			String[] animWalk, String[] animIdle, String[] animAttack, //
			Vector3f initPosition, float widthBox, float heightBox, //
			float scale, float radius, float height, float speed, int life) {

		super(node, physicsSpace, assetManager, name, model, //
				animWalk, animIdle, animAttack, initPosition, //
				scale, radius, height, speed);

		minX = initPosition.getX() - widthBox / 2;
		minZ = initPosition.getZ() - heightBox / 2;

		maxX = initPosition.getX() + widthBox / 2;
		maxZ = initPosition.getZ() + heightBox / 2;

		src.set(getPhysicsLocation());
		tgt.setX((float) Math.random() * (maxX - minX) + minX);
		tgt.setZ((float) Math.random() * (maxZ - minZ) + minZ);
		Vector3f v = tgt.subtract(src);
		v.setY(0);
		distanceToMove = v.length();
		currentDirection.set(v.normalize().divide(speed));
		this.life = life;
		currentLife = life;

		if (model.toLowerCase().contains("oto")) {
			isOto = true;
		}

	}

	@Override
	public void computeNextPosition() {
		if (status == CreatureStatus.WALKING) {

			src.set(getPhysicsLocation());
			tgt.setX((float) Math.random() * (maxX - minX) + minX);
			tgt.setZ((float) Math.random() * (maxZ - minZ) + minZ);
			Vector3f v = tgt.subtract(src);
			v.setY(0);
			distanceToMove = v.length();
			currentDirection.set(v.normalize().divide(speed));
			viewDirection.setY(0);
			viewDirection.set(viewDirection);
		}
	}

	@Override
	public void actionForPlayer(PhysicalCharacter player, long dTime) {

		Vector3f vectorBetweenPlayerAndMe = player.getPhysicsLocation()
				.subtract(this.getPhysicsLocation());
		vectorBetweenPlayerAndMe.setY(0);

		if (contAttack < attackTime) {
			contAttack += dTime;
		}

		currentDirection.setY(0);
		viewDirection.setY(0);
		walkDirection.addLocal(currentDirection);
		setWalkDirection(currentDirection);
		setViewDirection(viewDirection);

		if (currentLife > 0) {
			switch (status) {
			case WALKING:

				float u1 = vectorBetweenPlayerAndMe.getX();
				float u2 = vectorBetweenPlayerAndMe.getZ();

				float v1 = getViewDirection().getX();
				float v2 = getViewDirection().getZ();

				double angleBetweenPlayerAndMe = Math.acos((u1 * v1 + u2 * v2)
						/ (Math.sqrt(u1 * u1 + u2 * u2) * Math.sqrt(v1 * v1
								+ v2 * v2)))
						* 180 / Math.PI;

				if (vectorBetweenPlayerAndMe.length() <= listenRatio
						|| (angleBetweenPlayerAndMe < lookAngle && vectorBetweenPlayerAndMe
								.length() <= lookRatio)) {
					status = CreatureStatus.FOLLOWING;
				}

				animationUpdate((float) dTime);
				break;

			case FOLLOWING: {
				src.set(getPhysicsLocation());
				tgt.set(player.getPhysicsLocation());
				Vector3f v = tgt.subtract(src);
				v.setY(0);
				distanceToMove = v.length();
				currentDirection.set(v.normalize().divide(speed));
				viewDirection.set(v);
				viewDirection.setY(0);
				setViewDirection(viewDirection);

				if (vectorBetweenPlayerAndMe.length() <= minDistanceWithPlayer) {
					status = CreatureStatus.ATTACKING;
					currentDirection.set(0, 0, 0);
				} else if (vectorBetweenPlayerAndMe.length() >= lookRatio) {
					status = CreatureStatus.WALKING;
					computeNextPosition();
				}

				animationUpdate((float) dTime);
			}
				break;

			case ATTACKING: {
				src.set(getPhysicsLocation());
				tgt.set(player.getPhysicsLocation());
				Vector3f v = tgt.subtract(src);
				v.setY(0);
				distanceToMove = v.length();
				currentDirection.set(v.normalize().divide(speed));
				viewDirection.set(v);
				viewDirection.setY(0);
				setViewDirection(viewDirection);
				if (contAttack > attackTime) {

					int danio = calculateDamage();

					player.setCurrentLife(player.getCurrentLife() - danio);
					ScreenControler.instance.sendMessage( //
							name + ": te ha hecho " + danio + " puntos de dano");

					contAttack = 0;

					animationUpdate((float) dTime);

				}
				if (player.getCurrentLife() <= 0) {
					status = CreatureStatus.WALKING;
					computeNextPosition();
				}
				if (vectorBetweenPlayerAndMe.length() > minDistanceWithPlayer) {
					status = CreatureStatus.FOLLOWING;
				}
			}
				break;
			}

		} else {
			this.setPhysicsLocation(new Vector3f(10, -1000, 10));
			setSelected(false);
			currentLife = life;
		}

	}

	@Override
	public void setupAttackChannel() {

		if (!isOto) {
			return;
		}
		AnimChannel[] animChannel = new AnimChannel[getAnimChannel().length + 1];

		for (int i = 0; i < animChannel.length - 1; i++) {
			animChannel[i] = getAnimChannel()[i];
		}

		animChannel[animChannel.length - 1] = //
		getAnimationControl().createChannel();
		animChannel[animChannel.length - 1].addBone( //
				getAnimationControl().getSkeleton() //
						.getBone("uparm.right"));
		animChannel[animChannel.length - 1].addBone( //
				getAnimationControl().getSkeleton() //
						.getBone("arm.right"));
		animChannel[animChannel.length - 1].addBone( //
				getAnimationControl().getSkeleton() //
						.getBone("hand.right"));

		setAnimChannel(animChannel);

	}

	@Override
	public CharacterStatus getStatus() {

		switch (status) {
		case WALKING:
			return Utils.CharacterStatus.WALKING;
		case FOLLOWING:
			return Utils.CharacterStatus.FOLLOWING;
		case ATTACKING:
			return Utils.CharacterStatus.ATTACKING;
		default:
			return Utils.CharacterStatus.WALKING;
		}

	}

	@Override
	public PhysicalCharacterType getType() {
		return Utils.PhysicalCharacterType.CREATURE;

	}
}