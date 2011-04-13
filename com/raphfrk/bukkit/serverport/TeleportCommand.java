package com.raphfrk.bukkit.serverport;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;


public class TeleportCommand implements Runnable {

	final static MyServer server = MyServer.getServer();
	
	CommunicationManager communicationManager = null;
	String playerName = null;
	int playerHealth = 20;
	String targetServer = null;
	String targetGate = null;
	
	String homeServer = "";
	String homeGate = "";
	
	boolean killOnFail = false;
	
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
			if( killOnFail ) killPlayer(playerName);
			return;
		}

		String[] vars = params.split(",",-1);
		
		if( vars.length > 9 && MiscUtils.isBoolean(vars[9])) {
			this.killOnFail = MiscUtils.getBoolean(vars[9]);
		} else {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
			return;
		}

		if( vars.length < 3 ) {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
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
			if( killOnFail ) killPlayer(playerName);
			return;
		}
		if( vars.length > 4 ) {
			this.playerIP = new String(vars[4]);
		} else {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
			return;
		}
		
		if( vars.length > 6 && MiscUtils.isDouble(vars[5]) && MiscUtils.isDouble(vars[6])) {
			
			rotX = MiscUtils.getDouble(vars[5]);
			rotY = MiscUtils.getDouble(vars[6]);
			
		} else {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
			return;
		}
		
		if( vars.length > 8 ) {
			
			this.homeServer = new String( vars[7] );
			this.homeGate = new String( vars[8] );
			
			
		} else {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
			return;
		}
		
		if( vars.length > 9 && MiscUtils.isBoolean(vars[9])) {
			this.killOnFail = MiscUtils.getBoolean(vars[9]);
		} else {
			isValid = false;
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);
			return;
		}
		
		

		this.playerData = new String(playerData);

		isValid = true;

		MiscUtils.safeLogging("[ServerPort] Attempting to teleport " + this.playerName + " to " + this.targetGate + " on " + this.targetServer );


		
	}

	static HashSet<String> teleportingPlayers = new HashSet<String>();

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
			if( killOnFail ) killPlayer(playerName);
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
				if( killOnFail ) killPlayer(playerName);
				return;
			}

			peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);

		}

		if( peerServerInfoFromDatabase == null ) {

			MiscUtils.safeMessage(playerName, "[ServerPort] Server " + targetServer + " is not known" );
			restoreInventory();
			if( killOnFail ) killPlayer(playerName);

		} else {

			String gateExists = serverPortClient.sendRequest( 
					"GATETEST" , 
					targetGate + "," + playerIP + "," + playerName,
					peerServerInfoFromDatabase);

			if( gateExists.equals("NOGATE")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Target gate does not exist");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				if( killOnFail ) killPlayer(playerName);
				return;
			} else if( gateExists.equals("BADIP")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] The target server refused to connections from your IP");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				if( killOnFail ) killPlayer(playerName);
				return;
			} else if( gateExists.equals("SPAMSHIELD")) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Teleport refused due to cooldown timer");
				serverPortClient.close(peerServerInfoFromConnection);
				restoreInventory();
				unlockPlayer();
				if( killOnFail ) killPlayer(playerName);
				return;
			} else if( !gateExists.equals("OK") ) {

				if( MiscUtils.errorCheck(gateExists) != null ) {
					MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(gateExists)  );
				} else {
					MiscUtils.safeMessage(playerName, "[ServerPort] Target gate is not activated");
				}
				restoreInventory();
				unlockPlayer();
				serverPortClient.close(peerServerInfoFromConnection);
				if( killOnFail ) killPlayer(playerName);
				return;
			}

			String reply = serverPortClient.sendRequest( "TRANSFERINV" , "playername=" + playerName + ";playerhealth=" + playerHealth + ";homeserver=" + homeServer + ";homegate=" + homeGate + ";" + playerData, peerServerInfoFromDatabase );

			if( MiscUtils.errorCheck(reply) != null ) {
				MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
				restoreInventory();
				serverPortClient.close(peerServerInfoFromConnection);
				if( killOnFail ) killPlayer(playerName);
				return;
			} 

			if( reply.indexOf("REJECTEDITEMS") == 0 ) {
				communicationManager.commandFIFO.runMainCommand(reply);

				reply = serverPortClient.sendRequest( "TELEPORT", playerName + "," + targetGate + "," + rotX + "," + rotY , peerServerInfoFromDatabase);

				if( MiscUtils.errorCheck(reply) != null ) {
					MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
					restoreInventory();
					serverPortClient.close(peerServerInfoFromConnection);
					if( killOnFail ) killPlayer(playerName);
					return;
				} 

				if( reply.indexOf("OK") == 0 ) {

					String[] okSplit = reply.split(":",-1);

					if( okSplit.length != 2 ) {
						MiscUtils.safeMessage(playerName, "[ServerPort] Reply from server did not have the correct number of parts");
						restoreInventory();
						serverPortClient.close(peerServerInfoFromConnection);
						if( killOnFail ) killPlayer(playerName);
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

		server.addToServerQueue(new Runnable() {

			public void run() {
				LimboStore.addToPlayerInv( "STORE:" + finalPlayerName + ";" + finalPlayerData );
			}
		}
		);

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

		server.addToServerQueue(new Runnable() {

			public void run() {

				MyPlayer player = server.getPlayer(finalPlayerName);
				if( player != null && !player.isNull() ) {
					//MyServer.bukkitServer.broadcastMessage("[ServerPort] " + player.getName() + " is about to teleport");
					synchronized(teleportingPlayers) {
						teleportingPlayers.add(finalPlayerName);
					}
					String playerName = player.getName();
					CommandFIFO.spamShield.put(playerName, System.currentTimeMillis());
					player.kick( finalKickString );
					
				}
			}

		});
	}
	
	static boolean playerKicked(String playerName) {
		synchronized(teleportingPlayers) {
			if(teleportingPlayers.contains(playerName)) {
				teleportingPlayers.remove(playerName);
				return true;
			} else {
				return false;
			}
		}
	}
	
	static MyLocation teleportToBind( CommunicationManager communicationManager , MyPlayer player  ) {
		
		LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());
		
		String targetServer;
		String targetGate;
		
		synchronized( limboInfo ) {
			targetServer = limboInfo.getHomeServer();
			targetGate = limboInfo.getHomeGate();
		}
		
		if( targetServer.equals("none") || targetGate.equals("none")) {
			
			//player.setHealth(0);
			
			return null;
			
		}  else {
			
			MyLocation ret = teleport( communicationManager , player , targetServer, targetGate , true );
			return ret;
			
		}
		
		
		
	}
	
	static Integer getLocalWorld(String name) {
		List<World> worlds = MyServer.bukkitServer.getWorlds();
		for(int cnt=0;cnt<worlds.size();cnt++) {
			if(worlds.get(cnt).getName().equalsIgnoreCase(name)) {
				return cnt;
			}
		}
		return null;
	}
	
	static MyLocation teleport( CommunicationManager communicationManager , MyPlayer player , String targetServer , String targetGate , boolean killOnFail ) {
		
		PortalInfo portalInfo = new PortalInfo();
		
		portalInfo.targetServer = targetServer;
		portalInfo.targetGate = targetGate;
		
		Integer targetIndex = null;
		PortalInfo localGate = communicationManager.portalManager.getPortal(targetGate);
		if( localGate != null ) {
			targetIndex = getLocalWorld(localGate.portalWorld);
		}
		
		if( targetServer.equals("here") || targetIndex != null ) {

			portalInfo = localGate;
			
			if( portalInfo == null ) {
				
				MiscUtils.safeMessage(player, "[ServerPort] Unknown target gate " + targetGate );
				
				return null;
				
			} else {
				
				if(targetIndex==null) {
					targetIndex = 0;
				}
				
				IntLocation locInt = portalInfo.getExitPoint();
				
				MyLocation loc = new MyLocation( (World)MyServer.bukkitServer.getWorlds().get(targetIndex), 0.5+(double)locInt.getX() , (double)locInt.getY() , 0.5+(double)locInt.getZ() , (float)portalInfo.getDir() , (float)0 );
				
				return loc;
				
			}
			
		} else {
			teleport( communicationManager , player , portalInfo , killOnFail );
			return null;
		}
		
	}
	
	static MyLocation teleport( CommunicationManager communicationManager , MyPlayer player , PortalInfo portalInfo , boolean killOnFail ) {
		
		communicationManager.limboStore.lockPlayer(player.getName(), 5000);

		String playerData = killOnFail?"":LimboStore.removePlayerInv( player );
		
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
				homeGate + "," + 
				killOnFail , 
				playerData );

		if( teleportCommand.isValid ) {
			Thread t = new Thread( teleportCommand );
			t.start();
		} else {
			MiscUtils.safeMessage(player, "The teleport command was formatted incorrectly");
		}
		
		return null;
		
	}
	
	void killPlayer(String playerName) {
		
		/*MiscUtils.safeLogging("Activating delayed kill on " + playerName );
		
		final String finalName = new String(playerName);
		
		MyServer.getServer().addToServerQueue(new Runnable() {
			
			public void run() {
				
				MyPlayer player = MyServer.getServer().getPlayer(finalName);
				
				player.setHealth(0);
				
			}
			
		});*/
		
	}

}
