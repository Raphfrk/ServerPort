public class MyServer {
	
	public static org.bukkit.Server bukkitServer;
	
	public static Server hmodServer;
	
	private static MyServer myServer = new MyServer();
	
	private boolean hmod = false;
	
	static void setHmodServer( Server server ) {
		
		MyServer.hmodServer = server;
		myServer.hmod = true;
		
	}
	

	static void setBukkitServer( org.bukkit.Server server ) {
		
		myServer.bukkitServer = server;
		myServer.hmod = false;
		
	}

	
	public static MyServer getServer() {
		
		return myServer;
		
	}
	
	public boolean getHmod() {
		return hmod;
	}
	
	boolean isPlayerListEmpty() {
		if( hmod ) {
			return hmodServer.getPlayerList().isEmpty();
		} else {
			return false;
		}
	}
	
	void messageAll( String message ) {
		
		if( hmod ) {
			hmodServer.messageAll( message );
		}
	}
	
	void addToServerQueue( Runnable runnable, long delay ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable , delay );
			
		} 
		
	}

	void addToServerQueue( Runnable runnable ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable );
			
		} 
		
	}
	
	void dropItem( MyLocation loc, int id, int quantity ) {
		
		if( hmod ) {
			
			hmodServer.dropItem(loc.getHmodLocation(), id, quantity);
			
		}
		
	}
	
	MyPlayer getPlayer( String name ) {
		
		if( hmod ) {
			
			MyPlayer player = new MyPlayer();
			
			player.setHmodPlayer(hmodServer.getPlayer(name));
			
			return player;
			
		} else {
			return null;
		}
		
	}
	
	int getBlockIdAt(int x, int y, int z) {
		
		if( hmod ) {
			return hmodServer.getBlockIdAt(x, y, z);
		} else {
			return 0;
		}
	}
	
	void loadChunk(int x, int y, int z) {
		
		if( hmod ) {	
			hmodServer.loadChunk(x, y, z);
		}
		
	}
	
	void setBlockAt(int id, int x, int y, int z) {
		
		if( hmod ) {
			hmodServer.setBlockAt(id, x, y, z);
		}
	}
	
	void setBlockData(int x, int y, int z, int d) {
		
		if( hmod ) {
			hmodServer.setBlockData(x, y, z, d);
		}
	}

	String getColor(String color) {

		if( hmod ) {
			if( color.equals("Green")) return Colors.Green;
			if( color.equals("LightBlue")) return Colors.LightBlue;
			if( color.equals("White")) return Colors.White;
			if( color.equals("Green")) return Colors.Green;
			if( color.equals("Green")) return Colors.Green;
			return "";
		} else {
			return "";
		}
	}
	
	int getBlockData(int x, int y, int z) {
		
		if( hmod ) {
			return hmodServer.getBlockData(x, y, z);
		} else {
			return 0;
		}
		
	}
	
	MySign getComplexBlock( int x, int y, int z) {
		if( hmod ) {
			MySign sign = new MySign();
			sign.hmodSign = (Sign)hmodServer.getComplexBlock( x, y, z );
			return sign;
		} else {
			return null;
		}
	}
	
}