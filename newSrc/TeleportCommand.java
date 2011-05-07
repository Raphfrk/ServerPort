import org.bukkit.Player;


public class TeleportCommand implements Runnable {


	CommunicationManager communicationManager = null;
	String playerName = null;
	int playerHealth = 20;
	String targetServer = null;
	String targetGate = null;
	
	String homeServer = "";
	String homeGate = "";
	
	double rotX = 0;
	double rotY = 0;

	String playerData = "";

	String playerIP = "";

	boolean isValid = false;
	
	PeerServerDatabase peerServerDatabase = null;


	synchronized boolean isValid() {
		return isValid;
	}

	TeleportCommand( CommunicationManager communicationManager , String params , String playerData) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			restoreInventory();
			return;
		}

		String[] vars = params.split(",",-1);

		if( vars.length < 3 ) {
			isValid = false;
			restoreInventory();
			return;
		}

		this.playerName = new String(vars[0]);
		this.targetServer = new String(vars[1]);
		this.targetGate = new String(vars[2]);
		if( vars.length > 3 && MiscUtils.isInt(vars[3])) {
			this.playerHealth = MiscUtils.getInt(vars[3]);
		} else {
			isValid = false;
			restoreInventory();
			return;
		}
		if( vars.length > 4 ) {
			this.playerIP = new String(vars[4]);
		} else {
			isValid = false;
			restoreInventory();
			return;
		}
		
		if( vars.length > 6 && MiscUtils.isDouble(vars[5]) && MiscUtils.isDouble(vars[6])) {
			
			rotX = MiscUtils.getDouble(vars[5]);
			rotY = MiscUtils.getDouble(vars[6]);
			
		} else {
			isValid = false;
			restoreInventory();
			return;
		}
		
		if( vars.length > 8 ) {
			
			this.homeServer = new String( vars[7] );
			this.homeGate = new String( vars[8] );
			
			
		} else {
			isValid = false;
			restoreInventory();
			return;
		}
		
		

		this.playerData = new String(playerData);

		isValid = true;

		MiscUtils.safeLogging("[ServerPort] Attempting to teleport " + this.playerName + " to " + this.targetGate + " on " + this.targetServer );


		
	}


	public void run() {


		PeerServerInfo peerServerInfoFromConnection = null;
		PeerServerInfo peerServerInfoFromDatabase = null;

		peerServerDatabase = communicationManager.peerServerDatabase;

		ServerPortClient serverPortClient = new ServerPortClient( communicationManager , targetServer );
		String error;

		if( (error=serverPortClient.connect()) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
			restoreInventory();
			unlockPlayer();
			return;
		}

		synchronized( peerServerDatabase ) {

			peerServerInfoFromConnection = new PeerServerInfo();

			if( (error=serverPortClient.getPeerServerInfo(
					peerServerInfoFromConnection, 
					peerServerDatabase)) 

					!= null ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
				restoreInventory();
				unlockPlayer();
				return;
			}

			peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);

		}

		if( peerServerInfoFromDatabase == null ) {

			MiscUtils.safeMessage(playerName, "[ServerPort] Server " + targetServer + " is not known" );
			restoreInventory();

		} else {

			String gateExists = serverPortClient.sendRequest( 
					"GATETEST" , 
					targetGate + "," + playerIP,
					peerServerInfoFromDatabase);

			if( gateExists.equals("NOGATE")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Target gate does not exist");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				return;
			} else if( gateExists.equals("BADIP")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] The target server refused to connections from your IP");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				return;
			} else if( !gateExists.equals("OK") ) {

				MiscUtils.safeMessage(playerName, "[ServerPort] Target gate is not activated");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				return;
			}

			String reply = serverPortClient.sendRequest( "TRANSFERINV" , "playername=" + playerName + ";playerhealth=" + playerHealth + ";homeserver=" + homeServer + ";homegate=" + homeGate + ";" + playerData, peerServerInfoFromDatabase );

			if( MiscUtils.errorCheck(reply) != null ) {
				MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				return;
			} 

			if( reply.indexOf("REJECTEDITEMS") == 0 ) {
				communicationManager.commandFIFO.runMainCommand(reply);

				reply = serverPortClient.sendRequest( "TELEPORT", playerName + "," + targetGate + "," + rotX + "," + rotY , peerServerInfoFromDatabase);

				if( MiscUtils.errorCheck(reply) != null ) {
					MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
					serverPortClient.close(peerServerInfoFromConnection);
					restoreInventory();
					return;
				} 

				if( reply.indexOf("OK") == 0 ) {

					String[] okSplit = reply.split(":",-1);

					if( okSplit.length != 2 ) {
						MiscUtils.safeMessage(playerName, "[ServerPort] Reply from server did not have the correct number of parts");
						serverPortClient.close(peerServerInfoFromConnection);
						restoreInventory();
						return;
					}

					String[] vars = okSplit[1].split(",",-1);

					LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(playerName);

					limboInfo.setCurrentServer(targetServer);
					limboInfo.setCurrentGate(targetGate);
					communicationManager.limboStore.updateDatabase(limboInfo);

					teleport( playerName , playerIP , vars , peerServerInfoFromDatabase.hostname );

				}
			}

			MiscUtils.safeMessage(playerName, "Reply was: " + reply);

		}

		if( (error = serverPortClient.close(peerServerInfoFromConnection) ) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
		}

	}


	void restoreInventory( ) {

		final String finalPlayerName = new String(playerName);
		final String finalPlayerData = new String(playerData);

		etc.getServer().addToServerQueue(new Runnable() {

			public void run() {
				LimboStore.addToPlayerInv( "STORE:" + finalPlayerName + ";" + finalPlayerData );
			}
		}
		);

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


	static void teleport( String playerName , String playerIP , String[] params , String hostname ) {


		int gameport;
		String globalHostname = ChatCommand.decodeString(params[0]);
		
		boolean singleHost = true;
		
		String[] multiHostSplit = globalHostname.split(",");
		
		globalHostname = multiHostSplit[0];
		
		if( multiHostSplit.length > 1 ) {
			
			singleHost = false;
			int cnt = 0;
			
			while( multiHostSplit.length > cnt+1 ) {
				
				if( playerIP.matches(multiHostSplit[cnt+1])) {
					globalHostname = multiHostSplit[cnt];
					cnt = multiHostSplit.length;
				}
			
				cnt+=2;
				
			}
				
		}
		
		String[] hostSplit = globalHostname.split(":");

		if( hostSplit.length == 2 && MiscUtils.isInt(hostSplit[1])) {

			globalHostname = hostSplit[0];
			gameport = MiscUtils.getInt(hostSplit[1]);
			MiscUtils.safeLogging("[ServerPort] portnum overridden by globalhostname");

		} else if( params.length > 1 && MiscUtils.isInt(params[1])) {
			gameport = MiscUtils.getInt(params[1]);
		} else {
			gameport = 25565;
		}

		if( 
				MiscUtils.isAddressLocal(hostname) &&
				MiscUtils.isAddressLocal(playerIP) &&
				singleHost
		) {
			globalHostname = hostname;
			MiscUtils.safeLogging("[ServerPort] Local IPs detected using localhost for teleport (" + globalHostname + ")");
		}

		String kickString;

		if( gameport == 25565 ) {
			kickString = "[Serverport] You have teleported, please connect to : " + globalHostname;
		} else {
			kickString = "[Serverport] You have teleported, please connect to : " + globalHostname + ":" + gameport;
		}


		final String finalKickString = new String(kickString);
		final String finalPlayerName = new String(playerName);

		etc.getServer().addToServerQueue(new Runnable() {

			public void run() {

				Player player = etc.getServer().getPlayer(finalPlayerName);
				if( player != null ) {
					player.kick( finalKickString );
				}
			}

		});
	}
	
	static void teleportToBind( CommunicationManager communicationManager , Player player  ) {
		
		LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());
		
		String targetServer;
		String targetGate;
		
		synchronized( limboInfo ) {
			targetServer = limboInfo.getHomeServer();
			targetGate = limboInfo.getHomeGate();
		}
		
		if( targetServer.equals("none")) {
			
			player.setHealth(0);
			
			return;
			
		} 
		
		teleport( communicationManager , player , targetServer, targetGate );
		
	}
	
	static void teleport( CommunicationManager communicationManager , Player player , String targetServer , String targetGate ) {
		
		PortalInfo portalInfo = new PortalInfo();
		
		portalInfo.targetServer = targetServer;
		portalInfo.targetGate = targetGate;
		
		if( targetServer.equals("here") ) {
			
			portalInfo = communicationManager.portalManager.getPortal(targetGate);
			
			if( portalInfo == null ) {
				
				MiscUtils.safeMessage(player, "[ServerPort] Unknown target gate " + targetGate );
				
			} else {
				
				IntLocation locInt = portalInfo.getExitPoint();
				
				Location loc = new Location( 0.5+(double)locInt.getX() , (double)locInt.getY() , 0.5+(double)locInt.getZ() , (float)portalInfo.getDir() , (float)0 );
				
				player.teleportTo(loc);
				
			}
			
		} else {
			teleport( communicationManager , player , portalInfo );
		}
		
	}
	
	static void teleport( CommunicationManager communicationManager , Player player , PortalInfo portalInfo ) {
		
		communicationManager.limboStore.lockPlayer(player.getName(), 5000);

		String playerData = LimboStore.removePlayerInv( player );
		
		LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());
		
		String homeServer = "";
		String homeGate = "";
		
		if( limboInfo != null ) {
			homeServer = limboInfo.getHomeServer();
			homeGate = limboInfo.getHomeGate();
			if( homeServer.equals("here") ) {
				homeServer = new String( communicationManager.serverPortServer.serverName );
			}
		}
		
		double portalDir = portalInfo.getDir();

		TeleportCommand teleportCommand = new TeleportCommand( 
				communicationManager , 
				player.getName() + "," + 
				portalInfo.targetServer + "," + 
				portalInfo.targetGate + "," + 
				player.getHealth() + "," + 
				player.getIP() + "," +
				( player.getRotation() - portalDir ) + "," +
				player.getPitch() + "," +
				homeServer + "," + 
				homeGate, 
				playerData );

		if( teleportCommand.isValid ) {
			Thread t = new Thread( teleportCommand );
			t.start();
		} else {
			MiscUtils.safeMessage(player, "The teleport command was formatted incorrectly");
		}
		
	}

}
