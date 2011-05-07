import org.bukkit.Material;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortBlockListener extends BlockListener {

	JavaPlugin serverPort = null;

	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortBlockListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}


	
	public void onBlockDamage(BlockDamageEvent event) {
		
		MyBlock block = new MyBlock();
		block.setBukkitBlock(event.getBlock(), event.getDamageLevel().getLevel());

		MyPlayer player = new MyPlayer(event.getPlayer());
		
		if( serverPortListenerCommon.onBlockDestroy(player, block) ) {
			event.setCancelled(true);
			return;
		}

		if(event.getDamageLevel() == BlockDamageLevel.DIGGING ) {

			if( event.getBlock().getType().equals(Material.WALL_SIGN) ) {

				MySign sign = new MySign();

				sign.setBukkitSign((Sign)(event.getBlock()).getState(), 0);

				serverPortListenerCommon.onSignChange(player, sign);
				
			}

		}

	}
	
    public void onBlockFlow(BlockFromToEvent event) {
    	
    	MyBlock blockFrom = new MyBlock(event.getBlock(), 0);
    	MyBlock blockTo = new MyBlock(event.getToBlock(), 0);
    	
    	if( serverPortListenerCommon.onFlow(blockFrom, blockTo) ) {
    		event.setCancelled(true);
    	}
    	
    }


    public void onBlockPhysics(BlockPhysicsEvent event) {
    	
    	MyBlock block = new MyBlock(event.getBlock(), 0);

    	if( serverPortListenerCommon.onBlockPhysics(block, false) ) {
    		event.setCancelled(true);
    	}
    	
    }

}
