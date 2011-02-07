
public class MyLocation {
	
	private org.bukkit.Location bukkitLocation;
	
	private Location hmodLocation;
	
	private boolean hmod = false;
	
	public MyLocation() {
		
		hmod = MyServer.getServer().getHmod();
		
		if( hmod ) {
			hmodLocation = new Location();	
		} else {
			bukkitLocation = new org.bukkit.Location(MyServer.getServer().getMainWorld(), 0, 0, 0);
		}
		
	}
	
	MyLocation( org.bukkit.World world, int x, int y, int z) {
		this(world, (double)x, (double)y, (double)z);
	}
	
	MyLocation( org.bukkit.World world, double x, double y, double z) {
		this(world, x, y, z, (float)0, (float)0);
	}
	
	MyLocation( org.bukkit.World world, double x, double y, double z, float rotX, float rotY) {
		this();
		
		if( hmod ) {
			hmodLocation = new Location( x, y, z, rotX, rotY );
		} else {
			bukkitLocation = new org.bukkit.Location( world, x, y, z, rotX, rotY );
		}
	}
	
	public MyLocation( Location loc ) {
		hmodLocation = loc;
		hmod = true;
	}
	
	public MyLocation( org.bukkit.Location loc ) {
		bukkitLocation = loc;
		hmod = false;
	}
	
	void setHmodLocation( Location loc ) {
		this.hmodLocation = loc;
	}
	
	void setBukkitLocation( org.bukkit.Location loc ) {
		this.bukkitLocation = loc;
	}
	
	Location getHmodLocation() {
		return hmodLocation;
	}
	
	org.bukkit.Location getBukkitLocation() {
		return bukkitLocation;
	}
	
	double getX() {
		if( hmod ) {
			return hmodLocation.x;
		} else {
			return bukkitLocation.getX();
		}
	}
	
	double getY() {
		if( hmod ) {
			return hmodLocation.y;
		} else {
			return bukkitLocation.getY();
		}
	}
	
	double getZ() {
		if( hmod ) {
			return hmodLocation.z;
		} else {
			return bukkitLocation.getZ();
		}
	}
	
	float getRotX() {
		if( hmod ) {
			return hmodLocation.rotX;
		} else {
			return bukkitLocation.getYaw();
		}
	}
	
	float getRotY() {
		if( hmod ) {
			return hmodLocation.rotY;
		} else {
			return bukkitLocation.getPitch();
		}
	}
	
	void setX(double x) {
		if( hmod ) {
			hmodLocation.x=x;
		} else {
			bukkitLocation.setX(x);
		}
	}
	
	void setY(double y) {
		if( hmod ) {
			hmodLocation.y=y;
		} else {
			bukkitLocation.setY(y);
		}
	}
	
	void setWorld(org.bukkit.World world) {
		bukkitLocation.setWorld(world);
	}
	
	void setZ(double z) {
		if( hmod ) {
			hmodLocation.z=z;
		} else {
			bukkitLocation.setZ(z);
		}
	}
	
	void setRotX(float rotX) {
		if( hmod ) {
			hmodLocation.rotX=rotX;
		} else {
			bukkitLocation.setYaw(rotX);
		}
	}
	
	void setRotY(float rotY) {
		if( hmod ) {
			hmodLocation.rotY=rotY;
		} else {
			bukkitLocation.setPitch(rotY);
		}
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodLocation == null;
		} else {
			return bukkitLocation == null;
		}
	}


}
