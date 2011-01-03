
public class ReconnectCommand implements Runnable {


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
			
			final Server server = etc.getServer();
			
			server.addToServerQueue( new Runnable() {
				
				public void run() {
					
					Player player = server.getPlayer(finalPlayerName);
					
					player.kick("[ServerPort] New players cannot connect to this server directly.");
					
				}
				
			});
			
		} else {
			
			String[] split = command.split(":");
			String[] params = split[1].split(",",-1);
			//MiscUtils.safeMessage(playerName, "[ServerPort] Forwarding complete : " + command );
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
			serverPortClient.close(peerServerInfoFromConnection);
			return null;
			
		} else {
			
			String forwardTarget = serverPortClient.sendRequest( 
					"FORWARD" , 
					playerName ,
					peerServerInfoFromDatabase);
			
			String[] split = forwardTarget.split(":");
			
			if( forwardTarget.indexOf("FORWARD") == 0 && split.length == 2 ) {
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				serverPortClient.close(peerServerInfoFromConnection);
				return forwardTarget;
				
			} else if( forwardTarget.indexOf("HERE") == 0 && split.length == 2 ) {
				serverPortClient.close(peerServerInfoFromConnection);
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				return forwardTarget;
			} else if( forwardTarget.indexOf("UNKNOWN") == 0 && split.length == 2 ) {
				serverPortClient.close(peerServerInfoFromConnection);
				lastGoodServerHostname = peerServerInfoFromDatabase.hostname;
				return forwardTarget;
			} 
			
			MiscUtils.safeMessage(playerName, "Reply was: " + forwardTarget);

		}

		if( (error = serverPortClient.close(peerServerInfoFromConnection) ) != null ) {
			MiscUtils.safeLogging( "[ServerPort] " + error);
		}
		
		return null;

	}


	void unlockPlayer() {
		
		final String finalPlayerName = new String( playerName );

		etc.getServer().addToServerQueue(new Runnable() {

			public void run() {
				communicationManager.limboStore.unLockPlayer(finalPlayerName);

			}
		}
		);
		
	}

}
