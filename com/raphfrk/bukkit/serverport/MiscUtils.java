package com.raphfrk.bukkit.serverport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;


public class MiscUtils {

	static final String slash = System.getProperty("file.separator");

	protected static final Logger log = Logger.getLogger("Minecraft");
	static Object logSync = new Object();

	static MyServer server = MyServer.getServer();

	static void safeLogging( String message ) {
		safeLogging( log , message );
	}

	static public SecureRandom random = new SecureRandom();

	static void safeLogging( Logger log , String message ) {

		synchronized( logSync ) {
			log.info( message );
		}

	}


	static void safeMessage( MyPlayer player , String message ) {

		safeMessage( player.getName(), message );


	}

	static void safeMessage( String playerName , String message ) {

		final String finalMessage = message;
		final String finalPlayer = playerName;

		server.addToServerQueue( new Runnable() {

			public void run() {

				MyPlayer player = null;

				if( finalPlayer != null ) {
					player = server.getPlayer(finalPlayer);
				}

				if( player != null && !player.isNull() ) {
					player.sendMessage(finalMessage);
				}

			}

		});

	}

	static HashMap<String,HashMap> fileToMap(String filename, boolean forceLowerCase) {

		String[] strings = fileToString(filename);

		HashMap<String,HashMap> map = new HashMap<String,HashMap>();

		for(int cnt=0;cnt<strings.length;cnt++) {

			String string = strings[cnt].trim();

			if(string.startsWith("#")) {
				continue;
			}

			if(forceLowerCase) {
				string = string.toLowerCase();
			}

			recursiveToSet(string, map);

		}
		
		return map;

	}

	static void recursiveToSet(String string,  HashMap<String,HashMap> top) {

		//System.out.println("String: " + string);
		//System.out.println("top:    " + (top==null?"null":top.keySet()));
		
		if(string == null) {
			return;
		}

		String[] split = string.split(",", 2);
		//System.out.println("split:  " + Arrays.toString(split));

		if(split.length<=1) {
			if(!top.containsKey(string)) {
				top.put(string.trim(), null);
			}
		} else {
			if(!top.containsKey(split[0])) {
				top.put(split[0].trim(), new HashMap<String,HashMap>());
			}
			recursiveToSet(split[1],top.get(split[0].trim()));
		}

	}

	static boolean allowed(String[] values, int pos, HashMap<String,HashMap> map, boolean forceLowerCase) {

		if(map==null && pos==(values.length)) {
			return true;
		} else if(map==null) {
			return false;
		} else if(pos>=values.length) {
			return false;
		}

		String current = (forceLowerCase)?(values[pos].toLowerCase()):(values[pos]);

		if(map.containsKey("*")) {
			if(allowed(values,pos+1,map.get("*"), forceLowerCase)) {
				return true;
			}
		}

		if(map.containsKey(current)) {
			if(allowed(values,pos+1,map.get(current), forceLowerCase)) {
				return true;
			}
		}

		if(values[pos].trim().equals("*")) {
			Iterator<String> itr = map.keySet().iterator();
			while(itr.hasNext()) {
				if(allowed(values,pos+1,map.get(itr.next()), forceLowerCase)) {
					return true;
				}
			}
		}

		return false;

	}

	static HashSet<String> fileToSet( String filename , boolean forceLowerCase ) {

		String filePath = "serverport" + slash + filename;

		try {
			(new File( filePath )).createNewFile();
		} catch (IOException e) {}

		String[] list = fileToString( filePath );

		HashSet<String> set = new HashSet<String>();

		for( String current : list ) {
			if(current.trim().startsWith("#")) {
				continue;
			}
			if( forceLowerCase ) {
				set.add(current.toLowerCase().trim());
			} else {
				set.add(current.trim());
			}
		}

		return set;

	}

	static void stringToFile( ArrayList<String> string , String filename ) {

		File portalFile = new File( filename );

		BufferedWriter bw;

		try {
			bw = new BufferedWriter(new FileWriter(portalFile));
		} catch (FileNotFoundException fnfe ) {
			MiscUtils.safeLogging(log, "[Serverport] Unable to write to gate file: " + filename );
			return;
		} catch (IOException ioe) {
			MiscUtils.safeLogging(log, "[Serverport] Unable to write to gate file: " + filename );
			return;
		}

		try {
			for( Object line : string.toArray() ) {
				bw.write((String)line);
				bw.newLine();
			}
			bw.close();
		} catch (IOException ioe) {
			MiscUtils.safeLogging(log, "[Serverport] Unable to write to gate file: " + filename );
			return;
		}

	}

