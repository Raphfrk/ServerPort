/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.raphfrk.bukkit.serverport;
import java.util.List;

import org.bukkit.World;


public class IntLocation implements Comparable<IntLocation> {
	
	@Override 
	public int hashCode() { 

		int hash = x + y%1023 + z%65535 + worldIndex;
				
		return hash;
		
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
			z==otherIntLocation.z && 
			worldIndex == otherIntLocation.worldIndex;
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
		
		if( this.worldIndex > other.worldIndex ) {
			return 1;
		} else if (this.worldIndex < other.worldIndex ){
			return -1;
		}
		
		return 0;
	}

	int x;
	int y;
	int z;
	
	String worldName = null;
	int worldIndex;
	World world = null;
	
	IntLocation( IntLocation loc ) {
		this.x = loc.x;
		this.y = loc.y;
		this.z = loc.z;
		this.worldIndex = loc.worldIndex;
		this.world = loc.world;
	}
	
	IntLocation( int x , int y , int z , String worldName ) {
		this.x = x;
		this.y = y;
		this.z = z;
		List<World> worlds = MyServer.bukkitServer.getWorlds();
		int cnt;
		this.worldName = worldName;
		for(cnt=0;cnt<worlds.size();cnt++) {
			if(worlds.get(cnt).getName().equalsIgnoreCase(worldName)) {
				this.worldIndex = cnt;
				this.world = worlds.get(cnt);
				this.worldName = worldName;
				return;
			}
		}
		this.worldIndex = -1;
	}
	
	IntLocation( MyBlock block ) {
		this(block.getX(), block.getY(), block.getZ(), block.bukkitBlock.getWorld().getName());
	}
	
	IntLocation( MyLocation loc ) {
		this(
				(int)Math.floor(loc.getX()),
				(int)Math.floor(loc.getY()),
				(int)Math.floor(loc.getZ()),
				loc.getBukkitLocation().getWorld().getName()
				);
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
	
	World getWorld() {
		return world;
	}
	
	int getWorldId() {
		return worldIndex;
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
	
	void setWorldIndex( int index ) {
		this.worldIndex = index;
		worldName = null;
	}
	
	static int getWorldIndex( World world ) {
		
		List<World> worlds = MyServer.bukkitServer.getWorlds();
		int cnt;
		for( cnt = 0;cnt < worlds.size(); cnt++ ) {
			if(worlds.get(cnt).equals(world)) {
				return cnt;
			}
		}
		return -1;
		
	}
	
	String getWorldName() {
		if(worldIndex < 0) {
			return null;
		}
		if(worldName == null) {
			worldName = ((World)MyServer.bukkitServer.getWorlds().get(worldIndex)).getName();
		}
		return worldName;
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
		
		if( split.length != 4 && split.length != 3 ) {
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

		if( split.length != 4 && split.length != 3 ) {
			MiscUtils.safeLogging( "[Serverport] Unable to parse " + string + " as int location" );
			return null;
		}

		String name;
		if( split.length == 3 ) {
			name = ((World)MyServer.bukkitServer.getWorlds().get(0)).getName();
		} else {
			name = split[3];
		}
		
		return new IntLocation( 
				MiscUtils.getInt(split[0]),
				MiscUtils.getInt(split[1]),
				MiscUtils.getInt(split[2]),
				name);


	}

	String getString() {
		String name = getWorldName();
		if( name == null ) {
			name = ((World)MyServer.bukkitServer.getWorlds().get(0)).getName();
		}
		return x + ", " + y + ", " + z + ", " + getWorldName();
	}
	
	MyLocation toLocation() {
		
		MyLocation loc = new MyLocation();
		
		loc.setX( this.x + 0.5 );
		loc.setY( this.y );
		loc.setZ( this.z + 0.5 );
		loc.getBukkitLocation().setWorld((World)MyServer.bukkitServer.getWorlds().get(worldIndex));
		
		return loc;
		
	}
	
	String getFileString() {
		
		return minusInt(x) + "_" + minusInt(y) + "_" + minusInt(z) + "_" + getWorldName();
		
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
		
		if( a.worldIndex != b.worldIndex ) {
			d+=100000000;
		}
		
		return d;
		
		
		
	}

}
