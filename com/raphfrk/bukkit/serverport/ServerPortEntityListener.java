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

