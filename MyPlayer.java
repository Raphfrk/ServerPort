
public class MyPlayer {

	private org.bukkit.Player bukkitPlayer;

	private Player hmodPlayer;

	private boolean hmod = false;

	public MyPlayer( Player player ) {
		hmodPlayer = player;
		hmod = true;
	}

	public MyPlayer( org.bukkit.Player player ) {
		bukkitPlayer = player;
		hmod = false;
	}

}
