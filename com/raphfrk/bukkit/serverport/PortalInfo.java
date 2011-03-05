package com.raphfrk.bukkit.serverport;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.World;

public class PortalInfo {
	
	final ServerPortBukkit plugin = (ServerPortBukkit)MyServer.plugin;

	final int SIGN = 68;
	final int BUTTON = 77;
	
	String worldDir = null;

	MyServer server = MyServer.getServer();

	String[] portalString = null;
		
	static final String slash = System.getProperty("file.separator");

	int x=0, y=0, z=0;
	int x2=0, y2=0, z2=0;

	int dx=1, dz=0;

	int odx = dx;
	int odz = dz;
	int ox = x;
	int oy = y;
	int oz = z;

	int sx=0, sy=0, sz=0;

	int mistID = 49;
	int offID = 0;
	int portalDuration = 30000;
	long portalEndtime = -1;

	boolean autoCreate = false;

	boolean bindStone = false;

	String portalFileName = "No filename set";

	String portalName = "noname";
	String portalActualName = "noname";
	String portalType = "";
	String owner = "";
	String targetServer = "";
	String targetGate = "";
	String portalWorld = "_default";
	String targetWorld = "_default";

	HashMap<IntLocation,Integer> blockTypes = new HashMap<IntLocation,Integer>();
	HashMap<IntLocation,Integer> portalBlocks = new HashMap<IntLocation,Integer>();
	HashMap<IntLocation,Integer> signBlocks = new HashMap<IntLocation,Integer>();

	HashMap<Character,Integer> blockLookup = new HashMap<Character,Integer>();

	IntLocation exitPoint = new IntLocation(0,0,0,"");
	boolean exitPointSet = false;

	boolean linearGate = false;
	boolean innerZone     = false;

	private boolean portalActive = false;

	protected static final Logger log = Logger.getLogger("Minecraft");

