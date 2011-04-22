package com.raphfrk.bukkit.serverport;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;


public class CommandFIFO {
	
	protected static final Logger log = Logger.getLogger("Minecraft");

	MyServer server = MyServer.getServer();	
	
	CommunicationManager communicationManager = null;
	PortalManager portalManager = null;
	LimboStore limboStore = null;
	
	void setCommunicationManager( CommunicationManager communicationManager ) {
		this.communicationManager = communicationManager;
	}
	
	void setPortalManager( PortalManager portalManager ) {
		this.portalManager = portalManager;
	}
	
	void setLimboStore( LimboStore limboStore ) {
		this.limboStore = limboStore;
		TimeUnit a;
	}
	
	String runMainCommand( String command ) {
		
		String input = new String(command);
		StringBuffer ret = new StringBuffer("");
		Object sync = new Object();
		
		SyncRunnable syncRunnable = new SyncRunnable( sync, ret, input , this );
				
		synchronized( sync ) {		
			server.addToServerQueue( syncRunnable );

			try {
				sync.wait();
			} catch (InterruptedException ie ) {
				MiscUtils.safeLogging(log, "[ServerPort] Command thread handover exception");
				ie.printStackTrace();
				return null;
			}

		}
		TimeoutException s;
		String retString = ret.toString();
		
		if( retString == null ) {
			return "null";
		} else {
			return ret.toString();
		}
		
		
		
	}
	
	public static ConcurrentHashMap<String,Long> spamShield = new ConcurrentHashMap<String,Long>();
	
	String syncedRunCommand( String command ) {
		
		String[] split = command.split(":");
		
		if( split.length != 2 ) {
			return "malformed command:" + command;
		} 
		
		String[] vars = split[1].split(",");
		
		if( split[0].equals("OPENGATE")) {
			
			return portalManager.activatePortalCommand(command);

		} else if( split[0].equals("TRANSFERINV")) {
			
			String rejectedItems = limboStore.processTransfer( command );
			
			if( rejectedItems != null ) {
				/*try {
					Object x = new Object();
					synchronized(x) {
						x.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				
				return "REJECTEDITEMS:" + rejectedItems;
			}
		} else if( split[0].equals("REJECTEDITEMS")) {
			
			String rejectedItems = LimboStore.addToPlayerInv( command );
			
			if( rejectedItems != null ) {
				return "REJECTEDITEMS:" + rejectedItems;
			}
		} else if( split[0].equals("GATETEST")) {
			
			String playerIP = "";
			
			if( vars.length > 1 ) {
				playerIP = vars[1];
			}
			
			String playerName = null;
			Long currentTime = null;
			if( vars.length > 2 ) {
				playerName = vars[2];
				currentTime = System.currentTimeMillis();
				Long lastTeleport = spamShield.get(playerName);
				if(lastTeleport != null && ( lastTeleport + portalManager.teleportCooldown ) > currentTime) {
					return "SPAMSHIELD";
				}
				
			}
			
			PortalInfo portalInfo = portalManager.getPortal(vars[0]);
			
			if( portalInfo == null ) {
				return "NOGATE";
			} else {
				if( !playerIP.matches(communicationManager.serverPortServer.requiredIP) ) {
					return "BADIP";
				} else if( portalInfo.isActive() ) {
					if(playerName != null) {
						spamShield.put(playerName, currentTime);
					}
					return "OK";
				} else {
					return "INACTIVE";
				}
			}			
		} else if( split[0].equals("TELEPORT")) {
			
			return limboStore.processTeleport( command );
			
		} else if( split[0].equals("FORWARD")) {
			
			return limboStore.processForward( command );
			
		} else if( split[0].equals("MESSAGE")) {
		
			return communicationManager.chatManager.processMessage(command);
		} else if( split[0].equals("TELL")) {
		
			return communicationManager.chatManager.processMessage(command);
		}
		
		return "NOACTION";
		
	}
	
	void runBlindCommand( String command ) {
		
		final String commandStore = new String(command);
		
		Thread t = new Thread( new Runnable() {
			
			public void run() {
				
				processBlindCommand( commandStore );
				
			}
			
		}
		);
		
		t.start();
		
	}
	
	void illegalCommand( String command ) {
		MiscUtils.safeLogging(log, "[ServerPort] Illegal command sent from main thread:");
		MiscUtils.safeLogging(log, "[ServerPort] " + command);
		return;
	}
	
	void processBlindCommand( String command ) {
		
		String[] split = command.split(":");
		
		if( split.length != 2 ) {
			illegalCommand( command );
			return;
		}
		
		if( split[0].equals("INVITE")) {
						
			InviteCommand inviteCommand = new InviteCommand( communicationManager , split[1] );
			
			if( inviteCommand.isValid ) {
				Thread t = new Thread( inviteCommand );
				t.run();
			} else {
				illegalCommand( command );
				return;
			}
			
			return;
			
		}
		else if( split[0].equals("OPENGATE")) {
			OpenGateCommand openGateCommand = new OpenGateCommand( communicationManager , split[1] );
			
			if( openGateCommand.isValid ) {
				Thread t = new Thread( openGateCommand );
				t.run();
			} else {
				illegalCommand( command );
			}
			
		}
		
	}
	
	
}
