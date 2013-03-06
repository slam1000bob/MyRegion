package com.TactileCraft.MyRegion;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class StuffListener implements Listener{
	
	private MyRegion plugin;
	private WorldGuardPlugin WGplugin;
	
	public StuffListener(MyRegion plugin, WorldGuardPlugin WGplugin){
		this.plugin = plugin;
		this.WGplugin = WGplugin;
	}
	
	@EventHandler
	public void onProtectionCreate(ProtectionCreateEvent event){
		Location pos1 = event.getPos1();
		Location pos2 = event.getPos2();
		Player player = event.getPlayer();
		pos1.setY(0);
		pos2.setY(256);
		org.bukkit.util.BlockVector pos1Vec = pos1.toVector().toBlockVector();
		org.bukkit.util.BlockVector pos2Vec = pos2.toVector().toBlockVector();
		ProtectedCuboidRegion pr = new ProtectedCuboidRegion(event.getProtectionName(), 
				new BlockVector(pos1Vec.getX(), pos1Vec.getY(), pos1Vec.getZ()), 
				new BlockVector(pos2Vec.getX(), pos2Vec.getY(), pos2Vec.getZ()));
		int size = pr.volume()/256;
		if(size > plugin.maxRegionSize){
			player.sendMessage(ChatColor.RED+"Region too large to protect!");
			return;
		}else if(size < plugin.minRegionSize){
			player.sendMessage(ChatColor.RED+"Region too small to protect!");
			return;
		}
		DefaultDomain dd = new DefaultDomain();
		dd.addPlayer(player.getName());
		pr.setOwners(dd);
		RegionManager rm = WGplugin.getGlobalRegionManager().get(pos1.getWorld());
		List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
		regions.addAll(WGplugin.getRegionManager(pos1.getWorld()).getRegions().values());
		try {
			for(ProtectedRegion reg : pr.getIntersectingRegions(regions)){
				if(!reg.getOwners().contains(player.getName()) && !reg.getMembers().contains(player.getName())){
					player.sendMessage(ChatColor.RED+"Your selection overlaps another region that you dont own/are a member of!");
					return;
				}
			}
		} catch (UnsupportedIntersectionException e) {
			player.sendMessage(ChatColor.RED+"Unsupported overlaping, see your server administrator!");
			return;
		}
		double price = plugin.pricePerBlock*size+plugin.setRegionPrice;
		if(price < 0)
			price = 0;
		if(!plugin.charge(event.getPlayer(), price)){
			player.sendMessage(ChatColor.RED+"You cant afford an area that big! ($"+price+")");
			return;
		}
		rm.addRegion(pr);
		try {
			rm.save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
		}
		PRegion uareg = new PRegion(pos1.getWorld().getName(), event.getProtectionName(), event.getPlayer().getName());
		plugin.addPendingRegion(uareg);
		
		player.sendMessage(ChatColor.AQUA+"Region "+event.getProtectionName()+"created and is pending acception! It cost $" + price +".");
		for(Player p : plugin.getServer().getOnlinePlayers())
			if(p.hasPermission("MyRegion.Control") && !p.getName().equalsIgnoreCase(player.getName()))
				p.sendMessage(ChatColor.AQUA+"Region "+event.getProtectionName()+" is now pending acceptance!");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		plugin.addPlayer(new MyPlayer(event.getPlayer().getName()));
		Player player = event.getPlayer();
		if(player.hasPermission("MyRegion.Control"))
			if(plugin.getPendingRegions().size() > 0)
				player.sendMessage(ChatColor.RED+"[MyRegion]"+ChatColor.YELLOW+"There are "+plugin.getPendingRegions().size()+" regions pending!");
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		plugin.removePlayer(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event){
		Player player = event.getPlayer();
		MyPlayer mp = plugin.getPlayer(player.getName());
		if(event.getPlayer().getItemInHand().getType() == Material.STICK){
			if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK){
				Block block = player.getTargetBlock(null, 100);
				Location loc = block.getLocation();
				if(block.getType() == Material.AIR){
					player.sendMessage(ChatColor.RED+"Please look at a block/too far away!");
					return;
				}
				mp.setPos1(loc);
				player.sendMessage(ChatColor.LIGHT_PURPLE+"Postion 1 set!");
				player.sendMessage(ChatColor.LIGHT_PURPLE+"X:"+loc.getBlockX()+" Y:"+loc.getBlockY()+" Z:"+loc.getBlockZ());
				if(mp.getPos2() != null){
					Location pos1 = mp.getPos1();
					Location pos2 = mp.getPos2();
					pos1.setY(0);
					pos2.setY(256);
					org.bukkit.util.BlockVector pos1Vec = pos1.toVector().toBlockVector();
					org.bukkit.util.BlockVector pos2Vec = pos2.toVector().toBlockVector();
					ProtectedCuboidRegion pr = new ProtectedCuboidRegion("Temp", 
							new BlockVector(pos1Vec.getX(), pos1Vec.getY(), pos1Vec.getZ()), 
							new BlockVector(pos2Vec.getX(), pos2Vec.getY(), pos2Vec.getZ()));
					CuboidSelection sel = new CuboidSelection(pos1.getWorld(), pos1, pos2);
					int size = pr.volume()/256;
					if(size > plugin.maxRegionSize){
						player.sendMessage(ChatColor.RED+"Region too large to protect!");
					}else if(size < plugin.minRegionSize){
						player.sendMessage(ChatColor.RED+"Region too small to protect!");
					}else{
						double price = plugin.pricePerBlock*size+plugin.setRegionPrice;
						player.sendMessage(ChatColor.LIGHT_PURPLE+"Size: "+size+" ("+sel.getLength()+" X "+sel.getWidth()+") Price: "+price);
					}
				}
				event.setCancelled(true);
			}else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
				Block block = player.getTargetBlock(null, 100);
				Location loc = block.getLocation();
				if(block.getType() == Material.AIR){
					player.sendMessage(ChatColor.RED+"Please look at a block/too far away!");
					return;
				}
				mp.setPos2(loc);
				player.sendMessage(ChatColor.LIGHT_PURPLE+"Postion 2 set!");
				player.sendMessage(ChatColor.LIGHT_PURPLE+"X:"+loc.getBlockX()+" Y:"+loc.getBlockY()+" Z:"+loc.getBlockZ());
				if(mp.getPos1() != null){
					Location pos1 = mp.getPos1();
					Location pos2 = mp.getPos2();
					pos1.setY(0);
					pos2.setY(256);
					org.bukkit.util.BlockVector pos1Vec = pos1.toVector().toBlockVector();
					org.bukkit.util.BlockVector pos2Vec = pos2.toVector().toBlockVector();
					ProtectedCuboidRegion pr = new ProtectedCuboidRegion("Temp", 
							new BlockVector(pos1Vec.getX(), pos1Vec.getY(), pos1Vec.getZ()), 
							new BlockVector(pos2Vec.getX(), pos2Vec.getY(), pos2Vec.getZ()));
					CuboidSelection sel = new CuboidSelection(pos1.getWorld(), pos1, pos2);
					int size = pr.volume()/256;
					if(size > plugin.maxRegionSize){
						player.sendMessage(ChatColor.RED+"Region too large to protect!");
					}else if(size < plugin.minRegionSize){
						player.sendMessage(ChatColor.RED+"Region too small to protect!");
					}else{
						double price = plugin.pricePerBlock*size+plugin.setRegionPrice;
						player.sendMessage(ChatColor.LIGHT_PURPLE+"Size: "+size+" ("+sel.getLength()+" X "+sel.getWidth()+") Price: "+price);
					}
				}
				event.setCancelled(true);
			}
		}
	}
}
