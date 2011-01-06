
public class MyItem {
	
	Item hmodItem;
	
//	org.bukkit bukkitItem;
	
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
	
	void setItemId(int id) {
		if( hmod ) {
			hmodItem.setItemId(id);
		}	
	}
	
	void setAmount(int amount) {
		if( hmod ) {
			hmodItem.setAmount(amount);
		}
	}

	void setDamage(int damage) {
		if(hmod) {
			hmodItem.setDamage(damage);
		}
	}
	
	boolean isNull() {
		if( hmod ) {
			return hmodItem==null;
		} else {
			return false;
		}
	}
	
	int getSlot() {
		if(hmod) {
			return hmodItem.getSlot();
		} else {
			return 0;
		}
	}
	
	int getItemId() {
		if(hmod) {
			return hmodItem.getItemId();
		} else {
			return 0;
		}
	}
	
	int getDamage() {
		if(hmod) {
			return hmodItem.getDamage();
		} else {
			return 0;
		}
	}
	
	int getAmount() {
		if(hmod) {
			return hmodItem.getAmount();
		} else {
			return 0;
		}
	}
	
	

}
