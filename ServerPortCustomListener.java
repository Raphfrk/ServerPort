
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;

public class ServerPortCustomListener implements Listener, CustomEventListener {

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