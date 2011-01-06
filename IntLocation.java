
public class IntLocation implements Comparable<IntLocation> {
	
	@Override 
	public int hashCode() { 

		return x + y%1023 + z%65535;
		
	}
	
	@Override 
	public boolean equals( Object other ) {
		
		if( !other.getClass().equals(IntLocation.class) ) {
			return false;
		}
		
		IntLocation otherIntLocation = (IntLocation)other;
		
		return 
			x==otherIntLocation.x && 
			y==otherIntLocation.y && 
			z==otherIntLocation.z;
	}
		
	
	public int compareTo( IntLocation other ) {
				
		if( this.z > other.z ) {
			return 1;
		} else if ( this.z < other.z ) {
			return -1;
		}
		
		if( this.y < other.y ) {
			return 1;
		} else if ( this.y > other.y ) {
			return -1;
		}
		
		if( this.x > other.x ) {
			return 1;
		} else if ( this.x < other.x ) {
			return -1;
		}
		
		return 0;
	}

	int x;
	int y;
	int z;
	
	IntLocation( IntLocation loc ) {
		this.x = loc.x;
		this.y = loc.y;
		this.z = loc.z;
	}
	
	IntLocation( int x , int y , int z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	IntLocation( MyBlock block ) {
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}
	
	IntLocation( MyLocation loc ) {
		this.x = (int)Math.floor(loc.getX());
		this.y = (int)Math.floor(loc.getY());
		this.z = (int)Math.floor(loc.getZ());
	}
	
	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	int getZ() {
		return z;
	}
	
	void setX( int x ) {
		this.x = x;
	}
	
	void setY( int y ) {
		this.y = y;
	}
	
	void setZ( int z ) {
		this.z = z;
	}
	
	@Override
	public String toString() {
		return getString();
	}
	
	static public boolean isIntLocation( String string ) {
		
		if( string == null ) {
			return false;
		}
		
		String[] split = string.split(",");
		
		if( split.length != 3 ) {
			return false;
		}
		
		return 
		MiscUtils.isInt(split[0]) &&
		MiscUtils.isInt(split[1]) &&
		MiscUtils.isInt(split[2]);


	}

	static public IntLocation getIntLocation( String string ) {

		if( string == null ) {
			MiscUtils.safeLogging( "[Serverport] Unable to parse " + string + " as int location" );
			return null;
		}

		String[] split = string.split(",");

		if( split.length != 3 ) {
			MiscUtils.safeLogging( "[Serverport] Unable to parse " + string + " as int location" );
			return null;
		}

		return new IntLocation( 
				MiscUtils.getInt(split[0]),
				MiscUtils.getInt(split[1]),
				MiscUtils.getInt(split[2]));


	}

	String getString() {
		return x + ", " + y + ", " + z;
	}
	
	MyLocation toLocation() {
		
		MyLocation loc = new MyLocation();
		
		loc.setX( this.x + 0.5 );
		loc.setY( this.y );
		loc.setZ( this.z + 0.5 );
		
		return loc;
		
	}
	
	String getFileString() {
		
		return minusInt(x) + "_" + minusInt(y) + "_" + minusInt(z);
		
	}
	
	String minusInt( int num ) {
		if( num < 0 ) {
			return "m" + (-num);
		} else {
			return "" + num;
		}
	}
	
	static int dist( IntLocation a , IntLocation b ) {
		
		int dx = Math.abs(a.getX() - b.getX());
		
		int dy = Math.abs(a.getY() - b.getY());
		
		int dz = Math.abs(a.getZ() - b.getZ());
		
		int d = dx;
		
		if( dy > d ) d = dy;
		if( dz > d ) d = dz;
		
		return d;
		
		
		
	}

}
