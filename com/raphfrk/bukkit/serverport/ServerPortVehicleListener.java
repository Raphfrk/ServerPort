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

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortVehicleListener implements Listener {

	JavaPlugin serverPort = null;

	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortVehicleListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}
	
	
	HashMap<Integer,IntLocation> oldPositions = new HashMap<Integer,IntLocation>();
	
	@EventHandler(priority = EventPriority.NORMAL)
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
