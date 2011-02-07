import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;


public class MyServer {
	
	public static org.bukkit.Server bukkitServer;
	public static JavaPlugin plugin;
	
	public static void setJavaPlugin(JavaPlugin plugin) {
		MyServer.plugin = plugin;
	}
	
	public static Server hmodServer;
	
	private static MyServer myServer = new MyServer();
	
	private boolean hmod = false;
	
	static void setHmodServer( Server server ) {
		
		MyServer.hmodServer = server;
		myServer.hmod = true;
		
	}
	

	static void setBukkitServer( org.bukkit.Server server ) {
		
		MyServer.bukkitServer = server;
		myServer.hmod = false;
		
	}

	
	public static MyServer getServer() {
		
		return myServer;
		
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
	
	World getMainWorld() {
		return bukkitServer.getWorlds().get(0);
	}
	
	void addToServerQueue( Runnable runnable, long delay ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable , delay );
			
		} else {
			
			bukkitServer.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay/50);
			
		}
		
	}

	void addToServerQueue( Runnable runnable ) {
		
		if( hmod ) {
			
			hmodServer.addToServerQueue( runnable );
			
		} else {
			bukkitServer.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
		}
		
	}
	
	void dropItem( MyLocation loc, int id, int quantity ) {
		
		if( hmod ) {
			
			hmodServer.dropItem(loc.getHmodLocation(), id, quantity);
			
		} else {
			loc.getBukkitLocation().getWorld().dropItem(loc.getBukkitLocation(), new org.bukkit.inventory.ItemStack( id, quantity ));
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
	
	int getBlockIdAt(World world, int x, int y, int z) {
		
		if( hmod ) {
			return hmodServer.getBlockIdAt(x, y, z);
		} else {
			return world.getBlockAt(x, y, z).getTypeId();
		}
	}
	
	void loadChunk(org.bukkit.World world, int x, int y, int z) {
		
		if( hmod ) {	
			hmodServer.loadChunk(x, y, z);
		} else {
			if(!world.isChunkLoaded(x>>4, z>>4)) {
				world.loadChunk(x>>4, z>>4);
			} 
		}
		
	}
	
	void setBlockAt(org.bukkit.World world, int id, int x, int y, int z) {
		
		if( hmod ) {
			hmodServer.setBlockAt(id, x, y, z);
		} else {
			org.bukkit.block.Block block = world.getBlockAt(x,y,z);
			block.setTypeId(id);
		}
	}
	
	void setBlockData(org.bukkit.World world, int x, int y, int z, int d) {
		
		if( hmod ) {
			hmodServer.setBlockData(x, y, z, d);
		} else {
			org.bukkit.block.Block block = world.getBlockAt(x,y,z);
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
	
	int getBlockData(org.bukkit.World world, int x, int y, int z) {
				
		if( hmod ) {
			return hmodServer.getBlockData(x, y, z);
		} else {
			org.bukkit.block.Block block = world.getBlockAt(x,y,z);
			return block.getData()&0xFF;
		}
		
	}
	
	MySign getComplexBlock( org.bukkit.World world, int x, int y, int z, int status ) {
		MySign sign = new MySign();

		if( hmod ) {
			sign.hmodSign = (Sign)hmodServer.getComplexBlock( x, y, z );
		} else {
			org.bukkit.block.BlockState blockState = world.getBlockAt(x,y,z).getState();
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
			return bukkitServer == null;
		}
	}
	
}