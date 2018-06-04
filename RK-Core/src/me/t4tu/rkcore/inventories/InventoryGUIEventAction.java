package me.t4tu.rkcore.inventories;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface InventoryGUIEventAction extends InventoryGUIAction {
	
	public void onClick(InventoryClickEvent e);
	
	public void onClickAsync(InventoryClickEvent e);
}