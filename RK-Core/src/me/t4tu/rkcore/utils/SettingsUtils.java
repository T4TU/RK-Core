package me.t4tu.rkcore.utils;

import org.bukkit.entity.Player;

import me.t4tu.rkcore.Core;

public class SettingsUtils {
	
	private static Core core;
	
	public static void setCore(Core plugin) {
		core = plugin;
	}
	
	public static void reloadSettings(Player player) {
		MySQLResult settingsData = MySQLUtils.get("SELECT * FROM player_settings WHERE name=?", player.getName());
		if (settingsData != null) {
			for (String setting : settingsData.getRow(0).keySet()) {
				if (!setting.equals("name") && !setting.equals("uuid")) {
					core.getConfig().set("users." + player.getName() + ".settings." + setting, settingsData.getBoolean(0, setting));
				}
			}
			core.saveConfig();
		}
	}
	
	public static boolean getSetting(Player player, String setting) {
		return core.getConfig().getBoolean("users." + player.getName() + ".settings." + setting, true);
	}
	
	public static void setSetting(Player player, String setting, boolean value) {
		MySQLUtils.set("UPDATE player_settings SET " + setting + "=" + value + " WHERE name=?", player.getName());
		reloadSettings(player);
	}
}