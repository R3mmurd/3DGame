package com.gsd.jme3.character;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.EventListenerList;

import com.gsd.jme3.utils.Utils;
import com.gsd.jme3.utils.Utils.CharacterStatus;
import com.gsd.jme3.utils.Utils.PhysicalCharacterType;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public abstract class PhysicalCharacter extends CharacterControl {

	private EventListenerList eventListenerList = new EventListenerList();

	public PhysicalCharacter characterTarget;

	protected String name;
	protected int life = 200;
	protected int currentLife = 200;
	private int attackDamage = 10;
	protected int variationAttackDamage = 5;
	protected Vector3f src = new Vector3f();
	protected Vector3f tgt = new Vector3f();
	protected float distanceToMove = 0;
	protected Vector3f currentDirection = new Vector3f();
	protected Vector3f currentViewDirection = new Vector3f();
	protected float speed;
	private EnergyBar energyBar;
	private boolean isSelected = false;

	// Atributos de control del framework

	protected Node characterNode;

	// Atributos del caracter en el terreno

	protected Vector3f initPosition;

	// Animaciones
	private String[] animWalk;
	private String[] animIdle;
	private String[] animAttack;

	private AnimControl animationControl;
	private AnimChannel[] animChannel;

	private float airTime;

	private int regenLifeRate = 5;

	private Geometry sphere;

	private final String model;
	private final float scale;
	private final float radius;
	private final float height;

	public PhysicalCharacter(Node node, PhysicsSpace physicsSpace, //
			AssetManager assetManager, String name, String model,//
			String[] animWalk, String[] animIdle, String[] animAttack, //
			Vector3f initPosition, float scale, float radius, //
			float height, float speed) {

		super(new CapsuleCollisionShape(radius*scale, height*scale), 0.01f);

		this.name = name;
		this.model = model;

		this.animWalk = animWalk;
		this.animIdle = animIdle;
		this.animAttack = animAttack;

		this.initPosition = initPosition;
		this.scale = scale;
		this.radius = radius;
		this.height = height;
		this.speed = speed;

		characterNode = (Node) assetManager.loadModel(model);
		characterNode.setShadowMode(ShadowMode.CastAndReceive);
		characterNode.setLocalScale(scale);
		characterNode.addControl(this);

		setPhysicsLocation(initPosition);
		node.attachChild(characterNode);
		physicsSpace.add(this);

		setupAnimationController();

		Material matWire = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		matWire.getAdditionalRenderState().setWireframe(true);
		matWire.setColor("Color", ColorRGBA.Green);

		float r = 1f;
		sphere = new Geometry("cannonball", new Sphere(5, 5, r));
		sphere.setMaterial(matWire);

		sphere.setLocalTranslation(this.getPhysicsLocation());
		node.attachChild(sphere);
	}

	public PhysicalCharacter getCharacterTarget() {
		return characterTarget;
	}

	public void setCharacterTarget(PhysicalCharacter characterTarget) {
		this.characterTarget = characterTarget;
	}

	public int getLife() {
		return life;
	}

	public int getCurrentLife() {
		return currentLife;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public void setCurrentLife(int currentLife) {
		if (currentLife > life) {
			currentLife = life;
		} else if (currentLife < 0) {
			currentLife = 0;
		}
		this.currentLife = currentLife;
	}

	public Vector3f getSrc() {
		return src;
	}

	public Vector3f getTgt() {
		return tgt;
	}

	public void setSrc(Vector3f src) {
		this.src = src;
	}

	public void setTgt(Vector3f tgt) {
		this.tgt = tgt;
	}

	public Node getCharacterNode() {
		return characterNode;
	}

	public String getName() {
		return name;
	}

	public void regenLife() {
		if (life >= 100) {
			return;
		}
		life = life + regenLifeRate; 
	}

	public void move(PhysicalCharacter player, long dTime, Vector3f cameraPosition) {
		float currentDistance = getPhysicsLocation().subtract(src).length();

		if (currentDistance >= distanceToMove) {
			computeNextPosition();
		}

		actionForPlayer(player, dTime);

		if (isSelected) {
			sphere.setLocalTranslation( //
					new Vector3f(this.getPhysicsLocation().x, //
							this.getPhysicsLocation().y + 8, //
							this.getPhysicsLocation().z));
		} else {
			sphere.setLocalTranslation(new Vector3f(-100,-1000, -100));
		}

	}

	public abstract void actionForPlayer(PhysicalCharacter  player, long dTime);

	public abstract void computeNextPosition();

	public boolean getFirstCollition(Ray ray) {
		boolean hit = false;
		CollisionResults results = new CollisionResults();
		int numCollisions = spatial.collideWith(ray, results);
		if (numCollisions > 0) {
			hit = true;
		}
		return hit;
	}


	public void attack(PhysicalCharacter character) {
		this.life -= character.getAttackDamage() //
				+ (int) ((Math.random()*1000) //
						% character.variationAttackDamage);
	}

	public void setDistanceToMove(float distance) {
		distanceToMove = distance;
	}

	protected int calculateDamage() {
		return getAttackDamage() + (int) ((Math.random()*1000) //
				% variationAttackDamage);
	}

	private void setupAnimationController() {

		animationControl = characterNode.getControl(AnimControl.class);
		animationControl.addListener(animListener);

		int t = Math.max(Math.max(//
				animWalk.length, animIdle.length), //
				animAttack.length);

		animChannel = new AnimChannel[t];

		for (int i = 0; i < t; i++) {
			if (animationControl != null && this != null) {
				animChannel[i] = animationControl.createChannel();
			}
		}

		setupAttackChannel();

	}

	public abstract void setupAttackChannel();

	AnimEventListener animListener = new AnimEventListener() {

		@Override
		public void onAnimCycleDone(AnimControl control, AnimChannel channel,
				String animName) {

			for (int i = 0; i < animAttack.length; i++) {
				if ((animName.equals(animAttack[i]) //
						|| animName.equals(animAttack[i]))) {
					animChannel[i].setAnim(animIdle[i], .1f);			
				}
			}

		}

		@Override
		public void onAnimChange(AnimControl control, AnimChannel channel,
				String animName) {
			// empty			
		}

	};

	public void animationUpdate(float tpf) {

		if (!onGround()) {
			airTime = airTime + tpf;
		} else {
			airTime = .0f;
		}

		if (walkDirection.length() > 0) {

			for (int i = 0; i < animWalk.length; i++) {
				if (!animWalk[i].equals(animChannel[i].getAnimationName())) {
					animChannel[i].setAnim(animWalk[i], .7f);
				}
			}

		} else if (getStatus() == Utils.CharacterStatus.ATTACKING) {

			for (int i = 0; i < animAttack.length; i++) {
				if (!animAttack[i].equals(animChannel[i].getAnimationName())) {
					animChannel[i].setAnim(animAttack[i]);
					animChannel[i].setLoopMode(LoopMode.Loop);
				}
			}

		} else {

			for (int i = 0; i < animIdle.length; i++) {
				if (!animIdle[i].equals(animChannel[i].getAnimationName())) {
					animChannel[i].setAnim(animIdle[i], .1f);
				}
			}

		}

	}

	public abstract CharacterStatus getStatus();

	public AnimControl getAnimationControl() {
		return animationControl;
	}

	public void setAnimationControl(AnimControl animationControl) {
		this.animationControl = animationControl;
	}

	public AnimChannel[] getAnimChannel() {
		return animChannel;
	}

	public void setAnimChannel(AnimChannel[] animTopChannel) {
		this.animChannel = animTopChannel;
	}

	public String[] getAnimWalkTop() {
		return animWalk;
	}

	public void setAnimWalkTop(String[] animWalkTop) {
		this.animWalk = animWalkTop;
	}

	public String[] getAnimIdleTop() {
		return animIdle;
	}

	public void setAnimIdleTop(String[] animIdleTop) {
		this.animIdle = animIdleTop;
	}

	public String[] getAnimAttack() {
		return animAttack;
	}

	public void setAnimAttack(String[] animAttack) {
		this.animAttack = animAttack;
	}

	public abstract PhysicalCharacterType getType();

	public void setAttackDamage(int attackDamage) {
		this.attackDamage = attackDamage;
	}

	public int getAttackDamage() {
		return attackDamage;
	}

	public EnergyBar getEnergyBar() {
		return energyBar;
	}

	public void setEnergyBar(EnergyBar energyBar) {
		this.energyBar = energyBar;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getModel() {
		return model;
	}

	public float getScale() {
		return scale;
	}

	public float getRadius() {
		return radius;
	}

	public float getHeight() {
		return height;
	}

	public void addActionListener(ActionListener listener) {
		eventListenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		eventListenerList.remove(ActionListener.class, listener);
	}

	public ActionListener[] getActionListeners() {
		return eventListenerList.getListeners(ActionListener.class);
	}

	protected void fireActionEvent(ActionEvent evt) {
		ActionListener[] actionListeners = getActionListeners();

		for (ActionListener actionListener : actionListeners) {
			actionListener.actionPerformed(evt);
		}
	}

}