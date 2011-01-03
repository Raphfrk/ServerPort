import java.io.File;
import java.util.logging.Logger;

public class ServerPort extends Plugin {
	
	private ServerPortListener serverPortListener = new ServerPortListener(this);
	
	public PortalManager portalManager = new PortalManager();
	public ParameterManager parameterManager = new ParameterManager();
	public CommunicationManager communicationManager = new CommunicationManager();
	
	static public String name = "ServerPort";
	static private String version = "";
	static String prefix = "sptwo";
	
	final static String propertiesFilename = "serverport.txt";
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	static final String slash = System.getProperty("file.separator");
	
	public void enable() {
		
		MiscUtils.safeLogging("Enabling");
		
		VersionNumbering.name = ServerPort.name;
		VersionNumbering.prefix = ServerPort.prefix;
		
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
		
		registerParameters();
		
		parameterManager.loadParameters();
		
		portalManager.init();
		communicationManager.init();
		
		serverPortListener.setPortalManager( portalManager );
		serverPortListener.setCommunicationManager(communicationManager);
	
	}

	public void disable() {
		
		if( communicationManager != null ) {
			communicationManager.stopServer();
		}
		
	}
	
	public void initialize() {
		
		registerHooks();
		
		portalManager.refreshAtiveStates();
		
		MiscUtils.safeLogging( log , name + " " + VersionNumbering.version + " initialized");

		
		
	}
	
	void registerParameters() {
		
		portalManager.registerParameters( parameterManager );
		serverPortListener.registerParameters( parameterManager );
		communicationManager.registerParameters( parameterManager );
		
	}

	void registerHooks() {
		etc.getLoader().addListener( PluginLoader.Hook.BLOCK_PLACE, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.SIGN_CHANGE, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.BLOCK_DESTROYED, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.BLOCK_PHYSICS, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.FLOW, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.COMMAND, serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.PLAYER_MOVE,serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.LOGIN,serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.CHAT,serverPortListener , this, PluginListener.Priority.MEDIUM);
		//etc.getLoader().addListener( PluginLoader.Hook.VEHICLE_POSITIONCHANGE,serverPortListener , this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener( PluginLoader.Hook.HEALTH_CHANGE,serverPortListener , this, PluginListener.Priority.MEDIUM);
		
	}
	

}
