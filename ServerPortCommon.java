import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.World.Environment;


public class ServerPortCommon {
	
	
	public PortalManager portalManager = new PortalManager();
	public ParameterManager parameterManager = new ParameterManager();
	public CommunicationManager communicationManager = new CommunicationManager();
	
	static public String name = "ServerPort";
	static private String version = "";
	static String prefix = "sptwo";
	
	final static String propertiesFilename = "serverport.txt";
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	static final String slash = System.getProperty("file.separator");
	
	// should be called by hmod enable
	synchronized public void init( ServerPortListenerCommon serverPortListenerCommon ) {
		
		MiscUtils.safeLogging("Initialisation");
		
		VersionNumbering.name = ServerPortCommon.name;
		VersionNumbering.prefix = ServerPortCommon.prefix;
		
		VersionNumbering.updateUpdatr();
		
		File serverportDir;
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
		}
		
		parameterManager.setPropertiesFilename( propertiesFilename );
		parameterManager.setCommunicationManager(communicationManager);
		portalManager.setCommunicationManager(communicationManager);
		communicationManager.setPortalManager(portalManager);
		serverPortListenerCommon.setParameterManager(parameterManager);
		
		registerParameters();
		
		parameterManager.loadParameters();

		loadWorlds(portalManager);
		
		portalManager.init();
		communicationManager.init();
		
		serverPortListenerCommon.setPortalManager( portalManager );
		serverPortListenerCommon.setCommunicationManager(communicationManager);
		
	
	}

	synchronized public void disable() {
		
		if( communicationManager != null ) {
			communicationManager.stopServer();
		}
		
	}
	
	// called by hmod initialize
	synchronized public void enable() {
		
		portalManager.refreshAtiveStates();
		
		MiscUtils.safeLogging( log , name + " " + VersionNumbering.version + " initialized");

		
		
	}
	
	synchronized void registerParameters() {
		
		portalManager.registerParameters( parameterManager );
		communicationManager.registerParameters( parameterManager );
		
	}
	
	static void loadWorlds(PortalManager portalManager) {
		
		Iterator<String> itr = portalManager.worldList.getValues().keySet().iterator();

		while(itr.hasNext()) {
			String world = itr.next();
			log.info("[ServerPort] loading: " + world);
			String[] params = world.split(";");
			if(params.length==1) {
				MyServer.bukkitServer.createWorld(world, Environment.NORMAL);
				continue;
			} else if(params.length==2) {
				if(params[1].equalsIgnoreCase("nether")) {
					MyServer.bukkitServer.createWorld(params[0], Environment.NETHER);
					continue;
				} else if(params[1].equalsIgnoreCase("normal")) {
					MyServer.bukkitServer.createWorld(params[0], Environment.NETHER);
					continue;
				} 
			}
			log.info("[ServerPort] Unable to load: " + world);
		}
		
	}
	

}
