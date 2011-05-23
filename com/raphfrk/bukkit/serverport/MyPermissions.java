package com.raphfrk.bukkit.serverport;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class MyPermissions {

	private PermissionHandler permissionHandler;

	ServerPortBukkit p;
	MyPermissions (ServerPortBukkit p) {
		this.p = p;
	}
	
	public boolean isActive() {
		return permissionHandler != null;
	}

	void init() {
		Plugin permissionsPlugin = p.getServer().getPluginManager().getPlugin("Permissions");
		if (this.permissionHandler == null) {
			if (permissionsPlugin != null) {
				this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				MiscUtils.safeLogging("Connection to permissions established");
			} else {
				MiscUtils.safeLogging("Permission system not detected, defaulting to internal system");
			}
		}
	}

	public boolean has(Player player, String permission) {
		if(permissionHandler == null) {
			return false;
		} else {
			return permissionHandler.has(player, permission);
		}
	}
	
	public int getValue(String world, String player, String permission) {
		if (this.permissionHandler == null) {
			return -1;
		} else {
			return permissionHandler.getUserPermissionInteger(world, player, permission);
		}
	}
	
	public String[] getGroups(String world, String player) {
		if (this.permissionHandler == null) {
			return null;
		} else {
			return permissionHandler.getGroups(world, player);
		}
	}
}
