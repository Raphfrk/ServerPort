/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.raphfrk.bukkit.serverport;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

public class WorldManager {
	
	HashSet<String> netherWorlds = new HashSet<String>();

	void loadWorlds(PortalManager portalManager) {
		
		Iterator<String> itr = portalManager.worldList.getValues().keySet().iterator();

		while(itr.hasNext()) {
			String world = itr.next();
			MiscUtils.safeLogging("[ServerPort] loading: " + world);
			String[] params = world.split(";");
			if(params.length==1) {
				MyServer.bukkitServer.createWorld(new WorldCreator(params[0]));
				continue;
			} else if(params.length==2) {
				if(params[1].equalsIgnoreCase("nether")) {
					MyServer.bukkitServer.createWorld(new WorldCreator(params[0]).environment(Environment.NETHER));
					netherWorlds.add(params[0]);
					continue;
				} else if(params[1].equalsIgnoreCase("normal")) {
					MyServer.bukkitServer.createWorld(new WorldCreator(params[0]).environment(Environment.NORMAL));
					continue;
				} else if(params[1].equals("end") || params[1].equals("theend") || params[1].equals("the_end")) {
					MyServer.bukkitServer.createWorld(new WorldCreator(params[0]).environment(Environment.THE_END));
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
