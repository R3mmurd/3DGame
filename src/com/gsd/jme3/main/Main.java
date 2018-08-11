package com.gsd.jme3.main;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import com.gsd.jme3.character.Player;
import com.gsd.jme3.character.EnergyBar;
import com.gsd.jme3.creature.Creature;
import com.gsd.jme3.creature.CreaturesLoader;
import com.gsd.jme3.npcs.Npc;
import com.gsd.jme3.npcs.NpcsLoader;
import com.gsd.jme3.screens.ScreenControler;
import com.gsd.jme3.utils.Utils;
import com.gsd.jme3.utils.Utils.CharacterStatus;
import com.gsd.jme3.utils.WorldCreator;
import com.gsd.jme3.utils.WorldData;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.ui.Picture;

import de.lessvoid.nifty.Nifty;

public class Main extends SimpleApplication implements
/*	*/ActionListener {

	static final String FOLLOWER_CAMERA = "Follower Camera";
	static final String FLYER_CAMERA = "Flyer Camera";
	static final String FIRST_PERSON_CAMERA = "First Person Camera";

	enum CameraType {
		FOLLOWER, FLYER, FIRST_PERSON
	}

	private Npc npcTgt;
	private Picture picCreature;
	private Picture picNpc;
	private EnergyBar energyBar;
	private BitmapText hudText;
	private BitmapText hudTextCreature;
	private BitmapText hudTextCreatureLife;
	private BitmapText hudTextNpc;
	private EnergyBar energyCreatureBar;
	private EnergyBar energyBarCharacter;

	private WorldCreator worldCreator;
	private WorldData worldData;
	private boolean terrainIsSnow = false;
	private double deadTime;
	private boolean gsd = true;
	private Spatial skyBox;
	private TerrainQuad terrain;
	protected BitmapText hintText;
	private Geometry collisionMarker;
	private BulletAppState bulletAppState;

	private Player player;
	private Vector3f initPosCharacter = new Vector3f();
	boolean leftStrafe = false, rightStrafe = false, forward = false,
			backward = false, leftRotate = false, rightRotate = false;

	private ChaseCamera chaseCamera;
	private Nifty nifty;
	private ScreenControler controller;
	private NiftyJmeDisplay niftyDisplay;
	public AudioNode audioSource;
	private float frameCount = 0;
	private Vector3f clickDirection = new Vector3f(0, 0, 0);

	private List<Creature> creatureList;
	private List<Npc> npcList;
	Creature creatureTgt;

	private Vector3f src = new Vector3f(0, 0, 0);
	private Vector3f tgt = new Vector3f(0, 0, 0);

	private float distance = 0;
	private CameraType cameraType = CameraType.FOLLOWER;

	private boolean started = false;

	private long lastTime = 0;

	public static void main(String[] args) {
		Main app = new Main();

		AppSettings settings = new AppSettings(true);
		settings.setTitle("Misterios de Munrrael");
		settings.setResolution(1024, 768);
		settings.setBitsPerPixel(24);
		settings.setFrameRate(60);

		app.setSettings(settings);
		app.setShowSettings(false);
		app.start();
	}

	private void setupKeys() {
		inputManager.addMapping("Strafe Left", new KeyTrigger(KeyInput.KEY_Q),
				new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("Strafe Right", new KeyTrigger(KeyInput.KEY_E),
				new KeyTrigger(KeyInput.KEY_X));
		inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_A),
				new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_D),
				new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addMapping("Walk Forward", new KeyTrigger(KeyInput.KEY_W),
				new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Walk Backward",
				new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(
						KeyInput.KEY_DOWN));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Send", new KeyTrigger(KeyInput.KEY_RETURN));
		inputManager.addMapping(FOLLOWER_CAMERA,
				new KeyTrigger(KeyInput.KEY_F1));
		inputManager.addMapping(FLYER_CAMERA, new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addMapping(FIRST_PERSON_CAMERA, new KeyTrigger(
				KeyInput.KEY_F3));

		inputManager.addMapping("walk", new MouseButtonTrigger(
				MouseInput.BUTTON_LEFT));
		inputManager.addListener(actionListener, "shoot");
		inputManager.addMapping("shoot", new MouseButtonTrigger(
				MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, "Strafe Left", "Strafe Right");
		inputManager.addListener(this, "Rotate Left", "Rotate Right");
		inputManager.addListener(this, "Walk Forward", "Walk Backward");
		inputManager.addListener(this, "Jump", "Shoot");
		// inputManager.addMapping("shoot", new
		// MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(actionListener, "shoot");
		inputManager.addListener(actionListener, "walk");
		inputManager.addListener(actionListener, "flyingCamera");
		inputManager.addListener(actionListener, FOLLOWER_CAMERA);
		inputManager.addListener(actionListener, FLYER_CAMERA);
		inputManager.addListener(actionListener, FIRST_PERSON_CAMERA);
		inputManager.addListener(actionListener, "send");

	}

	private ActionListener actionListener = new ActionListener() {

		public void onAction(String name, boolean isPressed, float tpf) {
			if (!started) {
				return;
			}

			if (name.equals(FOLLOWER_CAMERA) && !isPressed) {
				flyCam.setEnabled(false);
				chaseCamera.setEnabled(true);
				chaseCamera.setMinDistance(10);
				chaseCamera.setMaxDistance(150);
			} else if (name.equals(FLYER_CAMERA) && !isPressed) {
				flyCam.setEnabled(true);
				chaseCamera.setEnabled(false);
			} else if (name.equals(FIRST_PERSON_CAMERA) && !isPressed) {
				cameraType = CameraType.FIRST_PERSON;
				flyCam.setEnabled(false);
				chaseCamera.setEnabled(true);
				chaseCamera.setMinDistance(0);
				chaseCamera.setMaxDistance(0);
				chaseCamera.update(0);
			} else if (name.equals("walk") && !isPressed
					&& cameraType != CameraType.FIRST_PERSON) {
				Vector3f origin = cam.getWorldCoordinates(
						inputManager.getCursorPosition(), 0.0f);
				Vector3f direction = cam.getWorldCoordinates(
						inputManager.getCursorPosition(), 0.3f);
				direction.subtractLocal(origin).normalizeLocal();

				Ray ray = new Ray(origin, direction);

				CollisionResults results = new CollisionResults();
				int numCollisions = terrain.collideWith(ray, results);
				if (numCollisions == 0) {
					return;
				}

				CollisionResult hit = results.getClosestCollision();
				if (collisionMarker == null) {
					createCollisionMarker();
				}
				Vector2f loc = new Vector2f(hit.getContactPoint().x,
						hit.getContactPoint().z);
				float height = terrain.getHeight(loc);
				collisionMarker.setLocalTranslation(new Vector3f(hit
						.getContactPoint().x, height, hit.getContactPoint().z));
				src.set(player.getPhysicsLocation());
				src.setY(0);
				tgt.set(hit.getContactPoint().x, 0, hit.getContactPoint().z);
				Vector3f v = tgt.subtract(src);
				distance = v.length();
				clickDirection = v.normalize();
				clickDirection.setY(0);
				player.setStatus(CharacterStatus.WALKING);
				player.setDistanceToMove(distance);
				player.setClickDirection(clickDirection);
				player.setSrc(src);

				player.setCharacterTarget(null);
				creatureTgt.setSelected(false);
				npcTgt.setSelected(false);

				for (Creature c : creatureList) {

					if (c.getFirstCollition(ray)) {
						creatureTgt = c;
						src.set(player.getPhysicsLocation());
						src.setY(0);
						player.setStatus(CharacterStatus.FOLLOWING);
						player.setCharacterTarget(c);
						player.setSrc(src);
						creatureTgt.setSelected(true);
						break;
					}

					for (Npc npc : npcList) {

						if (npc.getFirstCollition(ray)) {
							src.set(player.getPhysicsLocation());
							src.setY(0);
							player.setStatus(CharacterStatus.FOLLOWING);
							player.setCharacterTarget(npc);
							player.setSrc(src);
							npc.setCharacterTarget(player);
							npcTgt = npc;
							npcTgt.setSelected(true);

							break;
						}

					}
					if (!npcTgt.isSelected()) {
						picNpc.setPosition(-1000, -1000);
						hudTextNpc.setLocalTranslation(-1000, -1000, 0);
					} else {
						hudTextNpc.setText("Comandos de " + npcTgt.getName()
								+ "\n\n" + npcTgt.getSentencesList());
						hudTextNpc.setLocalTranslation(
								settings.getWidth() - 200,
								(settings.getHeight() / 2) - 5, 0);
						picNpc.setPosition(settings.getWidth() - 210,
								(settings.getHeight() / 2) - 200);
					}

				}

			}
		}

	};

	protected boolean dead;
	private Picture pic;
	private BitmapText hudText1;
	private int gsdCounter = 300;

	@Override
	public void simpleInitApp() {

		setDisplayFps(false);
		setDisplayStatView(false);

		assetManager.registerLocator("assets", //
				FileLocator.class.getName());

		audioSource = new AudioNode(assetManager,
				"com/gsd/jme3/sounds/preview4.ogg", true);
		// audioSource.setLooping(true);
		audioSource.play();

		flyCam.setEnabled(false);

		niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager,
				audioRenderer, guiViewPort);

		nifty = niftyDisplay.getNifty();

		controller = new ScreenControler(this, nifty);

		nifty.fromXml("screens.xml", "gsd", controller);

		guiViewPort.addProcessor(niftyDisplay);
		inputManager.setCursorVisible(true);

		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);

		energyBar = new EnergyBar(10, assetManager, guiNode);

	}

	public void setAudio() {
		audioSource = new AudioNode(assetManager,
				"com/gsd/jme3/sounds/preview4.ogg", true);
	}

	public void setAudioDead() {
		audioSource = new AudioNode(assetManager,
				"com/gsd/jme3/sounds/Muerte.ogg", true);
	}

	public void changeWorld() {

		removeWorld();
		terrainIsSnow = !terrainIsSnow;

		if (terrainIsSnow) {

			worldData.setDirt("com/gsd/jme3/textures/Snow.jpg");
			worldData.setGrass("com/gsd/jme3/textures/Snow2.jpg");
			worldData.setRock("com/gsd/jme3/textures/road2.jpg");
			worldData.setSkyBox("/com/gsd/jme3/models/skyboxSnow.j3o");
			initPosCharacter = new Vector3f(400, 10, 400);

		} else {

			worldData.setDefaultData();
			initPosCharacter = new Vector3f(150, 12, 10);

		}

		worldCreator = new WorldCreator( //
				this, rootNode, bulletAppState, worldData);
		setTerrainAndSkyBox();

		player.setPhysicsLocation(initPosCharacter);

	}

	private void setTerrainAndSkyBox() {
		this.terrain = worldCreator.getTerrain();
		this.skyBox = worldCreator.getSkyBox();
	}

	private void removeWorld() {
		bulletAppState.getPhysicsSpace().remove(terrain);
		rootNode.detachChild(terrain);
		rootNode.detachChild(skyBox);
	}

	private void addObjects() {

		// Tree1

		Vector3f treePosition = new Vector3f(-120, 10, 190);

		Spatial tree = assetManager.loadModel("Models/Tree/Tree.mesh.xml");
		tree.setShadowMode(ShadowMode.CastAndReceive);
		tree.setLocalTranslation(treePosition);
		tree.setLocalScale(4f);

		CollisionShape treeShape = CollisionShapeFactory
				.createDynamicMeshShape(tree);
		RigidBodyControl treePhy = new RigidBodyControl(treeShape, 500f);
		treePhy.setKinematic(true);
		tree.addControl(treePhy);

		rootNode.attachChild(tree);
		bulletAppState.getPhysicsSpace().add(treePhy);

		// Tree2

		Vector3f treePosition2 = new Vector3f(-300, 1, 190);

		Spatial tree2 = assetManager.loadModel("Models/Tree/Tree.mesh.xml");
		tree2.setShadowMode(ShadowMode.CastAndReceive);
		tree2.setLocalTranslation(treePosition2);
		tree2.setLocalScale(4f);

		CollisionShape treeShape2 = CollisionShapeFactory
				.createDynamicMeshShape(tree2);
		RigidBodyControl treePhy2 = new RigidBodyControl(treeShape2, 500f);
		treePhy2.setKinematic(true);
		tree2.addControl(treePhy2);

		rootNode.attachChild(tree2);
		bulletAppState.getPhysicsSpace().add(treePhy2);

	}

	private void setupLights() {

		PssmShadowRenderer pssmr = new PssmShadowRenderer(assetManager, 1024, 3);
		pssmr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
		pssmr.setLambda(0.55f);
		pssmr.setShadowIntensity(0.5f);
		pssmr.setCompareMode(CompareMode.Hardware);
		pssmr.setFilterMode(FilterMode.Bilinear);
		viewPort.addProcessor(pssmr);

		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
		rootNode.addLight(dl);

		dl = new DirectionalLight();
		dl.setDirection(new Vector3f(0.5f, -0.1f, 0.3f).normalizeLocal());
		rootNode.addLight(dl);

	}

	private void createCollisionMarker() {
		Sphere s = new Sphere(6, 6, 1);
		collisionMarker = new Geometry("collisionMarker");
		collisionMarker.setMesh(s);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Orange);
		collisionMarker.setMaterial(mat);
		rootNode.attachChild(collisionMarker);
	}

	@Override
	public void simpleUpdate(float tpf) {

		if (gsd) {
			gsdCounter--;
			System.out.println(gsdCounter);
			if (gsdCounter == 0) {
				gsd = false;
				nifty.gotoScreen("start");
			}
			return;
		}

		if (controller.load) {

			if (frameCount == 1) {
				controller.setProgress(0.0f, "Despertando a Munrrael");
				setupKeys();
			} else if (frameCount == 2) {
				controller.setProgress(0.2f, "Creando Terreno");
				worldData = new WorldData();
				worldCreator = new WorldCreator( //
						this, rootNode, bulletAppState, worldData);
				setTerrainAndSkyBox();
			} else if (frameCount == 3) {
				controller.setProgress(0.3f,
						"Descubriendo el cielo y Despertando al sol");
				setupLights();
			} else if (frameCount == 4) {
				controller.setProgress(0.4f,
						"Plantando arboles y Construyendo casas");
				addObjects();
			} else if (frameCount == 5) {
				controller.setProgress(0.6f, "Creando Criaturas Asesinas");
				String[] walk = { "RunTop", "RunBase" };
				String[] idle = { "IdleTop", "IdleBase" };
				String[] attack = { "SliceHorizontal" };

				initPosCharacter = new Vector3f(-100, 20, -100);
				player = new Player( //
						rootNode, bulletAppState.getPhysicsSpace(), //
						assetManager, "Katherino", //
						"Models/Sinbad/Sinbad.mesh.xml", //
						walk, idle, attack, initPosCharacter, //
						1.0f, 2.0f, 7.0f, 10);

				player.addActionListener(new java.awt.event.ActionListener() {

					@Override
					public void actionPerformed(ActionEvent evt) {
						creatureTgt.setSelected(false);
						// TODO: AGREGAR LO QUE SE ANTOJE CUANDO EL
						// CHARACTER MUERA
						energyBarCharacter.paint(0, 0, 0, new Vector2f(-1000,
								-1000));
						audioSource.stop();
						setAudioDead();
						audioSource.play();
						ScreenControler.instance.sendMessage("System: "
								+ evt.getActionCommand());
						nifty.gotoScreen("dead");
						dead = true;
					}
				});

				ScreenControler.instance.addActionListener(player);
				
			} else if (frameCount == 6) {
				controller.setProgress(0.8f, "Dando de comer al munrrael");
				try {
					creatureList = CreaturesLoader.load(rootNode,
							bulletAppState.getPhysicsSpace(), assetManager,
							"creatures");
					NpcsLoader.setMain(this);
					npcList = NpcsLoader.load(rootNode,
							bulletAppState.getPhysicsSpace(), assetManager,
							"npcs");
					npcTgt = npcList.get(0);
				
				} catch (IOException e) {
					e.printStackTrace();
				}
				creatureTgt = creatureList.get(1);

				lastTime = System.currentTimeMillis();
				

			} else if (frameCount == 7) {
				controller.setProgress(1f, "Afilando espadas");
				flyCam.setEnabled(false);

				chaseCamera = new ChaseCamera( //
						cam, player.getMyNode(), inputManager);
				chaseCamera.setDefaultVerticalRotation(-50);
				chaseCamera.setMinDistance(10);
				chaseCamera.setMaxDistance(150);

				chaseCamera.setDefaultDistance(100);
				nifty.gotoScreen("end");
				started = true;

				pic = new Picture("HUD Picture");
				pic.setImage(assetManager, "com/gsd/jme3/images/energia.png",
						true);
				pic.setWidth(settings.getWidth() / 6);
				pic.setHeight(settings.getHeight() / 10);
				guiNode.attachChild(pic);

				energyBarCharacter = new EnergyBar(15, assetManager, guiNode);

				picCreature = new Picture("CreaturePicture");
				picCreature.setImage(assetManager,
						"com/gsd/jme3/images/energia.png", true);
				picCreature.setWidth(settings.getWidth() / 6);
				picCreature.setHeight(settings.getHeight() / 10);
				picCreature.setPosition(
						settings.getWidth() - settings.getWidth() / 6,
						settings.getHeight() - settings.getHeight() / 10);
				guiNode.attachChild(picCreature);

				picNpc = new Picture("NpcPicture");
				picNpc.setImage(assetManager,
						"com/gsd/jme3/images/energia.png", true);
				picNpc.setWidth(200);
				picNpc.setHeight(200);
				picNpc.setPosition(-1000, -1000);
				guiNode.attachChild(picNpc);

				energyCreatureBar = new EnergyBar(15, assetManager, guiNode);

				hudText1 = new BitmapText(guiFont, false);
				hudText1.setSize(guiFont.getCharSet().getRenderedSize()); // font
				// size
				hudText1.setColor(ColorRGBA.Black); // font color
				hudText1.setText("Energia"); // the text
				guiNode.attachChild(hudText1);

				hudText = new BitmapText(guiFont, false);
				hudText.setSize(guiFont.getCharSet().getRenderedSize()); // font
				// size
				hudText.setColor(ColorRGBA.Black); // font color
				hudText.setText("100/100"); // the text
				guiNode.attachChild(hudText);

				hudTextNpc = new BitmapText(guiFont, false);
				hudTextNpc.setColor(ColorRGBA.Black);
				guiNode.attachChild(hudTextNpc);

				hudTextCreature = new BitmapText(guiFont, false);
				hudTextCreature.setColor(ColorRGBA.Black);
				guiNode.attachChild(hudTextCreature);

				hudTextCreatureLife = new BitmapText(guiFont, false);
				hudTextCreatureLife.setColor(ColorRGBA.Black);
				guiNode.attachChild(hudTextCreatureLife);

				deadTime = 20;

			}
			frameCount++;
		}

		if (!started) {
			return;
		}

		updateEnergyBar();

		long time = System.currentTimeMillis();
		long dTime = (time - lastTime) * 3;

		for (Creature c : creatureList) {
			c.move(player, dTime, cam.getDirection());
		}

		for (Npc n : npcList) {
			n.move(player, dTime, cam.getDirection());
		}

		player.move(player, dTime, cam.getDirection());
		lastTime = System.currentTimeMillis();

		Vector3f origin = cam.getWorldCoordinates(
				inputManager.getCursorPosition(), 0.0f);
		Vector3f direction = cam.getWorldCoordinates(
				inputManager.getCursorPosition(), 0.3f);
		direction.subtractLocal(origin).normalizeLocal();
		Ray ray = new Ray(origin, direction);

		for (Creature c : creatureList) {

			if (c.getFirstCollition(ray)) {
				energyBar.paint(
						c.getCurrentLife(),
						c.getLife(),
						100,
						new Vector2f(inputManager.getCursorPosition().x
								- ((c.getLife() * 100) / c.getLife() / 2),
								inputManager.getCursorPosition().y + 5));
				break;
			}
			energyBar.paint(-100, -100, 0, new Vector2f(-100, -100));

		}

		if (dead) {
			deadTime--;
			System.out.println(deadTime);
			if (deadTime == 0) {
				deadTime = 20;
				dead = false;
				nifty.gotoScreen("end");
				audioSource.stop();
				setAudio();
				audioSource.play();
			}
		}

	}

	public void updateEnergyBar() {
		if (!dead) {
			energyBarCharacter.paint(player.getCurrentLife(), player.getLife(),
					156, new Vector2f(6, settings.getHeight() - 46));

			hudText.setText((int) player.getCurrentLife() + "/"
					+ player.getLife());
			pic.setPosition(0, settings.getHeight() - settings.getHeight() / 10);
			hudText.setLocalTranslation(60, settings.getHeight() - 48, 0);
			hudText1.setLocalTranslation(55, settings.getHeight() - 4, 0);
		} else {
			pic.setPosition(-1000, -100);
			hudText.setLocalTranslation(new Vector3f(-1000, -1000, 0));
			hudText1.setLocalTranslation(-1000, -1000, 0);
		}

		if (player.getCharacterTarget() == null
				|| player.getCharacterTarget().getType() == Utils.PhysicalCharacterType.NPC
				|| !creatureTgt.isSelected()) {
			energyCreatureBar.paint(0, 0, 0, new Vector2f(-1000, -1000));
			picCreature.setPosition(-1000, -1000);
			hudTextCreatureLife.setLocalTranslation(-1000, -1000, 0);
			hudTextCreature.setLocalTranslation(-1000, -1000, 0);

		} else {
			energyCreatureBar.paint(
					player.getCharacterTarget().getCurrentLife(),
					player.getCharacterTarget().getLife(),
					156,
					new Vector2f(settings.getWidth() - 163, settings
							.getHeight() - 46));
			picCreature.setPosition(settings.getWidth() - settings.getWidth()
					/ 6, settings.getHeight() - settings.getHeight() / 10);

			hudTextCreature.setText(creatureTgt.getName());
			hudTextCreature.setLocalTranslation(settings.getWidth() - 130,
					settings.getHeight() - 10, 0);

			hudTextCreatureLife.setText(creatureTgt.getCurrentLife() + "/"
					+ creatureTgt.getLife());
			hudTextCreatureLife.setLocalTranslation(settings.getWidth() - 120,
					settings.getHeight() - 48, 0);
		}

	}

	@Override
	public void onAction(String binding, boolean value, float tpf) {
		if (binding.equals("Strafe Left")) {
			if (value) {
				leftStrafe = true;
			} else {
				leftStrafe = false;
			}
		} else if (binding.equals("Strafe Right")) {
			if (value) {
				rightStrafe = true;
			} else {
				rightStrafe = false;
			}
		} else if (binding.equals("Rotate Left")) {
			if (value) {
				leftRotate = true;
			} else {
				leftRotate = false;
			}
		} else if (binding.equals("Rotate Right")) {
			if (value) {
				rightRotate = true;
			} else {
				rightRotate = false;
			}
		} else if (binding.equals("Walk Forward")) {
			if (value) {
				forward = true;
			} else {
				forward = false;
			}
		} else if (binding.equals("Walk Backward")) {
			if (value) {
				backward = true;
			} else {
				backward = false;
			}
		} else if (binding.equals("Jump")) {
			// player.jump();
		}
	}

	public AudioNode getAudioSource() {
		return audioSource;
	}

	public void setAudioSource(AudioNode audioSource) {
		this.audioSource = audioSource;
	}

}