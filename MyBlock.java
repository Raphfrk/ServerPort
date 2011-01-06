public class MyBlock {
	
	private org.bukkit.Block bukkitBlock;
	
	private Block hmodBlock;
	
	private boolean hmod = false;
	
	public MyBlock() {
		hmod = MyServer.getServer().getHmod();
	}
	
	public MyBlock( Block block ) {
		hmodBlock = block;
		hmod = true;
	}
	
	public MyBlock( org.bukkit.Block block ) {
		bukkitBlock = block;
		hmod = false;
	}
	
	MyBlock( int id, int x, int y, int z) {
		
		hmod = MyServer.getServer().getHmod();
		
		if( hmod ) {
			hmodBlock = new Block( id, x, y, z );
		}
		
	}
	
	void setHmodBlock(Block block) {
		hmodBlock = block;
	}
	
	int getType() {
		if( hmod ) {
			return hmodBlock.getType();
		} else {
			return 0;
		}
	}
	
	int getStatus() {
		if( hmod ) {
			return hmodBlock.getStatus();
		} else {
			return 0;
		}
	}

	int getX() {
		if( hmod ) {
			return hmodBlock.getX();
		} else {
			return 0;
		}
	}
	
	int getY() {
		if( hmod ) {
			return hmodBlock.getY();
		} else {
			return 0;
		}
	}
	
	int getZ() {
		if( hmod ) {
			return hmodBlock.getZ();
		} else {
			return 0;
		}
	}
	
	void setX(int x) {
		if( hmod ) {
			hmodBlock.setX(x);
		} else {

		}
	}
	
	void setY(int y) {
		if( hmod ) {
			hmodBlock.setY(y);
		} else {
			
		}
	}
	
	void setZ(int z) {
		if( hmod ) {
			hmodBlock.setZ(z);
		} else {

		}
	}
	

}