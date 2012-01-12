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
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.java.JavaPlugin;


public class ServerPortEntityListener extends EntityListener implements Listener {
	
	JavaPlugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortEntityListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}
	
	@Override 
	public void onEntityExplode(EntityExplodeEvent event) {
		
		List<Block> blocks = event.blockList();
		
		PortalManager pm = serverPortListenerCommon.portalManager;
		
		for(Block b : blocks) {
			MyBlock block = new MyBlock(b, 0);
			if(pm.testProtectedBlock(block)) {
				event.setCancelled(true);
				return;
			}
		}
		
	}

/*	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		
		Entity entity = event.getEntity();
		
		if( entity == null ) return;
		
		if( !( entity instanceof Player )) return;
		
		Player player = (Player)entity;
		
		int currentHealth = player.getHealth();
		int newHealth = currentHealth - event.getDamage();
		
		if( serverPortListenerCommon.onHealthChange(new MyPlayer(player), currentHealth , newHealth ) ) {
			
			event.setCancelled(true);
			
		}

	
	}
	*/
	
	/*@Override
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
		System.out.println( "Block Damage event" + event);
    }

	@Override
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		System.out.println( "By entiry damage event" + event);
    }
    
	@Override
	public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
		System.out.println( "By projectile damage event" + event);
    }
    
	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		System.out.println( "By combust damage event" + event);
    }

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		System.out.println( "By explode damage event" + event);
    }*/

}

