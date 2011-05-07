import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;


public class ServerPortWorldListener extends WorldListener {
	JavaPlugin serverPort = null;
	
	ServerPortWorldListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
	}

    public void onChunkUnloaded(ChunkUnloadEvent event) {
    }
	
}
