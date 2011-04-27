package com.raphfrk.bukkit.serverport;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;


public class ChatCommand implements Runnable {


	StringList targetServers = null;
	
	CommunicationManager communicationManager = null;
	String playerName = null;
	String targetServer = null;
	String message = null;
	String targetPlayer = null;
	
	HashMap<String,Long> onlineTargetServers = null;
	
	boolean isValid = false;

	PeerServerDatabase peerServerDatabase = null;

	synchronized boolean isValid() {
		return isValid;
	}

	ChatCommand( CommunicationManager communicationManager , String playerName , String targetServer , String message , HashMap<String,Long> onlineTargetServers) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			return;
		}

		this.playerName = new String(playerName);
		this.targetServer = new String(targetServer);
		this.message = new String(message);
		this.onlineTargetServers = onlineTargetServers;

		isValid = true;

	}
	
	ChatCommand( CommunicationManager communicationManager , String targetPlayer, String playerName , String targetServer , String message , HashMap<String,Long> onlineTargetServers) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			return;
		}

		if( targetPlayer != null ) {
			this.targetPlayer = new String(targetPlayer);
		}
		this.playerName = new String(playerName);
		this.targetServer = new String(targetServer);
		this.message = new String(message);
		this.onlineTargetServers = onlineTargetServers;

		isValid = true;

	}


	public void run() {
		

		PeerServerInfo peerServerInfoFromConnection = null;
		PeerServerInfo peerServerInfoFromDatabase = null;

		peerServerDatabase = communicationManager.peerServerDatabase;

		ServerPortClient serverPortClient = new ServerPortClient( communicationManager , targetServer );
		String error;

		if( (error=serverPortClient.connect()) != null ) {
			synchronized( onlineTargetServers ) {
				onlineTargetServers.put(targetServer, System.currentTimeMillis() );
			}
			MiscUtils.safeLogging("[ServerPort] " + error + " trying to connect to " + targetServer);
			return;
		} else {
			synchronized( onlineTargetServers ) {
				onlineTargetServers.put(targetServer, -1L );
			}
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

			MiscUtils.safeMessage(playerName, "[ServerPort] Server " + targetServer + " is not known" );
			
		} else {
				
			if(targetPlayer == null) {
				serverPortClient.sendRequest( "MESSAGE" , message , peerServerInfoFromDatabase, communicationManager.verbose);
			} else {
				String reply = serverPortClient.sendRequest( "TELL" , targetPlayer + "," + message , peerServerInfoFromDatabase, communicationManager.verbose);
				if(!reply.equals("UNKNOWN")) {
					MiscUtils.safeMessage(playerName, reply);
				}
			}

		}

		if( (error = serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose) ) != null ) {
			MiscUtils.safeMessage(playerName , "[ServerPort] " + error + " for connection to " + targetServer );
		} 
	}


	static String encodeString( String string ) {
		
		string = string.replace("E" , "EE");
		
		string = string.replace(";" , "sEs" );
		
		string = string.replace("," , "cEc" );
		
		string = string.replace(":" , "CEC" );
		
		string = string.replace("/" , "fEf" );
		
		string = string.replace("\\" , "bEb" );
		
		int length = string.length();
		
		StringBuilder sb = new StringBuilder();
		
		Formatter formatter = new Formatter(sb, Locale.US);
		
		for( int cnt=0;cnt<length;cnt++) {
			
			char current = string.charAt(cnt);
			
			//if( current == ' ' || (current >= 'A' && current <= 'Z' ) || ( current >= 'a' && current <= 'z') || ( current >= '0' && current <= '9') ) {
			if( current >= ' ' && current <= (char)127 ) {
				sb.append(current);
				
			} else {
				formatter.format("vEv%04x", (int)(current));
			}
			
		}
		
		string = sb.toString();
		
		return string;
		
	}
	
	static String decodeString( String string ) {
		
		
		
		int pos = 0;
		
		while( (pos = string.indexOf("vEv", pos )) != -1 ) {
			
			int length = string.length();
			
			if( pos < length - 7 ) {
				
				String num = string.substring(pos + 3 , pos + 7 );
				
				int code;
				try { 
				code = Integer.parseInt(num, 16);
				} catch ( NumberFormatException nfe ) {
					code = 32;
				}
				
				string = string.substring(0, pos) + ((char)code) + string.substring(pos+7,length);
				
			}
			
		}
		
		string = string.replace("fEf" , "/");
		
		string = string.replace("bEb" , "\\");
		
		string = string.replace("cEc", "," );
		
		string = string.replace("CEC", ":" );
		
		string = string.replace("sEs", ";" );
		
		string = string.replace("EE", "E");
		
		return string;
	}



}
