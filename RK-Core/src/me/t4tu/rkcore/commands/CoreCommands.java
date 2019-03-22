package me.t4tu.rkcore.commands;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.ChatPaginator;

import com.meowj.langutils.lang.LanguageHelper;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.inventories.InventoryGUIAction;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.FriendRequest;
import me.t4tu.rkcore.utils.GuildRequest;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import me.t4tu.rkcore.utils.TeleportRequest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CoreCommands implements CommandExecutor {
	
	private Core core;
	private List<String> vanishedPlayers;
	private List<String> godPlayers;
	private List<String> debugPlayers;
	private List<String> spyPlayers;
	private List<String> commandSpyPlayers;
	private List<String> teleportingPlayers;
	private List<String> powerTools;
	private List<Location> tardisBlocks1;
	private List<Location> tardisBlocks2;
	private List<Location> tardisBlocks3;
	private Map<String, String> mailWritingPlayers;
	private Map<String, PermissionAttachment> permissions;
	private Map<String, ArmorStand> selectedHolograms;
	private Block b1;
	private Block b2;
	private int autoRestartTaskId;
	private boolean evokkiModeEnabled;
	private boolean canTardisMove1;
	private boolean canTardisMove2;
	private boolean canTardisMove3;
	
	public CoreCommands(Core core) {
		this.core = core;
		vanishedPlayers = new ArrayList<String>();
		godPlayers = new ArrayList<String>();
		debugPlayers = new ArrayList<String>();
		spyPlayers = new ArrayList<String>();
		commandSpyPlayers = new ArrayList<String>();
		teleportingPlayers = new ArrayList<String>();
		powerTools = new ArrayList<String>();
		tardisBlocks1 = new ArrayList<Location>();
		tardisBlocks2 = new ArrayList<Location>();
		tardisBlocks3 = new ArrayList<Location>();
		mailWritingPlayers = new HashMap<String, String>();
		permissions = new HashMap<String, PermissionAttachment>();
		selectedHolograms = new HashMap<String, ArmorStand>();
		b1 = null;
		b2 = null;
		autoRestartTaskId = -1;
		evokkiModeEnabled = true;
		canTardisMove1 = true;
		canTardisMove2 = true;
		canTardisMove3 = true;
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
	
	public List<String> getVanishedPlayers() {
		return vanishedPlayers;
	}
	
	public List<String> getGodPlayers() {
		return godPlayers;
	}
	
	public List<String> getDebugPlayers() {
		return debugPlayers;
	}
	
	public List<String> getSpyPlayers() {
		return spyPlayers;
	}
	
	public List<String> getCommandSpyPlayers() {
		return commandSpyPlayers;
	}
	
	public List<String> getTeleportingPlayers() {
		return teleportingPlayers;
	}
	
	public List<String> getPowerTools() {
		return powerTools;
	}
	
	public List<Location> getTardisBlocks(int i) {
		if (i == 0) {
			return tardisBlocks1;
		}
		else if (i == 1) {
			return tardisBlocks2;
		}
		else {
			return tardisBlocks3;
		}
	}
	
	public Map<String, String> getMailWritingPlayers() {
		return mailWritingPlayers;
	}
	
	public Map<String, PermissionAttachment> getPermissions() {
		return permissions;
	}
	
	public Block getBlockOne() {
		return b1;
	}
	
	public Block getBlockTwo() {
		return b2;
	}
	
	public void setBlockOne(Block b1) {
		this.b1 = b1;
	}
	
	public void setBlockTwo(Block b2) {
		this.b2 = b2;
	}
	
	public boolean isEvokkiModeEnabled() {
		return evokkiModeEnabled;
	}
	
	public boolean canTardisMove(int i) {
		if (i == 0) {
			return canTardisMove1;
		}
		else if (i == 1) {
			return canTardisMove2;
		}
		else {
			return canTardisMove3;
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		
		String usage = CoreUtils.getUsageString();
		String noPermission = CoreUtils.getNoPermissionString();
		String playersOnly = CoreUtils.getPlayersOnlyString();
		
		// reconnect
		
		if (cmd.getName().equalsIgnoreCase("reconnect")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				sender.sendMessage(tc2 + "Yhdistetään tietokantaan...");
				MySQLUtils.closeConnection();
				if (MySQLUtils.openConnection()) {
					sender.sendMessage(tc2 + "Yhdistettiin tietokantaan onnistuneesti!");
				}
				else {
					sender.sendMessage(tc3 + "Virhe yhdistettäessä tietokantaan!");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// updateplayerdata
		
		if (cmd.getName().equalsIgnoreCase("updateplayerdata")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						new BukkitRunnable() {
							public void run() {
								// TODO
								String name = target.getName();
								String uuid = target.getUniqueId().toString();
								MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE uuid=?", uuid);
								MySQLResult statsData = MySQLUtils.get("SELECT * FROM player_stats WHERE uuid=?", uuid);
								core.getConfig().set("users." + name + ".chat_prefix", infoData.getStringNotNull(0, "chat_prefix"));
								core.getConfig().set("users." + name + ".chat_color", infoData.getStringNotNull(0, "chat_color"));
								core.getConfig().set("users." + name + ".chat_nick", infoData.getString(0, "chat_nick"));
								core.getConfig().set("users." + name + ".rank", infoData.getString(0, "rank"));
								core.getConfig().set("users." + name + ".status", statsData.getString(0, "status"));
								core.saveConfig();
								SettingsUtils.reloadSettings(target);
								CoreUtils.updatePermissions(target);
								CoreUtils.updateNotes(target);
								CoreUtils.updateTabForAll();
								target.updateCommands();
								sender.sendMessage(tc2 + "Päivitettiin pelaajan " + tc1 + name + tc2 + " tiedot!");
							}
						}.runTaskAsynchronously(core);
					}
					else {
						sender.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/updateplayerdata <pelaaja>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// playerlookup
		
		if (cmd.getName().equalsIgnoreCase("playerlookup")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
							if (infoData != null) {
								
								String name = infoData.getString(0, "name");
								String uuid = infoData.getString(0, "uuid");
								String uuidWithoutDashes = uuid.replace("-", "");
								String ip = infoData.getString(0, "ip");
								String chatPrefix = infoData.getStringNotNull(0, "chat_prefix");
								String chatColor = infoData.getStringNotNull(0, "chat_color");
								String chatNick = infoData.getStringNotNull(0, "chat_nick");
								String rank = infoData.getStringNotNull(0, "rank");
								long seconds = infoData.getLong(0, "seconds");
								if (core.getOntimes().containsKey(uuid)) {
									seconds += core.getOntimes().get(uuid);
								}
								String secondsString = CoreUtils.getHoursAndMinsFromMillis(seconds * 1000);
								long lastSeen = infoData.getLong(0, "last_seen");
								String lastSeenString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
										new Date(lastSeen + CoreUtils.TIME_OFFSET));
								long nickLastChanged = infoData.getLong(0, "nick_last_changed");
								String nickLastChangedString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
										new Date(nickLastChanged + CoreUtils.TIME_OFFSET));
								
								sender.sendMessage("§e§m--------------------");
								sender.sendMessage("§eNimi: §r" + name);
								sender.sendMessage("§eUUID: §r" + uuid);
								sender.sendMessage("§eIP: §r" + ip);
								sender.sendMessage("§eEtuliite: §r" + chatPrefix);
								sender.sendMessage("§eVäri: §r" + chatColor);
								sender.sendMessage("§eLempinimi: §r" + chatNick);
								sender.sendMessage("§eLempinimi vaihdettu: §r" + nickLastChangedString);
								sender.sendMessage("§eArvo: §r" + rank);
								sender.sendMessage("§ePelannut: §r" + secondsString);
								if (Bukkit.getPlayer(name) != null) {
									sender.sendMessage("§eViimeksi nähty: §rNyt");
								}
								else {
									sender.sendMessage("§eViimeksi nähty: §r" + lastSeenString);
								}
								
								MySQLResult statsData = MySQLUtils.get("SELECT * FROM player_stats WHERE uuid=?", uuid);
								if (statsData != null) {
									int money = statsData.getInt(0, "money");
									int profession = statsData.getInt(0, "profession");
									long professionLastChanged = statsData.getLong(0, "profession_last_changed");
									String professionLastChangedString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
											new Date(professionLastChanged + CoreUtils.TIME_OFFSET));
									boolean visited1 = statsData.getBoolean(0, "visited_1");
									boolean visited2 = statsData.getBoolean(0, "visited_2");
									boolean visited3 = statsData.getBoolean(0, "visited_3");
									String status = statsData.getString(0, "status");
									
									sender.sendMessage("§eRahat: §r" + money);
									sender.sendMessage("§eAmmatti: §r" + profession); // TODO
									sender.sendMessage("§eAmmatti vaihdettu: §r" + professionLastChangedString);
									sender.sendMessage("§evisited_1: §r" + visited1);
									sender.sendMessage("§evisited_2: §r" + visited2);
									sender.sendMessage("§evisited_3: §r" + visited3);
									sender.sendMessage("§eTilaviesti: §r" + status);
								}
								
								sender.sendMessage("§eKaverit: §r");
								
								for (String friend : CoreUtils.getFriendsUuids(name)) {
									MySQLResult friendData = MySQLUtils.get("SELECT name FROM player_info WHERE uuid=?", friend);
									if (friendData != null) {
										sender.sendMessage("§e - §r" + friendData.getString(0, "name"));
									}
								}
								
								MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes);
								if (banData != null) {
									
									String banner = banData.getString(0, "banner");
									String reason = banData.getString(0, "reason");
									long time = banData.getLong(0, "time");
									long duration = banData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									
									sender.sendMessage("§ePorttikielto:");
									sender.sendMessage("§e - Antanut: §r" + banner);
									sender.sendMessage("§e - Annettu: §r" + timeGiven);
									sender.sendMessage("§e - Päättyy: §r" + expires);
									sender.sendMessage("§e - Syy: §r" + reason);
								}
								else {
									sender.sendMessage("§ePorttikielto: §rEi ole");
								}
								
								MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuidWithoutDashes);
								if (jailData != null) {
									
									String jailer = jailData.getString(0, "jailer");
									String reason = jailData.getString(0, "reason");
									long time = jailData.getLong(0, "time");
									long duration = jailData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									
									sender.sendMessage("§eVangittu:");
									sender.sendMessage("§e - Antanut: §r" + jailer);
									sender.sendMessage("§e - Annettu: §r" + timeGiven);
									sender.sendMessage("§e - Päättyy: §r" + expires);
									sender.sendMessage("§e - Syy: §r" + reason);
								}
								else {
									sender.sendMessage("§eVangittu: §rEi ole");
								}
								
								MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuidWithoutDashes);
								if (muteData != null) {
									
									String banner = muteData.getString(0, "muter");
									String reason = muteData.getString(0, "reason");
									long time = muteData.getLong(0, "time");
									long duration = muteData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									
									sender.sendMessage("§eHiljennys:");
									sender.sendMessage("§e - Antanut: §r" + banner);
									sender.sendMessage("§e - Annettu: §r" + timeGiven);
									sender.sendMessage("§e - Päättyy: §r" + expires);
									sender.sendMessage("§e - Syy: §r" + reason);
								}
								else {
									sender.sendMessage("§eHiljennys: §rEi ole");
								}
								
								sender.sendMessage("§e§m--------------------");
							}
							else {
								sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/playerlookup <pelaaja>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// kickall
		
		if (cmd.getName().equalsIgnoreCase("kickall")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 1) {
					String reason = "";
					for (String word : args) {
						reason = reason + " " + word;
					}
					reason = ChatColor.translateAlternateColorCodes('&', tc3 + reason.trim());
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (!player.getName().equals(sender.getName())) {
							player.kickPlayer(reason);
						}
					}
				}
				else {
					sender.sendMessage(usage + "/kickall <syy>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// huolto
		
		if (cmd.getName().equalsIgnoreCase("huolto")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				boolean maintenanceMode = core.getCoreListener().getMaintenanceMode();
				if (maintenanceMode) {
					core.getConfig().set("maintenance-mode", false);
					core.getCoreListener().setMaintenanceMode(false);
					sender.sendMessage(tc2 + "Huoltotila pois päältä!");
				}
				else {
					core.getConfig().set("maintenance-mode", true);
					core.getCoreListener().setMaintenanceMode(true);
					sender.sendMessage(tc2 + "Huoltotila päällä!");
				}
				core.saveConfig();
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// xstop
		
		if (cmd.getName().equalsIgnoreCase("xstop")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 1) {
					String reason = "";
					for (String word : args) {
						reason = reason + " " + word;
					}
					reason = ChatColor.translateAlternateColorCodes('&', tc3 + reason.trim());
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.kickPlayer(reason);
					}
					core.getConfig().set("users", null);
					core.saveConfig();
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
				}
				else {
					sender.sendMessage(usage + "/xstop <syy>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// autorestart
		
		if (cmd.getName().equalsIgnoreCase("autorestart")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 1 && args[0].equalsIgnoreCase("cancel")) {
					if (autoRestartTaskId != -1) {
						Bukkit.getScheduler().cancelTask(autoRestartTaskId);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "send all chat &a&lPalvelimen uudelleenkäynnistys keskeytetty!");
						autoRestartTaskId = -1;
					}
					else {
						sender.sendMessage(tc3 + "Autorestart ei ole käynnissä!");
					}
					return true;
				}
				if (autoRestartTaskId != -1) {
					sender.sendMessage(tc3 + "Autorestart on jo käynnissä!");
					return true;
				}
				new BukkitRunnable() {
					int i = 0;
					CommandSender console = Bukkit.getConsoleSender();
					public void run() {
						if (i == 0) {
							autoRestartTaskId = getTaskId();
							Bukkit.dispatchCommand(console, "send all chat &c&lPalvelin käynnistyy uudelleen 1 minuutin kuluttua!");
							for (Player player : Bukkit.getOnlinePlayers()) {
								player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 2);
							}
						}
						if (i == 30) {
							Bukkit.dispatchCommand(console, "send all chat &c&lPalvelin käynnistyy uudelleen 30 sekunnin kuluttua!");
						}
						if (i == 45) {
							Bukkit.dispatchCommand(console, "send all chat &c&lPalvelin käynnistyy uudelleen 15 sekunnin kuluttua!");
						}
						if (i >= 50 && i < 60) {
							Bukkit.dispatchCommand(console, "send all chat &c&lPalvelin käynnistyy uudelleen " + (60 - i) + " sekunnin kuluttua!");
						}
						if (i >= 60) {
							cancel();
							Bukkit.dispatchCommand(console, "send all chat &c&lPalvelin käynnistyy uudelleen...");
							Bukkit.dispatchCommand(console, "xstop Palvelin käynnistyy uudelleen...");
						}
						i++;
					}
				}.runTaskTimer(core, 0, 20);
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// config
		
		if (cmd.getName().equalsIgnoreCase("config")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						if (args.length >= 2) {
							Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
							if (plugin != null) {
								plugin.reloadConfig();
								sender.sendMessage(tc2 + "Uudelleenladattiin pluginin " + tc1 + plugin.getName() + tc2 + " config.yml!");
							}
							else {
								sender.sendMessage(tc3 + "Tuntematon plugin!");
							}
						}
						else {
							sender.sendMessage(usage + "/config reload <plugin>");
						}
					}
					else if (args.length >= 3) {
						Plugin plugin = Bukkit.getPluginManager().getPlugin(args[0]);
						if (plugin == null) {
							sender.sendMessage(tc3 + "Tuntematon plugin!");
							return true;
						}
						boolean cannotBeBoolean = false;
						String value = "";
						for (int i = 2; i < args.length; i++) {
							value = value + " " + args[i];
						}
						value = value.trim();
						if (value.startsWith("'") && value.endsWith("'")) {
							cannotBeBoolean = true;
							value = value.substring(1, value.length() - 1);
						}
						if (value.equalsIgnoreCase("null")) {
							plugin.getConfig().set(args[1], null);
							plugin.saveConfig();
							sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi " + tc1 + "null" + tc2 + " (null)");
							return true;
						}
						else if (value.equalsIgnoreCase("true") && !cannotBeBoolean) {
							plugin.getConfig().set(args[1], true);
							plugin.saveConfig();
							sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi " + tc1 + "true" + tc2 + " (boolean)");
							return true;
						}
						else if (value.equalsIgnoreCase("false") && !cannotBeBoolean) {
							plugin.getConfig().set(args[1], false);
							plugin.saveConfig();
							sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi " + tc1 + "false" + tc2 + " (boolean)");
							return true;
						}
						try {
							int i = Integer.parseInt(value);
							plugin.getConfig().set(args[1], i);
							plugin.saveConfig();
							sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi " + tc1 + i + tc2 + " (int)");
						}
						catch (NumberFormatException e) {
							try {
								double d = Double.parseDouble(value);
								plugin.getConfig().set(args[1], d);
								plugin.saveConfig();
								sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi " + tc1 + d + tc2 + " (double)");
							}
							catch (NumberFormatException e2) {
								plugin.getConfig().set(args[1], value);
								plugin.saveConfig();
								sender.sendMessage(tc2 + "Asetettiin polun " + tc1 + args[1] + tc2 + " arvoksi '" + tc1 + value + tc2 + "' (String)");
							}
						}
					}
					else {
						sender.sendMessage(usage + "/config <plugin> <polku> <arvo>");
					}
				}
				else {
					sender.sendMessage(usage + "/config <plugin> <polku> <arvo>" + tc3 + " tai " + tc4 + "/config reload <plugin>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// sql
		
		if (cmd.getName().equalsIgnoreCase("sql")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 2) {
					if (args[0].equalsIgnoreCase("get")) {
						new BukkitRunnable() {
							public void run() {
								String query = "";
								for (int i = 1; i < args.length; i++) {
									query = query + " " + args[i];
								}
								query = query.trim();
								MySQLResult result = MySQLUtils.get(query);
								if (result != null) {
									for (int i = 0; i < result.getRows(); i++) {
										sender.sendMessage(tc2 + "§m--------------------");
										for (String column : result.getResult().get(i).keySet()) {
											sender.sendMessage(tc1 + " " + column + ": " + tc2 + result.getString(i, column));
										}
										sender.sendMessage(tc2 + "§m--------------------");
									}
								}
								else {
									sender.sendMessage(tc3 + "Ei yhtäkään kyselyä vastaavaa riviä!");
								}
							}
						}.runTaskAsynchronously(core);
					}
					else if (args[0].equalsIgnoreCase("set")) {
						new BukkitRunnable() {
							public void run() {
								String query = "";
								for (int i = 1; i < args.length; i++) {
									query = query + " " + args[i];
								}
								query = query.trim();
								int i = MySQLUtils.set(query);
								sender.sendMessage(tc2 + "SQL-kysely lähetetty, vaikutusalue " + tc1 + i + tc2 + " rivi(ä)!");
							}
						}.runTaskAsynchronously(core);
					}
					else {
						sender.sendMessage(usage + "/sql <get/set> <query>");
					}
				}
				else {
					sender.sendMessage(usage + "/sql <get/set> <query>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// list, who, pelaajat
		
		if (cmd.getName().equalsIgnoreCase("list") || cmd.getName().equalsIgnoreCase("who") || cmd.getName().equalsIgnoreCase("pelaajat")) {
			if (sender.getName().equals(Bukkit.getConsoleSender().getName()) && args.length == 0) {
				return true;
			}
			new BukkitRunnable() {
				public void run() {
					MySQLResult globalData = MySQLUtils.get("SELECT * FROM global");
					sender.sendMessage("");
					sender.sendMessage(tc2 + "§m----------" + tc1 + " Pelaajat " + tc2 + "§m----------");
					sender.sendMessage("");
					sender.sendMessage(tc2 + " Palvelimelle on liittynyt yhteensä " + tc1 + globalData.getInt(0, "uniquejoins") + tc2 + " pelaajaa,");
					sender.sendMessage(tc2 + " pelaajaennätys on " + tc1 + globalData.getInt(0, "record") + tc2 + " samanaikaista pelaajaa.");
					sender.sendMessage("");
					if (CoreUtils.hasRank(sender, "valvoja")) {
						sender.sendMessage(tc2 + " Paikalla on tällä hetkellä " + tc1 + Bukkit.getOnlinePlayers().size() + tc2 + " pelaajaa:");
					}
					else {
						sender.sendMessage(tc2 + " Paikalla on tällä hetkellä " + tc1 + (Bukkit.getOnlinePlayers().size() - vanishedPlayers.size()) 
								+ tc2 + " pelaajaa:");
					}
					sender.sendMessage("");
					String playerList = "";
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (vanishedPlayers.contains(player.getName())) {
							if (CoreUtils.hasRank(sender, "valvoja")) {
								playerList = playerList + " [V]" + player.getName();
							}
						}
						else {
							playerList = playerList + " " + player.getName();
						}
					}
					playerList = playerList.trim().replace(" ", tc1 + ", " + tc2);
					sender.sendMessage(tc2 + "  " + playerList);
					sender.sendMessage("");
				}
			}.runTaskAsynchronously(core);
			return true;
		}
		
		// alert
		
		if (cmd.getName().equalsIgnoreCase("alert")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 2) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
						message = message + " " + args[i];
					}
					message = ChatColor.translateAlternateColorCodes('&', message.trim());
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (args[0].equalsIgnoreCase("chat")) {
							player.sendMessage("");
							player.sendMessage("§4§lIlmoitus: §c" + message);
							player.sendMessage("");
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						}
						else if (args[0].equalsIgnoreCase("title")) {
							player.sendTitle("§4§lIlmoitus:", "§c" + message, 20, 60, 20);
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						}
						else if (args[0].equalsIgnoreCase("both")) {
							player.sendMessage("");
							player.sendMessage("§4§lIlmoitus: §c" + message);
							player.sendMessage("");
							player.sendTitle("§4§lIlmoitus:", "§c" + message, 20, 60, 20);
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						}
						else {
							sender.sendMessage(usage + "/alert <chat/title/both> <viesti>");
						}
					}
				}
				else {
					sender.sendMessage(usage + "/alert <chat/title/both> <viesti>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// send
		
		if (cmd.getName().equalsIgnoreCase("send")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 3) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null || args[0].equalsIgnoreCase("all")) {
						
						String message = "";
						for (int i = 2; i < args.length; i++) {
							message = message + " " + args[i];
						}
						message = ChatColor.translateAlternateColorCodes('&', message.trim());
						
						List<Player> players = new ArrayList<Player>();
						if (args[0].equalsIgnoreCase("all")) {
							players.addAll(Bukkit.getOnlinePlayers());
						}
						else {
							players.add(target);
						}
						
						if (args[1].equalsIgnoreCase("chat")) {
							for (Player player : players) {
								player.sendMessage(message);
							}
						}
						else if (args[1].equalsIgnoreCase("title")) {
							for (Player player : players) {
								player.sendTitle(message, "", 10, 60, 10);
							}
						}
						else if (args[1].equalsIgnoreCase("subtitle")) {
							for (Player player : players) {
								player.sendTitle("", message, 10, 60, 10);
							}
						}
						else if (args[1].equalsIgnoreCase("actionbar")) {
							TextComponent text = new TextComponent(message);
							for (Player player : players) {
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
							}
						}
						else {
							sender.sendMessage(usage + "/send <pelaaja> <chat/title/subtitle/actionbar> <viesti>");
						}
					}
					else  {
						sender.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/send <pelaaja> <chat/title/subtitle/actionbar> <viesti>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// chat
		
		if (cmd.getName().equalsIgnoreCase("chat")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (core.getChatListener().isDisabled()) {
					core.getChatListener().setDisabled(false);
					sender.sendMessage(tc2 + "Chat on nyt käytettävissä!");
				}
				else {
					core.getChatListener().setDisabled(true);
					sender.sendMessage(tc2 + "Chat on nyt hiljennetty!");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// perm
		
		if (cmd.getName().equalsIgnoreCase("perm")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 3) {
					MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
					if (infoData != null) {
						String name = infoData.getString(0, "name");
						String uuid = infoData.getString(0, "uuid");
						if (args[2].equalsIgnoreCase("true")) {
							core.getConfig().set("permissions." + uuid + "." + args[1].replace(".", ";"), true);
							core.saveConfig();
							sender.sendMessage("");
							sender.sendMessage(tc2 + "Asetettiin permissionin '" + args[1] + "' arvoksi " + args[2].toUpperCase() + " pelaajalle " + name + "!");
							sender.sendMessage("");
							sender.sendMessage(tc2 + " Pelaajalla " + name + " on nyt seuraavat permissionit:");
							sender.sendMessage("");
							if (core.getConfig().getConfigurationSection("permissions." + uuid) != null) {
								for (String perm : core.getConfig().getConfigurationSection("permissions." + uuid).getKeys(false)) {
									sender.sendMessage(tc2 + " - " + perm.replace(";", ".") + " | " +  core.getConfig().getBoolean("permissions." + uuid + "." + perm));
								}
							}
							sender.sendMessage("");
							if (Bukkit.getPlayer(name) != null) {
								CoreUtils.updatePermissions(Bukkit.getPlayer(name));
							}
						}
						else if (args[2].equalsIgnoreCase("false")) {
							core.getConfig().set("permissions." + uuid + "." + args[1].replace(".", ";"), false);
							core.saveConfig();
							sender.sendMessage("");
							sender.sendMessage(tc2 + "Asetettiin permissionin '" + args[1] + "' arvoksi " + args[2].toUpperCase() + " pelaajalle " + name + "!");
							sender.sendMessage("");
							sender.sendMessage(tc2 + " Pelaajalla " + name + " on nyt seuraavat permissionit:");
							sender.sendMessage("");
							if (core.getConfig().getConfigurationSection("permissions." + uuid) != null) {
								for (String perm : core.getConfig().getConfigurationSection("permissions." + uuid).getKeys(false)) {
									sender.sendMessage(tc2 + " - " + perm.replace(";", ".") + " | " +  core.getConfig().getBoolean("permissions." + uuid + "." + perm));
								}
							}
							sender.sendMessage("");
							if (Bukkit.getPlayer(name) != null) {
								CoreUtils.updatePermissions(Bukkit.getPlayer(name));
							}
						}
						else if (args[2].equalsIgnoreCase("reset")) {
							core.getConfig().set("permissions." + uuid + "." + args[1].replace(".", ";"), null);
							core.saveConfig();
							sender.sendMessage("");
							sender.sendMessage(tc2 + "Asetettiin permissionin '" + args[1] + "' arvoksi " + args[2].toUpperCase() + " pelaajalle " + name + "!");
							sender.sendMessage("");
							sender.sendMessage(tc2 + " Pelaajalla " + name + " on nyt seuraavat permissionit:");
							sender.sendMessage("");
							if (core.getConfig().getConfigurationSection("permissions." + uuid) != null) {
								for (String perm : core.getConfig().getConfigurationSection("permissions." + uuid).getKeys(false)) {
									sender.sendMessage(tc2 + " - " + perm.replace(";", ".") + " | " +  core.getConfig().getBoolean("permissions." + uuid + "." + perm));
								}
							}
							sender.sendMessage("");
							if (Bukkit.getPlayer(name) != null) {
								CoreUtils.updatePermissions(Bukkit.getPlayer(name));
							}
						}
					}
					else {
						sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
					}
				}
				else {
					sender.sendMessage(usage + "/perm <pelaaja> <permission> <true/false/reset>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// group
		
		if (cmd.getName().equalsIgnoreCase("group")) {
			if (CoreUtils.hasAdminPowers(sender)) {
				if (args.length >= 4) {
					new BukkitRunnable() {
						public void run() {
							MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
							if (infoData != null) {
								String name = infoData.getString(0, "name");
								String prefix = args[1].replace("<=", "«").replace("=>", "»");
								if (prefix.equals("ritari")) {
									prefix = "&7[«&2Ritari&7»]";
								}
								else if (prefix.equals("aatelinen")) {
									prefix = "&7[«&6Aatelinen&7»]";
								}
								else if (prefix.equals("arkkitehti")) {
									prefix = "&7[«&eArkkitehti&7»]";
								}
								else if (prefix.equals("valvoja")) {
									prefix = "&7[«&cValvoja&7»]";
								}
								else if (prefix.equals("moderaattori")) {
									prefix = "&7[«&cModeraattori&7»]";
								}
								else if (prefix.equals("ylläpitäjä")) {
									prefix = "&7[«&4Ylläpitäjä&7»]";
								}
								else if (prefix.equals("pääarkkitehti")) {
									prefix = "&7[«&4Pääarkkitehti&7»]";
								}
								else if (prefix.equals("pääsuunnittelija")) {
									prefix = "&7[«&4Pääsuunnittelija&7»]";
								}
								else if (prefix.equals("pääkehittäjä")) {
									prefix = "&7[«&4Pääkehittäjä&7»]";
								}
								prefix = prefix + " ";
								if (prefix.equals("default ")) {
									prefix = "";
								}
								MySQLUtils.set("UPDATE player_info SET chat_prefix=?, chat_color=?, rank=? WHERE name=?", prefix, args[2], 
										args[3], name);
								MySQLResult newInfoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", name);
								Player player = Bukkit.getPlayer(args[0]);
								if (player != null) {
									core.getConfig().set("users." + player.getName() + ".chat_prefix", newInfoData.getStringNotNull(0, "chat_prefix"));
									core.getConfig().set("users." + player.getName() + ".chat_color", newInfoData.getStringNotNull(0, "chat_color"));
									core.getConfig().set("users." + player.getName() + ".rank", newInfoData.getString(0, "rank"));
									core.saveConfig();
									CoreUtils.updateTabForAll();
									player.updateCommands();
								}
								String newPrefix = ChatColor.translateAlternateColorCodes('&', newInfoData.getStringNotNull(0, "chat_prefix"));
								String newColor = ChatColor.translateAlternateColorCodes('&', newInfoData.getStringNotNull(0, "chat_color"));
								String newRank = ChatColor.translateAlternateColorCodes('&', newInfoData.getString(0, "rank"));
								sender.sendMessage(tc2 + "Pelaaja " + tc1 + name + tc2 + " on nyt: §r" + newPrefix + newColor + name + tc2 
										+ " (" + newRank + ")");
							}
							else {
								sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/group <pelaaja> <etuliite> <väri> <arvo>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// history
		
		if (cmd.getName().equalsIgnoreCase("history")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							MySQLResult historyData = MySQLUtils.get("SELECT * FROM player_history WHERE name=?", args[0]);
							if (historyData != null) {
								String name = historyData.getString(0, "name");
								sender.sendMessage("");
								sender.sendMessage(tc2 + "§m----------" + tc1 + " Rangaistukset: " + name + " " + tc2 + "§m----------");
								sender.sendMessage("");
								for (int i = 0; i < historyData.getRows(); i++) {
									SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");
									String type = historyData.getString(i, "type");
									String giver = historyData.getString(i, "giver");
									String reason = historyData.getString(i, "reason");
									long time = historyData.getLong(i, "time");
									long duration = historyData.getLong(i, "duration");
									long offset = CoreUtils.TIME_OFFSET;
									if (reason == null && duration == 0) {
										sender.sendMessage(tc1 + " - " + tc2 + "Tyyppi: " + tc1 + type + tc2 + ", antaja: " + tc1 + giver 
												+ tc2 + ", annettu: " + tc1 + f.format(new Date(time + offset)));
									}
									else if (reason == null) {
										sender.sendMessage(tc1 + " - " + tc2 + "Tyyppi: " + tc1 + type + tc2 + ", antaja: " + tc1 + giver 
												+ tc2 + ", annettu: " + tc1 + f.format(new Date(time + offset)) + tc2 + ", päättyy: " + tc1 
												+ f.format(new Date(duration + offset)));
									}
									else if (duration == 0) {
										sender.sendMessage(tc1 + " - " + tc2 + "Tyyppi: " + tc1 + type + tc2 + ", antaja: " + tc1 + giver 
												+ tc2 + ", annettu: " + tc1 + f.format(new Date(time + offset)) + tc2 + ", syy: " + tc1 
												+ reason);
									}
									else {
										sender.sendMessage(tc1 + " - " + tc2 + "Tyyppi: " + tc1 + type + tc2 + ", antaja: " + tc1 + giver 
												+ tc2 + ", annettu: " + tc1 + f.format(new Date(time + offset)) + tc2 + ", päättyy: " + tc1 
												+ f.format(new Date(duration + offset)) + tc2 + ", syy: " + tc1 + reason);
									}
								}
								sender.sendMessage("");
							}
							else {
								sender.sendMessage(tc3 + "Ei löydetty rangaistushistoriaa antamallasi nimellä!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					sender.sendMessage(usage + "/history <pelaaja>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// halttime
		
		if (cmd.getName().equalsIgnoreCase("halttime")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (core.isTimeHalted()) {
					sender.sendMessage(tc2 + "Aika liikkuu taas!");
				}
				else {
					sender.sendMessage(tc2 + "Pelinsisäinen aika pysäytetty!");
				}
				core.setTimeHalted(!core.isTimeHalted());
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// give
		
		if (cmd.getName().equals("give")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 2) {
					
					List<Player> receivers = new ArrayList<Player>();
					
					if (args[0].equalsIgnoreCase("all")) {
						receivers.addAll(Bukkit.getOnlinePlayers());
					}
					else if (args[0].equalsIgnoreCase("world")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							for (Player p : player.getWorld().getPlayers()) {
								receivers.add(p);
							}
						}
						else {
							sender.sendMessage(tc3 + "Vaihtoehtoa \"world\" ei voi käyttää konsolista käsin!");
							return true;
						}
					}
					else {
						if (Bukkit.getPlayer(args[0]) != null) {
							receivers.add(Bukkit.getPlayer(args[0]));
						}
						else if (args[0].endsWith("m")) {
							try {
								int i = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
								if (sender instanceof Player) {
									Player player = (Player) sender;
									for (Player p : player.getWorld().getPlayers()) {
										if (player.getLocation().distance(p.getLocation()) <= i) {
											receivers.add(p);
										}
									}
								}
								else {
									sender.sendMessage(tc3 + "Vaihtoehtoa \"säde\" ei voi käyttää konsolista käsin!");
									return true;
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								return true;
							}
						}
						else {
							sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							return true;
						}
					}
					
					ItemStack item = new ItemStack(Material.APPLE);
					int amount = 1;
					
					Material materialByName = Material.getMaterial(args[1].toUpperCase());
					if (materialByName != null) {
						item.setType(materialByName);
					}
					else {
						sender.sendMessage(tc3 + "Tuntematon esine!");
						return true;
					}
					
					if (args.length >= 3) {
						try {
							int i = Integer.parseInt(args[2]);
							item.setAmount(i);
							amount = i;
						}
						catch (NumberFormatException e) {
							sender.sendMessage(tc3 + "Virheellinen määrä!");
							return true;
						}
					}
					
					if (args.length >= 4) {
						for (int i = 3; i < args.length; i++) {
							
							String property = "";
							String value = "";
							
							if (args[i].contains(":")) {
								property = args[i].split(":")[0];
								value = ChatColor.translateAlternateColorCodes('&', args[i].substring(property.length() + 1).replace("\\_", " "));
							}
							else {
								sender.sendMessage(tc3 + "Virheellinen ominaisuus: \"" + args[i] + "\"");
								return true;
							}
							
							if (property.equalsIgnoreCase("damage")) {
								try {
									int damage = Integer.parseInt(value);
									ItemMeta meta = item.getItemMeta();
									Damageable damageable = (Damageable) meta;
									damageable.setDamage(damage);
									item.setItemMeta(meta);
								}
								catch (NumberFormatException e) {
									sender.sendMessage(tc3 + "Virheellinen ominaisuus: \"" + args[i] + "\"");
									return true;
								}
							}
							else if (property.equalsIgnoreCase("mapid")) {
								try {
									int mapId = Integer.parseInt(value);
									if (item.getItemMeta() instanceof MapMeta) {
										MapMeta mapMeta = (MapMeta) item.getItemMeta();
										mapMeta.setMapId(mapId);
										item.setItemMeta(mapMeta);
									}
									else {
										sender.sendMessage(tc3 + "Tälle esineelle ei voi asettaa kartan ID:tä!");
										return true;
									}
								}
								catch (NumberFormatException e) {
									sender.sendMessage(tc3 + "Virheellinen ominaisuus: \"" + args[i] + "\"");
									return true;
								}
							}
							else if (property.equalsIgnoreCase("name")) {
								ItemMeta meta = item.getItemMeta();
								meta.setDisplayName(value);
								item.setItemMeta(meta);
							}
							else if (property.equalsIgnoreCase("lore")) {
								List<String> lore = new ArrayList<String>();
								for (String line : value.split("\\\\n")) {
									lore.add(line);
								}
								ItemMeta meta = item.getItemMeta();
								meta.setLore(lore);
								item.setItemMeta(meta);
							}
							else if (property.equalsIgnoreCase("unbreakable")) {
								ItemMeta meta = item.getItemMeta();
								meta.setUnbreakable(true);
								item.setItemMeta(meta);
							}
							else if (property.equalsIgnoreCase("hide")) {
								ItemMeta meta = item.getItemMeta();
								meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
								item.setItemMeta(meta);
							}
							else {
								try {
									int level = Integer.parseInt(value);
									Enchantment enchantment = Enchantment.getByName(property.toUpperCase());
									if (enchantment != null) {
										ItemMeta meta = item.getItemMeta();
										meta.addEnchant(enchantment, level, true);
										item.setItemMeta(meta);
									}
									else {
										sender.sendMessage(tc3 + "Virheellinen ominaisuus: \"" + args[i] + "\"");
										return true;
									}
								}
								catch (NumberFormatException e) {
									sender.sendMessage(tc3 + "Virheellinen ominaisuus: \"" + args[i] + "\"");
									return true;
								}
							}
						}
					}
					
					if (!receivers.isEmpty()) {
						for (Player receiver : receivers) {
							receiver.getInventory().addItem(item);
						}
						if (receivers.size() == 1) {
							String itemName = LanguageHelper.getItemDisplayName(item, "fi_FI");
							sender.sendMessage(tc2 + "Annettiin pelaajalle " + tc1 + receivers.get(0).getName() + tc2 + " " + tc1 
									+ amount + " kappaletta" + tc2 + " esinettä " + tc1 + itemName + tc2 + "!");
						}
						else {
							String itemName = LanguageHelper.getItemDisplayName(item, "fi_FI");
							sender.sendMessage(tc2 + "Annettiin yhteensä " + tc1 + receivers.size() + tc2 + " pelaajalle " + tc1 
									+ amount + " kappaletta" + tc2 + " esinettä " + tc1 + itemName + tc2 + "!");
						}
					}
					else {
						sender.sendMessage(tc3 + "Ei kriteerejä vastaavia pelaajia!");
						return true;
					}
				}
				else {
					sender.sendMessage(usage + "/give <pelaaja/all/world/säde> <esine> [määrä] [ominaisuudet]");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// effect
		
		if (cmd.getName().equals("effect")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 2) {
					
					List<Player> receivers = new ArrayList<Player>();
					boolean clear = false;
					
					if (args[0].equalsIgnoreCase("all")) {
						receivers.addAll(Bukkit.getOnlinePlayers());
					}
					else if (args[0].equalsIgnoreCase("world")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							for (Player p : player.getWorld().getPlayers()) {
								receivers.add(p);
							}
						}
						else {
							sender.sendMessage(tc3 + "Vaihtoehtoa \"world\" ei voi käyttää konsolista käsin!");
							return true;
						}
					}
					else {
						if (Bukkit.getPlayer(args[0]) != null) {
							receivers.add(Bukkit.getPlayer(args[0]));
						}
						else if (args[0].endsWith("m")) {
							try {
								int i = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
								if (sender instanceof Player) {
									Player player = (Player) sender;
									for (Player p : player.getWorld().getPlayers()) {
										if (player.getLocation().distance(p.getLocation()) <= i) {
											receivers.add(p);
										}
									}
								}
								else {
									sender.sendMessage(tc3 + "Vaihtoehtoa \"säde\" ei voi käyttää konsolista käsin!");
									return true;
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								return true;
							}
						}
						else {
							sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							return true;
						}
					}
					
					PotionEffectType type = PotionEffectType.SPEED;
					int level = 1;
					int duration = 1;
					
					if (args[1].equalsIgnoreCase("clear")) {
						clear = true;
					}
					else {
						if (args.length >= 4) {
							
							type = PotionEffectType.getByName(args[1].toUpperCase());
							if (type == null) {
								sender.sendMessage(tc3 + "Tuntematon efekti!");
								return true;
							}
							
							try {
								level = Integer.parseInt(args[2]);
								duration = Integer.parseInt(args[3]);
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Virheellinen taso tai kesto!");
								return true;
							}
						}
						else {
							sender.sendMessage(usage + "/effect <pelaaja/all/world/säde> <efekti> <taso> <kesto>");
							return true;
						}
					}
					
					PotionEffect effect = new PotionEffect(type, duration * 20, level - 1);
					
					if (!receivers.isEmpty()) {
						for (Player receiver : receivers) {
							if (clear) {
								for (PotionEffect activeEffect : receiver.getActivePotionEffects()) {
									receiver.removePotionEffect(activeEffect.getType());
								}
							}
							else {
								receiver.addPotionEffect(effect);
							}
						}
						if (receivers.size() == 1) {
							if (clear) {
								sender.sendMessage(tc2 + "Poistettiin kaikki efektit pelaajalta " + tc1 + receivers.get(0).getName() + tc2 + "!");
							}
							else {
								sender.sendMessage(tc2 + "Annettiin pelaajalle " + tc1 + receivers.get(0).getName() + tc2 + " efekti " + tc1 + 
										type.getName() + " " + level + tc2 + " ajaksi " + tc1 + duration + " sekuntia" + tc2 + "!");
							}
						}
						else {
							if (clear) {
								sender.sendMessage(tc2 + "Poistettiin kaikki efektit yhteensä " + tc1 + receivers.size() + tc2 + " pelaajalta!");
							}
							else {
								sender.sendMessage(tc2 + "Annettiin yhteensä " + tc1 + receivers.size() + tc2 + " pelaajalle efekti " + tc1 + 
										type.getName() + " " + level + tc2 + " ajaksi " + tc1 + duration + " sekuntia" + tc2 + "!");
							}
						}
					}
					else {
						sender.sendMessage(tc3 + "Ei kriteerejä vastaavia pelaajia!");
						return true;
					}
				}
				else {
					sender.sendMessage(usage + "/effect <pelaaja/all/world/säde> <efekti> <taso> <kesto>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// sudo
		
		if (cmd.getName().equalsIgnoreCase("sudo")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 2) {
					Player player = Bukkit.getPlayer(args[0]);
					if (player != null) {
						String command = "";
						for (int i = 1; i < args.length; i++) {
							command = command + " " + args[i];
						}
						command = command.trim();
						sender.sendMessage(tc2 + "Pakotettiin pelaaja " + tc1 + player.getName() + tc2 + " suorittamaan komento " + tc1 
								+ "/" + command + tc2 + "!");
						player.performCommand(command);
					}
					else {
						sender.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/sudo <pelaaja> <komento>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// sudochat
		
		if (cmd.getName().equalsIgnoreCase("sudochat")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 2) {
					Player player = Bukkit.getPlayer(args[0]);
					if (player != null) {
						String message = "";
						for (int i = 1; i < args.length; i++) {
							message = message + " " + args[i];
						}
						message = message.trim();
						sender.sendMessage(tc2 + "Pakotettiin pelaaja " + tc1 + player.getName() + tc2 + " kirjoittamaan viesti \"" + tc1 
								+ message + tc2 + "\"!");
						player.chat(message);
					}
					else {
						sender.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					sender.sendMessage(usage + "/sudochat <pelaaja> <viesti>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// setnews
		
		if (cmd.getName().equalsIgnoreCase("setnews")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("clear")) {
						core.getConfig().set("motd", null);
						core.saveConfig();
						sender.sendMessage(tc2 + "Poistettiin Mitä uutta? -teksti!");
						return true;
					}
					String json = "";
					for (String word : args) {
						json = json + " " + word;
					}
					json = json.trim();
					core.getConfig().set("motd.motd", json);
					core.getConfig().set("motd.seen", null);
					core.saveConfig();
					sender.sendMessage(tc2 + "Asetettiin uusi Mitä uutta? -teksti!");
				}
				else {
					sender.sendMessage(usage + "/setnews <json/clear>");
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// vanhentuneetsakot
		
		if (cmd.getName().equalsIgnoreCase("vanhentuneetsakot")) {
			if (CoreUtils.hasRank(sender, "valvoja")) {
				new BukkitRunnable() {
					public void run() {
						boolean console = false;
						if (args.length >= 1 && args[0].equalsIgnoreCase("console")) {
							console = true;
						}
						if (!console) {
							sender.sendMessage("");
							sender.sendMessage(tc2 + "§m----------" + tc1 + " Vanhentuneet sakot " + tc2 + "§m----------");
							sender.sendMessage("");
						}
						List<String> expiredFines = new ArrayList<String>();
						MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines");
						if (finesData != null) {
							for (int i = 0; i < finesData.getRows(); i++) {
								
								int id = finesData.getInt(i, "id");
								int amount = finesData.getInt(i, "amount");
								String name = finesData.getString(i, "name");
								String uuidWithoutDashes = finesData.getString(i, "uuid").replace("-", "");
								String reason = finesData.getString(i, "reason");
								long duration = finesData.getLong(i, "duration");
								
								if (duration - System.currentTimeMillis() < 0) {
									MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes);
									MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuidWithoutDashes);
									if (banData != null || jailData != null) {
										expiredFines.add(tc1 + " - #" + id + ", " + name + ", " + amount + "£ " + tc2 + "§m" + reason);
									}
									else {
										expiredFines.add(tc1 + " - #" + id + ", " + name + ", " + amount + "£ " + tc2 + reason);
									}
								}
							}
						}
						if (!expiredFines.isEmpty()) {
							for (String fine : expiredFines) {
								if (!console) {
									sender.sendMessage(fine);
								}
							}
							if (console) {
								TextComponent text = new TextComponent("\nHavaittiin " + expiredFines.size() + " erääntynyttä sakkomaksua! "
										+ "Tarkista klikkaamalla tästä!\n");
								text.setColor(ChatColor.GOLD);
								text.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/vanhentuneetsakot"));
								for (Player player : Bukkit.getOnlinePlayers()) {
									player.spigot().sendMessage(text);
									player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
								}
							}
						}
						else {
							if (!console) {
								sender.sendMessage(tc3 + " Ei vanhentuneita sakkomaksuja!");
							}
						}
						if (!console) {
							sender.sendMessage("");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// clearfire
		
		if (cmd.getName().equalsIgnoreCase("clearfire")) {
			if (CoreUtils.hasRank(sender, "ylläpitäjä")) {
				core.getCoreListener().getFireSpreadCooldown().clear();
				sender.sendMessage(tc2 + "Komento suoritettiin onnistuneesti!");
			}
			return true;
		}
		
		// komennon suorittaja
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(tc3 + playersOnly);
			return true;
		}
		Player player = (Player) sender;
		
		// debug
		
		if (cmd.getName().equalsIgnoreCase("debug")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (debugPlayers.contains(player.getName())) {
					debugPlayers.remove(player.getName());
					player.sendMessage(tc2 + "Debug-tila pois päältä!");
				}
				else {
					debugPlayers.add(player.getName());
					player.sendMessage(tc2 + "Debug-tila päällä!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// ping, lag, tps
		
		if (cmd.getName().equalsIgnoreCase("ping") || cmd.getName().equalsIgnoreCase("lag")) {
			int ping = ReflectionUtils.getPing(player);
			String s = ping + "ms";
			double tps = BigDecimal.valueOf(ReflectionUtils.getTPS()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
			String pColor = "§2";
			String tColor = "§2";
			if (ping > 10000 || ping < 1) {
				pColor = "§f";
				s = "Tuntematon...";
			}
			else if (ping > 175) {
				pColor = "§4";
			}
			else if (ping > 125) {
				pColor = "§c";
			}
			else if (ping > 75) {
				pColor = "§e";
			}
			else if (ping > 25) {
				pColor = "§a";
			}
			if (tps > 20) {
				tps = 20;
			}
			if (tps < 14) {
				tColor = "§4";
			}
			else if (tps < 16) {
				tColor = "§c";
			}
			else if (tps < 18) {
				tColor = "§e";
			}
			else if (tps < 19.5) {
				tColor = "§a";
			}
			player.sendMessage("");
			player.sendMessage(tc2 + " Viiveesi palvelimelle: " + pColor + s);
			player.sendMessage(tc2 + " Palvelimen TPS: " + tColor + tps);
			player.sendMessage("");
			return true;
		}
		
		// etsi, find, search
		
		if (cmd.getName().equalsIgnoreCase("etsi") || cmd.getName().equalsIgnoreCase("find") || cmd.getName().equalsIgnoreCase("search")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					if (args[0].length() < 2 || args[0].length() > 16) {
						player.sendMessage(tc3 + "Hakusanan pituuden tulee olla 2-16 merkkiä!");
						return true;
					}
					player.sendMessage(tc2 + "Haetaan pelaajia hakusanalla " + tc1 + args[0] + tc2 + ", odota...");
					new BukkitRunnable() {
						public void run() {
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Hakutulokset " + tc2 + "§m----------");
							player.sendMessage("");
							MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name LIKE ?", "%" + args[0] + "%");
							if (infoData != null) {
								player.sendMessage(tc2 + " Löydettiin " + tc1 + infoData.getRows() + tc2 + " osuma(a):");
								player.sendMessage("");
								int counter = 0;
								for (int i = 0; i < infoData.getRows(); i++) {
									String name = infoData.getString(i, "name");
									TextComponent t1 = new TextComponent(tc1 + " - ");
									TextComponent t2 = new TextComponent(tc2 + name);
									t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pelaaja " + name));
									t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc2 + "Näytä pelaajan profiili klikkaamalla!").create()));
									t1.addExtra(t2);
									player.spigot().sendMessage(t1);
									counter++;
									if (counter >= 20 && infoData.getRows() > 20) {
										player.sendMessage(tc2 + "    ...");
										break;
									}
								}
							}
							else {
								player.sendMessage(tc3 + " Ei löydetty yhtäkään pelaajaa antamallasi nimellä...");
							}
							player.sendMessage("");
						}
					}.runTaskAsynchronously(core);
				}
				else {
					player.sendMessage(usage + "/etsi <nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// pelaaja, profiili, player, profile, p
		
		if (cmd.getName().equalsIgnoreCase("pelaaja") || cmd.getName().equalsIgnoreCase("profiili") || cmd.getName().equalsIgnoreCase("player") || 
				cmd.getName().equalsIgnoreCase("profile") || cmd.getName().equalsIgnoreCase("p")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
						MySQLResult statsData = MySQLUtils.get("SELECT * FROM player_stats WHERE name=?", args[0]);
						if (infoData != null && statsData != null) {
							
							String id = infoData.getString(0, "id");
							String name = infoData.getString(0, "name");
							String uuid = infoData.getString(0, "uuid");
							String ip = infoData.getString(0, "ip");
							String rank = infoData.getString(0, "rank");
							String status = statsData.getStringNotNull(0, "status");
							long seconds = infoData.getLong(0, "seconds");
							long lastSeen = infoData.getLong(0, "last_seen");
							if (core.getOntimes().containsKey(uuid)) {
								seconds += core.getOntimes().get(uuid);
							}
							String timePlayed = CoreUtils.getHoursAndMinsFromMillis(seconds * 1000);
							SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");
							String lastTimeOnline = f.format(new Date(lastSeen + CoreUtils.TIME_OFFSET));
							String[] wrappedStatus = ChatPaginator.wordWrap(status, 30);
							
							InventoryGUI gui = new InventoryGUI(54, "Profiili: " + name);
							
							List<String> lore = new ArrayList<String>();
							lore.add("");
							lore.add("§aArvo: §7" + CoreUtils.firstUpperCase(rank).replace("Default", "Pelaaja"));
							lore.add("§aPelannut: §7" + timePlayed);
							if (Bukkit.getPlayerExact(name) != null) {
								lore.add("§aViimeksi nähty: §7Nyt");
							}
							else {
								lore.add("§aViimeksi nähty: §7" + lastTimeOnline);
							}
							lore.add("§aTilaviesti:");
							lore.add("");
							for (int i = 0; i < wrappedStatus.length; i++) {
								lore.add("§7§o" + ChatColor.stripColor(wrappedStatus[i]));
							}
							
							gui.addItem(CoreUtils.getSkull("§a" + name, lore, name), 13, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() { }
							});
							
							List<String> comingSoon = Arrays.asList("", "§7Tulossa pian...");
							
							gui.addItem(CoreUtils.getItem(Material.NETHER_STAR, "§aAmmatti", comingSoon, 1), 29, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() { }
							});
							
							gui.addItem(CoreUtils.getItem(Material.DIAMOND, "§aSaavutukset", comingSoon, 1), 31, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() { }
							});
							
							gui.addItem(CoreUtils.getItem(Material.TOTEM_OF_UNDYING, "§aTilastot", comingSoon, 1), 33, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() { }
							});
							
							gui.addItem(CoreUtils.getItem(Material.BARRIER, "§cSulje valikko", null, 1), 49, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(player);
									player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
								}
							});
							
							if (CoreUtils.hasRank(player, "ylläpitäjä")) {
								List<String> adminLore = new ArrayList<String>();
								adminLore.add("");
								adminLore.add("§aID: §7" + id);
								adminLore.add("§aUUID: §7" + uuid);
								adminLore.add("§aIP: §7" + ip);
								adminLore.add("");
								adminLore.add("§7 » Näytä huomautukset klikkaamalla!");
								gui.addItem(CoreUtils.getItem(Material.PAPER, "§aYlläpitotiedot", adminLore, 1), 11, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() {
										gui.close(player);
										player.performCommand("huomautus " + name);
									}
								});
							}
							else if (CoreUtils.hasRank(player, "valvoja")) {
								List<String> adminLore = new ArrayList<String>();
								adminLore.add("");
								adminLore.add("§7 » Näytä huomautukset klikkaamalla!");
								gui.addItem(CoreUtils.getItem(Material.PAPER, "§aHuomautukset", adminLore, 1), 11, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() {
										gui.close(player);
										player.performCommand("huomautus " + name);
									}
								});
							}
							
							if (CoreUtils.hasRank(player, "valvoja")) {
								List<String> moderatorLore = new ArrayList<String>();
								moderatorLore.add("");
								moderatorLore.add("§7 » Näytä tiedot klikkaamalla!");
								gui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§aRangaistustiedot", moderatorLore, 1), 15, 
										new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() {
										gui.close(player);
										player.performCommand("rankaise " + name);
									}
								});
							}
							
							gui.open(player);
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(usage + "/pelaaja <pelaaja>");
			}
			return true;
		}
		
		// help, apua, ?
		
		if (cmd.getName().equalsIgnoreCase("help") || cmd.getName().equalsIgnoreCase("apua") || cmd.getName().equalsIgnoreCase("?")) {
			player.sendMessage("");
			player.sendMessage(tc2 + "§m----------" + tc1 + " Apua " + tc2 + "§m----------");
			player.sendMessage("");
			player.sendMessage(tc2 + " Tulossa..."); // TODO
			player.sendMessage("");
			return true;
		}
		
		// säännöt, rules
		
		if (cmd.getName().equalsIgnoreCase("säännöt") || cmd.getName().equalsIgnoreCase("rules")) {
			player.sendMessage("");
			player.sendMessage(tc2 + "§m----------" + tc1 + " Säännöt " + tc2 + "§m----------");
			player.sendMessage("");
			player.sendMessage(tc2 + " Pelaamalla palvelimellamme hyväksyt säännöt:");
			player.sendMessage(tc1 + " http://esimerkki.fi/tietoa/yleiset/säännöt"); // TODO
			player.sendMessage("");
			return true;
		}
		
		// s
		
		if (cmd.getName().equalsIgnoreCase("s")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					String message = "";
					for (String word : args) {
						message = message + " " + word;
					}
					message = ChatColor.translateAlternateColorCodes('&', message.trim());
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (CoreUtils.hasRank(p, "valvoja")) {
							p.sendMessage("§7[§aHenkilökunta§7] " + player.getName() + "§a: " + message);
						}
					}
				}
				else {
					player.sendMessage(usage + "/s <viesti>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// sa
		
		if (cmd.getName().equalsIgnoreCase("sa")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					String message = "";
					for (String word : args) {
						message = message + " " + word;
					}
					message = ChatColor.translateAlternateColorCodes('&', message.trim());
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (CoreUtils.hasRank(p, "valvoja")) {
							p.sendMessage("§7[§eYlläpito§7] " + player.getName() + "§e: " + message);
						}
					}
				}
				else {
					player.sendMessage(usage + "/sa <viesti>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// a, helpop
		
		if (cmd.getName().equalsIgnoreCase("a") || cmd.getName().equalsIgnoreCase("helpop")) {
			if (core.getConfig().getBoolean("users." + player.getName() + ".mute.muted")) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää tätä komentoa, sillä sinut on hiljennetty!");
				return true;
			}
			if (args.length >= 1) {
				String message = "";
				for (String word : args) {
					message = message + " " + word;
				}
				message = message.trim();
				boolean staffOnline = false;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (CoreUtils.hasRank(p, "valvoja")) {
						p.sendMessage("§7[§cApua§7] " + player.getName() + "§c: " + message);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 10, 0.1f);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 2);
						if (player.canSee(p)) {
							staffOnline = true;
						}
					}
				}
				if (staffOnline) {
					player.sendMessage("§2§lSinä -> Henkilökunta §a" + message);
				}
				else {
					player.sendMessage(tc3 + "Valitettavasti ketään henkilökunnasta ei ole paikalla tällä hetkellä!"); // TODO
				}
			}
			else {
				player.sendMessage(usage + "/a <viesti>");
			}
			return true;
		}
		
		// henkilökunta, staff
		
		if (cmd.getName().equalsIgnoreCase("henkilökunta") || cmd.getName().equalsIgnoreCase("staff")) {
			player.sendMessage("");
			player.sendMessage(tc2 + "§m----------" + tc1 + " Henkilökunta " + tc2 + "§m----------");
			player.sendMessage("");
			player.sendMessage("§4 Ylläpitäjät:");
			if (Bukkit.getPlayer("T4TU_") != null && (!vanishedPlayers.contains("T4TU_") || CoreUtils.hasRank(player, "valvoja"))) {
				player.sendMessage(tc2 + "  - T4TU_ §a(paikalla)");
			}
			else {
				player.sendMessage(tc2 + "  - T4TU_");
			}
			if (Bukkit.getPlayer("evokki0075") != null && (!vanishedPlayers.contains("evokki0075") || CoreUtils.hasRank(player, "valvoja"))) {
				player.sendMessage(tc2 + "  - evokki0075 §a(paikalla)");
			}
			else {
				player.sendMessage(tc2 + "  - evokki0075");
			}
			if (Bukkit.getPlayer("Ahishi") != null && (!vanishedPlayers.contains("Ahishi") || CoreUtils.hasRank(player, "valvoja"))) {
				player.sendMessage(tc2 + "  - Ahishi §a(paikalla)");
			}
			else {
				player.sendMessage(tc2 + "  - Ahishi");
			}
			player.sendMessage("");
			return true;
		}
		
		// viesti, msg, tell, w, t, m
		
		if (cmd.getName().equalsIgnoreCase("viesti") ||cmd.getName().equalsIgnoreCase("msg") || cmd.getName().equalsIgnoreCase("tell") 
				|| cmd.getName().equalsIgnoreCase("w") || cmd.getName().equalsIgnoreCase("t") || cmd.getName().equalsIgnoreCase("m")) {
			if (core.getConfig().getBoolean("users." + player.getName() + ".mute.muted")) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(CoreUtils.getErrorBaseColor() + "Et voi lähettää yksityisviestejä, sillä sinut on hiljennetty!");
				return true;
			}
			if (args.length >= 2) {
				Player target = Bukkit.getPlayer(args[0]);
				if (target != null) {
					if (SettingsUtils.getSetting(target, "show_msg")) {
						
						String message = "";
						for (int i = 1; i < args.length; i++) {
							message = message + " " + args[i];
						}
						message = message.trim();
						
						HoverEvent hoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder("Klikkaa tästä vastataksesi viestiin!").color(ChatColor.YELLOW).create());
						ClickEvent clickEvent = new ClickEvent(Action.SUGGEST_COMMAND, "/viesti " + player.getName() + " ");
						
						TextComponent baseComponent = new TextComponent("");
						BaseComponent[] senderComponents = new ComponentBuilder(player.getName()).color(ChatColor.YELLOW).bold(true)
								.event(hoverEvent).event(clickEvent).create();
						BaseComponent[] arrowComponents = new ComponentBuilder(" ▶ ").color(ChatColor.GOLD).bold(true).create();
						BaseComponent[] receiverComponents = new ComponentBuilder("Sinä ").color(ChatColor.YELLOW).bold(true).create();
						BaseComponent[] messageComponents = TextComponent.fromLegacyText("§7" + message);
						
						for (BaseComponent component : senderComponents) {
							baseComponent.addExtra(component);
						}
						for (BaseComponent component : arrowComponents) {
							baseComponent.addExtra(component);
						}
						for (BaseComponent component : receiverComponents) {
							baseComponent.addExtra(component);
						}
						for (BaseComponent component : messageComponents) {
							baseComponent.addExtra(component);
						}
						
						if (SettingsUtils.getSetting(target, "play_sound_msg")) {
							target.playSound(target.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
						}
						
						target.spigot().sendMessage(baseComponent);
						player.sendMessage("§e§lSinä§6§l ▶ §e§l" + target.getName() + " §7" + message);
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (CoreUtils.hasRank(p, "valvoja") && spyPlayers.contains(player.getName())) {
								player.sendMessage("§8§l" + player.getName() + "§7§l ▶ §8§l" + target.getName() + " §7" + message);
							}
						}
						
						core.getConfig().set("users." + player.getName() + ".lastMessage", target.getName());
						core.getConfig().set("users." + target.getName() + ".lastMessage", player.getName());
						core.saveConfig();
					}
					else {
						player.sendMessage(tc3 + target.getName() + " on poistanut yksityisviestit käytöstä!");
					}
				}
				else {
					player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
				}
			}
			else {
				player.sendMessage(usage + "/viesti <pelaaja> <viesti>");
			}
			return true;
		}
		
		// vastaa, r
		
		if (cmd.getName().equalsIgnoreCase("vastaa") || cmd.getName().equalsIgnoreCase("r")) {
			if (args.length >= 1) {
				String to = core.getConfig().getString("users." + player.getName() + ".lastMessage");
				if (to != null) {
					
					String message = "";
					for (String word : args) {
						message = message + " " + word;
					}
					message = message.trim();
					
					player.performCommand("viesti " + to + " " + message);
				}
				else {
					player.sendMessage(tc3 + "Ei ketään, kenelle vastata!");
				}
			}
			else {
				player.sendMessage(usage + "/vastaa <viesti>");
			}
			return true;
		}
		
		// posti, mail
		
		if (cmd.getName().equalsIgnoreCase("posti") || cmd.getName().equalsIgnoreCase("mail")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						if (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list")) {
							MySQLResult mailData = MySQLUtils.get("SELECT * FROM player_mails WHERE receiver=?", player.getUniqueId().toString());
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Postilaatikko " + tc2 + "§m----------");
							player.sendMessage("");
							if (mailData != null) {
								for (int i = 0; i < mailData.getRows(); i++) {
									int id = mailData.getInt(i, "id");
									String sender = mailData.getString(i, "sender");
									String subject = mailData.getString(i, "subject");
									boolean read = mailData.getBoolean(i, "seen");
									String newMessage = "";
									if (!read) {
										newMessage = "§c§lUUSI ";
									}
									String from = CoreUtils.uuidToName(sender);
									TextComponent textComponent = new TextComponent(tc1 + " - " + newMessage + tc2 + subject + " (" + from + ")");
									textComponent.setColor(ChatColor.getByChar(tc2.charAt(1)));
									textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc2 + "Lue viesti klikkaamalla!").create()));
									textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/posti lue " + id));
									player.spigot().sendMessage(textComponent);
								}
							}
							else {
								player.sendMessage(tc3 + " Postilaatikossasi ei ole viestejä!");
							}
							player.sendMessage("");
							player.sendMessage(tc2 + " Lähetä uusi viesti: " + tc1 + "/posti lähetä");
							player.sendMessage("");
						}
						else if (args[0].equalsIgnoreCase("lue") || args[0].equalsIgnoreCase("read")) {
							if (args.length >= 2) {
								MySQLResult mailData = MySQLUtils.get("SELECT * FROM player_mails WHERE id=?", args[1]);
								if (mailData != null) {
									int id = mailData.getInt(0, "id");
									String sender = mailData.getString(0, "sender");
									String receiver = mailData.getString(0, "receiver");
									String subject = mailData.getString(0, "subject");
									String message = mailData.getString(0, "message");
									if (receiver.equalsIgnoreCase(player.getUniqueId().toString())) {
										MySQLUtils.set("UPDATE player_mails SET seen=true WHERE id=" + id);
										String from = CoreUtils.uuidToName(sender);
										player.sendMessage("");
										player.sendMessage(tc2 + "§m----------" + tc1 + " Postilaatikko " + tc2 + "§m----------");
										player.sendMessage("");
										player.sendMessage(tc1 + " Lähettäjä: " + tc2 + from);
										player.sendMessage(tc1 + " Aihe: " + tc2 + subject);
										player.sendMessage("");
										player.sendMessage(tc2 + " §o" + message);
										player.sendMessage("");
									}
									else {
										player.sendMessage(tc3 + "Ei löydetty viestiä antamallasi ID:llä!");
									}
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty viestiä antamallasi ID:llä!");
								}
							}
							else {
								player.sendMessage(usage + "/posti lue <id>");
							}
						}
						else if (args[0].equalsIgnoreCase("lähetä") || args[0].equalsIgnoreCase("send")) {
							if (core.getConfig().getBoolean("users." + player.getName() + ".mute.muted")) {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage(CoreUtils.getErrorBaseColor() + "Et voi lähettää postia, sillä sinut on hiljennetty!");
								return;
							}
							if (args.length >= 3) {
								String subject = "";
								for (int i = 2; i < args.length; i++) {
									subject = subject + " " + args[i];
								}
								subject = subject.trim();
								MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[1]);
								if (infoData != null) {
									String uuid = infoData.getString(0, "uuid");
									mailWritingPlayers.put(player.getName(), uuid + "§" + subject);
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
									player.sendMessage("");
									player.sendMessage(tc2 + "Asetettiin viestin aiheeksi: §o" + subject);
									player.sendMessage("");
									player.sendMessage(tc2 + "Kirjoita lähetettävä viesti chattiin:");
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								}
							}
							else {
								player.sendMessage(usage + "/posti lähetä <pelaaja> <viestin aihe>");
							}
						}
						else if (args[0].equalsIgnoreCase("tyhjennä") || args[0].equalsIgnoreCase("clear")) {
							if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
								MySQLUtils.set("DELETE FROM player_mails WHERE receiver=?", player.getUniqueId().toString());
								player.sendMessage(tc2 + "Poistettiin kaikki viestit!");
							}
							else {
								TextComponent textComponent = new TextComponent("Haluatko varmasti poistaa " + tc1 + "KAIKKI" + tc2 
										+ " viestisi? Varmista tämä klikkaamalla tätä tekstiä!");
								textComponent.setColor(ChatColor.getByChar(tc2.charAt(1)));
								textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
										new ComponentBuilder(tc2 + "Poista kaikki vastaanottamasi viestit klikkaamalla tästä!").create()));
								textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/posti tyhjennä confirm"));
								player.spigot().sendMessage(textComponent);
							}
						}
						else {
							player.sendMessage(usage + "/posti [lähetä/tyhjennä]");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.performCommand("posti lista");
			}
			return true;
		}
		
		// afk
		
		if (cmd.getName().equalsIgnoreCase("afk")) {
			if (CoreUtils.getAfkCounter().containsKey(player.getName())) {
				if (CoreUtils.getAfkCounter().get(player.getName()) == -1) {
					CoreUtils.setAfkCounter(player, 0);
				}
				else {
					CoreUtils.setAfkCounter(player, -1);
				}
			}
			else {
				CoreUtils.setAfkCounter(player, -1);
			}
			return true;
		}
		
		// uutiset, news, motd
		
		if (cmd.getName().equalsIgnoreCase("uutiset") || cmd.getName().equalsIgnoreCase("news") || cmd.getName().equalsIgnoreCase("motd")) {
			if (core.getConfig().getString("motd.motd") == null) {
				return true;
			}
			player.sendMessage("");
			player.sendMessage(tc1 + "✸ §m-------------" + tc2 + " Mitä uutta? " + tc1 + "§m-------------" + tc1 + " ✸");
			player.sendMessage("");
			ReflectionUtils.sendChatPacket(player, core.getConfig().getString("motd.motd"), ChatMessageType.CHAT);
			player.sendMessage("");
			player.sendMessage(tc1 + "✸ §m------------------------------------" + tc1 + " ✸");
			core.getConfig().set("motd.seen." + player.getName(), true);
			core.saveConfig();
			return true;
		}
		
		// tilaviesti, status
		
		if (cmd.getName().equalsIgnoreCase("tilaviesti") || cmd.getName().equalsIgnoreCase("status")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						String status = "";
						for (String word : args) {
							status = status + " " + word;
						}
						status = status.trim();
						MySQLUtils.set("UPDATE player_stats SET status=? WHERE name=?", status, player.getName());
						MySQLResult newStatusData = MySQLUtils.get("SELECT status FROM player_stats WHERE name=?", player.getName());
						core.getConfig().set("users." + player.getName() + ".status", newStatusData.getString(0, "status"));
						core.saveConfig();
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						player.sendMessage(tc2 + "Päivitettiin tilaviesti: §o" + newStatusData.getString(0, "status"));
						List<String> friends = CoreUtils.getFriendsUuids(player.getName());
						for (String friend : friends) {
							Player p = Bukkit.getPlayer(UUID.fromString(friend));
							if (p != null) {
								if (SettingsUtils.getSetting(p, "show_friend_status")) {
									p.sendMessage(tc1 + player.getName() + tc2 + " päivitti tilaviestinsä: §o" + 
											newStatusData.getString(0, "status"));
								}
							}
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(usage + "/tilaviesti <uusi tilaviesti>");
			}
			return true;
		}
		
		// kompassi
		
		if (cmd.getName().equalsIgnoreCase("kompassi")) {
			if (args.length >= 2) {
				try {
					int x = Integer.parseInt(args[0]);
					int z = Integer.parseInt(args[1]);
					player.setCompassTarget(new Location(player.getWorld(), x, 0, z));
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					player.sendMessage(tc2 + "Asetettiin kompassi osoittamaan koordinaatteihin " + 
							tc1 + "x: " + x + tc2 + ", " + tc1 + "z: " + z + tc2 + "!");
				}
				catch (Exception e) {
					player.sendMessage(tc3 + "Virheelliset koordinaatit!");
				}
			}
			else {
				player.sendMessage(usage + "/kompassi <x> <z>");
			}
			return true;
		}
		
		// lähellä, near TODO
		
		if (cmd.getName().equalsIgnoreCase("lähellä") || cmd.getName().equalsIgnoreCase("near")) {
			if (CoreUtils.hasRank(player, "ritari")) {
				boolean b = false;
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Pelaajat lähistöllä " + tc2 + "§m----------");
				player.sendMessage("");
				for (Player p : player.getWorld().getPlayers()) {
					if (player != p) {
						if (player.getLocation().distance(p.getLocation()) < 150 && (!vanishedPlayers.contains(p.getName()) || 
								CoreUtils.hasRank(player, "valvoja"))) {
							player.sendMessage(tc1 + " - " + tc2 + p.getName() + ": §o" + (int) (player.getLocation()
									.distance(p.getLocation())) + "m");
							b = true;
						}
					}
				}
				if (!b) {
					player.sendMessage(tc3 + " Ei pelaajia lähistöllä!");
				}
				player.sendMessage("");
			}
			else {
				player.spigot().sendMessage(CoreUtils.getVipNeededMessage());
			}
			return true;
		}
		
		// spawn, hub, lobby
		
		if (cmd.getName().equalsIgnoreCase("spawn") || cmd.getName().equalsIgnoreCase("hub") || cmd.getName().equalsIgnoreCase("lobby")) {
			Location spawn = CoreUtils.loadLocation(core, "spawn");
			if (spawn == null) {
				player.sendMessage(tc3 + "Spawn-pistettä ei ole vielä asetettu!");
				return true;
			}
			CoreUtils.teleport(player, spawn);
			return true;
		}
		
		// asetaspawn, setspawn
		
		if (cmd.getName().equalsIgnoreCase("asetaspawn") || cmd.getName().equalsIgnoreCase("setspawn")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				CoreUtils.setLocation(core, "spawn", player.getLocation());
				player.sendMessage(tc2 + "Asetettiin spawn-piste nykyiseen sijaintiisi!");
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// setstartpoint
		
		if (cmd.getName().equalsIgnoreCase("setstartpoint")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				CoreUtils.setLocation(core, "startpoint", player.getLocation());
				player.sendMessage(tc2 + "Asetettiin uusien pelaajien aloituspiste nykyiseen sijaintiisi!");
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// koti, home
		
		if (cmd.getName().equalsIgnoreCase("koti") || cmd.getName().equalsIgnoreCase("home")) {
			new BukkitRunnable() {
				public void run() {
					if (args.length >= 1) {
						try {
							int i = Integer.parseInt(args[0]);
							Location home = CoreUtils.getHome(player.getName(), i);
							if (home != null) {
								CoreUtils.teleport(player, home);
							}
							else {
								if (CoreUtils.hasAccessToHome(player.getName(), i)) {
									new BukkitRunnable() {
										public void run() {
											player.performCommand("asetakoti " + i);
										}
									}.runTask(core);
								}
								else if (i >= 1 && i <= 7) {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Et ole vielä ansainnut tätä kotipistettä!");
								}
								else {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Kotipisteen täytyy olla välillä 1-7!");
								}
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen kotipiste! Kotipisteen tätyy olla numero välillä 1-7!");
						}
					}
					else {
						
						InventoryGUI gui = new InventoryGUI(27, "Kotipisteet");
						
						gui.open(player);
						
						gui.addItem(CoreUtils.getHomeItem(player, 1), 10, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 1");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 2), 11, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 2");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 3), 12, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 3");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 4), 13, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 4");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 5), 14, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 5");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 6), 15, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 6");
							}
						});
						
						gui.addItem(CoreUtils.getHomeItem(player, 7), 16, new InventoryGUIAction() {
							public void onClickAsync() {}
							public void onClick() {
								gui.close(player);
								player.performCommand("koti 7");
							}
						});
					}
				}
			}.runTaskAsynchronously(core);
			return true;
		}
		
		// asetakoti, sethome
		
		if (cmd.getName().equalsIgnoreCase("asetakoti") || cmd.getName().equalsIgnoreCase("sethome")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						try {
							int i = Integer.parseInt(args[0]);
							Location home = CoreUtils.getHome(player.getName(), i);
							if (home != null) {
								if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
									CoreUtils.setHome(player, i);
									player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
									player.sendMessage(tc2 + "Asetettiin kotipiste " + tc1 + "#" + i + tc2 + 
											" nykyiseen sijaintiisi! Voit nyt teleportata sen luo komennolla " + tc1 + "/koti" + tc2 + ".");
								}
								else {
									TextComponent textComponent = new TextComponent("Olet jo asettanut tämän kotipisteen! "
											+ "Jos asetat pisteen nyt tähän, edellinen pisteesi katoaa. Varmista pisteen "
											+ "asettaminen klikkaamalla tätä tekstiä.");
									textComponent.setColor(ChatColor.getByChar(tc2.charAt(1)));
									textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc2 + "Aseta kotipiste tähän sijaintiin ja poista edellinen piste "
													+ "klikkaamalla tästä!").create()));
									textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/asetakoti " + i + " confirm"));
									player.spigot().sendMessage(textComponent);
								}
							}
							else {
								if (CoreUtils.hasAccessToHome(player.getName(), i)) {
									CoreUtils.setHome(player, i);
									player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
									player.sendMessage(tc2 + "Asetettiin kotipiste " + tc1 + "#" + i + tc2 + 
											" nykyiseen sijaintiisi! Voit nyt teleportata sen luo komennolla " + tc1 + "/koti" + tc2 + ".");
								}
								else if (i >= 1 && i <= 7) {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Et ole vielä ansainnut tätä kotipistettä!");
								}
								else {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Kotipisteen täytyy olla välillä 1-7!");
								}
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen kotipiste! Kotipisteen tätyy olla numero välillä 1-7!");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(usage + "/asetakoti <1/2/3/4/5/6/7>");
			}
			return true;
		}
		
		// poistakoti, delhome
		
		if (cmd.getName().equalsIgnoreCase("poistakoti") || cmd.getName().equalsIgnoreCase("delhome")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						try {
							int i = Integer.parseInt(args[0]);
							Location home = CoreUtils.getHome(player.getName(), i);
							if (home != null) {
								if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
									CoreUtils.delHome(player, i);
									player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
									player.sendMessage(tc2 + "Poistettiin kotipiste " + tc1 + "#" + i + tc2 + "!");
								}
								else {
									TextComponent textComponent = new TextComponent("Haluatko aivan varmasti poistaa tämän kotipisteen? "
											+ "Klikkaa tätä tekstiä varmistaaksesi.");
									textComponent.setColor(ChatColor.getByChar(tc2.charAt(1)));
									textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc2 + "Poista tämä kotipiste klikkaamalla tästä!").create()));
									textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/poistakoti " + i + " confirm"));
									player.spigot().sendMessage(textComponent);
								}
							}
							else if (i >= 1 && i <= 7) {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage(tc3 + "Et ole vielä asettanut tätä kotipistettä!");
							}
							else {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage(tc3 + "Kotipisteen täytyy olla välillä 1-7!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen kotipiste! Kotipisteen tätyy olla numero välillä 1-7!");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(usage + "/poistakoti <1/2/3/4/5/6/7>");
			}
			return true;
		}
		
		// matkusta, warp
		
		if (cmd.getName().equalsIgnoreCase("matkusta") || cmd.getName().equalsIgnoreCase("warp")) {
			new BukkitRunnable() {
				public void run() {
					if (args.length >= 1) {
						String warp = "";
						for (String word : args) {
							warp = warp + " " + word;
						}
						warp = warp.trim().replace(" ", "_").toLowerCase();
						Location location = CoreUtils.loadLocation(core, "warps." + warp + ".location");
						if (location != null) {
							MySQLResult visitedData = MySQLUtils.get("SELECT visited_1, visited_2, visited_3 FROM player_stats WHERE name=?", player.getName());
							if (visitedData != null) {
								if (warp.equalsIgnoreCase("port_rotfield") && !visitedData.getBoolean(0, "visited_1")) {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Et ole vielä ansainnut tätä matkustuspistettä!");
									return;
								}
								if (warp.equalsIgnoreCase("lorem_ipsum") && !visitedData.getBoolean(0, "visited_2")) { // TODO
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Et ole vielä ansainnut tätä matkustuspistettä!");
									return;
								}
								if (warp.equalsIgnoreCase("dolor_sit_amet") && !visitedData.getBoolean(0, "visited_3")) { // TODO
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Et ole vielä ansainnut tätä matkustuspistettä!");
									return;
								}
							}
							
							CoreUtils.teleport(player, location);
						}
						else {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(tc3 + "Tuntematon matkustuspiste!");
						}
					}
					else {
						
						InventoryGUI gui = new InventoryGUI(45, "Valitse määränpääsi...");
						
						gui.addItem(CoreUtils.getItem(Material.ENDER_EYE, "§aFort Royal (Spawn)", Arrays.asList("" , 
								"§7§oFort Royal on kuningaskunnan", "§7§okeskus. Korkeiden muurien takana", 
								"§7§okohoaa vielä korkeampi linna, jonka", "§7§osuojissa itse kuningas asustaa.", "", 
								"§a » Teleporttaa klikkaamalla!"), 1), 12, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								gui.close(player);
								player.performCommand("spawn");
							}
						});
						gui.addItem(CoreUtils.getItem(Material.RED_BED, "§aKotipisteet", Arrays.asList("" , 
								"§7§oVoit asettaa itsellesi kotipisteitä,", "§7§ojoihin voit teleportata helposti", 
								"§7§omistä päin maailmaa tahansa.", "", "§a » Avaa valikko klikkaamalla!"), 1, 14), 
								14, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								gui.close(player);
								player.performCommand("koti");
							}
						});
						
						String colorPrefix1 = "§c";
						String actionText1 = "§c ✖ Lukittu!";
						String colorPrefix2 = "§c";
						String actionText2 = "§c ✖ Lukittu!";
						String colorPrefix3 = "§c";
						String actionText3 = "§c ✖ Lukittu!";
						
						MySQLResult visitedData = MySQLUtils.get("SELECT visited_1, visited_2, visited_3 FROM player_stats WHERE name=?", 
								player.getName());
						if (visitedData != null) {
							if (visitedData.getBoolean(0, "visited_1")) {
								colorPrefix1 = "§a";
								actionText1 = "§a » Teleporttaa klikkaamalla!";
							}
							if (visitedData.getBoolean(0, "visited_2")) {
								colorPrefix2 = "§a";
								actionText2 = "§a » Teleporttaa klikkaamalla!";
							}
							if (visitedData.getBoolean(0, "visited_3")) {
								colorPrefix3 = "§a";
								actionText3 = "§a » Teleporttaa klikkaamalla!";
							}
						}
						
						gui.addItem(CoreUtils.getItem(Material.MAP, colorPrefix1 + "Port Rotfield", Arrays.asList("" , 
								"§7§oPort Rotfield on rauhallinen kaupunki", "§7§omeren rannalla. Se on tunnettu laajoista", 
								"§7§opelloistaan ja suuresta satamastaan.", "", actionText1), 1), 29, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								gui.close(player);
								player.performCommand("matkusta port_rotfield");
							}
						});
						gui.addItem(CoreUtils.getItem(Material.MAP, colorPrefix2 + "???", Arrays.asList("" , 
								"§7§oLorem ipsum dolor sit amet,", "§7§oconsectetur adipiscing elit.", 
								"§7§oSed fermentum blandit ante", "§7§oac tristique", "", actionText2), 1), 31, 
								new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								gui.close(player);
								player.performCommand("matkusta lorem_ipsum"); // TODO
							}
						});
						gui.addItem(CoreUtils.getItem(Material.MAP, colorPrefix3 + "???", Arrays.asList("" , 
								"§7§oTulossa pian...", "", actionText3), 1), 33, 
								new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								gui.close(player);
								player.performCommand("matkusta dolor_sit_amet"); // TODO
							}
						});
						
						if (core.getConfig().getConfigurationSection("warps") != null) {
							for (String warp : core.getConfig().getConfigurationSection("warps").getKeys(false)) {
								int slot = core.getConfig().getInt("warps." + warp + ".slot");
								ItemStack item = (ItemStack) core.getConfig().get("warps." + warp + ".item");
								if (item != null && slot >= 0 && slot < gui.getInventory().getSize()) {
									gui.addItem(item, slot, new InventoryGUIAction() {
										public void onClickAsync() { }
										public void onClick() {
											gui.close(player);
											player.performCommand("matkusta " + warp);
										}
									});
								}
							}
						}
						
						gui.open(player);
					}
				}
			}.runTaskAsynchronously(core);
			return true;
		}
		
		// setwarp
		
		if (cmd.getName().equalsIgnoreCase("setwarp")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 2) {
					String warp = "";
					for (int i = 1; i < args.length; i++) {
						warp = warp + " " + args[i];
					}
					warp = warp.trim().replace(" ", "_").toLowerCase();
					try {
						int slot = Integer.parseInt(args[0]);
						CoreUtils.setLocation(core, "warps." + warp + ".location", player.getLocation());
						core.getConfig().set("warps." + warp + ".slot", slot);
						core.getConfig().set("warps." + warp + ".item", player.getInventory().getItemInMainHand());
						core.saveConfig();
						player.sendMessage(tc2 + "Asetettiin warp-piste " + tc1 + warp + tc2 + " nykyiseen sijaintiisi!");
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen slotti!");
					}
				}
				else {
					player.sendMessage(usage + "/setwarp <slot/-1> <pisteen nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// delwarp
		
		if (cmd.getName().equalsIgnoreCase("delwarp")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					String warp = "";
					for (String word : args) {
						warp = warp + " " + word;
					}
					warp = warp.trim().replace(" ", "_").toLowerCase();
					if (core.getConfig().get("warps." + warp) != null) {
						core.getConfig().set("warps." + warp, null);
						core.saveConfig();
						player.sendMessage(tc2 + "Poistettiin warp-piste " + tc1 + warp + tc2 + "!");
					}
					else {
						player.sendMessage(tc3 + "Ei löydetty warp-pistettä antamallasi nimellä!");
					}
				}
				else {
					player.sendMessage(usage + "/delwarp <pisteen nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// swarp
		
		if (cmd.getName().equalsIgnoreCase("swarp")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					String swarp = "";
					for (String word : args) {
						swarp = swarp + " " + word;
					}
					swarp = swarp.trim().replace(" ", "_").toLowerCase();
					Location location = CoreUtils.loadLocation(core, "swarps." + swarp);
					if (location != null) {
						player.teleport(location);
					}
					else {
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						player.sendMessage(tc3 + "Tuntematon matkustuspiste!");
					}
				}
				else {
					
					InventoryGUI swarpGui = new InventoryGUI(27, "Henkilökunnan warp-pisteet");
					
					if (core.getConfig().getConfigurationSection("swarps") != null) {
						int slot = 0;
						for (String warp : core.getConfig().getConfigurationSection("swarps").getKeys(false)) {
							ItemStack item = CoreUtils.getItem(Material.ENDER_EYE, tc1 + warp, null, 1);
							if (slot < swarpGui.getInventory().getSize()) {
								swarpGui.addItem(item, slot, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() {
										swarpGui.close(player);
										player.performCommand("swarp " + warp);
									}
								});
								slot++;
							}
						}
					}
					
					swarpGui.open(player);
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// setswarp
		
		if (cmd.getName().equalsIgnoreCase("setswarp")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					String swarp = "";
					for (String word : args) {
						swarp = swarp + " " + word;
					}
					swarp = swarp.trim().replace(" ", "_").toLowerCase();
					CoreUtils.setLocation(core, "swarps." + swarp, player.getLocation());
					player.sendMessage(tc2 + "Asetettiin swarp-piste " + tc1 + swarp + tc2 + " nykyiseen sijaintiisi!");
				}
				else {
					player.sendMessage(usage + "/setswarp <pisteen nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// delswarp
		
		if (cmd.getName().equalsIgnoreCase("delswarp")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					String swarp = "";
					for (String word : args) {
						swarp = swarp + " " + word;
					}
					swarp = swarp.trim().replace(" ", "_").toLowerCase();
					if (core.getConfig().get("swarps." + swarp) != null) {
						core.getConfig().set("swarps." + swarp, null);
						core.saveConfig();
						player.sendMessage(tc2 + "Poistettiin swarp-piste " + tc1 + swarp + tc2 + "!");
					}
					else {
						player.sendMessage(tc3 + "Ei löydetty swarp-pistettä antamallasi nimellä!");
					}
				}
				else {
					player.sendMessage(usage + "/delswarp <pisteen nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// spy
		
		if (cmd.getName().equalsIgnoreCase("spy")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (spyPlayers.contains(player.getName())) {
					spyPlayers.remove(player.getName());
					player.sendMessage(tc2 + "Et enää näe muiden pelaajien yksityisviestejä!");
				}
				else {
					spyPlayers.add(player.getName());
					player.sendMessage(tc2 + "Näet nyt muiden pelaajien yksityisviestit!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// comspy
		
		if (cmd.getName().equalsIgnoreCase("comspy")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (commandSpyPlayers.contains(player.getName())) {
					commandSpyPlayers.remove(player.getName());
					player.sendMessage(tc2 + "Et enää näe muiden pelaajien komentoja!");
				}
				else {
					commandSpyPlayers.add(player.getName());
					player.sendMessage(tc2 + "Näet nyt muiden pelaajien komennot!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// vanish
		
		if (cmd.getName().equalsIgnoreCase("vanish")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (vanishedPlayers.contains(player.getName())) {
					vanishedPlayers.remove(player.getName());
					player.sendMessage(tc2 + "Et ole enää näkymätön!");
				}
				else {
					vanishedPlayers.add(player.getName());
					player.sendMessage(tc2 + "Olet nyt näkymätön muille pelaajille!");
				}
				CoreUtils.updateVanish();
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// poof
		
		if (cmd.getName().equalsIgnoreCase("poof")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				player.performCommand("vanish");
				player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().getX(), player.getLocation().getY() + 1, 
						player.getLocation().getZ(), 30, 0, 0.5, 0, 0.1);
				player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().getX(), player.getLocation().getY() + 1, 
						player.getLocation().getZ(), 30, 0, 0.5, 0, 0.1);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 10, 2);
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// god
		
		if (cmd.getName().equalsIgnoreCase("god")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (godPlayers.contains(player.getName())) {
					godPlayers.remove(player.getName());
					player.sendMessage(tc2 + "Et ole enää kuolematon!");
				}
				else {
					godPlayers.add(player.getName());
					player.sendMessage(tc2 + "Olet nyt kuolematon!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// fly
		
		if (cmd.getName().equalsIgnoreCase("fly")) {
			if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
				if (player.getAllowFlight()) {
					player.setAllowFlight(false);
					player.sendMessage(tc2 + "Et voi enää lentää!");
				}
				else {
					player.setAllowFlight(true);
					player.sendMessage(tc2 + "Voit nyt lentää!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// gamemode, gm
		
		if (cmd.getName().equalsIgnoreCase("gamemode") || cmd.getName().equalsIgnoreCase("gm")) {
			if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("survival") || args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("0")) {
						player.setGameMode(GameMode.SURVIVAL);
						player.sendMessage(tc2 + "Asetettiin pelitilaksi " + tc1 + "Survival" + tc2 + "!");
					}
					else if (args[0].equalsIgnoreCase("creative") || args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("1")) {
						player.setGameMode(GameMode.CREATIVE);
						player.sendMessage(tc2 + "Asetettiin pelitilaksi " + tc1 + "Creative" + tc2 + "!");
					}
					else if (args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("2")) {
						player.setGameMode(GameMode.ADVENTURE);
						player.sendMessage(tc2 + "Asetettiin pelitilaksi " + tc1 + "Adventure" + tc2 + "!");
					}
					else if (args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("sp") || args[0].equalsIgnoreCase("3")) {
						player.setGameMode(GameMode.SPECTATOR);
						player.sendMessage(tc2 + "Asetettiin pelitilaksi " + tc1 + "Spectator" + tc2 + "!");
					}
					else {
						player.sendMessage(tc3 + "Tuntematon pelitila!");
					}
				}
				else {
					player.sendMessage(usage + "/gamemode <pelitila>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// heal
		
		if (cmd.getName().equalsIgnoreCase("heal")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						double heal = target.getMaxHealth();
						if (args.length >= 2) {
							try {
								heal = Double.parseDouble(args[1]);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						target.setHealth(heal);
						player.sendMessage(tc2 + "Paransit pelaajan " + tc1 + target.getName() + tc2 + "! (" + heal + ")");
					}
					else {
						player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
					}
				}
				else {
					player.setHealth(player.getMaxHealth());
					player.sendMessage(tc2 + "Sinut on parannettu!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// feed
		
		if (cmd.getName().equalsIgnoreCase("feed")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						int feed = 20;
						if (args.length >= 2) {
							try {
								feed = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						target.setFoodLevel(feed);
						target.setSaturation(20);
						player.sendMessage(tc2 + "Tyydytit pelaajan " + tc1 + target.getName() + tc2 + " ruokahalun! (" + feed + ")");
					}
					else {
						player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
					}
				}
				else {
					player.setFoodLevel(20);
					player.setSaturation(20);
					player.sendMessage(tc2 + "Ruokahalusi on tyydytetty!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// back
		
		if (cmd.getName().equalsIgnoreCase("back")) {
			if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
				Location location = CoreUtils.loadLocation(core, "users." + player.getName() + ".back");
				if (location != null) {
					player.teleport(location);
					player.sendMessage(tc2 + "Sinut palautettiin aikaisempaan sijaintiisi!");
				}
				else {
					player.sendMessage(tc3 + "Ei paikkaa, johon teleportata!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// top
		
		if (cmd.getName().equalsIgnoreCase("top")) {
			if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
				int y = player.getWorld().getHighestBlockYAt(player.getLocation());
				Location location = player.getLocation();
				location.setY(y);
				player.teleport(location);
				player.sendMessage(tc2 + "Sinut teleportattiin korkeimpaan mahdolliseen sijaintiin!");
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// clear, ci
		
		if (cmd.getName().equalsIgnoreCase("clear") || cmd.getName().equalsIgnoreCase("ci")) {
			if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
				if (player.getGameMode() == GameMode.CREATIVE) {
					player.getInventory().clear();
					player.sendMessage(tc2 + "Tyhjennettiin tavaraluettelosi!");
				}
				else {
					if (args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
						player.getInventory().clear();
						player.sendMessage(tc2 + "Tyhjennettiin tavaraluettelosi!");
					}
					else {
						TextComponent text = new TextComponent("Olet survival-tilassa! Varmista tavaraluettelon tyhjennys klikkaamalla tästä!");
						text.setColor(ChatColor.getByChar(tc3.charAt(1)));
						text.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/clear confirm"));
						player.spigot().sendMessage(text);
					}
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// invsee
		
		if (cmd.getName().equalsIgnoreCase("invsee")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						if (player != target) {
							player.openInventory(target.getInventory());
						}
						else {
							player.sendMessage(tc3 + "Et voi avata omaa tavaraluetteloasi tällä komennolla!");
						}
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.sendMessage(usage + "/invsee <pelaaja>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// enderchest, echest
		
		if (cmd.getName().equalsIgnoreCase("enderchest") || cmd.getName().equalsIgnoreCase("echest")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						player.openInventory(target.getEnderChest());
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.performCommand("enderchest " + player.getName());
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// swapinventories
		
		if (cmd.getName().equalsIgnoreCase("swapinventories")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						if (core.getConfig().contains("inventories." + target.getUniqueId().toString())) {
							ItemStack[] contents = CoreUtils.loadInventory(core, "inventories." + target.getUniqueId().toString());
							CoreUtils.setInventory(core, "inventories." + target.getUniqueId().toString(), target.getInventory().getContents());
							target.getInventory().setContents(contents);
						}
						else {
							CoreUtils.setInventory(core, "inventories." + target.getUniqueId().toString(), target.getInventory().getContents());
							target.getInventory().clear();
						}
						player.sendMessage(tc2 + "Vaihdettiin pelaajan " + tc1 + target.getName() + tc2 + " survival- ja creative-tavaraluettelot päittäin!");
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.performCommand("swapinventories " + player.getName());
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// evokkimoodi
		
		if (cmd.getName().equalsIgnoreCase("evokkimoodi")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (evokkiModeEnabled) {
					evokkiModeEnabled = false;
					player.sendMessage(tc2 + "Evokkimoodi pois päältä!");
				}
				else {
					evokkiModeEnabled = true;
					player.sendMessage(tc2 + "Evokkimoodi päällä!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// tp, tpo
		
		if (cmd.getName().equalsIgnoreCase("tp") || cmd.getName().equalsIgnoreCase("tpo")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						player.teleport(target);
						player.sendMessage(tc2 + "Sinut teleportattiin pelaajan " + tc1 + target.getName() + tc2 + " luokse!");
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.sendMessage(usage + "/tp <pelaaja>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// tphere, tpohere
		
		if (cmd.getName().equalsIgnoreCase("tphere") || cmd.getName().equalsIgnoreCase("tpohere")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						target.teleport(player);
						player.sendMessage(tc2 + "Pelaaja " + tc1 + target.getName() + tc2 + " teleportattiin sinun luoksesi!");
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.sendMessage(usage + "/tphere <pelaaja>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// tppos
		
		if (cmd.getName().equalsIgnoreCase("tppos")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 3) {
					try {
						int x = 0;
						int y = 0;
						int z = 0;
						if (args[0].equals("~")) {
							x = player.getLocation().getBlockX();
						}
						else if (args[0].startsWith("~")) {
							x = Integer.parseInt(args[0].replace("~", "")) + player.getLocation().getBlockX();
						}
						else {
							x = Integer.parseInt(args[0]);
						}
						if (args[1].equals("~")) {
							y = player.getLocation().getBlockY();
						}
						else if (args[1].startsWith("~")) {
							y = Integer.parseInt(args[1].replace("~", "")) + player.getLocation().getBlockY();
						}
						else {
							y = Integer.parseInt(args[1]);
						}
						if (args[2].equals("~")) {
							z = player.getLocation().getBlockZ();
						}
						else if (args[2].startsWith("~")) {
							z = Integer.parseInt(args[2].replace("~", "")) + player.getLocation().getBlockZ();
						}
						else {
							z = Integer.parseInt(args[2]);
						}
						player.teleport(new Location(player.getWorld(), x, y, z));
						player.sendMessage(tc2 + "Sinut teleportattiin koordinaatteihin x = " + x + ", y = " + y + ", z = " + z);
					}
					catch(Exception e) {
						player.sendMessage(tc3 + "Virheelliset kordinaatit!");
					}
				}
				else {
					player.sendMessage(usage + "/tppos <x> <y> <z>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// noafk
		
		if (cmd.getName().equalsIgnoreCase("noafk")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (CoreUtils.getHaltAfkCounter().contains(player.getName())) {
					CoreUtils.getHaltAfkCounter().remove(player.getName());
					player.sendMessage(tc2 + "NoAFK-tila pois käytöstä!");
				}
				else {
					CoreUtils.getHaltAfkCounter().add(player.getName());
					player.sendMessage(tc2 + "NoAFK-tila käytössä!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// lempinimi, nick
		
		if (cmd.getName().equalsIgnoreCase("lempinimi") || cmd.getName().equalsIgnoreCase("nick")) {
			if (CoreUtils.hasRank(player, "aatelinen")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("pois") || args[0].equalsIgnoreCase("off")) {
						if (core.getConfig().contains("users." + player.getName() + ".chat_nick")) {
							if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
								new BukkitRunnable() {
									public void run() {
										core.getConfig().set("users." + player.getName() + ".chat_nick", null);
										core.saveConfig();
										player.sendMessage(tc2 + "Poistettiin lempinimi käytöstä!");
										MySQLUtils.set("UPDATE player_info SET chat_nick=? WHERE uuid=?", "", player.getUniqueId().toString());
									}
								}.runTaskAsynchronously(core);
							}
							else {
								TextComponent t = new TextComponent("\nHaluatko varmasti poistaa lempinimesi?\n\n\n    ");
								TextComponent yes = new TextComponent("[Kyllä, poista!]");
								yes.setBold(true);
								yes.setColor(ChatColor.DARK_GREEN);
								yes.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lempinimi pois confirm"));
								yes.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klikkaa tästä poistaaksesi lempinimesi!").color(ChatColor.GREEN).create()));
								TextComponent t2 = new TextComponent("\n\n       ");
								t2.setBold(false);
								t2.setClickEvent(new ClickEvent(Action.CHANGE_PAGE, "1"));
								t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, null));
								TextComponent no = new TextComponent("[Peruuta]");
								no.setBold(true);
								no.setColor(ChatColor.DARK_RED);
								no.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lempinimi cancel"));
								no.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klikkaa tästä peruuttaaksesi!").color(ChatColor.RED).create()));
								t.addExtra(yes);
								t.addExtra(t2);
								t.addExtra(no);
								CoreUtils.openBookJson(player, "Lempinimi", "Royal Kingdom", t);
							}
						}
						else {
							player.sendMessage(tc3 + "Sinulla ei ole lempinimeä!");
						}
					}
					else if (args[0].equalsIgnoreCase("cancel")) {
						player.closeInventory();
					}
					else {
						new BukkitRunnable() {
							public void run() {
								MySQLResult infoData = MySQLUtils.get("SELECT nick_last_changed FROM player_info WHERE uuid=?", player.getUniqueId().toString());
								if (infoData != null) {
									long lastChanged = infoData.getLong(0, "nick_last_changed");
									if (lastChanged <= System.currentTimeMillis() - 86400000) {
										String nick = args[0];
										if (nick.length() <= 16) {
											if (nick.matches("\\w+")) {
												if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
													core.getConfig().set("users." + player.getName() + ".chat_nick", nick);
													core.saveConfig();
													player.sendMessage(tc2 + "Asetettiin lempinimeksi: " + nick);
													MySQLUtils.set("UPDATE player_info SET chat_nick=?, nick_last_changed=? WHERE uuid=?", nick, System.currentTimeMillis() + "", player.getUniqueId().toString());
												}
												else {
													TextComponent t = new TextComponent("\nHaluatko varmasti asettaa lempinimeksesi ");
													TextComponent lempinimi = new TextComponent(nick);
													lempinimi.setBold(true);
													TextComponent t2 = new TextComponent("? Voit vaihtaa lempinimeäsi vain 24 tunnin välein.\n\n\n    ");
													t2.setBold(false);
													t2.setColor(ChatColor.RESET);
													TextComponent yes = new TextComponent("[Kyllä, haluan!]");
													yes.setBold(true);
													yes.setColor(ChatColor.DARK_GREEN);
													yes.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lempinimi " + nick + " confirm"));
													yes.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klikkaa tästä asettaaksesi lempinimesi!").color(ChatColor.GREEN).create()));
													TextComponent t3 = new TextComponent("\n\n       ");
													t3.setBold(false);
													t3.setClickEvent(new ClickEvent(Action.CHANGE_PAGE, "1"));
													t3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, null));
													TextComponent no = new TextComponent("[Peruuta]");
													no.setBold(true);
													no.setColor(ChatColor.DARK_RED);
													no.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lempinimi cancel"));
													no.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klikkaa tästä peruuttaaksesi!").color(ChatColor.RED).create()));
													t.addExtra(lempinimi);
													t.addExtra(t2);
													t.addExtra(yes);
													t.addExtra(t3);
													t.addExtra(no);
													CoreUtils.openBookJson(player, "Lempinimi", "Royal Kingdom", t);
												}
											}
											else {
												player.sendMessage(tc3 + "Lempinimi saa sisältää ainoastaan kirjaimia, numeroita ja alaviivoja!");
											}
										}
										else {
											player.sendMessage(tc3 + "Antamasi lempinimi on liian pitkä! Maksimipituus on 16 merkkiä.");
										}
									}
									else {
										player.sendMessage(tc3 + "Lempinimeä voi vaihtaa enintään 24 tunnin välein!");
										player.sendMessage(tc3 + "Sinun täytyy odottaa vielä " + tc4 + CoreUtils.getHoursAndMinsFromMillis(lastChanged + 86400000 - System.currentTimeMillis()) + tc3 +  ".");
									}
								}
							}
						}.runTaskAsynchronously(core);
					}
				}
				else {
					player.sendMessage(usage + "/lempinimi <lempinimi/pois>");
				}
			}
			else {
				player.spigot().sendMessage(CoreUtils.getVipNeededMessage2());
			}
			return true;
		}
		
		// fix
		
		if (cmd.getName().equalsIgnoreCase("fix")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item != null && item.hasItemMeta()) {
					ItemMeta meta = item.getItemMeta();
					Damageable damageable = (Damageable) meta;
					damageable.setDamage(0);
					item.setItemMeta(meta);
					player.sendMessage(tc2 + "Korjasit kädessäsi olevan esineen!");
				}
				else {
					player.sendMessage(tc3 + "Tätä esinettä ei voi korjata!");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// setamount
		
		if (cmd.getName().equalsIgnoreCase("setamount")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					try {
						int amount = Integer.parseInt(args[0]);
						ItemStack item = player.getInventory().getItemInMainHand();
						if (CoreUtils.isNotAir(item)) {
							item.setAmount(amount);
							player.sendMessage(tc2 + "Asetettiin kädessäsi olevan esineen määräksi " + tc1 + amount + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Pidä kädessäsi jotakin esinettä!");
						}
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen määrä!");
					}
				}
				else {
					player.sendMessage(usage + "/setamount <määrä>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// pt
		
		if (cmd.getName().equalsIgnoreCase("pt")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("clear")) {
						ListIterator<String> iterator = powerTools.listIterator();
						while (iterator.hasNext()) {
							String powerTool = iterator.next();
							try {
								String owner = powerTool.split("§")[0];
								String item = powerTool.split("§")[1];
								ItemStack heldItem = player.getInventory().getItemInMainHand();
								if (owner.equalsIgnoreCase(player.getName()) && item.equalsIgnoreCase(heldItem.getType() + "")) {
									iterator.remove();
								}
							}
							catch (ArrayIndexOutOfBoundsException e) {
							}
						}
						player.sendMessage(tc2 + "Poistettiin kaikki komennot kädessäsi olevasta esineestä!");
					}
					else if (args[0].equalsIgnoreCase("clearall")) {
						ListIterator<String> iterator = powerTools.listIterator();
						while (iterator.hasNext()) {
							String powerTool = iterator.next();
							try {
								String owner = powerTool.split("§")[0];
								if (owner.equalsIgnoreCase(player.getName())) {
									iterator.remove();
								}
							}
							catch (ArrayIndexOutOfBoundsException e) {
							}
						}
						player.sendMessage(tc2 + "Poistettiin kaikki komennot kaikista esineistä!");
					}
					else if (args[0].equalsIgnoreCase("dump")) {
						for (String powerTool : powerTools) {
							player.sendMessage(powerTool.replace("§", "&"));
						}
					}
					else {
						String command = "";
						for (String arg : args) {
							command = command + " " + arg;
						}
						command = command.trim();
						ItemStack heldItem = player.getInventory().getItemInMainHand();
						String powerTool = player.getName() + "§" + heldItem.getType() + "§" + command;
						powerTools.add(powerTool);
						player.sendMessage(tc2 + "Lisättiin komento " + tc1 + "/" + command + tc2 + " kädessäsi olevaan esineeseen!");
					}
				}
				else {
					player.sendMessage(usage + "/pt <komento/clear/clearall>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// lightfix
		
		if (cmd.getName().equalsIgnoreCase("lightfix")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("1")) {
						Set<Material> transparent = new HashSet<Material>();
						transparent.add(Material.AIR);
						transparent.add(Material.WATER);
						Block block = player.getTargetBlock(transparent, 150);
						if (block != null) {
							BlockState state = block.getState();
							block.setType(Material.GLOWSTONE);
							new BukkitRunnable() {
								public void run() {
									state.update(true);
								}
							}.runTaskLater(core, 10);
						}
					}
					else if (args[0].equalsIgnoreCase("2")) {
						Location location = player.getLocation();
						for (int x = -20; x <= 20; x++) {
							for (int y = -20; y <= 20; y++) {
								for (int z = -20; z <= 20; z++) {
									location.add(x, y, z);
									Block block = location.getBlock();
									location.subtract(x, y, z);
									if (block != null && block.getLightFromBlocks() >= 14) {
										new BukkitRunnable() {
											int i = 0;
											public void run() {
												i++;
												player.spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 1);
												if (i >= 20) {
													cancel();
												}
											}
										}.runTaskTimer(core, 0, 10);
									}
								}
							}
						}
					}
					else if (args[0].equalsIgnoreCase("3")) {
						Location location = player.getLocation();
						for (int x = -20; x <= 20; x++) {
							for (int y = -20; y <= 20; y++) {
								for (int z = -20; z <= 20; z++) {
									location.add(x, y, z);
									Block block = location.getBlock();
									location.subtract(x, y, z);
									if (block != null && block.getLightFromBlocks() >= 14) {
										BlockState state = block.getState();
										block.setType(Material.STONE_BRICKS, true);
										new BukkitRunnable() {
											public void run() {
												state.update(true);
											}
										}.runTaskLater(core, 20);
									}
								}
							}
						}
					}
					else {
						player.sendMessage(usage + "/lightfix <1/2/3>");
					}
				}
				else {
					player.sendMessage(usage + "/lightfix <1/2/3>");
				}
			}
			return true;
		}
		
		// thor
		
		if (cmd.getName().equalsIgnoreCase("thor")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				Set<Material> transparent = new HashSet<Material>();
				transparent.add(Material.AIR);
				Block block = player.getTargetBlock(transparent, 100);
				player.getWorld().strikeLightningEffect(block.getLocation());
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// getpos
		
		if (cmd.getName().equalsIgnoreCase("getpos")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						String world = target.getWorld().getName();
						int x = target.getLocation().getBlockX();
						int y = target.getLocation().getBlockY();
						int z = target.getLocation().getBlockZ();
						player.sendMessage(tc2 + "Pelaajan " + tc1 + target.getName() + tc2 + " sijainti: " + tc1 + x + " " + y + " " + z + " (" + world + ")");
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					String world = player.getWorld().getName();
					int x = player.getLocation().getBlockX();
					int y = player.getLocation().getBlockY();
					int z = player.getLocation().getBlockZ();
					player.sendMessage(tc2 + "Sijaintisi: " + tc1 + x + " " + y + " " + z + " (" + world + ")");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// entity, mob
		
		if (cmd.getName().equalsIgnoreCase("entity") || cmd.getName().equalsIgnoreCase("mob")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					String entity = args[0].toUpperCase();
					Set<Material> transparent = new HashSet<Material>();
					transparent.add(Material.AIR);
					Block block = player.getTargetBlock(transparent, 100);
					try {
						int amount = 1;
						if (args.length >= 2) {
							try {
								amount = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheellinen määrä!");
								return true;
							}
						}
						Location location = block.getLocation().add(0.5, 1, 0.5);
						for (int i = 0; i < amount; i++) {
							location.getWorld().spawnEntity(location, EntityType.fromName(entity));
						}
						player.sendMessage(tc2 + "Spawnattiin " + tc1 + amount + tc2 + " kappaletta entityä " + tc1 + entity + tc2 + "!");
					}
					catch (NullPointerException e) {
						player.sendMessage(tc3 + "Tuntematon entityn tyyppi!");
					}
				}
				else {
					player.sendMessage(usage + "/entity <tyyppi> [määrä]");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// killall
		
		if (cmd.getName().equalsIgnoreCase("killall")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 2) {
					String type = args[0].toUpperCase();
					try {
						int radius = Integer.parseInt(args[1]);
						if (type.equalsIgnoreCase("all")) {
							int counter = 0;
							for (Entity entity : player.getWorld().getEntities()) {
								if (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.ITEM_FRAME && 
										entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PAINTING && 
										(entity.getCustomName() == null || entity.getCustomName().startsWith("§c"))) { // TODO parempi tapa erottaa custom-mobit pelaajien itse nimeämistä mobeista
									if (entity.getLocation().distance(player.getLocation()) <= radius || radius == -1) {
										entity.remove();
										counter++;
									}
								}
							}
							player.sendMessage(tc2 + "Tapettiin kaikki entityt säteellä " + tc1 + radius + "m" + tc2 + "! (" + counter + ")");
						}
						else {
							try {
								int counter = 0;
								EntityType entityType = EntityType.fromName(type);
								for (Entity entity : player.getWorld().getEntities()) {
									if (entity.getType() == entityType) {
										if (entity.getLocation().distance(player.getLocation()) <= radius || radius == -1) {
											entity.remove();
											counter++;
										}
									}
								}
								player.sendMessage(tc2 + "Tapettiin kaikki entityt tyyppiä " + tc1 + entityType.toString() + tc2 + 
										" säteellä " + tc1 + radius + "m" + tc2 + "! (" + counter + ")");
							}
							catch (NullPointerException e) {
								player.sendMessage(tc3 + "Tuntematon entityn tyyppi!");
							}
						}
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen säde!");
					}
				}
				else {
					player.sendMessage(usage + "/killall <tyyppi/all> <säde>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// xp
		
		if (cmd.getName().equalsIgnoreCase("xp")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 3) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						float amount = 0;
						try {
							amount = Float.parseFloat(args[2]);
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen kokemuksen määrä!");
							return true;
						}
						if (args[1].equalsIgnoreCase("set")) {
							if (amount > 1 || amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemuksen täytyy olla välillä 0-1!");
								return true;
							}
							target.setExp(amount);
							player.sendMessage(tc2 + "Asetettiin pelaajan " + tc1 + target.getName() + tc2 + " kokemukseksi " + 
									tc1 + amount + tc2 + "!");
						}
						else if (args[1].equalsIgnoreCase("give")) {
							if (target.getExp() + amount > 1 || target.getExp() + amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemuksen täytyy olla välillä 0-1!");
								return true;
							}
							target.setExp(target.getExp() + amount);
							player.sendMessage(tc2 + "Lisättiin " + tc1 + amount + tc2 + " kokemusta pelaajalle " + tc1 + 
									target.getName() + tc2 + "!");
						}
						else if (args[1].equalsIgnoreCase("take")) {
							if (target.getExp() - amount > 1 || target.getExp() - amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemuksen täytyy olla välillä 0-1!");
								return true;
							}
							target.setExp(target.getExp() - amount);
							player.sendMessage(tc2 + "Vähennettiin " + tc1 + amount + tc2 + " kokemusta pelaajalta " + tc1 + 
									target.getName() + tc2 + "!");
						}
						else {
							player.sendMessage(usage + "/xp <pelaaja> [<set/give/take> <määrä>]");
						}
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else if (args.length == 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						player.sendMessage(tc2 + "Pelaajan " + tc1 + target.getName() + tc2 + " kokemus: " + tc1 + target.getExp());
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.sendMessage(usage + "/xp <pelaaja> [<set/give/take> <määrä>]");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// xpl
		
		if (cmd.getName().equalsIgnoreCase("xpl")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 3) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						int amount = 0;
						try {
							amount = Integer.parseInt(args[2]);
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen kokemustasojen määrä!");
							return true;
						}
						if (args[1].equalsIgnoreCase("set")) {
							if (amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemustaso ei voi olla alle 0!");
								return true;
							}
							target.setLevel(amount);
							player.sendMessage(tc2 + "Asetettiin pelaajan " + tc1 + target.getName() + tc2 + " kokemustasoksi " + 
									tc1 + amount + tc2 + "!");
						}
						else if (args[1].equalsIgnoreCase("give")) {
							if (target.getLevel() + amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemustaso ei voi olla alle 0!");
								return true;
							}
							target.setLevel(target.getLevel() + amount);
							player.sendMessage(tc2 + "Lisättiin " + tc1 + amount + tc2 + " kokemustasoa pelaajalle " + tc1 + 
									target.getName() + tc2 + "!");
						}
						else if (args[1].equalsIgnoreCase("take")) {
							if (target.getLevel() - amount < 0) {
								player.sendMessage(tc3 + "Virhe: Pelaajan kokemustaso ei voi olla alle 0!");
								return true;
							}
							target.setLevel(target.getLevel() - amount);
							player.sendMessage(tc2 + "Vähennettiin " + tc1 + amount + tc2 + " kokemustasoa pelaajalta " + tc1 + 
									target.getName() + tc2 + "!");
						}
						else {
							player.sendMessage(usage + "/xpl <pelaaja> [<set/give/take> <määrä>]");
						}
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else if (args.length == 1) {
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null) {
						player.sendMessage(tc2 + "Pelaajan " + tc1 + target.getName() + tc2 + " kokemustaso: " + tc1 + target.getExp());
					}
					else {
						player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
					}
				}
				else {
					player.sendMessage(usage + "/xpl <pelaaja> [<set/give/take> <määrä>]");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// portti
		
		if (cmd.getName().equalsIgnoreCase("portti")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 2) {
					if (args[0].equalsIgnoreCase("aseta")) {
						if (b1 != null && b2 != null) {
							CoreUtils.setLocation(core, "gates." + args[1] + ".location-1", b1.getLocation());
							CoreUtils.setLocation(core, "gates." + args[1] + ".location-2", b2.getLocation());
							core.getConfig().set("gates." + args[1] + ".status", 1);
							core.saveConfig();
							b1 = null;
							b2 = null;
							player.sendMessage(tc2 + "Asetettiin portti " + tc1 + "#" + args[1] + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Aseta ensin portin sijainti käyttämällä porttityökalua!");
						}
					}
					else if (args[0].equalsIgnoreCase("poista")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						core.getConfig().set("gates." + args[1], null);
						core.saveConfig();
						player.sendMessage(tc2 + "Poistettiin portti " + tc1 + "#" + args[1] + tc2 + "!");
					}
					else if (args[0].equalsIgnoreCase("lisäänappi")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						Block block = player.getTargetBlock(null, 5);
						if (block != null && block.getType().toString().contains("BUTTON")) {
							int i = new Random().nextInt(10000);
							CoreUtils.setLocation(core, "gates." + args[1] + ".buttons." + i, block.getLocation());
							player.sendMessage(tc2 + "Lisättiin nappi porttiin " + tc1 + "#" + args[1] + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Katso kohti sitä nappia, jonka haluat lisätä!");
						}
					}
					else if (args[0].equalsIgnoreCase("poistanapit")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						core.getConfig().set("gates." + args[1] + ".buttons", null);
						core.saveConfig();
						player.sendMessage(tc2 + "Poistettiin kaikki napit portista " + tc1 + "#" + args[1] + tc2 + "!");
					}
					else if (args[0].equalsIgnoreCase("avaa")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						player.sendMessage(tc2 + "Avataan portti " + tc1 + "#" + args[1] + tc2 + "!");
						Location l1 = CoreUtils.loadLocation(core, "gates." + args[1] + ".location-1");
						Location l2 = CoreUtils.loadLocation(core, "gates." + args[1] + ".location-2");
						if (l1.getWorld() == l2.getWorld() && l1.getBlockX() <= l2.getBlockX() && l1.getBlockY() <= l2.getBlockY() && l1.getBlockZ() <= l2.getBlockZ()) {
							core.getConfig().set("gates." + args[1] + ".status", 2);
							core.saveConfig();
							new BukkitRunnable() {
								int y = l1.getBlockY();
								public void run() {
									for (int x = l1.getBlockX(); x <= l2.getBlockX(); x++) {
										for (int z = l1.getBlockZ(); z <= l2.getBlockZ(); z++) {
											Block block = l1.getWorld().getBlockAt(x, y, z);
											if (args[1].startsWith("-")) {
												if (block != null && block.getType() == Material.SMOOTH_STONE) {
													block.setType(Material.AIR);
													block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.1f, 2);
												}
											}
											else {
												if (block != null && block.getType() == Material.OAK_FENCE) {
													block.setType(Material.AIR);
													block.getWorld().playSound(block.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.1f, 2);
												}
											}
										}
									}
									if (y >= l2.getBlockY()) {
										cancel();
										core.getConfig().set("gates." + args[1] + ".status", 1);
										core.saveConfig();
									}
									else {
										y++;
									}
								}
							}.runTaskTimer(core, 15, 15);
						}
					}
					else if (args[0].equalsIgnoreCase("sulje")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						player.sendMessage(tc2 + "Suljetaan portti " + tc1 + "#" + args[1] + tc2 + "!");
						Location l1 = CoreUtils.loadLocation(core, "gates." + args[1] + ".location-1");
						Location l2 = CoreUtils.loadLocation(core, "gates." + args[1] + ".location-2");
						if (l1.getWorld() == l2.getWorld() && l1.getBlockX() <= l2.getBlockX() && l1.getBlockY() <= l2.getBlockY() && l1.getBlockZ() <= l2.getBlockZ()) {
							core.getConfig().set("gates." + args[1] + ".status", 2);
							core.saveConfig();
							new BukkitRunnable() {
								int y = l2.getBlockY();
								public void run() {
									for (int x = l1.getBlockX(); x <= l2.getBlockX(); x++) {
										for (int z = l1.getBlockZ(); z <= l2.getBlockZ(); z++) {
											Block block = l1.getWorld().getBlockAt(x, y, z);
											if (args[1].startsWith("-")) {
												if (block != null && block.getType() == Material.AIR) {
													block.setType(Material.SMOOTH_STONE);
													block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.1f, 2);
												}
											}
											else {
												if (block != null && block.getType() == Material.AIR) {
													block.setType(Material.OAK_FENCE);
													block.getWorld().playSound(block.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.1f, 2);
												}
											}
										}
									}
									if (y <= l1.getBlockY()) {
										cancel();
										core.getConfig().set("gates." + args[1] + ".status", 0);
										core.saveConfig();
									}
									else {
										y--;
									}
								}
							}.runTaskTimer(core, 15, 15);
						}
					}
					else if (args[0].endsWith("tp")) {
						if (core.getConfig().get("gates." + args[1]) == null) {
							player.sendMessage(tc3 + "Ei löydetty porttia kyseisellä ID:llä!");
							return true;
						}
						player.teleport(CoreUtils.loadLocation(core, "gates." + args[1] + ".location-1"));
						player.sendMessage(tc2 + "Sinut teleportattiin portin " + tc1 + "#" + args[1] + tc2 + " luokse!");
					}
					else {
						player.sendMessage(usage + "/portti <avaa/sulje/aseta/poista/lisäänappi/poistanapit/tp> <ID>" + tc3 + " tai " + tc4 + "/portti <lista/työkalu>");
					}
				}
				else {
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("työkalu")) {
							ItemStack i = new ItemStack(Material.BLAZE_ROD); {
								ItemMeta m = i.getItemMeta();
								m.setDisplayName("§6Porttityökalu");
								ArrayList<String> l = new ArrayList<String>();
								l.add("");
								l.add("§7Klikkaa hiiren vasemmalla asettaaksesi pisteen #1,");
								l.add("§7klikkaa hiiren oikealla asettaaksesi pisteen #2.");
								m.setLore(l);
								i.setItemMeta(m);
							}
							player.getInventory().addItem(i);
							player.sendMessage(tc3 + "Huom! Varmista, että kaikki ykköspisteen kordinaatit ovat PIENEMPIÄ kuin kakkospisteen!");
						}
						else if (args[0].equalsIgnoreCase("lista")) {
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Portit " + tc2 + "§m----------");
							player.sendMessage("");
							if (core.getConfig().getConfigurationSection("gates") != null && 
									!core.getConfig().getConfigurationSection("gates").getKeys(false).isEmpty()) {
								for (String s : core.getConfig().getConfigurationSection("gates").getKeys(false)) {
									String world = core.getConfig().getString("gates." + s + ".location-1.world");
									TextComponent t = new TextComponent(tc2 + " - " + tc1 + "#" + s + tc2 + " maailmassa '" + world + "' " + 
											tc1 + "[Teleporttaa klikkaamalla]");
									t.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/portti tp " + s));
									player.spigot().sendMessage(t);
								}
								player.sendMessage("");
							}
							else {
								player.sendMessage(tc3 + " Ei portteja!");
								player.sendMessage("");
							}
						}
						else {
							player.sendMessage(usage + "/portti <avaa/sulje/aseta/poista/lisäänappi/poistanapit/tp> <ID>" + tc3 + " tai " + tc4 + "/portti <lista/työkalu>");
						}
					}
					else {
						player.sendMessage(usage + "/portti <avaa/sulje/aseta/poista/lisäänappi/poistanapit/tp> <ID>" + tc3 + " tai " + tc4 + "/portti <lista/työkalu>");
					}
				}
			}
			else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		// komentokuutio, command-block
		
		if (cmd.getName().equalsIgnoreCase("komentokuutio") || cmd.getName().equalsIgnoreCase("command-block")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("aseta") || args[0].equalsIgnoreCase("set")) {
						if (args.length >= 2) {
							Block block = player.getTargetBlock(null, 20);
							if (block != null) {
								if (block.getType().toString().contains("SIGN") || block.getType().toString().contains("BUTTON") || block.getType().toString().contains("PRESSURE_PLATE")) {
									String command = "";
									for (int i = 1; i < args.length; i++) {
										command += args[i] + " ";
									}
									command = command.trim();
									Location location = block.getLocation();
									String key = location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ();
									core.getConfig().set("command-blocks." + key, command);
									core.saveConfig();
									player.sendMessage(tc2 + "Lisättiin komento!");
								}
								else {
									player.sendMessage(tc3 + "Komentoja voi lisätä vain painelaattoihin, nappeihin ja kyltteihin!");
								}
							}
							else {
								player.sendMessage(tc3 + "Komentoja voi lisätä vain painelaattoihin, nappeihin ja kyltteihin!");
							}
						}
						else {
							player.sendMessage(usage + "/komentokuutio <aseta> <komento>");
						}
					}
					else if (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list")) {
						player.sendMessage("");
						player.sendMessage(tc2 + "§m----------" + tc1 + " Komentokuutiot " + tc2 + "§m----------");
						player.sendMessage("");
						if (core.getConfig().getConfigurationSection("command-blocks") != null && 
								!core.getConfig().getConfigurationSection("command-blocks").getKeys(false).isEmpty()) {
							for (String key : core.getConfig().getConfigurationSection("command-blocks").getKeys(false)) {
								try {
									String world = key.split("/")[0];
									int x = Integer.parseInt(key.split("/")[1]);
									int y = Integer.parseInt(key.split("/")[2]);
									int z = Integer.parseInt(key.split("/")[3]);
									String command = core.getConfig().getString("command-blocks." + key);
									player.sendMessage(tc2 + " - " + tc1 + world + " " + x + " " + y + " " + z + ": " + tc2 + "/" + command);
								}
								catch (Exception e) {
								}
							}
							player.sendMessage("");
						}
						else {
							player.sendMessage(tc3 + " Ei komentokuutioita!");
							player.sendMessage("");
						}
					}
					else if (args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("remove")) {
						Block block = player.getTargetBlock(null, 20);
						if (block != null) {
							Location location = block.getLocation();
							String key = location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ();
							if (core.getConfig().contains("command-blocks." + key)) {
								core.getConfig().set("command-blocks." + key, null);
								core.saveConfig();
								player.sendMessage(tc2 + "Poistettiin komento!");
							}
							else {
								player.sendMessage(tc3 + "Tähän kuutioon ei ole asetettu komentoa!");
							}
						}
						else {
							player.sendMessage(tc3 + "Tähän kuutioon ei ole asetettu komentoa!");
						}
					}
					else {
						player.sendMessage(usage + "/komentokuutio <aseta> <komento>" + tc3 + " tai " + tc4 + "/komentokuutio <lista/poista>");
					}
				}
				else {
					player.sendMessage(usage + "/komentokuutio <aseta> <komento>" + tc3 + " tai " + tc4 + "/komentokuutio <lista/poista>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// maailma, world
		
		if (cmd.getName().equalsIgnoreCase("maailma") || cmd.getName().equalsIgnoreCase("world")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list")) {
						player.sendMessage("");
						player.sendMessage(tc2 + "§m----------" + tc1 + " Maailmat " + tc2 + "§m----------");
						player.sendMessage("");
						for (World world : Bukkit.getWorlds()) {
							TextComponent textComponent = new TextComponent(tc2 + " - " + tc1 + world.getName() + tc2 + " [Teleporttaa klikkaamalla]");
							textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder(tc1 + "Teleporttaa tähän maailmaan klikkaamalla!").create()));
							textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/maailma tp " + world.getName()));
							player.spigot().sendMessage(textComponent);
						}
						player.sendMessage("");
						player.sendMessage(tc2 + " Olet tällä hetkellä maailmassa " + tc1 + player.getWorld().getName() + tc2 + ".");
						player.sendMessage("");
					}
					else if (args[0].equalsIgnoreCase("tp")) {
						if (args.length >= 2) {
							World world = Bukkit.getWorld(args[1]);
							if (world != null) {
								player.teleport(world.getSpawnLocation());
								player.sendMessage(tc2 + "Sinut teleportattiin maailman " + tc1 + world.getName() + tc2 + " spawn-pisteeseen!");
							}
							else {
								player.sendMessage(tc3 + "Tuntematon maailma!");
							}
						}
						else {
							player.sendMessage(usage + "/maailma tp <maailma>");
						}
					}
					else if (args[0].equalsIgnoreCase("luo") || args[0].equalsIgnoreCase("create")) {
						if (args.length >= 3) {
							for (World world : Bukkit.getWorlds()) {
								if (world.getName().equalsIgnoreCase(args[1])) {
									player.sendMessage(tc3 + "Maailma tällä nimellä on jo olemassa!");
									return true;
								}
							}
							Environment environment;
							if (args[2].equalsIgnoreCase("normal")) {
								environment = Environment.NORMAL;
							}
							else if (args[2].equalsIgnoreCase("nether")) {
								environment = Environment.NETHER;
							}
							else if (args[2].equalsIgnoreCase("end")) {
								environment = Environment.THE_END;
							}
							else {
								player.sendMessage(tc3 + "Tuntematon maailman tyyppi!");
								return true;
							}
							player.sendMessage(tc2 + "Luodaan maailmaa " + tc1 + args[1] + tc2 + " tyypillä " + tc1 + 
									environment.toString() + tc2 + ", odota...");
							WorldCreator worldCreator = new WorldCreator(args[1]);
							worldCreator.environment(environment);
							Bukkit.createWorld(worldCreator);
							core.getConfig().set("worlds." + args[1], environment.toString());
							core.saveConfig();
							player.sendMessage(tc2 + "Maailman luonti valmis!");
						}
						else {
							player.sendMessage(usage + "/maailma luo <nimi> <tyyppi>");
						}
					}
					else if (args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("remove")) {
						if (args.length >= 2) {
							if (core.getConfig().contains("worlds." + args[1])) {
								core.getConfig().set("worlds." + args[1], null);
								core.saveConfig();
								player.sendMessage(tc2 + "Poistettiin maailma " + tc1 + args[1] + tc2 + "! Maailma poistuu käytöstä "
										+ "seuraavan uudelleenkäynnistyksen yhteydessä.");
							}
							else {
								player.sendMessage(tc3 + "Tuntematon maailma!");
							}
						}
						else {
							player.sendMessage(usage + "/maailma poista <nimi>");
						}
					}
					else {
						player.sendMessage(usage + "/maailma <lista>" + tc3 + " tai " + tc4 + "/maailma <tp/luo/poista> <nimi>");
					}
				}
				else {
					player.sendMessage(usage + "/maailma <lista>" + tc3 + " tai " + tc4 + "/maailma <tp/luo/poista> <nimi>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// talli, stable
		
		if (cmd.getName().equalsIgnoreCase("talli") || cmd.getName().equalsIgnoreCase("stable")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 2) {
					if (args[0].equalsIgnoreCase("lisää") || args[0].equalsIgnoreCase("add")) {
						String id = args[1];
						if (!core.getConfig().contains("stables." + id)) {
							core.getConfig().set("stables." + id + ".in-use", false);
							core.saveConfig();
							player.sendMessage(tc2 + "Lisättiin uusi hevostalli ID:llä " + tc1 + "#" + id + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Hevostalli tällä ID:llä on jo olemassa!");
						}
					}
					else if (args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("remove")) {
						String id = args[1];
						if (core.getConfig().contains("stables." + id)) {
							core.getConfig().set("stables." + id, null);
							core.saveConfig();
							player.sendMessage(tc2 + "Poistettiin hevostalli ID:llä " + tc1 + "#" + id + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty hevostallia kyseisellä ID:llä!");
						}
					}
					else if (args[0].equalsIgnoreCase("tiedot") || args[0].equalsIgnoreCase("info")) {
						String id = args[1];
						if (core.getConfig().contains("stables." + id)) {
							// TODO näytä enemmän tietoja
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Hevostalli " + tc2 + "§m----------");
							player.sendMessage("");
							player.sendMessage(tc2 + " ID: " + tc1 + id);
							player.sendMessage("");
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty hevostallia kyseisellä ID:llä!");
						}
					}
					else if (args[0].equalsIgnoreCase("lisääkyltti") || args[0].equalsIgnoreCase("addsign")) {
						String id = args[1];
						if (core.getConfig().contains("stables." + id)) {
							Block block = player.getTargetBlock(null, 10);
							if (block != null && block.getState() instanceof Sign) {
								int identifier = new Random().nextInt(10000);
								CoreUtils.setLocation(core, "stables." + id + ".signs." + identifier, block.getLocation());
								updateStableSigns();
								player.sendMessage(tc2 + "Lisättiin uusi kyltti hevostalliin " + tc1 + "#" + id + tc2 + "!");
							}
							else {
								player.sendMessage(tc3 + "Katso kohti sitä kylttiä, jonka haluat lisätä!");
							}
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty hevostallia kyseisellä ID:llä!");
						}
					}
					else if (args[0].equalsIgnoreCase("poistakyltit") || args[0].equalsIgnoreCase("removesigns")) {
						String id = args[1];
						if (core.getConfig().contains("stables." + id)) {
							core.getConfig().set("stables." + id + ".signs", null);
							core.saveConfig();
							player.sendMessage(tc2 + "Poistettiin kaikki kyltit hevostallista " + tc1 + "#" + id + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty hevostallia kyseisellä ID:llä!");
						}
					}
					else {
						player.sendMessage(usage + "/talli <lisää/poista/tiedot/lisääkyltti/poistakyltit> <ID>" + tc3 + " tai " + tc4 + "/talli <lista>");
					}
				}
				else if (args.length >= 1 && (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list"))) {
					player.sendMessage("");
					player.sendMessage(tc2 + "§m----------" + tc1 + " Hevostallit " + tc2 + "§m----------");
					player.sendMessage("");
					if (core.getConfig().getConfigurationSection("stables") != null) {
						for (String key : core.getConfig().getConfigurationSection("stables").getKeys(false)) {
							// TODO näytä enemmän tietoja
							player.sendMessage(tc2 + " - " + tc1 + "#" + key);
							player.sendMessage("");
						}
					}
					else {
						player.sendMessage(tc3 + " Ei hevostalleja!");
						player.sendMessage("");
					}
				}
				else {
					player.sendMessage(usage + "/talli <lisää/poista/tiedot/lisääkyltti/poistakyltit> <ID>" + tc3 + " tai " + tc4 + "/talli <lista>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// holo
		
		if (cmd.getName().equalsIgnoreCase("holo")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("create")) {
						if (args.length >= 2) {
							
							String text = "";
							for (int i = 1; i < args.length; i++) {
								text = text + " " + args[i];
							}
							text = ChatColor.translateAlternateColorCodes('&', text.trim());
							
							ArmorStand a = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
							a.setCustomName(text);
							a.setCustomNameVisible(true);
							a.setRemoveWhenFarAway(false);
							a.setGravity(false);
							a.setVisible(false);
							a.setMarker(true);
							a.setInvulnerable(true);
							a.setAI(false);
							a.setBasePlate(false);
							a.setSmall(true);
							
							selectedHolograms.put(player.getName(), a);
							player.sendMessage(tc2 + "Luotiin uusi hologrammi ja valittiin se!");
						}
						else {
							player.sendMessage(usage + "/holo create <teksti>");
						}
					}
					else if (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("sel")) {
						
						ArmorStand select = null;
						
						for (Entity entity : player.getWorld().getEntities()) {
							if (entity.getType() == EntityType.ARMOR_STAND) {
								ArmorStand a = (ArmorStand) entity;
								if (a.getCustomName() != null && a.isCustomNameVisible() && a.isMarker()) {
									if (a.getLocation().distance(player.getLocation()) < 20) {
										if (select != null) {
											if (a.getLocation().distance(player.getLocation()) < 
													select.getLocation().distance(player.getLocation())) {
												select = a;
											}
										}
										else {
											select = a;
										}
									}
								}
							}
						}
						
						if (select != null) {
							selectedHolograms.put(player.getName(), select);
							player.sendMessage(tc2 + "Valittiin hologrammi: " + select.getCustomName());
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty hologrammeja lähistöltä!");
						}
					}
					else if (args[0].equalsIgnoreCase("edit")) {
						if (args.length >= 2) {
							
							String text = "";
							for (int i = 1; i < args.length; i++) {
								text = text + " " + args[i];
							}
							text = ChatColor.translateAlternateColorCodes('&', text.trim());
							
							ArmorStand a = selectedHolograms.get(player.getName());
							
							if (a != null && !a.isDead()) {
								a.setCustomName(text);
								player.sendMessage(tc2 + "Muokattiin valitun hologrammin tekstiä!");
							}
							else {
								player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
							}
						}
						else {
							player.sendMessage(usage + "/holo edit <teksti>");
						}
					}
					else if (args[0].equalsIgnoreCase("move")) {
						if (args.length >= 3) {
							
							float amount = 0;
							int direction = 0;
							
							try {
								amount = Float.parseFloat(args[2]);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheellinen määrä!");
								return true;
							}
							
							if (args[1].equalsIgnoreCase("x")) {
								direction = 0;
							}
							else if (args[1].equalsIgnoreCase("y")) {
								direction = 1;
							}
							else if (args[1].equalsIgnoreCase("z")) {
								direction = 2;
							}
							else {
								player.sendMessage(tc3 + "Virheellinen suunta!");
								return true;
							}
							
							ArmorStand a = selectedHolograms.get(player.getName());
							
							if (a != null && !a.isDead()) {
								if (direction == 0) {
									a.teleport(a.getLocation().clone().add(amount, 0, 0));
									player.sendMessage(tc2 + "Siirrettiin valittua hologrammia " + tc1 + amount + "m" + tc2 + 
											" akselilla " + tc1 + "x" + tc2 + "!");
								}
								else if (direction == 1) {
									a.teleport(a.getLocation().clone().add(0, amount, 0));
									player.sendMessage(tc2 + "Siirrettiin valittua hologrammia " + tc1 + amount + "m" + tc2 + 
											" akselilla " + tc1 + "y" + tc2 + "!");
								}
								else if (direction == 2) {
									a.teleport(a.getLocation().clone().add(0, 0, amount));
									player.sendMessage(tc2 + "Siirrettiin valittua hologrammia " + tc1 + amount + "m" + tc2 + 
											" akselilla " + tc1 + "z" + tc2 + "!");
								}
							}
							else {
								player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
							}
						}
						else {
							player.sendMessage(usage + "/holo move <x/y/z> <määrä>");
						}
					}
					else if (args[0].equalsIgnoreCase("remove")) {
						ArmorStand a = selectedHolograms.get(player.getName());
						if (a != null && !a.isDead()) {
							a.remove();
							player.sendMessage(tc2 + "Poistettiin valittu hologrammi!");
						}
						else {
							player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
						}
					}
					else if (args[0].equalsIgnoreCase("tp")) {
						ArmorStand a = selectedHolograms.get(player.getName());
						if (a != null && !a.isDead()) {
							player.teleport(a);
							player.sendMessage(tc2 + "Teleportattiin valitun hologrammin luo!");
						}
						else {
							player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
						}
					}
					else if (args[0].equalsIgnoreCase("tphere")) {
						ArmorStand a = selectedHolograms.get(player.getName());
						if (a != null && !a.isDead()) {
							a.teleport(player);
							player.sendMessage(tc2 + "Teleportattiin valittu hologrammi luoksesi!");
						}
						else {
							player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
						}
					}
					else if (args[0].equalsIgnoreCase("vis")) {
						ArmorStand a = selectedHolograms.get(player.getName());
						if (a != null && !a.isDead()) {
							a.setVisible(!a.isVisible());
							player.sendMessage(tc2 + "Vaihdettiin valitun hologrammin näkyvyyttä!");
						}
						else {
							player.sendMessage(tc3 + "Sinulla ei ole hologrammia valittuna!");
						}
					}
					else {
						player.sendMessage(usage + "/holo <create/select/edit/move/remove/tp/tphere/vis>");
					}
				}
				else {
					player.sendMessage(usage + "/holo <create/select/edit/move/remove/tp/tphere/vis>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// tutorial
		
		if (cmd.getName().equalsIgnoreCase("tutorial")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("start")) {
						core.getTutorial().startTutorial(player);
					}
					else if (args[0].equalsIgnoreCase("title")) {
						if (args.length >= 3) {
							String text = "";
							for (int i = 2; i < args.length; i++) {
								text = text + " " + args[i];
							}
							text = ChatColor.translateAlternateColorCodes('&', text.trim());
							core.getConfig().set("tutorial." + args[1] + ".title", text);
							core.saveConfig();
							player.sendMessage(tc2 + "Asetettiin teksti!");
						}
						else {
							player.sendMessage(usage + "/tutorial title <stage> <teksti>");
						}
					}
					else if (args[0].equalsIgnoreCase("subtitle")) {
						if (args.length >= 3) {
							String text = "";
							for (int i = 2; i < args.length; i++) {
								text = text + " " + args[i];
							}
							text = ChatColor.translateAlternateColorCodes('&', text.trim());
							core.getConfig().set("tutorial." + args[1] + ".subtitle", text);
							core.saveConfig();
							player.sendMessage(tc2 + "Asetettiin teksti!");
						}
						else {
							player.sendMessage(usage + "/tutorial subtitle <stage> <teksti>");
						}
					}
					else if (args[0].equalsIgnoreCase("location")) {
						if (args.length >= 2) {
							CoreUtils.setLocation(core, "tutorial." + args[1] + ".location", player.getLocation().add(0, 2, 0));
							player.sendMessage(tc2 + "Asetettiin sijainti!");
						}
						else {
							player.sendMessage(usage + "/tutorial location <stage>");
						}
					}
					else if (args[0].equalsIgnoreCase("startpoint")) {
						CoreUtils.setLocation(core, "tutorial-start-point", player.getLocation().add(0, 2, 0));
						player.sendMessage(tc2 + "Asetettiin aloitussijainti!");
					}
					else if (args[0].equalsIgnoreCase("reload")) {
						core.getTutorial().reload();
						player.sendMessage(tc2 + "Uudelleenladattiin tutoriaali!");
					}
					else {
						player.sendMessage(usage + "/tutorial <start/title/subtitle/location/startpoint/reload>");
					}
				}
				else {
					player.sendMessage(usage + "/tutorial <start/title/subtitle/location/startpoint/reload>");
				}
			}
			else if (args.length >= 1 && args[0].equalsIgnoreCase("start")) {
				core.getTutorial().startTutorial(player);
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// tardis
		
		if (cmd.getName().equalsIgnoreCase("tardis")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä") && (player.getName().equals("T4TU_") || player.getName().equals("Ahishi") || player.getName().equals("evokki0075"))) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("spawn")) {
						if (!canTardisMove1 && player.getName().equals("T4TU_")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi spawnata juuri nyt!");
							return true;
						}
						if (!canTardisMove2 && player.getName().equals("Ahishi")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi spawnata juuri nyt!");
							return true;
						}
						if (!canTardisMove3 && player.getName().equals("evokki0075")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi spawnata juuri nyt!");
							return true;
						}
						final Location modelLocation = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".model-location");
						if (modelLocation == null) {
							player.sendMessage("§9§lTARDIS" + tc3 + "in mallin sijaintia ei ole asetettu!");
							return true;
						}
						final Location newLocation;
						if (args.length >= 5) {
							try {
								World world = Bukkit.getWorld(args[1]);
								double x = Double.parseDouble(args[2]);
								double y = Double.parseDouble(args[3]);
								double z = Double.parseDouble(args[4]);
								if (world != null) {
									newLocation = new Location(world, x, y, z).getBlock().getLocation().add(0.5, 0, 0.5);
								}
								else {
									player.sendMessage(tc3 + "Virheellinen maailman nimi!");
									return true;
								}
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheellinen sijainti!");
								return true;
							}
						}
						else if (args.length >= 4) {
							try {
								World world = player.getWorld();
								double x = Double.parseDouble(args[1]);
								double y = Double.parseDouble(args[2]);
								double z = Double.parseDouble(args[3]);
								newLocation = new Location(world, x, y, z).getBlock().getLocation().add(0.5, 0, 0.5);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheellinen sijainti!");
								return true;
							}
						}
						else if (args.length >= 3) {
							try {
								World world = player.getWorld();
								double x = Double.parseDouble(args[1]);
								double z = Double.parseDouble(args[2]);
								double y = world.getHighestBlockYAt((int) x, (int) z);
								newLocation = new Location(world, x, y, z).getBlock().getLocation().add(0.5, 0, 0.5);
							}
							catch (NumberFormatException e) {
								player.sendMessage(tc3 + "Virheellinen sijainti!");
								return true;
							}
						}
						else if (args.length >= 2) {
							String swarp = args[1].toLowerCase();
							Location swarpLocation = CoreUtils.loadLocation(core, "swarps." + swarp);
							if (swarpLocation != null) {
								newLocation = swarpLocation.getBlock().getLocation().add(0.5, 0, 0.5);
							}
							else {
								player.sendMessage(tc3 + "Virheellinen sijainti!");
								return true;
							}
						}
						else {
							newLocation = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
						}
						for (int x = -1; x <= 1; x++) {
							for (int y = 0; y <= 2; y++) {
								for (int z = -1; z <= 1; z++) {
									newLocation.add(x, y, z);
									if (newLocation.getBlock() == null || newLocation.getBlock().getType() != Material.AIR) {
										player.sendMessage(tc3 + "Ei tarpeeksi tilaa §9§lTARDIS" + tc3 + "ille!");
										return true;
									}
									newLocation.subtract(x, y, z);
								}
							}
						}
						final Location currentLocation = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".current-location");
						final Location centerLocation = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".center-location");
						if (player.getName().equals("T4TU_")) {
							canTardisMove1 = false;
						}
						if (player.getName().equals("Ahishi")) {
							canTardisMove2 = false;
						}
						if (player.getName().equals("evokki0075")) {
							canTardisMove3 = false;
						}
						player.sendMessage(tc2 + "Spawnataan §9§lTARDIS" + tc2 + "...");
						new BukkitRunnable() {
							int i = 0;
							public void run() {
								i++;
								if (i >= 16) {
									cancel();
									for (int x = -1; x <= 1; x++) {
										for (int y = 0; y <= 2; y++) {
											for (int z = -1; z <= 1; z++) {
												if (currentLocation != null) {
													currentLocation.add(x, y, z);
													currentLocation.getBlock().setType(Material.AIR, false);
													currentLocation.subtract(x, y, z);
												}
												modelLocation.add(x, y, z);
												newLocation.add(x, y, z);
												Block modelBlock = modelLocation.getBlock();
												Block newBlock = newLocation.getBlock();
												newBlock.setType(modelBlock.getType(), false);
												newBlock.setBlockData(modelBlock.getBlockData(), false);
												if (modelBlock.getState() instanceof Banner) {
													Banner modelBanner = (Banner) modelBlock.getState();
													Banner newBanner = (Banner) newBlock.getState();
													newBanner.setBaseColor(modelBanner.getBaseColor());
													newBanner.setPatterns(modelBanner.getPatterns());
													newBanner.update(false, false);
												}
												modelLocation.subtract(x, y, z);
												newLocation.subtract(x, y, z);
											}
										}
									}
									newLocation.getWorld().playSound(newLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1, 0.1f);
									if (currentLocation != null) {
										currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1, 0.1f);
									}
									if (centerLocation != null) {
										centerLocation.getWorld().playSound(centerLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1, 0.1f);
									}
									if (player.getName().equals("T4TU_")) {
										canTardisMove1 = true;
									}
									if (player.getName().equals("Ahishi")) {
										canTardisMove2 = true;
									}
									if (player.getName().equals("evokki0075")) {
										canTardisMove3 = true;
									}
									CoreUtils.setLocation(core, "tardis." + player.getName() + ".current-location", newLocation);
									updateTardisBlocks(player.getName());
								}
								else {
									newLocation.add(0, 1.5, 0);
									newLocation.getWorld().spawnParticle(Particle.CRIT, newLocation, 20, 0.5, 0.75, 0.5, 0);
									newLocation.getWorld().spawnParticle(Particle.CRIT_MAGIC, newLocation, 20, 0.5, 0.75, 0.5, 0);
									newLocation.getWorld().playSound(newLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1);
									newLocation.subtract(0, 1.5, 0);
									if (currentLocation != null) {
										currentLocation.add(0, 1.5, 0);
										currentLocation.getWorld().spawnParticle(Particle.CRIT, currentLocation, 20, 0.5, 0.75, 0.5, 0);
										currentLocation.getWorld().spawnParticle(Particle.CRIT_MAGIC, currentLocation, 20, 0.5, 0.75, 0.5, 0);
										currentLocation.getWorld().playSound(currentLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1);
										currentLocation.subtract(0, 1.5, 0);
									}
									if (centerLocation != null) {
										centerLocation.getWorld().playSound(centerLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1);
									}
								}
							}
						}.runTaskTimer(core, 5, 5);
					}
					else if (args[0].equalsIgnoreCase("despawn")) {
						if (!canTardisMove1 && player.getName().equals("T4TU_")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi despawnata juuri nyt!");
							return true;
						}
						if (!canTardisMove2 && player.getName().equals("Ahishi")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi despawnata juuri nyt!");
							return true;
						}
						if (!canTardisMove3 && player.getName().equals("evokki0075")) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei voi despawnata juuri nyt!");
							return true;
						}
						final Location currentLocation = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".current-location");
						if (currentLocation == null) {
							player.sendMessage("§9§lTARDIS" + tc3 + "ta ei ole tällä hetkellä spawnattuna!");
							return true;
						}
						final Location centerLocation = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".center-location");
						if (player.getName().equals("T4TU_")) {
							canTardisMove1 = false;
						}
						if (player.getName().equals("Ahishi")) {
							canTardisMove2 = false;
						}
						if (player.getName().equals("evokki0075")) {
							canTardisMove3 = false;
						}
						player.sendMessage(tc2 + "Despawnataan §9§lTARDIS" + tc2 + "...");
						new BukkitRunnable() {
							int i = 0;
							public void run() {
								i++;
								if (i >= 16) {
									cancel();
									for (int x = -1; x <= 1; x++) {
										for (int y = 0; y <= 2; y++) {
											for (int z = -1; z <= 1; z++) {
												currentLocation.add(x, y, z);
												currentLocation.getBlock().setType(Material.AIR, false);
												currentLocation.subtract(x, y, z);
											}
										}
									}
									currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1, 0.1f);
									if (centerLocation != null) {
										centerLocation.getWorld().playSound(centerLocation, Sound.ENTITY_ARMOR_STAND_BREAK, 1, 0.1f);
									}
									if (player.getName().equals("T4TU_")) {
										canTardisMove1 = true;
									}
									if (player.getName().equals("Ahishi")) {
										canTardisMove2 = true;
									}
									if (player.getName().equals("evokki0075")) {
										canTardisMove3 = true;
									}
									core.getConfig().set("tardis." + player.getName() + ".current-location", null);
									core.saveConfig();
									updateTardisBlocks(player.getName());
								}
								else {
									currentLocation.add(0, 1.5, 0);
									currentLocation.getWorld().spawnParticle(Particle.CRIT, currentLocation, 20, 0.5, 0.75, 0.5, 0);
									currentLocation.getWorld().spawnParticle(Particle.CRIT_MAGIC, currentLocation, 20, 0.5, 0.75, 0.5, 0);
									currentLocation.getWorld().playSound(currentLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1);
									currentLocation.subtract(0, 1.5, 0);
									if (centerLocation != null) {
										centerLocation.getWorld().playSound(centerLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1);
									}
								}
							}
						}.runTaskTimer(core, 5, 5);
					}
					else if (args[0].equalsIgnoreCase("tp")) {
						Location location = CoreUtils.loadLocation(core, "tardis." + player.getName() + ".interior-location");
						if (location != null) {
							player.teleport(location);
							player.sendMessage(tc2 + "Teleportattiin §9§lTARDIS" + tc2 + "iin!");
						}
						else {
							player.sendMessage("§9§lTARDIS" + tc3 + "in sisustan sijaintia ei ole asetettu!");
						}
					}
					else if (args[0].equalsIgnoreCase("set-model-location")) {
						CoreUtils.setLocation(core, "tardis." + player.getName() + ".model-location", player.getLocation());
						player.sendMessage(tc2 + "Asetettiin §9§lTARDIS" + tc2 + "in mallin sijainti nykyiseen sijaintiisi!");
					}
					else if (args[0].equalsIgnoreCase("set-interior-location")) {
						CoreUtils.setLocation(core, "tardis." + player.getName() + ".interior-location", player.getLocation());
						player.sendMessage(tc2 + "Asetettiin §9§lTARDIS" + tc2 + "in sisustan sijainti nykyiseen sijaintiisi!");
					}
					else if (args[0].equalsIgnoreCase("set-center-location")) {
						CoreUtils.setLocation(core, "tardis." + player.getName() + ".center-location", player.getLocation());
						player.sendMessage(tc2 + "Asetettiin §9§lTARDIS" + tc2 + "in keskustan sijainti nykyiseen sijaintiisi!");
					}
					else if (args[0].equalsIgnoreCase("key")) {
						player.getInventory().addItem(CoreUtils.getItem(Material.TRIPWIRE_HOOK, "§9§lTARDIS§bin avain", null, 1));
						player.sendMessage(tc2 + "Annettiin §9§lTARDIS" + tc2 + "in avain!");
					}
					else {
						player.sendMessage(usage + "/tardis <spawn/despawn/tp/set-model-location/set-interior-location/set-center-location/key>");
					}
				}
				else {
					player.sendMessage(usage + "/tardis <spawn/despawn/tp/set-model-location/set-interior-location/set-center-location/key>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// rankaise, h
		
		if (cmd.getName().equalsIgnoreCase("rankaise") || cmd.getName().equalsIgnoreCase("h")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				if (args.length >= 1) {
					new BukkitRunnable() {
						public void run() {
							MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
							if (infoData != null) {
								
								String name = infoData.getString(0, "name");
								String uuidWithoutDashes = infoData.getString(0, "uuid").replace("-", "");
								
								InventoryGUI gui = new InventoryGUI(54, "Rankaise: " + name);
								
								gui.open(player);
								
								// TODO
								
								List<String> ban = Arrays.asList("", "§7Ei ole");
								List<String> jail = Arrays.asList("", "§7Ei ole");
								List<String> mute = Arrays.asList("", "§7Ei ole");
								List<String> history = Arrays.asList("", "§7 » Näytä historia klikkaamalla");
								
								MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes);
								if (banData != null) {
									
									String banner = banData.getString(0, "banner");
									String reason = banData.getString(0, "reason");
									long time = banData.getLong(0, "time");
									long duration = banData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									String[] wrappedReason = ChatPaginator.wordWrap(reason, 30);
									
									if (duration == 0) {
										expires = "Ei koskaan";
									}
									
									ban = Arrays.asList("", "§cAntanut: §7" + banner, "§cAnnettu: §7" + timeGiven, "§cPäättyy: §7" + expires, "§cSyy:", "");
									
									ArrayList<String> temp = new ArrayList<String>();
									temp.addAll(ban);
									ban = temp;
									
									for (int i = 0; i < wrappedReason.length; i++) {
										ban.add("§7§o" + ChatColor.stripColor(wrappedReason[i]));
									}
								}
								
								MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuidWithoutDashes);
								if (jailData != null) {
									
									String jailer = jailData.getString(0, "jailer");
									String reason = jailData.getString(0, "reason");
									long time = jailData.getLong(0, "time");
									long duration = jailData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									String[] wrappedReason = ChatPaginator.wordWrap(reason, 30);
									
									if (duration == 0) {
										expires = "Ei koskaan";
									}
									
									jail = Arrays.asList("", "§6Antanut: §7" + jailer, "§6Annettu: §7" + timeGiven, "§6Päättyy: §7" + expires, "§6Syy:", "");
									
									ArrayList<String> temp = new ArrayList<String>();
									temp.addAll(jail);
									jail = temp;
									
									for (int i = 0; i < wrappedReason.length; i++) {
										jail.add("§7§o" + ChatColor.stripColor(wrappedReason[i]));
									}
								}
								
								MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuidWithoutDashes);
								if (muteData != null) {
									
									String muter = muteData.getString(0, "muter");
									String reason = muteData.getString(0, "reason");
									long time = muteData.getLong(0, "time");
									long duration = muteData.getLong(0, "duration");
									String timeGiven = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
									String expires = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
									String[] wrappedReason = ChatPaginator.wordWrap(reason, 30);
									
									if (duration == 0) {
										expires = "Ei koskaan";
									}
									
									mute = Arrays.asList("", "§eAntanut: §7" + muter, "§eAnnettu: §7" + timeGiven, "§ePäättyy: §7" + expires, "§eSyy:", "");
									
									ArrayList<String> temp = new ArrayList<String>();
									temp.addAll(mute);
									mute = temp;
									
									for (int i = 0; i < wrappedReason.length; i++) {
										mute.add("§7§o" + ChatColor.stripColor(wrappedReason[i]));
									}
								}
								
								int banCounter = 10;
								for (String punishment : banPunishments) {
									try {
										String description = punishment.split("§")[0];
										String command = punishment.split("§")[1].replace("<user>", name);
										List<String> lore = Arrays.asList("", "§7/" + command);
										gui.addItem(CoreUtils.getItem(Material.RED_STAINED_GLASS_PANE, "§c" + description, lore, 1), banCounter, 
												new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand(command);
											}
										});
									}
									catch (ArrayIndexOutOfBoundsException e) {
									}
									banCounter++;
								}
								
								int jailCounter = 19;
								for (String punishment : jailPunishments) {
									try {
										String description = punishment.split("§")[0];
										String command = punishment.split("§")[1].replace("<user>", name);
										List<String> lore = Arrays.asList("", "§7/" + command);
										gui.addItem(CoreUtils.getItem(Material.ORANGE_STAINED_GLASS_PANE, "§6" + description, lore, 1), jailCounter, 
												new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand(command);
											}
										});
									}
									catch (ArrayIndexOutOfBoundsException e) {
									}
									jailCounter++;
								}
								
								int muteCounter = 28;
								for (String punishment : mutePunishments) {
									try {
										String description = punishment.split("§")[0];
										String command = punishment.split("§")[1].replace("<user>", name);
										List<String> lore = Arrays.asList("", "§7/" + command);
										gui.addItem(CoreUtils.getItem(Material.YELLOW_STAINED_GLASS_PANE, "§e" + description, lore, 1), muteCounter, 
												new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand(command);
											}
										});
									}
									catch (ArrayIndexOutOfBoundsException e) {
									}
									muteCounter++;
								}
								
								gui.addItem(CoreUtils.getItem(Material.RED_TERRACOTTA, "§cPorttikielto", ban, 1), 47, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() { }
								});
								
								gui.addItem(CoreUtils.getItem(Material.ORANGE_TERRACOTTA, "§6Vangittu", jail, 1), 48, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() { }
								});
								
								gui.addItem(CoreUtils.getItem(Material.YELLOW_TERRACOTTA, "§eHiljennys", mute, 1), 49, new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() { }
								});
								
								gui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§aRangaistushistoria", history, 1), 51, 
										new InventoryGUIAction() {
									public void onClickAsync() { }
									public void onClick() {
										gui.close(player);
										player.performCommand("history " + name);
									}
								});
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
							}
						}
					}.runTaskAsynchronously(core);
				}
				else {
					player.sendMessage(usage + "/rankaise <pelaaja>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// huomautus, note
		
		if (cmd.getName().equalsIgnoreCase("huomautus") || cmd.getName().equalsIgnoreCase("note")) {
			if (CoreUtils.hasRank(player, "valvoja")) {
				new BukkitRunnable() {
					public void run() {
						if (args.length >= 1) {
							if (args.length >= 3)  {
								if (args[1].equalsIgnoreCase("lisää") || args[1].equalsIgnoreCase("add")) {
									
									String note = "";
									for (int i = 2; i < args.length; i++) {
										note = note + " " + args[i];
									}
									note = note.trim();
									
									MySQLResult infoData = MySQLUtils.get("SELECT name, uuid FROM player_info WHERE name=?", args[0]);
									if (infoData == null) {
										player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
										return;
									}
									
									String name = infoData.getString(0, "name");
									String uuid = infoData.getString(0, "uuid");
									
									MySQLUtils.set("INSERT INTO player_notes (name, uuid, note, giver, time) VALUES (?, ?, ?, ?, ?)", 
											name, uuid, note, player.getName(), System.currentTimeMillis() + "");
									player.sendMessage(tc2 + "Lisättiin uusi huomautus pelaajalle " + tc1 + name + tc2 + "!");
									
									if (Bukkit.getPlayer(name) != null) {
										CoreUtils.updateNotes(Bukkit.getPlayer(name));
									}
								}
								else if (args[1].equalsIgnoreCase("poista") || args[1].equalsIgnoreCase("remove")) {
									
									MySQLResult notesData = MySQLUtils.get("SELECT * FROM player_notes WHERE id=? AND name=?", args[2], args[0]);
									if (notesData == null) {
										player.sendMessage(tc3 + "Ei löydetty huomautuksia annetuilla nimellä ja ID:llä!");
										return;
									}
									
									String id = notesData.getString(0, "id");
									String name = notesData.getString(0, "name");
									
									MySQLUtils.set("DELETE FROM player_notes WHERE id=?", id);
									
									player.sendMessage(tc2 + "Poistettiin huomautus pelaajalta " + tc1 + name + tc2 + "!");
									
									if (Bukkit.getPlayer(name) != null) {
										CoreUtils.updateNotes(Bukkit.getPlayer(name));
									}
								}
								else {
									player.sendMessage(usage + "/huomautus <pelaaja> [<lisää/poista> <huomautus>]");
								}
							}
							else {
								
								MySQLResult notesData = MySQLUtils.get("SELECT * FROM player_notes WHERE name=?", args[0]);
								if (notesData == null) {
									player.sendMessage(tc3 + "Ei löydetty huomautuksia antamallasi nimellä!");
									return;	
								}
								
								String name = notesData.getString(0, "name");
								
								player.sendMessage("");
								player.sendMessage(tc2 + "§m----------" + tc1 + " Huomautukset: " + name + " " + tc2 + "§m----------");
								player.sendMessage("");
								
								for (int i = 0; i < notesData.getRows(); i++) {
									String id = notesData.getString(i, "id");
									String text = notesData.getString(i, "note");
									String giver = notesData.getString(i, "giver");
									String time = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(notesData.getLong(i, "time") + 
											CoreUtils.TIME_OFFSET);
									TextComponent textComponent = new TextComponent(tc1 + " - " + giver + ": §7§o" + text);
									textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc1 + "ID: " + tc2 + id + "\n" + tc1 + "Antoi: " + tc2 + giver + "\n" + 
											tc1 + "Annettu: " + tc2 + time).create()));
									player.spigot().sendMessage(textComponent);
								}
								
								player.sendMessage("");
							}
						}
						else {
							player.sendMessage(usage + "/huomautus <pelaaja> [<lisää/poista> <huomautus>]");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		// sakko, sakot
		
		if (cmd.getName().equalsIgnoreCase("sakko") || cmd.getName().equalsIgnoreCase("sakot")) {
			new BukkitRunnable() {
				public void run() {
					if (args.length >= 1) {
						if (args[0].equalsIgnoreCase("maksa")) {
							if (args.length >= 2) {
								String id = args[1];
								MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines WHERE name=? AND id=?", player.getName(), id);
								if (finesData != null) {
									int amount = finesData.getInt(0, "amount");
									MySQLResult statsData = MySQLUtils.get("SELECT * FROM player_stats WHERE name=?", player.getName());
									if (statsData != null) {
										int money = statsData.getInt(0, "money");
										if (money >= amount) {
											MySQLUtils.set("UPDATE player_stats SET money=? WHERE money=? AND name=?", (money - amount) + "", 
													money + "", player.getName());
											MySQLUtils.set("DELETE FROM player_fines WHERE id=?", id);
											player.sendMessage(tc2 + "Maksoit pois " + tc1 + amount + "£" + tc2 + " arvoisen sakkomaksun!");
											player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
										}
										else {
											player.sendMessage(tc3 + "Tililläsi ei ole tarpeeksi rahaa tämän sakkomaksun maksamiseen!");
											player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
										}
									}
								}
							}
							else {
								new BukkitRunnable() {
									public void run() {
										player.performCommand("sakko");
									}
								}.runTask(core);
							}
						}
						else if (CoreUtils.hasRank(player, "valvoja")) {
							if (args[0].equalsIgnoreCase("poista")) {
								if (args.length >= 2) {
									String id = args[1];
									MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines WHERE id=?", id);
									if (finesData != null) {
										MySQLUtils.set("DELETE FROM player_fines WHERE id=?", id);
										player.sendMessage(tc2 + "Poistettiin sakkomaksu ID:llä " + tc1 + id + tc2 + "!");
									}
									else {
										player.sendMessage(tc3 + "Ei löydetty sakkomaksua antamallasi ID:llä!");
									}
								}
								else {
									player.sendMessage(usage + "/sakko poista <ID>");
								}
							}
							else {
								if (args.length >= 2) {
									if (args.length >= 4) {
										try {
											
											int amount = Integer.parseInt(args[1]);
											int days = Integer.parseInt(args[2]);
											
											String reason = "";
											for (int i = 3; i < args.length; i++) {
												reason = reason + " " + args[i];
											}
											reason = reason.trim();
											
											long duration = System.currentTimeMillis() + days * 86400000;
											
											MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[0]);
											if (infoData != null) {
												
												String name = infoData.getString(0, "name");
												String uuid = infoData.getString(0, "uuid");
												
												MySQLUtils.set("INSERT INTO player_fines (name, uuid, amount, giver, reason, duration, time) "
														+ "VALUES (?, ?, ?, ?, ?, ?, ?)", name, uuid, amount + "", player.getName(), reason, 
														duration + "", System.currentTimeMillis() + "");
												
												player.sendMessage(tc2 + "Määrättiin " + tc1 + amount + "£" + tc2 + " sakkoa pelaajalle " + 
														tc1 + name + tc2 + "!");
												
												Player p = Bukkit.getPlayer(name);
												if (p != null) {
													p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
													p.sendMessage("");
													p.sendMessage(tc3 + "§m---------------" + tc4 + " Sakkomaksu " + tc3 + "§m---------------");
													p.sendMessage("");
													p.sendMessage(tc3 + " Sinulle on määrätty maksettavaksi " + tc4 + amount + "£" + tc3 + 
															" sakkoa!");
													p.sendMessage("");
													TextComponent text = new TextComponent(tc3 + " Lisätietoja tästä sakkomaksusta saat komennolla " + tc4 + "/sakot" + tc3 + " tai klikkaamalla tästä.");
													text.setColor(ChatColor.getByChar(tc3.charAt(1)));
													text.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sakot"));
													p.spigot().sendMessage(text);
													p.sendMessage("");
												}
											}
											else {
												player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
											}
										}
										catch (NumberFormatException e) {
											player.sendMessage(tc3 + "Virheellinen summa tai maksuaika!");
										}
									}
									else {
										player.sendMessage(usage + "/sakko <pelaaja> <summa> <maksuaika päivinä> <syy>");
									}
								}
								else {
									
									MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines WHERE name=?", args[0]);
									if (finesData == null) {
										player.sendMessage(tc3 + "Ei löydetty sakkomaksuja antamallasi nimellä!");
										return;	
									}
									
									String name = finesData.getString(0, "name");
									
									player.sendMessage("");
									player.sendMessage(tc2 + "§m----------" + tc1 + " Sakkomaksut: " + name + " " + tc2 + "§m----------");
									player.sendMessage("");
									for (int i = 0; i < finesData.getRows(); i++) {
										
										int id = finesData.getInt(i, "id");
										int amount = finesData.getInt(i, "amount");
										String giver = finesData.getString(i, "giver");
										String reason = finesData.getString(i, "reason");
										long duration = finesData.getLong(i, "duration");
										long time = finesData.getLong(i, "time");
										
										String expirationDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
												new Date(duration + CoreUtils.TIME_OFFSET));
										String givenDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
												new Date(time + CoreUtils.TIME_OFFSET));
										
										TextComponent t1 = new TextComponent(tc1 + " - ");
										TextComponent t2 = new TextComponent(tc1 + amount + "£: ");
										TextComponent t3 = new TextComponent(tc2 + reason);
										t3.setColor(ChatColor.getByChar(tc2.charAt(1)));
										t3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
												new ComponentBuilder(tc1 + "ID: " + tc2 + id + "\n" + tc1 + "Summa: " + tc2 + amount + "£\n" + 
														tc1 + "Antanut: " + tc2 + giver + "\n" + tc1 + "Annettu: " + tc2 + givenDate + "\n" + 
														tc1 + "Erääntyy: " + tc2 + expirationDate).create()));
										t1.addExtra(t2);
										t1.addExtra(t3);
										
										player.spigot().sendMessage(t1);
									}
									player.sendMessage("");
								}
							}
						}
						else {
							new BukkitRunnable() {
								public void run() {
									player.performCommand("sakko");
								}
							}.runTask(core);
						}
					}
					else {
						player.sendMessage("");
						player.sendMessage(tc3 + "§m----------" + tc4 + " Sakkomaksut " + tc3 + "§m----------");
						player.sendMessage("");
						MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines WHERE name=?", player.getName());
						if (finesData != null) {
							for (int i = 0; i < finesData.getRows(); i++) {
								
								int id = finesData.getInt(i, "id");
								int amount = finesData.getInt(i, "amount");
								String reason = finesData.getString(i, "reason");
								long duration = finesData.getLong(i, "duration");
								
								String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(duration + CoreUtils.TIME_OFFSET));
								
								ClickEvent clickEvent = new ClickEvent(Action.RUN_COMMAND, "/sakko maksa " + id);
								HoverEvent hoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
										new ComponentBuilder(tc4 + "Summa: " + tc3 + amount + "£\n" + tc4 + "Erääntyy: " + 
												tc3 + date + "\n\n" + tc3 + "Maksa tämä sakkomaksu klikkaamalla!").create());
								
								TextComponent t1 = new TextComponent(tc4 + " - ");
								t1.setColor(ChatColor.getByChar(tc4.charAt(1)));
								t1.setClickEvent(clickEvent);
								t1.setHoverEvent(hoverEvent);
								TextComponent t2 = new TextComponent(tc4 + amount + "£: ");
								t2.setColor(ChatColor.getByChar(tc4.charAt(1)));
								t2.setClickEvent(clickEvent);
								t2.setHoverEvent(hoverEvent);
								TextComponent t3 = new TextComponent(tc3 + reason);
								t3.setColor(ChatColor.getByChar(tc3.charAt(1)));
								t3.setClickEvent(clickEvent);
								t3.setHoverEvent(hoverEvent);
								t1.addExtra(t2);
								t1.addExtra(t3);
								
								player.spigot().sendMessage(t1);
							}
							player.sendMessage("");
							player.sendMessage(tc3 + " Maksa sakkomaksut klikkaamalla niitä!");
						}
						else {
							player.sendMessage(tc3 + " Sinulla ei ole maksamattomia sakkomaksuja! :)");
						}
						player.sendMessage("");
					}
				}
			}.runTaskAsynchronously(core);
			return true;
		}
		
		// tiketti, grief, ticket
		
		if (cmd.getName().equalsIgnoreCase("tiketti") || cmd.getName().equalsIgnoreCase("grief") || cmd.getName().equalsIgnoreCase("ticket")) {
			if (args.length >= 1) {
				
				if (args.length < 3) {
					player.sendMessage(tc3 + "Tikettisi vaikuttaa hieman lyhyeltä. Olethan ystävällinen ja annat tiketissäsi hieman "
							+ "enemmän tietoa asiastasi.");
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return true;
				}
				
				String message = "";
				for (String arg : args) {
					message = message + " " + arg;
				}
				message = message.trim();
				
				int ticketCount = core.getConfig().getInt("ticketcount") + 1;
				Location location = player.getLocation();
				core.getConfig().set("tickets." + ticketCount + ".ilmoittaja", player.getName());
				core.getConfig().set("tickets." + ticketCount + ".viesti", message);
				core.getConfig().set("tickets." + ticketCount + ".aika", System.currentTimeMillis());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.world", location.getWorld().getName());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.x", location.getX());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.y", location.getY());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.z", location.getZ());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.yaw", location.getYaw());
				core.getConfig().set("tickets." + ticketCount + ".sijainti.pitch", location.getPitch());
				core.getConfig().set("ticketcount", ticketCount);
				core.saveConfig();
				
				TextComponent t1 = new TextComponent(tc1 + " " + player.getName() + tc2 + " avasi uuden tiketin " + tc1 + "#" + 
						ticketCount + tc2 + "!  ");
				t1.setColor(ChatColor.getByChar(tc2.charAt(1)));
				TextComponent t2 = new TextComponent("[Teleporttaa]");
				t2.setColor(ChatColor.getByChar(tc2.charAt(1)));
				t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/tiketit tp " + ticketCount));
				t1.addExtra(t2);
				
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (CoreUtils.hasRank(p, "valvoja")) {
						p.sendMessage("");
						p.spigot().sendMessage(t1);
						p.sendMessage("");
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 10, 0.1f);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 2);
					}
				}
				
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketti " + tc2 + "§m----------");
				player.sendMessage("");
				player.sendMessage(tc2 + " Kiitos tiketin luomisesta! Henkilökuntamme käsittelee sen mahdollisimman pian.");
				player.sendMessage("");
				player.sendMessage(tc2 + " Nähdäksesi listan kaikista omista tiketeistäsi, kirjoita:");
				player.sendMessage(tc1 + "  /tiketit");
				player.sendMessage("");
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				
				String webhook = core.getConfig().getString("discord-ticket-webhook");
				
				if (webhook != null) {
					String discordId = "#" + ticketCount;
					String discordColor = "13645610";
					String discordAuthor = player.getName().replace("_", "\\\\_");
					String discordTimestamp = new SimpleDateFormat("dd.MM.yyyy HH.mm").format(new Date(System.currentTimeMillis() + CoreUtils.TIME_OFFSET)).replace(" ", " klo ");
					String discordMessage = message.replace("\\", "").replace("_", "\\\\_").replace("*", "\\\\*").replace("~", "\\\\~").replace("`", "\\\\`");
					String json = "{\"embeds\":[{\"title\":\"Uusi tiketti! " + discordId + "\",\"description\":\"⁣\",\"color\":" + discordColor + ",\"fields\":["
							+ "{\"name\":\"Ilmoittaja\",\"value\":\"" + discordAuthor + "\",\"inline\":true},"
							+ "{\"name\":\"Aika\",\"value\":\"" + discordTimestamp + "\",\"inline\":true},"
							+ "{\"name\":\"ID\",\"value\":\"" + discordId + "\\n⁣\",\"inline\":true},"
							+ "{\"name\":\"Viesti\",\"value\":\"_" + discordMessage + "_\"}"
							+ "]}]}";
					CoreUtils.sendJsonASync(webhook, json);
				}
			}
			else {
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketti " + tc2 + "§m----------");
				player.sendMessage("");
				player.sendMessage(tc2 + " Mikäli talosi tai muu rakennelmasi on hajotettu jonkun muun pelaajan toimesta ilman lupaasi, "
						+ "voit ilmoittaa siitä henkilökunnallemme luomalla uuden " + tc1 + "tiketin" + tc2 + ". Kun teet tiketin, muista " + 
						tc1 + "seisoa hajotetun paikan vieressä" + tc2 + "!");
				player.sendMessage("");
				player.sendMessage(tc2 + " Luodaksesi uuden tiketin, kirjoita:");
				player.sendMessage(tc1 + "  /tiketti <mitä on hajotettu>");
				player.sendMessage("");
			}
			return true;
		}
		
		// tiketit, griefs, tickets
		
		if (cmd.getName().equalsIgnoreCase("tiketit") || cmd.getName().equalsIgnoreCase("griefs") || cmd.getName().equalsIgnoreCase("tickets")) {
			if (args.length >= 2) {
				String ticket = args[1].replace(".", "");
				if (args[0].equalsIgnoreCase("tiedot") || args[0].equalsIgnoreCase("info")) {
					if (CoreUtils.hasRank(player, "valvoja")) {
						if (core.getConfig().contains("tickets." + ticket)) {
							
							String creator = core.getConfig().getString("tickets." + ticket + ".ilmoittaja");
							String message = core.getConfig().getString("tickets." + ticket + ".viesti");
							boolean closed = core.getConfig().getBoolean("tickets." + ticket + ".suljettu");
							long time = core.getConfig().getLong("tickets." + ticket + ".aika");
							String timeCreated = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
							
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketti " + tc2 + "§m----------");
							player.sendMessage("");
							player.sendMessage(tc1 + " ID: " + tc2 + ticket);
							player.sendMessage(tc1 + " Avannut: " + tc2 + creator);
							player.sendMessage(tc1 + " Aika: " + tc2 + timeCreated);
							player.sendMessage("");
							if (closed) {
								player.sendMessage(tc2 + "§o §m" + message + "§c Suljettu");
							}
							else {
								player.sendMessage(tc2 + "§o " + message);
							}
							player.sendMessage("");
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty tikettiä antamallasi ID:llä!");
						}
					}
					else {
						player.performCommand("tiketit");
					}
				}
				else if (args[0].equalsIgnoreCase("tp")) {
					if (CoreUtils.hasRank(player, "valvoja")) {
						if (core.getConfig().contains("tickets." + ticket)) {
							
							String world = core.getConfig().getString("tickets." + ticket + ".sijainti.world");
							double x = core.getConfig().getDouble("tickets." + ticket + ".sijainti.x");
							double y = core.getConfig().getDouble("tickets." + ticket + ".sijainti.y");
							double z = core.getConfig().getDouble("tickets." + ticket + ".sijainti.z");
							double yaw = core.getConfig().getDouble("tickets." + ticket + ".sijainti.yaw");
							double pitch = core.getConfig().getDouble("tickets." + ticket + ".sijainti.pitch");
							
							Location location = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
							
							player.teleport(location);
							player.performCommand("tiketit tiedot " + ticket);
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty tikettiä antamallasi ID:llä!");
						}
					}
					else {
						player.performCommand("tiketit");
					}
				}
				else if (args[0].equalsIgnoreCase("sulje") || args[0].equalsIgnoreCase("close")) {
					if (core.getConfig().contains("tickets." + ticket)) {
						if (CoreUtils.hasRank(player, "valvoja")) {
							if (!core.getConfig().getBoolean("tickets." + ticket + ".suljettu")) {
								core.getConfig().set("tickets." + ticket + ".suljettu", true);
								core.saveConfig();
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(p, "valvoja")) {
										p.sendMessage("");
										p.sendMessage(tc1 + " " + player.getName() + tc2 + " sulki tiketin " + tc1 + "#" + ticket + tc2 + "!");
										p.sendMessage("");
									}
								}
								Player p = Bukkit.getPlayer(core.getConfig().getString("tickets." + ticket + ".ilmoittaja"));
								if (p != null) {
									p.sendMessage("");
									p.sendMessage(tc2 + "§m----------" + tc1 + " Tiketit " + tc2 + "§m----------");
									p.sendMessage("");
									p.sendMessage(tc2 + " Henkilökuntamme on käsitellyt ja sulkenut tikettisi " + tc1 + "#" + ticket + tc2 + ".");
									p.sendMessage(tc2 + " Kiitos tiketistäsi!");
									p.sendMessage("");
									p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								}
								else {
									int i = core.getConfig().getInt("ticketinfo." + player.getName());
									core.getConfig().set("ticketinfo." + player.getName(), i - 1);
									core.saveConfig();
								}
							}
							else {
								player.sendMessage(tc3 + "Tämä tiketti on jo suljettu!");
							}
						}
						else {
							if (core.getConfig().getString("tickets." + ticket + ".ilmoittaja").equals(player.getName())) {
								if (!core.getConfig().getBoolean("tickets." + ticket + ".suljettu")) {
									core.getConfig().set("tickets." + ticket + ".suljettu", true);
									core.saveConfig();
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (CoreUtils.hasRank(p, "valvoja")) {
											p.sendMessage("");
											p.sendMessage(tc1 + " " + player.getName() + tc2 + " sulki oman tikettinsä " + tc1 + "#" + 
													ticket + tc2 + "!");
											p.sendMessage("");
										}
									}
									player.sendMessage(tc2 + "Suljettiin tiketti!");
								}
							}
						}
					}
					else {
						player.sendMessage(tc3 + "Ei löydetty tikettiä antamallasi ID:llä!");
					}
				}
				else if (args[0].equalsIgnoreCase("avaa") || args[0].equalsIgnoreCase("open")) {
					if (CoreUtils.hasRank(player, "valvoja")) {
						if (core.getConfig().contains("tickets." + ticket)) {
							if (core.getConfig().getBoolean("tickets." + ticket + ".suljettu")) {
								core.getConfig().set("tickets." + ticket + ".suljettu", false);
								core.saveConfig();
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (CoreUtils.hasRank(p, "valvoja")) {
										p.sendMessage("");
										p.sendMessage(tc1 + " " + player.getName() + tc2 + " avasi uudelleen tiketin " + tc1 + "#" + ticket + tc2 + "!");
										p.sendMessage("");
									}
								}
								Player p = Bukkit.getPlayer(core.getConfig().getString("tickets." + ticket + ".ilmoittaja"));
								if (p != null) {
									p.sendMessage("");
									p.sendMessage(tc2 + "§m----------" + tc1 + " Tiketit " + tc2 + "§m----------");
									p.sendMessage("");
									p.sendMessage(tc2 + " Henkilökuntamme on avannut uudelleen käsittelyyn tikettisi " + tc1 + "#" + ticket + 
											tc2 + ".");
									p.sendMessage("");
									p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								}
								else {
									int i = core.getConfig().getInt("ticketinfo." + player.getName());
									core.getConfig().set("ticketinfo." + player.getName(), i - 1);
									core.saveConfig();
								}
							}
							else {
								player.sendMessage(tc3 + "Tämä tiketti on jo avoin!");
							}
						}
						else {
							player.sendMessage(tc3 + "Ei löydetty tikettiä antamallasi ID:llä!");
						}
					}
					else {
						player.performCommand("tiketit");
					}
				}
				else if (args[0].equalsIgnoreCase("reset") && ticket.equalsIgnoreCase("confirm")) {
					if (CoreUtils.hasRank(player, "ylläpitäjä")) {
						core.getConfig().set("tickets", null);
						core.getConfig().set("ticketinfo", null);
						core.getConfig().set("ticketcount", 0);
						core.saveConfig();
						player.sendMessage(tc2 + "Poistettiin kaikki tiketit!");
					}
				}
				else if (CoreUtils.hasRank(player, "valvoja")) {
					player.sendMessage(usage + "/tiketit <tiedot/tp/sulje/avaa> <ID>");
				}
				else {
					player.performCommand("tiketit");
				}
			}
			else if (args.length >= 1 && args[0].equalsIgnoreCase("reset") && CoreUtils.hasRank(player, "ylläpitäjä")) {
				player.sendMessage(tc2 + "Oletko aivan varma? Vahvista kaikkien tikettien poistaminen komennolla " + tc1 + 
						"/tiketit reset confirm" + tc2 + ".");
			}
			else if (args.length >= 1 && CoreUtils.hasRank(player, "valvoja")) {
				player.sendMessage(usage + "/tiketit <tiedot/tp/sulje/avaa> <ID>");
			}
			else {
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketit " + tc2 + "§m----------");
				player.sendMessage("");
				if (core.getConfig().getConfigurationSection("tickets") != null && 
						!core.getConfig().getConfigurationSection("tickets").getKeys(false).isEmpty()) {
					boolean openTickets = false;
					for (String ticket : core.getConfig().getConfigurationSection("tickets").getKeys(false)) {
						
						if (core.getConfig().getBoolean("tickets." + ticket + ".suljettu")) {
							continue;
						}
						
						String creator = core.getConfig().getString("tickets." + ticket + ".ilmoittaja");
						String message = core.getConfig().getString("tickets." + ticket + ".viesti");
						long time = core.getConfig().getLong("tickets." + ticket + ".aika");
						String timeCreated = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time + CoreUtils.TIME_OFFSET));
						
						if (CoreUtils.hasRank(player, "valvoja")) {
							TextComponent t1 = new TextComponent(tc1 + " - ");
							TextComponent t2 = new TextComponent(tc1 + "#" + ticket + ": " + tc2 + "§o" + message);
							t2.setColor(ChatColor.getByChar(tc2.charAt(1)));
							t2.setItalic(true);
							t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/tiketit tp " + ticket));
							t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder(tc1 + "Tekijä: " + tc2 + creator + "\n" + tc1 + "Tehty: " + tc2 + timeCreated + 
									"\n\n" + tc2 + "Teleporttaa klikkaamalla!").create()));
							t1.addExtra(t2);
							player.spigot().sendMessage(t1);
							openTickets = true;
						}
						else {
							TextComponent t1 = new TextComponent(tc1 + " - ");
							TextComponent t2 = new TextComponent(tc1 + "Tiketti #" + ticket + ": " + tc2 + message + " ");
							t2.setColor(ChatColor.getByChar(tc2.charAt(1)));
							TextComponent t3 = new TextComponent("[Sulje]");
							t3.setColor(ChatColor.RED);
							t3.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/tiketit sulje " + ticket));
							t3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder("§cSulje tämä tiketti klikkaamalla!").create()));
							t1.addExtra(t2);
							t1.addExtra(t3);
							player.spigot().sendMessage(t1);
							openTickets = true;
						}
					}
					if (!openTickets) {
						player.sendMessage(tc2 + " Ei avoimia tikettejä! :)");
					}
				}
				else {
					player.sendMessage(tc2 + " Ei avoimia tikettejä! :)");
				}
				player.sendMessage("");
			}
			return true;
		}
		
		// kaveri, kaverit, k, friend, friends, f
		
		if (cmd.getName().equalsIgnoreCase("kaveri") || cmd.getName().equalsIgnoreCase("kaverit") || 
				cmd.getName().equalsIgnoreCase("k") || cmd.getName().equalsIgnoreCase("friend") || 
				cmd.getName().equalsIgnoreCase("friends") || cmd.getName().equalsIgnoreCase("f")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						if (args[0].equalsIgnoreCase("lisää") || args[0].equalsIgnoreCase("add")) {
							if (args.length >= 2) {
								
								Player target = Bukkit.getPlayer(args[1]);
								List<String> friends = CoreUtils.getFriendsUuids(player.getName());
								
								if (target == null) {
									player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
									return;
								}
								if (target.equals(player)) {
									player.sendMessage(tc3 + "Et voi lähettää kaveripyyntöä itsellesi... :(");
									return;
								}
								if (friends.contains(target.getUniqueId().toString())) {
									player.sendMessage(tc3 + "Olet jo pelaajan " + tc4 + target.getName() + tc3 + " kaveri!");
									return;
								}
								if (friends.size() >= 20) {
									player.sendMessage(tc3 + "Olet jo saavuttanut kavereiden maksimimäärän!");
									return;
								}
								if (!SettingsUtils.getSetting(target, "show_friend_requests")) {
									player.sendMessage(tc4 + target.getName() + tc3 + " on asetuksissaan estänyt kaveripyynnöt!");
									return;
								}
								if (FriendRequest.hasSentRequestTo(player.getName(), target.getName())) {
									player.sendMessage(tc3 + "Olet jo lähettänyt kaveripyynnön tälle pelaajalle!");
									return;
								}
								
								player.sendMessage(tc2 + "Lähetit kaveripyynnön pelaajalle " + 
										tc1 + target.getName() + tc2 + "!");
								target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								target.sendMessage("§6§m----------------------------------------");
								target.sendMessage("§e " + player.getName() + " lähetti sinulle kaveripyynnön!");
								
								target.spigot().sendMessage(CoreUtils.getAcceptDeny(" ", "§e - ", 
										"§aHyväksy tämä kaveripyyntö klikkaamalla!", "§cHylkää tämä kaveripyyntö klikkaamalla!", 
										"/k hyväksy " + player.getName(), "/k hylkää " + player.getName()));
								
								target.sendMessage("§6§m----------------------------------------");
								
								FriendRequest request = new FriendRequest(player.getName(), target.getName());
								FriendRequest.getFriendRequests().add(request);
								
								new BukkitRunnable() {
									public void run() {
										if (FriendRequest.getFriendRequests().contains(request)) {
											FriendRequest.getFriendRequests().remove(request);
											player.sendMessage(tc3 + "Kaveripyyntösi pelaajalle " + tc4 + target.getName() + tc3 + 
													" on vanhentunut!");
											target.sendMessage(tc3 + "Pelaajan " + tc4 + player.getName() + tc3 + 
													" kaveripyyntö on vanhentunut!");
										}
									}
								}.runTaskLater(core, 400);
							}
							else {
								player.sendMessage(usage + "/k lisää <pelaaja>");
							}
						}
						else if (args[0].equalsIgnoreCase("hyväksy") || args[0].equalsIgnoreCase("accept")) {
							if (args.length >= 2) {
								List<FriendRequest> requests = FriendRequest.getFriendRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (FriendRequest request : requests) {
										String from = request.getFrom();
										if (from.equalsIgnoreCase(args[1])) {
											
											String fromUuid = CoreUtils.nameToUuid(from);
											String toUuid = player.getUniqueId().toString();
											
											List<String> fromFriends = CoreUtils.getFriendsUuids(from);
											List<String> toFriends = CoreUtils.getFriendsUuids(player.getName());
											
											if (fromFriends.contains(toUuid) || toFriends.contains(fromUuid)) {
												player.sendMessage(tc3 + "Olet jo tämän pelaajan kaveri!");
												return;
											}
											if (fromFriends.size() >= 20) {
												player.sendMessage(tc4 + from + tc3 + " on jo saavuttanut kavereiden maksimimäärän!");
												return;
											}
											if (toFriends.size() >= 20) {
												player.sendMessage(tc3 + "Olet jo saavuttanut kavereiden maksimimäärän!");
												return;
											}
											
											FriendRequest.getFriendRequests().remove(request);
											
											fromFriends.add(toUuid);
											toFriends.add(fromUuid);
											
											CoreUtils.setFriendsUuids(from, fromFriends);
											CoreUtils.setFriendsUuids(player.getName(), toFriends);
											
											player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
											player.sendMessage(tc2 + "Olet nyt pelaajan " + tc1 + from + tc2 + " kaveri!");
											
											Player target = Bukkit.getPlayer(from);
											if (target != null) {
												target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
												target.sendMessage(tc2 + "Olet nyt pelaajan " + tc1 + player.getName() + tc2 + " kaveri!");
											}
											
											return;
										}
									}
									player.sendMessage(tc3 + "Ei hyväksymättömiä kaveripyyntöjä tällä nimellä!");
								}
								else {
									player.sendMessage(tc3 + "Ei hyväksymättömiä kaveripyyntöjä tällä nimellä!");
								}
							}
							else {
								List<FriendRequest> requests = FriendRequest.getFriendRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (FriendRequest request : requests) {
										String from = request.getFrom();
										new BukkitRunnable() {
											public void run() {
												player.performCommand("k hyväksy " + from);
											}
										}.runTask(core);
									}
								}
								else {
									player.sendMessage(tc3 + "Sinulla ei ole hyväksymättömiä kaveripyyntöjä!");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("hylkää") || args[0].equalsIgnoreCase("deny")) {
							if (args.length >= 2) {
								List<FriendRequest> requests = FriendRequest.getFriendRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (FriendRequest request : requests) {
										String from = request.getFrom();
										if (from.equalsIgnoreCase(args[1])) {
											FriendRequest.getFriendRequests().remove(request);
											player.sendMessage(tc2 + "Hylättiin pelaajan " + tc1 + from + tc2 + " kaveripyyntö!");
											Player target = Bukkit.getPlayer(from);
											if (target != null) {
												target.sendMessage(tc4 + player.getName() + tc3 + " hylkäsi kaveripyyntösi!");
											}
											return;
										}
									}
									player.sendMessage(tc3 + "Ei hyväksymättömiä kaveripyyntöjä tällä nimellä!");
								}
								else {
									player.sendMessage(tc3 + "Ei hyväksymättömiä kaveripyyntöjä tällä nimellä!");
								}
							}
							else {
								List<FriendRequest> requests = FriendRequest.getFriendRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (FriendRequest request : requests) {
										String from = request.getFrom();
										new BukkitRunnable() {
											public void run() {
												player.performCommand("k hylkää " + from);
											}
										}.runTask(core);
									}
								}
								else {
									player.sendMessage(tc3 + "Sinulla ei ole hyväksymättömiä kaveripyyntöjä!");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
							List<String> friends = CoreUtils.getFriendsUuids(player.getName());
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Kaverit " + tc2 + "§m----------");
							player.sendMessage("");
							if (friends.isEmpty()) {
								player.sendMessage(tc3 + " Sinulla ei ole yhtäkään kaveria... :(");
							}
							else {
								if (friends.size() == 1) {
									player.sendMessage(tc2 + " Sinulla on yhteensä " + tc1 + "1" + tc2 + " kaveri!");
								}
								else {
									player.sendMessage(tc2 + " Sinulla on yhteensä " + tc1 + friends.size() + tc2 + " kaveria!");
								}
								player.sendMessage("");
								List<TextComponent> onlineFriends = new ArrayList<TextComponent>();
								List<TextComponent> offlineFriends = new ArrayList<TextComponent>();
								for (String uuid : friends) {
									MySQLResult infoData = MySQLUtils.get("SELECT name FROM player_info WHERE uuid=?", uuid);
									if (infoData != null) {
										String name = infoData.getString(0, "name");
										if (Bukkit.getPlayer(name) != null) {
											TextComponent t1 = new TextComponent("§a - ");
											TextComponent t2 = new TextComponent(name);
											t2.setColor(ChatColor.getByChar(tc2.charAt(1)));
											t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pelaaja " + name));
											t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
													new ComponentBuilder(tc2 + "Näytä pelaajan profiili klikkaamalla!").create()));
											TextComponent t3 = new TextComponent("§a§l∙");
											t3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
													new ComponentBuilder("§aTämä pelaaja on paikalla!").create()));
											t1.addExtra(t2);
											t1.addExtra(t3);
											onlineFriends.add(t1);
										}
										else {
											TextComponent t1 = new TextComponent("§c - ");
											TextComponent t2 = new TextComponent(name);
											t2.setColor(ChatColor.getByChar(tc2.charAt(1)));
											t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pelaaja " + name));
											t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
													new ComponentBuilder(tc2 + "Näytä pelaajan profiili klikkaamalla!").create()));
											TextComponent t3 = new TextComponent("§c§l∙");
											t3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
													new ComponentBuilder("§cTämä pelaaja ei ole paikalla!").create()));
											t1.addExtra(t2);
											t1.addExtra(t3);
											offlineFriends.add(t1);
										}
									}
								}
								for (TextComponent component : onlineFriends) {
									player.spigot().sendMessage(component);
								}
								for (TextComponent component : offlineFriends) {
									player.spigot().sendMessage(component);
								}
							}
							player.sendMessage("");
						}
						else if (args[0].equalsIgnoreCase("tp")) {
							if (args.length >= 2) {
								if (args[1].equalsIgnoreCase("hyväksy") || args[1].equalsIgnoreCase("accept")) {
									if (args.length >= 3) {
										List<TeleportRequest> requests = TeleportRequest.getTeleportRequestsTo(player.getName());
										if (!requests.isEmpty()) {
											for (TeleportRequest request : requests) {
												String from = request.getFrom();
												if (from.equalsIgnoreCase(args[2])) {
													
													TeleportRequest.getTeleportRequests().remove(request);
													
													Player target = Bukkit.getPlayer(from);
													if (target != null) {
														player.sendMessage(tc2 + "Hyväksyit pelaajan " + tc1 + from + tc2 + " teleporttauspyynnön!");
														target.sendMessage(tc1 + player.getName() + tc2 + " hyväksyi teleporttauspyyntösi! "
																+ "Pysy paikoillasi...");
														new BukkitRunnable() {
															public void run() {
																if (target.isOnline()) {
																	CoreUtils.teleport(target, player.getLocation());
																}
															}
														}.runTaskLater(core, 50);
													}
													else {
														player.sendMessage(tc4 + from + tc3 + " ei ole enää paikalla!");
													}
													
													return;
												}
											}
											player.sendMessage(tc3 + "Ei hyväksymättömiä teleporttauspyyntöjä tällä nimellä!");
										}
										else {
											player.sendMessage(tc3 + "Ei hyväksymättömiä teleporttauspyyntöjä tällä nimellä!");
										}
									}
									else {
										player.sendMessage(usage + "/k tp hyväksy <kaveri>");
									}
								}
								else if (args[1].equalsIgnoreCase("hylkää") || args[1].equalsIgnoreCase("deny")) {
									if (args.length >= 3) {
										List<TeleportRequest> requests = TeleportRequest.getTeleportRequestsTo(player.getName());
										if (!requests.isEmpty()) {
											for (TeleportRequest request : requests) {
												String from = request.getFrom();
												if (from.equalsIgnoreCase(args[2])) {
													TeleportRequest.getTeleportRequests().remove(request);
													player.sendMessage(tc2 + "Hylättiin pelaajan " + tc1 + from + tc2 + " teleporttauspyyntö!");
													Player target = Bukkit.getPlayer(from);
													if (target != null) {
														target.sendMessage(tc4 + player.getName() + tc3 + " hylkäsi teleporttauspyyntösi!");
													}
													return;
												}
											}
											player.sendMessage(tc3 + "Ei hyväksymättömiä teleporttauspyyntöjä tällä nimellä!");
										}
										else {
											player.sendMessage(tc3 + "Ei hyväksymättömiä teleporttauspyyntöjä tällä nimellä!");
										}
									}
									else {
										player.sendMessage(usage + "/k tp hylkää <kaveri>");
									}
								}
								else {
									
									Player target = Bukkit.getPlayer(args[1]);
									List<String> friends = CoreUtils.getFriendsUuids(player.getName());
									
									if (target == null) {
										player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
										return;
									}
									if (target.equals(player)) {
										player.sendMessage(tc3 + "Et voi lähettää teleporttauspyyntöä itsellesi... :(");
										return;
									}
									if (!friends.contains(target.getUniqueId().toString())) {
										player.sendMessage(tc4 + target.getName() + tc3 + " ei ole kaverisi!");
										return;
									}
									if (!SettingsUtils.getSetting(target, "show_teleport_requests")) {
										player.sendMessage(tc4 + target.getName() + tc3 + " on poistanut teleporttauspyynnöt käytöstä!");
										return;
									}
									if (TeleportRequest.hasSentRequestTo(player.getName(), target.getName())) {
										player.sendMessage(tc3 + "Olet jo lähettänyt teleporttauspyynnön tälle pelaajalle!");
										return;
									}
									
									player.sendMessage(tc2 + "Lähetit teleporttauspyynnön pelaajalle " + 
											tc1 + target.getName() + tc2 + "!");
									target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
									target.sendMessage("§6§m----------------------------------------");
									target.sendMessage("§e " + player.getName() + " haluaisi teleportata luoksesi.");
									
									target.spigot().sendMessage(CoreUtils.getAcceptDeny(" ", "§e - ", 
											"§aHyväksy tämä teleporttauspyyntö klikkaamalla!", "§cHylkää tämä teleporttauspyyntö klikkaamalla!", 
											"/k tp hyväksy " + player.getName(), "/k tp hylkää " + player.getName()));
									
									target.sendMessage("§6§m----------------------------------------");
									
									TeleportRequest request = new TeleportRequest(player.getName(), target.getName());
									TeleportRequest.getTeleportRequests().add(request);
									
									new BukkitRunnable() {
										public void run() {
											if (TeleportRequest.getTeleportRequests().contains(request)) {
												TeleportRequest.getTeleportRequests().remove(request);
												player.sendMessage(tc3 + "Teleporttauspyyntösi pelaajalle " + tc4 + target.getName() + tc3 + 
														" on vanhentunut!");
												target.sendMessage(tc3 + "Pelaajan " + tc4 + player.getName() + tc3 + 
														" teleporttauspyyntö on vanhentunut!");
											}
										}
									}.runTaskLater(core, 400);
								}
							}
							else {
								player.sendMessage(usage + "/k tp <kaveri>");
							}
						}
						else if (args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("remove")) {
							if (args.length >= 2) {
								MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[1]);
								if (infoData != null) {
									
									String name = infoData.getString(0, "name");
									String uuid = infoData.getString(0, "uuid");
									
									List<String> friends1 = CoreUtils.getFriendsUuids(player.getName());
									List<String> friends2 = CoreUtils.getFriendsUuids(name);
									
									if (friends1.contains(uuid) || friends2.contains(player.getUniqueId().toString())) {
										
										friends1.remove(uuid);
										friends2.remove(player.getUniqueId().toString());
										
										CoreUtils.setFriendsUuids(player.getName(), friends1);
										CoreUtils.setFriendsUuids(name, friends2);
										
										player.sendMessage(tc2 + "Poistettiin pelaaja " + tc1 + name + tc2 + " kavereistasi!");
										
										Player target = Bukkit.getPlayer(name);
										if (target != null) {
											target.sendMessage(tc4 + player.getName() + tc3 + " poisti sinut kavereistaan!");
										}
									}
									else {
										player.sendMessage(tc4 + name + tc3 + " ei ole kaverilistallasi!");
									}
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								}
							}
							else {
								player.sendMessage(usage + "/k poista <kaveri>");
							}
						}
						else {
							new BukkitRunnable() {
								public void run() {
									player.performCommand("k");
								}
							}.runTask(core);
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Kaverit " + tc2 + "§m----------");
				player.sendMessage("");
				player.sendMessage(tc1 + " /k lisää <pelaaja>" + tc2 + " - Lähetä kaveripyyntö pelaajalle");
				player.sendMessage(tc1 + " /k hyväksy" + tc2 + " - Hyväksy kaikki kaveripyyntösi");
				player.sendMessage(tc1 + " /k hylkää" + tc2 + " - Hylkää kaikki kaveripyyntösi");
				player.sendMessage(tc1 + " /k lista" + tc2 + " - Näytä kaikki kaverisi listassa");
				player.sendMessage(tc1 + " /k tp <kaveri>" + tc2 + " - Lähetä teleporttauspyyntö kaverillesi");
				player.sendMessage(tc1 + " /k poista <kaveri>" + tc2 + " - Poista pelaaja kavereistasi");
				player.sendMessage("");
			}
			return true;
		}
		
		// kc, gc
		
		if (cmd.getName().equalsIgnoreCase("kc") || cmd.getName().equalsIgnoreCase("gc")) {
			if (core.getConfig().getBoolean("users." + player.getName() + ".mute.muted")) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää tätä komentoa, sillä sinut on hiljennetty!");
				return true;
			}
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
								"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
						if (guildsData != null) {
							
							String uuid = player.getUniqueId().toString();
							String leader = guildsData.getString(0, "leader_uuid");
							List<String> members = CoreUtils.getGuildMembersUuids(guildsData);
							
							String prefix = "";
							if (uuid.equals(leader)) {
								prefix = "**";
							}
							else if (members.contains(uuid + ":UPSEERI")) {
								prefix = "*";
							}
							
							String message = "";
							for (String arg : args) {
								message += " " + arg;
							}
							message = message.trim();
							
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (leader.equals(p.getUniqueId().toString()) || members.toString().contains(p.getUniqueId().toString())) {
									p.sendMessage("§7[§9Kilta§7] " + prefix + player.getName() + "§9: " + message);
								}
							}
						}
						else {
							player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage(usage + "/kilta chat <viesti>");
			}
			return true;
		}
		
		// kilta, killat, guild, guilds, g
		
		if (cmd.getName().equalsIgnoreCase("kilta") || cmd.getName().equalsIgnoreCase("killat") || cmd.getName().equalsIgnoreCase("guild") || 
				cmd.getName().equalsIgnoreCase("guilds") || cmd.getName().equalsIgnoreCase("g")) {
			if (args.length >= 1) {
				new BukkitRunnable() {
					public void run() {
						if (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c")) {
							new BukkitRunnable() {
								public void run() {
									String arguments = "";
									for (int i = 1; i < args.length; i++) {
										arguments += " " + args[i];
									}
									player.performCommand("kc " + arguments);
								}
							}.runTask(core);
						}
						else if (args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
							player.sendMessage("");
							player.sendMessage(tc2 + "§m----------" + tc1 + " Killat " + tc2 + "§m----------");
							player.sendMessage("");
							MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds");
							if (guildsData != null) {
								for (int i = 0; i < guildsData.getRows(); i++) {
									String name = guildsData.getString(i, "guild_name");
									int members = CoreUtils.getGuildMembersUuids(guildsData).size() + 1;
									// TODO maksimimäärä
									TextComponent component = new TextComponent(tc1 + " - " + tc2 + name + " [" + members + "/20]");
									component.setColor(ChatColor.getByChar(tc2.charAt(1)));
									component.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kilta info " + name));
									player.spigot().sendMessage(component);
								}
							}
							else {
								player.sendMessage(tc3 + " Ei löydetty kiltoja! :(");
							}
							player.sendMessage("");
						}
						else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
							if (args.length == 1) {
								MySQLResult guildsData = MySQLUtils.get("SELECT guild_name FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								if (guildsData != null) {
									String name = guildsData.getString(0, "guild_name");
									new BukkitRunnable() {
										public void run() {
											player.performCommand("kilta info " + name);
										}
									}.runTask(core);
									return;
								}
								else {
									player.sendMessage(usage + "/kilta info <kilta>");
								}
							}
							else {
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE guild_name=?", args[1]);
								if (guildsData != null) {
									String name = guildsData.getString(0, "guild_name");
									String description = guildsData.getString(0, "guild_description");
									String leader = guildsData.getString(0, "leader_name");
									player.sendMessage("");
									player.sendMessage(tc2 + "§m----------" + tc1 + " Kilta: " + name + " " + tc2 + "§m----------");
									player.sendMessage("");
									player.sendMessage(tc2 + "§o " + description);
									player.sendMessage("");
									player.sendMessage(tc1 + " Johtaja: " + tc2 + leader);
									player.sendMessage("");
									List<String> officers = new ArrayList<String>();
									List<String> regulars = new ArrayList<String>();
									for (String member : CoreUtils.getGuildMembersUuids(guildsData)) {
										if (member.endsWith(":UPSEERI")) {
											officers.add(member.substring(0, member.length() - 8));
										}
										else if (member.endsWith(":DEFAULT")) {
											regulars.add(member.substring(0, member.length() - 8));
										}
									}
									String officersString = "";
									String regularsString = "";
									for (String uuid : officers) {
										officersString += " " + CoreUtils.uuidToName(uuid);
									}
									for (String uuid : regulars) {
										regularsString += " " + CoreUtils.uuidToName(uuid);
									}
									officersString = officersString.trim().replace(" ", ", ");
									regularsString = regularsString.trim().replace(" ", ", ");
									player.sendMessage(tc1 + " Upseerit: " + tc2 + officersString);
									player.sendMessage("");
									player.sendMessage(tc1 + " Jäsenet: " + tc2 + regularsString);
									player.sendMessage("");
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty kiltaa antamallasi nimellä!s");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("kutsu") || args[0].equalsIgnoreCase("invite")) {
							if (args.length >= 2) {
								
								Player target = Bukkit.getPlayer(args[1]);
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								
								if (guildsData != null) {
									
									String leader = guildsData.getString(0, "leader_uuid");
									List<String> members = CoreUtils.getGuildMembersUuids(guildsData);
									
									if (!leader.equals(player.getUniqueId().toString()) && 
											!members.contains(player.getUniqueId().toString() + ":UPSEERI")) {
										player.sendMessage(tc3 + "Sinulla ei ole tarvittavia oikeuksia kutsua jäseniä tähän kiltaan!");
										return;
									}
									if (target == null) {
										player.sendMessage(tc3 + "Kyseinen pelaaja ei ole paikalla!");
										return;
									}
									if (target.equals(player)) {
										player.sendMessage(tc3 + "Et voi lähettää kutsua itsellesi... :(");
										return;
									}
									if (leader.equals(target.getUniqueId().toString()) || members.toString().contains(target.getUniqueId().toString())) {
										player.sendMessage(tc4 + target.getName() + tc3 + " on jo killassasi!");
										return;
									}
									if (members.size() >= 20) { // TODO maksimimäärä
										player.sendMessage(tc3 + "Kiltasi on jo saavuttanut jäsenten maksimimäärän!");
										return;
									}
									if (!SettingsUtils.getSetting(target, "show_guild_requests")) {
										player.sendMessage(tc4 + target.getName() + tc3 + " on asetuksissaan estänyt kutsut kiltoihin!");
										return;
									}
									if (GuildRequest.hasSentRequestTo(player.getName(), target.getName())) {
										player.sendMessage(tc3 + "Olet jo lähettänyt kutsun tälle pelaajalle!");
										return;
									}
									
									player.sendMessage(tc2 + "Lähetit kutsun liittyä kiltaasi pelaajalle " + 
											tc1 + target.getName() + tc2 + "!");
									target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
									target.sendMessage("§6§m----------------------------------------");
									target.sendMessage("§e " + player.getName() + " lähetti sinulle kutsun kiltaansa!");
									
									target.spigot().sendMessage(CoreUtils.getAcceptDeny(" ", "§e - ", 
											"§aHyväksy tämä kutsu klikkaamalla!", "§cHylkää tämä kutsu klikkaamalla!", 
											"/kilta hyväksy " + player.getName(), "/kilta hylkää " + player.getName()));
									
									target.sendMessage("§6§m----------------------------------------");
									
									GuildRequest request = new GuildRequest(player.getName(), target.getName());
									GuildRequest.getGuildRequests().add(request);
									
									new BukkitRunnable() {
										public void run() {
											if (GuildRequest.getGuildRequests().contains(request)) {
												GuildRequest.getGuildRequests().remove(request);
												player.sendMessage(tc3 + "Kutsusi pelaajalle " + tc4 + target.getName() + tc3 + 
														" on vanhentunut!");
												target.sendMessage(tc3 + "Pelaajan " + tc4 + player.getName() + tc3 + 
														" kutsu on vanhentunut!");
											}
										}
									}.runTaskLater(core, 400);
								}
								else {
									player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta kutsu <pelaaja>");
							}
						}
						else if (args[0].equalsIgnoreCase("hyväksy") || args[0].equalsIgnoreCase("accept")) {
							if (args.length >= 2) {
								List<GuildRequest> requests = GuildRequest.getGuildRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (GuildRequest request : requests) {
										String from = request.getFrom();
										if (from.equalsIgnoreCase(args[1])) {
											
											MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
													"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
											
											if (guildsData == null) {
												
												MySQLResult guildData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
														"%" + player.getUniqueId().toString() + "%", CoreUtils.nameToUuid(from));
												
												String id = guildData.getString(0, "id");
												String name = guildData.getString(0, "guild_name");
												String leader = guildData.getString(0, "leader_uuid");
												List<String> members = CoreUtils.getGuildMembersUuids(guildData);
												
												members.add(player.getUniqueId().toString() + ":DEFAULT");
												
												CoreUtils.setGuildMembersUuids(id, members);
												
												GuildRequest.getGuildRequests().remove(request);
												
												for (Player p : Bukkit.getOnlinePlayers()) {
													if (members.toString().contains(p.getUniqueId().toString()) || 
															leader.equals(p.getUniqueId().toString())) {
														if (!p.getName().equals(player.getName())) {
															p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
															p.sendMessage(tc1 + player.getName() + tc2 + " liittyi kiltaan!");
														}
														else {
															p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
															p.sendMessage(tc2 + "Liityit kiltaan " + tc1 + name + tc2 + "!");
														}
													}
												}
											}
											else {
												player.sendMessage(tc3 + "Kuulut jo johonkin toiseen kiltaan! Sinun täytyy poistua nykyisestä "
														+ "killastasi voidaksesi liittyä uuteen!");
											}
											
											return;
										}
									}
									player.sendMessage(tc3 + "Ei hyväksymättömiä kiltakutsuja tällä nimellä!");
								}
								else {
									player.sendMessage(tc3 + "Ei hyväksymättömiä kiltakutsuja tällä nimellä!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta hyväksy <kutsun lähettäjä>");
							}
						}
						else if (args[0].equalsIgnoreCase("hylkää") || args[0].equalsIgnoreCase("deny")) {
							if (args.length >= 2) {
								List<GuildRequest> requests = GuildRequest.getGuildRequestsTo(player.getName());
								if (!requests.isEmpty()) {
									for (GuildRequest request : requests) {
										String from = request.getFrom();
										if (from.equalsIgnoreCase(args[1])) {
											GuildRequest.getGuildRequests().remove(request);
											player.sendMessage(tc2 + "Hylättiin pelaajan " + tc1 + from + tc2 + " kutsu!");
											Player target = Bukkit.getPlayer(from);
											if (target != null) {
												target.sendMessage(tc4 + player.getName() + tc3 + " hylkäsi kutsusi!");
											}
											return;
										}
									}
									player.sendMessage(tc3 + "Ei hyväksymättömiä kiltakutsuja tällä nimellä!");
								}
								else {
									player.sendMessage(tc3 + "Ei hyväksymättömiä kiltakutsuja tällä nimellä!");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("poistu") || args[0].equalsIgnoreCase("leave")) {
							
							MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
									"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
							
							if (guildsData != null) {
								
								String id = guildsData.getString(0, "id");
								String name = guildsData.getString(0, "guild_name");
								String leader = guildsData.getString(0, "leader_uuid");
								List<String> members = CoreUtils.getGuildMembersUuidsId(id);
								
								if (player.getUniqueId().toString().equals(leader)) {
									player.sendMessage(tc3 + "Et voi poistua killastasi, sillä olet sen johtaja! Voit joko poistaa kiltasi "
											+ "tai siirtää sen johtajuuden toiselle henkilölle.");
									return;
								}
								
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (members.toString().contains(p.getUniqueId().toString()) || leader.equals(p.getUniqueId().toString())) {
										if (!p.getName().equals(player.getName())) {
											p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
											p.sendMessage(tc1 + player.getName() + tc2 + " poistui killasta!");
										}
										else {
											p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
											p.sendMessage(tc2 + "Poistuit killasta " + tc1 + name + tc2 + "!");
										}
									}
								}
								
								ListIterator<String> iterator = members.listIterator();
								while (iterator.hasNext()) {
									String member = iterator.next();
									if (member.startsWith(player.getUniqueId().toString())) {
										iterator.remove();
									}
								}
								
								CoreUtils.setGuildMembersUuids(id, members);
							}
							else {
								player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
							}
						}
						else if (args[0].equalsIgnoreCase("perusta") || args[0].equalsIgnoreCase("create")) {
							if (args.length >= 2) {
								if (CoreUtils.hasRank(player, "ritari")) {
									
									MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds");
									if (guildsData != null) {
										for (int i = 0; i < guildsData.getRows(); i++) {
											
											String name = guildsData.getString(i, "guild_name");
											String leader = guildsData.getString(i, "leader_uuid");
											List<String> members = CoreUtils.getGuildMembersUuids(guildsData);
											
											if (leader.equals(player.getUniqueId().toString())) {
												player.sendMessage(tc3 + "Olet jo jonkun killan johtaja!");
												return;
											}
											else if (members.toString().contains(player.getUniqueId().toString())) {
												player.sendMessage(tc3 + "Kuulut jo johonkin kiltaan!");
												return;
											}
											else if (name.equalsIgnoreCase(args[1])) {
												player.sendMessage(tc3 + "Antamasi killan nimi on jo varattu");
												return;
											}
										}
									}
									
									if (args[1].length() <= 48) {
										MySQLUtils.set("INSERT INTO guilds (guild_name, guild_description, leader_name, leader_uuid, "
												+ "members) VALUES (?, ?, ?, ?, ?)", args[1], "<Ei kuvausta>", 
												player.getName(), player.getUniqueId().toString(), "");
										player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
										player.sendMessage(tc2 + "Perustettiin onnistuneesti uusi kilta " + tc1 + args[1] + tc2 + "!");
										player.sendMessage(tc2 + " Aseta killallesi kuvaus komennolla " + tc1 + "/kilta kuvaus");
										player.sendMessage(tc2 + " Kutsu jäseniä kiltaasi komennolla " + tc1 + "/kilta kutsu <pelaaja>");
									}
									else {
										player.sendMessage(tc3 + "Antamasi killan nimi on liian pitkä. Nimen maksimipituus on 48 merkkiä.");
									}
								}
								else {
									player.spigot().sendMessage(CoreUtils.getVipNeededMessage("luoda kiltoja"));
								}
							}
							else {
								player.sendMessage(usage + "/kilta perusta <killan nimi>");
							}
						}
						else if (args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("delete")) {
							
							MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
									"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
							
							if (guildsData != null) {
								
								String id = guildsData.getString(0, "id");
								String leader = guildsData.getString(0, "leader_uuid");
								List<String> members = CoreUtils.getGuildMembersUuidsId(id);
								
								if (!leader.equals(player.getUniqueId().toString())) {
									player.sendMessage(tc3 + "Vain killan johtaja voi poistaa killan!");
									return;
								}
								
								if (args.length >= 2 && args[1].equals("confirm")) {
									MySQLUtils.set("DELETE FROM guilds WHERE leader_uuid=?", leader);
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (members.toString().contains(p.getUniqueId().toString())) {
											p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 2);
											p.sendMessage(tc3 + "Kiltasi johtaja poisti killan lopullisesti!");
										}
									}
									player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 2);
									player.sendMessage(tc2 + "Poistettiin kilta onnistuneesti!");
								}
								else {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									TextComponent t = new TextComponent(tc4 + "§lVaroitus: " + tc3 + "Killan poistaminen poistaa sen lopullisesti! "
											+ "Mikäli olet aivan varma, että haluat poistaa kiltasi, klikkaa ");
									t.setColor(ChatColor.RED);
									TextComponent t2 = new TextComponent(tc3 + "[tästä]");
									t2.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kilta poista confirm"));
									t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
											new ComponentBuilder(tc3 + "Klikkaa tästä poistaaksesi kiltasi lopullisesti!").create()));
									t.addExtra(t2);
									t.addExtra(new TextComponent(tc3 + "!"));
									player.spigot().sendMessage(t);
								}
							}
							else {
								player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
							}
						}
						else if (args[0].equalsIgnoreCase("johtaja") || args[0].equalsIgnoreCase("leader")) {
							if (args.length >= 2) {
								
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								
								if (guildsData != null) {
									
									String id = guildsData.getString(0, "id");
									String leader = guildsData.getString(0, "leader_uuid");
									String members = guildsData.getStringNotNull(0, "members");
									
									if (!leader.equals(player.getUniqueId().toString())) {
										player.sendMessage(tc3 + "Sinä et ole tämän killan johtaja!");
										return;
									}
									
									MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[1]);
									if (infoData != null) {
										String name = infoData.getString(0, "name");
										String uuid = infoData.getString(0, "uuid");
										if (members.contains(uuid)) {
											if (CoreUtils.hasRankSQL(uuid, "ritari")) {
												members.replace(uuid, player.getUniqueId().toString());
												MySQLUtils.set("UPDATE guilds SET members=?, leader_name=?, leader_uuid=? WHERE id=?", 
														members, name, uuid, id);
												for (Player p : Bukkit.getOnlinePlayers()) {
													if (members.contains(p.getUniqueId().toString())) {
														p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
														p.sendMessage(tc1 + name + tc2 + " on nyt killan uusi johtaja!");
													}
													else if (p.getName().equals(name)) {
														p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
														p.sendMessage(tc2 + "Olet nyt killan uusi johtaja!");
													}
												}
											}
											else {
												player.sendMessage(tc3 + "Uuden johtajan täytyy olla arvoltaan vähintään Ritari!");
											}
										}
										else if (leader.equals(uuid)) {
											player.sendMessage(tc3 + "Et voi siirtää killan johtajuutta itsellesi!");
										}
										else {
											player.sendMessage(tc3 + "Kyseinen pelaaja ei ole tämän killan jäsen!");
										}
									}
									else {
										player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
									}
								}
								else {
									player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta johtaja <uusi johtaja>");
							}
						}
						else if (args[0].equalsIgnoreCase("nimi") || args[0].equalsIgnoreCase("rename")) {
							if (args.length >= 2) {
								
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								
								if (guildsData != null) {
									
									String id = guildsData.getString(0, "id");
									String leader = guildsData.getString(0, "leader_uuid");
									List<String> members = CoreUtils.getGuildMembersUuidsId(id);
									
									if (!leader.equals(player.getUniqueId().toString())) {
										player.sendMessage(tc3 + "Vain killan johtaja voi muuttaa killan nimeä!");
										return;
									}
									
									if (args[1].length() <= 48) {
										if (MySQLUtils.get("SELECT guild_name FROM guilds WHERE guild_name=?", args[1]) == null) {
											MySQLUtils.set("UPDATE guilds SET guild_name=? WHERE id=?", args[1], id);
											for (Player p : Bukkit.getOnlinePlayers()) {
												if (members.toString().contains(p.getUniqueId().toString())) {
													p.sendMessage(tc2 + "Kiltasi on nyt nimeltään " + tc1 + args[1] + tc2 + "!");
												}
											}
											player.sendMessage(tc2 + "Kiltasi on nyt nimeltään " + tc1 + args[1] + tc2 + "!");
										}
										else {
											player.sendMessage(tc3 + "Antamasi killan nimi on jo varattu!");
										}
									}
									else {
										player.sendMessage(tc3 + "Antamasi killan nimi on liian pitkä. Nimen maksimipituus on 48 merkkiä.");
									}
								}
								else {
									player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta nimi <uusi nimi>");
							}
						}
						else if (args[0].equalsIgnoreCase("kuvaus") || args[0].equalsIgnoreCase("description")) {
							if (args.length >= 2) {
								
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								
								if (guildsData != null) {
									
									String id = guildsData.getString(0, "id");
									String leader = guildsData.getString(0, "leader_uuid");
									List<String> members = CoreUtils.getGuildMembersUuidsId(id);
									
									if (!leader.equals(player.getUniqueId().toString())) {
										player.sendMessage(tc3 + "Vain killan johtaja voi muokata killan kuvausta!");
										return;
									}
									
									String description = "";
									for (int i = 1; i < args.length; i++) {
										description += " " + args[i];
									}
									description = description.trim();
									
									MySQLUtils.set("UPDATE guilds SET guild_description=? WHERE id=?", description, id);
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (members.toString().contains(p.getUniqueId().toString())) {
											p.sendMessage(tc2 + "Kiltasi johtaja päivitti killan kuvauksen!");
										}
									}
									player.sendMessage(tc2 + "Päivitettiin killan kuvaus!");
								}
								else {
									player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta kuvaus <kuvaus>");
							}
						}
						else if (args[0].equalsIgnoreCase("rooli") || args[0].equalsIgnoreCase("role")) {
							if (args.length >= 2) {
								if (args.length >= 3 && (args[2].equalsIgnoreCase("jäsen") || args[2].equalsIgnoreCase("upseeri"))) {
									
									MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
											"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
									
									if (guildsData != null) {
										
										String id = guildsData.getString(0, "id");
										String leader = guildsData.getString(0, "leader_uuid");
										List<String> members = CoreUtils.getGuildMembersUuidsId(id);
										
										if (!leader.equals(player.getUniqueId().toString())) {
											player.sendMessage(tc3 + "Sinulla ei ole tarvittavia oikeuksia muuttaa jäsenten rooleja!");
											return;
										}
										
										MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[1]);
										if (infoData != null) {
											String name = infoData.getString(0, "name");
											String uuid = infoData.getString(0, "uuid");
											if (members.toString().contains(uuid)) {
												
												ListIterator<String> iterator = members.listIterator();
												while (iterator.hasNext()) {
													String member = iterator.next();
													if (member.startsWith(uuid)) {
														if (args[2].equalsIgnoreCase("jäsen")) {
															iterator.set(uuid + ":DEFAULT");
															break;
														}
														else if (args[2].equalsIgnoreCase("upseeri")) {
															iterator.set(uuid + ":UPSEERI");
															break;
														}
														else {
															break;
														}
													}
												}
												
												for (Player p : Bukkit.getOnlinePlayers()) {
													if (members.toString().contains(p.getUniqueId().toString()) || 
															leader.equals(p.getUniqueId().toString())) {
														p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
														p.sendMessage(tc1 + name + tc2 + " on nyt " + tc1 + args[2].toLowerCase() + tc2 + "!");
													}
													else if (p.getName().equals(name)) {
														p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
														p.sendMessage(tc2 + "Roolisi on nyt " + tc1 + args[2].toLowerCase() + tc2 + "!");
													}
	 											}
												
												CoreUtils.setGuildMembersUuids(id, members);
											}
											else if (leader.equals(uuid)) {
												player.sendMessage(tc3 + "Et voi muuttaa omaa rooliasi! "
														+ "Jos haluat siirtää killan johtajuuden, käytä " + tc4 + "/kilta johtaja" + tc3 + ".");
											}
											else {
												player.sendMessage(tc3 + "Kyseinen pelaaja ei ole tämän killan jäsen!");
											}
										}
										else {
											player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
										}
									}
									else {
										player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
									}
								}
								else {
									player.sendMessage("");
									player.sendMessage(tc2 + "Käytettävissä olevat roolit:");
									player.sendMessage("");
									player.sendMessage(tc2 + " Jäsen (Oletusrooli)");
									player.sendMessage(tc2 + "  - §oVoi käyttää killan chattia");
									player.sendMessage("");
									player.sendMessage(tc2 + " Upseeri");
									player.sendMessage(tc2 + "  - §oVoi kutsua uusia jäseniä");
									player.sendMessage(tc2 + "  - §oVoi potkia pois jäseniä");
									player.sendMessage("");
									player.sendMessage(usage + "/kilta rooli <pelaaja> <rooli>");
								}
							}
							else {
								player.sendMessage(usage + "/kilta rooli <pelaaja> <rooli>");
							}
						}
						else if (args[0].equalsIgnoreCase("potki") || args[0].equalsIgnoreCase("kick")) {
							if (args.length >= 2) {
								
								MySQLResult guildsData = MySQLUtils.get("SELECT * FROM guilds WHERE members LIKE ? OR leader_uuid=?", 
										"%" + player.getUniqueId().toString() + "%", player.getUniqueId().toString());
								
								if (guildsData != null) {
									
									String id = guildsData.getString(0, "id");
									String leader = guildsData.getString(0, "leader_uuid");
									List<String> members = CoreUtils.getGuildMembersUuidsId(id);
									
									if (!leader.equals(player.getUniqueId().toString()) && 
											!members.contains(player.getUniqueId().toString() + ":UPSEERI")) {
										player.sendMessage(tc3 + "Sinulla ei ole tarvittavia oikeuksia potkia jäseniä tästä killasta!");
										return;
									}
									
									MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE name=?", args[1]);
									if (infoData != null) {
										String name = infoData.getString(0, "name");
										String uuid = infoData.getString(0, "uuid");
										if (members.toString().contains(uuid)) {
											
											if (members.contains(uuid + ":UPSEERI") && !leader.equals(player.getUniqueId().toString())) {
												player.sendMessage(tc3 + "Et voi potkia muita upseereita pois killasta!");
												return;
											}
											
											ListIterator<String> iterator = members.listIterator();
											while (iterator.hasNext()) {
												String member = iterator.next();
												if (member.startsWith(uuid)) {
													iterator.remove();
												}
											}
											
											for (Player p : Bukkit.getOnlinePlayers()) {
												if (members.toString().contains(p.getUniqueId().toString()) || 
														leader.equals(p.getUniqueId().toString())) {
													p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
													p.sendMessage(tc1 + name + tc2 + " potkittiin pois killasta!");
												}
												else if (p.getName().equals(name)) {
													p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
													p.sendMessage(tc3 + "Sinut potkittiin pois killasta!");
												}
 											}
											
											CoreUtils.setGuildMembersUuids(id, members);
										}
										else if (leader.equals(uuid)) {
											player.sendMessage(tc3 + "Et voi potkia killan johtajaa pois killasta!");
										}
										else {
											player.sendMessage(tc3 + "Kyseinen pelaaja ei ole tämän killan jäsen!");
										}
									}
									else {
										player.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
									}
								}
								else {
									player.sendMessage(tc3 + "Et kuulu mihinkään kiltaan!");
								}
							}
							else {
								player.sendMessage(usage + "/kilta potki <pelaaja>");
							}
						}
						else {
							new BukkitRunnable() {
								public void run() {
									player.performCommand("kilta");
								}
							}.runTask(core);
						}
					}
				}.runTaskAsynchronously(core);
			}
			else {
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Killat " + tc2 + "§m----------");
				player.sendMessage("");
				player.sendMessage(tc1 + " /kilta chat <viesti>" + tc2 + " tai " + tc1 + "/kc <viesti>" + tc2 + 
						" - Lähetä viesti oman kiltasi chattiin");
				player.sendMessage(tc1 + " /kilta lista" + tc2 + " - Näytä kaikki palvelimen killat");
				player.sendMessage(tc1 + " /kilta info [kilta]" + tc2 + " - Näytä tietoja killasta");
				player.sendMessage(tc1 + " /kilta hyväksy" + tc2 + " - Hyväksy uusin pyyntö liittyä kiltaan");
				player.sendMessage(tc1 + " /kilta hylkää" + tc2 + " - Hylkää uusin pyyntö liittyä kiltaan");
				player.sendMessage(tc1 + " /kilta poistu" + tc2 + " - Poistu nykyisestä killastasi");
				player.sendMessage(tc1 + " /kilta perusta <killan nimi>" + tc2 + " - Perusta uusi kilta");
				player.sendMessage(tc1 + " /kilta poista" + tc2 + " - Poista kiltasi");
				player.sendMessage(tc1 + " /kilta johtaja <pelaaja>" + tc2 + " - Siirrä killan johtajuus toiselle pelaajalle");
				player.sendMessage(tc1 + " /kilta nimi <uusi nimi>" + tc2 + " - Vaihda kiltasi nimeä");
				player.sendMessage(tc1 + " /kilta kuvaus <kuvaus>" + tc2 + " - Aseta kiltasi kuvaus");
				player.sendMessage(tc1 + " /kilta rooli <pelaaja> <rooli>" + tc2 + " - Muokkaa kiltasi jäsenten rooleja");
				player.sendMessage(tc1 + " /kilta kutsu <pelaaja>" + tc2 + " - Kutsu pelaaja kiltaasi");
				player.sendMessage(tc1 + " /kilta potki <pelaaja>" + tc2 + " - Potki pelaaja pois killastasi");
				player.sendMessage("");
			}
			return true;
		}
		
		// asetukset
		
		if (cmd.getName().equalsIgnoreCase("asetukset") || cmd.getName().equalsIgnoreCase("settings") || 
				cmd.getName().equalsIgnoreCase("options")) {
			if (args.length >= 1) {
				if (args.length >= 2 && args[0].equals("muuta")) {
					// TODO synkronoitu SQL-query...
					SettingsUtils.setSetting(player, args[1], !SettingsUtils.getSetting(player, args[1]));
					SettingsUtils.reloadSettings(player);
				}
				else {
					List<String> settings;
					if (args[0].equalsIgnoreCase("chat")) {
						settings = chatSettings;
					}
					else if (args[0].equalsIgnoreCase("profiili")) {
						settings = profileSettings;
					}
					else if (args[0].equalsIgnoreCase("muut")) {
						settings = miscSettings;
					}
					else {
						player.performCommand("asetukset");
						return true;
					}
					
					InventoryGUI settingsGui = new InventoryGUI(54, "Asetukset");
					
					settingsGui.addItem(CoreUtils.getItem(Material.ARROW, "§c« Takaisin päävalikkoon", null, 1), 49, new InventoryGUIAction() {
						public void onClickAsync() { }
						public void onClick() {
							player.performCommand("asetukset");
							player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
						}
					});
					
					String activatedPrefix = "§a";
					List<String> activatedLore = Arrays.asList("", "§a§l[✔]§7 Päällä");
					String deactivatedPrefix = "§c";
					List<String> deactivatedLore = Arrays.asList("", "§c§l[✖]§7 Pois päältä");
					
					for (String setting : settings) {
						try {
							
							String settingName = setting.split("§")[0];
							String settingId = setting.split("§")[1];
							int settingSlot = Integer.parseInt(setting.split("§")[2]);
							
							String finalName = deactivatedPrefix + settingName;
							List<String> finalLore = deactivatedLore;
							if (SettingsUtils.getSetting(player, settingId)) {
								finalName = activatedPrefix + settingName;
								finalLore = activatedLore;
							}
							
							settingsGui.addItem(CoreUtils.getItem(Material.NAME_TAG, finalName, finalLore, 1), settingSlot, new InventoryGUIAction() {
								public void onClickAsync() {
								}
								public void onClick() {
									player.performCommand("asetukset muuta " + settingId);
									player.performCommand("asetukset " + args[0]);
									player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
								}
							});
						}
						catch (NumberFormatException e) {
						}
						catch (ArrayIndexOutOfBoundsException e) {
						}
					}
					
					settingsGui.open(player);
				}
			}
			else {
				InventoryGUI settingsGui = new InventoryGUI(54, "Asetukset");
				settingsGui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§aChat & viestit", 
						Arrays.asList("", "§7Muokkaa chattiin ja", "§7viesteihin liittyviä", "§7asetuksia klikkaamalla", "§7tästä."), 1), 
						20, new InventoryGUIAction() {
					public void onClickAsync() { }
					public void onClick() {
						player.performCommand("asetukset chat");
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					}
				});
				settingsGui.addItem(CoreUtils.getSkull("§aProfiili", 
						Arrays.asList("", "§7Muokkaa omaan profiiliin", "§7liittyviä asetuksia", "§7klikkaamalla tästä."), player.getName()), 
						22, new InventoryGUIAction() {
					public void onClickAsync() { }
					public void onClick() {
						player.performCommand("asetukset profiili");
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					}
				});
				settingsGui.addItem(CoreUtils.getItem(Material.GOLDEN_APPLE, "§aMuut", 
						Arrays.asList("", "§7Muokkaa muita asetuksia", "§7klikkaamalla tästä."), 1), 
						24, new InventoryGUIAction() {
					public void onClickAsync() { }
					public void onClick() {
						player.performCommand("asetukset muut");
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					}
				});
				settingsGui.addItem(CoreUtils.getItem(Material.BARRIER, "§cSulje valikko", null, 1), 49, new InventoryGUIAction() {
					public void onClickAsync() { }
					public void onClick() {
						player.closeInventory();
						player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
					}
				});
				settingsGui.open(player);
			}
			return true;
		}
		
		return true;
	}
	
	public void updateTardisBlocks(String name) {
		if (name.equals("T4TU_")) {
			tardisBlocks1.clear();
		}
		if (name.equals("Ahishi")) {
			tardisBlocks2.clear();
		}
		if (name.equals("evokki0075")) {
			tardisBlocks3.clear();
		}
		Location location = CoreUtils.loadLocation(core, "tardis." + name + ".current-location");
		if (location != null) {
			for (int x = -1; x <= 1; x++) {
				for (int y = 0; y <= 2; y++) {
					for (int z = -1; z <= 1; z++) {
						location.add(x, y, z);
						if (name.equals("T4TU_")) {
							tardisBlocks1.add(location.clone());
						}
						if (name.equals("Ahishi")) {
							tardisBlocks2.add(location.clone());
						}
						if (name.equals("evokki0075")) {
							tardisBlocks3.add(location.clone());
						}
						location.subtract(x, y, z);
					}
				}
			}
		}
	}
	
	public void updateStableSigns() {
		if (core.getConfig().getConfigurationSection("stables") != null) {
			for (String id : core.getConfig().getConfigurationSection("stables").getKeys(false)) {
				if (core.getConfig().getConfigurationSection("stables." + id + ".signs") != null) {
					for (String identifier : core.getConfig().getConfigurationSection("stables." + id + ".signs").getKeys(false)) {
						Location location = CoreUtils.loadLocation(core, "stables." + id + ".signs." + identifier);
						if (location != null && location.getBlock() != null && location.getBlock().getState() instanceof Sign) {
							Sign sign = (Sign) location.getBlock().getState();
							sign.setLine(0, "§8[Hevostalli]");
							sign.setLine(1, "");
							sign.setLine(3, "");
							if (core.getConfig().getBoolean("stables." + id + ".in-use")) {
								sign.setLine(2, core.getConfig().getString("stables." + id + ".name"));
							}
							else {
								sign.setLine(2, "§4Vapaa...");
							}
							sign.update();
						}
					}
				}
			}
		}
	}
	
	private List<String> chatSettings = Arrays.asList(
			"Näytä pelaajien chat-viestit§show_chat§10", 
			"Soita ääni, kun nimeni mainitaan chatissa§play_sound_mentioned§11", 
			"Vastaanota yksityisviestejä pelaajilta§show_msg§12", 
			"Soita ääni, kun saan yksityisviestin§play_sound_msg§13", 
			"Vastaanota kaveripyyntöjä pelaajilta§show_friend_requests§14", 
			"Soita ääni, kun kaverini liittyy palvelimelle§play_sound_friends§15", 
			"Vastaanota teleporttauspyyntöjä kavereiltasi§show_teleport_requests§16", 
			"Vastaanota kiltapyyntöjä pelaajilta§show_guild_requests§19", 
			"Näytä viesti, kun minulta ostetaan tuotteita§show_bought_items§20", 
			"Näytä pelaajien kuolinilmoitukset§show_death_messages§21", 
			"Näytä muiden pelaajien AFK-ilmoitukset§show_afk§22", 
			"Huomauta, jos chat-viestissä mainitsemani pelaaja on AFK§show_afk_chat_notification§23", 
			"Näytä kavereideni tilaviestien päivitykset§show_friend_status§24"
			);
	
	private List<String> profileSettings = Arrays.asList(
			""
			);
	
	private List<String> miscSettings = Arrays.asList(
			"Istu tuoleille, kun klikkaan niitä§use_chairs§10", 
			"Näytä tietoa lukituista kohteistani, kun klikkaan niitä§show_lock_info§11"
			);
	
	private List<String> banPunishments = Arrays.asList(
			"Tämä on testi 1§ban <user> testi1", 
			"Tämä on testi 2§ban <user> testi2"
			);
	
	private List<String> jailPunishments = Arrays.asList(
			"Tämä on testi 1§jail <user> testi1", 
			"Tämä on testi 2§jail <user> testi2"
			);
	
	private List<String> mutePunishments = Arrays.asList(
			"Tämä on testi 1§mute <user> testi1", 
			"Tämä on testi 2§mute <user> testi2"
			);
}