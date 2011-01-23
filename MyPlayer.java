import java.util.HashSet;


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
			return (ip[0]&0xFF) + "." + (ip[1]&0xFF) + "." + (ip[2]&0xFF) + "." + (ip[3]&0xFF);
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

	static HashSet<String> admins = null;
	static HashSet<String> create = null;
	static HashSet<String> use = null;	
	static HashSet<String> destroy = null;

	boolean canUse(String player) {
		if( use == null ) {
			use = MiscUtils.fileToSet("use_list.txt");
		}
		return use.contains(player);
	}

	boolean isAdmin(String player) {
		if( admins == null ) {
			admins = MiscUtils.fileToSet("admin_list.txt");
		}
		return admins.contains(player);
	}

	boolean canCreate(String player) {
		if( create == null ) {
			create = MiscUtils.fileToSet("create_list.txt");
		}
		return create.contains(player);
	}
	
	boolean canDestroy(String player) {
		if( destroy == null ) {
			destroy = MiscUtils.fileToSet("destroy_list.txt");
		}
		return destroy.contains(player);
	}

	boolean canUseCommand( String command ) {
		if( hmod ) {
			return hmodPlayer.canUseCommand(command);
		} else {
			if( command.indexOf("/serverportuse") == 0 ) return canUse(getName());
			if( command.indexOf("/serverportcreate") == 0 ) return canCreate(getName());
			if( command.indexOf("/serverportdestroy") == 0 ) return canDestroy(getName());
			// TODO
			return false;
		}
	}

	boolean isAdmin() {
		if( hmod ) {
			return hmodPlayer.isAdmin();
		} else {
			return isAdmin(getName());
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
				if( bukkitPlayer.isOnline() ) {
					System.out.println("Kicking " + bukkitPlayer.getName() + " for " + message );
					System.out.flush();
					bukkitPlayer.kickPlayer(message);
				}
			}
		}
	}

	boolean isNull() {
		if( hmod ) {
			return hmodPlayer == null;
		} else {
			return bukkitPlayer == null || !bukkitPlayer.isOnline();
		}
	}
}

