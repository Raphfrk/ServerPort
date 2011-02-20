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
