import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;


public class ServerPortEntityListener extends EntityListener implements Listener {
	
	JavaPlugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortEntityListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		
		//System.out.println( "Damage event" + event);
		
		Entity entity = event.getEntity();
		
		if( entity == null ) return;
		
		if( !( entity instanceof Player )) return;
		
		Player player = (Player)entity;
		
		int currentHealth = player.getHealth();
		int newHealth = currentHealth - event.getDamage();
		
		if( serverPortListenerCommon.onHealthChange(new MyPlayer(player), currentHealth , newHealth ) ) {
			
			event.setCancelled(true);
			
		}

	
	}
	
	/*@Override
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
		System.out.println( "Block Damage event" + event);
    }

	@Override
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		System.out.println( "By entiry damage event" + event);
    }
    
	@Override
	public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
		System.out.println( "By projectile damage event" + event);
    }
    
	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		System.out.println( "By combust damage event" + event);
    }

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		System.out.println( "By explode damage event" + event);
    }*/

}

