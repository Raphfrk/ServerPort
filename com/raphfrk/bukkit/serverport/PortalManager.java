package com.raphfrk.bukkit.serverport;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class PortalManager {

	final int SIGN = 68;
	final int BUTTON = 77;

	String mainWorld;

	protected static final Logger log = Logger.getLogger("Minecraft");

	CommunicationManager communicationManager = null;

	static final String slash = System.getProperty("file.separator");

	HashMap<IntLocation,Integer> portalBlocks = new HashMap<IntLocation,Integer>();
	HashMap<IntLocation,Integer> signBlocks = new HashMap<IntLocation,Integer>();
	HashMap<IntLocation,Integer> blockBlocks = new HashMap<IntLocation,Integer>();
	HashMap<String,Integer>      nameLookup = new HashMap<String,Integer>();


	ArrayList<PortalInfo> portalCustom = new ArrayList<PortalInfo>();
	ArrayList<PortalInfo> portalList = new ArrayList<PortalInfo>();

	// parameters

	public String portalDirectory = "serverport";
	public long buttonRepeat = 30000;
	public boolean listCustomGates = true;
	public boolean listBuiltGates = false;
	public StringList worldList = new StringList();

	public boolean createDefaultGates = true;

	public StringMap expansionFactor = new StringMap();
	public Boolean autoCreate = true;

	public Boolean newChunks = false;

	public String worldName = "world";

	public Boolean safeExit = false;
	public StringList softBlocks = new StringList();

	public StringMap fireTarget = new StringMap();

	void setButtonRepeat( int delay ) {
		buttonRepeat = delay;
	}

	int getButtonRepeat( ) {
		return (int)buttonRepeat;
	}

	void setListCustomGates( boolean list ) {
		listCustomGates = list;
	}

	boolean getListCustomGates( ) {
		return listCustomGates;
	}

	void setListBuiltGates( boolean list ) {
		listBuiltGates = list;
	}

	boolean getListBuiltGates( ) {
		return listBuiltGates;
	}

	void setPortalDirectory( String directory ) {

		portalDirectory = directory;

	}

	String getPortalDirectory( ) {
		return portalDirectory;
	}


	void init() {

		mainWorld = MyServer.bukkitServer.getWorlds().get(0).getName();

		loadPortalInfo();
		savePortalInfo();

		listCustomGates(listCustomGates);
		listBuiltGates(listBuiltGates);

		File properties = MiscUtils.dirScan( "." , "server.PROPERTIES");

		if( properties == null ) {
			worldName = "world";
		} else {
			MyPropertiesFile pf = new MyPropertiesFile( properties.getPath() );

			pf.load();

			if( pf.containsKey("level-name")) {
				worldName = pf.getString("level-name");
			} else {
				worldName = "world";
			}

			if( pf.containsKey("hellworld") && pf.getBoolean("hellworld")) {

				worldName = worldName + slash + "DIM-1";
			} 
		}


	}

	void setCommunicationManager(CommunicationManager communicationManager) {
		this.communicationManager = communicationManager;
	}

	void registerParameters( ParameterManager parameterManager ) {

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"buttonRepeat",
						"buttonrepeat",
						Integer.class,
						new Integer(2000),
						new String[] {
							"This parameter sets the minimum delay between" + 
							"button presses on a gate in ms.  Button presses faster than that will be ignored.  The default is 2 seconds (2000)."
						},
						"sets the button repeat rate"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"listCustomGates",
						"listcustom",
						Boolean.class,
						new Boolean(true),
						new String[] {
							"If this parameter is true, then the plugin will list all custom gates when started"
						},
						"enables/disables listing of custom gates"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"worldList",
						"worldlist",
						StringList.class,
						new String(""),
						new String[] {
							"This is a list of worlds for the plugin to load/create"
						},
						"lists multi-worlds"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"safeExit",
						"safeexit",
						Boolean.class,
						new Boolean(false),
						new String[] {
							"If this parameter is true, then any exit portals created will be in safe(r) locations",
							"The portal will be moved up and down as so that the exit point is free"
						},
						"moves exit portals to safe(r) locations"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"softBlocks",
						"softblocks",
						StringList.class,
						new String("0,6,17,18,37,38,39,40,50,51,55,59,63,65,66,68,69,70,72,75,76,78,83,85,90"),
						new String[] {
							"This is a list of blocks that are considered soft when working out where to place auto-generated exit portals"
						},
						"sets soft blocks for safe exit portal placement"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"newChunks",
						"newchunks",
						Boolean.class,
						new Boolean(true),
						new String[] {
							"If this parameter is true, then the plugin can " +
							"generate new portals outside already generated areas of the world"
						},
						"enables/disables new chunk generation code"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"createDefaultGates",
						"defaultgates",
						Boolean.class,
						new Boolean(true),
						new String[] {
							"If this parameter is true, then the plugin will" +
							"create the default gates at startup"
						},
						"enables/disables creation of default gates"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"listBuiltGates",
						"listbuilt",
						Boolean.class,
						new Boolean(false),
						new String[] {
							"If this parameter is true, then the plugin will" +
							"list all built gates when started"
						},
						"enables/disables listing of built gates"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"fireTarget",
						"firetarget",
						StringMap.class,
						new String(""),
						new String[] {
							"This parameter sets the server name for fire started portals"
						},
						"Sets the target for fire started portals"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"expansionFactor",
						"expansion",
						StringMap.class,
						new String(""),
						new String[] {
							"This parameter determines the expansion factor for each of the worlds.  This is used when automatically creating target gates.",
							"The default is 100.  A server with a number lower than 100 will act like the Nether on single player.  Nether would have an expansion factor of 12.5, as the real world has 8 blocks for every Nether block.",
							"Expansion factors below 1 are assumed to be 1"
						},
						"sets the expansion factor"
				)
		);

		parameterManager.registerParameter( 
				new ParameterInfo( 
						this, 
						"autoCreate",
						"autocreate",
						Boolean.class,
						new Boolean(true),
						new String[] {
							"If this parameter is true, then gates are allowed to be created when players try to open a gate that doesn't exist."
						},
						"enables/disables automatic builting of exit gates"
				)
		);



	}

	boolean testProtectedBlock( MyBlock block ) {
		IntLocation loc = new IntLocation( block );

		return blockBlocks.containsKey(loc);

	}

	boolean testSignBlock( MyBlock block ) {
		IntLocation loc = new IntLocation( block );

		return signBlocks.containsKey(loc);

	}

	boolean testPortalBlock( IntLocation loc ) {

		return testPortalBlock( new MyLocation( loc.getWorld(), loc.getX() , loc.getY() , loc.getZ() ));

	}

	boolean testPortalBlock( MyLocation loc ) {
		IntLocation intLoc = new IntLocation( loc );

		if( portalBlocks.containsKey(intLoc) ) {
			int idNum = portalBlocks.get(intLoc);
			PortalInfo portalInfo = portalList.get(idNum);
			return portalInfo.isActive();
		} else {
			return false;
		}

	}

	boolean testPortalBlock( MyBlock block ) {

		IntLocation loc = new IntLocation( block );

		return portalBlocks.containsKey(loc);

	}

	String getPortalType( MyBlock block ) {

		IntLocation loc = new IntLocation( block );

		return getPortalType( loc );

	}

	String getPortalType( IntLocation loc ) {

		int id = 0;
		if( blockBlocks.containsKey(loc)) {
			id = blockBlocks.get(loc);
		}  else if ( signBlocks.containsKey(loc) ) {
			id = signBlocks.get(loc);
		} else {
			return null;
		}

		PortalInfo portalInfo = portalList.get(id);

		return portalInfo.portalType;

	}


	boolean destroyPortal( MyBlock block ) {

		boolean signBlock = testSignBlock(block);
		boolean protectedBlock = testProtectedBlock(block); 

		int idNum=-1;
		if( signBlock ) {
			IntLocation loc = new IntLocation( block );
			idNum = signBlocks.get(loc);
		} else if (protectedBlock){
			IntLocation loc = new IntLocation( block );
			idNum = blockBlocks.get(loc);
		}

		if( idNum == -1 ) {
			return false;
		} else {
			IntLocation loc = new IntLocation( block );

			PortalInfo portalInfo = portalList.get(idNum);

			if( protectedBlock || !portalInfo.hasSign(loc) ) {
				File file = new File( portalInfo.getFileName() );
				if( file.exists() ) {
					file.delete();
					portalInfo.destroy();
					loadPortalInfo();
					return true;
				} else {
					MiscUtils.safeLogging(log, "[ServerPort] Unable to find gate to delete");
					return false;
				}

			} else {
				return false;
			}
		}

	}

	boolean testPortalBlock( MyBlock block , boolean checkActive ) {

		IntLocation loc = new IntLocation( block );

		if( !portalBlocks.containsKey(loc) ) {
			return false;
		}

		int idNum = portalBlocks.get(loc);

		PortalInfo portalInfo = portalList.get(idNum);

		return portalInfo.isActive() || (!checkActive) ;

	}

	boolean isLocalWorld(String name) {

		List<World> worlds = MyServer.bukkitServer.getWorlds();

		for(World world : worlds ) {
			if(world.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;

	}

	long lastPress = -1;

	void buttonPress( MyBlock block , MyPlayer player ) {

		long currentTime = System.currentTimeMillis();

		if( currentTime < lastPress + buttonRepeat ) {
			return;
		}

		lastPress = currentTime;

		IntLocation loc = new IntLocation( block );

		if( !signBlocks.containsKey(loc) ) {
			return;
		}

		int portalID = signBlocks.get(loc);

		PortalInfo portalInfo = portalList.get(portalID);

		if( portalInfo.isActive() && portalInfo.portalDuration != -1 ) {
			return;
		}

		String targetServer = portalInfo.targetServer;
		String targetGate = portalInfo.targetGate;
		String thisGate = portalInfo.getName();
		String playerName = player.getName();
		String targetWorld = portalInfo.targetWorld;
		int portalDuration = portalInfo.portalDuration;


		IntLocation portalLoc = portalInfo.getPos();

		String worldExpansionString = expansionFactor.getValue(block.getWorld().getName());
		if(worldExpansionString == null) {
			worldExpansionString = expansionFactor.getValue("*");
		}

		Double expansion = 100.0;
		if(worldExpansionString != null) {
			try {
				expansion = Double.parseDouble(worldExpansionString);
			} catch (NumberFormatException nfe) {
			}
		}

		double effectiveExpansion = (expansion<1)?1:expansion;

		double x = ((double)portalLoc.getX()) / effectiveExpansion;
		double y = ((double)portalLoc.getY()) ;
		double z = ((double)portalLoc.getZ()) / effectiveExpansion;

		int dx = portalInfo.dx;
		int dz = portalInfo.dz;

		String type = portalInfo.portalType;

		boolean createGate = portalInfo.autoCreate;

		if( targetServer.equals("here") && !targetWorld.equals("_default")) {
			targetServer = targetWorld;
		}

		if( isLocalWorld(targetServer) ) {
			String command = 
				"OPENGATE:" + 
				playerName + "," + 
				targetGate + "," + 
				portalDuration + "," +
				createGate + "," +
				type + "," +
				dx + "," + dz + "," +
				x + "," + y + "," + z + "," + 
				"here" + "," +
				targetServer;

			//player.sendMessage(command);

			String reply = activatePortalCommand(command);

			//player.sendMessage(reply);

			final String finalPlayerName = playerName;
			final String finalCommandString = command;

			if( reply.contains("CHUNKGENFAIL")) {
				player.sendMessage("Please wait for world loading");
				MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {

					public void run() {
						if( finalPlayerName != null ) {
							String reply = activatePortalCommand(finalCommandString);

							org.bukkit.entity.Player player = MyServer.bukkitServer.getPlayer(finalPlayerName);
							if(!reply.contains("GATEBUILT") && !reply.contains("GATEOPENED")) {

								player.sendMessage("Unable to build gate in target world");

							} else {
								player.sendMessage(reply);
							}
						}
					}

				},40);
			}

			if( reply != null ) {
				String[] vars = reply.split(",",-1);
				boolean gateOpened = reply.indexOf("GATEOPENED") == 0;
				if( gateOpened && vars.length > 1 && MiscUtils.isInt(vars[1])) {
					portalInfo.activate(MiscUtils.getInt(vars[1]));

				}
			}

		} else if( targetServer.equals("here")) {

			String reply = activatePortalCommand("OPENGATE:" + playerName + "," + targetGate + "," + portalDuration );

			if( reply != null ) {
				String[] vars = reply.split(",",-1);
				boolean gateOpened = reply.indexOf("GATEOPENED") == 0;
				if( gateOpened && vars.length > 1 && MiscUtils.isInt(vars[1])) {
					portalInfo.activate(MiscUtils.getInt(vars[1]));
				}
			}

		} else {

			if( portalInfo.portalName.equalsIgnoreCase(portalInfo.targetGate) ) {
				targetGate = portalInfo.portalActualName;
			}

			communicationManager.commandFIFO.runBlindCommand( 
					"OPENGATE:" + 
					playerName + "," + 
					targetServer + "," + 
					targetGate + "," + 
					thisGate + "," + 
					portalDuration + "," +
					x + "," + y + "," + z + "," + 
					createGate + "," +
					dx + "," + dz + "," +
					type + "," +
					communicationManager.serverPortServer.serverName);

		}

	}

	/*	void enteredPortal( BaseVehicle vehicle, IntLocation loc ) {

		Player player = vehicle.getPassenger();

		if( player != null ) {

			enteredPortal( player , new Location( loc.x , loc.y , loc.z ) , vehicle );

		}

	}


	void enteredPortal( Player player , Location loc ) {

		enteredPortal( player , loc , null );

	}

	HashMap<Integer,Location> delayedMotions = new HashMap<Integer,Location>();

	HashMap<Integer,Location> getDelayedMotions() {

		return delayedMotions;

	}
	 */

	PortalInfo getPortalByBlock(MyBlock block) {

		IntLocation intLoc = new IntLocation( block );

		return getPortalByBlock(intLoc);

	}

	PortalInfo getPortalByBlock(IntLocation intLoc) {

		int idNum;

		if( blockBlocks.containsKey(intLoc) ) {
			idNum = blockBlocks.get(intLoc);
			return portalList.get(idNum);
		} 

		if( signBlocks.containsKey(intLoc) ) {
			idNum = signBlocks.get(intLoc);
			return portalList.get(idNum);
		} else {
			return null;
		}
	}

	String getDestination(PortalInfo portalInfo) {
		if(portalInfo.targetServer.equals("here")) {
			PortalInfo dest = getPortal(portalInfo.targetGate);
			if( dest == null ) {
				return null;
			} else {
				return dest.portalWorld;
			}
		} else {
			return portalInfo.targetServer;
		}
	}

	void enteredPortal( MyPlayer player , MyLocation loc /*, BaseVehicle vehicle */ ) {

		IntLocation intLoc = new IntLocation( loc );

		int idNum;

		PortalInfo portalInfo;
		if( portalBlocks.containsKey(intLoc) ) {
			idNum = portalBlocks.get(intLoc);
			portalInfo = portalList.get(idNum);;
		} else {
			return;
		}

		MiscUtils.safeLogging("[ServerPort] " + player.getName() + " entered " + portalInfo.getName() );


		if( portalInfo.targetServer.equals("here")  ) {

			PortalInfo targetPortal = this.getPortal(portalInfo.targetGate);

			if( targetPortal == null ) {
				MiscUtils.safeMessage(player, "[ServerPort] Unable to find target gate");
				return;
			} 

			if( portalInfo.bindStone ) {
				return;
			}

			if( targetPortal.isActive() ) {
				IntLocation target = targetPortal.getExitPoint();
				MyLocation newLoc = new MyLocation();

				newLoc.setX( target.x + 0.5 );
				newLoc.setY( target.y + 0.0 );
				newLoc.setZ( target.z + 0.5 );

				newLoc.setRotX( (float)(loc.getRotX() + 180 + targetPortal.getDir() - portalInfo.getDir()) % 360 );
				newLoc.setRotY( loc.getRotY() );

				newLoc.setWorld( target.getWorld() );

				Vehicle vehicle = player.getVehicle();

				if( vehicle == null ) {
					player.teleportTo(newLoc);
				} else {
					newLoc.setX( target.x + 0.0 );
					newLoc.setY( target.y + 0.5 );
					newLoc.setZ( target.z + 0.0 );
					
					float rotation = newLoc.getRotX() - loc.getRotX();
					float radians = -(float)Math.toRadians(rotation);

					Vector velocity = vehicle.getVelocity();

					double newsx = Math.cos(radians)*velocity.getX() + Math.sin(radians)*velocity.getZ();
					double newsz = Math.cos(radians)*velocity.getZ() + Math.sin(radians)*velocity.getX();

					Vector newVelocity = new Vector(newsx, velocity.getY(), newsz);

					MiscUtils.gridLoad(newLoc.getWorld(), (int)Math.floor(newLoc.getX()), (int)Math.floor(newLoc.getY()), (int)Math.floor(newLoc.getZ()));

					//((ServerPortBukkit)MyServer.plugin).playerListener.oldPositions.remove(player.getBukkitPlayer().getEntityId());

					//player.leaveVehicle();
					
					vehicle.setVelocity(new Vector(0,0,0));

					final MyPlayer finalPlayer = player;
					final Vehicle finalVehicle = vehicle;
					final Vector finalNewVelocity = newVelocity.clone();
					final MyLocation finalNewLoc = newLoc;
										
					finalVehicle.teleport(finalNewLoc.getBukkitLocation());
					//finalPlayer.teleportTo(finalNewLoc);
					finalVehicle.setVelocity(finalNewVelocity);

					/*MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {
						public void run() {
							((ServerPortBukkit)MyServer.plugin).playerListener.oldPositions.remove(finalPlayer.getBukkitPlayer().getEntityId());
					//		finalPlayer.teleportTo(finalNewLoc);
						}
					},5);
					
					MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {
						public void run() {
							((ServerPortBukkit)MyServer.plugin).playerListener.oldPositions.remove(finalPlayer.getBukkitPlayer().getEntityId());
							finalVehicle.teleportTo(finalNewLoc.getBukkitLocation());
							finalVehicle.setVelocity(new Vector(0,0,0));
						}
					},10);


					MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {

						public void run() {
							System.out.println("Setting delayed info");
							((ServerPortBukkit)MyServer.plugin).playerListener.oldPositions.remove(finalPlayer.getBukkitPlayer().getEntityId());
					//		if(!finalVehicle.setPassenger(finalPlayer.getBukkitPlayer())) {
					//			System.out.println("Unable to set passenger");
					//		}
						}
					}, 100);
					
					MyServer.bukkitServer.getScheduler().scheduleSyncDelayedTask(MyServer.plugin, new Runnable() {

						public void run() {
							System.out.println("Setting velocity info");
							finalVehicle.setVelocity(finalNewVelocity);
						}
					}, 200);

*/
				}

			} else {
				MiscUtils.safeMessage(player, "[ServerPort] Target gate is not open");
				return;
			}

		} else {

			MiscUtils.safeLogging("[ServerPort] Teleporting due to entering portal " + portalInfo.getName() );			
			TeleportCommand.teleport(communicationManager, player, portalInfo, false );

		}

	}

	PortalInfo getPortal( String name ) {

		for( PortalInfo portalInfo : portalList ) {

			if( portalInfo.getName().equals(name)) {
				return portalInfo;
			}

		}

		return null;


	}

	PortalInfo getCustomPortal( String name ) {

		for( PortalInfo portalInfo : portalCustom ) {

			if( portalInfo.getName().equalsIgnoreCase(name)) {
				return portalInfo;
			}

		}

		return null;


	}

	String activatePortalCommand( String command  ) {

		String[] split = command.split(":");

		String[] vars = split[1].split(",");

		if( vars.length < 3 ) return "WRONGVARS";

		String playerName = vars[0];
		String targetGate = vars[1];

		if( !MiscUtils.isInt(vars[2])) return "WRONGVARS";
		int durationRequest = MiscUtils.getInt(vars[2]);

		String reply = activatePortal( targetGate.toLowerCase() , durationRequest );

		if( !reply.equals("NOGATE") || vars.length < 11 ) {
			return reply;
		}

		if( !autoCreate ) {
			return "BUILDBANNED";
		}

		if( !MiscUtils.isBoolean(vars[3])) return "WRONGVARS";
		boolean createGate = MiscUtils.getBoolean(vars[3]);

		if( !createGate ) {
			return reply;
		}

		String type = vars[4];

		PortalInfo portalCustom = getCustomPortal( type );

		if( portalCustom == null ) {
			return "UNKNOWNTYPE";
		}

		if( !MiscUtils.isInt(vars[5])) return "WRONGVARS";
		int dx = MiscUtils.getInt(vars[5]);

		if( !MiscUtils.isInt(vars[6])) return "WRONGVARS";
		int dz = MiscUtils.getInt(vars[6]);

		String peerServerName = vars[10];

		String targetWorld;

		if( vars.length < 12 ) {
			targetWorld = mainWorld; //MyServer.bukkitServer.getWorlds().get(0).getName();
		} else {
			targetWorld = vars[11];
		}

		String worldExpansionString = expansionFactor.getValue(targetWorld);
		if(worldExpansionString == null) {
			worldExpansionString = expansionFactor.getValue("*");
		}

		Double expansion = 100.0;
		if(worldExpansionString != null) {
			try {
				expansion = Double.parseDouble(worldExpansionString);
			} catch (NumberFormatException nfe) {
			}
		}

		double effectiveExpansion = (expansion<1)?1:expansion;

		if( !MiscUtils.isDouble(vars[7])) return "WRONGVARS";
		double x = MiscUtils.getDouble(vars[7]) * effectiveExpansion;

		if( !MiscUtils.isDouble(vars[8])) return "WRONGVARS";
		double y = MiscUtils.getDouble(vars[8]);

		if( !MiscUtils.isDouble(vars[9])) return "WRONGVARS";
		double z = MiscUtils.getDouble(vars[9]) * effectiveExpansion;

		int px = (int)Math.round(x);
		int py = (int)y;
		int pz = (int)Math.round(z);

		if( py > 127 ) py = 127;
		if( py < 0 ) py = 0;

		portalCustom.x = px;
		portalCustom.y = py;
		portalCustom.z = pz;
		portalCustom.dx = dx;
		portalCustom.dz = dz;
		portalCustom.portalWorld = targetWorld;

		if( !portalCustom.testDraw(blockBlocks) || !portalCustom.testDraw(signBlocks)) {
			portalCustom.x = 0;
			portalCustom.y = 0;
			portalCustom.z = 0;
			portalCustom.dx = 1;
			portalCustom.dz = 0;
			portalCustom.portalWorld = "_default";
			return "BADBUILD";
		}

		if( !newChunks && !MiscUtils.fileCheck(worldName, portalCustom.getBlocks(-1))) {
			portalCustom.x = 0;
			portalCustom.y = 0;
			portalCustom.z = 0;
			portalCustom.dx = 1;
			portalCustom.dz = 0;
			portalCustom.portalWorld = "_default";
			return "CHUNKGENBAN";

		}

		if( !MiscUtils.generatedTest( worldName , portalCustom.getBlocks(-1))) {

			MiscUtils.loadChunks(portalCustom.getBlocks(-1));

			loadPortalInfo();

			return "CHUNKGENFAIL";
		} 

		String newPortalName = targetGate.toLowerCase();
		portalCustom.targetGate = targetGate.toLowerCase();
		portalCustom.owner = playerName;
		portalCustom.portalActualName = targetGate;
		portalCustom.targetServer = peerServerName;

		if( vars.length == 12 ) {
			if(targetGate.startsWith("_")) {
				portalCustom.x = 0;
				portalCustom.y = 0;
				portalCustom.z = 0;
				portalCustom.dx = 1;
				portalCustom.dz = 0;
				portalCustom.portalWorld = "_default";
				return "BADNAME";			
			} else {
				String targetFormatted = targetGate.toLowerCase();
				PortalInfo originPortal = getPortal(targetFormatted);
				if( originPortal == null && targetFormatted.endsWith("_")) {
					targetFormatted = targetFormatted.substring(0, targetFormatted.length()-1);
					originPortal = getPortal(targetFormatted);
				}
				if( originPortal == null ) {
					portalCustom.x = 0;
					portalCustom.y = 0;
					portalCustom.z = 0;
					portalCustom.dx = 1;
					portalCustom.dz = 0;
					portalCustom.portalWorld = "_default";
					return "NOSOURCEGATE";
				}
				originPortal.targetServer = "here";
				originPortal.targetGate = targetFormatted + "_";
				originPortal.save();
				newPortalName = originPortal.targetGate;
				portalCustom.targetServer = "here";
				portalCustom.targetGate = originPortal.portalName;
				portalCustom.portalActualName = targetFormatted;
			}
		}

		IntLocation loc = portalCustom.getExitPoint();
		int shift = 0;

		if( safeExit ) {
			shift = MiscUtils.getSafeExit( loc , softBlocks.getValues() );
			portalCustom.y += shift;

			if( !portalCustom.testDraw(blockBlocks) || !portalCustom.testDraw(signBlocks)) {
				portalCustom.y -= shift;
			}
		}

		portalCustom.portalName = newPortalName;

		portalCustom.fullDraw();
		portalCustom.fullDraw();

		portalCustom.setFileName( portalCustom.portalWorld + slash + portalDirectory + slash + "gates" + slash + targetGate.toLowerCase() + ".gat" );

		portalCustom.save();

		loadPortalInfo();

		portalCustom.drawClosed();

		reply = activatePortal( targetGate.toLowerCase() , durationRequest );

		reply = reply.replace("GATEOPENED", "GATEBUILT");

		return reply;

	}






	String activatePortal( String portalName , int durationRequest ) {

		PortalInfo portalInfo = getPortal(portalName);

		if( portalInfo == null ) {
			return "NOGATE";
		} else {
			if( portalInfo.isActive() && portalInfo.portalDuration != -1 ) {
				return "OPENALREADY";
			} else {
				return "GATEOPENED," + portalInfo.activate(durationRequest);
			}
		}

	}

	String activatePortal( String portalName ) {

		return activatePortal( portalName , -1 );

	}

	void refreshAtiveStates() {

		for( PortalInfo portalInfo : portalList) {

			portalInfo.refreshActiveState();

		}

	}

	void listCustomGates( boolean list ) {
		if( !list ) {
			return;
		}

		MiscUtils.safeLogging(log, "Custom gates defined:");

		for( PortalInfo portalInfo : portalCustom) {

			MiscUtils.safeLogging(log, portalInfo.getName() );

		}
	}


	void listBuiltGates( boolean list ) {
		if( !list ) {
			return;
		}

		MiscUtils.safeLogging(log, "Built gates:");

		for( PortalInfo portalInfo : portalList) {

			MiscUtils.safeLogging(log, portalInfo.getName() );

		}
	}

	void savePortalInfo() {

		for( PortalInfo portalInfo : portalList ) {

			portalInfo.save();

		}

	}

	void loadPortalInfo() {

		portalBlocks = new HashMap<IntLocation,Integer>();
		signBlocks = new HashMap<IntLocation,Integer>();
		blockBlocks = new HashMap<IntLocation,Integer>();
		nameLookup = new HashMap<String,Integer>();

		portalCustom = new ArrayList<PortalInfo>();
		portalList = new ArrayList<PortalInfo>();

		File olddir =  new File( portalDirectory + slash + "custom" );

		if(olddir.exists()) {
			MiscUtils.safeLogging(
					"\n\n" +
					"[ServerPort] WARNING: directory structure changed\n" + 
					"[ServerPort] WARNING: custom gates should now be placed in\n" + 
					"[ServerPort] " + MyServer.getBaseFolder() + MiscUtils.slash + "custom\n");
		}

		File directory = new File(MyServer.getBaseFolder() + MiscUtils.slash + "custom"); 


		File netherFile = new File( directory + slash + "Nether.gat");

		if( createDefaultGates ) {

			PortalInfo netherGate = netherGate();

			netherGate.portalFileName = netherFile.getPath();

			netherGate.save();

		}

		File waterCurtainFile = new File( directory + slash + "WaterCurtain.gat");

		if( createDefaultGates ) {

			PortalInfo waterCurtainGate = waterCurtainGate();

			waterCurtainGate.portalFileName = waterCurtainFile.getPath();

			waterCurtainGate.save();

		}

		File templeFile = new File( directory + slash + "Temple.gat");

		if( createDefaultGates ) {

			PortalInfo TempleGate = templeGate();

			TempleGate.portalFileName = templeFile.getPath();

			TempleGate.save();

		}

		File bindFile = new File( directory + slash + "BindStone.gat");

		if( createDefaultGates ) {

			PortalInfo bindStone = bindStone();

			bindStone.portalFileName = bindFile.getPath();

			bindStone.save();

		}
		
		File cartFile = new File( directory + slash + "CartGate.gat");

		if( createDefaultGates ) {

			PortalInfo cartGate = cartGate();

			cartGate.portalFileName = cartFile.getPath();

			cartGate.save();

		}

		String[] filenames = directory.list();

		for( String filename : filenames ) {
			if( filename.matches(".*gat$") ) {
				PortalInfo portalInfo = new PortalInfo();
				portalInfo.load( mainWorld, directory + slash + filename );
				portalInfo.portalType = portalInfo.portalName;
				portalCustom.add(portalInfo);
			}

		}

		directory = new File(MyServer.getBaseFolder() + MiscUtils.slash + "gates"); 

		if(directory.exists()) {
			filenames = directory.list();

			for( String filename : filenames ) {
				if( filename.matches(".*gat$") ) {
					PortalInfo portalInfo = new PortalInfo();
					portalInfo.load( null, directory + slash + filename );
					portalList.add(portalInfo);
					int idNum = portalList.size()-1;
					blockBlocks.putAll(portalInfo.getBlocks(idNum));
					signBlocks.putAll(portalInfo.getSigns(idNum));
					portalBlocks.putAll(portalInfo.getPortal(idNum));
					nameLookup.put(portalInfo.portalName, idNum);
				}

			}
		}

		List<World> worlds = MyServer.bukkitServer.getWorlds();
		for(World world : worlds ) {
			String worldName = world.getName();
			directory = new File( worldName + slash + portalDirectory + slash + "gates" );
			filenames = directory.list();

			if(filenames!=null) {
				for( String filename : filenames ) {
					if( filename.matches(".*gat$") ) {
						PortalInfo portalInfo = new PortalInfo();
						portalInfo.load( worldName, directory + slash + filename );
						portalList.add(portalInfo);
						int idNum = portalList.size()-1;
						blockBlocks.putAll(portalInfo.getBlocks(idNum));
						signBlocks.putAll(portalInfo.getSigns(idNum));
						portalBlocks.putAll(portalInfo.getPortal(idNum));
						nameLookup.put(portalInfo.portalName, idNum);
					}

				}
			}
		}


	}

	boolean drawGate( MyPlayer player , String gate ) {

		if( player == null || player.isNull() ) {
			return false;
		}

		PortalInfo portalInfo = getCustomPortal( gate );

		if( portalInfo == null ) {
			return false;
		}

		IntLocation loc = new IntLocation( player.getLocation() );

		double dir = player.getLocation().getRotX() + 180;

		int rot = PortalInfo.dirToRot(dir);

		portalInfo.dx = PortalInfo.rotToDX(rot);
		portalInfo.dz = PortalInfo.rotToDZ(rot);

		IntLocation exitBase = portalInfo.getExitPoint();

		portalInfo.x = loc.x-exitBase.getX();
		portalInfo.y = loc.y-exitBase.getY();
		portalInfo.z = loc.z-exitBase.getZ();

		portalInfo.portalWorld = player.getWorld().getName();

		if( !portalInfo.testDraw(blockBlocks) || !portalInfo.testDraw(signBlocks)) {

			portalInfo.dx = 1;
			portalInfo.dz = 0;

			portalInfo.x = 0;
			portalInfo.y = 0;
			portalInfo.z = 0;

			return false;

		}

		MiscUtils.blockDraw(portalInfo.getBlocks(-1));
		MiscUtils.blockDraw(portalInfo.getBlocks(-1));

		portalInfo.dx = 1;
		portalInfo.dz = 0;

		portalInfo.x = 0;
		portalInfo.y = 0;
		portalInfo.z = 0;
		portalInfo.portalWorld = "_default";

		return true;


	}

	void regenGates( MyPlayer player , int d ) {

		if( player != null && !player.isNull() ) {
			regenGates( player.getLocation() , d );
		}

	}

	void regenGates( MyLocation loc , int d ) {

		if( loc != null && !loc.isNull() ) {

			regenGates( new IntLocation( loc ) , d );

		}

	}

	void regenGates( IntLocation loc , int d ) {

		for( PortalInfo portalInfo : portalList ) {

			if( d == -1 || IntLocation.dist( loc , portalInfo.getExitPoint() ) <= d ) {

				MiscUtils.safeLogging("[ServerPort] Regenerating gate: " + portalInfo.portalName );
				portalInfo.fullDraw();

				if( portalInfo.isActive() ) {

					portalInfo.drawMist();

				}

			}

		}


	}

	void refreshSign( MyPlayer player , MySign sign ) {

		int id = signBlocks.get(new IntLocation( sign.getBlock() ));

		PortalInfo portalInfo = portalList.get(id);

		if( portalInfo == null ) return;

		String name = portalInfo.portalName.replace("_", "");
		String target = portalInfo.targetGate.replace("_","");

		sign.setText(0, "");
		sign.setText(1, portalInfo.portalActualName );
		sign.setText(2, portalInfo.targetServer.equals("here")?"":portalInfo.targetServer);
		sign.setText(3, target.equalsIgnoreCase(name)?"":target);

		sign.update();

		if( portalInfo.bindStone ) {
			if( player != null && !player.isNull() ) {
				LimboInfo limboInfo = communicationManager.limboStore.getLimboInfo(player.getName());
				if( limboInfo != null ) {

					if( !limboInfo.getHomeGate().equals(portalInfo.portalName) || !limboInfo.getHomeServer().equals("here") ) {

						MiscUtils.safeMessage(player, "[ServerPort] You have bound to this location");
						limboInfo.setHomeGate(portalInfo.portalName);
						limboInfo.setHomeServer("here");
						communicationManager.limboStore.updateDatabase(limboInfo);

					}
				}
			}
		}

	}

	int gateCounter = 1;

	void fireStarted( MyPlayer player , MyBlock block ) {

		String target;

		if( (target = fireTarget.getValue(block.getWorld().getName())) == null ) {
			return;
		}

		IntLocation loc = new IntLocation( block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

		if( blockBlocks.containsKey(loc) || signBlocks.containsKey(loc) ) {
			return;
		}

		for( PortalInfo portalInfo : portalCustom ) {

			String gateType = portalInfo.portalType;

			boolean allowed = 
				player.permissionCheck("create_fire_gate_type" , new String[] {gateType, player.getWorld().getName(),target});

			if( allowed && portalInfo.testMatch(block, blockBlocks) ) {

				MiscUtils.safeMessage(player, "[ServerPort] Gate Match detected");

				String gateName;
				do {
					gateName = "FIRE_" + MiscUtils.genRandomCode().substring(0, 6);
				} while ( nameLookup.containsKey(gateName));

				String actualName = gateName;
				String line1 = actualName.toLowerCase();
				String line2 = target;
				String line3 = "";

				processSign(portalInfo, player, block, actualName, line1, line2, line3);				

			}

		}

	}

	void signPlaced( MyPlayer player , MySign sign  ) {

		if( sign.getText(0).length() > 0 ) {
			return;
		}

		IntLocation loc = new IntLocation( sign.getX(), sign.getY(), sign.getZ() , sign.getBlock().getWorld().getName());

		if( blockBlocks.containsKey(loc) || signBlocks.containsKey(loc) ) {
			return;
		}

		String actualName = sign.getText(1);
		String line1 = actualName.toLowerCase();
		String line2 = sign.getText(2);
		String line3 = sign.getText(3);

		if( line1.length() + line2.length() + line3.length() == 0 ) {
			return;
		}

		for( PortalInfo portalInfo : portalCustom ) {

			String gateType = portalInfo.portalType;

			boolean allowed = player.permissionCheck("create_gate_type", new String[] {gateType, player.getWorld().getName(), "*"});

			if( allowed && portalInfo.testMatch(sign, blockBlocks) ) {

				MiscUtils.safeMessage(player, "[ServerPort] Gate Match detected");

				if( !MiscUtils.checkText(line1) ) {
					MiscUtils.safeMessage(player, MiscUtils.checkRules("[ServerPort] Gate name (" + line1 + ") "));
					return;
				}

				processSign(portalInfo, player, sign.getBlock(), actualName, line1, line2, line3);

				return;

			}

		}

	}

	void processSign(PortalInfo portalInfo, MyPlayer player, MyBlock block, String actualName, String line1, String line2, String line3) {

		if( line2.equals("")) {
			line2 = "here";
		} else if( !MiscUtils.checkText(line2) ) {
			MiscUtils.safeMessage(player, MiscUtils.checkRules("[ServerPort] Target server name "));
			return;
		}

		boolean automaticTarget = false;

		if( line3.length() == 0 ) {
			line3 = line1;
			automaticTarget = true;
		} else if( !MiscUtils.checkText(line3) ) {

			MiscUtils.safeMessage(player, MiscUtils.checkRules("[ServerPort] Target gate name "));
			return;
		}

		if( line2.equals("here") && !portalInfo.bindStone ) {

			if( nameLookup.containsKey(line1) ) {

				line1 = line1 + "_";

			} else if( automaticTarget ){
				line3 = line3 + "_";
			}
		}

		if( nameLookup.containsKey(line1)) {
			MiscUtils.safeMessage(player, "[ServerPort] Gate name \"" + line1 + "\" already in use");
			return;
		}

		if( !line2.equals("here") && !isLocalWorld(line2)) {
			if( communicationManager.peerServerDatabase.getServer(line2) == null ) {
				MiscUtils.safeMessage(player, "[ServerPort] Unknown target server \"" + line2 + "\"" );
				return;
			} else if ( !communicationManager.peerServerDatabase.getServer(line2).connected ) {
				MiscUtils.safeMessage(player, "[ServerPort] Connection has not been completed with \"" + line2 + "\"" );
				return;
			}
		}

		PortalInfo targetPortal = getPortal(line3);
		if(targetPortal != null) {
			if(!player.permissionCheck("create_gate_type", new String[] {portalInfo.portalType, player.getWorld().getName(), targetPortal.portalWorld})) {
				MiscUtils.safeMessage(player, "[ServerPort] Permission failure for player " + player.getName() );
				return;
			}
		}


		String oldName = portalInfo.getName();
		String oldFileName = portalInfo.getFileName();

		portalInfo.portalActualName = actualName;
		portalInfo.owner = player.getName();
		portalInfo.portalName = line1;
		portalInfo.targetServer = line2;
		portalInfo.targetGate = line3;
		portalInfo.portalWorld = block.bukkitBlock.getWorld().getName();

		//String newName = oldName + "_" + (portalInfo.getPos()).getFileString();

		portalInfo.setFileName( portalInfo.portalWorld + slash + portalDirectory + slash + "gates" + slash + line1 + ".gat" );


		portalInfo.save();

		portalInfo.setName(oldName);
		portalInfo.setFileName(oldFileName);

		loadPortalInfo();

		portalInfo.drawClosed();
		portalInfo.portalWorld = mainWorld;

		if( portalInfo.portalDuration == -1 ) {
			buttonPress(block, player);
		}



	}

	PortalInfo netherGate() {

		PortalInfo netherGate = new PortalInfo();

		netherGate.portalName = "Nether";

		netherGate.portalWorld = mainWorld;

		netherGate.mistID = 90;

		netherGate.blockLookup.put( '.', 0 );
		netherGate.blockLookup.put( '*', 0 );
		netherGate.blockLookup.put( '-', 49 );
		netherGate.blockLookup.put( 'X', 49 );

		netherGate.blockTypes.put( new IntLocation( 1 , 0 , 0, mainWorld), 49);
		netherGate.blockTypes.put( new IntLocation( 2 , 0 , 0, mainWorld), 49);

		netherGate.blockTypes.put( new IntLocation( 0 , -1 , 0, mainWorld), 49);
		netherGate.blockTypes.put( new IntLocation( 3 , -1 , 0, mainWorld), 49);

		netherGate.blockTypes.put( new IntLocation( 0 , -2 , 0, mainWorld), 49);
		netherGate.blockTypes.put( new IntLocation( 3 , -2 , 0, mainWorld), 49);
		netherGate.signBlocks.put( new IntLocation( 0 , -2 , 1, mainWorld), 49);
		netherGate.signBlocks.put( new IntLocation( 3 , -2 , 1, mainWorld), 49);

		netherGate.blockTypes.put( new IntLocation( 0 , -3 , 0, mainWorld), 49);
		netherGate.blockTypes.put( new IntLocation( 3 , -3 , 0, mainWorld), 49);

		netherGate.blockTypes.put( new IntLocation( 1 , -4 , 0, mainWorld), 49);
		netherGate.blockTypes.put( new IntLocation( 2 , -4 , 0, mainWorld), 49);

		netherGate.portalBlocks.put(new IntLocation( 1 , -1 , 0, mainWorld), 90);
		netherGate.portalBlocks.put(new IntLocation( 2 , -1 , 0, mainWorld), 90);

		netherGate.portalBlocks.put(new IntLocation( 1 , -2 , 0, mainWorld), 90);
		netherGate.portalBlocks.put(new IntLocation( 2 , -2 , 0, mainWorld), 90);

		netherGate.portalBlocks.put(new IntLocation( 1 , -3 , 0, mainWorld), 90);
		netherGate.portalBlocks.put(new IntLocation( 2 , -3 , 0, mainWorld), 90);

		netherGate.exitPoint = new IntLocation( 1, -3 , 0, mainWorld );

		netherGate.blockTypes.putAll(netherGate.portalBlocks);

		netherGate.portalDuration = -1;

		netherGate.autoCreate = true;

		return netherGate;
	}

	PortalInfo waterCurtainGate() {

		PortalInfo waterCurtain = new PortalInfo();

		waterCurtain.portalName = "WaterCurtain";

		waterCurtain.portalWorld = mainWorld;

		waterCurtain.mistID = 8;

		waterCurtain.blockLookup.put( '.', 0 );
		waterCurtain.blockLookup.put( '*', 0 );
		waterCurtain.blockLookup.put( '-', 1 );
		waterCurtain.blockLookup.put( 'X', 1 );

		int counter;
		for( counter = -2;counter<=0;counter++) {
			waterCurtain.blockTypes.put(new IntLocation(0,counter,0, mainWorld), 1);
			waterCurtain.blockTypes.put(new IntLocation(4,counter,0, mainWorld), 1);
			waterCurtain.portalBlocks.put(new IntLocation(1,counter,0, mainWorld), 1);
			waterCurtain.portalBlocks.put(new IntLocation(2,counter,0, mainWorld), 1);
			waterCurtain.portalBlocks.put(new IntLocation(3,counter,0, mainWorld), 1);

		}

		waterCurtain.signBlocks.put(new IntLocation(0,-1,1, mainWorld), 1);
		waterCurtain.signBlocks.put(new IntLocation(4,-1,1, mainWorld), 1);

		waterCurtain.blockTypes.putAll(waterCurtain.portalBlocks);

		waterCurtain.exitPoint = new IntLocation( 2 , -2 , 0, mainWorld );

		waterCurtain.portalDuration = 10000;

		return waterCurtain;
	}

	PortalInfo bindStone() {


		PortalInfo bindStone = new PortalInfo();

		bindStone.portalName = "BindStone";

		bindStone.portalWorld = mainWorld;

		bindStone.mistID = 0;

		bindStone.blockLookup.put( 'X', 4 );
		bindStone.blockLookup.put( '-', 4 );
		bindStone.blockLookup.put( '*', 0 );
		bindStone.blockLookup.put( '.', 0 );

		bindStone.blockTypes.put( new IntLocation( 1 , 0 , 1, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 1 , -1 , 1, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 1 , -2 , 1, mainWorld ), 4 );

		bindStone.blockTypes.put( new IntLocation( 1 , -3 , 1, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 0 , -3 , 1, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 2 , -3 , 1, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 1 , -3 , 2, mainWorld ), 4 );
		bindStone.blockTypes.put( new IntLocation( 1 , -3 , 0, mainWorld ), 4 );

		bindStone.signBlocks.put( new IntLocation( 1 , -1, 2, mainWorld), 43);

		bindStone.portalBlocks.put(new IntLocation( 1 , -2 , 3, mainWorld ) , 0 );
		bindStone.portalBlocks.put(new IntLocation( 1 , -1 , 3, mainWorld ) , 0 );

		bindStone.blockTypes.putAll(bindStone.portalBlocks);

		bindStone.exitPoint = new IntLocation( 1 , -2 , 3, mainWorld );

		bindStone.portalDuration = -1;

		bindStone.bindStone = true;

		return bindStone;

	}

	PortalInfo templeGate() {

		PortalInfo templeGate = new PortalInfo();

		templeGate.portalName = "Temple";

		templeGate.portalWorld = mainWorld;

		templeGate.mistID = 8;

		templeGate.blockLookup.put( '.', 0 );
		templeGate.blockLookup.put( '*', 0 );
		templeGate.blockLookup.put( '-', 4 );
		templeGate.blockLookup.put( 'X', 4 );
		templeGate.blockLookup.put( '^', 43 );
		templeGate.blockLookup.put( '_', 44 );


		int x,y,z;

		for( x = 0; x < 7; x++ ) 
			for( z = 0; z < 11; z++ ) {

				templeGate.blockTypes.put( new IntLocation( x , -1 , z , mainWorld), 44);

				templeGate.blockTypes.put( new IntLocation( x , -5 , z , mainWorld), 44 );

			}

		for( x = 1; x < 6; x++ ) 
			for( z = 0; z < 11; z++ ) {

				templeGate.blockTypes.put( new IntLocation( x , -1 , z , mainWorld), 43);
				if( z != 10 && z !=0 ) {
					templeGate.blockTypes.put( new IntLocation( x , -5 , z , mainWorld), 43);
				}

			}

		templePillar( 3 , -4 , 1 , templeGate.blockTypes);

		for( z = 1; z < 10; z+=2 ) {

			templePillar( 1 , -4 , z , templeGate.blockTypes );
			templePillar( 5 , -4 , z , templeGate.blockTypes );

		}

		for( z = 0; z < 11; z++ ) {

			templeGate.blockTypes.put( new IntLocation( 2 , 0 , z , mainWorld), 44);
			templeGate.blockTypes.put( new IntLocation( 3 , 0 , z , mainWorld), 43);
			templeGate.blockTypes.put( new IntLocation( 4 , 0 , z , mainWorld), 44);

		}

		for( x=2; x<5;x++ ) 
			for( y=-4;y<-1;y++)
				for( z = 5;z<6;z++) {
					templeGate.portalBlocks.put(new IntLocation( x, y, z, mainWorld), 1);
				}


		templeGate.signBlocks.put( new IntLocation( 1 , -3, 10, mainWorld), 43);
		templeGate.signBlocks.put( new IntLocation( 5 , -3, 10, mainWorld), 43);

		templeGate.blockTypes.putAll(templeGate.portalBlocks);

		templeGate.exitPoint = new IntLocation( 3 , -4 , 5, mainWorld );

		templeGate.portalDuration = 10000;

		return templeGate;
	}

	void templePillar( int x , int y , int z , HashMap<IntLocation, Integer> hashMap ) {

		for( int py = y; py < y + 3; py++ ) {

			hashMap.put(new IntLocation( x , py , z , mainWorld), 4);

		}

	}
	
	PortalInfo cartGate() {

		PortalInfo cartGate = new PortalInfo();

		cartGate.portalName = "CartGate";

		cartGate.portalWorld = mainWorld;

		cartGate.mistID = 66;

		cartGate.blockLookup.put( '.', 0 );
		cartGate.blockLookup.put( '*', 0 );
		cartGate.blockLookup.put( '-', 1 );
		cartGate.blockLookup.put( 'X', 1 );

		int counter;
		for( counter = 0;counter<=4;counter++) {
			cartGate.blockTypes.put(new IntLocation(counter,-4,0, mainWorld), 1);
		}
		for( counter = -3;counter<=-1;counter++) {
			cartGate.blockTypes.put(new IntLocation(0,counter,0, mainWorld), 1);
			cartGate.blockTypes.put(new IntLocation(4,counter,0, mainWorld), 1);
		}
		for( counter = 0;counter<=4;counter++) {
			cartGate.blockTypes.put(new IntLocation(counter,0,0, mainWorld), 1);
		}

		cartGate.portalBlocks.put(new IntLocation( 2 , -3 , 0, mainWorld ) , 0 );

		cartGate.exitPoint = new IntLocation( 2 , -3 , 0, mainWorld );
		
		cartGate.signBlocks.put(new IntLocation(0,-2,1, mainWorld), 1);
		cartGate.signBlocks.put(new IntLocation(4,-2,1, mainWorld), 1);

		cartGate.blockTypes.putAll(cartGate.portalBlocks);
		
		cartGate.portalDuration = -1;

		return cartGate;
	}

	/*	static void processDelayedMotions( HashMap<Integer,Location> delayedMotions, int id , double finalx, double finaly , double finalz, double finalRotX, double finalRotY,  double finalsx, double finalsy, double finalsz ) {

		System.out.println( "Attempting delayed action");

		if( !delayedMotions.containsKey(id)) return;

		List<BaseVehicle> vehicles = etc.getServer().getVehicleEntityList();

		for( BaseVehicle current : vehicles ) {

			if( current.getId() == id ) {

				current.getPassenger().teleportTo(finalx, finaly, finalz, (float)finalRotX, (float)finalRotY);

				current.teleportTo(finalx, finaly, finalz, (float)finalRotX, (float)finalRotY);

				current.setMotion( finalsx , finalsy , finalsz );

			}

		}

		delayedMotions.remove(id);

	}
	 */


}
