package com.TactileCraft.MyRegion;

import org.bukkit.Location;

public class MyPlayer {
	private String name;
	private Location pos1;
	private Location pos2;
	
	public MyPlayer(String Name){
		setName(Name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getPos1() {
		return pos1;
	}

	public void setPos1(Location pos1) {
		this.pos1 = pos1;
	}

	public Location getPos2() {
		return pos2;
	}

	public void setPos2(Location pos2) {
		this.pos2 = pos2;
	}
	
	
}
