
public class MyItem {
	
	Item hmodItem;
	
	org.bukkit.inventory.ItemStack bukkitItem;
	int bukkitSlot = 0;
	
	boolean hmod = false;
	
	MyItem() {
		hmod = MyServer.getServer().getHmod();
	}
	
	MyItem( Item item ) {
		hmod = true;
		hmodItem = item;
	}
	
	void setHmodItem( Item item ) {
		hmodItem = item;
	}
	
	void setBukkitItem( org.bukkit.inventory.ItemStack item, int slot ) {
		bukkitItem = item;
		bukkitSlot = slot;
	}
	
	void setItemId(int id) {
		if( hmod ) {
			hmodItem.setItemId(id);
		} else {
			bukkitItem.setTypeId(id);
		}
	}
	
	void setAmount(int amount) {
		if( hmod ) {
			hmodItem.setAmount(amount);
		} else {
			bukkitItem.setAmount(amount);
		}
	}

	void setDamage(int damage) {
		if(hmod) {
			hmodItem.setDamage(damage);
		} else {
			bukkitItem.setDamage((byte)damage);
		}
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodItem==null;
		} else {
			return !(bukkitItem!=null && bukkitItem.getTypeId()!=0);
		}
	}
	
	int getSlot() {
		if(hmod) {
			return hmodItem.getSlot();
		} else {
			return bukkitSlot;
		}
	}
	
	int getItemId() {
		if(hmod) {
			return hmodItem.getItemId();
		} else {
			return bukkitItem.getTypeId();
		}
	}
	
	int getDamage() {
		if(hmod) {
			return hmodItem.getDamage();
		} else {
			return (int)bukkitItem.getDamage();
		}
	}
	
	int getAmount() {
		if(hmod) {
			return hmodItem.getAmount();
		} else {
			return bukkitItem.getAmount();
		}
	}
	
	

}
