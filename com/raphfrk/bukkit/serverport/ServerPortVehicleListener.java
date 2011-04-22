package com.raphfrk.bukkit.serverport;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortVehicleListener extends VehicleListener {

	JavaPlugin serverPort = null;

	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortVehicleListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}
	
	
	HashMap<Integer,IntLocation> oldPositions = new HashMap<Integer,IntLocation>();
	
	@Override
    public void onVehicleMove(VehicleMoveEvent event) {
		
		Entity passenger = event.getVehicle().getPassenger();
		
		if(passenger == null || !(passenger instanceof Player) ) {
			return;
		}
		
		MyPlayer player = new MyPlayer((Player)passenger);
		
		int entityId = event.getVehicle().getEntityId();

		
		IntLocation oldPos = oldPositions.get(entityId);
		IntLocation newPos = new IntLocation(new MyLocation(event.getTo()));

		oldPositions.put(entityId, newPos );

		if(oldPos == null) {
			return;
		}
		
		if(oldPos.equals(newPos)) {
			return;
		}
		
		PortalManager portalManager = serverPortListenerCommon.portalManager;
		if( portalManager.testPortalBlock(newPos) && !portalManager.testPortalBlock(oldPos) ) {
			
			String gateType = portalManager.getPortalType( newPos );

			if( gateType == null ) {
				gateType = "";
			}

			PortalInfo portalInfo = portalManager.getPortalByBlock(newPos);

			if( player.permissionCheck("use_gate_type", new String[] {portalInfo.portalType, newPos.getWorld().getName(), portalManager.getDestination(portalInfo) } ) ) {
				oldPositions.remove(entityId);
				portalManager.enteredPortal(player, new MyLocation(event.getTo()));
			}
			
		}

		
    }
	
}
