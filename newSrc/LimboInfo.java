import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class LimboInfo {

	static final String slash = System.getProperty("file.separator");

	private String filename = "";
	private String playerName = "";
	private int playerHealth = 20;

	private String currentServer = "";

	private String currentGate = "";

	private boolean locked = false;

	private IntLocation lockedPosition = new IntLocation(0,0,0);

	private long lockRelease = -1;
	
	private String homeServer = "none";
	private String homeGate = "none";

	private double d;
	private int sx,sz;

	double rotX = 0;
	double rotY = 0;

	int timeToLive = 30;

	private LimboStore limboStore = null;
	private ParameterManager parameterManager = null;

	private HashMap<Integer,String> limboInv = new HashMap<Integer,String>();
	
	synchronized void setHomeServer( String homeServer ) {
		this.homeServer = new String( homeServer );
	}
	
	synchronized void setHomeGate( String homeGate ) {
		this.homeGate = new String( homeGate );
	}

	synchronized String getHomeServer(  ) {
		return new String( homeServer );
	}
	
	synchronized String getHomeGate(  ) {
		return new String( homeGate );
	}
	

	synchronized void setPlayerName( String playerName ) {

		this.playerName = new String( playerName );

	}

	synchronized String getPlayerName() {

		return new String( playerName );

	}

	synchronized void setLocked( boolean locked ) {

		this.locked = locked;
	}

	synchronized boolean getLocked() {

		return locked;

	}

	synchronized void setTimeToLive( int timeToLive ) {

		this.timeToLive = timeToLive;

	}

	synchronized int getTimeToLive() {

		return timeToLive;

	}



	synchronized void setRotX( double rotX ) {
		this.rotX = rotX;
	}

	synchronized void setRotY( double rotY ) {
		this.rotY = rotY;
	}

	synchronized double getRotX() {
		return rotX;
	}

	synchronized double getRotY() {
		return rotY;
	}

	synchronized void setLockRelease( long lockRelease ) {

		this.lockRelease = lockRelease;
	}

	synchronized long getLockRelease() {

		return lockRelease;

	}

	synchronized void setCurrentGate( String currentGate ) {

		this.currentGate = currentGate;

	}

	synchronized String getCurrentGate() {

		return new String(currentGate);

	}

	synchronized void setCurrentServer( String currentServer ) {

		this.currentServer = new String( currentServer );

	}

	synchronized String getCurrentServer() {

		return new String(currentServer);

	}

	synchronized int getPlayerHealth() {

		return playerHealth;

	}

	synchronized void load(String filename) {

		limboInv = new HashMap<Integer,String>();

		this.filename = filename;

		String[] strings = MiscUtils.fileToString(filename);

		if( strings != null ) {
			parseStrings( MiscUtils.fileToString(filename) );
		}
	}

	synchronized void initFilename(String path) {

		filename = path + slash + playerName + ".dat";

	}

	synchronized void setParameterManager( ParameterManager parameterManager ) {
		this.parameterManager = parameterManager;
	}

	synchronized void setLimboStore( LimboStore limboStore ) {
		this.limboStore = limboStore;
	}

	// Returns hashmap of refused items

	synchronized HashMap<Integer,String> parseString( String string ) {

		if( string == null ) {
			return null;
		}

		return parseStrings( string.split(";"));

	}

	synchronized HashMap<Integer,String> parseStrings( String[] limboString ) {

		HashMap<Integer,String> refusedItems = new HashMap<Integer,String>();

		for( String line : limboString ) {

			String[] split = line.split("=",-1);
			String[] split2 = {""};
			if( split.length > 1 ) {
				split2 = split[1].split(",",-1);
			}

			if( split.length != 2 ) {
				MiscUtils.safeLogging("[Serverport] unable to parse limbo file: " + filename );
				return null;
			}

			if( 
					MiscUtils.isInt(split[0]) && 
					split2.length == 3 && 
					MiscUtils.isInt(split2[0]) &&
					MiscUtils.isInt(split2[1]) &&
					MiscUtils.isInt(split2[2])  
			) {
				Integer slot = MiscUtils.getInt(split[0]);
				boolean banned = false;
				if( parameterManager != null && limboStore != null ) {
					synchronized( parameterManager ) {
						banned = limboStore.bannedItems.test(split2[0]) ^ limboStore.banExcept;
					}
				}

				if( banned ) {
					refusedItems.put(slot, split[1]);
				} else if( limboInv.containsKey(slot) ) {
					refusedItems.put(slot, split[1]);
				} else {
					limboInv.put(slot, split[1]); 
				}
			} else if( split[0].equals("playername")) {
				this.playerName = split[1];
			} else if( split[0].equals("homeserver")) {
				this.homeServer = split[1];
			} else if( split[0].equals("homegate")) {
				this.homeGate = split[1];
			} else if( split[0].equals("playerhealth")) {
				if( MiscUtils.isInt(split[1])) {
					this.playerHealth = MiscUtils.getInt(split[1]);
				} else {
					MiscUtils.safeLogging("[ServerPort] unable to parse player health string: " + split[1]);
					this.playerHealth = 20;
				}
			} else if( split[0].equals("currentserver")) {
				this.currentServer = split[1];
			} else if( split[0].equals("currentgate")) {
				this.currentGate = split[1];
			} else if( split[0].equals("locked")) {
				this.locked = split[1] == "true";
			} else if( split[0].equals("lockedposition")) {
				if( IntLocation.isIntLocation(split[1])) {
					this.lockedPosition = IntLocation.getIntLocation(split[1]);
				} else {
					this.lockedPosition = new IntLocation(0,0,0);
					MiscUtils.safeLogging("[ServerPort] Unable to parse lockedposition in file: " + filename);
				}
			} else if( split[0].equals("lockedrelease")) {
				if( MiscUtils.isLong( split[1] )) {
					this.lockRelease = MiscUtils.getLong(split[1]);
				} else {
					this.lockRelease = -1;
					MiscUtils.safeLogging("[ServerPort] Unable to parse lockRelease in file: " + filename);
				}
			} else if( split[0].equals("timetolive")) {
				if( MiscUtils.isInt( split[1] )) {
					this.timeToLive = MiscUtils.getInt(split[1]);
				} else {
					this.sx = 30;
					MiscUtils.safeLogging("[ServerPort] Unable to parse timetolive in file: " + filename);
				}
			} else if( split[0].equals("sx")) {
				if( MiscUtils.isInt( split[1] )) {
					this.sx = MiscUtils.getInt(split[1]);
				} else {
					this.sx = 0;
					MiscUtils.safeLogging("[ServerPort] Unable to parse sx in file: " + filename);
				}
			} else if( split[0].equals("sz")) {
				if( MiscUtils.isInt( split[1] )) {
					this.sz = MiscUtils.getInt(split[1]);
				} else {
					this.sz = 0;
					MiscUtils.safeLogging("[ServerPort] Unable to parse sz in file: " + filename);
				}

			} else if( split[0].equals("d")) {
				if( MiscUtils.isDouble( split[1] )) {
					this.d = MiscUtils.getDouble(split[1]);
				} else {
					this.d = 0;
					MiscUtils.safeLogging("[ServerPort] Unable to parse d in file: " + filename);
				}

			} else {
				MiscUtils.safeLogging("[ServerPort] Unknown field " + split[0] + " in file: " + filename);
			}


		}

		return refusedItems;

	}

	synchronized void save(){

		MiscUtils.stringToFile(toStrings(), filename);

	}

	@Override
	synchronized public String toString() {

		ArrayList<String> strings = toStrings();

		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for( Object stringObject : strings.toArray() ) {

			String string = stringObject.toString();

			if( !first ) {
				sb.append(";");
			}
			first = false;
			sb.append(string);

		}

		return sb.toString();

	}

	synchronized String removePlayerStore() {

		StringBuilder sb = new StringBuilder();

		Iterator<Integer> itr = limboInv.keySet().iterator();

		boolean first = true;
		while( itr.hasNext() ) {

			Integer current = itr.next();

			if( !first ) {
				sb.append(";");
			}
			first = false;
			sb.append(current + "=" + limboInv.get(current));

		}

		limboInv = new HashMap<Integer,String>();

		return sb.toString();

	}

	synchronized public ArrayList<String> toStrings() {

		ArrayList<String> limboString = new ArrayList<String>();

		Iterator<Integer> itr = limboInv.keySet().iterator();

		while( itr.hasNext() ) {
			Integer current = itr.next();
			limboString.add( current + "=" + limboInv.get(current) );
		}


		limboString.add( "playername=" + playerName );
		limboString.add( "playerhealth=" + playerHealth );
		limboString.add( "currentserver=" + currentServer );
		limboString.add( "currentgate=" + currentGate );
		limboString.add( "locked=" + locked );
		limboString.add( "lockedposition=" + lockedPosition );
		limboString.add( "lockedrelease=" + lockRelease );
		limboString.add( "timetolive=" + timeToLive );
		limboString.add( "sx=" + sx );
		limboString.add( "sz=" + sz );
		limboString.add( "d=" + d );
		limboString.add( "homeserver=" + homeServer );
		limboString.add( "homegate=" + homeGate );

		return limboString;


	}

}
