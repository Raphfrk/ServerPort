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

public class PeerServerInfo {

	public String name = "none";
	public String hostname = "unknown";
	public String passcode = "";
	public boolean connected = false;
	public int portnum = 25465;
	
	@Override public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(name + ",");
		sb.append(hostname + ",");
		sb.append(passcode + ",");
		sb.append(portnum + ",");
		sb.append(connected?"connected":"pending");
	
		return sb.toString();
		
	}
	
	PeerServerInfo() {
		
	}
	
	PeerServerInfo( String string ) {
		
		String[] split = string.split(",",-1);
		
		int length = split.length;
		
		if( length >= 1 ) name = split[0];
		if( length >= 2 ) hostname = split[1];
		if( length >= 3 ) passcode = split[2];
		if( length >= 4 ) portnum = MiscUtils.isInt(split[3])?MiscUtils.getInt(split[3]):25465;
		if( length >= 5 ) connected = split[4].equals("connected");
		
	}
	
	
	
	
}
