import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;


public class ServerPortServerRequestHandler implements Runnable {

	Socket connection;
	int timeout;
	PeerServerInfo peerServerInfoFromConnection = null;
	PeerServerInfo peerServerInfoFromDatabase = null;
	PeerServerDatabase peerServerDatabase = null;
	CommunicationManager communicationManager = null;
	static final String slash = System.getProperty("file.separator");

	
	String peerSession = null;
	String session = null;

	String serverName;
	
	String peerServerName = null;

	BufferedReader in = null;
	BufferedWriter out = null;

	ServerPortServerRequestHandler( Socket connection , int timeout , String serverName , PeerServerDatabase peerServerDatabase, CommunicationManager communicationManager) {

		this.connection = connection;
		this.timeout = timeout;
		this.serverName = serverName;
		this.peerServerDatabase = peerServerDatabase;
		this.communicationManager = communicationManager;

	}

	public void run() {

		if( !setTimeout() || !openConnection() ) {
			closeConnection();
			return;
		}

		if( !getPeerServerInfo( in , out ) ) {
			closeConnection();
			return;
		}

		String command = getCommand();

		String[] split = command.split(":");
		
		while( !split[0].equals("END")) {
			
			String[] vars = (split.length>1)?split[1].split(","):(new String[] {""});
			
			MiscUtils.safeLogging( "[ServerPort] Command received: " + command);
			
			if( command.indexOf(slash)!=-1 || command.indexOf("/") !=-1 || command.indexOf("\\") != -1) {
				MiscUtils.safeLogging("[ServerPort] WARNING connection from " + peerServerInfoFromConnection.hostname + " used file/dir separator character");
				MiscUtils.safeLogging("[ServerPort] Breaking connection");
				closeConnection();
				return;
			}
						
			if( command.equals("BADHASH")) {
				sendReply( "Error: Incorrect passcode used for connection" );
				MiscUtils.safeLogging("Connection from " + peerServerInfoFromConnection.hostname + " sent command with a bad HASH");
				closeConnection();
				return;
			} else if ( command.equals("BADIO")) {
				MiscUtils.safeLogging("IO error for connection from " + peerServerInfoFromConnection.hostname + " sent command with a bad HASH");
				closeConnection();
				return;
			} 

			if( split.length != 2 ) {
				MiscUtils.safeLogging(peerServerInfoFromConnection.hostname + " sent bad command: " + command);
				closeConnection();
				return;
			}

			if ( split[0].equals("PASSCODE") ) {
				MiscUtils.safeLogging(
						"[ServerPort] " + peerServerInfoFromConnection.name + " (" +  
						connection.getInetAddress().getHostAddress() + 
						") sent passcode creation request: " + split[1]);

				String passcodeExpanded = split[1] + MiscUtils.genRandomCode();

				if( peerServerInfoFromConnection.connected ) {
					passcodeExpanded = "";
					MiscUtils.safeLogging("[ServerPort] Passcode already stored, sending blank passcode");
				} else {
					passcodeExpanded = split[1] + MiscUtils.genRandomCode();
					MiscUtils.safeLogging("[ServerPort] New passcode generated: " + passcodeExpanded );
				}
				
				if( sendReply( passcodeExpanded ) && ( passcodeExpanded.length() > 0 ) ) {
					peerServerInfoFromConnection.passcode = passcodeExpanded;
					peerServerInfoFromConnection.portnum = -1;
					peerServerDatabase.setServer( peerServerInfoFromConnection );
				}

			} else if ( split[0].equals("CONNECT") ) {
				
				if( peerServerInfoFromConnection.portnum == -1 ) {
					sendReply( "NO" );
				} else {
					if( sendReply( "OK") ) {
						peerServerInfoFromConnection.connected = true;
						peerServerDatabase.setServer( peerServerInfoFromConnection );
					}
				}

			} else if ( split[0].equals("OPENGATE") ) {
				
				sendReply( communicationManager.commandFIFO.runMainCommand( new String(command)));

			} else if ( split[0].equals("TRANSFERINV") ) {
			
				sendReply( communicationManager.commandFIFO.runMainCommand(new String(command)));
				
			} else if ( split[0].equals("GATETEST") ) {
				
				sendReply( communicationManager.commandFIFO.runMainCommand(new String(command)));
				
			} else if ( split[0].equals("TELEPORT") ) {
				
				sendReply( communicationManager.commandFIFO.runMainCommand(new String(command)));
				
			} else if ( split[0].equals("FORWARD") ) {
				
				sendReply( communicationManager.commandFIFO.runMainCommand(new String(command)));
				
			} else if ( split[0].equals("MESSAGE") ) { 

				sendReply( communicationManager.commandFIFO.runMainCommand(new String(command + ";" + peerServerName )));
				
			} else if ( split[0].equals("ERROR")) {
				MiscUtils.safeLogging("[ServerPort] Client replied that passcode is invalid" );
				sendReply( "Error acknowledged" );
			}

			
			command = getCommand();

			split = command.split(":");
			
		}

		closeConnection();

	}

