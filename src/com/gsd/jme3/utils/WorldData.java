package com.gsd.jme3.utils;

public class WorldData {

	private String matRock;
	private String matRockTexture;
	private String matWire;
	private String heightMapImage;
	private String grass;
	private String dirt;
	private String rock;
	private String skyBox;

	public WorldData() {
		setDefaultData();
	}

	public WorldData( //
			String matRock, String matRockTexture, //
			String matWire, String heightMapImage, //
			String grass, String dirt, String rock, String skyBox) {

		this.matRock = matRock;
		this.matRockTexture = matRockTexture;
		this.matWire = matWire;
		this.heightMapImage = heightMapImage;
		this.grass = grass;
		this.dirt = dirt;
		this.rock = rock;
		this.skyBox = skyBox;

	}

	public void setDefaultData() {

		this.matRock = "Common/MatDefs/Terrain/Terrain.j3md";
		this.matRockTexture = "com/gsd/jme3/textures/alphamap.png";
		this.matWire = "Common/MatDefs/Misc/Unshaded.j3md";
		this.heightMapImage = "Textures/Terrain/splat/mountains512.png";
		this.grass = "Textures/Terrain/splat/grass.jpg";
		this.dirt = "Textures/Terrain/splat/dirt.jpg";
		this.rock = "Textures/Terrain/splat/dirt.jpg";
		this.skyBox = "/com/gsd/jme3/models/skyboxGrass.j3o";

	}

	public String getMatRock() {
		return matRock;
	}

	public void setMatRock(String matRock) {
		this.matRock = matRock;
	}

	public String getMatRockTexture() {
		return matRockTexture;
	}

	public void setMatRockTexture(String matRockTexture) {
		this.matRockTexture = matRockTexture;
	}

	public String getMatWire() {
		return matWire;
	}

	public void setMatWire(String matWire) {
		this.matWire = matWire;
	}

	public String getHeightMapImage() {
		return heightMapImage;
	}

	public void setHeightMapImage(String heightMapImage) {
		this.heightMapImage = heightMapImage;
	}

	public String getGrass() {
		return grass;
	}

	public void setGrass(String grass) {
		this.grass = grass;
	}

	public String getDirt() {
		return dirt;
	}

	public void setDirt(String dirt) {
		this.dirt = dirt;
	}

	public String getRock() {
		return rock;
	}

	public void setRock(String rock) {
		this.rock = rock;
	}

	public String getSkyBox() {
		return skyBox;
	}

	public void setSkyBox(String skyBox) {
		this.skyBox = skyBox;
	}

}
