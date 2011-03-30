package com.raphfrk.bukkit.serverport;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortBlockListener extends BlockListener {

	public static int START_DIGGING = 1;
	public static int BLOCK_BROKEN = 3;

	JavaPlugin serverPort = null;

	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortBlockListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	public void onSignChange(SignChangeEvent event) {
	}

	public void onBlockPlace(BlockPlaceEvent event) {
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		MyBlock block = new MyBlock();
		block.setBukkitBlock(event.getBlock(), BLOCK_BROKEN);

		MyPlayer player = new MyPlayer(event.getPlayer());

		if( serverPortListenerCommon.onBlockDestroy(player, block) ) {
			event.setCancelled(true);
			return;
		}
	}

	public void onBlockDamage(BlockDamageEvent event) {

		MyBlock block = new MyBlock();
		block.setBukkitBlock(event.getBlock(), START_DIGGING);

		MyPlayer player = new MyPlayer(event.getPlayer());

		if( serverPortListenerCommon.onBlockDestroy(player, block) ) {
			event.setCancelled(true);
			return;
		}

		if( event.getBlock().getType().equals(Material.WALL_SIGN) ) {

			MySign sign = new MySign();

			sign.setBukkitSign((Sign)(event.getBlock()).getState(), 0);

			serverPortListenerCommon.onSignChange(player, sign);

		}

	}

	public void onBlockFlow(BlockFromToEvent event) {

		MyBlock blockFrom = new MyBlock(event.getBlock(), 0);
		MyBlock blockTo = new MyBlock(event.getToBlock(), 0);

		if( serverPortListenerCommon.onFlow(blockFrom, blockTo) ) {
			event.setCancelled(true);
		}

	}


	public void onBlockPhysics(BlockPhysicsEvent event) {

		MyBlock block = new MyBlock(event.getBlock(), 0);

		if( serverPortListenerCommon.onBlockPhysics(block, false) ) {
			event.setCancelled(true);
		}

	}

}
