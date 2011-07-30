package com.raphfrk.bukkit.serverport;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortBukkit extends JavaPlugin {
	
	Server server;
	List<World> worlds;
	
	ServerPortListenerCommon serverPortListenerCommon = new ServerPortListenerCommon();
	
	ServerPort serverPort = new ServerPort();
	
	ServerPortCommon serverPortCommon = new ServerPortCommon();
	
	//MyPermissions permissions = new MyPermissions(this);
	
	protected final ServerPortPlayerListener playerListener = new ServerPortPlayerListener(this);
	
	private final ServerPortBlockListener blockListener = new ServerPortBlockListener(this);
	
	private final ServerPortCustomListener customListener = new ServerPortCustomListener(this);

	private final ServerPortEntityListener entityListener = new ServerPortEntityListener(this);

	private final ServerPortVehicleListener vehicleListener = new ServerPortVehicleListener(this);

	private final ServerPortWorldListener worldListener = new ServerPortWorldListener(this);
	
    public void onEnable() {
    	
    	server = getServer();
    	worlds = server.getWorlds();
    	
    	MyServer.setBukkitServer(getServer());
        MyServer.setJavaPlugin(this);
        
        MyServer.baseFolder = this.getDataFolder();
		
		serverPortCommon.init(serverPortListenerCommon);    
    	
        // TODO: Place any custom enable code here including the registration of any events
    	MyServer.setBukkitServer(server);
        // Register our events
        MyServer.setJavaPlugin(this);
    	registerHooks();
    	
    	//permissions.init();

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        MiscUtils.safeLogging( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void onDisable() {
    	MyPlayer.hashMaps = null;
		serverPortCommon.disable();
    }
	
	synchronized void registerHooks() {
		
		PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.CUSTOM_EVENT, customListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Priority.Normal, this);
        
        
       		
	}
	

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
		
		return serverPortListenerCommon.onCommand(commandSender, commandLabel, args);
		
	}
	
	
}