	static String[] fileToString( String filename ) {

		File portalFile = new File( filename );

		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(portalFile));
		} catch (FileNotFoundException fnfe ) {
			MiscUtils.safeLogging(log, "[Serverport] Unable to open gate file: " + filename );
			return null;
		} 

		StringBuffer sb = new StringBuffer();

		String line;

		try {
			while( (line=br.readLine()) != null ) {
				sb.append( line );
				sb.append( "\n" );

			}
			br.close();
		} catch (IOException ioe) {
			MiscUtils.safeLogging( log , "[Serverport] Error reading file: " + filename );
			return null;
		}

		return( sb.toString().split("\n") );
	}

	static String[] splitParam( String line ) {

		String[] split = line.split("=",-1);

		if( split.length < 2 ) {
			MiscUtils.safeLogging( log , "[Serverport] Unable to parse parameter from: " + line );
			return null;
		}

		String[] ret = new String[2];

		ret[0] = split[0];

		ret[1] = line.substring(ret[0].length() + 1 );

		return ret;


	}

	static boolean blockMatch( IntLocation loc , int id ) {

		int x = loc.getX();
		int y = loc.getY();
		if( y > 127 || y < 0 ) {
			return false;
		}
		int z = loc.getZ();

		return server.getBlockIdAt(loc.getWorld(), x, y, z) == id;

	}

	static boolean getBoolean( String var ) {

		if( 
				var.equalsIgnoreCase("true") || 
				var.equals("1") || 
				var.matches("^[tT].*$") ) {
			return true;
		}
		return false;

	}

	static boolean isBoolean( String var ) {

		if( 
				var.equalsIgnoreCase("true") || 
				var.equalsIgnoreCase("false") || 
				var.equals("1") || 
				var.equals("0") ||
				var.matches("^[tT].*$") ||
				var.matches("^[fF].*$") ) {
			return true;
		}
		return false;

	}

	static int getInt( String var ) {

		try {
			var = var.trim();
			int x = Integer.parseInt(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[Serverport] Unable to parse " + var + " as integer" );
			return 0;
		}

	}

	static boolean isInt (String string) {

		try {
			Integer.parseInt(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}

	static long getLong( String var ) {

		try {
			var = var.trim();
			long x = Long.parseLong(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[Serverport] Unable to parse " + var + " as Long" );
			return 0;
		}

	}

	static boolean isLong (String string) {

		try {
			Long.parseLong(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}

	static Double getDouble( String var ) {

		try {
			var = var.trim();
			double x = Double.parseDouble(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[Serverport] Unable to parse " + var + " as Double" );
			return 0.0;
		}

	}

	static boolean isDouble (String string) {

		try {
			Double.parseDouble(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}

	static boolean checkText( String text ) {

		if( text.length() > 15 ) {
			return false;
		}

		return text.matches("^[a-zA-Z0-9\\.\\-]+$");
	}

	static int getSafeExit( IntLocation startExit , TreeMap<String,Boolean> softBlocks ) {

		gridLoad(startExit.getWorld(), startExit.getX(), startExit.getY(), startExit.getZ());

		IntLocation temp = new IntLocation( startExit );

		String id = "none";
		while( temp.y > 2 && ( softBlocks.containsKey( id = ((Integer)server.getBlockIdAt(startExit.getWorld(), temp.x, temp.y-1, temp.z)).toString() ) ))  {

			temp.y--;

		}

		String id1 = "none";
		String id2 = "none";

		while
			( temp.y < 125 && 
					( 
							!softBlocks.containsKey( id1 = ((Integer)server.getBlockIdAt(startExit.getWorld(),temp.x, temp.y, temp.z)).toString() ) ||
							!softBlocks.containsKey( id2 = ((Integer)server.getBlockIdAt(startExit.getWorld(),temp.x, temp.y+1, temp.z)).toString() ) 

					)
			) {
			temp.y++;

		}

		int shift = temp.y - startExit.y;

		return shift;

	}

	static String checkRules(String name) {
		return name + "must be less than 16 characters and contain a-z, A-Z or . -";
	}

	static void placeSign( World world, String[] text , int x , int y , int z , int d ) {

		if( !isChunkLoaded(world, x, y, z)) {
			server.loadChunk(world, x, y, z);
		}

		if( y > 126 || y < 1 ) {
			return;
		} else {		
			server.setBlockAt(world, 68, x, y, z);

			server.setBlockData(world, x, y, z, d);
		}

		//Sign sign = (Sign)server.getComplexBlock(x,y,z);

		//if( text.length <= 4 ) {
		//	int cnt = 0;
		//	for( String line : text ) {
		//		sign.setText(cnt++, line);
		//	}
		//}

	}


	static void placeBlock( IntLocation loc , int id ) {

		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		World world = loc.getWorld();

		if( !isChunkLoaded(world, x, y, z)) {
			server.loadChunk(world, x, y, z);
		}

		if( isChunkLoaded(world, x, y, z)) {
			if( y > 0 && y < 127 ) {
				server.setBlockAt(world, id, x, y, z);
			}
		}

	}

	static void fixWorld(World world, HashMap<IntLocation,Integer> blocks ) {

		int worldIndex = IntLocation.getWorldIndex(world);

		if( worldIndex > -1 ) {
			for( IntLocation loc : blocks.keySet() ) {
				loc.setWorldIndex(worldIndex);
			}
		}

	}

	static void blockDraw( HashMap<IntLocation,Integer> blocks ) {

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		HashMap<IntLocation,Boolean> chunks = new HashMap<IntLocation,Boolean>();

		while( itr.hasNext() ) {

			IntLocation loc = itr.next();

			World world = loc.getWorld();

			world.getName();

			IntLocation chunkHash = new IntLocation( loc.getX()>>4 , 0 , loc.getZ()>>4 , world.getName() );


			if( !chunks.containsKey(chunkHash) ) {
				chunks.put(chunkHash, true);
				if( !isChunkLoaded(world, loc.getX(),loc.getY(),loc.getZ())) {
					server.loadChunk(world, loc.getX(),loc.getY(),loc.getZ());
				}
			}

			int blockType = blocks.get(loc);

			if( loc.getY() < 127 && loc.getY() > 0 ) {
				if( loc != null && server != null && !server.isNull() ) {
					server.setBlockAt(world, blockType, loc.getX(), loc.getY(), loc.getZ() );
				}
			}
		}

	}

	static boolean fileCheck( String world, HashMap<IntLocation,Integer> blocks ) {

		World worldInstance = MyServer.bukkitServer.getWorld(world);

		if(worldInstance.getEnvironment().equals(Environment.NETHER)) {
			world = "DIM-1" + slash + world;
		}

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		HashMap<IntLocation,Boolean> chunks = new HashMap<IntLocation,Boolean>();

		boolean exists = true;

		while( itr.hasNext() ) {

			IntLocation loc = itr.next();

			IntLocation chunkHash = new IntLocation( loc.getX()>>4 , 0 , loc.getZ()>>4, loc.getWorldName() );

			if( !chunks.containsKey(chunkHash) ) {
				chunks.put(chunkHash, true);
				if( !fileCheck(loc.getWorldName() , loc.getX(),loc.getY(),loc.getZ())) {
					exists = false;
				}
			}

		}

		return exists;


	}

	static boolean fileCheck( String world, int x , int y , int z ) {

		World worldInstance = MyServer.bukkitServer.getWorld(world);

		if(worldInstance.getEnvironment().equals(Environment.NETHER)) {
			world = world + slash + "DIM-1";
		}

		int cx = x >> 4;
		int cz = z >> 4;

		String folder1 = base36( mod( cx , 64 ));
		String folder2 = base36( mod( cz , 64 ));

		String filename = "c." + base36( cx ) + "." + base36( cz ) + ".dat";

		String fullPath = world + slash + folder1 + slash + folder2 + slash + filename;

		MiscUtils.safeLogging("[ServerPort] Chunk exists test: checking if " + fullPath + " exists");

		boolean exists = (new File( fullPath )).exists();

		return exists;

	}

	static boolean generatedTest( String world, HashMap<IntLocation,Integer> blocks ) {

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		HashMap<IntLocation,Boolean> chunks = new HashMap<IntLocation,Boolean>();

		while( itr.hasNext() ) {

			IntLocation loc = itr.next();

			IntLocation chunkHash = new IntLocation( loc.getX()>>4 , 0 , loc.getZ()>>4, loc.getWorldName() );

			if( !chunks.containsKey(chunkHash) ) {
				chunks.put(chunkHash, true);
				gridLoad(loc.getWorld() , loc.getX(),loc.getY(),loc.getZ());
			}

		}

		return true;


	}

	static boolean generatedTest( World world, int x , int y , int z ) {

		boolean exists = fileCheck( world.getName(), x, y, z );

		boolean chunkLoaded = isChunkLoaded(world, x,y,z);

		if( !chunkLoaded || !exists ) {
			if( !exists ) {
				MiscUtils.safeLogging("[ServerPort] Chunk file not found" );
			}
			MiscUtils.safeLogging("[ServerPort] Chunk loaded check: " + chunkLoaded );

			if( !chunkLoaded ) {

			}
			MiscUtils.safeLogging("[ServerPort] Executing chunk grid load: " + new IntLocation( x , y , z , world.getName()));
			gridLoad( world, x, y, z );
			return false;
		} 

		return exists;

	}

	static boolean isChunkLoaded( World world, int x , int y , int z ) {

		int id = server.getBlockIdAt(world, x, 0, z); 

		return id != 0;


	}

	static void gridLoad( World world, int x , int y , int z ) {

		server.loadChunk(world, x+16,y,z+16);
		server.loadChunk(world, x+16,y,z);
		server.loadChunk(world, x+16,y,z-16);
		server.loadChunk(world, x+00,y,z+16);
		server.loadChunk(world, x+00,y,z);
		server.loadChunk(world, x+00,y,z-16);
		server.loadChunk(world, x-16,y,z+16);
		server.loadChunk(world, x-16,y,z);
		server.loadChunk(world, x-16,y,z-16);

	}

	static boolean loadCircle = true;

	static void stopCircle(String playerName) {

		if( !loadCircle ) {
			MiscUtils.safeMessage(playerName , "[ServerPort] Circle load not in progress");
		}

		loadCircle = false;

	}

	static void loadCircle( World world, String playerName , int x , int y , int z , int r ) {

		if( r <= 0 ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] Radius must be positive" );
			return;
		} else if(world == null) {
			MiscUtils.safeMessage(playerName, "[ServerPort] Unknown world" );
			return;
		}

		loadCircle = true;

		MiscUtils.safeLogging("[ServerPort] Beginning circle load at centre " + x + ", " + z + " (" + world.getName() + ") with radius " + r );

		MiscUtils.safeMessage(playerName, "[ServerPort] Beginning circle load at centre " + x + ", " + z + " (" + world.getName() + ") with radius " + r );

		int rQuan = ((int)Math.ceil(r/16))*16;

		loadCircle( world, playerName , -rQuan , x , y , z , r , z-10*r);

	}

	static void loadCircle( World world, String playerName , int px , int x , int y, int z , int r , int last) {

		long startTime = System.currentTimeMillis();

		if( !loadCircle ) {

			MiscUtils.safeLogging("[ServerPort] circle generation stopped" );
			MiscUtils.safeMessage(playerName, "[ServerPort] circle generation stopped" );

			return;

		}

		int rQuan = ((int)Math.ceil(r/16))*16;

		int rQuanSquared = rQuan*rQuan;

		int percent = (100*(px+rQuan))/(rQuan)/2;

		if( px > rQuan || px < -rQuan ) {
			return;
		}

		int sz = ((int)Math.floor((Math.sqrt(rQuanSquared - px*px))/16))*16;



		int pz;

		if( last > - 2*r ) {
			pz = last;
			//MiscUtils.safeLogging( "[ServerPort] starting at z=" + pz );
		} else {		
			MiscUtils.safeLogging("[ServerPort] Loading row x=" + px + " with z = " + (z-sz) + " to " + (z+sz) + " (" + percent + "%)");
			MiscUtils.safeMessage( playerName , "[ServerPort] Loading row x=" + px + " with z = " + (z-sz) + " to " + (z+sz) + " (" + percent + "%)");
			MiscUtils.safeMessage( playerName , "Use: /stopcircle to cancel" );
			pz = -sz;
		}


		boolean chunkLoaded = false;
		for( ; pz<=sz && !chunkLoaded; pz+=16 ) {
			if(!world.isChunkLoaded(x, z)) {
				world.loadChunk( (x + px)>>4 , (z + pz)>>4 );
				world.unloadChunk( (x+px)>>4 , (z + pz)>>4 , false , false);
				chunkLoaded = true;
			} else {
				chunkLoaded = false;
			}
		}

		boolean finishedRow = pz>sz;
		//if(finishedRow) {
		//	MiscUtils.safeLogging("Finished row");
		//}

		final int finalx = x;
		final int finaly = y;
		final int finalz = z;
		final int finalr = r;
		final int finalpx = finishedRow?px+16:px;
		final int finallast = finishedRow?(-10*r):pz;
		final World finalWorld = world;
		final String finalPlayerName = new String( playerName );

		long delayTime = System.currentTimeMillis() - startTime;
		//MiscUtils.safeLogging("Delay until next burst: " + (System.currentTimeMillis() - startTime) );

		if( delayTime < 25 ) {
			delayTime = 25;
		}

		server.addToServerQueue(new Runnable() {

			public void run() {

				loadCircle( finalWorld, finalPlayerName, finalpx, finalx, finaly, finalz, finalr, finallast);

			}

		}, delayTime);


		 



	}

	static boolean loadChunks( HashMap<IntLocation,Integer> blocks ) {

		Iterator<IntLocation> itr = blocks.keySet().iterator();

		HashMap<IntLocation,Boolean> chunks = new HashMap<IntLocation,Boolean>();

		while( itr.hasNext() ) {

			IntLocation loc = itr.next();

			IntLocation chunkHash = new IntLocation( loc.getX()>>4 , 0 , loc.getZ()>>4, loc.getWorldName());

			if( !chunks.containsKey(chunkHash) ) {
				chunks.put(chunkHash, true);
				server.loadChunk(loc.getWorld(), loc.getX(),loc.getY(),loc.getZ());
			}

		}

		return true;


	}



	static String base36( int x ) {

		return Integer.toString( x , 36 );

	}

	static int mod( int x , int r ) {

		int m = x % r;
		if( m < 0 ) m+=r;

		return m;

	}


	static String blockString( MyBlock block ) {
		return block.getX() + ", " + block.getY() + ", " + block.getZ();
	}

	static String getHostname( int[] ipAddr ) {

		if( ipAddr == null ) {
			return null;
		}

		if( ipAddr.length != 4 ) {
			return null;
		}

		String ipString = ipAddr[0] + "." + ipAddr[1] + "." + ipAddr[2] + "." + ipAddr[3];

		InetAddress addr;

		try {
			addr = InetAddress.getByName( ipString );
		} catch (UnknownHostException uhe ) {
			return null;
		}

		return addr.getHostName();


	}

	static boolean isThisMyIpAddress(InetAddress addr) {
		// Check if the address is a valid special local or loop back
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
			return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		} catch (SocketException e) {
			return false;
		}
	}

	static boolean isAddressLocal(String address) {

		try {
			InetAddress addr = InetAddress.getByName( address );

			if( isThisMyIpAddress( addr ) ||
					addr.isLinkLocalAddress() ) {
				return true;
			} 

		} catch (Exception e) {};
		return false;

	}

	static int[] getLocalIP() {

		try {
			InetAddress addr = InetAddress.getLocalHost(); 

			byte[] ip = addr.getAddress(); 

			if( ip == null || ip.length != 4 ) {
				return null;
			} 

			int[] ret = new int[4];

			for( int cnt = 0;cnt<4;cnt++) {
				ret[cnt] = (int)ip[cnt];
				if( ret[cnt] < 0 ) {
					ret[cnt] += 256;
				}
			}

			return ret;

		} catch (UnknownHostException uhe ) {
			return null;
		}

	}

	static int[] getGlobalIP() {

		String globalServer = "checkip.dyndns.org";
		//String globalServer = "checkip.dyndns.org"

		//safeLogging(log , "Attempting to determine global IP using " + globalServer);

		try {
			URL ipCheck = new URL("http://checkip.dyndns.org");

			URLConnection ipCheckConnection = ipCheck.openConnection();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							ipCheckConnection.getInputStream()));

			String line;

			line = in.readLine();

			String[] split = line.split("\\.");

			if( split.length != 4 ) {
				safeLogging(log , "Incorrect number of bytes: " + split.length );
				safeLogging(log , "Unable to parse IP address from " + globalServer);
				return null;
			}

			int[] ret = new int[4];

			int cnt;

			for( cnt=0;cnt<4;cnt++) {
				String singleByte = split[cnt].replaceAll("[^0-9]","");
				ret[cnt] = Integer.parseInt(singleByte);
			}

			return ret;

		} catch (Exception e) {
			safeLogging(log , "Unable to parse IP address from " + globalServer);
			e.printStackTrace();
			return null;
		}

	}

	static String genRandomCode() {
		synchronized( random ) {
			return new BigInteger(130, random).toString(32);
		}
	}

	static String sha1Hash( String inputString ) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.reset();

			md.update(inputString.getBytes("utf-8"));

			BigInteger bigInt = new BigInteger( md.digest() );

			return bigInt.toString( 16 ) ;

		} catch (Exception ioe) {
			safeLogging( "Hashing error, this will probably prevent connections working");
			return "hash error";
		}

	}

	static String errorCheck( String line ) {

		if( line.matches("^Error: .*$")) {
			return line.substring(7);
		} else {
			return null;
		}

	}

	static File dirScan( String dir , String fileName ) {

		File dirFile = new File( dir );

		if( !dirFile.isDirectory() ) {
			return null;
		} else {

			File[] files = dirFile.listFiles();

			for( File file : files ) {

				if( file.getName().equalsIgnoreCase(fileName)) {
					return file;
				}

			}

		}

		return null;

	}

}
