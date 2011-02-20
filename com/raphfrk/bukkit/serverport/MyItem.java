package com.raphfrk.bukkit.serverport;

public class MyItem {
	
	org.bukkit.inventory.ItemStack bukkitItem;
	int bukkitSlot = 0;
	
	
	MyItem() {
	}
	
	void setBukkitItem( org.bukkit.inventory.ItemStack item, int slot ) {
		bukkitItem = item;
		bukkitSlot = slot;
	}
	
	void setItemId(int id) {
		bukkitItem.setTypeId(id);
	}
	
	void setAmount(int amount) {
		bukkitItem.setAmount(amount);
	}

	void setDamage(int damage) {
		bukkitItem.setDurability((byte)damage);
	}
	
	boolean isNull() {
		return !(bukkitItem!=null && bukkitItem.getTypeId()!=0);
	}
	
	int getSlot() {
		return bukkitSlot;
	}
	
	int getItemId() {
		return bukkitItem.getTypeId();
	}
	
	int getDamage() {
		return (int)bukkitItem.getDurability();
	}
	
	int getAmount() {
		return bukkitItem.getAmount();
	}
	
	

}
