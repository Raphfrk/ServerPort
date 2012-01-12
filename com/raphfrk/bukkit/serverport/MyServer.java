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

import java.io.File;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MyServer {

	public static org.bukkit.Server bukkitServer;
	public static JavaPlugin plugin;

	public static void setJavaPlugin(JavaPlugin plugin) {
		MyServer.plugin = plugin;
	}

	private static MyServer myServer = new MyServer();

	static void setBukkitServer( org.bukkit.Server server ) {

		MyServer.bukkitServer = server;
	}


	public static MyServer getServer() {

		return myServer;

	}

	boolean isPlayerListEmpty() {
		return bukkitServer.getOnlinePlayers().length == 0;
	}

	void messageAll( String message ) {

		if( message == null ) return;

		for( org.bukkit.entity.Player player : bukkitServer.getOnlinePlayers() ) {
			player.sendMessage(message);
		}
	}

	World getMainWorld() {
		return (World)bukkitServer.getWorlds().get(0);
	}

	void addToServerQueue( Runnable runnable, long delay ) {
		bukkitServer.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay/50);
	}

	void addToServerQueue( Runnable runnable ) {
		bukkitServer.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
	}

	void dropItem( MyLocation loc, int id, int quantity ) {
		loc.getBukkitLocation().getWorld().dropItem(loc.getBukkitLocation(), new ItemStack( id, quantity ));
	}

	MyPlayer getPlayer( String name ) {

		MyPlayer player = new MyPlayer();

		if( name == null ) return player;

		player.setBukkitPlayer(bukkitServer.getPlayer(name));

		return player;


	}

	int getBlockIdAt(World world, int x, int y, int z) {
		return world.getBlockAt(x, y, z).getTypeId();
	}

	void loadChunk(org.bukkit.World world, int x, int y, int z) {
		if(!world.isChunkLoaded(x>>4, z>>4)) {
			world.loadChunk(x>>4, z>>4);
		}

	}

	void setBlockAt(org.bukkit.World world, int id, int x, int y, int z) {
		org.bukkit.block.Block block = world.getBlockAt(x,y,z);
		block.setTypeId(id);
	}

	void setBlockData(org.bukkit.World world, int x, int y, int z, int d) {
		org.bukkit.block.Block block = world.getBlockAt(x,y,z);
		block.setData((byte)d);
	}

	String getColor(String color) {

		if( color.equals("Green"))     return org.bukkit.ChatColor.GREEN.toString();
		if( color.equals("LightBlue")) return org.bukkit.ChatColor.AQUA.toString();
		if( color.equals("White"))     return org.bukkit.ChatColor.WHITE.toString();
		return "";
	}

	int getBlockData(org.bukkit.World world, int x, int y, int z) {

		org.bukkit.block.Block block = world.getBlockAt(x,y,z);
		return block.getData()&0xFF;
	}

	MySign getComplexBlock( org.bukkit.World world, int x, int y, int z, int status ) {
		MySign sign = new MySign();

		org.bukkit.block.BlockState blockState = world.getBlockAt(x,y,z).getState();
		if( !blockState.getType().equals(org.bukkit.Material.WALL_SIGN) ) {
			sign.bukkitSign = null;
		} else {
			sign.bukkitSign = (org.bukkit.block.Sign)blockState;
			sign.status = status;
		}		
		return sign;
	}

	static public File baseFolder;

	public static File getBaseFolder() {
		return baseFolder;
	}

	boolean isNull() {
		return bukkitServer == null;	
	}

}
