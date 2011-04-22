package com.raphfrk.bukkit.serverport;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.World.Environment;

public class WorldManager {
	
	HashSet<String> netherWorlds = new HashSet<String>();

	void loadWorlds(PortalManager portalManager) {
		
		Iterator<String> itr = portalManager.worldList.getValues().keySet().iterator();

		while(itr.hasNext()) {
			String world = itr.next();
			MiscUtils.safeLogging("[ServerPort] loading: " + world);
			String[] params = world.split(";");
			if(params.length==1) {
				MyServer.bukkitServer.createWorld(world, Environment.NORMAL);
				continue;
			} else if(params.length==2) {
				if(params[1].equalsIgnoreCase("nether")) {
					MyServer.bukkitServer.createWorld(params[0], Environment.NETHER);
					netherWorlds.add(params[0]);
					continue;
				} else if(params[1].equalsIgnoreCase("normal")) {
					MyServer.bukkitServer.createWorld(params[0], Environment.NETHER);
					continue;
				} 
			}
			MiscUtils.safeLogging("[ServerPort] Unable to load: " + world);
		}
		
	}
	
	boolean isNether(String worldName) {
		return netherWorlds.contains(worldName);
	}
	
}