	void save() {
		
		if( portalFileName.equals("No filename set") ) {
			MiscUtils.safeLogging(log, "[ServerPort] unable to save gate as no filename set" );
			return;
		}
		
		String pathString = new File(portalFileName).getParent();
		
		if( pathString != null ) {
			File path = new File(pathString);
			if( !path.exists() ) {
				path.mkdirs();
		}

		ArrayList<String> portalString = new ArrayList<String>();

		if(!portalName.equals("noname"))       portalString.add( "name=" + portalName );
		if(!portalActualName.equals("noname")) portalString.add( "signname=" + portalActualName);
		if(!portalType.equals(""))             portalString.add( "type=" + portalType );
		if( x!=0 )                             portalString.add( "posx=" + x );
		if( y!=0 )                             portalString.add( "posy=" + y );
		if( z!=0 )                             portalString.add( "posz=" + z );
		if( x2!=0 )                            portalString.add( "posx2=" + x2 );
		if( y2!=0 )                            portalString.add( "posy2=" + y2 );
		if( z2!=0 )                            portalString.add( "posz2=" + z2 );
		if( dx!=1 )                            portalString.add( "dx=" + dx );
		if( dz!=0 )                            portalString.add( "dz=" + dz );
		portalString.add( "mist=" + mistID );
		portalString.add( "mistoff=" + offID );
		if( portalActive )                     portalString.add( "active=" + (portalActive?1:0) );
		if( !owner.equals("") )                portalString.add( "owner=" + owner );
		if( !targetServer.equals("") )         portalString.add( "targetserver=" + targetServer );
		if( !targetGate.equals("") )           portalString.add( "targetgate=" + targetGate );
		if( portalDuration != 30000 )          portalString.add( "duration=" + portalDuration );
		if( autoCreate )                       portalString.add( "autocreate=" + autoCreate );
		if( bindStone )                        portalString.add( "bind=" + bindStone );
		if( linearGate )                       portalString.add( "lineargate=" + linearGate );
		if( innerZone )                        portalString.add( "innerzone=" + innerZone );
		if(!portalWorld.equals("_default"))    portalString.add( "portalworld=" + portalWorld );
		if(!targetWorld.equals("_default"))    portalString.add( "targetworld=" + targetWorld );
		
		// 4 linear gates should be created for the outer edges
		// 1 "innerZone" for cities

		HashMap<Integer,Character> reverseLookup = new HashMap<Integer,Character>();

		Iterator<Character> itr = blockLookup.keySet().iterator();

		while( itr.hasNext() ) {

			Character ch = itr.next();

			portalString.add( ch + "=" + blockLookup.get(ch));

			if( ch != '-' && ch != '*' ) {
				reverseLookup.put( blockLookup.get(ch), ch);
			}

		}

		portalString.add("");

		int px=0;
		int py=0;
		int pz=0;

		Set<IntLocation> keys = blockTypes.keySet();

		SortedSet<IntLocation> keysSorted = new TreeSet<IntLocation>( keys ); 

		Iterator<IntLocation> itr2 = keysSorted.iterator();

		StringBuilder line = new StringBuilder();

		while( itr2.hasNext() ) {

			IntLocation current = itr2.next();


			if(
					current.getZ() < pz || 
					( 
							current.getZ() == pz && 
							current.getY() > py
					) 
			) {

				MiscUtils.safeLogging(log, "[ServerPort] Blocks (Z) sorted incorrectly when saving gates" );
				return;

			}

			if( 
					current.getZ() == pz && (
							current.getY() > py || 
							( 
									current.getY() == py && 
									current.getX() < px
							)
					)

			) {
				MiscUtils.safeLogging(log, "[ServerPort] Blocks sorted incorrectly when saving gates" );
				return;
			}

			while( current.getZ() > pz ) {

				portalString.add(line.toString());
				portalString.add(">>>");
				line = new StringBuilder();
				px = 0;
				py = 0;
				pz++;

			}

			while( current.getY() < py ) {
				portalString.add(line.toString());
				px = 0;
				py--;
				line = new StringBuilder("");
			}

			while( px < current.getX() ) {
				line.append(" ");
				px++;
			}

			IntLocation firstBlock;
			String tempWorld = null;

			if( signBlocks != null ) {
				Set<IntLocation> blockSet = signBlocks.keySet();
				if( blockSet != null ) {
					Iterator<IntLocation> itr3 = blockSet.iterator();
					if(itr3.hasNext()) {
						firstBlock = itr3.next();
						if( firstBlock != null ) {
							tempWorld = firstBlock.getWorldName();
						} 
					}
				}
			}


			if( tempWorld == null && portalBlocks != null ) {
				Set<IntLocation> blockSet = portalBlocks.keySet();
				if( blockSet != null ) {
					Iterator<IntLocation> itr3 = blockSet.iterator();
					if(itr3.hasNext()) {
						firstBlock = itr3.next();
						if( firstBlock != null ) {
							tempWorld = firstBlock.getWorldName();
						} 
					}
				}
			}
						
			if( tempWorld == null ) {
				tempWorld = portalWorld;
			}

			if( exitPoint.equals(current) ) {
				line.append("*");
			} else if( signBlocks.containsKey((new IntLocation( current.getX(), current.getY() , current.getZ() + 1 , tempWorld ) ) ) ) {
				line.append("-");
			} else if( portalBlocks.containsKey((new IntLocation( current.getX(), current.getY() , current.getZ() , tempWorld ))) ) {
				line.append(".");
			} else {
				line.append(reverseLookup.get(blockTypes.get(current)));
			}
			px++;

		}

		portalString.add(line.toString());

		MiscUtils.stringToFile( portalString , portalFileName  );
		}


	}

	void load( String worldName, String filename ) {
								
		worldDir = worldName;
		
		portalString = MiscUtils.fileToString( filename );

		portalFileName = filename;

		if( portalString == null ) {
			return;
		}

		parseString();

	}

