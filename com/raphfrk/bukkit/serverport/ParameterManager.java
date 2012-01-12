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
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;


public class ParameterManager {
	
	CommunicationManager communicationManager;
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	MyServer server = MyServer.getServer();
	
	String propertiesFilename;
	
	public FakeParamClass fakeHelp = new FakeParamClass();
	
	MyPropertiesFile pfLocal = null;
	
	ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
	
	boolean helpRegistered = false;
	
	void registerParameter( ParameterInfo parameterInfo ) {
		
		if( !helpRegistered ) {

			parameters.add(new ParameterInfo( 
						this, 
						"fakeHelp",
						"help",
						fakeHelp.getClass(),
						new Integer(2000),
						new String[] {
							"Use help to see all commands",
							"Use help <number> to see page <number> of command list",
							"Use help <command> to see detailed information"
						},
						"gives information on commands"
				)
			);
			helpRegistered = true;
		}
		
		parameters.add(parameterInfo);
		
	}
	
	synchronized void setPropertiesFilename( String propertiesFilename ) {
		
		this.propertiesFilename = propertiesFilename;
		
	}
	
	synchronized void setCommunicationManager( CommunicationManager communicationManager ) {
		this.communicationManager = communicationManager;
	}
	
	synchronized boolean processCommand( MyPlayer player , String[] split ) {
		
		if( split.length > 1 && split[0].equals("invite") ) {

			String hostname = split[1];
			String[] hostsplit = hostname.split(":",-1);

			int portnum = 25465;
			
			if( hostsplit.length > 1 ) {

				if( !MiscUtils.isInt(hostsplit[1]) ) {
					MiscUtils.safeMessage(player, "Unable to parse port number for target server");
					return true;
				} else {
					portnum = MiscUtils.getInt(hostsplit[1]);
					hostname = hostsplit[0];
				}
			} else if( split.length > 2 ) {
				if( !MiscUtils.isInt(split[2]) ) {
					MiscUtils.safeMessage(player, "Unable to parse port number for target server");
					return true;
				} else {
					portnum = MiscUtils.getInt(split[2]);
				}

			}
			communicationManager.attemptInvite( player.getName() , hostname , portnum );
			MiscUtils.safeMessage(player, "[ServerPort] Attempting to connect to " + hostname + " on port " + portnum);
			return true;
		}
		
		if( split.length < 1 || (split[0].equalsIgnoreCase("help") && split.length == 1) ) {
			
			MiscUtils.safeMessage(player, "Command list");
			
			Iterator<ParameterInfo> itr = parameters.iterator();
			
			StringBuilder sb = new StringBuilder("");
			
			while( itr.hasNext() ) {
				ParameterInfo current = itr.next();
				sb.append( ", " + current.commandName );
			}
			
			MiscUtils.safeMessage(player, sb.toString() );
			
			return true;
			
		}
		
		if( split.length > 1 && split[0].equals("help") ) {
			
			if( MiscUtils.isInt(split[1])) {
				
				MiscUtils.safeMessage(player, "");
				MiscUtils.safeMessage(player, server.getColor("Green") + "ServerPort Command List Page " + MiscUtils.getInt(split[1]));
				MiscUtils.safeMessage(player, "");
				int cnt=0;
				int target = 8*MiscUtils.getInt(split[1])-8;
				
				Iterator<ParameterInfo> itr = parameters.iterator();
				while( itr.hasNext() && cnt < target + 8 ) {
					ParameterInfo current = itr.next();
					if( cnt>=target ) {
						MiscUtils.safeMessage(player, 
								server.getColor("LightBlue") + current.commandName + 
								server.getColor("White") + ": " + current.shortHelp );
					}
					cnt++;
				}
				return true;
			}
			
			
			Iterator<ParameterInfo> itr = parameters.iterator();
			
			while( itr.hasNext() ) {
				ParameterInfo current = itr.next();
				if( split[1].equalsIgnoreCase(current.commandName) ) {
					MiscUtils.safeMessage(player, current.commandName );
					MiscUtils.safeMessage(player, "" );
					for( String line : current.longHelp ) {
						MiscUtils.safeMessage( player , line );
					}
					return true;
					
				}
			}
	
		}
		
		if( split.length > 1 ) {
			
			if( parameterExists( split[0] ) ) {
				
				if( !setParameter( split[0], split[1] ) ) {
					MiscUtils.safeMessage(player, "[Serverport] Unable to set parse parameter value");
				} else {
					MiscUtils.safeMessage(player, "[Serverport] " + split[0] + " set to " + getParameter( split[0] ));
				}
				return true;
				
			} else {
				return false;
			}
			
		} else if ( split.length == 1 ) {
			if( parameterExists( split[0] ) ) {

				MiscUtils.safeMessage(player, "[Serverport] " + split[0] + " set to " + getParameter( split[0] ));
				return true;

			} else {
				return false;
			}
		}

		return false;
		
	}
	
