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

	public void onBlockFromTo(BlockFromToEvent event) {

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
