
public class MyPlayer {

	private org.bukkit.Player bukkitPlayer;

	private Player hmodPlayer;

	private boolean hmod = false;
	
	MyPlayer() {
		hmod = MyServer.getServer().getHmod();	
	}

	public MyPlayer( Player player ) {
		hmodPlayer = player;
		hmod = true;
	}

	public MyPlayer( org.bukkit.Player player ) {
		bukkitPlayer = player;
		hmod = false;
	}
	
	void setHmodPlayer( Player player ) {
		this.hmodPlayer = player;
	}
	
	String getColor() {
		
		if( hmod ) {
			return hmodPlayer.getColor();
		} else {
			return "";
		}
		
	}

	String getName() {
		
		if( hmod ) {
			return hmodPlayer.getName();
		} else {
			return "";
		}
		
	}
	
	String getIP() {
		if( hmod ) {
			return hmodPlayer.getIP();
		} else {
			return "127.0.0.1";
		}
	}
	
	MyInventory getInventory() {
		
		if( hmod ) {
			
			MyInventory inv = new MyInventory();
			
			inv.setHmodInventory( hmodPlayer.getInventory() );
			
			return inv;
			
		} else {
			return null;
		}
		
	}
	
	MyLocation getLocation() {

		MyLocation loc = new MyLocation();
		
		if( hmod ) {
			loc.setHmodLocation(hmodPlayer.getLocation());
			return loc;
		} else {
			return null;
		}
		
	}
	
	void sendMessage(String message) {
		
		if( hmod ) {
			
			if( hmodPlayer != null ) {
				hmodPlayer.sendMessage(message);
			}
			
		}
		
	}
	
	void setHealth(int health) {
		if( hmod ) {
			hmodPlayer.setHealth(health);
		}
	}
	
	int getHealth() {
		if( hmod ) {
			return hmodPlayer.getHealth();
		} else {
			return 20;
		}
	}
	
	void teleportTo(MyLocation loc) {
		if( hmod ) {
			hmodPlayer.teleportTo(loc.getHmodLocation());
		}
	}
	
	boolean canUseCommand( String command ) {
		if( hmod ) {
			return hmodPlayer.canUseCommand(command);
		} else {
			return true;
		}
	}
	
	boolean isAdmin() {
		if( hmod ) {
			return hmodPlayer.isAdmin();
		} else {
			return true;
		}
	}
	
	float getRotation() {
		if( hmod ) {
			return hmodPlayer.getRotation();
		} else {
			return 0;
		}
	}
	
	float getPitch() {
		if( hmod ) {
			return hmodPlayer.getPitch();
		} else {
			return 0;
		}
	}
	
	void kick( String message ) {
		if( hmod ) {
			hmodPlayer.kick(message);
		}
	}
	
}