	synchronized boolean parameterExists( String parameterName ) {

		Iterator<ParameterInfo> itr = parameters.iterator();

		while( itr.hasNext() ) {

			ParameterInfo current = itr.next();

			if( current.commandName.equals(parameterName) ) {

				return true;

			}

		}
		
		return false;

	}
	
	synchronized void loadParameters() {
	
		File correctLocation= new File(MyServer.plugin.getDataFolder(), propertiesFilename);
		File oldLocation = new File(propertiesFilename);
		
		if (!correctLocation.exists() && oldLocation.exists()) {
			MiscUtils.safeLogging(log, "Moving " + oldLocation + " to " + correctLocation);
			oldLocation.renameTo(correctLocation);
		}
		
		MyPropertiesFile pf = new MyPropertiesFile( correctLocation.getPath() );
		
		pf.load();
		
		Iterator<ParameterInfo> itr = parameters.iterator();
		
		while( itr.hasNext() ) {
			loadParameter(pf , itr.next());
		}
		
	}
	
	synchronized void loadParameter( MyPropertiesFile pf , ParameterInfo parameterInfo ) {
		
		pfLocal = pf;
		
		String fieldName = parameterInfo.fieldName;
		
		Field paramField = null;
		
		try {
			paramField = (parameterInfo.location.getClass()).getField(fieldName);
		} catch (NoSuchFieldException nsfe) {
			MiscUtils.safeLogging(log, "[ServerPort] Unable to find field: " + fieldName + " of " + parameterInfo.location.getClass().getName());
			return;
		}
		
		Class type = parameterInfo.type;
				
		try {
		if( type.equals(Integer.class)) {
			Integer value = pf.getInt( parameterInfo.commandName , (Integer)parameterInfo.defaultValue);
			paramField.set(parameterInfo.location, value);
		} else if( type.equals(Long.class)) {
			Long value = pf.getLong( parameterInfo.commandName , (Long)parameterInfo.defaultValue);
			paramField.set(parameterInfo.location, value);
		} else if( type.equals(Boolean.class)) {
			Boolean value = pf.getBoolean( parameterInfo.commandName , (Boolean)parameterInfo.defaultValue);
			paramField.set(parameterInfo.location, value);
		} else if( type.equals(Double.class)) {
			Double value = pf.getDouble( parameterInfo.commandName , (Double)parameterInfo.defaultValue);
			paramField.set(parameterInfo.location, value);
		} else if( type.equals(String.class)) {
			String value = pf.getString( parameterInfo.commandName , (String)parameterInfo.defaultValue);
			paramField.set(parameterInfo.location, value);
		} else if( type.equals(StringList.class)) {
			String value = pf.getString( parameterInfo.commandName , (String)parameterInfo.defaultValue);
			try {
				Method setValues = StringList.class.getMethod("setValues", new Class[] { String.class } );
				try {
					setValues.invoke(paramField.get(parameterInfo.location), new Object[] { value } );
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					MiscUtils.safeLogging("[ServerPort] Illegal Arguments for setValues");
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					MiscUtils.safeLogging("[ServerPort] Illegal target for setValues");
				}
				
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				MiscUtils.safeLogging("[ServerPort] Unable to find setValues method");
			}
		} else if( type.equals(StringMap.class)) {
			String value = pf.getString( parameterInfo.commandName , (String)parameterInfo.defaultValue);
			try {
				Method setValues = StringMap.class.getMethod("setValues", new Class[] { String.class } );
				try {
					setValues.invoke(paramField.get(parameterInfo.location), new Object[] { value } );
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					MiscUtils.safeLogging("[ServerPort] Illegal Arguments for setValues");
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					MiscUtils.safeLogging("[ServerPort] Illegal target for setValues");
				}
				
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				MiscUtils.safeLogging("[ServerPort] Unable to find setValues method");
			}
		} else if( type.equals(FakeParamClass.class)) {
			
		} else {
			MiscUtils.safeLogging(log, "[ServerPort] Unable to handle " + type.getName() + " for parameters");
		}
		} catch (IllegalAccessException iae) {
			MiscUtils.safeLogging(log, "[ServerPort] unable to update memory for parameter " + fieldName );
			
		}
		
	}
	
