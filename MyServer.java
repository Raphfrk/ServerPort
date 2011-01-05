public class MyServer {
	
	private org.bukkit.Server bukkitServer;
	
	private Server hmodServer;
	
	private boolean hmod = false;
	
	public MyServer( Server server ) {
		hmodServer = server;
		hmod = true;
	}
	
	public MyServer( org.bukkit.Server server ) {
		bukkitServer = server;
		hmod = false;
	}

	
	static MyServer myServer;
	
	static void setHmodServer( Server server ) {
		
		myServer = new MyServer( server );
		
	}
	

	static void setBukkitServer( org.bukkit.Server server ) {
		
		myServer = new MyServer( server );;
		
	}

	
	public static MyServer getServer() {
		
		return myServer;
		
	}
	

}