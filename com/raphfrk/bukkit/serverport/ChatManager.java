package com.raphfrk.bukkit.serverport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.entity.Player;


public class ChatManager {

	MyServer server = MyServer.getServer();

	public StringList targetServers = new StringList();
	public StringList allowedServers = new StringList();
	Boolean allowAll = true;

	ParameterManager parameterManager = null;

	CommunicationManager communicationManager = null;

	public Long chatTimeout = 30000L;

	void setCommunicationManager ( CommunicationManager communicationManager ) {

		this.communicationManager = communicationManager;

	}

	void RegisterParameters( ParameterManager parameterManager ) {

		this.parameterManager = parameterManager;
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"targetServers",
				"chatsend",
				StringList.class,
				new String( "all" ),
				new String[] {
					"This lists the servers to send chat to.  It should use the names that are specificed by setname.  If set to all, it will send to all servers that have been invited.",
					"/serverport chatsend <name> will remove that name from the list if it already there and add it if it isn't",
					"If all is in the list, then that overrides any other names in the list"
				},
				"sets servers to send chat to"
		)
		);

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"allowedServers",
				"chatreceive",
				StringList.class,
				new String( "all" ),
				new String[] {
					"This lists the servers to receive chat from.  It should use the names that are specificed by setname.  If set to all, it will accept chat from any server.",
					"/serverport chatreceive <name> will remove that name from the list if it already there and add it if it isn't",
					"If all is in the list, then that overrides any other names in the list"
				},
				"sets servers with permission to receive chat"
		)
		);

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"chatTimeout",
				"chattimeout",
				Long.class,
				new Long( 30000 ),
				new String[] {
					"This sets how often the server should wait until re-trying to send chat.  " +
					"If it can't open the target server's port, then it will remove the target server from the send to list for a length of time set by this parameter.  " + 
					"The default is 30000 which is 30s (30000ms)."
				},
				"Sets how long to wait before rechecking servers for chat."
		)
		);

	}



	HashMap<String,Long> onlineTargetServers = new HashMap<String,Long>();

	HashSet<String> hashSetCache = null;
	long lastUpdate = -1;

	void sendChat( MyPlayer player , String message ) {

		sendChat(null, player, message);
	}


	void sendChat( String targetPlayer, MyPlayer player , String message ) {
	
		String colour = player.getColor();
		
		String fullMessage;
		if(targetPlayer == null) {
			fullMessage = "<" + colour + player.getName() + server.getColor("White") + "> " + message;
		} else {
			fullMessage = colour + player.getName() + server.getColor("White") + " sends "+ message;
		}
		
		List<Player> targetPlayerPlayer;

		if(targetPlayer != null && (targetPlayerPlayer = MyServer.bukkitServer.matchPlayer(targetPlayer)) != null) {
			if(targetPlayerPlayer.size() > 0) {
				String reply = "Message sent to ";

				for(Player p : targetPlayerPlayer) {
					p.sendMessage(fullMessage);
					reply = reply + p.getName() + " ";
				}
				MiscUtils.safeMessage(player.getName(), reply);
				return;
			}
		}

		fullMessage = ChatCommand.encodeString(fullMessage);

		Set<String> getServerNameList = null;

		long currentTime = System.currentTimeMillis();

		if( targetServers.getValues().containsKey("all")) {

			if( currentTime > lastUpdate + 1000 ) {
				getServerNameList = communicationManager.peerServerDatabase.getServerNameList();
				hashSetCache = new HashSet<String>( getServerNameList );
				lastUpdate = currentTime;
			} else {
				getServerNameList = hashSetCache;
			}
		} else {
			getServerNameList = targetServers.getValues().keySet();
		}

		HashMap<String,Long> onlineTargetServersSynced;

		synchronized( onlineTargetServers ) {
			onlineTargetServersSynced = new HashMap<String,Long>(onlineTargetServers);
		}

		Iterator<String> itr = getServerNameList.iterator();

		while( itr.hasNext() ) {
			String current = itr.next();
			if( !onlineTargetServersSynced.containsKey(current) || onlineTargetServersSynced.get(current) + chatTimeout < currentTime ) {
				ChatCommand chatCommand = new ChatCommand(communicationManager, targetPlayer, player.getName() , current , fullMessage , onlineTargetServers );
				Thread t = new Thread( chatCommand );
				t.start();
			}
		}

	}

	String processMessage( String command ) {

		String[] split = command.split(":");

		if(split.length != 2 ) {
			return "wrong number of parts in processMessage";
		}

		boolean tell = split[0].equals("TELL");

		String[] split2 = split[1].split(";");

		String peerServerName = split2[1];

		TreeMap<String,Boolean> allowedServersTree = allowedServers.getValues();

		if( allowedServersTree.containsKey("all") || allowedServersTree.containsKey(peerServerName)) {

			String prefix = communicationManager.showServerName ? "[" + peerServerName + "] " :"";

			if( !server.isPlayerListEmpty() ) {
				if(tell) {
					String[] split3 = split2[0].split(",");
					if(split3.length == 2) {
						String reply = "Message sent to ";
						final String namePortion = split3[0];
						//Future<String[]> playerNames = MyServer.bukkitServer.getScheduler().callSyncMethod(MyServer.plugin, new Callable<String[]>() {
						//	public String[] call() {

								List<Player> players = MyServer.bukkitServer.matchPlayer(namePortion);

								String[] names = new String[players.size()];

								int cnt = 0;
								for(Player p : players) {
									names[cnt++] = p.getName();
								}
						//		return names;
						//	}
						//});

						//String[] names = null;
						//try {
						//	System.out.println("About to get Callable");
						//	names = playerNames.get();
						//} catch (InterruptedException e) {
						//	return "OK";
						//} catch (ExecutionException e) {
						//	return "OK";
						//}

						if(names.length > 0) {
							for(String name : names ) {
								MiscUtils.safeMessage(name, ChatCommand.decodeString(split3[1]));
								reply = reply + name + " ";
							}
							return reply;
						} else {
							return "UNKNOWN";
						}
					}

				} else {
					server.messageAll( prefix + ChatCommand.decodeString(split2[0]));
				}
			}

			return "OK";

		} else {
			return "chat from server has been set to ignore";
		}

	}


}
