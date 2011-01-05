
public class MyLocation {
	
	private org.bukkit.Location bukkitLocation;
	
	private Location hmodLocation;
	
	private boolean hmod = false;
	
	public MyLocation( Location loc ) {
		hmodLocation = loc;
		hmod = true;
	}
	
	public MyLocation( org.bukkit.Location loc ) {
		bukkitLocation = loc;
		hmod = false;
	}


}
