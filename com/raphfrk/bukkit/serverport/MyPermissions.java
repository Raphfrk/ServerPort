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
