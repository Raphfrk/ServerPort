
public class MySign {
	
	Sign hmodSign;
	
	org.bukkit.block.Sign bukkitSign;
	int status;
	
	boolean hmod = false;
	
	MySign() {
		hmod = MyServer.getServer().getHmod();
	}
	
	MySign( Sign sign ) {
		this();
		hmodSign = sign;
	}

	void setHmodSign( Sign sign ) {
		hmodSign = sign;
	}
	
	void setBukkitSign( org.bukkit.block.Sign sign, int status ) {
		bukkitSign = sign;
		this.status = status;
	}
	
	int getX() {
		if( hmod ) {
			return hmodSign.getX();
		} else {
			return bukkitSign.getX();
		}
	}
	
	int getY() {
		if( hmod ) {
			return hmodSign.getY();
		} else {
			return bukkitSign.getY();
		}
	}
	
	int getZ() {
		if( hmod ) {
			return hmodSign.getZ();
		} else {
			return bukkitSign.getZ();
		}
	}
	
	void setText(int line, String text) {
		if( hmod ) {
			hmodSign.setText(line, text);
		} else {
			bukkitSign.setLine(line, text);
		}
	}
	
	String getText(int line) {
		if( hmod ) {
			return hmodSign.getText(line);
		} else {
			return bukkitSign.getLine(line);
		}
	}
	
	void update() {
		if( hmod ) {
			hmodSign.update();
		} else {
			bukkitSign.update();
		}
	}
	
	MyBlock getBlock() {

		MyBlock block = new MyBlock();
		
		if( hmod ) {
			
			block.setHmodBlock(hmodSign.getBlock());

		} else {

			block.setBukkitBlock(bukkitSign.getBlock(), status);
			
		}
		
		return block;
		
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodSign == null;
		} else {
			return bukkitSign == null;
		}
	}
	
	
}
