package com.raphfrk.bukkit.serverport;

public class SyncRunnable implements Runnable {

	Object sync=null;
	StringBuffer ret=null;
	String input=null;
	CommandFIFO commandFIFO=null;
	
	SyncRunnable( Object sync , StringBuffer ret , String input , CommandFIFO commandFIFO ) {
		this.sync = sync;
		this.ret = ret;
		this.input = input;
		this.commandFIFO = commandFIFO;
	}
	
	public void run() {

		ret.append(commandFIFO.syncedRunCommand( input ));

		synchronized( sync ) {
			sync.notifyAll();
		}
	}
	
}
