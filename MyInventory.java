
public class MyInventory {

	Inventory hmodInventory;

	org.bukkit.inventory.PlayerInventory bukkitInventory;
	int bukkitBasicSlots;

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
		bukkitBasicSlots = inventory.getSize();

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

			int length = items.length;

			MyItem[] myItems = new MyItem[length+4];

			int cnt = 0;
			for( org.bukkit.inventory.ItemStack item : items ) {
				myItems[cnt] = new MyItem();
				myItems[cnt].setBukkitItem(item,cnt);
				cnt++;
			}

			myItems[length+0] = new MyItem();
			myItems[length+0].setBukkitItem(bukkitInventory.getHelmet(), length);

			myItems[length+1] = new MyItem();
			myItems[length+1].setBukkitItem(bukkitInventory.getLeggings(), length+1);
			
			myItems[length+2] = new MyItem();
			myItems[length+2].setBukkitItem(bukkitInventory.getChestplate(), length+2);
			
			myItems[length+3] = new MyItem();
			myItems[length+3].setBukkitItem(bukkitInventory.getBoots(), length+3);

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
				if( slot >= bukkitBasicSlots ) {
					int offset = slot-bukkitBasicSlots;
					switch(offset) {
						case 0: bukkitInventory.setHelmet(new org.bukkit.inventory.ItemStack(id, amount, (byte)damage)); break;
						case 1: bukkitInventory.setLeggings(new org.bukkit.inventory.ItemStack(id, amount, (byte)damage)); break;
						case 2: bukkitInventory.setChestplate(new org.bukkit.inventory.ItemStack(id, amount, (byte)damage)); break;
						case 3: bukkitInventory.setBoots(new org.bukkit.inventory.ItemStack(id, amount, (byte)damage)); break;
						default: MiscUtils.safeLogging("Error slot out of range for setSlot in MyInventory");
					}
				}
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
