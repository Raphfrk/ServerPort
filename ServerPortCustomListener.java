
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPortCustomListener extends CustomEventListener implements Listener {

	JavaPlugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortCustomListener( ServerPortBukkit serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}
	
	public void onCustomEvent(Event event) {
		
		if( !event.getType().equals(Type.CUSTOM_EVENT) ) {
			return;
		}
		
		if( !event.getEventName().equals("ServerPortRunnableEvent")) {
			return;
		}
		
		((RunnableEvent)event).runnable.run();
		
	}
}