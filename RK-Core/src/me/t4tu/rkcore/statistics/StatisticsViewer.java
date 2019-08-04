package me.t4tu.rkcore.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	public StatisticsViewer(Core core) {
		StatisticsViewer.core = core;
		pvpTopCache = new ArrayList<PlayerStatisticsEntry>();
	}
	
	public List<PlayerStatisticsEntry> getPvpTopCache() {
		return pvpTopCache;
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
		MySQLResult statisticsData = MySQLUtils.get("SELECT * FROM statistics_player WHERE statistic=?", Statistic.PVP_KILLS.getId() + "");
		List<StatisticsEntry> entries = core.getStatisticsManager().combineSimilarPlayers(core.getStatisticsManager().parseStatistics(statisticsData));
		pvpTopCache.clear();
		int count = 0;
		for (StatisticsEntry entry : entries) {
			if (entry instanceof PlayerStatisticsEntry) {
				PlayerStatisticsEntry e = (PlayerStatisticsEntry) entry;
				if (count >= 20) {
					break;
				}
				pvpTopCache.add(e);
			}
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
	
	public void viewCommand(CommandSender sender, String[] args, String tc1, String tc2, String tc3, String tc4, String usage) {
		if (args.length >= 6) {
			try {
				String table = args[1];
				Statistic statistic = Statistic.valueOf(args[2].toUpperCase());
				int days = Integer.parseInt(args[3]);
				long maxTime = System.currentTimeMillis() - 86400000l * days;
				boolean timeGiven = days > 0;
				String player = null;
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
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, player, dataGiven, data, statisticsData);
					}
					else if (dataTop) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND player=? AND time>?", statistic.getId() + "", player, maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND player=?", statistic.getId() + "", player);
						}
						List<StatisticsEntry> entries = core.getStatisticsManager().combineSimilar(core.getStatisticsManager().parseStatistics(statisticsData));
						Collections.sort(entries, new StatisticsManager.ValueSorter());
						sender.sendMessage("");
						sender.sendMessage("§5§l " + statistic.toString());
						if (timeGiven)
							sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
						sender.sendMessage("§5 (Pelaaja: " + args[4] + ")");
						sender.sendMessage("§5 (Data: TOP)");
						sender.sendMessage("");
						int count = 0;
						for (StatisticsEntry entry : entries) {
							if (entry instanceof ComplexPlayerStatisticsEntry) {
								ComplexPlayerStatisticsEntry e = (ComplexPlayerStatisticsEntry) entry;
								if (count >= 10) {
									sender.sendMessage("§8  Ja " + (entries.size() - 10) + " lisää...");
									break;
								}
								sender.sendMessage("§5  " + e.getData() + ": §d" + e.getValue());
								count++;
							}
						}
						sender.sendMessage("");
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=? AND time>?", statistic.getId() + "", player, maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND player=?", statistic.getId() + "", player);
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, player, dataGiven, data, statisticsData);
					}
				}
				else if (playerTop) {
					if (dataGiven) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND data=? AND time>?", statistic.getId() + "", data + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND data=?", statistic.getId() + "", data + "");
						}
						List<StatisticsEntry> entries = core.getStatisticsManager().combineSimilar(core.getStatisticsManager().parseStatistics(statisticsData));
						Collections.sort(entries, new StatisticsManager.ValueSorter());
						sender.sendMessage("");
						sender.sendMessage("§5§l " + statistic.toString());
						if (timeGiven)
							sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
						sender.sendMessage("§5 (Pelaaja: TOP)");
						sender.sendMessage("§5 (Data: " + data + ")");
						sender.sendMessage("");
						int count = 0;
						for (StatisticsEntry entry : entries) {
							if (entry instanceof ComplexPlayerStatisticsEntry) {
								ComplexPlayerStatisticsEntry e = (ComplexPlayerStatisticsEntry) entry;
								if (count >= 10) {
									sender.sendMessage("§8  Ja " + (entries.size() - 10) + " lisää...");
									break;
								}
								String playerName = CoreUtils.uuidToName(e.getUuid());
								sender.sendMessage("§5  " + playerName + ": §d" + e.getValue());
								count++;
							}
						}
						sender.sendMessage("");
					}
					else if (dataTop) {
						sender.sendMessage(tc3 + "Vain yksi argumentti voi olla \"top\"!");
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND time>?", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=?", statistic.getId() + "");
						}
						List<StatisticsEntry> entries = core.getStatisticsManager().combineSimilarPlayers(core.getStatisticsManager().parseStatistics(statisticsData));
						Collections.sort(entries, new StatisticsManager.ValueSorter());
						sender.sendMessage("");
						sender.sendMessage("§5§l " + statistic.toString());
						if (timeGiven)
							sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
						sender.sendMessage("§5 (Pelaaja: TOP)");
						sender.sendMessage("");
						int count = 0;
						for (StatisticsEntry entry : entries) {
							if (entry instanceof PlayerStatisticsEntry) {
								PlayerStatisticsEntry e = (PlayerStatisticsEntry) entry;
								if (count >= 10) {
									sender.sendMessage("§8  Ja " + (entries.size() - 10) + " lisää...");
									break;
								}
								String playerName = CoreUtils.uuidToName(e.getUuid());
								sender.sendMessage("§5  " + playerName + ": §d" + e.getValue());
								count++;
							}
						}
						sender.sendMessage("");
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
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, player, dataGiven, data, statisticsData);
					}
					else if (dataTop) {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=? AND time>?", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT * FROM statistics_" + table + " WHERE statistic=?", statistic.getId() + "");
						}
						List<StatisticsEntry> entries = core.getStatisticsManager().combineSimilarData(core.getStatisticsManager().parseStatistics(statisticsData));
						Collections.sort(entries, new StatisticsManager.ValueSorter());
						sender.sendMessage("");
						sender.sendMessage("§5§l " + statistic.toString());
						if (timeGiven)
							sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
						sender.sendMessage("§5 (Data: TOP)");
						sender.sendMessage("");
						int count = 0;
						for (StatisticsEntry entry : entries) {
							if (entry instanceof ComplexPlayerStatisticsEntry) {
								ComplexPlayerStatisticsEntry e = (ComplexPlayerStatisticsEntry) entry;
								if (count >= 10) {
									sender.sendMessage("§8  Ja " + (entries.size() - 10) + " lisää...");
									break;
								}
								sender.sendMessage("§5  " + e.getData() + ": §d" + e.getValue());
								count++;
							}
						}
						sender.sendMessage("");
					}
					else {
						if (timeGiven) {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=? AND time>?", statistic.getId() + "", maxTime + "");
						}
						else {
							statisticsData = MySQLUtils.get("SELECT SUM(value) AS total, COUNT(value) AS count, MAX(value) AS max, MIN(value) AS min FROM statistics_" + table + " WHERE statistic=?", statistic.getId() + "");
						}
						showBasicInfo(sender, args, statistic, timeGiven, days, playerGiven, player, dataGiven, data, statisticsData);
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
			boolean playerGiven, String player, boolean dataGiven, int data, MySQLResult statisticsData) {
		if (statisticsData != null) {
			sender.sendMessage("");
			sender.sendMessage("§5§l " + statistic.toString());
			if (timeGiven)
				sender.sendMessage("§5 (Viimeiset " + days + " vuorokautta)");
			if (playerGiven)
				sender.sendMessage("§5 (Pelaaja: " + args[4] + ")");
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
}