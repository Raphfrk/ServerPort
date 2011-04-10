package com.raphfrk.bukkit.serverport;

import net.hailxenu.serverautostop.AutoStopPlugin;

import org.bukkit.command.ConsoleCommandSender;

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
					AutoStopPlugin autoStop;
					autoStop = (AutoStopPlugin)MyServer.bukkitServer.getPluginManager().getPlugin("ServerAutoStop");
					if(autoStop != null) {
						MyServer.bukkitServer.broadcastMessage("[ServerPort] CPU overload detected, restarting server");
						MyServer.bukkitServer.dispatchCommand(new ConsoleCommandSender(MyServer.bukkitServer), "restart");
					} else {
						MiscUtils.safeLogging("[ServerPort] Autostop plugin required to restart server");
					}
				}
				System.out.println("ticks per minute: " + ticksPerMinute);
			}
		}
	}

	
}
