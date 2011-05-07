import org.bukkit.Block;
import org.bukkit.Player;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;


public class ServerPortListener extends PluginListener {

	Plugin serverPort = null;
	
	ServerPortListenerCommon serverPortListenerCommon;

	ServerPortListener( ServerPort serverPort ) {
		this.serverPort = serverPort;
		this.serverPortListenerCommon = serverPort.serverPortListenerCommon;
	}

	public void setPortalManager( PortalManager portalManager ) {
		this.serverPortListenerCommon.setPortalManager(portalManager);
	}

	public void setCommunicationManager( CommunicationManager communicationManager ) {
		this.serverPortListenerCommon.setCommunicationManager(communicationManager);
	}

	public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {

		return serverPortListenerCommon.onBlockPlace(player, blockPlaced, blockClicked, itemInHand);
		
	}

	public void onLogin(Player player) {
		
		serverPortListenerCommon.onLogin(player);

	}
	

    public boolean onHealthChange(Player player, int oldValue, int newValue) {

    	return serverPortListenerCommon.onHealthChange(player, oldValue, newValue);
    }


	public void onPlayerMove(Player player, Location from, Location to) {
		
		serverPortListenerCommon.onPlayerMove(player, from, to);

	}

	public boolean onBlockDestroy(Player player, Block block) {

		return serverPortListenerCommon.onBlockDestroy(player, block);
	}


	public boolean onChat(Player player, String message) {

		return serverPortListenerCommon.onChat(player, message);
	}


	public boolean onCommand(Player player, String[] split) {
		
		return serverPortListenerCommon.onCommand(player, split);

	}


	public boolean onSignChange(Player player, Sign sign) {
		
		return serverPortListenerCommon.onSignChange(player, sign);
	}
	
    	
	// Preventing portal "mist" from updating


	public boolean onBlockPhysics(Block block, boolean placed) {

		return serverPortListenerCommon.onBlockPhysics(block, placed);
	}


	public boolean onFlow(Block blockFrom, Block blockTo) {

		return serverPortListenerCommon.onFlow(blockFrom, blockTo);
	}

}
