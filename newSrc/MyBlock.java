import org.bukkit.Block;

public class MyBlock {
	
	private org.bukkit.Block bukkitBlock;
	
	private Block hmodBlock;
	
	private boolean hmod = false;
	
	public MyBlock( Block block ) {
		hmodBlock = block;
		hmod = true;
	}
	
	public MyBlock( org.bukkit.Block block ) {
		bukkitBlock = block;
		hmod = false;
	}


}