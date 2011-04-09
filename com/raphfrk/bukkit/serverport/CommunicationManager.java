package com.raphfrk.bukkit.serverport;

public class CommunicationManager {

	ServerPortServer serverPortServer = new ServerPortServer();
	
	PeerServerDatabase peerServerDatabase = new PeerServerDatabase();
	
	ChatManager chatManager = new ChatManager();
	
	PortalManager portalManager = null;
	
	LimboStore limboStore = new LimboStore();
	
	public CommandFIFO commandFIFO = new CommandFIFO();
	
	public int clientTimeout = 10000;
	
	public Boolean showServerName = true;
	
	public Integer defaultTimeToLive = 10;
	
	public Integer autoPermissionReload = -1;
	
	ParameterManager parameterManager = null;
	
	void setPortalManager(PortalManager portalManager) {
		this.portalManager = portalManager;
	}
		
	void registerParameters( ParameterManager parameterManager ) {
		
		this.parameterManager = parameterManager;
		
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"autoPermissionReload",
				"autopermission",
				Integer.class,
				new Integer(-1),
				new String[] {
					"This sets the delay between auto-reloads of the permission file, in seconds.  Negative numbers mean not to reload."
				},
				"period of permission file reloads"
		)
		);
		
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"defaultTimeToLive",
				"timetolive",
				Integer.class,
				new Integer(10),
				new String[] {
					"This sets how many hops a player who logs onto the wrong server should be forwarded before giving up"
				},
				"time to live to server forwarding"
		)
		);
		
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"clientTimeout",
				"clienttimeout",
				Integer.class,
				new Integer(10000),
				new String[] {
					"This sets the timeout in ms for the client",
					"It defaults to 10 seconds (10000)"
				},
				"timeout for serverport client"
		)
		);
		
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"showServerName",
				"showserver",
				Boolean.class,
				new Boolean(true),
				new String[] {
					"If this parameter is set to true, the server name is show for inter server comms"
				},
				"show server name in the chat link"
		)
		);
		
		serverPortServer.registerParameters( parameterManager );
		limboStore.registerParameters(parameterManager);
		chatManager.RegisterParameters(parameterManager);

		
	}
	
	void init() {
		
		peerServerDatabase.init();
		
		serverPortServer.setPeerServerDatabase(peerServerDatabase);
		serverPortServer.setCommunicationManager(this);
		serverPortServer.init();
		
		commandFIFO.setCommunicationManager(this);
		commandFIFO.setPortalManager(portalManager);
		commandFIFO.setLimboStore(limboStore);
		
		limboStore.setCommunicationManager(this);
		limboStore.init();
		
		chatManager.setCommunicationManager(this);
		
	}
	
	void stopServer() {
		serverPortServer.stopServer();
	}
	
	void attemptInvite( String playerName , String hostname , int portnum ) {
		
		commandFIFO.runBlindCommand( "INVITE:" + playerName + "," + hostname + "," + portnum );
		
	}
	
}
