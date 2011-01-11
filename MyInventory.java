
public class MyInventory {
	
	Inventory hmodInventory;
	
//	org.bukkit.Inventory bukkitInventory;
	
	boolean hmod = false;
	
	MyInventory() {
		
		hmod = MyServer.getServer().getHmod();
		
	}
	
	MyInventory( Inventory inventory ) {
		
		hmod = true;
		hmodInventory = inventory;
		
	}
	
	void setHmodInventory( Inventory inventory ) {
		
		hmodInventory = inventory;
		
	}

	MyItem[] getContents() {
		
		if( hmod ) {
			
			Item[] items = hmodInventory.getContents();
			
			MyItem[] myItems = new MyItem[items.length];
			
			int cnt = 0;
			for( Item item : items ) {
				myItems[cnt] = new MyItem();
				myItems[cnt].setHmodItem(item);
				cnt++;
			}
			
			return myItems;
			
		} else {
			return null;
		}
		
	}
	
	void removeItem(int slot) {
		
		if( hmod ) {
			hmodInventory.removeItem(slot);
		}
		
	}
	
	void setSlot(int id, int amount, int damage, int slot) {
		
		if( hmod ) {
			
			hmodInventory.setSlot(id, amount, damage, slot);
			
		}
		
	}
	
	MyItem getItemFromSlot(int slot) {
		
		if( hmod ) {
			MyItem item = new MyItem();
			
			item.setHmodItem(hmodInventory.getItemFromSlot(slot));
			
			return item;
		} else {
			return null;
		}
		
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodInventory == null;
		} else {
			return true;
		}
	}
	
}
