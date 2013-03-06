package com.TactileCraft.MyRegion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class MyRegion extends JavaPlugin{
	private static MyRegion plugin;
	private final Logger logger = Logger.getLogger("Logger");
	private WorldGuardPlugin WGplugin;
	@SuppressWarnings("unused")
	private WorldEditPlugin WEplugin;
	private Economy eco;
	private File configFile;
	private FileConfiguration config;
	private File regionsFile;
	private FileConfiguration regions;
	
	private ArrayList<PRegion> pendingRegions = new ArrayList<PRegion>();
	private Map<String, MyPlayer> players = new HashMap<String, MyPlayer>();
	
	public double pricePerBlock = 0;
	public int minRegionSize = 0;
	public int maxRegionSize = Integer.MAX_VALUE;
	public double setRegionPrice = 0;
	public int maxPendingRegionsPerPlayer = 100;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		Plugin tempPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    if (tempPlugin == null || !(tempPlugin instanceof WorldGuardPlugin)) {
	    	logger.severe("[MyRegion] NO WORLDGUARD!");
	        return;
	    }
	    WGplugin = (WorldGuardPlugin) tempPlugin;
	    tempPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
	    if (tempPlugin == null || !(tempPlugin instanceof WorldEditPlugin)) {
	    	logger.severe("[MyRegion] NO WORLDEDIT!");
	        return;
	    }
	    WEplugin = (WorldEditPlugin) tempPlugin;
	    if(!setupEconomy()){
	    	logger.severe("[MyRegion] NO VAULT!");
	    }
	    
	    configFile = new File(getDataFolder(), "config.yml");
	    regionsFile = new File(getDataFolder(), "regions.yml");
		try {
			firstRun();
		} catch (Exception e) {e.printStackTrace();}
		config = new YamlConfiguration();
		regions = new YamlConfiguration();
		loadYamls();
		
		loadRegions();
		
		getCommand("protect").setExecutor(new cmdProtect(plugin, WGplugin));
		
		plugin.getServer().getPluginManager().registerEvents(new StuffListener(plugin, WGplugin), plugin);
		
		for(Player player : getServer().getOnlinePlayers()){
			addPlayer(new MyPlayer(player.getName()));
		}
	    
	    logger.info("MyRegion has been enabled!");
	}

	@Override
	public void onDisable(){
		writeRegions();
		logger.info("MyRegion has been disabled!");
	}
	
	private void loadRegions() {
		if(regions.contains("PendingRegions"))
			for(String name : regions.getConfigurationSection("PendingRegions").getKeys(false)){
				String world = regions.getString("PendingRegions."+name+".World");
				String player = regions.getString("PendingRegions."+name+".Player");
				System.out.println("Loaded: "+name+" "+world+" "+player);
				pendingRegions.add(new PRegion(world, name, player));
			}
		else
			regions.createSection("PendingRegions");
		if(config.contains("PricePerBlock"))
			pricePerBlock = config.getDouble("PricePerBlock");
		else
			config.set("PricePerBlock", pricePerBlock);
		if(config.contains("MinRegionSize"))
			minRegionSize = config.getInt("MinRegionSize");
		else
			config.set("MinRegionSize", minRegionSize);
		if(config.contains("MaxRegionSize"))
			maxRegionSize = config.getInt("MaxRegionSize");
		else
			config.set("MaxRegionSize", maxRegionSize);
		if(config.contains("SetRegionPrice"))
			setRegionPrice = config.getDouble("SetRegionPrice");
		else
			config.set("SetRegionPrice", setRegionPrice);
		if(config.contains("MaxPendingRegionsPerPlayer"))
			maxPendingRegionsPerPlayer = config.getInt("MaxPendingRegionsPerPlayer");
		else
			config.set("MaxPendingRegionsPerPlayer", maxPendingRegionsPerPlayer);
	}
	
	private void writeRegions() {
		FileConfiguration regions = new YamlConfiguration();
		regions.createSection("PendingRegions");
		for(PRegion reg : pendingRegions){
			regions.createSection("PendingRegions."+reg.getRegionName());
			regions.set("PendingRegions."+reg.getRegionName()+".World", reg.getWorldName());
			regions.set("PendingRegions."+reg.getRegionName()+".Player", reg.getPlayerName());
		}
		try {
			regions.save(regionsFile);
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	    if(!regionsFile.exists()){
	    	regionsFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), regionsFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void saveYamls() {
	    try {
	    	config.save(configFile);
	    	regions.save(regionsFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	    	config.load(configFile);
	    	regions.load(regionsFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            eco = economyProvider.getProvider();
        }
        return (eco != null);
    }
	
	public static MyRegion getInstance(){return plugin;}
	public ProtectedRegion getRegion(String reg, World world){return WGplugin.getRegionManager(world).getRegion(reg);}
	public PRegion getPendingRegion(String name){
		for(PRegion reg : pendingRegions){
			if(reg.getRegionName().equalsIgnoreCase(name)){
				return reg;
			}
		}
		return null;
	}
	public MyPlayer getPlayer(String name){if(players.containsKey(name))return players.get(name);return null;}
	public boolean charge(Player player, double amount){
		if(eco != null)
		if(eco.getBalance(player.getName()) >= amount){
			eco.withdrawPlayer(player.getName(), amount);
			return true;
		}
		return false;
	}
	public void addPendingRegion(PRegion reg){pendingRegions.add(reg);}
	public void removePendingRegion(PRegion reg){
		pendingRegions.remove(reg);
	}
	public void removePendingRegion(String name){
		for(PRegion reg : pendingRegions){
			if(reg.getRegionName().equalsIgnoreCase(name)){
				removePendingRegion(reg);
				return;
			}
		}
	}
	public FileConfiguration getFileConfig(){return config;}
	public void addPlayer(MyPlayer mp) {players.put(mp.getName(), mp);}
	public void removePlayer(String name) {if(players.containsKey(name)) players.remove(name);}
	public ArrayList<PRegion> getPendingRegions(){return pendingRegions;}
}
