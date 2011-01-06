
public class MySign {
	
	Sign hmodSign;
	
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
	
	int getX() {
		if( hmod ) {
			return hmodSign.getX();
		} else {
			return 0;
		}
	}
	
	int getY() {
		if( hmod ) {
			return hmodSign.getY();
		} else {
			return 0;
		}
	}
	
	int getZ() {
		if( hmod ) {
			return hmodSign.getZ();
		} else {
			return 0;
		}
	}
	
	void setText(int line, String text) {
		if( hmod ) {
			hmodSign.setText(line, text);
		}
	}
	
	String getText(int line) {
		if( hmod ) {
			return hmodSign.getText(line);
		} else {
			return "";
		}
	}
	
	void update() {
		if( hmod ) {
			hmodSign.update();
		}
	}
	
	MyBlock getBlock() {
		
		if( hmod ) {
			MyBlock block = new MyBlock();
			
			block.setHmodBlock(hmodSign.getBlock());
			return block;
		} else {
			return null;
		}
		
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodSign == null;
		} else {
			return false;
		}
	}
	
	
}
