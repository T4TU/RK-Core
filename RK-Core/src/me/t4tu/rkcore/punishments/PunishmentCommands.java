package me.t4tu.rkcore.punishments;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;

public class PunishmentCommands implements CommandExecutor {
	
	private Core core;
	
	public PunishmentCommands(Core core) {
		this.core = core;
	}
	
	public void registerCommand(String command, boolean tabCompletion) {
		core.getCommand(command).setExecutor(this);
		if (tabCompletion) {
			CoreUtils.getRegisteredCommandsWithTabCompletion().add(command);
		}
		else {
			CoreUtils.getRegisteredCommands().add(command);
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		String usage = CoreUtils.getUsageString();
		String noPermission = CoreUtils.getNoPermissionString();
		
		// kick
		
		if (cmd.getName().equalsIgnoreCase("kick")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 2) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						
						String reason = "";
						for (int i = 1; i < args.length; i++) {
							reason = reason + " " + args[i];
						}
						reason = reason.trim();
						
						boolean silent = false;
						if (reason.endsWith(" -s")) {
							silent = true;
							reason = reason.substring(0, reason.length() - 3);
						}
						
						target.kickPlayer("§c§m--------------------------------\n§c \n§cSinut potkaistiin pois palvelimelta seuraavalla syyllä:"
								+ "\n§c \n§c§o" + reason + "\n§c \n§c§m--------------------------------");
						
						String broadcastMessage = "§c" + sender.getName() + " potkaisi palvelimelta pelaajan " + target.getName() + 
								" syyllä '" + reason + "'";
						String playerBroadcastMessage = "§7" + target.getName() + " potkaistiin pois palvelimelta.";
						
						for (Player player : Bukkit.getOnlinePlayers()) {
							if (CoreUtils.hasRank(player, "valvoja") && !silent) {
								player.sendMessage(broadcastMessage);
							}
							else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
								player.sendMessage("§c(Hiljainen) " + broadcastMessage);
							}
							else if (!silent) {
								player.sendMessage(playerBroadcastMessage);
							}
						}
						Bukkit.getConsoleSender().sendMessage(broadcastMessage);
						
						logAction(ChatColor.stripColor(broadcastMessage));
						
						final String historyReason = reason;
						
						new BukkitRunnable() {
							public void run() {
								String uuid = target.getUniqueId().toString().replace("-", "");
								String name = target.getName();
								String type = "kick";
								String time = "" + System.currentTimeMillis();
								String giver = sender.getName();
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, type, giver, historyReason, time);
							}
						}.runTaskAsynchronously(core);
					}
					else {
						sender.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/kick <pelaaja> <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// mute
		
		if (cmd.getName().equalsIgnoreCase("mute")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 2) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 1; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuid);
							if (muteData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									final String finalReason = reason;
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".mute.muted", true);
											core.getConfig().set("users." + target.getName() + ".mute.reason", finalReason);
											core.getConfig().set("users." + target.getName() + ".mute.duration", 0);
											core.saveConfig();
											sendMuteInfo(target, finalReason, 0);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " hiljensi pelaajan " + name 
										+ " syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " hiljennettiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "mute";
								String duration = "0";
								String time = "" + System.currentTimeMillis();
								String muter = sender.getName();
								MySQLUtils.set("INSERT INTO player_mute (uuid, name, muter, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, muter, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, type, muter, reason, time);
							}
							else {
								sender.sendMessage(tc3 + "Tämä pelaaja on jo hiljennetty!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/mute <pelaaja> <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// tempmute
		
		if (cmd.getName().equalsIgnoreCase("tempmute")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 4) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 3; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							long expires = System.currentTimeMillis();
							
							try {
								double d = Double.parseDouble(args[1]);
								String unit = args[2];
								if (unit.equals("s")) {
									expires += (long) (d * 1000);
								}
								else if (unit.equals("min")) {
									expires += (long) (d * 1000 * 60);
								}
								else if (unit.equals("h")) {
									expires += (long) (d * 1000 * 60 * 60);
								}
								else if (unit.equals("d")) {
									expires += (long) (d * 1000 * 60 * 60 * 24);
								}
								else if (unit.equals("w")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 7);
								}
								else if (unit.equals("mon")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30);
								}
								else if (unit.equals("y")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30 * 12);
								}
								else {
									sender.sendMessage(tc3 + "Käytettävissä olevat ajan muodot: s, min, h, d, w, mon, y");
									return;
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Virheellinen hiljennyksen kesto!");
								return;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuid);
							if (muteData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									
									final String finalReason = reason;
									final long finalExpires = expires;
									
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".mute.muted", true);
											core.getConfig().set("users." + target.getName() + ".mute.reason", finalReason);
											core.getConfig().set("users." + target.getName() + ".mute.duration", finalExpires);
											core.saveConfig();
											sendMuteInfo(target, finalReason, finalExpires);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " hiljensi pelaajan " + name 
										+ " ajaksi '" + args[1] + args[2] + "' syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " hiljennettiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "tempmute";
								String duration = "" + expires;
								String time = "" + System.currentTimeMillis();
								String muter = sender.getName();
								MySQLUtils.set("INSERT INTO player_mute (uuid, name, muter, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, muter, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?, ?)", 
										uuid, name, type, muter, reason, duration, time);
							}
							else {
								sender.sendMessage(tc3 + "Tämä pelaaja on jo hiljennetty!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/tempmute <pelaaja> <kesto> s/min/h/d/w/mon/y <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// unmute
		
		if (cmd.getName().equalsIgnoreCase("unmute")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							
							boolean silent = false;
							if (args.length >= 2 && args[1].equals("-s")) {
								silent = true;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuid);
							if (muteData != null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".mute", null);
											core.saveConfig();
											sendUnmuteInfo(target);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§a" + sender.getName() + " poisti hiljennyksen pelaajalta " + name;
								String playerBroadcastMessage = "§7Pelaajan " + name + " hiljennys poistettiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§a(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "unmute";
								String time = "" + System.currentTimeMillis();
								String remover = sender.getName();
								MySQLUtils.set("DELETE FROM player_mute WHERE uuid=?", uuid);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, time) VALUES (?, ?, ?, ?, ?)", 
										uuid, name, type, remover, time);
							}
							else {
								sender.sendMessage(tc3 + "Tätä pelaajaa ei ole hiljennetty!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/unmute <pelaaja> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// jail
		
		if (cmd.getName().equalsIgnoreCase("jail")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 2) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 1; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuid);
							if (jailData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									final String finalReason = reason;
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".jail.jailed", true);
											core.getConfig().set("users." + target.getName() + ".jail.reason", finalReason);
											core.getConfig().set("users." + target.getName() + ".jail.duration", 0);
											core.saveConfig();
											teleportToJail(target, finalReason, 0);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " vangitsi pelaajan " + name 
										+ " syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " vangittiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "jail";
								String duration = "0";
								String time = "" + System.currentTimeMillis();
								String jailer = sender.getName();
								MySQLUtils.set("INSERT INTO player_jail (uuid, name, jailer, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, jailer, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, type, jailer, reason, time);
							}
							else {
								sender.sendMessage(tc3 + "Tämä pelaaja on jo vangittu!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/jail <pelaaja> <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// tempjail
		
		if (cmd.getName().equalsIgnoreCase("tempjail")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 4) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 3; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							long expires = System.currentTimeMillis();
							
							try {
								double d = Double.parseDouble(args[1]);
								String unit = args[2];
								if (unit.equals("s")) {
									expires += (long) (d * 1000);
								}
								else if (unit.equals("min")) {
									expires += (long) (d * 1000 * 60);
								}
								else if (unit.equals("h")) {
									expires += (long) (d * 1000 * 60 * 60);
								}
								else if (unit.equals("d")) {
									expires += (long) (d * 1000 * 60 * 60 * 24);
								}
								else if (unit.equals("w")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 7);
								}
								else if (unit.equals("mon")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30);
								}
								else if (unit.equals("y")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30 * 12);
								}
								else {
									sender.sendMessage(tc3 + "Käytettävissä olevat ajan muodot: s, min, h, d, w, mon, y");
									return;
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Virheellinen vankilatuomion kesto!");
								return;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuid);
							if (jailData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									
									final String finalReason = reason;
									final long finalExpires = expires;
									
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".jail.jailed", true);
											core.getConfig().set("users." + target.getName() + ".jail.reason", finalReason);
											core.getConfig().set("users." + target.getName() + ".jail.duration", finalExpires);
											core.saveConfig();
											teleportToJail(target, finalReason, finalExpires);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " vangitsi pelaajan " + name 
										+ " ajaksi '" + args[1] + args[2] + "' syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " vangittiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "tempjail";
								String duration = "" + expires;
								String time = "" + System.currentTimeMillis();
								String jailer = sender.getName();
								MySQLUtils.set("INSERT INTO player_jail (uuid, name, jailer, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, jailer, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?, ?)", 
										uuid, name, type, jailer, reason, duration, time);
							}
							else {
								sender.sendMessage(tc3 + "Tämä pelaaja on jo vangittu!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/tempjail <pelaaja> <kesto> s/min/h/d/w/mon/y <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// unjail
		
		if (cmd.getName().equalsIgnoreCase("unjail")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							
							boolean silent = false;
							if (args.length >= 2 && args[1].equals("-s")) {
								silent = true;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuid);
							if (jailData != null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									new BukkitRunnable() {
										public void run() {
											core.getConfig().set("users." + target.getName() + ".jail", null);
											core.saveConfig();
											releaseFromJail(target);
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§a" + sender.getName() + " vapautti vankilasta pelaajan " + name;
								String playerBroadcastMessage = "§7" + name + " vapautettiin vankilasta.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§a(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "unjail";
								String time = "" + System.currentTimeMillis();
								String remover = sender.getName();
								MySQLUtils.set("DELETE FROM player_jail WHERE uuid=?", uuid);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, time) VALUES (?, ?, ?, ?, ?)", 
										uuid, name, type, remover, time);
							}
							else {
								sender.sendMessage(tc3 + "Tätä pelaajaa ei ole vangittu!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/unjail <pelaaja> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// ban
		
		if (cmd.getName().equalsIgnoreCase("ban")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 2) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 1; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuid);
							if (banData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									final String finalReason = reason;
									new BukkitRunnable() {
										public void run() {
											target.kickPlayer("§c§m--------------------------------\n§c \n§cSinulle on annettu porttikielto " 
													+ "tälle palvelimelle seuraavalla syyllä:\n§c \n§c§o" + finalReason  
													+ "\n§c \n§c \n§7Tämä porttikielto on ikuinen." 
													+ "\n§c \n§c§m--------------------------------");
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " antoi porttikiellon pelaajalle " + name 
										+ " syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " sai porttikiellon palvelimelle.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "ban";
								String duration = "0";
								String time = "" + System.currentTimeMillis();
								String banner = sender.getName();
								MySQLUtils.set("INSERT INTO player_ban (uuid, name, banner, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, banner, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, type, banner, reason, time);
							}
							else {
								sender.sendMessage(tc3 + "Tällä pelaajalla on jo porttikielto palvelimelle!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/ban <pelaaja> <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// tempban
		
		if (cmd.getName().equalsIgnoreCase("tempban")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 4) {
					new BukkitRunnable() {
						public void run() {
							
							String reason = "";
							for (int i = 3; i < args.length; i++) {
								reason = reason + " " + args[i];
							}
							reason = reason.trim();
							
							boolean silent = false;
							if (reason.endsWith(" -s")) {
								silent = true;
								reason = reason.substring(0, reason.length() - 3);
							}
							
							long expires = System.currentTimeMillis();
							
							try {
								double d = Double.parseDouble(args[1]);
								String unit = args[2];
								if (unit.equals("s")) {
									expires += (long) (d * 1000);
								}
								else if (unit.equals("min")) {
									expires += (long) (d * 1000 * 60);
								}
								else if (unit.equals("h")) {
									expires += (long) (d * 1000 * 60 * 60);
								}
								else if (unit.equals("d")) {
									expires += (long) (d * 1000 * 60 * 60 * 24);
								}
								else if (unit.equals("w")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 7);
								}
								else if (unit.equals("mon")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30);
								}
								else if (unit.equals("y")) {
									expires += (long) (d * 1000 * 60 * 60 * 24 * 30 * 12);
								}
								else {
									sender.sendMessage(tc3 + "Käytettävissä olevat ajan muodot: s, min, h, d, w, mon, y");
									return;
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Virheellinen porttikiellon kesto!");
								return;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuid);
							if (banData == null) {
								
								Player target = Bukkit.getPlayer(name);
								if (target != null) {
									
									final String finalReason = reason;
									final long finalExpires = expires;
									
									new BukkitRunnable() {
										public void run() {
											target.kickPlayer("§c§m--------------------------------\n§c \n§cSinulle on annettu porttikielto " 
													+ "tälle palvelimelle seuraavalla syyllä:\n§c \n§c§o" + finalReason  
													+ "\n§c \n§c \n§7Aikaa jäjellä: " + CoreUtils.getDaysAndHoursAndMinsFromMillis(finalExpires - System.currentTimeMillis()) 
													+ ".\n§c \n§c§m--------------------------------");
										}
									}.runTask(core);
								}
								
								String broadcastMessage = "§c" + sender.getName() + " antoi porttikiellon pelaajalle " + name 
										+ " ajaksi '" + args[1] + args[2] + "' syyllä '" + reason + "'";
								String playerBroadcastMessage = "§7" + name + " sai porttikiellon palvelimelle.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§c(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "tempban";
								String duration = "" + expires;
								String time = "" + System.currentTimeMillis();
								String banner = sender.getName();
								MySQLUtils.set("INSERT INTO player_ban (uuid, name, banner, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?)", 
										uuid, name, banner, reason, duration, time);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, reason, duration, time) VALUES (?, ?, ?, ?, ?, ?, ?)", 
										uuid, name, type, banner, reason, duration, time);
							}
							else {
								sender.sendMessage(tc3 + "Tällä pelaajalla on jo porttikielto palvelimelle!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/tempban <pelaaja> <kesto> s/min/h/d/w/mon/y <syy> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// unban
		
		if (cmd.getName().equalsIgnoreCase("unban")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							
							boolean silent = false;
							if (args.length >= 2 && args[1].equals("-s")) {
								silent = true;
							}
							
							String name;
							String uuid;
							
							try {
								InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]).openStream();
								@SuppressWarnings("deprecation")
								String raw = IOUtils.toString(inputStream);
								inputStream.close();
								String[] data = raw.split("\"");
								if (data.length > 7) {
									uuid = data[3];
									name = data[7];
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty Minecraft-käyttäjää antamallasi käyttäjänimellä!");
									return;
								}
							}
							catch (Exception e) {
								sender.sendMessage(tc3 + "Virhe haettaessa pelaajan tietoja Mojangin palvelimelta!");
								return;
							}
							
							MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuid);
							if (banData != null) {
								
								String broadcastMessage = "§a" + sender.getName() + " poisti porttikiellon pelaajalta " + name;
								String playerBroadcastMessage = "§7Pelaajan " + name + " porttikielto poistettiin.";
								
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(player, "valvoja") && !silent) {
										player.sendMessage(broadcastMessage);
									}
									else if (CoreUtils.hasRank(player, "ylläpitäjä") || player.getName().equals(sender.getName())) {
										player.sendMessage("§a(Hiljainen) " + broadcastMessage);
									}
									else if (!silent) {
										player.sendMessage(playerBroadcastMessage);
									}
								}
								Bukkit.getConsoleSender().sendMessage(broadcastMessage);
								
								logAction(ChatColor.stripColor(broadcastMessage));
								
								String type = "unban";
								String time = "" + System.currentTimeMillis();
								String remover = sender.getName();
								MySQLUtils.set("DELETE FROM player_ban WHERE uuid=?", uuid);
								MySQLUtils.set("INSERT INTO player_history (uuid, name, type, giver, time) VALUES (?, ?, ?, ?, ?)", 
										uuid, name, type, remover, time);
							}
							else {
								sender.sendMessage(tc3 + "Tällä pelaajalla ei ole porttikieltoa palvelimelle!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/unban <pelaaja> [-s]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// ipban
		
		if (cmd.getName().equalsIgnoreCase("ipban")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("list")) {
						sender.sendMessage("");
						sender.sendMessage(tc2 + "§m----------" + tc1 + " IP-estot " + tc2 + "§m----------");
						sender.sendMessage("");
						MySQLResult ipBansData = MySQLUtils.get("SELECT * FROM ipbans");
						if (ipBansData != null) {
							for (int i = 0; i < ipBansData.getRows(); i++) {
								String ip = ipBansData.getString(i, "ip");
								String note = ipBansData.getString(i, "note");
								String joinedUuids = ipBansData.getStringNotNull(i, "joined_uuids");
								String[] uuids = joinedUuids.split(";");
								sender.sendMessage(tc1 + " - " + tc2 + ip + ", §o" + note);
								for (String uuid : uuids) {
									if (!uuid.isEmpty()) {
										sender.sendMessage(tc1 + "    - " + tc2 + uuid);
									}
								}
							}
						}
						else {
							sender.sendMessage(tc3 + " Ei IP-estoja!");
						}
						sender.sendMessage("");
					}
					else {
						String ip = args[0];
						String note = "";
						for (int i = 1; i < args.length; i++) {
							note = note + " " + args[i];
						}
						note = note.trim();
						MySQLResult ipBansData = MySQLUtils.get("SELECT * FROM ipbans WHERE ip=?", ip);
						if (ipBansData == null) {
							MySQLUtils.set("INSERT INTO ipbans (ip, note, joined_uuids) VALUES (?, ?, ?)", ip, note, "");
							sender.sendMessage(tc2 + "Lisättiin " + tc1 + ip + tc2 + " IP-osoitteiden estolistalle!");
						}
						else {
							sender.sendMessage(tc3 + "Tämä IP-osoite on jo estolistalla!");
						}
					}
				}
				else {
					sender.sendMessage(usage + "/ipban <ip> [selvennys]" + tc3 + " tai " + tc4 + "/ipban list");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// ipunban
		
		if (cmd.getName().equalsIgnoreCase("ipunban")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 1) {
					String ip = args[0];
					MySQLResult ipBansData = MySQLUtils.get("SELECT * FROM ipbans WHERE ip=?", ip);
					if (ipBansData != null) {
						MySQLUtils.set("DELETE FROM ipbans WHERE ip=?", ip);
						sender.sendMessage(tc2 + "Poistettiin " + tc1 + ip + tc2 + " IP-osoitteiden estolistalta!");
					}
					else {
						sender.sendMessage(tc3 + "Tätä IP-osoitetta ei ole estolistalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/ipunban <ip>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// showlog
		
		if (cmd.getName().equalsIgnoreCase("showlog")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				sender.sendMessage("");
				sender.sendMessage(tc2 + "§m----------" + tc1 + " Rangaistusloki " + tc2 + "§m----------");
				sender.sendMessage("");
				List<String> log = core.getConfig().getStringList("punishment-log");
				if (log.isEmpty()) {
					sender.sendMessage(tc3 + " Ei lokitietoja!");
				}
				else {
					for (String entry : log) {
						sender.sendMessage(" - " + entry);
					}
				}
				sender.sendMessage("");
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// setjail
		
		if (cmd.getName().equalsIgnoreCase("setjail")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					CoreUtils.setLocation(core, "jail", player.getLocation());
					sender.sendMessage(tc2 + "Asetettiin vankilan sijainti nykyiseen sijaintiisi!");
				}
				else {
					sender.sendMessage(tc3 + "Et voi asettaa vankilan sijaintia konsolista käsin!");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		return true;
	}
	
	public void teleportToJail(Player player, String reason, long expires) {
		Location location = CoreUtils.loadLocation(core, "jail");
		if (location == null) {
			location = player.getLocation();
		}
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1, 1);
		player.setGameMode(GameMode.ADVENTURE);
		player.sendTitle("§4§lSinut on vangittu!", "", 20, 40, 20);
		player.sendMessage("");
		player.sendMessage("§c§m--------------------------------");
		player.sendMessage("");
		player.sendMessage("§c§l Sinut on vangittu!");
		player.sendMessage("");
		player.sendMessage("§c  Syy: §7§o" + reason);
		player.sendMessage("");
		if (expires != 0) {
			player.sendMessage("§c  Aikaa vapautumiseen: §7" + CoreUtils.getDaysAndHoursAndMinsFromMillis(expires - System.currentTimeMillis() + 2000));
		}
		else {
			player.sendMessage("§c  Vankilatuomiosi on §nelinkautinen§c.");
		}
		player.sendMessage("");
		player.sendMessage("§c§m--------------------------------");
		CoreUtils.updateTabForAll();
	}
	
	public void releaseFromJail(Player player) {
		Location location = CoreUtils.loadLocation(core, "spawn");
		if (location == null) {
			location = player.getLocation();
		}
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
		player.setGameMode(GameMode.SURVIVAL);
		player.sendTitle("§2§lSinut on vapautettu!", "", 20, 40, 20);
		player.sendMessage("");
		player.sendMessage("§a§m--------------------------------");
		player.sendMessage("");
		player.sendMessage("§a§l Sinut on vapautettu vankilasta!");
		player.sendMessage("");
		player.sendMessage("§a§m--------------------------------");
		CoreUtils.updateTabForAll();
	}
	
	public void sendMuteInfo(Player player, String reason, long expires) {
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		player.sendMessage("");
		player.sendMessage("§c§m--------------------------------");
		player.sendMessage("");
		player.sendMessage("§c§l Sinut on hiljennetty!");
		player.sendMessage("");
		player.sendMessage("§c  Syy: §7§o" + reason);
		player.sendMessage("");
		if (expires != 0) {
			player.sendMessage("§c  Hiljennystä jäljellä: §7" + CoreUtils.getDaysAndHoursAndMinsFromMillis(expires - System.currentTimeMillis() + 2000));
		}
		else {
			player.sendMessage("§c  Hiljennyksesi on §nikuinen§c.");
		}
		player.sendMessage("");
		player.sendMessage("§c§m--------------------------------");
	}
	
	public void sendUnmuteInfo(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
		player.sendMessage("");
		player.sendMessage("§a§m--------------------------------");
		player.sendMessage("");
		player.sendMessage("§a§l Hiljennyksesi on päättynyt!");
		player.sendMessage("");
		player.sendMessage("§a§m--------------------------------");
	}
	
	private void logAction(String text) {
		List<String> log = core.getConfig().getStringList("punishment-log");
		log.add(text);
		core.getConfig().set("punishment-log", log);
		core.saveConfig();
	}
}