	void closeConnection() {
		try {
			connection.close();
		} catch (IOException e) {
			MiscUtils.safeLogging("[ServerPort] Unable to close connection from " + connection.getInetAddress().getHostAddress() );
			return;

		}
	}

	boolean setTimeout() {
		try {
			connection.setSoTimeout(timeout);
		} catch (SocketException e) {
			MiscUtils.safeLogging("[ServerPort] Unable to set timeout on socket");
			return false;
		}
		return true;
	}

	boolean openConnection() {
		try {
			in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
		} catch (IOException e1) {
			MiscUtils.safeLogging("[ServerPort] Unable to open in connection from " + connection.getInetAddress().getHostAddress() );
			try {
				if( in != null ) {
					in.close();
				}
			} catch (IOException e) {
				MiscUtils.safeLogging("[ServerPort] Connection close failed");
			}
			return false;

		}

		try {
			out = new BufferedWriter(
					new OutputStreamWriter(
							connection.getOutputStream()));
		} catch (IOException ioe) {
			MiscUtils.safeLogging("[ServerPort] Unable to open out connection from " + connection.getInetAddress().getHostAddress() );
			try {
				if( out != null ) {
					out.close();
				}
			} catch (IOException e) {
				MiscUtils.safeLogging("[ServerPort] Connection close failed");
			}
			return false;
		}

		return true;
	}

	boolean getPeerServerInfo( BufferedReader in , BufferedWriter out) {

		peerServerInfoFromConnection = new PeerServerInfo();

		try {
			peerServerInfoFromConnection.name = in.readLine();
			out.write(serverName);
			out.newLine();
			out.flush();
			peerSession = in.readLine();
			session = MiscUtils.genRandomCode();
			out.write(session);
			out.newLine();
			out.flush();
			peerServerInfoFromConnection.connected = in.readLine().equals("connected");
			if( serverName.equals(peerServerInfoFromConnection.name)) {
				MiscUtils.safeLogging("[ServerPort] Server attempted to connect to itself");
				return false;
			}

			peerServerName = peerServerInfoFromConnection.name;
			synchronized(peerServerDatabase) {
				
				peerServerInfoFromDatabase = peerServerDatabase.getServer(peerServerInfoFromConnection.name);
				
				if( peerServerInfoFromDatabase == null || !peerServerInfoFromDatabase.connected ) {
					peerServerInfoFromConnection.connected = false;
					if( peerServerInfoFromDatabase == null ) {
						peerServerInfoFromConnection.passcode = "";
						peerServerInfoFromConnection.portnum = -1;
					} else {
						peerServerInfoFromConnection.passcode = peerServerInfoFromDatabase.passcode;
						peerServerInfoFromConnection.portnum = peerServerInfoFromDatabase.portnum;
						peerServerInfoFromConnection.hostname = peerServerInfoFromDatabase.hostname;
					}
					
				} else {
					peerServerInfoFromConnection.connected = true;
					peerServerInfoFromConnection.passcode = peerServerInfoFromDatabase.passcode;
					peerServerInfoFromConnection.portnum = peerServerInfoFromDatabase.portnum;
					peerServerInfoFromConnection.hostname = peerServerInfoFromDatabase.hostname;		
				}
			}

		} catch (IOException e) {
			if( peerServerInfoFromConnection.name != null && !peerServerInfoFromConnection.name.equals("none")) {
				MiscUtils.safeLogging("[ServerPort] Connection handshake failed for server claiming to be: " + peerServerInfoFromConnection.name);
			} else {
				MiscUtils.safeLogging("[ServerPort] Connection handshake failed for unknown server" );
			}
			return false;
		}

		return true;
	}

	String getCommand() {

		try {
			String command = in.readLine();
			String commandHash = in.readLine();
			String expectedHash = MiscUtils.sha1Hash(peerServerInfoFromConnection.passcode + command + session + peerSession);

			if( expectedHash.equals(commandHash)) {
				return command;
			} else {
				return "BADHASH";
			}
		} catch (IOException ioe ) {
			return "BADIO";
		}

	}

	boolean sendReply( String reply ) {

		MiscUtils.safeLogging( "[ServerPort] Sending reply: " + reply);
		
		try {

			out.write(reply);
			out.newLine();
			out.write(MiscUtils.sha1Hash(peerServerInfoFromConnection.passcode + reply + session + peerSession));
			out.newLine();
			out.flush();
		} catch (IOException ioe ) {
			ioe.printStackTrace();
			return false;
		}

		return true;

	}


}
