package me.t4tu.rkcore.inventories;

import org.bukkit.inventory.ItemStack;

public class InventoryGUIItem {
	
	private ItemStack item;
	private int slot;
	
	public InventoryGUIItem(ItemStack item, int slot) {
		this.item = item;
		this.slot = slot;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getSlot() {
		return slot;
	}
}