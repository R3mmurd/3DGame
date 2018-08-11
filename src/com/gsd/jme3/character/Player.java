package com.gsd.jme3.character;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.gsd.jme3.npcs.Npc;
import com.gsd.jme3.utils.Utils;
import com.gsd.jme3.utils.Utils.CharacterStatus;
import com.gsd.jme3.utils.Utils.PhysicalCharacterType;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Player extends PhysicalCharacter implements ActionListener {

	private float tpf = .0f;
	private float airTime = .0f;
	private Utils.CharacterStatus status = CharacterStatus.WAITING;

	/*
	 * private Creature creatureTgt; private Npc npcTgt;
	 */

	private long attackTime = 2000;// segundos
	private long contAttack = 0;
    private int	attackDamage = 20;

	private float minDistanceWithPlayer = 10;

	Vector3f clickDirection = new Vector3f();

	public Player(Node node, PhysicsSpace physicsSpace, //
			AssetManager assetManager, String name, String model, //
			String[] animWalk, String[] animIdle, String[] animAttack, //
			Vector3f initPosition, float scale, float radius, //
			float height, float speed) {

		super(node, physicsSpace, assetManager, //
				name,model, animWalk, animIdle, animAttack, //
				initPosition, scale, radius, height, speed);
		super.setAttackDamage(attackDamage);

		setJumpSpeed(12.5f);
		setFallSpeed(30.0f);
		setGravity(30.0f);
	}

	@Override
	public void computeNextPosition() {

		status = CharacterStatus.WAITING;
		airTime = onGround() ? .0f : (airTime + tpf);

	}

	public Vector3f getCurrentDirection() {
		return currentDirection;
	}

	public void setCurrentDirection(Vector3f currentDirection) {
		this.currentDirection = currentDirection;
	}

	@Override
	public void actionForPlayer(PhysicalCharacter player, long dTime) {

		if (contAttack < attackTime) {
			contAttack += dTime;
		}
		if (contAttack > attackTime){
			super.regenLife();
		}
		walkDirection.set(0, 0, 0);

		if (currentLife > 0) {
			switch (status) {
			case WALKING:

				walkDirection.addLocal(clickDirection);
				animationUpdate((float) dTime);
				break;
			case FOLLOWING:
				src = getPhysicsLocation();
				distanceToMove = characterTarget.getPhysicsLocation()
						.subtract(src).length();
				walkDirection = characterTarget.getPhysicsLocation()
						.subtract(src).normalize();
				if (distanceToMove < minDistanceWithPlayer) {
					if (characterTarget.getType() == Utils.PhysicalCharacterType.CREATURE) {
						status = CharacterStatus.ATTACKING;
					} else if (characterTarget.getType() == Utils.PhysicalCharacterType.NPC) {
						status = CharacterStatus.WAITING;
					}
				}
				setViewDirection(walkDirection);
				setWalkDirection(walkDirection);
				animationUpdate((float) dTime);
				break;
			case ATTACKING:
				animationUpdate((float) dTime);
				if (contAttack > attackTime) {
					// atacar
					int danio = calculateDamage();
					System.err.println("Caracter: te he hecho " + danio
							+ " puntos de danio");
					contAttack = 0;
					characterTarget.setCurrentLife(characterTarget
							.getCurrentLife() - danio);
				}
				if (characterTarget.getCurrentLife() <= 0) {
					status = CharacterStatus.WAITING;
				}
				if (distanceToMove > minDistanceWithPlayer) {
					status = CharacterStatus.FOLLOWING;
				}
				break;
			default:
				animationUpdate((float) dTime);
			}

			setWalkDirection(walkDirection);
			setViewDirection(clickDirection);

		} else {
			fireActionEvent(new ActionEvent(this, 0, "Has muerto"));
			status = CharacterStatus.WAITING;
			setPhysicsLocation(initPosition);
			setSelected(false);
			characterTarget = null;
			setCurrentLife(life);
			animationUpdate((float) dTime);
		}
	}

	public void setCharacterTarget(PhysicalCharacter character) {
		characterTarget = character;
	}

	public Spatial getMyNode() {
		return this.characterNode;
	}

	public void setStatus(Utils.CharacterStatus sta) {
		status = sta;
	}

	public CharacterStatus getStatus() {
		return status;
	}

	public void setClickDirection(Vector3f v) {
		clickDirection = v;
	}

	@Override
	public void setupAttackChannel() {
		// empty
	}

	@Override
	public PhysicalCharacterType getType() {
		return Utils.PhysicalCharacterType.PLAYER;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (characterTarget == null) {
			return;
		}
		if (characterTarget.getType() != Utils.PhysicalCharacterType.NPC) {
			return;
		}

		Vector3f me = getPhysicsLocation();
		Vector3f it = characterTarget.getPhysicsLocation();

		float distance = it.subtract(me).length();

		if (distance > 30) {
			return;
		}

		Npc npc = (Npc) characterTarget;
		npc.listen(e.getActionCommand().toUpperCase(), this);
	}

}
