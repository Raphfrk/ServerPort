package com.raphfrk.bukkit.serverport;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class ServerPortListenerCommon {

	static MyServer server = MyServer.getServer();

	public String commandName = "/serverport";

	final int SIGN = 68;
	final int BUTTON = 77;
	final int MIST = 90;

	WorldManager worldManager = null;
	PortalManager portalManager = null;
	ParameterManager parameterManager = null;
	CommunicationManager communicationManager = null;

	synchronized public void setParameterManager( ParameterManager parameterManager ) {

		this.parameterManager = parameterManager;

	}

	synchronized public void setPortalManager( PortalManager portalManager ) {
		this.portalManager = portalManager;
	}

	synchronized public void setCommunicationManager( CommunicationManager communicationManager ) {
		this.communicationManager = communicationManager;
	}

	synchronized public void setWorldManager( WorldManager worldManager ) {
		this.worldManager = worldManager;
	}

	synchronized public boolean onBlockPlace(MyPlayer player, MyBlock blockPlaced, MyBlock blockClicked, MyItem itemInHand) {

		if( portalManager.testSignBlock( blockPlaced ) || portalManager.testProtectedBlock( blockPlaced ) ) {

			String gateType = portalManager.getPortalType( blockPlaced );

			if( gateType == null ) {
				gateType = "";
			}

			PortalInfo portal = portalManager.getPortalByBlock(blockPlaced);

			if( !player.permissionCheck("create_gate_type", new String[] {gateType, player.getWorld().getName(), portalManager.getDestination(portal)}) ) {
				return true;
			} 

			int blockID = blockPlaced.getType();
			if( blockID != SIGN && blockID != BUTTON ) {
				//player.sendMessage("block not sign and not button" + SIGN + " " + BUTTON + " block id " + blockID);
				return true;
			}

		}

		return false;
	}


	void restoreInventory(MyPlayer player) {
		String limboInventory = communicationManager.limboStore.removePlayerStore(player);

		String blockedItems = LimboStore.addToPlayerInv( "STORE:" + player.getName() + ";" + limboInventory);

		communicationManager.limboStore.processTransfer("STORE:playername=" + player.getName() + ";" + blockedItems);
	}

	synchronized public boolean onLogin(MyPlayer player) {

		boolean displayMessage = true;

		String playerName = new String( player.getName());

		communicationManager.limboStore.unLockPlayer(playerName);

		if( communicationManager.limboStore.updateInvOnLogin ) {
			restoreInventory(player);
		}

		LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());

		if( limboInfo == null ) {
			return displayMessage;
		}

		String currentServer = limboInfo.getCurrentServer();

		if( currentServer.equals("")) {

			if( !communicationManager.limboStore.newPlayersAllowed() ) {

				MiscUtils.safeMessage(playerName, "[ServerPort] New players are not allowed to connect to this server directly.  If you move you will be teleported");

			}


		} else if( !currentServer.equals("here") ) {
			displayMessage = true;
			MiscUtils.safeMessage(playerName, "[ServerPort] You have logged into the wrong server.  If you move you will be teleported");

			if( limboInfo.getTimeToLive() == 0 ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Unable to find correct server, max hops limit reached");
				limboInfo.setCurrentServer("here");
				currentServer = limboInfo.getCurrentServer();
				limboInfo.setCurrentGate( "" );
				communicationManager.limboStore.updateDatabase(limboInfo);
			} else if( player.permissionCheck("cancel_redirect", new String[] {"allow"}) ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] You can use /cancelredirect to cancel this effect.  Any items are still on the server you left them.");
			}

			//communicationManager.limboStore.lockPlayer(playerName);
			return displayMessage;
		}

		String currentGate = limboInfo.getCurrentGate();

		if( limboInfo.getTimeToLive() != communicationManager.defaultTimeToLive ) {
			limboInfo.setTimeToLive( communicationManager.defaultTimeToLive );
			communicationManager.limboStore.updateDatabase(limboInfo);
		}


		if( !currentGate.equals("") ) {

			displayMessage = false;
			int playerHealth = limboInfo.getPlayerHealth();

			PortalInfo portalInfo = portalManager.getPortal(currentGate);

			if( portalInfo != null ) {

				if( portalInfo.bindStone ) {
					playerHealth = 20;
				}

				IntLocation exitPoint = portalInfo.getExitPoint();


				MyLocation loc = exitPoint.toLocation();

				loc.setRotX( (float)limboInfo.getRotX() + 180);
				loc.setRotY( (float)limboInfo.getRotY() );

				player.teleportTo(loc);
				player.setHealth(playerHealth);



			} else {
				MiscUtils.safeLogging(player.getName() + " is set to appear at an unknown gate (" + currentGate + ")");
			}

			limboInfo.setCurrentGate("");
			communicationManager.limboStore.updateDatabase(limboInfo);


		}

		return displayMessage;

	}

	//HashMap<String,Long> spamShield = new HashMap<String,Long>();



	/*	synchronized public boolean onHealthChange(MyPlayer player, int oldValue, int newValue) {

		if( communicationManager.limboStore.bindEnable && newValue <= 0 ) {

			Long lastCheck;
			long currentTime = System.currentTimeMillis();
			String playerName = player.getName();

			boolean notSpam = (lastCheck = spamShield.get(playerName)) == null || lastCheck < currentTime - 1000;

			if( notSpam ) {
				MiscUtils.safeLogging(player.getName() + " has died, attempting to teleport to bind");
			}


			LimboStore.dropItems(player);

			if( TeleportCommand.teleportToBind(communicationManager, player) ) {
				player.sendMessage("You have died, restoring your position to bind");
				player.setHealth(20);
				return true;
			} else {

				if( notSpam ) {
					player.sendMessage("You have died with no bind stone set");
				}

				spamShield.put(playerName, currentTime);

				return false;

			}
		}


		return false;
	}*/


	synchronized public void onPlayerMove(MyPlayer player, MyLocation from, MyLocation to) {

		LimboStore limboStore = communicationManager.limboStore;

		LimboInfo limboInfo = limboStore.getLimboInfo(player.getName());

		String currentServer = limboInfo.getCurrentServer();

		if( currentServer.equals("")) {

			if( limboStore.newPlayerForward.equals("allow") ) {

				limboInfo.setCurrentServer( "here" );
				limboStore.updateDatabase(limboInfo);

				communicationManager.limboStore.unLockPlayer(player.getName());

			} else {

				ReconnectCommand reconnectCommand = new ReconnectCommand( communicationManager , player.getName() , player.getIP() , limboStore.newPlayerForward );
				Thread t = new Thread( reconnectCommand );
				t.start();
			}

		} else if( !currentServer.equals("here")) {

			String playerName = new String( player.getName() );

			int timeToLive = limboInfo.getTimeToLive();

			if( !limboInfo.getLocked()) {

				limboInfo.setLocked(true);
				communicationManager.limboStore.updateDatabase(limboInfo);

				ForwardCommand forwardCommand = new ForwardCommand( communicationManager , playerName , timeToLive , player.getIP());

				Thread t = new Thread( forwardCommand );
				t.start();
				return;
			}

		}

		if( communicationManager.limboStore.isLocked( player.getName() )) {
			MyLocation newFrom = new MyLocation(player.getWorld(), from.getX(),from.getY(),from.getZ(),from.getRotX(),from.getRotY());
			newFrom.setX( newFrom.getX() + 0.5);
			newFrom.setZ( newFrom.getZ() + 0.5);
			player.teleportTo(newFrom);
		} else if( portalManager.testPortalBlock(to) && !portalManager.testPortalBlock(from) ) {

			IntLocation intLoc = new IntLocation( to );

			String gateType = portalManager.getPortalType( intLoc );

			if( gateType == null ) {
				gateType = "";
			}

			PortalInfo portalInfo = portalManager.getPortalByBlock(intLoc);

			if( player.permissionCheck("use_gate_type", new String[] {portalInfo.portalType, to.getWorld().getName(), portalManager.getDestination(portalInfo) } ) ) {
				portalManager.enteredPortal(player, to);
			}
		}

	}

	synchronized public boolean onBlockDestroy(MyPlayer player, MyBlock block) {

		String gateType = portalManager.getPortalType( block );

		if( gateType == null ) {
			gateType = "";
		}

		if( portalManager.testProtectedBlock( block ) ) {

			PortalInfo origin = portalManager.getPortalByBlock(block);
			String destination = portalManager.getDestination(origin);

			if( !player.permissionCheck("destroy_gate", new String[] {origin.portalType, origin.portalWorld, destination} ) ) {
				return true; 
			} else {
				if( block.getStatus() == ServerPortBlockListener.BLOCK_BROKEN ) {

					if( portalManager.destroyPortal( block ) ) {
						MiscUtils.safeMessage(player, "Gate Destroyed");
						return false;
					} else {
						return true;
					}

				}
			}
		}

		if( portalManager.testSignBlock( block ) ) {
			
			player.sendMessage("Sign block = true");

			if( block.getType() == SIGN ) {
				
				MySign sign = (MySign)server.getComplexBlock(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getStatus());

				if( !sign.isNull() ) {
					portalManager.refreshSign( player , sign );
				}

			}

			boolean signPunched = block.getType() == SIGN && player.holding() <= 0;
			
			PortalInfo portalInfo = portalManager.getPortalByBlock(block);

			if( ( player.permissionCheck("use_gate_type", new String[] {portalInfo.portalType, portalInfo.portalWorld, portalManager.getDestination(portalInfo)})) && block.getStatus() == ServerPortBlockListener.START_DIGGING && ( block.getType() == BUTTON || signPunched ) ) {

				player.sendMessage("Pushing button");
				
				portalManager.buttonPress( block , player );

				return false;

			}

		}

		return false;
	}


	synchronized public boolean onChat(MyPlayer player, String message) {

		communicationManager.chatManager.sendChat(player, message);

		return false;
	}


	synchronized public boolean onCommand(CommandSender sender, String commandLabel, String[] split) {

		MyPlayer player = null;

		if(sender instanceof Player) {
			player = new MyPlayer((Player)sender);
		}


		if( commandLabel.equals("worldlist")) {
			List<World> worlds = MyServer.bukkitServer.getWorlds();
			for(World world : worlds) {
				sender.sendMessage("World name:" + world.getName());
			}
			return true;
		}

		if( sender instanceof Player && player.isAdmin() && commandLabel.equals("refresh") && split.length > 0) {

			int distance = Integer.parseInt(split[0]);

			Location loc = player.getLocation().getBukkitLocation();

			player.sendMessage("Attempting refresh" );
			player.getWorld().refreshChunk(loc.getBlockX()>>4, loc.getBlockY()>>4);		
			return true;

		}
		
		if( sender instanceof Player && commandLabel.equals("stell") && split.length > 1) {
			String targetPlayerName = split[0];
			String message = "";
			for(int cnt = 1; cnt < split.length;cnt++) {
				message = message + split[cnt] + " ";
			}
			communicationManager.chatManager.sendChat(targetPlayerName, player, message);
			return true;
		}


		if( sender instanceof Player && player.isAdmin() && commandLabel.equals("itemgen") && split.length > 0) {

			int typeId = 1;
			int amount = 1;

			if( split.length > 0 ) {
				try {
					typeId = Integer.parseInt(split[0]);
				} catch (NumberFormatException nfe) {
					sender.sendMessage("Unable to parse type id");
					return true;
				}
			}
			if( split.length > 1 ) {
				try {
					amount = Integer.parseInt(split[1]);
				} catch (NumberFormatException nfe) {
					sender.sendMessage("Unable to parse amount, using 1");
				}
			}

			((Player)sender).getInventory().addItem(new ItemStack(typeId, amount));

			return true;

		}

		if( commandLabel.equals("pos") && player != null && player.isAdmin() ) {
			player.sendMessage("Pos: " + player.getLocation().getBukkitLocation());
			if( split.length == 3 ) {
				int x = Integer.parseInt(split[0]);
				int y = Integer.parseInt(split[1]);
				int z = Integer.parseInt(split[2]);

				MiscUtils.gridLoad(player.getWorld(), x, y, z);
				player.teleportTo(new MyLocation(player.getWorld(), x,y,z));
				player.sendMessage("New pos: " + player.getLocation().getBukkitLocation());
			} else if ( split.length > 3 ) {
				int x = Integer.parseInt(split[0]);
				int y = Integer.parseInt(split[1]);
				int z = Integer.parseInt(split[2]);
				int index = Integer.parseInt(split[3]);
				World world = MyServer.bukkitServer.getWorlds().get(index);

				MiscUtils.gridLoad(world, x, y, z);
				player.teleportTo(new MyLocation(world, x,y,z));
				player.sendMessage("Teleporting to world: " + world.getName() );
				player.sendMessage("New pos: " + player.getLocation().getBukkitLocation());
			}
			return true;
		}

		if( commandLabel.equals("release") && player != null && player.permissionCheck("release", new String[] {"allow"}) && player.getHealth() > 0 ) {

			MiscUtils.safeLogging("[ServerPort] " + player.getName() + " is releasing");

			player.setHealth(-1);
			return true;

		}

		if( commandLabel.equals("usegate") && player != null && player.permissionCheck("opteleport", new String[] {"allow"}) ) {

			if( split.length > 0 ) {	
				PortalInfo portalInfo = portalManager.getPortal(split[0]);
				if(portalInfo == null) {
					player.sendMessage("[ServerPort] Unable to find portal " + split[0] + " on current server");
				} else {
					TeleportCommand.teleport(communicationManager, player, portalInfo, false);
				}
			} else {
				player.sendMessage("[ServerPort] Command format is: /stele <servername> <gatename>");
			}

			return true;


		}

		if( player != null && commandLabel.equals("getinv") ) {
			restoreInventory(player);
			return true;
		}

		if( ((player != null && player.isAdmin()) || sender.isOp()) && commandLabel.equals("serverport") && split.length>0 && split[0].equalsIgnoreCase("loadworlds")) {
			worldManager.loadWorlds(portalManager);
			return true;
		}

		if( commandLabel.equals("drawgate") && split.length == 1 && player != null && player.permissionCheck("draw_gate", new String[] {split[0]})) {

			if(!player.getInventory().bukkitInventory.contains(323)) {
				server.dropItem( player.getLocation(), 323, 1 );
			}
			if(!player.getInventory().bukkitInventory.contains(259)) {
				server.dropItem( player.getLocation(), 259, 1 );
			}


			if( portalManager.drawGate(player, split[0])) {
				MiscUtils.safeMessage(player.getName(), "[ServerPort] Gate Auto-generation success");
				return true;
			} else {
				MiscUtils.safeMessage(player.getName(), "[ServerPort] Gate Auto-generation failed");
				return true;
			}

		}

		if( split.length > 0 && commandLabel.equals("serverport") && split[0].equals("permissions") && ((player != null && player.isAdmin()) || sender.isOp()) ) {
			sender.sendMessage("Permissions reloaded");
			MyPlayer.hashMaps = null;
			return true;
		}

		if( commandLabel.equals("cancelredirect") && player != null && player.permissionCheck("cancel_redirect", new String[] {"allow"})  ) {

			LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());

			if( limboInfo == null ) {
				MiscUtils.safeMessage(player, "[ServerPort] Unable to open limbo database record");
			} else {
				limboInfo.setCurrentServer("here");
				limboInfo.setCurrentGate("");
				communicationManager.limboStore.updateDatabase(limboInfo);
				MiscUtils.safeMessage(player, "[ServerPort] Current server set to: here");				
			}
			return true;

		}

		if( commandLabel.equals("regengates") && player != null && player.permissionCheck("regen_gates", new String[] {"allow"}) ) {

			int d = 32;

			if( split.length > 1 && MiscUtils.isInt(split[1])) {
				d = MiscUtils.getInt(split[1]);
			}

			MiscUtils.safeLogging("[ServerPort] " + player.getName() + " is regenerating gates (d=" + d + ")");

			portalManager.regenGates( player , d );
			return true;

		}

		/*if( split[0].equals("/stopcircle" ) && ( player.canUseCommand("/circleload") || player.canUseCommand("/stopcircle") || player.isAdmin() )) {

			MiscUtils.stopCircle(player.getName());
			return true;

		}


		if( split[0].equals("/circleload" ) && ( player.canUseCommand("/circleload") || player.isAdmin() )) {

			IntLocation loc = new IntLocation( player.getLocation() );

			int r = 100;

			if( split.length>1 && MiscUtils.isInt(split[1])) {

				r = MiscUtils.getInt(split[1]);

			}

			MiscUtils.safeMessage(player, "[ServerPort] Loading circle (" + loc + ") with radius " + r);

			MiscUtils.loadCircle(portalManager.worldName, player.getName(), loc.x, loc.y, loc.z, r);

			return true;


		}
		 */


		if( !commandLabel.equals(commandName.substring(1)) ) {
			return false;
		}

		if( !player.permissionCheck("serverport", new String[] {"allow"}) && (player == null || (!player.isAdmin())) ) {
			return false;
		}

		if( parameterManager.processCommand( player , split ) ) {
			return true;
		}

		return false;

	}


	synchronized public boolean onSignChange(MyPlayer player, MySign sign) {

		portalManager.signPlaced( player, sign );

		return false;
	}


	/*	HashMap<Integer,IntLocation> vehicleStore = new HashMap<Integer,IntLocation>();


    public void onVehiclePositionChange(BaseVehicle vehicle, int x, int y, int z) {

    	if( vehicle == null ) return;

    	int vehicleID = vehicle.getId();

    	IntLocation to = new IntLocation( x , y , z );

    	if( vehicleStore.containsKey(vehicleID)) {

    		IntLocation from = vehicleStore.get(vehicleID);
    		IntLocation toPlus1 = new IntLocation( to );
    		toPlus1.y++;
    		IntLocation toMinus1 = new IntLocation( to );
    		toMinus1.y--;



    		if( !(from.equals(to) || from.equals(toPlus1) || from.equals(toMinus1))) {

    			onVehiclePositionChange( vehicle , from , to );

    		}
    	} 

    	vehicleStore.put(vehicleID, to);


    }

    synchronized public void onVehiclePositionChange( BaseVehicle vehicle, IntLocation from , IntLocation to ) {

   	if( portalManager.testPortalBlock(to)) {

    		Player player = vehicle.getPassenger();

    		String gateType = portalManager.getPortalType( to );

    		if( gateType == null ) {
    			gateType = "";
    		}

    		if( player == null || player.canUseCommand("/serverportuse") || player.canUseCommand("/serverportuse" + gateType ) || player.isAdmin() ) {
    			portalManager.enteredPortal(vehicle, to);
    		}
    	}

    }

	 */


	// Preventing portal "mist" from updating


	synchronized public boolean onBlockPhysics(MyBlock block, boolean placed) {

		if( block.getType() == MIST ) {
			if( portalManager.testPortalBlock(block, false) ) {
				return true;
			}
		}

		return false;
	}


	synchronized public boolean onFlow(MyBlock blockFrom, MyBlock blockTo) {

		if( portalManager.testPortalBlock(blockFrom, false) || portalManager.testPortalBlock(blockTo, false)) {
			return true;
		}

		return false;
	}

}
