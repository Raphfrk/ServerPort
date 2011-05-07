
public class PeerServerInfo {

	public String name = "none";
	public String hostname = "unknown";
	public String passcode = "";
	public boolean connected = false;
	public int portnum = 25465;
	
	@Override public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(name + ",");
		sb.append(hostname + ",");
		sb.append(passcode + ",");
		sb.append(portnum + ",");
		sb.append(connected?"connected":"pending");
	
		return sb.toString();
		
	}
	
	PeerServerInfo() {
		
	}
	
	PeerServerInfo( String string ) {
		
		String[] split = string.split(",",-1);
		
		int length = split.length;
		
		if( length >= 1 ) name = split[0];
		if( length >= 2 ) hostname = split[1];
		if( length >= 3 ) passcode = split[2];
		if( length >= 4 ) portnum = MiscUtils.isInt(split[3])?MiscUtils.getInt(split[3]):25465;
		if( length >= 5 ) connected = split[4].equals("connected");
		
	}
	
	
	
	
}
