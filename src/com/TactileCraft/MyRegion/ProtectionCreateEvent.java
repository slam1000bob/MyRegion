package com.TactileCraft.MyRegion;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProtectionCreateEvent extends Event implements Cancellable{
	
	private static final HandlerList handlers = new HandlerList();
	
	private Location pos1;
	private Location pos2;
	private Player player;
	private String protectionName;
	private boolean cancelled = false;
	
	public ProtectionCreateEvent(Player p, Location p1, Location p2, String pName){
		setPlayer(p);
		setPos1(p1);
		setPos2(p2);
		setProtectionName(pName);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
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

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getProtectionName() {
		return protectionName;
	}

	public void setProtectionName(String protectionName) {
		this.protectionName = protectionName;
	}

	@Override
	public boolean isCancelled() {return cancelled;}

	@Override
	public void setCancelled(boolean can) {
		cancelled = can;
	}

}
