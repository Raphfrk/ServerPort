import java.util.ArrayList;
import java.util.HashMap;


public class ServerPortListenerCommon {

	static MyServer server = MyServer.getServer();

	public String commandName = "/serverport";

	final int SIGN = 68;
	final int BUTTON = 77;
	final int MIST = 90;

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

	synchronized public boolean onBlockPlace(MyPlayer player, MyBlock blockPlaced, MyBlock blockClicked, MyItem itemInHand) {

		if( portalManager.testSignBlock( blockPlaced ) || portalManager.testProtectedBlock( blockPlaced ) ) {

			String gateType = portalManager.getPortalType( blockPlaced );

			if( gateType == null ) {
				gateType = "";
			}

			if( !player.canUseCommand("/serverportcreate") && !player.canUseCommand("/serverportcreate" + gateType ) && !player.isAdmin() ) {
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

	synchronized public void onLogin(MyPlayer player) {

		String playerName = new String( player.getName());

		communicationManager.limboStore.unLockPlayer(playerName);
		
		if( communicationManager.limboStore.updateInvOnLogin ) {
			restoreInventory(player);
		}

		LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());

		if( limboInfo == null ) {
			return;
		}
		
		String currentServer = limboInfo.getCurrentServer();

		if( currentServer.equals("")) {

			if( !communicationManager.limboStore.newPlayersAllowed() ) {

				MiscUtils.safeMessage(playerName, "[ServerPort] New players are not allowed to connect to this server directly.  If you move you will be teleported");

			}


		} else if( !currentServer.equals("here") ) {

			MiscUtils.safeMessage(playerName, "[ServerPort] You have logged into the wrong server.  If you move you will be teleported");

			if( limboInfo.getTimeToLive() == 0 ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] Unable to find correct server, max hops limit reached");
				limboInfo.setCurrentServer("here");
				currentServer = limboInfo.getCurrentServer();
				limboInfo.setCurrentGate( "" );
				communicationManager.limboStore.updateDatabase(limboInfo);
			} else if( player.canUseCommand("/cancelredirect") || player.isAdmin() ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] You can use /cancelredirect to cancel this effect.  Any items are still on the server you left them.");
			}

			//communicationManager.limboStore.lockPlayer(playerName);
			return;
		}

		String currentGate = limboInfo.getCurrentGate();

		if( limboInfo.getTimeToLive() != communicationManager.defaultTimeToLive ) {
			limboInfo.setTimeToLive( communicationManager.defaultTimeToLive );
			communicationManager.limboStore.updateDatabase(limboInfo);
		}


		if( !currentGate.equals("") ) {

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

	}

	HashMap<String,Long> spamShield = new HashMap<String,Long>();


	synchronized public boolean onHealthChange(MyPlayer player, int oldValue, int newValue) {

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
	}


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
			MyLocation newFrom = new MyLocation(from.getX(),from.getY(),from.getZ(),from.getRotX(),from.getRotY());
			newFrom.setX( newFrom.getX() + 0.5);
			newFrom.setZ( newFrom.getZ() + 0.5);
			player.teleportTo(newFrom);
		} else if( portalManager.testPortalBlock(to) && !portalManager.testPortalBlock(from) ) {

			IntLocation intLoc = new IntLocation( to );

			String gateType = portalManager.getPortalType( intLoc );

			if( gateType == null ) {
				gateType = "";
			}

			if( player.canUseCommand("/serverportuse") || player.canUseCommand("/serverportuse" + gateType ) || player.isAdmin() ) {
				portalManager.enteredPortal(player, to);
			}
		}

	}

	synchronized public boolean onBlockDestroy(MyPlayer player, MyBlock block) {


		if( portalManager.testProtectedBlock( block ) ) {
			return true;
		}

		if( portalManager.testSignBlock( block ) ) {

			String gateType = portalManager.getPortalType( block );

			if( gateType == null ) {
				gateType = "";
			}

			//System.out.println( "id" + block.getType() );

			if( block.getType() == SIGN ) {

				MySign sign = (MySign)server.getComplexBlock(block.getX(), block.getY(), block.getZ(), block.getStatus());

				if( !sign.isNull() ) {
					portalManager.refreshSign( player , sign );
				}

			}
			
			boolean signPunched = block.getType() == SIGN && player.holding() <= 0;

			if( ( player.canUseCommand("/serverportuse") || player.canUseCommand("/serverportuse"+gateType) || player.isAdmin() ) && block.getStatus() == 0 && ( block.getType() == BUTTON || signPunched ) ) {

				portalManager.buttonPress( block , player );

				return false;

			}

			if( !player.canUseCommand("/serverportdestroy") && !player.canUseCommand("/serverportdestroy"+gateType) && !player.isAdmin() ) {

				return true; 
			} else {
				if( block.getStatus() == 3 ) {

					if( portalManager.destroyPortal( block ) ) {
						MiscUtils.safeMessage(player, "Gate Destroyed");
					}
				}
			}
		}

		return false;
	}


	synchronized public boolean onChat(MyPlayer player, String message) {

		communicationManager.chatManager.sendChat(player, message);

		return false;
	}


	synchronized public boolean onCommand(MyPlayer player, String[] split) {

		double m = player.getLocation().getRotX() % 360;
		m = m < 0 ? m+360 : m;

		if( split[0].equals("/release") && player.canUseCommand("/release") && player.getHealth() > 0 ) {

			MiscUtils.safeLogging("[ServerPort] " + player.getName() + " is releasing");

			LimboStore.dropItems(player);
			if( TeleportCommand.teleportToBind(communicationManager, player) ) {
				player.setHealth(20);
			}
			return true;

		}
		
		if( split[0].equals("/getinv") ) {
			restoreInventory(player);
			return true;
		}

		if( split[0].equals("/drawgate") && split.length == 2 && ( player.isAdmin() || player.canUseCommand("/drawgate") || player.canUseCommand("/drawgate" + split[1]))) {

			server.dropItem( player.getLocation(), 68, 1 );

			if( portalManager.drawGate(player, split[1])) {
				MiscUtils.safeMessage(player.getName(), "[ServerPort] Gate Auto-generation success");
				return true;
			} else {
				MiscUtils.safeMessage(player.getName(), "[ServerPort] Gate Auto-generation failed");
				return true;
			}

		}

		if( split[0].equals("/cancelredirect") && ( player.canUseCommand("/cancelredirect") || player.isAdmin() ) ) {

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

		if( split[0].equals("/regengates") && ( player.canUseCommand("/regengates") || player.isAdmin() ) ) {



			int d = 32;

			if( split.length > 1 && MiscUtils.isInt(split[1])) {
				d = MiscUtils.getInt(split[1]);
			}

			MiscUtils.safeLogging("[ServerPort] " + player.getName() + " is regenerating gates (d=" + d + ")");

			portalManager.regenGates( player , d );
			return true;

		}

		if( split[0].equals("/stopcircle" ) && ( player.canUseCommand("/circleload") || player.canUseCommand("/stopcircle") || player.isAdmin() )) {

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

			MiscUtils.loadCircle(player.getName(), loc.x, loc.y, loc.z, r);

			return true;


		}


		if( !split[0].equals(commandName) ) {
			return false;
		}

		if( !player.canUseCommand(commandName) && !player.isAdmin() ) {
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
