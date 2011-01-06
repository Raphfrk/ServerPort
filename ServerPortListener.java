
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

		return serverPortListenerCommon.onBlockPlace(new MyPlayer(player), new MyBlock(blockPlaced), new MyBlock(blockClicked), new MyItem(itemInHand));
		
	}

	public void onLogin(Player player) {
		
		serverPortListenerCommon.onLogin(new MyPlayer(player));

	}
	

    public boolean onHealthChange(Player player, int oldValue, int newValue) {

    	return serverPortListenerCommon.onHealthChange(new MyPlayer(player), oldValue, newValue);
    }


	public void onPlayerMove(Player player, Location from, Location to) {
		
		serverPortListenerCommon.onPlayerMove(new MyPlayer(player), new MyLocation(from), new MyLocation(to));

	}

	public boolean onBlockDestroy(Player player, Block block) {

		return serverPortListenerCommon.onBlockDestroy(new MyPlayer(player), new MyBlock(block));
	}


	public boolean onChat(Player player, String message) {

		return serverPortListenerCommon.onChat(new MyPlayer(player), message);
	}


	public boolean onCommand(Player player, String[] split) {
		
		return serverPortListenerCommon.onCommand(new MyPlayer(player), split);

	}


	public boolean onSignChange(Player player, Sign sign) {
		
		return serverPortListenerCommon.onSignChange(new MyPlayer(player), new MySign(sign));
	}
	
    	
	// Preventing portal "mist" from updating


	public boolean onBlockPhysics(Block block, boolean placed) {

		return serverPortListenerCommon.onBlockPhysics(new MyBlock(block), placed);
	}


	public boolean onFlow(Block blockFrom, Block blockTo) {

		return serverPortListenerCommon.onFlow(new MyBlock(blockFrom), new MyBlock(blockTo));
	}

}
