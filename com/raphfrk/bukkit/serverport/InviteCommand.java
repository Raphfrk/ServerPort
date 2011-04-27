package com.raphfrk.bukkit.serverport;

public class InviteCommand implements Runnable {

	CommunicationManager communicationManager = null;
	String playerName = null;
	String hostname;
	int portnum;
	boolean isValid = false;

	PeerServerDatabase peerServerDatabase = null;

	synchronized boolean isValid() {
		return isValid;
	}

	InviteCommand( CommunicationManager communicationManager , String params ) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			return;
		}

		String[] vars = params.split(",");

		if( vars.length != 3 ) {
			isValid = false;
			return;
		}

		this.playerName = new String(vars[0]);
		this.hostname = new String(vars[1]);
		if( MiscUtils.isInt(vars[2])) {
			this.portnum = MiscUtils.getInt(vars[2]);
		} else {
			isValid = false;
			return;
		}

		isValid = true;

	}

	public void run() {

		PeerServerInfo peerServerInfoFromConnection = null;
		PeerServerInfo peerServerInfoFromDatabase = null;

		peerServerDatabase = communicationManager.peerServerDatabase;

		ServerPortClient serverPortClient = new ServerPortClient( communicationManager , hostname , portnum );
		String error;

		if( (error=serverPortClient.connect()) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
			return;
		}

		synchronized( peerServerDatabase ) {

			peerServerInfoFromConnection = new PeerServerInfo();

			if( (error=serverPortClient.getPeerServerInfo(
					peerServerInfoFromConnection, 
					peerServerDatabase)) 

					!= null ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
				return;
			}

			peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);

		}

		if( peerServerInfoFromDatabase == null ) {
			
			String passcode = MiscUtils.genRandomCode();
			String reply = serverPortClient.sendRequest( "PASSCODE" , passcode , peerServerInfoFromConnection , communicationManager.verbose);
			
			if( MiscUtils.errorCheck(reply) != null ) {
				MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				return;
			} 

			if( !reply.substring(0, passcode.length()).equals(passcode) ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] The server replied with an illegal passcode" );
				reply = serverPortClient.sendRequest( "ERROR" , "ILLEGAL-PASSCODE" , peerServerInfoFromConnection, communicationManager.verbose );
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				return;
			} else {
				peerServerInfoFromConnection.passcode = reply;
				MiscUtils.safeMessage(playerName, "[ServerPort] Passcode exchange handshake completed with " + peerServerInfoFromConnection.name);
				MiscUtils.safeMessage(playerName, "[ServerPort] Admin from other server must invite this server to complete connection");
				
				peerServerDatabase.setServer( peerServerInfoFromConnection );
			}

		} else {
						
			String reply = serverPortClient.sendRequest( "CONNECT" , "CONNECT" , peerServerInfoFromDatabase , communicationManager.verbose);
			
			if( MiscUtils.errorCheck(reply) != null ) {
				MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				return;
			} 

			if( reply.equals("OK")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Connection established with " + peerServerInfoFromConnection.name);
				MiscUtils.safeMessage(playerName, "[ServerPort] You can now create gates that will connect to that server");

				peerServerInfoFromConnection.connected = true;

				peerServerDatabase.setServer( peerServerInfoFromConnection );
			} else {
				MiscUtils.safeMessage(playerName, "[ServerPort] Server refused connection, admin on that server must invite this one");
				
			}
			
			
		}
		
		if( (error = serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose) ) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
		}

	}

}
