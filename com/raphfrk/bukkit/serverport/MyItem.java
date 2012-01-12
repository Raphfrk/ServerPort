/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
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
