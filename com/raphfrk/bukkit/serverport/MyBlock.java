package com.raphfrk.bukkit.serverport;

public class MyBlock {
	
	public org.bukkit.block.Block bukkitBlock;
	private int bukkitStatus;
	// 0: start
	// 3: broken
	
	public MyBlock() {
	}
	
	public MyBlock( org.bukkit.block.Block block, int status ) {
		bukkitBlock = block;
	}
	
	void setBukkitBlock(org.bukkit.block.Block block, int status) {
		bukkitBlock = block;
		bukkitStatus = status;
	}
	
	int getType() {
		
			return bukkitBlock.getTypeId();

	}
	
	int getStatus() {
		
			return bukkitStatus;

	}

	int getX() {
		
			return bukkitBlock.getX();

	}
	
	int getY() {
		return bukkitBlock.getY();
		
	}
	
	int getZ() {
		return bukkitBlock.getZ();
		
	}
	
	org.bukkit.World getWorld() {
		return bukkitBlock.getWorld();
	}
	

}