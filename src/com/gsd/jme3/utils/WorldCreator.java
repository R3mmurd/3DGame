package com.gsd.jme3.utils;

import jme3tools.converters.ImageToAwt;

import com.jme3.app.Application;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class WorldCreator {

	private Spatial skyBox;
	private TerrainQuad terrain;
	private Material matRock; 	
	private Material matWire;

	public WorldCreator(Application application, Node rootNode, //
			BulletAppState bulletAppState, WorldData worldData) {

		matRock = new Material(application.getAssetManager(),
				worldData.getMatRock());
		matRock.setTexture("Alpha",
				application.getAssetManager().loadTexture( //
						worldData.getMatRockTexture()));

		Texture heightMapImage = application.getAssetManager()
				.loadTexture(worldData.getHeightMapImage());
		Texture grass = application.getAssetManager()
				.loadTexture(worldData.getGrass());
		grass.setWrap(WrapMode.Repeat);
		matRock.setTexture("Tex1", grass);
		matRock.setFloat("Tex1Scale", 64f);

		Texture dirt = application.getAssetManager()
				.loadTexture(worldData.getDirt());
		dirt.setWrap(WrapMode.Repeat);
		matRock.setTexture("Tex2", dirt);
		matRock.setFloat("Tex2Scale", 32f);

		Texture rock = application.getAssetManager()
				.loadTexture(worldData.getRock());
		rock.setWrap(WrapMode.Repeat);
		matRock.setTexture("Tex3", rock);
		matRock.setFloat("Tex3Scale", 128f);

		matWire = new Material(application.getAssetManager(),
				worldData.getMatWire());
		matWire.getAdditionalRenderState().setWireframe(true);
		matWire.setColor("Color", ColorRGBA.Green);

		AbstractHeightMap heightmap = null;
		try {
			heightmap = new ImageBasedHeightMap(ImageToAwt.convert(
					heightMapImage.getImage(), false, true, 0), 0.25f);
			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}

		terrain = new TerrainQuad("terrain", 65, 513, //
				heightmap.getHeightMap());

		TerrainLodControl control = new TerrainLodControl( //
				terrain, application.getCamera());
		control.setLodCalculator(//
				new DistanceLodCalculator(65, 2.7f));

		terrain.setShadowMode(ShadowMode.CastAndReceive);
		terrain.addControl(control);
		terrain.setMaterial(matRock);
		terrain.setLocalScale(new Vector3f(2, 2, 2));
		terrain.setLocked(false); // unlock it so we can edit the height
		rootNode.attachChild(terrain);

		/**
		 * Create PhysicsRigidBodyControl for collision
		 */
		CollisionShape sceneShape = CollisionShapeFactory
				.createMeshShape((Node) terrain);
		terrain.addControl(new RigidBodyControl(sceneShape, 0));
		bulletAppState.getPhysicsSpace().addAll(terrain);

		skyBox = application.getAssetManager().loadModel(worldData.getSkyBox());
		rootNode.attachChild(skyBox);

	}

	public Spatial getSkyBox() {
		return skyBox;
	}

	public void setSkyBox(Spatial skyBox) {
		this.skyBox = skyBox;
	}

	public TerrainQuad getTerrain() {
		return terrain;
	}

	public void setTerrain(TerrainQuad terrain) {
		this.terrain = terrain;
	}

	public void getTerrainAndSkyBox(TerrainQuad terrain, Spatial skyBox) {
		terrain = this.terrain;
		skyBox = this.skyBox;
	}

	public void removeWorld(Node rootNode, BulletAppState bulletAppState) {
		bulletAppState.getPhysicsSpace().remove(terrain);
		rootNode.detachChild(terrain);
		rootNode.detachChild(skyBox);
	}

}
