
public class MyPlayer {

	private org.bukkit.entity.Player bukkitPlayer;
	
	private org.bukkit.Location teleportTo = null;

	private Player hmodPlayer;

	private boolean hmod = false;
	
	MyPlayer() {
		hmod = MyServer.getServer().getHmod();	
	}

	public MyPlayer( Player player ) {
		hmodPlayer = player;
		hmod = true;
	}

	public MyPlayer( org.bukkit.entity.Player player ) {
		bukkitPlayer = player;
		hmod = false;
	}
	
	void setHmodPlayer( Player player ) {
		hmod = true;
		this.hmodPlayer = player;
	}
	
	void setBukkitPlayer( org.bukkit.entity.Player player ) {
		this.bukkitPlayer = player;
		hmod = false;
	}
	
	String getColor() {
		
		if( hmod ) {
			return hmodPlayer.getColor();
		} else {
			
			String displayName = bukkitPlayer.getDisplayName();
			if( displayName.indexOf("\u00A7") == 0 ) {
				return displayName.substring(0,2);
			}
			
			return "";
		}
		
	}
	
	org.bukkit.Location getTeleportTo() {
		return teleportTo;
	}

	String getName() {
		
		if( hmod ) {
			return hmodPlayer.getName();
		} else {
			return bukkitPlayer.getName();
		}
		
	}
	
	String getIP() {
		if( hmod ) {
			return hmodPlayer.getIP();
		} else {
			byte[] ip = bukkitPlayer.getAddress().getAddress().getAddress();
			return ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
		}
	}
	
	MyInventory getInventory() {

		MyInventory inv = new MyInventory();
		
		if( hmod ) {
			
			inv.setHmodInventory( hmodPlayer.getInventory() );
			
			return inv;
			
		} else {
	
			inv.setBukkitInventory( bukkitPlayer.getInventory() );
			return inv;
		}
		
	}
	
	MyLocation getLocation() {

		MyLocation loc = new MyLocation();
		
		if( hmod ) {
			loc.setHmodLocation(hmodPlayer.getLocation());
			return loc;
		} else {
			loc.setBukkitLocation(bukkitPlayer.getLocation());
			return loc;
		}
		
	}
	
	void sendMessage(String message) {
		
		if( hmod ) {
			
			if( hmodPlayer != null ) {
				hmodPlayer.sendMessage(message);
			}
			
		} else {
			
			if( bukkitPlayer != null ) {
				bukkitPlayer.sendMessage(message);
			}
			
		}
		
	}
	
	void setHealth(int health) {
		if( hmod ) {
			hmodPlayer.setHealth(health);
		} else {
			bukkitPlayer.setHealth(health);
		}
	}
	
	int getHealth() {
		if( hmod ) {
			return hmodPlayer.getHealth();
		} else {
			return bukkitPlayer.getHealth();
		}
	}
	
	void teleportTo(MyLocation loc) {
		if( hmod ) {
			hmodPlayer.teleportTo(loc.getHmodLocation());
		} else {
			bukkitPlayer.teleportTo(loc.getBukkitLocation());
			teleportTo = loc.getBukkitLocation();
		}
	}
	
	boolean canUseCommand( String command ) {
		if( hmod ) {
			return hmodPlayer.canUseCommand(command);
		} else {
			// TODO
			return true;
		}
	}
	
	boolean isAdmin() {
		if( hmod ) {
			return hmodPlayer.isAdmin();
		} else {
			//TODO
			return true;
		}
	}
	
	float getRotation() {
		if( hmod ) {
			return hmodPlayer.getRotation();
		} else {
			return bukkitPlayer.getLocation().getYaw();
		}
	}
	
	float getPitch() {
		if( hmod ) {
			return hmodPlayer.getPitch();
		} else {
			return bukkitPlayer.getLocation().getPitch();
		}
	}
	
	void kick( String message ) {
		if( hmod ) {
			if( hmodPlayer != null && message != null ) {
				hmodPlayer.kick(message);
			}
		} else {
			if( bukkitPlayer != null && message != null ) {
				bukkitPlayer.kickPlayer(message);
			}
		}
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodPlayer == null;
		} else {
			return bukkitPlayer == null;
		}
	}
	
}
