public class MyBlock {
	
	public org.bukkit.block.Block bukkitBlock;
	private int bukkitStatus;
	// 0: start
	// 3: broken
	
	private Block hmodBlock;
	
	private boolean hmod = false;
	
	public MyBlock() {
		hmod = MyServer.getServer().getHmod();
	}
	
	public MyBlock( Block block ) {
		hmodBlock = block;
		hmod = true;
	}
	
	public MyBlock( org.bukkit.block.Block block, int status ) {
		bukkitBlock = block;
		hmod = false;
	}
	
	void setHmodBlock(Block block) {
		hmod = true;
		hmodBlock = block;
	}
	
	void setBukkitBlock(org.bukkit.block.Block block, int status) {
		bukkitBlock = block;
		bukkitStatus = status;
		hmod = false;
	}
	
	int getType() {
		if( hmod ) {
			return hmodBlock.getType();
		} else {
			return bukkitBlock.getTypeId();
		}
	}
	
	int getStatus() {
		if( hmod ) {
			return hmodBlock.getStatus();
		} else {
			return bukkitStatus;
		}
	}

	int getX() {
		if( hmod ) {
			return hmodBlock.getX();
		} else {
			return bukkitBlock.getX();
		}
	}
	
	int getY() {
		if( hmod ) {
			return hmodBlock.getY();
		} else {
			return bukkitBlock.getY();
		}
	}
	
	int getZ() {
		if( hmod ) {
			return hmodBlock.getZ();
		} else {
			return bukkitBlock.getZ();
		}
	}
	
	org.bukkit.World getWorld() {
		return bukkitBlock.getWorld();
	}
	

}