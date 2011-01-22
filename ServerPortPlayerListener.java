import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;


public class ServerPortPlayerListener extends PlayerListener {
	
	JavaPlugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortPlayerListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, ItemStack itemInHand) {

		MyItem newItem = new MyItem();
		newItem.setBukkitItem(itemInHand, 0);
		
		return serverPortListenerCommon.onBlockPlace(new MyPlayer(player), new MyBlock(blockPlaced, 0), new MyBlock(blockClicked, 0), newItem );
		
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		
		org.bukkit.entity.Player player = event.getPlayer();
		
		MyPlayer myPlayer = new MyPlayer(player);
		
		serverPortListenerCommon.onLogin(myPlayer);
    }
	
    public void onPlayerChat(PlayerChatEvent event) {
    	
    	MyPlayer player = new MyPlayer( event.getPlayer() );
    	
    	serverPortListenerCommon.onChat(player, event.getMessage());
    	
    }
	
    public void onPlayerCommand(PlayerChatEvent event) {
    	
    	String[] split = event.getMessage().split(" ",-1);
    	
    	if( split[0].equals("/clear")) {
    		event.getPlayer().getInventory().clear(Integer.parseInt(split[1]));
    		event.setCancelled(true);
    		return;
    	}

    	MyPlayer player = new MyPlayer(event.getPlayer());

    	boolean ret = serverPortListenerCommon.onCommand(player, split);
    	
    	if( ret ) {
    		event.setCancelled(true);
    	}
    	
    	
    }
    
    public void onPlayerMove(PlayerMoveEvent event) {
    	
    	MyPlayer player = new MyPlayer(event.getPlayer());
    	MyLocation from = new MyLocation(event.getFrom());
    	MyLocation to = new MyLocation(event.getTo());
    	
    	
    	serverPortListenerCommon.onPlayerMove(player, from, to);   	
    	if( player.getTeleportTo() != null ) {
    		event.setTo(player.getTeleportTo());
    	}
    	
    }
    
	

}
