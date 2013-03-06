package com.TactileCraft.MyRegion;

public class PRegion {
	private String worldName;
	private String regionName;
	private String playerName;
	
	public PRegion(String w, String r, String p){
		setWorldName(w);
		setRegionName(r);
		setPlayerName(p);
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
