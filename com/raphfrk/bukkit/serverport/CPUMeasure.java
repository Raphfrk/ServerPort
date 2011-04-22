package com.raphfrk.bukkit.serverport;

import java.io.IOException;
import java.util.List;

//import net.hailxenu.serverautostop.AutoStopPlugin;

import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CPUMeasure implements Runnable {

	Object sync = new Object();
	int counter = 0;
	long oldTime = 0;
    
	public void run() {
		synchronized(sync) {
			long delay = ((ServerPortBukkit)MyServer.plugin).serverPortListenerCommon.communicationManager.restartDelay;
			long currentTime = System.currentTimeMillis()/1000;
			if(oldTime == 0) {
				oldTime = currentTime;
				counter = 0;
				return;
			}
			counter += 10;
			if(currentTime > oldTime + delay) {
				double ticksPerMinute = (counter * 60.0) / (currentTime - oldTime);
				oldTime = currentTime;
				counter = 0;
				if(ticksPerMinute < ((ServerPortBukkit)MyServer.plugin).serverPortListenerCommon.communicationManager.restartThreshold) {
					System.out.println("[ServerPort] Auto-restrart functionality disabled");
					/*AutoStopPlugin autoStop;
					autoStop = (AutoStopPlugin)MyServer.bukkitServer.getPluginManager().getPlugin("ServerAutoStop");
					if(autoStop != null) {
						MyServer.bukkitServer.getPluginManager().disablePlugins();
						Player[] players = MyServer.bukkitServer.getOnlinePlayers();
						for(Player player : players) {
							player.saveData();
							player.kickPlayer("Restarting server - please wait 2 mins before reconnecting");
						}
						List<World> worlds = (MyServer.bukkitServer).getWorlds();
						for(World world : worlds) {
							world.save();
						}
						MyServer.bukkitServer.broadcastMessage("[ServerPort] CPU overload detected, restarting server");
						try {
							Runtime.getRuntime().exec("java -jar AutoRestart.jar " + ((ServerPortBukkit)MyServer.plugin).serverPortListenerCommon.startCommand);
							System.exit(0);
						} catch (IOException e) {
							MyServer.bukkitServer.broadcastMessage("[ServerPort] Restart failed");
						}
						
					} else {
						MiscUtils.safeLogging("[ServerPort] Autostop plugin required to restart server");
					}*/
					
				}
				//System.out.println("ticks per minute: " + ticksPerMinute);
			}
		}
	}

	
}