	synchronized boolean setParameter( String parameterName , String value ) {
		
		Iterator<ParameterInfo> itr = parameters.iterator();
		
		while( itr.hasNext() ) {
			
			ParameterInfo current = itr.next();
			
			if( current.commandName.equals(parameterName) ) {
				
				return setParameter( current , value );
				
			}
			
		}
		
		MiscUtils.safeLogging(log, "[ServerPort] attempted to set unknown paramer " + parameterName );
		return false;
	}
	
	synchronized boolean setParameter( ParameterInfo parameterInfo , String string ) {

		String fieldName = parameterInfo.fieldName;

		Field paramField = null;

		try {
			paramField = (parameterInfo.location.getClass()).getField(fieldName);
		} catch (NoSuchFieldException nsfe) {
			MiscUtils.safeLogging(log, "[ServerPort] Unable to find field: " + fieldName + " of " + parameterInfo.location.getClass().getName());
			return false;
		}

		Class type = parameterInfo.type;
		
		try {
			Object value;
			if( type.equals(Integer.class)) {
				if( !MiscUtils.isInt(string) ) {
					return false;
				} else {
					value = MiscUtils.getInt(string);
				}
				pfLocal.setInt(parameterInfo.commandName, (Integer)value);
				pfLocal.save();
				paramField.set(parameterInfo.location, (Integer)value);
				return true;
			} else if( type.equals(Boolean.class)) {
				if( !MiscUtils.isBoolean(string) ) {
					return false;
				} else {
					value = MiscUtils.getBoolean(string);
				}
				pfLocal.setBoolean(parameterInfo.commandName, (Boolean)value);
				pfLocal.save();
				paramField.set(parameterInfo.location, value);
				return true;
			} else if( type.equals(Long.class)) {
				if( !MiscUtils.isLong(string) ) {
					return false;
				} else {
					value = MiscUtils.getLong(string);
				}
				pfLocal.setLong(parameterInfo.commandName, (Long)value);
				pfLocal.save();
				paramField.set(parameterInfo.location, value);
				return true;
			} else if( type.equals(Double.class)) {
				if( !MiscUtils.isDouble(string) ) {
					return false;
				} else {
					value = MiscUtils.getDouble(string);
				}
				pfLocal.setDouble(parameterInfo.commandName, (Double)value);
				pfLocal.save();
				paramField.set(parameterInfo.location, value);
				return true;
			} else if( type.equals(String.class)) {
				value = string;
				pfLocal.setString(parameterInfo.commandName, (String)value);
				pfLocal.save();
				paramField.set(parameterInfo.location, value);
				return true;
			} else if( type.equals(StringList.class)) {
				StringList stringList = (StringList)paramField.get(parameterInfo.location);
				
				stringList.toggle(parameterInfo.commandName, string);
				
				value = stringList.toString();
				pfLocal.setString(parameterInfo.commandName, (String)value);
				pfLocal.save();
				
				return true;
			} else if( type.equals(StringMap.class)) {
				StringMap stringMap = (StringMap)paramField.get(parameterInfo.location);
				
				stringMap.toggle(parameterInfo.commandName, string);
				
				value = stringMap.toString();
				pfLocal.setString(parameterInfo.commandName, (String)value);
				pfLocal.save();
				
				return true;
			} else if( type.equals(FakeParamClass.class)) {
				return false;
			} else {
				MiscUtils.safeLogging(log, "[ServerPort] Unable to handle " + type.getName() + " for parameters");
				return false;
			}
		} catch (IllegalAccessException iae) {
			MiscUtils.safeLogging(log, "[ServerPort] unable to update memory for parameter " + fieldName );
			return false;
		}

	}
	
	
	synchronized String getParameter( String parameterName ) {
		
		Iterator<ParameterInfo> itr = parameters.iterator();
		
		while( itr.hasNext() ) {
			
			ParameterInfo current = itr.next();
			
			if( current.commandName.equals(parameterName) ) {
				
				return getParameter( current );
				
			}
			
		}
		
		MiscUtils.safeLogging(log, "[ServerPort] attempted to get unknown paramer " + parameterName );
		return "unknown";
	}
	
	synchronized String getParameter( ParameterInfo parameterInfo  ) {

		String fieldName = parameterInfo.fieldName;

		Field paramField = null;

		try {
			paramField = (parameterInfo.location.getClass()).getField(fieldName);
		} catch (NoSuchFieldException nsfe) {
			MiscUtils.safeLogging(log, "[ServerPort] Unable to find field: " + fieldName + " of " + parameterInfo.location.getClass().getName());
			return "unable to find field";
		}
		
		try {
			return paramField.get(parameterInfo.location).toString();
		} catch (IllegalAccessException iae) {
			MiscUtils.safeLogging(log, "[ServerPort] unable to update memory for parameter " + fieldName );
			return "unable to access memory for parameter";
		}

	}

	
	

}
