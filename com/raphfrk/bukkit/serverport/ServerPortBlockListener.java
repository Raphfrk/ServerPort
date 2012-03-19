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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortBlockListener implements Listener {

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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {

		MyItem newItem = new MyItem();
		newItem.setBukkitItem(event.getItemInHand(), 0);
		Player player = event.getPlayer();
		Block blockPlaced = event.getBlockPlaced();
		Block blockClicked = event.getBlockAgainst();

		serverPortListenerCommon.onBlockPlace(new MyPlayer(player), new MyBlock(blockPlaced, 0), new MyBlock(blockClicked, 0), newItem );
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		MyBlock block = new MyBlock();
		block.setBukkitBlock(event.getBlock(), BLOCK_BROKEN);

		MyPlayer player = new MyPlayer(event.getPlayer());

		if( serverPortListenerCommon.onBlockDestroy(player, block) ) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockFromTo(BlockFromToEvent event) {

		MyBlock blockFrom = new MyBlock(event.getBlock(), 0);
		MyBlock blockTo = new MyBlock(event.getToBlock(), 0);

		if( serverPortListenerCommon.onFlow(blockFrom, blockTo) ) {
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPhysics(BlockPhysicsEvent event) {

		MyBlock block = new MyBlock(event.getBlock(), 0);

		if( serverPortListenerCommon.onBlockPhysics(block, false) ) {
			event.setCancelled(true);
		}

	}

}
