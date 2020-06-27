package me.t4tu.rkcore.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;

public class StatisticsViewer {
	
	private static Core core;
	
	private List<PlayerStatisticsEntry> pvpTopCache;
	private Map<String, MySQLResult[]> viewerCache;
	
	public StatisticsViewer(Core core) {
		StatisticsViewer.core = core;
		pvpTopCache = new ArrayList<PlayerStatisticsEntry>();
		viewerCache = new HashMap<String, MySQLResult[]>();
	}
	
	public List<PlayerStatisticsEntry> getPvpTopCache() {
		return pvpTopCache;
	}
	
	public Map<String, MySQLResult[]> getViewerCache() {
		return viewerCache;
	}
	
	public void addKillToPvpTopCache(Player player) {
		for (PlayerStatisticsEntry entry : pvpTopCache) {
			if (entry.getUuid().equals(player.getUniqueId().toString())) {
				entry.setValue(entry.getValue() + 1);
				return;
			}
		}
		pvpTopCache.add(new PlayerStatisticsEntry(Statistic.PVP_KILLS, 1, player.getUniqueId().toString()));
	}
	
	public static void incrementKillsInPvpTopCache(Player player) {
		core.getStatisticsViewer().addKillToPvpTopCache(player);
	}
	
	public void updatePvpTopCache() {
		MySQLResult statisticsData = MySQLUtils.get("SELECT statistic, player, SUM(value) AS value, time FROM statistics_player WHERE statistic=? GROUP BY player ORDER BY value DESC LIMIT 30", 
				Statistic.PVP_KILLS.getId() + "");
		pvpTopCache.clear();
		int rows = statisticsData.getRows();
		for (int i = 0; i < rows; i++) {
			pvpTopCache.add(new PlayerStatisticsEntry(Statistic.PVP_KILLS, statisticsData.getInt(i, "value"), statisticsData.getString(i, "player")));
		}
	}
	
	public void updatePvpTopHolograms(World world) {
		Collections.sort(pvpTopCache, new StatisticsManager.ValueSorter());
		for (int i = 1; i <= 10; i++) {
			if (i <= pvpTopCache.size()) {
				PlayerStatisticsEntry entry = pvpTopCache.get(i - 1);
				String name = CoreUtils.uuidToName(entry.getUuid());
				int kills = entry.getValue();
				for (ArmorStand a : world.getEntitiesByClass(ArmorStand.class)) {
					if (a.getScoreboardTags().contains("RK-pvpstats-" + i)) {
						int position = i;
						new BukkitRunnable() {
							public void run() {
								a.setCustomName("§4§l" + position + ". §c" + name + "§7 - §c" + kills);
							}
						}.runTask(core);
					}
				}
			}
		}
	}
	
	public static void updatePvpTopScoreboard(World world) {
		core.getStatisticsViewer().updatePvpTopHolograms(world);
	}
	
