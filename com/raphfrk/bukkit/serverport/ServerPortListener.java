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

public class ServerPortListener /*extends PluginListener*/ {
/*
	Plugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortListener( ServerPort serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	public void setPortalManager( PortalManager portalManager ) {
		this.serverPortListenerCommon.setPortalManager(portalManager);
	}

	public void setCommunicationManager( CommunicationManager communicationManager ) {
		this.serverPortListenerCommon.setCommunicationManager(communicationManager);
	}

	public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {

		return serverPortListenerCommon.onBlockPlace(new MyPlayer(player), new MyBlock(blockPlaced), new MyBlock(blockClicked), new MyItem(itemInHand));
		
	}

	public void onLogin(Player player) {
		
		serverPortListenerCommon.onLogin(new MyPlayer(player));

	}
	

    public boolean onHealthChange(Player player, int oldValue, int newValue) {

    	return serverPortListenerCommon.onHealthChange(new MyPlayer(player), oldValue, newValue);
    }


	public void onPlayerMove(Player player, Location from, Location to) {
		
		serverPortListenerCommon.onPlayerMove(new MyPlayer(player), new MyLocation(from), new MyLocation(to));

	}

	public boolean onBlockDestroy(Player player, Block block) {

		return serverPortListenerCommon.onBlockDestroy(new MyPlayer(player), new MyBlock(block));
	}


	public boolean onChat(Player player, String message) {

		return serverPortListenerCommon.onChat(new MyPlayer(player), message);
	}


	public boolean onCommand(Player player, String[] split) {
		
		return serverPortListenerCommon.onCommand(new MyPlayer(player), split);

	}


	public boolean onSignChange(Player player, Sign sign) {
		
		return serverPortListenerCommon.onSignChange(new MyPlayer(player), new MySign(sign));
	}
	
    	
	// Preventing portal "mist" from updating


	public boolean onBlockPhysics(Block block, boolean placed) {

		return serverPortListenerCommon.onBlockPhysics(new MyBlock(block), placed);
	}


	public boolean onFlow(Block blockFrom, Block blockTo) {

		return serverPortListenerCommon.onFlow(new MyBlock(blockFrom), new MyBlock(blockTo));
	}
	*/

}
