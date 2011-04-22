package com.raphfrk.bukkit.serverport;
import org.bukkit.event.Event;

public class RunnableEvent extends Event {
	
	Runnable runnable;
	
    public RunnableEvent(final Runnable runnable) {
        super("ServerPortRunnableEvent");
        this.runnable = runnable;
    }

}
