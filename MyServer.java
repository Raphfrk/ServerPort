
public class MyServer {
	
	public static org.bukkit.Server bukkitServer;
	public static org.bukkit.World bukkitWorld;
	
	public static Server hmodServer;
	
	private static MyServer myServer = new MyServer();
	
	private boolean hmod = false;
	
	static void setHmodServer( Server server ) {
		
		MyServer.hmodServer = server;
		myServer.hmod = true;
		
	}
	

	static void setBukkitServer( org.bukkit.World world, org.bukkit.Server server ) {
		
		MyServer.bukkitWorld = world;
		MyServer.bukkitServer = server;
		myServer.hmod = false;
		
	}

	
	public static MyServer getServer() {
		
		return myServer;
		
	}
	
	public org.bukkit.World getBukkitWorld() {
		
		return bukkitWorld;
		
	}
	
	public boolean getHmod() {
		return hmod;
	}
	
	boolean isPlayerListEmpty() {
		if( hmod ) {
			return hmodServer.getPlayerList().isEmpty();
		} else {
			return bukkitServer.getOnlinePlayers().length == 0;
		}
	}
	
	void messageAll( String message ) {
		
		if( message == null ) return;
		
		if( hmod ) {
			hmodServer.messageAll( message );
		} else {
			for( org.bukkit.entity.Player player : bukkitServer.getOnlinePlayers() ) {
				player.sendMessage(message);
			}
		}
	}
	
	void addToServerQueue( Runnable runnable, long delay ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable , delay );
			
		} else {
			
			RunnableEvent runnableEvent = new RunnableEvent(runnable);
			bukkitServer.getAsyncEventManager().callAsyncEvent(runnableEvent,delay);
			
		}
		
	}

	void addToServerQueue( Runnable runnable ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable );
			
		} else {
			RunnableEvent runnableEvent = new RunnableEvent(runnable);
			bukkitServer.getAsyncEventManager().callAsyncEvent(runnableEvent);
		}
		
	}
	
	void dropItem( MyLocation loc, int id, int quantity ) {
		
		if( hmod ) {
			
			hmodServer.dropItem(loc.getHmodLocation(), id, quantity);
			
		} else {
			bukkitWorld.dropItem(loc.getBukkitLocation(), new org.bukkit.inventory.ItemStack( id, quantity ));
		}
		
	}
	
	MyPlayer getPlayer( String name ) {
		
		MyPlayer player = new MyPlayer();
		
		if( name == null ) return player;
		
		if( hmod ) {
			
			player.setHmodPlayer(hmodServer.getPlayer(name));
			
			
		} else {
			player.setBukkitPlayer(bukkitServer.getPlayer(name));
		}
		
		return player;

		
	}
	
	int getBlockIdAt(int x, int y, int z) {
		
		if( hmod ) {
			return hmodServer.getBlockIdAt(x, y, z);
		} else {
			return bukkitWorld.getBlockAt(x, y, z).getTypeId();
		}
	}
	
	void loadChunk(int x, int y, int z) {
		
		if( hmod ) {	
			hmodServer.loadChunk(x, y, z);
		} else {
			org.bukkit.Chunk chunk = bukkitWorld.getChunkAt(x>>4, z>>4);
			bukkitServer.getWorlds()[0].loadChunk(chunk);
		}
		
	}
	
	void setBlockAt(int id, int x, int y, int z) {
		
		if( hmod ) {
			hmodServer.setBlockAt(id, x, y, z);
		} else {
			org.bukkit.block.Block block = bukkitWorld.getBlockAt(x,y,z);
			block.setTypeId(id);
		}
	}
	
	void setBlockData(int x, int y, int z, int d) {
		
		if( hmod ) {
			hmodServer.setBlockData(x, y, z, d);
		} else {
			org.bukkit.block.Block block = bukkitWorld.getBlockAt(x,y,z);
			block.setData((byte)d);
		}
	}

	String getColor(String color) {

		if( hmod ) {
			if( color.equals("Green")) return Colors.Green;
			if( color.equals("LightBlue")) return Colors.LightBlue;
			if( color.equals("White")) return Colors.White;
			return "";
		} else {
			if( color.equals("Green"))     return org.bukkit.ChatColor.GREEN.toString();
			if( color.equals("LightBlue")) return org.bukkit.ChatColor.AQUA.toString();
			if( color.equals("White"))     return org.bukkit.ChatColor.WHITE.toString();
			return "";
		}
	}
	
	int getBlockData(int x, int y, int z) {
				
		if( hmod ) {
			return hmodServer.getBlockData(x, y, z);
		} else {
			org.bukkit.block.Block block = bukkitWorld.getBlockAt(x,y,z);
			return block.getData()&0xFF;
		}
		
	}
	
	MySign getComplexBlock( int x, int y, int z, int status ) {
		MySign sign = new MySign();

		if( hmod ) {
			sign.hmodSign = (Sign)hmodServer.getComplexBlock( x, y, z );
		} else {
			org.bukkit.block.BlockState blockState = bukkitWorld.getBlockAt(x,y,z).getState();
			if( !blockState.getType().equals(org.bukkit.Material.WALL_SIGN) ) {
				sign.bukkitSign = null;
			} else {
				sign.bukkitSign = (org.bukkit.block.Sign)blockState;
				sign.status = status;
			}
		}
		return sign;
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodServer == null;
		} else {
			return bukkitWorld == null || bukkitServer == null;
		}
	}
	
}