	void parseString() {

		blockLookup = new HashMap<Character,Integer>();
		blockTypes = new HashMap<IntLocation,Integer>();
		portalBlocks = new HashMap<IntLocation,Integer>();
		signBlocks = new HashMap<IntLocation,Integer>();

		int cnt = 0;


		while( cnt < portalString.length && portalString[cnt].length() != 0 ) {

			String[] currentParameter = MiscUtils.splitParam( portalString[cnt] );

			if( currentParameter == null ) {
				MiscUtils.safeLogging(log, "[ServerPort] Unable to parse line " + (cnt+1) + " (" + 
						portalFileName + ")"  );
				return;
			}

			if( currentParameter[0].equalsIgnoreCase("posx")) {
				x = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("posy")) {
				y = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("posz")) {
				z = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("posx2")) {
				x2 = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("posy2")) {
				y2 = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("posz2")) {
				z2 = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("dx")) {
				dx = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("dz")) {
				dz = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("active")) {
				portalActive = MiscUtils.getInt( currentParameter[1] )==1;
			} else if( currentParameter[0].equalsIgnoreCase("mist")) {
				mistID = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("mistoff")) {
				offID = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("name")) {
				portalName = currentParameter[1].toLowerCase();
			} else if( currentParameter[0].equalsIgnoreCase("signname")) {
				portalActualName = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("type")) {
				portalType = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("owner")) {
				owner = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("portalworld")) {
				portalWorld = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("targetworld")) {
				targetWorld = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("targetgate")) {
				targetGate = currentParameter[1].toLowerCase();
			} else if( currentParameter[0].equalsIgnoreCase("targetserver")) {
				targetServer = currentParameter[1];
			} else if( currentParameter[0].equalsIgnoreCase("duration")) {
				portalDuration = MiscUtils.getInt( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("autocreate")) {
				autoCreate = MiscUtils.getBoolean( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("bind")) {
				bindStone = MiscUtils.getBoolean( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("innerzone")) {
				innerZone = MiscUtils.getBoolean( currentParameter[1] );
			} else if( currentParameter[0].equalsIgnoreCase("lineargate")) {
				linearGate = MiscUtils.getBoolean( currentParameter[1] );
			} else if( currentParameter[0].length() == 1 ) {
				blockLookup.put(
						currentParameter[0].charAt(0), 
						MiscUtils.getInt( currentParameter[1] )
				);
			}

			cnt++;

		}

		if( !blockLookup.containsKey('.') ) {
			blockLookup.put('*', 90 );
		}

		if( !blockLookup.containsKey('*') ) {

			blockLookup.put('*', blockLookup.get('.'));

		}

		if(portalWorld.equals("_default")) {
			portalWorld = MyServer.bukkitServer.getWorlds().get(0).getName();
			MiscUtils.safeLogging("Changing gate to world: " + portalWorld);
		}

		cnt++;

		int lowestY = 1;
		int lowX = 0;
		int highX = 0;

		sy = 0;
		sx = 0;
		sz = 0;

		int x = 0;
		int z = 0;
		int y = 0;

		while( cnt < portalString.length ) {

			String line = portalString[cnt];

			if( line.equals(">>>")) {
				z++;
				y = 0;
				x = 0;
				cnt++;
				continue;
			}

			int cnt2;

			for(cnt2=0;cnt2<line.length();cnt2++) {
				char currentChar = line.charAt(cnt2);
				if( currentChar != ' ' ) {
					if( !blockLookup.containsKey(currentChar) ) {
						MiscUtils.safeLogging(log, "[ServerPort] No block id given for '" + currentChar + "' (" + 
								portalFileName + ")"  );
						return;
					} else {
						blockTypes.put(
								new IntLocation( cnt2 , y, z , portalWorld),
								blockLookup.get(currentChar)
						);
					}
					if( currentChar == '-' ) {
						signBlocks.put(
								new IntLocation( cnt2 , y, z+1 , portalWorld ),
								68
						);
					} else if( currentChar == '.' || currentChar == '*') {
						int yPos = y;
						if( yPos != lowestY ) {
							lowX = cnt2;
							highX = cnt2;
							lowestY = yPos;
						} else if( cnt2 < lowX ) {
							lowX = cnt2;
						} else if( cnt2 > highX ) {
							highX = cnt2;
						}
						portalBlocks.put(
								new IntLocation( cnt2 , y, z , portalWorld ),
								mistID
						);
					}
					if( currentChar == '*' ) {
						exitPoint = new IntLocation( cnt2 , y, z , portalWorld );
						exitPointSet = true;
					}
				}
			}

			if( y < sy ) {
				sy = y;
			}

			if( highX - lowX > sx ) {
				sx = highX - lowX;
			}

			cnt++;
			y--;


		}

		sy = -sy;
		sz = z + 1;

		if( !exitPointSet && z!=0 ) {

			MiscUtils.safeLogging("[ServerPort] Custom gate format error.  Exit points must be provided for 3d gates");
			return;
		} else if( !exitPointSet ) {
			exitPointSet = true;
			exitPoint = new IntLocation( (lowX+highX)/2 , lowestY , 0 , portalWorld );
		}

		if(worldDir == null) {
			worldDir = portalWorld;
			String newFileName = portalWorld + slash + portalFileName;
			String directory = portalWorld;
			directory += slash;
			ServerPortBukkit plugin = (ServerPortBukkit)MyServer.plugin;
			directory += plugin.serverPortCommon.portalManager.portalDirectory;

			File dir = new File(directory);
			dir.mkdirs();
			
			File oldFile = new File(portalFileName);
			File newFile = new File(oldFile + ".old");
			while(newFile.exists()) {
				newFile = new File(newFile + ".old");
			}
			oldFile.renameTo(newFile);
			
			portalFileName = newFileName;
						
			save();
			load(portalWorld, portalFileName);
		} else if( !portalWorld.equals(worldDir)){
			portalWorld = worldDir;
			save();
			load(portalWorld, portalFileName);
		} else if( MyServer.bukkitServer.getWorld(targetServer) != null) {
			targetWorld = targetServer;
			targetServer = "here";
			ServerPortBukkit plugin = (ServerPortBukkit)MyServer.plugin;
			PortalInfo other = plugin.serverPortCommon.portalManager.getPortal(targetGate);
			if(other != null) {
				if(other.targetGate.equals(portalName)) {
					(new File(portalFileName)).delete();
					portalName += "_";
					portalFileName += "_";
					save();
					load(portalWorld, portalFileName);
				}
			} else {
				other = plugin.serverPortCommon.portalManager.getPortal(targetGate + "_");
				targetGate += "_";
				save();
				load(portalWorld, portalFileName);
			}
			
		}

	}

	IntLocation transform( IntLocation current ) {

		IntLocation newLoc = new IntLocation(0,0,0, portalWorld);

		newLoc.setY(  current.getY() );
		newLoc.setX(  current.getX()*dx + current.getZ()*dz);
		newLoc.setZ( -current.getX()*dz + current.getZ()*dx);

		newLoc.setX( newLoc.getX() + this.x );
		newLoc.setY( newLoc.getY() + this.y );
		newLoc.setZ( newLoc.getZ() + this.z );

		return newLoc;


	}

	HashMap<IntLocation,Integer> transform( HashMap<IntLocation,Integer> blocks , int idNum ) {

		HashMap<IntLocation,Integer> ret = new HashMap<IntLocation,Integer>();

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		while( itr.hasNext() ) {
			IntLocation current = itr.next();

			IntLocation newLoc = transform(current);

			if( idNum == -1 ) {
				ret.put(newLoc, blocks.get(current));
			} else {
				ret.put(newLoc, idNum );
			}
		}

		return ret;

	}

	IntLocation getExitPoint() {

		return transform(exitPoint);

	}

	double getDir() {

		int rot = PortalInfo.DXDZtoRot(dx, dz);
		return (rotToDir(rot) + (bindStone?180:0)) % 360;

	}

	HashMap<IntLocation,Integer> getBlocks( int idNum ) {

		return transform( blockTypes , idNum );
	}

	HashMap<IntLocation,Integer> getSigns( int idNum ) {
		return transform( signBlocks , idNum );
	}

	HashMap<IntLocation,Integer> getPortal( int idNum ) {
		return transform( portalBlocks , idNum );
	}

	String getFileName() {
		return portalFileName;
	}

	void setFileName( String portalFileName ) {

		this.portalFileName = portalFileName;

	}

	String getName() {
		return portalName;
	}

	void setName( String portalName ) {
		this.portalName = portalName;
	}

	boolean isActive() {

		long currentTime = System.currentTimeMillis();

		if( portalActive && portalDuration != -1 && currentTime > portalEndtime ) {
			deactivate();
		}

		return portalActive;
	}

	boolean testMatch( MyBlock block , HashMap<IntLocation,Integer> otherPortals ) {

		int r;

		for( r=2;r<6;r++ ) {
			if( testMatch( new IntLocation(block) , rotToDX(r) , rotToDZ(r) , otherPortals ) ) {
				return true;
			}
		}

		return false;

	}

	boolean testMatch( MySign sign , HashMap<IntLocation,Integer> otherPortals ) {

		if( sign == null ) {
			return false;
		}

		int d = server.getBlockData(sign.getBlock().getWorld(), sign.getX(),sign.getY(),sign.getZ());

		return testMatch( new IntLocation( sign.getX(),sign.getY(),sign.getZ() , sign.getBlock().getWorld().getName() ) , rotToDX(d) , rotToDZ(d) , otherPortals );

	}

	boolean testMatch( IntLocation block , int dx , int dz , HashMap<IntLocation,Integer> otherPortals ) {

		IntLocation loc = new IntLocation( block );

		this.x = 0;
		this.y = 0;
		this.z = 0;

		this.dx = dx;
		this.dz = dz;

		HashMap<IntLocation,Integer> signs = getSigns(-1);

		Iterator<IntLocation> itr = signs.keySet().iterator();

		while( itr.hasNext() ) {

			IntLocation current = itr.next();

			this.x = loc.getX()-current.getX();
			this.y = loc.getY()-current.getY();
			this.z = loc.getZ()-current.getZ();
			this.portalWorld = block.getWorldName();

			HashMap<IntLocation,Integer> gateBlocks = getBlocks(-1);

			Iterator<IntLocation> itr2 = gateBlocks.keySet().iterator();

			boolean match = true;

			while( itr2.hasNext() ) {

				IntLocation blockPos = itr2.next();

				if( 
						( otherPortals != null && otherPortals.containsKey(blockPos) ) || 
						( !MiscUtils.blockMatch( blockPos , gateBlocks.get(blockPos) ) ) 
				) {
					match = false;
					break;
				}

			}

			if( match ) {
				return true;
			}


		}

		return false;



	}


	void backupPos() {
		odx = dx;
		odz = dz;
		ox = x;
		oy = y;
		oz = z;
	}

	void restorePos() {
		dx = odx;
		dz = odz;
		x = ox;
		y = oy;
		z = oz;
	}

	IntLocation getPos() {

		return new IntLocation( this.x , this.y , this.z, portalWorld );

	}

	void refreshActiveState() {

		if( isActive() ) {

			portalActive = false;
			clear();

			if( portalDuration == -1 ) {
				activate();
			}

		} else {
			clear();
			drawClosed();
		}

	}

	void activate() {
		activate(-1);
	}

	long activate( int durationRequest ) {
		if( isActive() ) {
			if(portalDuration == -1) {
				return durationRequest;
			} else {
				return portalEndtime - System.currentTimeMillis();
			}
		}

		clear();

		drawMist();

		final int lowerDuration = ( durationRequest > 0 && durationRequest < portalDuration )? durationRequest : portalDuration;

		portalActive = true;

		portalEndtime = System.currentTimeMillis() + lowerDuration;

		this.save();

		if( portalDuration != -1 ) {
			server.addToServerQueue(new Runnable() {

				public void run() {

					if( portalActive ) {
						if( portalEndtime != -1 && System.currentTimeMillis() > portalEndtime ) {
							deactivate();
						}
					}

				}

			}, lowerDuration + 500 );

			return lowerDuration;
		} else {
			return durationRequest;
		}



	}

	void deactivate() {
		if( !portalActive ) {
			return;
		}

		portalEndtime = -1;

		clear();

		drawClosed();

		portalActive = false;

	}
	


	void destroy() {

		clear();

		HashMap<IntLocation,Integer> allBlocks = getBlocks( -1 );

		MiscUtils.blockDraw( allBlocks );

		portalActive = false;

	}

	boolean blocksChecked = false;
	
	void drawClosed() {
		
		HashMap<IntLocation,Integer> portalBlocks = getPortal( offID );

		if(!blocksChecked) {
			World world = MyServer.bukkitServer.getWorld(portalWorld);
			if(world!=null) {
				MiscUtils.fixWorld(world, portalBlocks);
				blocksChecked = true;
			}
		}
		
		MiscUtils.blockDraw(portalBlocks);

	}

	boolean hasSign( IntLocation loc ) {

		boolean otherSign = false;

		Iterator<IntLocation> itr = getSigns(-1).keySet().iterator();

		while( itr.hasNext() ) {

			IntLocation current = itr.next();

			if( MiscUtils.isChunkLoaded(current.getWorld(), current.getX(), current.getY(), current.getZ())) {

				MiscUtils.gridLoad(current.getWorld(), current.getX(), current.getY(), current.getZ());

			}

			int currentId = server.getBlockIdAt(current.getWorld(), current.getX(), current.getY(), current.getZ());

			if( currentId == SIGN && (loc == null || !loc.equals(current) ) ) {
				otherSign = true;
			}

		}

		return otherSign;

	}

	void fullDraw() {

		HashMap<IntLocation,Integer> allBlocks = getBlocks( -1 );

		MiscUtils.blockDraw(allBlocks);

		HashMap<IntLocation,Integer> signBlocks = getSigns(-1);

		Iterator<IntLocation> itr = signBlocks.keySet().iterator();

		if( !hasSign(null) && portalName.indexOf("fire_") != 0 ) {

			if( itr.hasNext() ) {
				IntLocation firstLoc = itr.next();

				if( firstLoc != null ) {

					String[] text = { "" , portalName , targetServer , targetGate.equals(portalName)?"":targetGate };

					MiscUtils.placeSign(firstLoc.getWorld(), text, firstLoc.getX(), firstLoc.getY(), firstLoc.getZ(), DXDZtoRot(dx, dz));

				}

				while( itr.hasNext() ) {
					MiscUtils.placeBlock( itr.next() , 0 );
				}

			}
		}


	}

	boolean testDraw(HashMap<IntLocation,Integer> portals) {

		return testDraw( getBlocks( -1 ) , portals );

	}

	boolean testDraw( HashMap<IntLocation,Integer> blocks , HashMap<IntLocation,Integer> portals ) {

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		while( itr.hasNext() ) {
			IntLocation current = itr.next();

			if( portals.containsKey(current)) {
				return false;
			}

			int y = current.getY();

			if( y > 126 || y < 1 ) {
				return false;
			}
		}

		return true;

	}

	void drawMist() {

		HashMap<IntLocation,Integer> portalBlocks = getPortal( mistID );

		MiscUtils.blockDraw(portalBlocks);
	}

	void clear() {

		HashMap<IntLocation,Integer> portalBlocks = getPortal( 0 );

		MiscUtils.blockDraw( portalBlocks );

	}

	void toggleActive() {
		if( isActive() ) {
			deactivate();
		} else {
			activate();
		}
	}

	static int dirToRot( double dir ) {

		// dx,dz are the coefficients for drawing the gate
		// dir is the compass direction from Location
		// rot is the sign data direction

		// dx=1 , dz=0  <-> dir = 0   <-> rot = 3
		// dx=0 , dz=1  <-> dir = 270 <-> rot = 5
		// dx=0 , dz=-1 <-> dir = 90  <-> rot = 4
		// dx=-1, dz=0  <-> dir = 180 <-> rot = 2 

		int rounded =  (int)(Math.round( dir / 90 ) * 90);

		rounded = rounded % 360;
		if( rounded < 0 ) {
			rounded += 360;
		}

		if( rounded == 0 ) {
			return 3;
		} else if ( rounded == 90 ) {
			return 4;
		} else if ( rounded == 180 ) {
			return 2;
		} else if ( rounded == 270 ) {
			return 5;
		} else {
			MiscUtils.safeLogging("[ServerPort] Error in dir to rot conversion");
			return 3;
		}

	}

	static double rotToDir( int rot ) {

		// dx,dz are the coefficients for drawing the gate
		// dir is the compass direction from Location
		// rot is the sign data direction

		// dx=1 , dz=0  <-> dir = 0   <-> rot = 3
		// dx=0 , dz=1  <-> dir = 270 <-> rot = 5
		// dx=0 , dz=-1 <-> dir = 90  <-> rot = 4
		// dx=-1, dz=0  <-> dir = 180 <-> rot = 2 

		if( rot == 3 ) return 0;
		if( rot == 4 ) return 90;
		if( rot == 2 ) return 180;
		if( rot == 5 ) return 270;
		return 0;

	}

	static int rotToDX( int rot ) {

		if( rot == 2 ) {
			return -1;
		} else if ( rot == 3 ) {
			return 1;
		} else if ( rot == 4 ) {
			return 0;
		} else {
			return 0;
		} 
	}

	static int rotToDZ( int rot ) {

		if( rot == 2 ) {
			return 0;
		} else if ( rot == 3 ) {
			return 0;
		} else if ( rot == 4 ) {
			return -1;
		} else {
			return 1;
		} 
	}

	static int DXDZtoRot( int dx , int dz ) {

		if( dx == 0 ) {
			return (dz == 1)?5:4;
		} else {
			return (dx == 1)?3:2;
		}


	}
	
	String getServerName() {
		return plugin.serverPortCommon.communicationManager.serverPortServer.serverName;
	}


}