	public MySQLResult fetchData(CommandSender sender, String[] args, String tc1, String tc2, String tc3, String tc4, String usage) {
		if (args.length >= 6) {
			try {
				String table = args[1];
				Statistic statistic = Statistic.valueOf(args[2].toUpperCase());
				int days = Integer.parseInt(args[3]);
				long maxTime = System.currentTimeMillis() - 86400000l * days;
				boolean timeGiven = days > 0;
				String player = null;
				boolean playerGiven = false;
				int data = 0;
				boolean dataGiven = false;
				if (!args[4].equalsIgnoreCase("none")) {
					playerGiven = true;
					player = CoreUtils.nameToUuid(args[4]);
					if (player == null) {
						sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
						return null;
					}
				}
				if (!args[5].equalsIgnoreCase("none")) {
					dataGiven = true;
					try {
						data = Integer.parseInt(args[5]);
					}
					catch (NumberFormatException e) {
						sender.sendMessage(tc3 + "Virheellinen data!");
						return null;
					}
				}
				MySQLResult statisticsData;
				if (playerGiven) {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND player=? AND data=? AND time>?", statistic.getId() + "", player, data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND player=? AND data=?", statistic.getId() + "", player, data + "");
						}
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND player=? AND time>?", statistic.getId() + "", player, maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND player=?", statistic.getId() + "", player);
						}
					}
				}
				else {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND data=? AND time>?", statistic.getId() + "", data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND data=?", statistic.getId() + "", data + "");
						}
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=? AND time>?", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT value, time FROM statistics_" + table + " WHERE statistic=?", statistic.getId() + "");
						}
					}
				}
				return statisticsData;
			}
			catch (NumberFormatException e) {
				sender.sendMessage(tc3 + "Virheellinen vuorokausien määrä!");
			}
			catch (IllegalArgumentException e) {
				sender.sendMessage(tc3 + "Ei löydetty tilastoa \"" + tc4 + args[2].toUpperCase() + tc3 + "\"!");
			}
		}
		else {
			sender.sendMessage(usage + "/statistics export <tyyppi> <tilasto> <vuorokaudet> <pelaaja>/none <data>/none");
		}
		return null;
	}
	
	public void viewCommand(CommandSender sender, String[] args, String tc1, String tc2, String tc3, String tc4, String usage) {
		if (args.length >= 6) {
			try {
				String table = args[1];
				Statistic statistic = Statistic.valueOf(args[2].toUpperCase());
				int days = Integer.parseInt(args[3]);
				long maxTime = System.currentTimeMillis() - 86400000l * days;
				boolean timeGiven = days > 0;
				String player = null;
				String playerName = null;
				boolean playerGiven = false;
				boolean playerTop = false;
				int data = 0;
				boolean dataGiven = false;
				boolean dataTop = false;
				if (!args[4].equalsIgnoreCase("none")) {
					if (args[4].equalsIgnoreCase("top")) {
						playerTop = true;
					}
					else {
						playerGiven = true;
						player = CoreUtils.nameToUuid(args[4]);
						if (player == null) {
							sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							return;
						}
						playerName = CoreUtils.uuidToName(player);
					}
				}
				if (!args[5].equalsIgnoreCase("none")) {
					if (args[5].equalsIgnoreCase("top")) {
						dataTop = true;
					}
					else {
						dataGiven = true;
						try {
							data = Integer.parseInt(args[5]);
						}
						catch (NumberFormatException e) {
							sender.sendMessage(tc3 + "Virheellinen data!");
							return;
						}
					}
				}
				MySQLResult statisticsData;
				if (playerGiven) {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=? AND data=? AND time>?", statistic.getId() + "", player, data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=? AND data=?", statistic.getId() + "", player, data + "");
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, playerName, dataGiven, data, statisticsData);
					}
					else if (dataTop) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? AND player=? AND time>? GROUP BY player, data ORDER BY value DESC", statistic.getId() + "", player, maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? AND player=? GROUP BY player, data ORDER BY value DESC", statistic.getId() + "", player);
						}
						showTopInfo(sender, args, statistic, timeGiven, days, playerTop, playerGiven, playerName, dataTop, dataGiven, data, statisticsData);
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=? AND time>?", statistic.getId() + "", player, maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=?", statistic.getId() + "", player);
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, playerName, dataGiven, data, statisticsData);
					}
				}
				else if (playerTop) {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? AND data=? AND time>? GROUP BY player, data ORDER BY value DESC", statistic.getId() + "", data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? AND data=? GROUP BY player, data ORDER BY value DESC", statistic.getId() + "", data + "");
						}
						showTopInfo(sender, args, statistic, timeGiven, days, playerTop, playerGiven, playerName, dataTop, dataGiven, data, statisticsData);
					}
					else if (dataTop) {
						sender.sendMessage(tc3 + "Vain yksi argumentti voi olla \"top\"!");
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value FROM statistics_" + table + " WHERE statistic=? AND time>? GROUP BY player ORDER BY value DESC", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value FROM statistics_" + table + " WHERE statistic=? GROUP BY player ORDER BY value DESC", statistic.getId() + "");
						}
						showTopInfo(sender, args, statistic, timeGiven, days, playerTop, playerGiven, playerName, dataTop, dataGiven, data, statisticsData);
					}
				}
				else {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND data=? AND time>?", statistic.getId() + "", data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND data=?", statistic.getId() + "", data + "");
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, playerName, dataGiven, data, statisticsData);
					}
					else if (dataTop) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? AND time>? GROUP BY data ORDER BY value DESC", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT player, SUM(value) AS value, data FROM statistics_" + table + " WHERE statistic=? GROUP BY data ORDER BY value DESC", statistic.getId() + "");
						}
						showTopInfo(sender, args, statistic, timeGiven, days, playerTop, playerGiven, playerName, dataTop, dataGiven, data, statisticsData);
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND time>?", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=?", statistic.getId() + "");
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, playerName, dataGiven, data, statisticsData);
					}
				}
			}
			catch (NumberFormatException e) {
				sender.sendMessage(tc3 + "Virheellinen vuorokausien määrä!");
			}
			catch (IllegalArgumentException e) {
				sender.sendMessage(tc3 + "Ei löydetty tilastoa \"" + tc4 + args[2].toUpperCase() + tc3 + "\"!");
			}
		}
		else {
			sender.sendMessage(usage + "/statistics view <tyyppi> <tilasto> <vuorokaudet> <pelaaja>/none/top <data>/none/top");
		}
	}
	
	private void showBasicInfo(CommandSender sender, String[] args, Statistic statistic, boolean timeGiven, int days, 
			boolean playerGiven, String playerName, boolean dataGiven, int data, MySQLResult statisticsData) {
		if (statisticsData != null) {
			sender.sendMessage("");
			sender.sendMessage("§5§l " + statistic.toString());
			if (timeGiven)
				sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
			if (playerGiven)
				sender.sendMessage("§5 (Pelaaja: " + playerName + ")");
			if (dataGiven)
				sender.sendMessage("§5 (Data: " + data + ")");
			sender.sendMessage("");
			sender.sendMessage("§5 Yhteensä: §d" + statisticsData.getInt(0, "total"));
			sender.sendMessage("§5 Mainintoja: §d" + statisticsData.getInt(0, "count"));
			sender.sendMessage("§5 Suurin: §d" + statisticsData.getInt(0, "max"));
			sender.sendMessage("§5 Pienin: §d" + statisticsData.getInt(0, "min"));
			sender.sendMessage("");
		}
		else {
			sender.sendMessage(CoreUtils.getErrorBaseColor() + "Ei löydetty yhtään dataa antamillasi parametreillä!");
		}
	}
	
	private void showTopInfo(CommandSender sender, String[] args, Statistic statistic, boolean timeGiven, int days, 
			boolean playerTop, boolean playerGiven, String playerName, boolean dataTop, boolean dataGiven, int data, MySQLResult statisticsData) {
		if (statisticsData != null) {
			sender.sendMessage("");
			sender.sendMessage("§5§l " + statistic.toString());
			if (timeGiven)
				sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
			if (playerTop)
				sender.sendMessage("§5 (Pelaaja: TOP)");
			if (playerGiven)
				sender.sendMessage("§5 (Pelaaja: " + playerName + ")");
			if (dataTop)
				sender.sendMessage("§5 (Data: TOP)");
			if (dataGiven)
				sender.sendMessage("§5 (Data: " + data + ")");
			sender.sendMessage("");
			int rows = statisticsData.getRows();
			int results = Math.min(rows, 10);
			for (int i = 0; i < results; i++) {
				if (playerTop)
					sender.sendMessage("§5  " + CoreUtils.uuidToName(statisticsData.getString(i, "player")) + ": §d" + statisticsData.getInt(i, "value"));
				if (dataTop)
					sender.sendMessage("§5  " + statisticsData.getInt(i, "data") + ": §d" + statisticsData.getInt(i, "value"));
			}
			if (rows > 10) {
				sender.sendMessage("§8  Ja " + (rows - 10) + " lisää...");
			}
			sender.sendMessage("");
		}
	}
}