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
