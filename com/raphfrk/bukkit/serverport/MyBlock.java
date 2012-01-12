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
