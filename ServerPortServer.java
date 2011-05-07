import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;


public class ServerPortServer {

	ParameterManager parameterManager = null;

	PeerServerDatabase peerServerDatabase = null;

	protected static final Logger log = Logger.getLogger("Minecraft");

	public String globalHostName = "none";
	public String serverName = "none";
	public int gamePortNum = 25565;
	public int portNum = 25465;
	public int serverTimeout = 10000;
	public String requiredIP = ".*";

	CommunicationManager communicationManager = null;

	private Integer actualPortNum = -1;

	Thread serverThread = null;
	Object serverThreadSync = new Object();
	ServerSocket listener = null;

	Object serverStopped = new Object();

	Boolean toClose = false;
	Boolean socketOpen = false;

	void setParameterManager( ParameterManager parameterManager ) {
		this.parameterManager = parameterManager;
	}

	void setCommunicationManager( CommunicationManager communicationManager ) {
		this.communicationManager = communicationManager;
	}

	void registerParameters( ParameterManager parameterManager ) {

		setParameterManager( parameterManager );

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"serverTimeout",
				"servertimeout",
				Integer.class,
				new Integer(10000),
				new String[] {
					"This parameter sets the server timeout for the server in ms",
					"It is set to 10 seconds (10000) by default"
				},
				"server timeout for server"
		)
		);

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"globalHostName",
				"globalhostname",
				String.class,
				new String("auto"),
				new String[] {
					"This parameter sets the global (internet) hostname of the server",
					"If set to auto (default), it will try to autodetect the global hostname/IP when the plugin is started",
					"If set to hostname:port, it will override the standard port"
				},
				"global hostname of the server"
		)
		);
		
		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"requiredIP",
				"requiredIP",
				String.class,
				new String(".*"),
				new String[] {
					"This is a regular expression that is matched against any players trying to teleport to the server.  If it doesn't match the player will not be allowed to transfer their inv and the teleport will fail."
				},
				"allows IP ranges to be restricted"
		)
		);

		/*		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"gamePortNum",
				"gameportnum",
				Integer.class,
				new Integer(25565),
				new String[] {
					"This parameter should be set equal to the gameport of the server",
					"This is the \"server-port\" parameter in the server.PROPERTIES file"
				},
				"gameport of the server"
		)
		);*/

		File properties = MiscUtils.dirScan( "." , "server.PROPERTIES");

		String gamePortString;
		
		if( properties == null ) {
			gamePortString = "unknown";
		} else {
			MyPropertiesFile pf = new MyPropertiesFile( properties.getPath() );

			pf.load();

			if( pf.containsKey( "server-port")) {
				gamePortString = pf.getString("server-port");
			} else {
				gamePortString = "unknown";
			}
		}
		
		if( !MiscUtils.isInt(gamePortString)) {
			MiscUtils.safeLogging("[ServerPort] Unable to determine server's gameport, using default (25565)" );
			gamePortNum = 25565;
		} else {
			gamePortNum = MiscUtils.getInt(gamePortString);
			MiscUtils.safeLogging("[ServerPort] Read gameport from server.PROPERTIES file: " + gamePortNum );
		}
		

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"portNum",
				"portnum",
				Integer.class,
				new Integer(25465),
				new String[] {
					"This parameter is the port for the plugin to use",
					"When inviting other servers you must use their port"
				},
				"ServerPort plugin port of the server"
		)
		);

		parameterManager.registerParameter(new ParameterInfo( 
				this, 
				"serverName",
				"setname",
				String.class,
				new String("none"),
				new String[] {
					"This sets the name of the current server",
					"This must be unique for all servers that are connected to each other"
				},
				"ServerPort name for the server"
		)
		);


	}

	void init() {

		checkGlobalHostName();

		if( serverThread == null || !serverThread.isAlive() ) {

			synchronized( toClose ) {
				toClose = false;
			}

			serverThread = new Thread( new Runnable() {

				public void run() {
					startServer();
				}
			}
			);
			serverThread.start();
		} else {
			MiscUtils.safeLogging("[ServerPort] Unable to start ServerPort server" );
		}


	}

	void stopServer() {
		boolean connectionSuccess;

		int portnum;
		synchronized( actualPortNum ) {
			portnum = this.actualPortNum;
		}

		if( portnum < 1024 || portnum > 65536 ) {
			MiscUtils.safeLogging("[ServerPort] No server port to close" );
			return;
		}

		synchronized( toClose ) {
			toClose = true;
		}

		Socket pulse = null;

		connectionSuccess = true;

		synchronized( serverStopped ) {
			try {
				pulse = new Socket("127.0.0.1" , portnum );
			} catch (UnknownHostException e) {
				connectionSuccess = false;
				MiscUtils.safeLogging("[ServerPort] Unable to find localhost to disable server" );
			} catch (IOException e) {
				connectionSuccess = false;
				MiscUtils.safeLogging("[ServerPort] Unable to establish localhost connection to disable server" );
			} finally {
				try {
					if( pulse != null && !pulse.isClosed() ) {
						pulse.close();
					}
				} catch (IOException e) {
					MiscUtils.safeLogging("[ServerPort] Unable to close localhost connection for disabling server" );
				}
			}

			if( connectionSuccess ) {
				try {
					serverStopped.wait();
				} catch (InterruptedException e) {}


			}
		}
	}

	void setPeerServerDatabase( PeerServerDatabase peerServerDatabase) {
		this.peerServerDatabase = peerServerDatabase;
	}

	void checkGlobalHostName() {

		if( globalHostName.equalsIgnoreCase("auto") ) {
			String hostNameLookup = MiscUtils.getHostname(MiscUtils.getGlobalIP());
			MiscUtils.safeLogging(log, "[ServerPort] Global hostname detection: " );

			if( hostNameLookup != null ) {
				globalHostName = hostNameLookup;
				MiscUtils.safeLogging(log, "[ServerPort] " + globalHostName);
			} else {
				MiscUtils.safeLogging(log, "[ServerPort] failed");
			}
			MiscUtils.safeLogging(log, "[ServerPort] use /serverport globalhostname to set a particular hostname" );
		}

	}

	ArrayList<Thread> connectionThreads = new ArrayList<Thread>();

	void startServer() {

		try {


			synchronized( socketOpen ) { 
				if( socketOpen ) {
					MiscUtils.safeLogging("[ServerPort] Socket already open" );
					return;
				}
				if( portNum < 1024 || portNum > 65535 ) {
					MiscUtils.safeLogging("[ServerPort] Port Number out of range: " + portNum );
					return;
				}

				listener = new ServerSocket( portNum );
				synchronized( actualPortNum ) {
					actualPortNum = portNum;

					if( listener != null ) {
						MiscUtils.safeLogging("[ServerPort] Bound to port " + actualPortNum + " correctly" );
						socketOpen = true;
					} 
				}
			}


			Socket server;

			while( true ) {

				server = listener.accept();

				synchronized( toClose ) {
					if( toClose ) {

						MiscUtils.safeLogging("[Serverport] Closing server listener on port " + actualPortNum );

						synchronized( socketOpen ) {
							socketOpen = false;
							server.close(); 
						}
						closeListener();
						return;
					}
				}

				ServerPortServerRequestHandler serverPortServerRequestHandler 
				= new ServerPortServerRequestHandler( 
						server , 
						serverTimeout , 
						serverName , 
						peerServerDatabase, 
						communicationManager
				);
				Thread t = new Thread( serverPortServerRequestHandler );
				synchronized( connectionThreads ) {
					connectionThreads.add(t);
				}
				t.start();

			}

		} catch (BindException be) {
			synchronized( actualPortNum ) {
				MiscUtils.safeLogging("[ServerPort] Unable to bind to port: " + actualPortNum );	
			}
		} catch (IOException ioe) {
			MiscUtils.safeLogging("[ServerPort] Server I/O exception error");	
		} finally {
			closeListener();
		}


	}

	void closeListener() {

		if( listener != null ) {
			try {
				listener.close();
			} catch (IOException e) {
				MiscUtils.safeLogging("[ServerPort] Unable to cleanly close server connection");
			}
		}

		synchronized( serverStopped ) {
			serverStopped.notifyAll();
		}

	}


}
