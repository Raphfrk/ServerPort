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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class MyPlayer {

	private org.bukkit.entity.Player bukkitPlayer;

	private org.bukkit.Location teleportTo = null;

	MyPlayer() {
		bukkitPlayer = null;
	}

	public MyPlayer( org.bukkit.entity.Player player ) {
		bukkitPlayer = player;
	}

	void setBukkitPlayer( org.bukkit.entity.Player player ) {
		this.bukkitPlayer = player;
	}

	String getColor() {

		String displayName = bukkitPlayer.getDisplayName();
		if( displayName.indexOf("\u00A7") == 0 ) {
			return displayName.substring(0,2);
		}

		return "";

	}

	org.bukkit.Location getTeleportTo() {
		return teleportTo;
	}

	String getName() {

		return bukkitPlayer.getName();

	}
	
	Vehicle getVehicle() {
		
		if(bukkitPlayer == null || !bukkitPlayer.isInsideVehicle()) {
			return null;
		} else {
			return bukkitPlayer.getVehicle();
		}
		
	}
	
	org.bukkit.entity.Player getBukkitPlayer() {
		return bukkitPlayer;
	}
	
	boolean leaveVehicle() {
		return bukkitPlayer.leaveVehicle();
	}

	String getIP() {
		byte[] ip = bukkitPlayer.getAddress().getAddress().getAddress();
		return (ip[0]&0xFF) + "." + (ip[1]&0xFF) + "." + (ip[2]&0xFF) + "." + (ip[3]&0xFF);
	}

	MyInventory getInventory() {

		MyInventory inv = new MyInventory();

		inv.setBukkitInventory( bukkitPlayer.getInventory() );
		return inv;

	}

	MyLocation getLocation() {

		MyLocation loc = new MyLocation();

		loc.setBukkitLocation(bukkitPlayer.getLocation());
		return loc;


	}

	void sendMessage(String message) {

		if( bukkitPlayer != null ) {
			bukkitPlayer.sendMessage(message);
		}


	}

	void setHealth(int health) {
		bukkitPlayer.setHealth(health);
	}

	int getHealth() {
		return bukkitPlayer.getHealth();	
	}

	void teleportTo(MyLocation loc) {
		bukkitPlayer.teleport(loc.getBukkitLocation());
		teleportTo = loc.getBukkitLocation();
	}

	static String[][] comments = new String[][] {
		new String[] {"Allows sign activated gates of type <gate type> to be created from <from> to <to>" , "create_gate_type" , "<gate type>, <from-world>, <to-world/server>"},
		new String[] {"Allows flint activated gates of type <gate type> to be created from <from> to <to>" , "create_fire_gate_type" , "<gate type>, <from-world>, <to-world/server>"},
		new String[] {"Allows gates of type <gate type> connecting <from> to <to> to be used" , "use_gate_type" ,"<gate type>, <from-world>, <to-world/server>"},
		new String[] {"Allows gates of type <gate type> connecting <from> to <to> to be destroyed" , "destroy_gate" , "<gate type>, <from-world>, <to-world/server>"},
		new String[] {"Allows use of /cancelredirect command",  "cancel_redirect" , "allow"},
		new String[] {"Allows use of /release command",  "release" , "allow"},
		new String[] {"Allows use of /regengates command",  "regen_gates" , "allow"},
		new String[] {"Allows use of /serverport command",  "serverport" , "allow"},
		new String[] {"Allows use of /drawgate command",  "draw_gate" , "<gate type>"},
		new String[] {"Allows use of /stele command", "opteleport", "allow"}
	};

	static HashMap<String,HashMap> hashMaps = new HashMap<String,HashMap>();

	boolean permissionCheck(String permissionName, String[] params) {
		
		if(isOp()) {
			return true;
		}
		
		/*MyPermissions handler = ((ServerPortBukkit)MyServer.plugin).permissions;
		
		if(handler.isActive()) {
			StringBuilder sb = new StringBuilder("serverport." + permissionName);
			for(String current : params) {
				if(current != null && !current.equals("*")) {
					sb.append("." + current);
				}
			}
			String checkString = sb.toString();
			boolean adminCheck = !permissionName.equals("admins") && isAdmin();
			return adminCheck || handler.has(bukkitPlayer, checkString);
		}*/

		String[] paramsAndName = new String[params.length + 1];
		for(int cnt=0;cnt<params.length;cnt++) {
			if( params[cnt] == null ) {
				return false;
			} else {
				paramsAndName[cnt+1] = params[cnt].trim();
			}
		}
		paramsAndName[0] = getName();
		
		if(!hashMaps.containsKey(permissionName)) {

			File folder = new File(MyServer.getBaseFolder() + MiscUtils.slash + "permissions");

			if(!folder.exists()) {
				folder.mkdirs();
			}

			File file = new File(folder + MiscUtils.slash + permissionName + "_list.txt" );

			if(!file.exists()) {
				BufferedWriter out = null;

				try {
					out = new BufferedWriter(new FileWriter(file));
					
					
					String[] arr = null;
					
					for(String[] current: comments) {
						if(current[1].equalsIgnoreCase(permissionName)) {
							arr = current;
							break;
						}
					}

					if(arr!=null) {
						out.write("# " + arr[0]);
						out.newLine();
						out.write("# <playername>, " + arr[2]);
						out.newLine();
					}
					
				} catch (IOException e) {
				} finally {
					try {
						if(out!=null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}

			HashMap<String,HashMap> map = MiscUtils.fileToMap(file.toString(), true);

			hashMaps.put(permissionName.trim(), map);

		}
		
		if(MiscUtils.allowed(paramsAndName, 0, hashMaps.get(permissionName), true)) {
			return true;
		}
		
		if(!permissionName.equals("admins") && isAdmin()) {
			return true;
		}

		return false;

	}
	
	boolean isOp() {
		return bukkitPlayer != null && bukkitPlayer.isOp();
	}
	
	boolean isAdmin(String player) {
		if(bukkitPlayer != null && bukkitPlayer.isOp()) {
			return true;
		}
		boolean admin = permissionCheck("admins", new String[] {});
		return admin;
	}

	static HashSet<String> admins = null;

	int holding() {
		return bukkitPlayer.getItemInHand().getAmount();
	}

	boolean isAdmin() {
		return isAdmin(getName());	
	}

	float getRotation() {
		return bukkitPlayer.getLocation().getYaw();
	}

	float getPitch() {
		return bukkitPlayer.getLocation().getPitch();
	}

	org.bukkit.World getWorld() {
		return bukkitPlayer.getLocation().getWorld();
	}

	void kick( String message ) {
		final String finalMessage = message;
		final org.bukkit.entity.Player finalPlayer = bukkitPlayer;

		MyServer.getServer().addToServerQueue(new Runnable() {
			public void run() {
				if( finalPlayer != null && finalMessage != null ) {
					if( finalPlayer.isOnline() ) {
						finalPlayer.kickPlayer(finalMessage);
					}
				}
			}
		});
	}

	boolean isNull() {
		return bukkitPlayer == null || !bukkitPlayer.isOnline();
	}
}

