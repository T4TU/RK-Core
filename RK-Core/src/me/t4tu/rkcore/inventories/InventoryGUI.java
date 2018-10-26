package me.t4tu.rkcore.inventories;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;

public class InventoryGUI implements Listener {
	
	private static Core core;
	
	private Map<InventoryGUIItem, InventoryGUIAction> actions;
	private Inventory inventory;
	
	public InventoryGUI(int inventorySize, String inventoryTitle) {
		actions = new HashMap<InventoryGUIItem, InventoryGUIAction>();
		inventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);
		Bukkit.getPluginManager().registerEvents(this, core);
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
	}
	
	public void close(Player player) {
		if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().equals(inventory)) {
			player.closeInventory();
		}
	}
	
	@EventHandler
	public void onItemClick(InventoryClickEvent e) {
		if (e.getInventory().equals(inventory)) {
			e.setCancelled(true);
			for (InventoryGUIItem item : actions.keySet()) {
				if (e.getCurrentItem() != null && e.getCurrentItem().equals(item.getItem()) && actions.get(item) != null) {
					new BukkitRunnable() {
						public void run() {
							if (actions.get(item) instanceof InventoryGUIEventAction) {
								((InventoryGUIEventAction) actions.get(item)).onClickAsync(e);
							}
							else {
								actions.get(item).onClickAsync();
							}
						}
					}.runTaskAsynchronously(core);
					if (actions.get(item) instanceof InventoryGUIEventAction) {
						((InventoryGUIEventAction) actions.get(item)).onClick(e);
					}
					else {
						actions.get(item).onClick();
					}
					return;
				}
			}
		}
	}
	
	public static void setCore(Core plugin) {
		core = plugin;
	}
}