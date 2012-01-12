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

public class OpenGateCommand implements Runnable {


	CommunicationManager communicationManager = null;
	String playerName = null;
	String targetServer = null;
	String targetGate = null;
	String thisGate = null;
	String thisServerName = null;
	
	boolean createGate = false;
	String gateType = "Nether";
	int dx,dz;
	double x, y, z;
	
	int duration = 10000;
	
	boolean isValid = false;

	PeerServerDatabase peerServerDatabase = null;

	synchronized boolean isValid() {
		return isValid;
	}

	OpenGateCommand( CommunicationManager communicationManager , String params ) {

		if( communicationManager != null ) {
			this.communicationManager = communicationManager;
		} else {
			isValid = false;
			return;
		}

		String[] vars = params.split(",");

		if( vars.length < 5 ) {
			isValid = false;
			return;
		}

		this.playerName = new String(vars[0]);
		this.targetServer = new String(vars[1]);
		this.targetGate = new String(vars[2]);
		this.thisGate = new String(vars[3]);
		if( MiscUtils.isInt(vars[4]) ) {
			this.duration = MiscUtils.getInt(vars[4]); 
		} else {
			isValid = false;
			return;
		}
		
		if( this.playerName == null || this.targetGate == null || this.targetServer == null || this.thisGate == null ) {
			isValid = false;
			return;
		}
				
		if( vars.length > 7 && MiscUtils.isDouble(vars[5]) && MiscUtils.isDouble(vars[6]) && MiscUtils.isDouble(vars[7])) {
			x = MiscUtils.getDouble(vars[5]);
			y = MiscUtils.getDouble(vars[6]);
			z = MiscUtils.getDouble(vars[7]);
		} else {
			isValid = false;
			return;
		}
		
		if( vars.length > 8 && MiscUtils.isBoolean(vars[8])) {
			createGate = MiscUtils.getBoolean(vars[8]);
		} else {
			isValid = false;
			return;
		}
		
		if( vars.length > 10 && MiscUtils.isInt(vars[9]) && MiscUtils.isInt(vars[10])) {
			dx = MiscUtils.getInt(vars[9]);
			dz = MiscUtils.getInt(vars[10]);
		} else {
			isValid = false;
			return;
		}
		
		if( vars.length > 11 ) {
			gateType = new String(vars[11]);
		} else {
			isValid = false;
			return;
		}
		
		if( vars.length > 12 ) {
			thisServerName = new String(vars[12]);
		}
		
		isValid = true;

	}


	public void run() {
		

		PeerServerInfo peerServerInfoFromConnection = null;
		PeerServerInfo peerServerInfoFromDatabase = null;

		peerServerDatabase = communicationManager.peerServerDatabase;

		ServerPortClient serverPortClient = new ServerPortClient( communicationManager , targetServer );
		String error;

		if( (error=serverPortClient.connect()) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
			return;
		}
		
		synchronized( peerServerDatabase ) {

			peerServerInfoFromConnection = new PeerServerInfo();

			if( (error=serverPortClient.getPeerServerInfo(
					peerServerInfoFromConnection, 
					peerServerDatabase)) 

					!= null ) {
				MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
				return;
			}

			peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);

		}

		if( peerServerInfoFromDatabase == null ) {

			MiscUtils.safeMessage(playerName, "[ServerPort] Server " + targetServer + " is not known" );
			
		} else {

			String reply = serverPortClient.sendRequest( 
					"OPENGATE" , 
					playerName + "," + 
					targetGate + "," + 
					duration + "," +
					createGate + "," + 
					gateType + "," + 
					dx + "," + 
					dz + "," + 
					x + "," + 
					y + "," + 
					z + "," +
					thisServerName, 
					peerServerInfoFromDatabase , communicationManager.verbose);

			if( MiscUtils.errorCheck(reply) != null ) {
				MiscUtils.safeMessage(playerName, MiscUtils.errorCheck(reply) );
				serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
				return;
			} 
			
			if( reply.indexOf("GATEBUILT") == 0) {
				
				MiscUtils.safeMessage(playerName, "[ServerPort] Gate Constructed at target server");
				
			}
			
			if( reply.indexOf("GATEOPENED") == 0 || reply.indexOf("GATEBUILT") == 0) {
				
				String[] vars = reply.split(",");
				int repliedDuration = -1;
				if( vars.length == 2 && MiscUtils.isInt(vars[1])) {
					repliedDuration = MiscUtils.getInt(vars[1]);
				}
				String commandString = "OPENGATE:" + playerName + "," + thisGate + "," + repliedDuration ;
				communicationManager.commandFIFO.runMainCommand(commandString);
				
			} else {
				
				if( reply.equals("NOGATE")) {
					MiscUtils.safeMessage(playerName, "[ServerPort] The gate doesn't exist on the target server");
				} else if( reply.equals("CHUNKGENFAIL" ) ) {
					MiscUtils.safeMessage(playerName, "[ServerPort] Cannot create destination gate as exit point doesn't exist on target server. Will try again in 10 seconds, please wait.");
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
					
					String reply2 = serverPortClient.sendRequest( 
							"OPENGATE" , 
							playerName + "," + 
							targetGate + "," + 
							duration + "," +
							createGate + "," + 
							gateType + "," + 
							dx + "," + 
							dz + "," + 
							x + "," + 
							y + "," + 
							z + "," +
							thisServerName, 
							peerServerInfoFromDatabase , communicationManager.verbose);
					
					if( reply2.indexOf("GATEBUILT") == 0) {
						
						MiscUtils.safeMessage(playerName, "[ServerPort] Gate Constructed at target server");
						
					}
					
					if( reply2.indexOf("GATEOPENED") == 0 || reply2.indexOf("GATEBUILT") == 0) {
						
						String[] vars = reply2.split(",");
						int repliedDuration = -1;
						if( vars.length == 2 && MiscUtils.isInt(vars[1])) {
							repliedDuration = MiscUtils.getInt(vars[1]);
						}
						
						communicationManager.commandFIFO.runMainCommand("OPENGATE:" + playerName + "," + thisGate + "," + repliedDuration );
						
					} else {
						MiscUtils.safeMessage(playerName, "[ServerPort] Second attempt failed, please try again manually");
					}
					
					serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
					return;
				} else if ( reply.equals("CHUNKGENBAN" ) ) {
					MiscUtils.safeMessage(playerName, "[ServerPort] Admin on target server has banned automatic portal creation outside of already created chunks");
					MiscUtils.safeMessage(playerName, "[ServerPort] Try building nearer the spawn point");
					serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
					return;
				} else if ( reply.equals("BUILDBANNED" ) ) {
					MiscUtils.safeMessage(playerName, "[ServerPort] Admin on target server has banned automatic portal creation");
					serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
					return;
				} else {
					MiscUtils.safeMessage(playerName, "Reply was: " + reply);
					serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose);
					return;
				} 
				
			}


		}

		if( (error = serverPortClient.close(peerServerInfoFromConnection, communicationManager.verbose) ) != null ) {
			MiscUtils.safeMessage(playerName, "[ServerPort] " + error);
		}

	}




}
