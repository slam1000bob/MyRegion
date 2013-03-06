package com.TactileCraft.MyRegion;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class cmdProtect implements CommandExecutor {

	private MyRegion plugin;
	private WorldGuardPlugin WGplugin;

	public cmdProtect(MyRegion plugin, WorldGuardPlugin WGplugin) {
		this.plugin = plugin;
		this.WGplugin = WGplugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("protect")){
			Player player = plugin .getServer().getPlayer(sender.getName());
			MyPlayer mp = plugin.getPlayer(player.getName());
			if(!player.hasPermission("MyRegion.Protect")){
				player.sendMessage(ChatColor.DARK_RED+"You do not have permisson to do this!");
				return true;
			}
			if(args.length == 0 || args[0].equalsIgnoreCase("list")){
				int i = 1;
				player.sendMessage(ChatColor.GOLD+"---------=[MyRegions]=---------");
				for(World world : plugin.getServer().getWorlds())
				for( Entry<String, ProtectedRegion> proreg : WGplugin.getGlobalRegionManager().get(world).getRegions().entrySet()){
					ProtectedRegion reg = proreg.getValue();
					if(reg.getOwners().contains(WGplugin.wrapPlayer(player))){
						String s = reg.getId();
						String e = reg.getId();
						s = ChatColor.YELLOW+"("+i+")"+ChatColor.DARK_PURPLE+s;
						if(plugin.getPendingRegion(e) != null){
							s = s + ChatColor.RED + "  Pending...";
						}
						player.sendMessage(s);
						i++;
					}
				}
				player.sendMessage(ChatColor.GOLD+"-------------------------------");
			}else if(args[0].equalsIgnoreCase("pos1")){
				Location loc = player.getLocation();
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
			}else if(args[0].equalsIgnoreCase("pos2")){
				Location loc = player.getLocation();
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
			}else if(args[0].equalsIgnoreCase("create") && args.length > 1 && args[1] != null){
				if(mp.getPos1() == null || mp.getPos2() == null){
					player.sendMessage(ChatColor.DARK_RED+"Please make a selection first! (/protect pos1 and /protect pos2");
					return true;
				}
				int currentPending = 0;
				for(PRegion reg : plugin.getPendingRegions())
					if(reg.getPlayerName().equalsIgnoreCase(player.getName()))
						currentPending++;
				if(currentPending >= plugin.maxPendingRegionsPerPlayer){
					player.sendMessage(ChatColor.DARK_RED+"You have too many regions pending right now please wait!");
					return true;
				}
				ProtectedRegion proreg = getRegion(args[1]);
				if(proreg == null){
					ProtectionCreateEvent pce = new ProtectionCreateEvent(player, mp.getPos1(), mp.getPos2(), args[1]);
					plugin.getServer().getPluginManager().callEvent(pce);
				}else
					player.sendMessage(ChatColor.RED+"That name is already taken!");
			}else if(args[0].equalsIgnoreCase("remove")){
				if(args.length > 2 && args[1] != null && args[2] != null){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						RegionManager rm = WGplugin.getRegionManager(world);
						rm.removeRegion(proreg.getId());
						try { rm.save(); }catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("pending")){
				if(player.hasPermission("MyRegion.Control")){
					int i = 1;
					player.sendMessage(ChatColor.GOLD+"---------=[Pending Regions]=---------");
					for(PRegion reg : plugin.getPendingRegions()){
						player.sendMessage(ChatColor.YELLOW+"("+i+") "+ChatColor.DARK_PURPLE+reg.getRegionName()+ChatColor.YELLOW+" : "+ChatColor.DARK_PURPLE+reg.getPlayerName());
						i++;
					}
					player.sendMessage(ChatColor.GOLD+"-------------------------------------");
				}
			}else if(args[0].equalsIgnoreCase("info")){
				if(args.length > 1 && args[1] != null){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						
						String owners = "";
						for(String own : proreg.getOwners().getPlayers())
							owners += own+" ";
						
						String members = "";
						for(String mem : proreg.getMembers().getPlayers())
							members += mem+" ";
						
						BlockVector corner1 = proreg.getMaximumPoint();
						BlockVector corner2 = proreg.getMinimumPoint();
						
						CuboidSelection sel = new CuboidSelection(world, corner1, corner2);
						int area = sel.getArea()/256;
						
						double price = plugin.pricePerBlock*area+plugin.setRegionPrice;
						if(price < 0)
							price = 0;
						
						boolean pending = false;
						for(PRegion reg : plugin.getPendingRegions())
							if(reg.getRegionName().equalsIgnoreCase(proreg.getId()))
								pending = true;
						
						player.sendMessage(ChatColor.GOLD+"---------=[Region Info]=---------");
						player.sendMessage(ChatColor.YELLOW+"Region Name: "+ChatColor.DARK_PURPLE+proreg.getId() + (pending ? "" : ChatColor.DARK_RED+"  pending..."));
						player.sendMessage(ChatColor.YELLOW+"World: "+ChatColor.DARK_PURPLE+world.getName());
						player.sendMessage(ChatColor.YELLOW+"Owners: "+ChatColor.DARK_PURPLE+owners);
						player.sendMessage(ChatColor.YELLOW+"Members: "+ChatColor.DARK_PURPLE+members);
						player.sendMessage(ChatColor.YELLOW+"Corner 1: "+ChatColor.DARK_AQUA+"X= "+corner1.getBlockX()+" Z= "+corner1.getBlockZ());
						player.sendMessage(ChatColor.YELLOW+"Corner 2: "+ChatColor.DARK_AQUA+"X= "+corner2.getBlockX()+" Z= "+corner2.getBlockZ());
						player.sendMessage(ChatColor.YELLOW+"Surface Area: "+ChatColor.DARK_PURPLE+area);
						player.sendMessage(ChatColor.YELLOW+"Price: "+price);
						player.sendMessage(ChatColor.GOLD+"---------------------------------");
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("accept")){
				if(player.hasPermission("MyRegion.Control")){
					if(args.length > 1 && args[1] != null){
						for(PRegion reg : plugin.getPendingRegions()){
							if(reg.getRegionName().equalsIgnoreCase(args[1])){
								plugin.removePendingRegion(reg);
								player.sendMessage(ChatColor.AQUA+"Region "+args[1]+" has been accepted!");
								player.performCommand("mail send "+reg.getPlayerName()+" Region "+reg.getRegionName()+" has been accepted!");
								for(Player p : plugin.getServer().getOnlinePlayers())
									if(p.hasPermission("MyRegion.Control") && !p.getName().equalsIgnoreCase(player.getName()))
										p.sendMessage(ChatColor.AQUA+"Region "+reg.getRegionName()+" has been accepted!");
								return true;
							}
						}
					}
				}
			}else if(args[0].equalsIgnoreCase("deny")){
				if(player.hasPermission("MyRegion.Control")){
					if(args.length > 1 && args[1] != null){
						for(PRegion reg : plugin.getPendingRegions()){
							if(reg.getRegionName().equalsIgnoreCase(args[1])){
								World world = plugin.getServer().getWorld(reg.getWorldName());
								WGplugin.getGlobalRegionManager().get(world).removeRegion(reg.getRegionName());
								plugin.removePendingRegion(reg);
								player.sendMessage(ChatColor.DARK_AQUA+"Region "+args[1]+" has been denied!");
								player.performCommand("mail send "+reg.getPlayerName()+" Region "+reg.getRegionName()+" has been denied!");
								for(Player p : plugin.getServer().getOnlinePlayers())
									if(p.hasPermission("MyRegion.Control") && !p.getName().equalsIgnoreCase(player.getName()))
										p.sendMessage(ChatColor.AQUA+"Region "+reg.getRegionName()+" has been denied!");
								return true;
							}
						}
					}
				}
			}else if(args[0].equalsIgnoreCase("tp")){
				if(player.hasPermission("MyRegion.Control")){
					if(args.length > 1 && args[1] != null){
						ProtectedRegion proreg = getRegion(args[1]);
						World world = getWorld(proreg);
						if(proreg == null){
							player.sendMessage(ChatColor.RED+"Region not found!");
							return true;
						}
						Vector vec = proreg.getMaximumPoint();
						Location loc = new Location(world, vec.getX(), vec.getY(), vec.getZ());
						player.teleport(SafeLocation.getSafeDestination(loc), TeleportCause.COMMAND);
						player.sendMessage(ChatColor.AQUA+"You have been teleported to region " + proreg.getId() + ".");
						return true;
					}
				}
			}else if(args[0].equalsIgnoreCase("addMember")){
				if(args.length > 2 && args[1] != null && args[2] != null){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						proreg.getMembers().addPlayer(args[2]);
						player.sendMessage(ChatColor.AQUA+"Player "+ args[2] + " has been added to region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("removeMember")){
				if(args.length > 2 && args[1] != null && args[2] != null){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						proreg.getMembers().removePlayer(args[2]);
						player.sendMessage(ChatColor.AQUA+"Player "+ args[2] + " has been removed from region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("Yowu dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("setEntryMsg")){
				if(args.length > 2 && args[1] != null){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						String s = "";
						for(int i = 2; i<args.length; i++){
							s += args[i] + " ";
						}
						proreg.setFlag(DefaultFlag.GREET_MESSAGE, s);
						player.sendMessage(ChatColor.AQUA+"Greeting message set for region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("setExitMsg")){
				if(args.length > 2 && args[1] != null ){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						String s = "";
						for(int i = 2; i<args.length; i++){
							s += args[i]+ " ";
						}
						proreg.setFlag(DefaultFlag.FAREWELL_MESSAGE, s);
						player.sendMessage(ChatColor.AQUA+"Farewell message set for region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("priority")){
				if(args.length > 2 && args[1] != null ){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						int priority = 1;
						try{
							priority = Integer.parseInt(args[2]);
						}catch(NumberFormatException e){
							player.sendMessage(ChatColor.RED+"Please input a valid number!");
							return true;
						}
						proreg.setPriority(priority);
						player.sendMessage(ChatColor.AQUA+"Priority set for region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("use")){
				if(args.length > 2 && args[1] != null ){
					ProtectedRegion proreg = getRegion(args[1]);
					World world = getWorld(proreg);
					if(proreg == null){
						player.sendMessage(ChatColor.RED+"Region not found!");
						return true;
					}
					if(proreg.getOwners().contains(player.getName()) || player.hasPermission("MyRegion.Control")){
						if(args[2].equalsIgnoreCase("deny")){
							proreg.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
						}else if(args[2].equalsIgnoreCase("allow")){
							proreg.setFlag(DefaultFlag.USE, StateFlag.State.ALLOW);
						}else{
							player.sendMessage(ChatColor.RED+"Please use deny or allow!");
							return true;
						}
						player.sendMessage(ChatColor.AQUA+"Use flag set for region "+ proreg.getId()+".");
						try {
							WGplugin.getGlobalRegionManager().get(world).save();
						} catch (ProtectionDatabaseException e) {}
					}else
						player.sendMessage("You dont have permission to do this!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("help")){	
				player.sendMessage("http://tinyurl.com/myregions");
			}else
				player.sendMessage(ChatColor.DARK_RED+"Incorrect syntax!");
			return true;	
		}
		return false;
	}
	
	private World getWorld(ProtectedRegion proreg) {
		for(World world : plugin.getServer().getWorlds())
			if(WGplugin.getRegionManager(world) != null && 
				WGplugin.getRegionManager(world).getRegion(proreg.getId()) != null)
				return world;
		return null;
	}

	private ProtectedRegion getRegion(String name){
		ProtectedRegion reg = null;
		for(World world : plugin.getServer().getWorlds()){
			reg = WGplugin.getGlobalRegionManager().get(world).getRegion(name);
			if(reg != null) break;
		}
		return reg;
	}
}