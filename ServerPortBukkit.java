import java.io.File;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortBukkit extends JavaPlugin {
	
	final Server server = getServer();
	final List worlds = server.getWorlds();
	
	ServerPortListenerCommon serverPortListenerCommon = new ServerPortListenerCommon();
	
	ServerPort serverPort = new ServerPort();
	
	ServerPortCommon serverPortCommon = new ServerPortCommon();
	
	private final ServerPortPlayerListener playerListener = new ServerPortPlayerListener(this);
	
	private final ServerPortBlockListener blockListener = new ServerPortBlockListener(this);
	
	private final ServerPortCustomListener customListener = new ServerPortCustomListener(this);

	private final ServerPortEntityListener entityListener = new ServerPortEntityListener(this);
	
	private final ServerPortWorldListener worldListener = new ServerPortWorldListener(this);
	
    public ServerPortBukkit(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        
        MyServer.setBukkitServer(server);
		
		serverPortCommon.init(serverPortListenerCommon);    
		
    }

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
        MyServer.setJavaPlugin(this);
    	registerHooks();

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        MiscUtils.safeLogging( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void onDisable() {
		serverPortCommon.disable();

    }
	
	synchronized void registerHooks() {
		
		PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.CUSTOM_EVENT, customListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.CHUNK_UNLOADED, worldListener, Priority.Normal, this);
        
        
       		
	}
	
	
}