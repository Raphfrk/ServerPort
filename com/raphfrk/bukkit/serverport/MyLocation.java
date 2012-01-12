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

public class MyLocation {
	
	private org.bukkit.Location bukkitLocation;
		
	public MyLocation() {
		bukkitLocation = new org.bukkit.Location(MyServer.getServer().getMainWorld(), 0, 0, 0);
	}
	
	MyLocation( org.bukkit.World world, int x, int y, int z) {
		this(world, (double)x, (double)y, (double)z);
	}
	
	MyLocation( org.bukkit.World world, double x, double y, double z) {
		this(world, x, y, z, (float)0, (float)0);
	}
	
	MyLocation( org.bukkit.World world, double x, double y, double z, float rotX, float rotY) {
		this();
		
		bukkitLocation = new org.bukkit.Location( world, x, y, z, rotX, rotY );
	}
	
	public MyLocation( org.bukkit.Location loc ) {
		bukkitLocation = loc;
	}
	
	void setBukkitLocation( org.bukkit.Location loc ) {
		this.bukkitLocation = loc;
	}
	
	org.bukkit.Location getBukkitLocation() {
		return bukkitLocation;
	}
	
	double getX() {
		return bukkitLocation.getX();
	}
	
	double getY() {
		return bukkitLocation.getY();
	}
	
	double getZ() {
		return bukkitLocation.getZ();
	}
	
	float getRotX() {
		return bukkitLocation.getYaw();
	}
	
	float getRotY() {
		return bukkitLocation.getPitch();
	}
	
	void setX(double x) {
		bukkitLocation.setX(x);
	}
	
	void setY(double y) {
		bukkitLocation.setY(y);
	}
	
	void setWorld(org.bukkit.World world) {
		bukkitLocation.setWorld(world);
	}
	
	org.bukkit.World getWorld() {
		return bukkitLocation.getWorld();
	}
	
	void setZ(double z) {
		bukkitLocation.setZ(z);
	}
	
	void setRotX(float rotX) {
		bukkitLocation.setYaw(rotX);
	}
	
	void setRotY(float rotY) {
		bukkitLocation.setPitch(rotY);
	}
	
	boolean isNull() {
		return bukkitLocation == null;
	}


}
