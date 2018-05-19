package me.t4tu.rkcore.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.t4tu.rkcore.Core;

public class InventoryGUI implements Listener {
	
	private static Core core;
	
	private Map<InventoryGUIItem, InventoryGUIAction> actions;
	private Inventory inventory;
	private List<String> viewers;
	
	public InventoryGUI(int inventorySize, String inventoryTitle) {
		actions = new HashMap<InventoryGUIItem, InventoryGUIAction>();
		inventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);
		viewers = new ArrayList<String>();
		Bukkit.getPluginManager().registerEvents(this, core);
		core.getInventoryGuis().add(this);
	}
	
	public void addItem(InventoryGUIItem item, InventoryGUIAction action) {
		actions.put(item, action);
		inventory.setItem(item.getSlot(), item.getItem());
	}
	
	public void addItem(ItemStack item, int slot, InventoryGUIAction action) {
		InventoryGUIItem guiItem = new InventoryGUIItem(item, slot);
		addItem(guiItem, action);
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public void open(Player player) {
		player.openInventory(inventory);
		viewers.add(player.getUniqueId().toString());
	}
	
	public void close(Player player) {
		String uuid = player.getUniqueId().toString();
		if (viewers.contains(uuid)) {
			if (player.getOpenInventory() != null && player.getOpenInventory().getTitle() != null && 
					player.getOpenInventory().getTitle().equals(inventory.getTitle())) {
				player.closeInventory();
			}
		}
		viewers.remove(uuid);
	}
	
	public void destroy() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			close(player);
		}
		actions.clear();
		viewers.clear();
		inventory.clear();
		inventory = null;
	}
	
	@EventHandler
	public void onItemClick(InventoryClickEvent e) {
		if (e.getInventory().equals(inventory)) {
			e.setCancelled(true);
			for (InventoryGUIItem item : actions.keySet()) {
				if (e.getCurrentItem() != null && e.getCurrentItem().equals(item.getItem())) {
					actions.get(item).onClickAsync();
					actions.get(item).onClick();
				}
			}
		}
	}
	
	public static void setCore(Core plugin) {
		core = plugin;
	}
}