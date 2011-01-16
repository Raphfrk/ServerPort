
public class MyInventory {
	
	Inventory hmodInventory;
	
    org.bukkit.inventory.PlayerInventory bukkitInventory;
	
	boolean hmod = false;
	
	MyInventory() {
		
		hmod = MyServer.getServer().getHmod();
		
	}
	
	MyInventory( Inventory inventory ) {
		
		hmod = true;
		hmodInventory = inventory;
		
	}
	
	void setHmodInventory( Inventory inventory ) {
		
		hmod = true;
		hmodInventory = inventory;
		
	}
	
	void setBukkitInventory( org.bukkit.inventory.PlayerInventory inventory ) {
		
		hmod = false;
		bukkitInventory = inventory;
		
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
			org.bukkit.inventory.ItemStack[] items = bukkitInventory.getContents();
			
			MyItem[] myItems = new MyItem[items.length];
			
			int cnt = 0;
			for( org.bukkit.inventory.ItemStack item : items ) {
				myItems[cnt] = new MyItem();
				myItems[cnt].setBukkitItem(item,cnt);
				cnt++;
			}
			
			return myItems;
		}
		
	}
	
	void removeItem(int slot) {
		
		if( hmod ) {
			hmodInventory.removeItem(slot);
		} else {
			bukkitInventory.clear(slot);
		}
		
	}
	
	void setSlot(int id, int amount, int damage, int slot) {
		
		if( hmod ) {
			
			hmodInventory.setSlot(id, amount, damage, slot);
			
		} else {
			if( amount == 0 ) {
				bukkitInventory.clear(slot);
			} else {
				bukkitInventory.setItem(slot, new org.bukkit.inventory.ItemStack(id, amount, (byte)damage));
			}
		}
		
	}
	
	MyItem getItemFromSlot(int slot) {

		MyItem item = new MyItem();
		
		if( hmod ) {
			item.setHmodItem(hmodInventory.getItemFromSlot(slot));
			
			return item;
		} else {
			item.setBukkitItem(bukkitInventory.getItem(slot), slot);
			return item;
		}
		
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodInventory == null;
		} else {
			return bukkitInventory == null;
		}
	}
	
}
