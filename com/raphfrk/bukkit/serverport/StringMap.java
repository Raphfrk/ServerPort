package com.raphfrk.bukkit.serverport;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class StringMap {

	
	HashMap<String,String> list = new HashMap<String,String>();
	
	
	@Override
	public String toString() {
		
		Iterator<String> itr = list.keySet().iterator();
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		while( itr.hasNext() ) {
			
			if( !first ) {
				sb.append(",");
			}
			first = false;
			String key = itr.next();
			sb.append(key + ";" + list.get(key));
		}
		
		return sb.toString();
		
		
	}
	
	public void setValues( String commaString ) {
		
		list = new HashMap<String,String>();
		
		String[] split = (commaString.trim()).split(",");
		
		for( String line : split ) {
			if( line.length() > 0 ) {
				String[] split2 = line.split(";");
				if(split2.length==2) {
					list.put(split2[0], split2[1]);
				}
			}
		}
		
	}
		
	String toggle( String listName , String string ) {
		
		if( string.indexOf(",") != -1 ) {
			return "Comma may not be used as within the string";
		}
		
		if( string.equals("clear")) {
			list = new HashMap<String,String>();
			return listName + " cleared";
		}
		
		String[] split = string.split(";");
		if(split.length!=2) {
			return "Please use key;value as input";
		}
		String key = split[0];
		String value = split[1];
		
		if( list.containsKey(key)) {
			String ret = key + "->" + list.get(key) + " removed from " + listName;
			list.remove(key);
			return ret;
		} else {
			list.put(key, value);
			return key + "->" + value + " added to " + listName;
		}
				
	}
	
	boolean test( String string ) {
		
		return list.containsKey(string.trim());
		
	}
	
	String getValue( String key ) {
		return list.get(key);
	}
	
}
