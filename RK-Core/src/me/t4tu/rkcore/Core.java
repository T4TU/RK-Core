package me.t4tu.rkcore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.commands.CoreCommands;
import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.listeners.ChatListener;
import me.t4tu.rkcore.listeners.CoreListener;
import me.t4tu.rkcore.punishments.PunishmentCommands;
import me.t4tu.rkcore.tutorials.Tutorial;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Core extends JavaPlugin {
	
	private CoreListener coreListener;
	private ChatListener chatListener;
	private CoreCommands coreCommands;
	private PunishmentCommands punishmentCommands;
	private Tutorial tutorial;
	private List<InventoryGUI> guis = new ArrayList<InventoryGUI>();
	private long ingameTime;
	private boolean timeHalted;
	
	@Override
	public void onEnable() {
		
		loadConfiguration();
		
		CoreUtils.setCore(this);
		SettingsUtils.setCore(this);
		//ReflectionUtils.setCore(this);
		InventoryGUI.setCore(this);
		ReflectionUtils.loadScoreboardTeams();
		MySQLUtils.setCore(this);
		if (MySQLUtils.openConnection()) {
			// TODO printtaa onnistuminen consoleen
		}
		else {
			// TODO automaattisesti huoltotilaan
		}
		MySQLUtils.startCounterClock();
		
		coreListener = new CoreListener(this);
		chatListener = new ChatListener(this);
		coreCommands = new CoreCommands(this);
		punishmentCommands = new PunishmentCommands(this);
		tutorial = new Tutorial(this);
		
		registerCoreCommands(coreCommands);
		registerPunishmentCommands(punishmentCommands);
		
		coreListener.setMaintenanceMode(getConfig().getBoolean("maintenance-mode"));
		
		ingameTime = getConfig().getLong("time");
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			CoreUtils.updatePermissions(player);
		}
		
		loops();
		
		createWorlds();
	}
	
	@Override
	public void onDisable() {
		
		MySQLUtils.closeConnection();
		
		for (String name : coreCommands.getPermissions().keySet()) {
			if (Bukkit.getPlayer(name) != null) {
				Bukkit.getPlayer(name).removeAttachment(coreCommands.getPermissions().get(name));
			}
		}
		
		for (InventoryGUI gui : guis) {
			gui.destroy();
		}
		
		getConfig().set("time", ingameTime);
	}
	
	public CoreListener getCoreListener() {
		return coreListener;
	}
	
	public ChatListener getChatListener() {
		return chatListener;
	}
	
	public CoreCommands getCoreCommands() {
		return coreCommands;
	}
	
	public PunishmentCommands getPunishmentCommands() {
		return punishmentCommands;
	}
	
	public Tutorial getTutorial() {
		return tutorial;
	}
	
	public List<InventoryGUI> getInventoryGuis() {
		return guis;
	}
	
	public long getIngameTime() {
		return ingameTime;
	}
	
	public boolean isTimeHalted() {
		return timeHalted;
	}
	
	public void setTimeHalted(boolean halted) {
		timeHalted = halted;
	}
	
	private void loadConfiguration() {
		setConfigurationDefaultString("colors.base", "&7");
		setConfigurationDefaultString("colors.highlight", "&a");
		setConfigurationDefaultString("colors.error-base", "&c");
		setConfigurationDefaultString("colors.error-highlight", "&4");
		setConfigurationDefaultString("messages.usage", "&cKomennon käyttö: &4");
		setConfigurationDefaultString("messages.no-permission", "&cTuota komentoa ei ole olemassa, tai sinulla ei ole oikeutta siihen. Saat apua kirjoittamalla komennon §4/apua§c.");
		setConfigurationDefaultString("messages.players-only", "&cTätä komentoa ei voi käyttää konsolista!");
		setConfigurationDefaultString("mysql.host", "host/database");
		setConfigurationDefaultString("mysql.username", "username");
		setConfigurationDefaultString("mysql.password", "password");
		setConfigurationDefaultBoolean("maintenance-mode", true);
		setConfigurationDefaultInt("time", 0);
		setConfigurationDefaultInt("ticketcount", 0);
		saveConfig();
	}
	
	private void setConfigurationDefaultString(String path, String value) {
		if (getConfig().getString(path) == null) {
			getConfig().set(path, value);
		}
	}
	
	private void setConfigurationDefaultBoolean(String path, boolean value) {
		if (getConfig().getString(path) == null) {
			getConfig().set(path, value);
		}
	}
	
	private void setConfigurationDefaultInt(String path, int value) {
		if (getConfig().getString(path) == null) {
			getConfig().set(path, value);
		}
	}
	
	private void registerCoreCommands(CoreCommands commands) {
		commands.registerCommand("reconnect", false);
		commands.registerCommand("updateplayerdata", false);
		commands.registerCommand("playerlookup", false);
		commands.registerCommand("kickall", false);
		commands.registerCommand("huolto", false);
		commands.registerCommand("xstop", false);
		commands.registerCommand("autorestart", false);
		commands.registerCommand("config", false);
		commands.registerCommand("sql", false);
		commands.registerCommand("list", true);
		commands.registerCommand("who", true);
		commands.registerCommand("pelaajat", true);
		commands.registerCommand("alert", false);
		commands.registerCommand("send", false);
		commands.registerCommand("chat", false);
		commands.registerCommand("perm", false);
		commands.registerCommand("group", false);
		commands.registerCommand("history", false);
		commands.registerCommand("halttime", false);
		commands.registerCommand("give", false);
		commands.registerCommand("effect", false);
		commands.registerCommand("sudo", false);
		commands.registerCommand("sudochat", false);
		commands.registerCommand("setnews", false);
		commands.registerCommand("vanhentuneetsakot", false);
		commands.registerCommand("clearfire", false);
		commands.registerCommand("debug", false);
		commands.registerCommand("ping", true);
		commands.registerCommand("lag", true);
		commands.registerCommand("etsi", false);
		commands.registerCommand("find", false);
		commands.registerCommand("search", false);
		commands.registerCommand("pelaaja", true);
		commands.registerCommand("profiili", true);
		commands.registerCommand("player", true);
		commands.registerCommand("profile", true);
		commands.registerCommand("p", true);
		commands.registerCommand("help", true);
		commands.registerCommand("apua", true);
		commands.registerCommand("?", true);
		commands.registerCommand("säännöt", true);
		commands.registerCommand("rules", true);
		commands.registerCommand("s", false);
		commands.registerCommand("sa", false);
		commands.registerCommand("a", true);
		commands.registerCommand("helpop", true);
		commands.registerCommand("henkilökunta", true);
		commands.registerCommand("staff", true);
		commands.registerCommand("viesti", true);
		commands.registerCommand("msg", true);
		commands.registerCommand("tell", true);
		commands.registerCommand("w", true);
		commands.registerCommand("t", true);
		commands.registerCommand("m", true);
		commands.registerCommand("vastaa", true);
		commands.registerCommand("r", true);
		commands.registerCommand("posti", true);
		commands.registerCommand("mail", true);
		commands.registerCommand("afk", true);
		commands.registerCommand("uutiset", true);
		commands.registerCommand("news", true);
		commands.registerCommand("motd", true);
		commands.registerCommand("tilaviesti", true);
		commands.registerCommand("status", true);
		commands.registerCommand("kompassi", true);
		commands.registerCommand("lähellä", true);
		commands.registerCommand("near", true);
		commands.registerCommand("spawn", true);
		commands.registerCommand("hub", true);
		commands.registerCommand("lobby", true);
		commands.registerCommand("asetaspawn", false);
		commands.registerCommand("setspawn", false);
		commands.registerCommand("koti", true);
		commands.registerCommand("home", true);
		commands.registerCommand("asetakoti", true);
		commands.registerCommand("sethome", true);
		commands.registerCommand("poistakoti", true);
		commands.registerCommand("delhome", true);
		commands.registerCommand("matkusta", true);
		commands.registerCommand("warp", true);
		commands.registerCommand("setwarp", false);
		commands.registerCommand("delwarp", false);
		commands.registerCommand("swarp", false);
		commands.registerCommand("setswarp", false);
		commands.registerCommand("delswarp", false);
		commands.registerCommand("spy", false);
		commands.registerCommand("comspy", false);
		commands.registerCommand("vanish", false);
		commands.registerCommand("poof", false);
		commands.registerCommand("god", false);
		commands.registerCommand("fly", false);
		commands.registerCommand("gamemode", false);
		commands.registerCommand("gm", false);
		commands.registerCommand("heal", false);
		commands.registerCommand("feed", false);
		commands.registerCommand("back", false);
		commands.registerCommand("top", false);
		commands.registerCommand("clear", false);
		commands.registerCommand("ci", false);
		commands.registerCommand("invsee", false);
		commands.registerCommand("tp", false);
		commands.registerCommand("tpo", false);
		commands.registerCommand("tphere", false);
		commands.registerCommand("tpohere", false);
		commands.registerCommand("tppos", false);
		commands.registerCommand("noafk", false);
		commands.registerCommand("nick", false);
		commands.registerCommand("fix", false);
		commands.registerCommand("pt", false);
		commands.registerCommand("thor", false);
		commands.registerCommand("entity", false);
		commands.registerCommand("mob", false);
		commands.registerCommand("killall", false);
		commands.registerCommand("xp", false);
		commands.registerCommand("xpl", false);
		commands.registerCommand("portti", false);
		commands.registerCommand("maailma", false);
		commands.registerCommand("world", false);
		commands.registerCommand("holo", false);
		commands.registerCommand("tutorial", false);
		commands.registerCommand("rankaise", false);
		commands.registerCommand("h", false);
		commands.registerCommand("huomautus", false);
		commands.registerCommand("note", false);
		commands.registerCommand("sakko", true);
		commands.registerCommand("tiketti", true);
		commands.registerCommand("grief", true);
		commands.registerCommand("ticket", true);
		commands.registerCommand("tiketit", true);
		commands.registerCommand("griefs", true);
		commands.registerCommand("tickets", true);
		commands.registerCommand("kaveri", true);
		commands.registerCommand("kaverit", true);
		commands.registerCommand("k", true);
		commands.registerCommand("friend", true);
		commands.registerCommand("friends", true);
		commands.registerCommand("f", true);
		commands.registerCommand("kc", true);
		commands.registerCommand("gc", true);
		commands.registerCommand("kilta", true);
		commands.registerCommand("killat", true);
		commands.registerCommand("guild", true);
		commands.registerCommand("guilds", true);
		commands.registerCommand("g", true);
		commands.registerCommand("asetukset", true);
		commands.registerCommand("settings", true);
		commands.registerCommand("options", true);
	}
	
	private void registerPunishmentCommands(PunishmentCommands commands) {
		commands.registerCommand("kick", false);
		commands.registerCommand("mute", false);
		commands.registerCommand("tempmute", false);
		commands.registerCommand("unmute", false);
		commands.registerCommand("jail", false);
		commands.registerCommand("tempjail", false);
		commands.registerCommand("unjail", false);
		commands.registerCommand("ban", false);
		commands.registerCommand("tempban", false);
		commands.registerCommand("unban", false);
		commands.registerCommand("ipban", false);
		commands.registerCommand("ipunban", false);
		commands.registerCommand("showlog", false);
		commands.registerCommand("setjail", false);
	}
	
	private void loops() {
		
		new BukkitRunnable() {
			World world = Bukkit.getWorlds().get(0);
			public void run() {
				ingameTime += 50 * CoreUtils.INGAME_TIME_SPEED_MULTIPLIER;
				if (!timeHalted) {
					long millisSinceStartOfDay = CoreUtils.getMillisecondsFromStartOfDay(ingameTime) - 21600000;
					if (millisSinceStartOfDay <= 0) {
						millisSinceStartOfDay = 86400000 + millisSinceStartOfDay;
					}
					float time = millisSinceStartOfDay / 72f / 1000f * 20f;
					world.setTime((long) time);
				}
			}
		}.runTaskTimer(this, 0, 1);
		
		new BukkitRunnable() {
			public void run() {
				
				// aika configiin
				
				getConfig().set("time", ingameTime);
				saveConfig();
				
				// vangitut pelajaat
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					long expires = getConfig().getLong("users." + player.getName() + ".jail.duration");
					if (expires != 0) {
						if (System.currentTimeMillis() >= expires) {
							getConfig().set("users." + player.getName() + ".jail", null);
							saveConfig();
							punishmentCommands.releaseFromJail(player);
							new BukkitRunnable() {
								public void run() {
									MySQLUtils.set("DELETE FROM player_jail WHERE uuid=?", player.getUniqueId().toString().replace("-", ""));
								}
							}.runTaskAsynchronously(Core.this);
						}
					}
				}
				
				// hiljennetyt pelajaat
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					long expires = getConfig().getLong("users." + player.getName() + ".mute.duration");
					if (expires != 0) {
						if (System.currentTimeMillis() >= expires) {
							getConfig().set("users." + player.getName() + ".mute", null);
							saveConfig();
							punishmentCommands.sendUnmuteInfo(player);
							new BukkitRunnable() {
								public void run() {
									MySQLUtils.set("DELETE FROM player_mute WHERE uuid=?", player.getUniqueId().toString().replace("-", ""));
								}
							}.runTaskAsynchronously(Core.this);
						}
					}
				}
			}
		}.runTaskTimer(this, 100, 100);
		
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					
					// debug
					
					if (coreCommands.getDebugPlayers().contains(player.getName())) {
						@SuppressWarnings("deprecation")
						double tps = BigDecimal.valueOf(ReflectionUtils.getTPS()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
						TextComponent message = new TextComponent("§7TPS: §e" + tps + "§7 | Ping: §e" + ReflectionUtils.getPing(player) 
								+ "ms§7 | Pelaajat: §e" + Bukkit.getOnlinePlayers().size() + "§7/§e" + Bukkit.getMaxPlayers() + "§7 | SQL: §e" 
								+ MySQLUtils.getQueriesPerMinute() + "§7/§emin");
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
					}
					
					// afk
					
					int current = 0;
					if (CoreUtils.getAfkCounter().containsKey(player.getName())) {
						current = CoreUtils.getAfkCounter().get(player.getName());
					}
					if (current != -1 && !CoreUtils.getHaltAfkCounter().contains(player.getName())) {
						if (current == 120) {
							current = -2;
						}
						CoreUtils.setAfkCounter(player, current + 1);
					}
				}
			}
		}.runTaskTimer(this, 0, 20);
		
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					
					// kompassi
					
					if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS || 
							player.getInventory().getItemInOffHand().getType() == Material.COMPASS) {
						if (!player.getCompassTarget().equals(player.getWorld().getSpawnLocation())) {
							double d = Math.sqrt(Math.pow(player.getLocation().getX() - player.getCompassTarget().getX(), 2) + 
									Math.pow(player.getLocation().getZ() - player.getCompassTarget().getZ(), 2));
							TextComponent component = new TextComponent("§a§lx: §a§o" + player.getCompassTarget().getBlockX() + 
									"§a§l z: §a§o" + player.getCompassTarget().getBlockZ() + 
									"§a§l Etäisyys: §a§o" + BigDecimal.valueOf(d).setScale(0, RoundingMode.HALF_UP).intValue());
							player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
						}
						else {
							TextComponent component = new TextComponent("§cAseta kompassin kohde komennolla §4/kompassi");
							player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
						}
					}
					
					// kello
					
					if (player.getInventory().getItemInMainHand().getType() == Material.WATCH || 
							player.getInventory().getItemInOffHand().getType() == Material.WATCH) {
						TextComponent component = new TextComponent(CoreUtils.getFriendlyTimeString(ingameTime));
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
					}
				}
			}
		}.runTaskTimer(this, 0, 10);
		
		new BukkitRunnable() {
			public void run() {
				
				// online-pelaajat
				
				MySQLUtils.set("UPDATE global SET count=" + Bukkit.getOnlinePlayers().size());
			}
		}.runTaskTimerAsynchronously(this, 100, 100);
		
		new BukkitRunnable() {
			public void run() {
				
				// tiketit
				
				int counter = 0;
				if (getConfig().getConfigurationSection("tickets") == null) {
					return;
				}
				for (String s : getConfig().getConfigurationSection("tickets").getKeys(false)) {
					if (!getConfig().getBoolean("tickets." + s + ".suljettu")) {
						counter++;
					}
				}
				if (counter > 0) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (CoreUtils.hasRank(player, "valvoja")) {
							player.sendMessage("");
							player.sendMessage(" §6§lHuomio! §eYhteensä §6" + counter + "§e tikettiä on hoitamatta!");
							player.sendMessage("");
						}
					}
				}
			}
		}.runTaskTimer(this, 200, 12000);
		
		new BukkitRunnable() {
			int i = 0;
			int stay = 0;
			public void run() {
				String s = header.get(i);
				
				if (stay <= 0) {
					i++;
					if (i >= header.size()) {
						i = 0;
					}
					if (i == 0 || i == 14) {
						stay = 20;
					}
				}
				else {
					stay--;
				}
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					ReflectionUtils.sendTabHeaderFooterPacket(player, "{\"text\":\"§f\n" + s + "\n\"}", 
							"{\"text\":\"§f\n§a RoyalKingdom.net9.fi \n\"}"); // TODO
				}
			}
		}.runTaskTimer(this, 20, 2);
	}
	
	private void createWorlds() {
		if (getConfig().getConfigurationSection("worlds") != null) {
			if (!getConfig().getConfigurationSection("worlds").getKeys(false).isEmpty()) {
				for (String worldName : getConfig().getConfigurationSection("worlds").getKeys(false)) {
					String type = getConfig().getString("worlds." + worldName);
					WorldCreator worldCreator = new WorldCreator(worldName);
					worldCreator.environment(Environment.valueOf(type));
					Bukkit.createWorld(worldCreator);
				}
			}
		}
	}
	
	private List<String> header = Arrays.asList(
			"§a§lRoyal Kingdom", 
			"§6§lR§a§loyal Kingdom", 
			"§e§lR§6§lo§a§lyal Kingdom", 
			"§e§lRo§6§ly§a§lal Kingdom", 
			"§e§lRoy§6§la§a§ll Kingdom", 
			"§e§lRoya§6§ll§a§l Kingdom", 
			"§e§lRoyal§a§l Kingdom", 
			"§e§lRoyal §6§lK§a§lingdom", 
			"§e§lRoyal K§6§li§a§lngdom", 
			"§e§lRoyal Ki§6§ln§a§lgdom", 
			"§e§lRoyal Kin§6§lg§a§ldom", 
			"§e§lRoyal King§6§ld§a§lom", 
			"§e§lRoyal Kingd§6§lo§a§lm", 
			"§e§lRoyal Kingdo§6§lm", 
			"§e§lRoyal Kingdom", 
			"§6§lR§e§loyal Kingdom", 
			"§a§lR§6§lo§e§lyal Kingdom", 
			"§a§lRo§6§ly§e§lal Kingdom", 
			"§a§lRoy§6§la§e§ll Kingdom", 
			"§a§lRoya§6§ll§e§l Kingdom", 
			"§a§lRoyal§e§l Kingdom", 
			"§a§lRoyal §6§lK§e§lingdom", 
			"§a§lRoyal K§6§li§e§lngdom", 
			"§a§lRoyal Ki§6§ln§e§lgdom", 
			"§a§lRoyal Kin§6§lg§e§ldom", 
			"§a§lRoyal King§6§ld§e§lom", 
			"§a§lRoyal Kingd§6§lo§e§lm", 
			"§a§lRoyal Kingdo§6§lm"
			);
}