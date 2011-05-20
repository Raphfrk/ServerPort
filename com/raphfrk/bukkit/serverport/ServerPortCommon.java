package com.raphfrk.bukkit.serverport;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.World.Environment;


public class ServerPortCommon {
	
	public PortalManager portalManager = new PortalManager();
	public ParameterManager parameterManager = new ParameterManager();
	public CommunicationManager communicationManager = new CommunicationManager();
	public WorldManager worldManager = new WorldManager();
	
	static public String name = "ServerPort";
	static private String version = "";
	static String prefix = "sptwo";
	
	final static String propertiesFilename = "serverport.txt";
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	static final String slash = System.getProperty("file.separator");
	
	// should be called by hmod enable
	synchronized public void init( ServerPortListenerCommon serverPortListenerCommon ) {
		
		MiscUtils.safeLogging("Initialisation");
		
		/*File serverportDir;
		if( !(serverportDir = new File( "serverport" )).exists() ) {
			serverportDir.mkdir();
		}
		
		File gatesDir;
		if( !(gatesDir = new File( "serverport" + slash + "gates")).exists() ) {
			gatesDir.mkdir();
		}
		
		File customDir;
		if( !(customDir = new File( "serverport" + slash + "custom")).exists() ) {
			customDir.mkdir();
		}*/
		
		parameterManager.setPropertiesFilename( propertiesFilename );
		parameterManager.setCommunicationManager(communicationManager);
		portalManager.setCommunicationManager(communicationManager);
		communicationManager.setPortalManager(portalManager);
		serverPortListenerCommon.setParameterManager(parameterManager);
		serverPortListenerCommon.setWorldManager(worldManager);
		
		registerParameters();
		
		parameterManager.loadParameters();

		worldManager.loadWorlds(portalManager);
		
		portalManager.init();
		communicationManager.init();
		
		serverPortListenerCommon.setPortalManager( portalManager );
		serverPortListenerCommon.setCommunicationManager(communicationManager);
		
		autoPermissionReload(300);
		
	}

	void autoPermissionReload(long delay) {
		final CommunicationManager finalCM = communicationManager;

		MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {
			
			public void run() {
				int newPeriod = finalCM.autoPermissionReload;
				int period = 1;
				if(newPeriod < 0) {
					period = 300;
				} else if(newPeriod < 5) {
					period = 100;
				} else {
					period = newPeriod * 20;
				}
				
				if(newPeriod > 0) {
					MiscUtils.safeLogging("[ServerPort] Auto-reloading permissions");
					MyPlayer.hashMaps = new HashMap<String,HashMap>();
				}
				
				autoPermissionReload(period);
				
			}
			
		}, delay);
	}
	
	synchronized public void disable() {
		
		if( communicationManager != null ) {
			communicationManager.stopServer();
		}
		
	}
	
	// called by hmod initialize
	synchronized public void enable() {
		
		portalManager.refreshAtiveStates();
		
		MiscUtils.safeLogging( log , name + " " + version + " initialized");

		
		
	}
	
	synchronized void registerParameters() {
		
		portalManager.registerParameters( parameterManager );
		communicationManager.registerParameters( parameterManager );
		
	}
	

}
