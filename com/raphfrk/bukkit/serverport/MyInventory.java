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

public class MyInventory {

	org.bukkit.inventory.PlayerInventory bukkitInventory;
	int bukkitBasicSlots;

	MyInventory() {
	}

	void setBukkitInventory( org.bukkit.inventory.PlayerInventory inventory ) {

		bukkitInventory = inventory;
		bukkitBasicSlots = inventory.getSize();

	}


	MyItem[] getContents() {

		org.bukkit.inventory.ItemStack[] items = bukkitInventory.getContents();

		int length = bukkitInventory.getSize();

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

	void removeItem(int slot) {

		if( slot >= bukkitBasicSlots ) {
			int offset = slot-bukkitBasicSlots;

			switch(offset) {
			case 0: bukkitInventory.clear(bukkitBasicSlots+3); break;
			case 1: bukkitInventory.clear(bukkitBasicSlots+1); break;
			case 2: bukkitInventory.clear(bukkitBasicSlots+2); break;
			case 3: bukkitInventory.clear(bukkitBasicSlots+0); break;
			default: MiscUtils.safeLogging("Error slot out of range for setSlot in MyInventory");
			}
		}	else {
			bukkitInventory.clear(slot);
		}


	}

	void setSlot(int id, int amount, int damage, int slot) {

		if( amount == 0 ) {
			bukkitInventory.clear(slot);
		} else {

			if( slot >= bukkitBasicSlots ) {
				int offset = slot-bukkitBasicSlots;

				switch(offset) {
				case 0: bukkitInventory.setHelmet(new org.bukkit.inventory.ItemStack(id, amount, (short)damage)); break;
				case 1: bukkitInventory.setLeggings(new org.bukkit.inventory.ItemStack(id, amount, (short)damage)); break;
				case 2: bukkitInventory.setChestplate(new org.bukkit.inventory.ItemStack(id, amount, (short)damage)); break;
				case 3: bukkitInventory.setBoots(new org.bukkit.inventory.ItemStack(id, amount, (short)damage)); break;
				default: MiscUtils.safeLogging("Error slot out of range for setSlot in MyInventory");
				}
			} else {
				bukkitInventory.setItem(slot, new org.bukkit.inventory.ItemStack(id, amount, (short)damage));
			}
		}


	}

	MyItem getItemFromSlot(int slot) {

		MyItem item = new MyItem();

		if( slot >= bukkitBasicSlots ) {
			int offset = slot-bukkitBasicSlots;

			switch(offset) {
			case 0: item.setBukkitItem(bukkitInventory.getHelmet(), slot); break;
			case 1: item.setBukkitItem(bukkitInventory.getLeggings(), slot); break;
			case 2: item.setBukkitItem(bukkitInventory.getChestplate(), slot); break;
			case 3: item.setBukkitItem(bukkitInventory.getBoots(), slot); break;
			}
		} else {
			item.setBukkitItem(bukkitInventory.getItem(slot), slot);
		}

		return item;


	}

	boolean isNull() {
		return bukkitInventory == null;
		
	}

}
