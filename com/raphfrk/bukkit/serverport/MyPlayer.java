package com.raphfrk.bukkit.serverport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
		bukkitPlayer.teleportTo(loc.getBukkitLocation());
		teleportTo = loc.getBukkitLocation();
	}

	static String[][] comments = new String[][] {
		new String[] {"Allows sign activated gates of type <gate type> to be created from <from> to <to>" , "create_gate_type" , "<gate type>, <from>, <to>"},
		new String[] {"Allows flint activated gates of type <gate type> to be created from <from> to <to>" , "create_fire_gate_type" , "<gate type>, <from>, <to>"},
		new String[] {"Allows gates of type <gate type> connecting <from> to <to> to be used" , "use_gate_type" , "<gate type>, <from>, <to>"},
		new String[] {"Allows gates of type <gate type> connecting <from> to <to> to be destroyed" , "destroy_gate" , "<gate type>, <from>, <to>"},
		new String[] {"Allows use of /cancelredirect command",  "cancel_redirect" , "allow"},
		new String[] {"Allows use of /release command",  "release" , "allow"},
		new String[] {"Allows use of /regengates command",  "regen_gates" , "allow"},
		new String[] {"Allows use of /serverport command",  "serverport" , "allow"},
		new String[] {"Allows use of /drawgate command",  "draw_gate" , "<gate type>"},
	};

	static HashMap<String,HashMap> hashMaps = new HashMap<String,HashMap>();

	boolean permissionCheck(String permissionName, String[] params) {

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

	boolean isAdmin(String player) {
		return permissionCheck("admins", new String[] {});
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

