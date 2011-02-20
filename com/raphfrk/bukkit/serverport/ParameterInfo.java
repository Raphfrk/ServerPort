package com.raphfrk.bukkit.serverport;

public class ParameterInfo {

	Object location;
	String fieldName;
	String commandName;
	Class type;
	Object defaultValue;
	String[] longHelp;
	String   shortHelp;
	
	ParameterInfo( 
			Object location,
			String fieldName,
			String commandName,
			Class type,
			Object defaultValue,
			String[] longHelp,
			String   shortHelp
			) {
		this.location = location;
		this.fieldName = fieldName;
		this.commandName = commandName;
		this.type = type;
		this.defaultValue = defaultValue;
		this.longHelp = longHelp;
		this.shortHelp = shortHelp;
	}
	
	
}
