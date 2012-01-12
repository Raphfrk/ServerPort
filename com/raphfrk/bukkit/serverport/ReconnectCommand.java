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

public class ReconnectCommand implements Runnable {
	
	final static MyServer server = MyServer.getServer();

	CommunicationManager communicationManager = null;
	String playerName = null;
	String targetServer = null;
	String lastGood = "here";
	String lastGoodServerHostname = "";
	
	String playerIP = "";
	
	Boolean isValid = false;

	PeerServerDatabase peerServerDatabase = null;
	

	synchronized boolean isValid() {
		return isValid;
	}

	ReconnectCommand( CommunicationManager communicationManager , String playerName, String playerIP , String targetServer ) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			unlockPlayer();
			return;
		}
				
		this.playerName = new String( playerName );
		this.playerIP   = new String( playerIP );
		
		this.targetServer = targetServer;
		
	}


	public void run() {

		lastGood = "here";
		String nextServer = targetServer;
		String command = "FORWARD:" + nextServer + ",somehostname";
		
		command = getNextServer( nextServer );

		if( command != null ) {
			String[] split = command.split(":");

			if( split.length != 2 ) {
				nextServer = null;
				command = null;
			} else {
				lastGood = command;
				String[] vars = split[1].split(",");
				nextServer = vars[0];
			}
		}
		
		if( command == null ) {
			
			final String finalPlayerName = new String( playerName );
			
			server.addToServerQueue( new Runnable() {
				
				public void run() {
					
					MyPlayer player = server.getPlayer(finalPlayerName);
					
					player.kick("[ServerPort] New players cannot connect to this server directly.");
					
				}
				
			});
			
		} else {
			
			String[] split = command.split(":");
			String[] params = split[1].split(",",-1);
			//MiscUtils.safeMessage(playerName, "[ServerPort] Forwarding complete : " + command );
			MiscUtils.safeLogging("[ServerPort] Teleporting due to new player redirect" );
			TeleportCommand.teleport(playerName, playerIP, params, lastGoodServerHostname );
			
		}
		
	}

		
		
	String getNextServer( String targetServer ) {
		
		MiscUtils.safeLogging("[ServerPort] Checking if " + playerName + " is on " + targetServer );

		PeerServerInfo peerServerInfoFromConnection = null;
		PeerServerInfo peerServerInfoFromDatabase = null;

		peerServerDatabase = communicationManager.peerServerDatabase;
		
		if( peerServerDatabase.getServer(targetServer) == null ) {
			MiscUtils.safeLogging( "[ServerPort] Unknown server " + targetServer);
			return null;
		}

		ServerPortClient serverPortClient = new ServerPortClient( communicationManager , targetServer );
		String error;

		if( (error=serverPortClient.connect()) != null ) {
			MiscUtils.safeLogging( "[ServerPort] " + error);
			return null;
		}
		
		synchronized( peerServerDatabase ) {

			peerServerInfoFromConnection = new PeerServerInfo();

			if( (error=serverPortClient.getPeerServerInfo(
					peerServerInfoFromConnection, 
					peerServerDatabase)) 

					!= null ) {
				MiscUtils.safeLogging( "[ServerPort] " + error);
				return null;
			}

			peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);

		}

		if( peerServerInfoFromDatabase == null ) {

			MiscUtils.safeMessage(playerName, "[ServerPort] Server " + targetServer + " is not known" );
			serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
			return null;
			
		} else {
			
			String forwardTarget = serverPortClient.sendRequest( 
					"FORWARD" , 
					playerName ,
					peerServerInfoFromDatabase, communicationManager.verbose);
			
			String[] split = forwardTarget.split(":");
			
			if( forwardTarget.indexOf("FORWARD") == 0 && split.length == 2 ) {
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				return forwardTarget;
				
			} else if( forwardTarget.indexOf("HERE") == 0 && split.length == 2 ) {
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				return forwardTarget;
			} else if( forwardTarget.indexOf("UNKNOWN") == 0 && split.length == 2 ) {
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				return forwardTarget;
			} 
			
			MiscUtils.safeMessage(playerName, "Reply was: " + forwardTarget);

		}

		if( (error = serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose) ) != null ) {
			MiscUtils.safeLogging( "[ServerPort] " + error);
		}
		
		return null;

	}


	void unlockPlayer() {
		
		final String finalPlayerName = new String( playerName );

		server.addToServerQueue(new Runnable() {

			public void run() {
				communicationManager.limboStore.unLockPlayer(finalPlayerName);

			}
		}
		);
		
	}

}
