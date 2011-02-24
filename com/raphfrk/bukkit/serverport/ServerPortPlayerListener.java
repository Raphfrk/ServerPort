package com.raphfrk.bukkit.serverport;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class ServerPortPlayerListener extends PlayerListener {

	JavaPlugin serverPort = null;

	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortPlayerListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, ItemStack itemInHand) {

		MyItem newItem = new MyItem();
		newItem.setBukkitItem(itemInHand, 0);

		return serverPortListenerCommon.onBlockPlace(new MyPlayer(player), new MyBlock(blockPlaced, 0), new MyBlock(blockClicked, 0), newItem );

	}

	public void onPlayerJoin(PlayerEvent event) {

		org.bukkit.entity.Player player = event.getPlayer();

		MyPlayer myPlayer = new MyPlayer(player);

		serverPortListenerCommon.onLogin(myPlayer);
	}

	public void onPlayerChat(PlayerChatEvent event) {

		MyPlayer player = new MyPlayer( event.getPlayer() );

		serverPortListenerCommon.onChat(player, event.getMessage());

	}

	public void onPlayerCommand(PlayerChatEvent event) {

		String[] split = event.getMessage().split(" ",-1);

		if( split[0].equals("/clear")) {
			event.getPlayer().getInventory().clear(Integer.parseInt(split[1]));
			event.setCancelled(true);
			return;
		}

		if( split.length > 1 && split[0].equals("/kickme")) {
			event.getPlayer().kickPlayer(split[1]);
			event.setCancelled(true);
		}

		MyPlayer player = new MyPlayer(event.getPlayer());

		boolean ret = serverPortListenerCommon.onCommand(player, split);

		if( ret ) {
			event.setCancelled(true);
		}


	}

	public void onPlayerRespawn(PlayerRespawnEvent e) {

		Player player = e.getPlayer();
		if( serverPortListenerCommon.communicationManager.limboStore.bindEnable ) {

			{
				//if(!deadPlayers.isEmpty()) {

					MyLocation loc = TeleportCommand.teleportToBind(serverPortListenerCommon.communicationManager, new MyPlayer(player));

					if( loc != null) {
						player.sendMessage("You have died, restoring your position to bind - new version");

						e.setRespawnLocation(loc.getBukkitLocation());
						return;

					} 

				//}
			}
			

			/*MiscUtils.safeLogging(player.getName() + " has died, attempting to teleport to bind");

			final String name = player.getName();

			MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(serverPort, new Runnable() {

				public void run() {
					deadPlayers.add(name); 
				}

			},20);
			*/
		}

	}

	public void onPlayerItem(PlayerItemEvent event) {

		Block target = event.getBlockClicked();

		ItemStack item = event.getItem();

		if( item.getType().equals(Material.FLINT_AND_STEEL)) {
			if( target != null && target.getType() != null && target.getType().equals(Material.OBSIDIAN)) {
				event.setCancelled(true);

				Block blockFace = target.getFace(event.getBlockFace());

				MyBlock myBlock = new MyBlock(event.getBlockClicked().getFace(event.getBlockFace()),0);

				MyPlayer player = new MyPlayer(event.getPlayer());

				if(serverPortListenerCommon.portalManager.testSignBlock(myBlock)) {
					serverPortListenerCommon.portalManager.buttonPress(myBlock, player);
				} else {
					serverPortListenerCommon.portalManager.fireStarted(player , myBlock );
				}
			}
		}


	}

	HashSet<String> deadPlayers = new HashSet<String>();
	HashMap<String, Long> spamShield = new HashMap<String, Long>();

	public void onPlayerMove(PlayerMoveEvent event) {

		if(!deadPlayers.isEmpty()) {
			Player player = event.getPlayer();

			if(player.getHealth()>0 && deadPlayers.contains(player.getName())) {

				long currentTime = System.currentTimeMillis();
				if( spamShield.containsKey(player.getName()) && spamShield.get(player.getName()) > currentTime - 5000 ) {
					return;
				}

				spamShield.put(player.getName(), currentTime);

				MyLocation loc = TeleportCommand.teleportToBind(serverPortListenerCommon.communicationManager, new MyPlayer(player));

				if( loc != null) {
					//player.sendMessage("You have died, restoring your position to bind");

					player.teleportTo(loc.getBukkitLocation());
					event.setTo(loc.getBukkitLocation());
					event.setFrom(loc.getBukkitLocation());
					deadPlayers.remove(player.getName());
					return;

				} else {
					
					deadPlayers.remove(player.getName());
					
				}

			}
		}

		MyPlayer player = new MyPlayer(event.getPlayer());

		org.bukkit.Location from = event.getFrom().clone();
		org.bukkit.Location to = event.getTo().clone();

		int fx = from.getBlockX();
		int fy = from.getBlockY();
		int fz = from.getBlockZ();

		int tx = to.getBlockX();
		int ty = to.getBlockY();
		int tz = to.getBlockZ();

		if( fx == tx && fy == ty && fz == tz )  return;

		from.setX(fx);
		from.setY(fy);
		from.setZ(fz);

		to.setX(tx);
		to.setY(ty);
		to.setZ(tz);

		MyLocation myFrom = new MyLocation(from);
		MyLocation myTo= new MyLocation(to);

		serverPortListenerCommon.onPlayerMove(player, myFrom, myTo);   	
		if( player.getTeleportTo() != null ) {
			event.setTo(player.getTeleportTo());
		}

	}



}
