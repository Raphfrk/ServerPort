
public class MyLocation {
	
	private org.bukkit.Location bukkitLocation;
	
	private Location hmodLocation;
	
	private boolean hmod = false;
	
	public MyLocation() {
		
		hmod = MyServer.getServer().getHmod();
		
		if( hmod ) {
			hmodLocation = new Location();	
		}
		
	}
	
	MyLocation( int x, int y, int z) {
		this((double)x, (double)y, (double)z);
	}
	
	MyLocation( double x, double y, double z) {
		this( x, y, z, 0, 0);
	}
	
	MyLocation( double x, double y, double z, float rotX, float rotY) {
		this();
		
		if( hmod ) {
			hmodLocation = new Location( x, y, z, rotX, rotY );
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
	
	Location getHmodLocation() {
		return hmodLocation;
	}
	
	double getX() {
		if( hmod ) {
			return hmodLocation.x;
		} else {
			return 0;
		}
	}
	
	double getY() {
		if( hmod ) {
			return hmodLocation.y;
		} else {
			return 0;
		}
	}
	
	double getZ() {
		if( hmod ) {
			return hmodLocation.z;
		} else {
			return 0;
		}
	}
	
	float getRotX() {
		if( hmod ) {
			return hmodLocation.rotX;
		} else {
			return 0;
		}
	}
	
	float getRotY() {
		if( hmod ) {
			return hmodLocation.rotY;
		} else {
			return 0;
		}
	}
	
	void setX(double x) {
		if( hmod ) {
			hmodLocation.x=x;
		} else {
			
		}
	}
	
	void setY(double y) {
		if( hmod ) {
			hmodLocation.y=y;
		} else {
			
		}
	}
	
	void setZ(double z) {
		if( hmod ) {
			hmodLocation.z=z;
		} else {
			
		}
	}
	
	void setRotX(float rotX) {
		if( hmod ) {
			hmodLocation.rotX=rotX;
		} else {
			
		}
	}
	
	void setRotY(float y) {
		if( hmod ) {
			hmodLocation.rotY=y;
		} else {
			
		}
	}


}
