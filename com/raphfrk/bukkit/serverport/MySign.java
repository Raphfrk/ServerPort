package com.raphfrk.bukkit.serverport;


public class MySign {
	
	
	org.bukkit.block.Sign bukkitSign;
	int status;
	
	MySign() {
	}
	

	void setBukkitSign( org.bukkit.block.Sign sign, int status ) {
		bukkitSign = sign;
		this.status = status;
	}
	
	int getX() {
		return bukkitSign.getX();
	}
	
	int getY() {
		return bukkitSign.getY();
	}
	
	int getZ() {
		return bukkitSign.getZ();
	}
	
	void setText(int line, String text) {
		bukkitSign.setLine(line, text);
	}
	
	String getText(int line) {
		return bukkitSign.getLine(line);
	}
	
	void update() {
		bukkitSign.update();
	}
	
	MyBlock getBlock() {

		MyBlock block = new MyBlock();
		
		block.setBukkitBlock(bukkitSign.getBlock(), status);

		return block;
		
	}
	
	boolean isNull() {
		return bukkitSign == null;
	}
	
	
}
