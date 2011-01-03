import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;


public class LimboStore {

	protected static final Logger log = Logger.getLogger("Minecraft");

	private static final String slash = System.getProperty("file.separator");

	private final static Server server = etc.getServer();

	public StringList bannedItems = new StringList();
	public Boolean banExcept = false;

	public String newPlayerForward = "allow";

	public String limboDirectory = "serverport";

	private CommunicationManager communicationManager = null;
	private ParameterManager parameterManager = null;

	private HashMap<String,LimboInfo> limboDatabase = new HashMap<String,LimboInfo>(); 

	synchronized void registerParameters( ParameterManager parameterManager ) {

		this.parameterManager = parameterManager;

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"limboDirectory",
						"limbodir",
						String.class,
						new String("serverport"),
						new String[] {
							"This parameter sets the directory to store " + 
							"the limbo files in"
						},
						"sets the limbo directory"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"banExcept",
						"banexcept",
						Boolean.class,
						new Boolean(false),
						new String[] {
							"If this parameter is set to true, all items will be banned " + 
							"except those on the ban list"
						},
						"inverts the ban list"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"bannedItems",
						"banned",
						StringList.class,
						new String("46,10,11"),
						new String[] {
							"This is a list of items that are banned from the server to server " +
							"teleport system.  If another server attempts to send these items " +
							"to the server, they will be refused." 
						},
						"sets the banned items list"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"newPlayerForward",
						"newplayers",
						String.class,
						new String("allow"),
						new String[] {
							"Unless this is set to allow, new players will be redirected to this server.  "
						},
						"sets the limbo directory"
				)
		);


	}

	synchronized void init() {

		loadLimboDatabase();

	}

	synchronized void setCommunicationManager( CommunicationManager communicationManager ) {
		this.communicationManager = communicationManager;
	}

	synchronized LimboInfo getLimboInfo( String playerName ) {

		LimboInfo limboInfo;
		if( !limboDatabase.containsKey(playerName)) {
			limboInfo = new LimboInfo();
			limboInfo.setPlayerName(playerName);
			limboInfo.initFilename(limboDirectory + slash + "limbo");
		} else {
			limboInfo = limboDatabase.get(playerName);
		}

		return limboInfo;


	}

	synchronized boolean newPlayersAllowed() {

		return this.newPlayerForward.equals("allow");

	}

	synchronized void lockPlayer( String playerName ) {

		lockPlayer( playerName , -1 );

	}

	synchronized void lockPlayer( String playerName , int duration ) {

		LimboInfo limboInfo = getLimboInfo( playerName );

		if( limboInfo != null ) {
			limboInfo.setLocked(true);
			if( duration == -1 ) {
				limboInfo.setLockRelease(-1);
			} else {
				limboInfo.setLockRelease(System.currentTimeMillis() + duration);
			}
			updateDatabase(limboInfo);
		} else {
			MiscUtils.safeLogging("[ServerPort] Limbo HashMap error, unable to lock player" );
		}

	}

	synchronized boolean isLocked( String playerName ) {

		LimboInfo limboInfo = getLimboInfo( playerName );

		if( limboInfo.getLocked() ) {
			long lockRelease = limboInfo.getLockRelease();
			if( lockRelease != -1 && lockRelease < System.currentTimeMillis() ) {
				limboInfo.setLocked(false);
				updateDatabase(limboInfo);
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	synchronized void unLockPlayer( String playerName ) {

		LimboInfo limboInfo = getLimboInfo( playerName );

		limboInfo.setLocked(false);

		updateDatabase(limboInfo);

	}

	synchronized void updateDatabase(LimboInfo limboInfo) {
		if( limboInfo != null ) {
			limboDatabase.put(limboInfo.getPlayerName(), limboInfo);
			limboInfo.save();
		}
	}

	synchronized void loadLimboDatabase() {

		File limboDirFile = new File( limboDirectory );

		if( !limboDirFile.exists() ) {
			limboDirFile.mkdir();
		}

		File limboSubDirFile = new File ( limboDirectory + slash + "limbo");

		if( !limboSubDirFile.exists() ) {
			limboSubDirFile.mkdir();
		}

		String[] filenames = limboSubDirFile.list();

		for( String filename : filenames ) {
			if( filename.matches("^.*dat$")) {

				LimboInfo limboInfo = new LimboInfo();
				limboInfo.load( limboDirectory + slash + "limbo" + slash + filename );
				updateDatabase(limboInfo);

			}
		}


	}

	synchronized void saveLimboRecord(String name) {

		LimboInfo limboInfo = limboDatabase.get(name);

		if( limboInfo != null ) {
			limboInfo.save();
		}

	}


	synchronized String processForward( String command ) {


		String[] split = command.split(":");

		if( split.length != 2 ) {
			return "UNKNOWN";
		} else {

			String[] vars = split[1].split(",");
			String playerName = vars[0];

			LimboInfo limboInfo = this.getLimboInfo(playerName);

			if( vars.length > 1 && MiscUtils.isInt(vars[1])) {
				int timeToLive = MiscUtils.getInt(vars[1]);
				limboInfo.setTimeToLive(timeToLive);
				updateDatabase(limboInfo);
			}

			final String globalHostName = ChatCommand.encodeString(communicationManager.serverPortServer.globalHostName);
			final int portnum = communicationManager.serverPortServer.gamePortNum;


			if( limboInfo == null ) {
				return "UNKNOWN:" + globalHostName + "," + portnum;
			} else if( limboInfo.getCurrentServer().equals("here")) {

				return "HERE:" + globalHostName + "," + portnum;

			} else {

				return "FORWARD:" + globalHostName + "," + portnum + "," + limboInfo.getCurrentServer();

			}

		}


	}


	synchronized String processTeleport( String command ) {

		String[] split = command.split(":",-1);

		if( split.length != 2 ) {
			return "Error:[ServerPort] command split wrong length";
		}

		String[] strings = split[1].split(",",-1);

		if( strings.length < 4 ) {
			return "Error: [ServerPort] At least playername, gate name and both rotation values must be sent";
		}

		PortalInfo portalInfo = null;

		if( (portalInfo = communicationManager.portalManager.getPortal(strings[1])) != null ) {
			LimboInfo limboInfo = getLimboInfo( strings[0] );

			limboInfo.setCurrentServer("here");
			limboInfo.setCurrentGate(strings[1]);

			double rotX = 0;
			double rotY = 0;

			if( MiscUtils.isDouble(strings[2]) && MiscUtils.isDouble(strings[3])) {

				rotX = MiscUtils.getDouble(strings[2]);
				rotY = MiscUtils.getDouble(strings[3]);

			}

			limboInfo.setRotX(rotX + portalInfo.getDir());
			limboInfo.setRotY(rotY);

			updateDatabase(limboInfo);
			int gameport = -1;
			String globalHostname = "";
			synchronized( parameterManager ) {
				gameport = communicationManager.serverPortServer.gamePortNum;
				globalHostname = communicationManager.serverPortServer.globalHostName;
			}
			return "OK:" + ChatCommand.encodeString(globalHostname) + "," + gameport;
		} else {
			return "NOGATE";
		}


	}

	synchronized String processTransfer( String command ) {

		String[] split = command.split(":",-1);

		if( split.length != 2 ) {
			return "Error:[ServerPort] command split wrong length";
		}

		String[] strings = split[1].split(";",-1);

		if( strings[0].indexOf("playername=") != 0 ) {
			return "Error: [ServerPort] no player name given for inv transfer";
		}

		String playerName = strings[0].substring(11);

		LimboInfo limboInfo = getLimboInfo(playerName);

		limboInfo.setLimboStore( this );
		limboInfo.setParameterManager( parameterManager );	

		HashMap<Integer,String> reply = limboInfo.parseString(split[1]);

		if( limboInfo.getHomeServer().equals(communicationManager.serverPortServer.serverName) ) {
			limboInfo.setHomeServer( "here");
		}

		limboInfo.setParameterManager(null);

		updateDatabase(limboInfo);

		return playerName + ";" + implode(reply);

	}

	synchronized String implode( HashMap<Integer,String> map ) {

		StringBuilder sb = new StringBuilder();

		Iterator<Integer> itr = map.keySet().iterator();

		boolean first = true;
		while( itr.hasNext() ) {
			if( !first ) {
				sb.append(";");
			}
			first = false;
			Integer current = itr.next();
			sb.append(current + "=" + map.get(current));
		}

		return sb.toString();

	}

	synchronized String removePlayerStore( Player player ) {

		String playerName = player.getName();

		LimboInfo limboInfo = getLimboInfo( playerName );

		String storedItems = limboInfo.removePlayerStore();

		updateDatabase(limboInfo);

		return storedItems;

	}

	synchronized static String removePlayerInv( Player player ) {

		Inventory inv = player.getInventory();

		if( inv == null ) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		Item[] contents = inv.getContents();

		boolean first = true;
		for( Item item : contents ) {
			if( item != null ) {
				if( !first ) {
					sb.append(";");
				}
				first = false;
				sb.append(item.getSlot() + "=" + item.getItemId() + "," + item.getDamage() + "," + item.getAmount());
				item.setItemId(0);
				item.setAmount(0);
				item.setDamage(0);
				int slot = item.getSlot();
				inv.removeItem(slot);
			}
		}

		return sb.toString();
	}

	int counter = 77;

	synchronized static void dropItems( Player player ) {

		final String playerData = new String( LimboStore.removePlayerInv( player ) );
		final Location loc = player.getLocation();
		loc.y += 5;

		String[] items = playerData.split(";");

		for( String item : items ) {

			String[] equalSplit = item.split("=");
			if( equalSplit.length>1) {
				String[] vars = equalSplit[1].split(",");
				if( vars.length == 3 ) {

					if( MiscUtils.isInt(vars[0]) && MiscUtils.isInt(vars[1]) && MiscUtils.isInt(vars[2])) {

						int id = MiscUtils.getInt(vars[0]);
						int damage = MiscUtils.getInt(vars[1]);
						int quantity = MiscUtils.getInt(vars[2]);

						System.out.println( "Data to restore: " + quantity + " of "+ id  + " at " + new IntLocation( loc ));

						server.dropItem(loc, id, quantity );

					}

				}
				
			}
		}

	}

	synchronized static String addToPlayerInv( String itemString ) {

		if( itemString == null ) {
			return "";
		}

		String[] split = itemString.split(":",-1);

		if( split.length != 2 ) {
			return "";
		}

		String[] strings = split[1].split(";");

		StringBuilder sb = new StringBuilder();

		boolean first = true;

		Player player = null;

		for( String item : strings ) {

			if( player == null ) {

				if( (player = server.getPlayer(item)) == null ) {
					return "";
				}
			} else {
				String reject = addToPlayerInvSingle( player , item );
				if( reject != null ) {
					if( !first ) {
						sb.append(";");
					}
					first=false;
					sb.append(reject);
				}
			}
		}

		return sb.toString();


	}

	synchronized static String addToPlayerInvSingle( Player player , String itemString ) {

		if( itemString == null ) {
			return null;
		}

		String[] split = itemString.split("=",-1);

		if( split.length != 2 ) {
			return null;
		}

		String[] vars = split[1].split(",",-1);

		if( vars.length != 3 ) {
			return null;
		}


		if( 
				MiscUtils.isInt(split[0]) &&
				MiscUtils.isInt(vars[0]) &&
				MiscUtils.isInt(vars[1]) &&
				MiscUtils.isInt(vars[2]) ) {

			int slot = MiscUtils.getInt(split[0]);
			int damage = MiscUtils.getInt(vars[1]);
			int id = MiscUtils.getInt(vars[0]);
			int amount = MiscUtils.getInt(vars[2]);

			Inventory inv = player.getInventory();

			Item itm = inv.getItemFromSlot(slot);

			if( itm == null ) {
				inv.setSlot(id, amount, damage, slot);
				return null;
			} else {
				return itemString;
			}


		} else {
			return null;
		}

	}


}
