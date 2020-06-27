package me.t4tu.rkcore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.t4tu.rkcore.commands.CoreCommands;
import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.listeners.ChatListener;
import me.t4tu.rkcore.listeners.CoreListener;
import me.t4tu.rkcore.parties.PartyManager;
import me.t4tu.rkcore.punishments.PunishmentCommands;
import me.t4tu.rkcore.statistics.Statistic;
import me.t4tu.rkcore.statistics.StatisticsEntry;
import me.t4tu.rkcore.statistics.StatisticsManager;
import me.t4tu.rkcore.statistics.StatisticsViewer;
import me.t4tu.rkcore.tutorials.Tutorial;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MapAnimationManager;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class Core extends JavaPlugin {
	
	private CoreListener coreListener;
	private ChatListener chatListener;
	private CoreCommands coreCommands;
	private PunishmentCommands punishmentCommands;
	private PartyManager partyManager;
	private Tutorial tutorial;
	private MapAnimationManager mapAnimationManager;
	private StatisticsManager statisticsManager;
	private StatisticsViewer statisticsViewer;
	private Map<String, Long> ontimes = new HashMap<String, Long>();
	private long ingameTime;
	private boolean timeHalted = false;
	private int sleepCounter = 0;
	private boolean noSQL = false;
	
	@Override
	public void onEnable() {
		
		loadConfiguration();
		
		CoreUtils.setCore(this);
		SettingsUtils.setCore(this);
		InventoryGUI.setCore(this);
		ReflectionUtils.loadScoreboardTeams();
		MySQLUtils.setCore(this);
		if (!MySQLUtils.openConnection()) {
			noSQL = true;
			Bukkit.getConsoleSender().sendMessage("Virhe luodessa yhteyttä MySQL-tietokantaan! Huoltotila on asetettu päälle, ja plugin toimii rajoittuneessa tilassa.");
		}
		MySQLUtils.startCounterClock();
		
		coreListener = new CoreListener(this);
		chatListener = new ChatListener(this);
		coreCommands = new CoreCommands(this);
		punishmentCommands = new PunishmentCommands(this);
		partyManager = new PartyManager();
		tutorial = new Tutorial(this);
		mapAnimationManager = new MapAnimationManager(this);
		statisticsManager = new StatisticsManager(this);
		statisticsViewer = new StatisticsViewer(this);
		
		registerCoreCommands(coreCommands);
		registerPunishmentCommands(punishmentCommands);
		addStaffCommands();
		
		addCustomRecipes();
		
		if (noSQL) {
			coreListener.setMaintenanceMode(true);
		}
		else {
			coreListener.setMaintenanceMode(getConfig().getBoolean("maintenance-mode"));
		}
		
		ingameTime = getConfig().getLong("time");
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			CoreUtils.updatePermissions(player);
		}
		
		loops();
		
		createWorlds();
		
		coreCommands.updateTardisBlocks("T4TU_");
		coreCommands.updateTardisBlocks("Ahishi");
		coreCommands.updateTardisBlocks("evokki0075");
	}
	
	@Override
	public void onDisable() {
		
		if (!noSQL) {
			for (String uuid : ontimes.keySet()) {
				MySQLResult infoData = MySQLUtils.get("SELECT seconds FROM player_info WHERE uuid=?", uuid);
				if (infoData != null) {
					long seconds = infoData.getLong(0, "seconds");
					seconds += ontimes.get(uuid);
					MySQLUtils.set("UPDATE player_info SET seconds=? WHERE uuid=?", "" + seconds, uuid);
				}
			}
			ontimes.clear();
			statisticsManager.saveCacheToDatabase();
			MySQLUtils.closeConnection();
		}
		
		for (String name : coreCommands.getPermissions().keySet()) {
			if (Bukkit.getPlayer(name) != null) {
				Bukkit.getPlayer(name).removeAttachment(coreCommands.getPermissions().get(name));
			}
		}
		
		getConfig().set("time", ingameTime);
		saveConfig();
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
	
	public PartyManager getPartyManager() {
		return partyManager;
	}
	
	public Tutorial getTutorial() {
		return tutorial;
	}
	
	public MapAnimationManager getMapAnimationManager() {
		return mapAnimationManager;
	}
	
	public StatisticsManager getStatisticsManager() {
		return statisticsManager;
	}
	
	public StatisticsViewer getStatisticsViewer() {
		return statisticsViewer;
	}
	
	public Map<String, Long> getOntimes() {
		return ontimes;
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
	
	public boolean isNoSQL() {
		return noSQL;
	}
	
	private void loadConfiguration() {
		setConfigurationDefaultString("colors.base", "&7");
		setConfigurationDefaultString("colors.highlight", "&a");
		setConfigurationDefaultString("colors.error-base", "&c");
		setConfigurationDefaultString("colors.error-highlight", "&4");
		setConfigurationDefaultString("messages.usage", "&cKomennon käyttö: &4");
		setConfigurationDefaultString("messages.no-permission", "&cKomentoa ei ole olemassa, tai sinulla ei ole oikeutta siihen. Saat apua kirjoittamalla komennon §4/apua§c.");
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
		commands.registerCommand("setupmysql", false);
		commands.registerCommand("updateplayerdata", false);
		commands.registerCommand("playerlookup", false);
		commands.registerCommand("kickall", false);
		commands.registerCommand("huolto", false);
		commands.registerCommand("xstop", false);
		commands.registerCommand("autorestart", false);
		commands.registerCommand("config", false);
		commands.registerCommand("sql", false);
		commands.registerCommand("statistics", false);
		commands.registerCommand("list", true);
		commands.registerCommand("who", true);
		commands.registerCommand("pelaajat", true);
		commands.registerCommand("alert", false);
		commands.registerCommand("autobroadcast", false);
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
		commands.registerCommand("tilastot", true);
		commands.registerCommand("stats", true);
		commands.registerCommand("help", true);
		commands.registerCommand("apua", true);
		commands.registerCommand("?", true);
		commands.registerCommand("säännöt", true);
		commands.registerCommand("rules", true);
		commands.registerCommand("discord", true);
		commands.registerCommand("bugi", true);
		commands.registerCommand("bug", true);
		commands.registerCommand("s", false);
		commands.registerCommand("sa", false);
		commands.registerCommand("a", true);
		commands.registerCommand("helpop", true);
		commands.registerCommand("ar", false);
		commands.registerCommand("henkilökunta", true);
		commands.registerCommand("staff", true);
		commands.registerCommand("viesti", true);
		commands.registerCommand("msg", true);
		commands.registerCommand("tell", true);
		commands.registerCommand("v", true);
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
		commands.registerCommand("kalenteri", true);
		commands.registerCommand("päivämäärä", true);
		commands.registerCommand("juhlapäivä", false);
		commands.registerCommand("juhlapäivät", false);
		commands.registerCommand("lähellä", true);
		commands.registerCommand("near", true);
		commands.registerCommand("sijainti", true);
		commands.registerCommand("getpos", true);
		commands.registerCommand("spawn", true);
		commands.registerCommand("hub", true);
		commands.registerCommand("lobby", true);
		commands.registerCommand("asetaspawn", false);
		commands.registerCommand("setspawn", false);
		commands.registerCommand("setstartpoint", false);
		commands.registerCommand("koti", true);
		commands.registerCommand("home", true);
		commands.registerCommand("asetakoti", true);
		commands.registerCommand("sethome", true);
		commands.registerCommand("nimeäkoti", true);
		commands.registerCommand("namehome", true);
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
		commands.registerCommand("enderchest", false);
		commands.registerCommand("echest", false);
		commands.registerCommand("swapinventories", false);
		commands.registerCommand("evokkimoodi", false);
		commands.registerCommand("endinaattori", false);
		commands.registerCommand("tp", false);
		commands.registerCommand("tpo", false);
		commands.registerCommand("tphere", false);
		commands.registerCommand("tpohere", false);
		commands.registerCommand("tppos", false);
		commands.registerCommand("noafk", false);
		commands.registerCommand("lempinimi", true);
		commands.registerCommand("nick", true);
		commands.registerCommand("fix", false);
		commands.registerCommand("name", false);
		commands.registerCommand("lore", false);
		commands.registerCommand("setamount", false);
		commands.registerCommand("pt", false);
		commands.registerCommand("lightfix", false);
		commands.registerCommand("thor", false);
		commands.registerCommand("entity", false);
		commands.registerCommand("mob", false);
		commands.registerCommand("killall", false);
		commands.registerCommand("xp", false);
		commands.registerCommand("xpl", false);
		commands.registerCommand("portti", false);
		commands.registerCommand("tykki", false);
		commands.registerCommand("kallot", false);
		commands.registerCommand("komentokuutio", false);
		commands.registerCommand("command-block", false);
		commands.registerCommand("maailma", false);
		commands.registerCommand("world", false);
		commands.registerCommand("talli", false);
		commands.registerCommand("stable", false);
		commands.registerCommand("holo", false);
		commands.registerCommand("armorstand", false);
		commands.registerCommand("stand", false);
		commands.registerCommand("vnpc", false);
		commands.registerCommand("tutorial", false);
		commands.registerCommand("tardis", false);
		commands.registerCommand("musiikkikauppa", false);
		commands.registerCommand("pvpstats", true);
		commands.registerCommand("matka", true);
		commands.registerCommand("rankaise", false);
		commands.registerCommand("h", false);
		commands.registerCommand("huomautus", false);
		commands.registerCommand("note", false);
		commands.registerCommand("sakko", true);
		commands.registerCommand("sakot", true);
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
		commands.registerCommand("pc", true);
		commands.registerCommand("party", true);
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
	
	private void addStaffCommands() {
		CoreUtils.getRegisteredStaffCommands().add("co");
		CoreUtils.getRegisteredStaffCommands().add("core");
		CoreUtils.getRegisteredStaffCommands().add("coreprotect");
		CoreUtils.getRegisteredStaffCommands().add("/wand");
	}
	
	private void addCustomRecipes() {
		
		ItemStack chainmailHelmet = new ItemStack(Material.CHAINMAIL_HELMET);
		ShapedRecipe chainmailHelmetRecipe1 = new ShapedRecipe(new NamespacedKey(this, "chainmail_helmet1"), chainmailHelmet);
		chainmailHelmetRecipe1.shape("III", "N N", "   ");
		chainmailHelmetRecipe1.setIngredient('I', Material.IRON_INGOT);
		chainmailHelmetRecipe1.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailHelmetRecipe1);
		ShapedRecipe chainmailHelmetRecipe2 = new ShapedRecipe(new NamespacedKey(this, "chainmail_helmet2"), chainmailHelmet);
		chainmailHelmetRecipe2.shape("   ", "III", "N N");
		chainmailHelmetRecipe2.setIngredient('I', Material.IRON_INGOT);
		chainmailHelmetRecipe2.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailHelmetRecipe2);
		
		ItemStack chainmailChestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
		ShapedRecipe chainmailChestplateRecipe = new ShapedRecipe(new NamespacedKey(this, "chainmail_chestplate"), chainmailChestplate);
		chainmailChestplateRecipe.shape("N N", "III", "III");
		chainmailChestplateRecipe.setIngredient('I', Material.IRON_INGOT);
		chainmailChestplateRecipe.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailChestplateRecipe);
		
		ItemStack chainmailLeggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
		ShapedRecipe chainmailLeggingsRecipe = new ShapedRecipe(new NamespacedKey(this, "chainmail_leggings"), chainmailLeggings);
		chainmailLeggingsRecipe.shape("III", "I I", "N N");
		chainmailLeggingsRecipe.setIngredient('I', Material.IRON_INGOT);
		chainmailLeggingsRecipe.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailLeggingsRecipe);
		
		ItemStack chainmailBoots = new ItemStack(Material.CHAINMAIL_BOOTS);
		ShapedRecipe chainmailBootsRecipe1 = new ShapedRecipe(new NamespacedKey(this, "chainmail_boots1"), chainmailBoots);
		chainmailBootsRecipe1.shape("I I", "N N", "   ");
		chainmailBootsRecipe1.setIngredient('I', Material.IRON_INGOT);
		chainmailBootsRecipe1.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailBootsRecipe1);
		ShapedRecipe chainmailBootsRecipe2 = new ShapedRecipe(new NamespacedKey(this, "chainmail_boots2"), chainmailBoots);
		chainmailBootsRecipe2.shape("   ", "I I", "N N");
		chainmailBootsRecipe2.setIngredient('I', Material.IRON_INGOT);
		chainmailBootsRecipe2.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(chainmailBootsRecipe2);
	}
	
	private void loops() {
		
		new BukkitRunnable() {
			public void run() {
				statisticsViewer.updatePvpTopCache();
				statisticsViewer.updatePvpTopHolograms(Bukkit.getWorlds().get(0));
			}
		}.runTaskAsynchronously(this);
		
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
				for (Villager npc : coreListener.getNPCLook()) {
					if (!npc.isDead()) {
						Player nearestPlayer = null;
						for (Player player : npc.getWorld().getPlayers()) {
							if (nearestPlayer == null || npc.getLocation().distance(player.getLocation()) < npc.getLocation().distance(nearestPlayer.getLocation())) {
								nearestPlayer = player;
							}
						}
						if (nearestPlayer != null && npc.getLocation().distance(nearestPlayer.getLocation()) <= 6) {
							Vector vector = nearestPlayer.getLocation().toVector().subtract(npc.getLocation().toVector());
							Location location = npc.getLocation();
							location.setDirection(vector);
							float difference = location.getYaw() - CoreUtils.scaleYaw(npc.getLocation().getYaw());
							if ((difference > 20 && difference <= 180) || difference < -180) {
								location.setYaw(CoreUtils.scaleYaw(npc.getLocation().getYaw() + 20));
							}
							else if ((difference < -20 && difference >= -180) || difference > 180) {
								location.setYaw(CoreUtils.scaleYaw(npc.getLocation().getYaw() - 20));
							}
							npc.teleport(location);
						}
					}
				}
			}
		}.runTaskTimer(this, 0, 1);
		
		new BukkitRunnable() {
			public void run() {
				coreListener.getNPCLook().clear();
				for (World world : Bukkit.getWorlds()) {
					for (Villager villager : world.getEntitiesByClass(Villager.class)) {
						if (CoreUtils.isNPC(villager) && villager.getScoreboardTags().contains("RK-look")) {
							coreListener.getNPCLook().add(villager);
						}
					}
				}
			}
		}.runTaskTimer(this, 0, 100);
		
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
					
					if (!tutorial.getPlayersInTutorial().contains(player.getName())) {
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
					
					// ontime
					
					if (CoreUtils.getAfkCounter().get(player.getName()) != -1) {
						long seconds = 0;
						if (ontimes.containsKey(player.getUniqueId().toString())) {
							seconds = ontimes.get(player.getUniqueId().toString());
						}
						ontimes.put(player.getUniqueId().toString(), seconds + 1);
					}
				}
				
				// nukkuminen
				
				int playersSleeping = 0;
				int playersOnline = Bukkit.getWorlds().get(0).getPlayers().size();
				int playersNeededToSleep = playersOnline % 2 == 0 ? playersOnline / 2 : playersOnline / 2 + 1;
				for (Player player : Bukkit.getWorlds().get(0).getPlayers()) {
					if (player.isSleeping()) {
						playersSleeping++;
					}
				}
				for (Player player : Bukkit.getWorlds().get(0).getPlayers()) {
					if (player.isSleeping()) {
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6" + playersSleeping + "§e/§6" + playersNeededToSleep + "§e pelaajaa nukkumassa"));
					}
				}
				if (playersSleeping >= playersNeededToSleep) {
					sleepCounter++;
				}
				else {
					sleepCounter = 0;
				}
				if (sleepCounter >= 3) {
					if (CoreUtils.getHourOfDay(ingameTime) < 19 && (Bukkit.getWorlds().get(0).hasStorm() || Bukkit.getWorlds().get(0).isThundering())) {
						ingameTime = ingameTime + 3600000;
						Bukkit.getWorlds().get(0).setStorm(false);
						Bukkit.getWorlds().get(0).setThundering(false);
						for (Player player : Bukkit.getWorlds().get(0).getPlayers()) {
							if (player.isSleeping()) {
								player.wakeup(true);
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eNukuit päiväunet, eikä ulkona enää sada."));
							}
						}
					}
					else {
						ingameTime = CoreUtils.getNextMorningTime(ingameTime);
						Bukkit.getWorlds().get(0).setStorm(false);
						Bukkit.getWorlds().get(0).setThundering(false);
						for (Player player : Bukkit.getWorlds().get(0).getPlayers()) {
							if (player.isSleeping()) {
								player.wakeup(true);
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eNukuit yön yli."));
							}
						}
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
					
					if (player.getInventory().getItemInMainHand().getType() == Material.CLOCK || 
							player.getInventory().getItemInOffHand().getType() == Material.CLOCK) {
						TextComponent component = new TextComponent(CoreUtils.getFriendlyTimeString(ingameTime));
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
					}
				}
			}
		}.runTaskTimer(this, 0, 10);
		
		if (!noSQL) {
			
			new BukkitRunnable() {
				public void run() {
					
					// online-pelaajat
					
					MySQLUtils.set("UPDATE global SET online=" + Bukkit.getOnlinePlayers().size());
				}
			}.runTaskTimerAsynchronously(this, 100, 100);
			
			new BukkitRunnable() {
				public void run() {
					
					// ontime
					
					for (String uuid : ontimes.keySet()) {
						MySQLResult infoData = MySQLUtils.get("SELECT seconds FROM player_info WHERE uuid=?", uuid);
						if (infoData != null) {
							long seconds = infoData.getLong(0, "seconds");
							seconds += ontimes.get(uuid);
							MySQLUtils.set("UPDATE player_info SET seconds=? WHERE uuid=?", "" + seconds, uuid);
						}
					}
					ontimes.clear();
				}
			}.runTaskTimerAsynchronously(this, 1200, 1200);
			
			new BukkitRunnable() {
				public void run() {
					
					// tilastot
					
					int players = 0;
					int staff = 0;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (CoreUtils.hasRank(player, "valvoja")) {
							staff++;
						}
						players++;
					}
					statisticsManager.logStatistic(new StatisticsEntry(Statistic.PLAYERS_ONLINE, players));
					statisticsManager.logStatistic(new StatisticsEntry(Statistic.STAFF_ONLINE, staff));
					statisticsManager.saveCacheToDatabase();
					statisticsViewer.updatePvpTopCache();
					statisticsViewer.updatePvpTopHolograms(Bukkit.getWorlds().get(0));
					statisticsViewer.getViewerCache().clear();
				}
			}.runTaskTimerAsynchronously(this, 12000, 12000);
			
			new BukkitRunnable() {
				public void run() {
					
					// vangitut pelajaat
					
					for (Player player : Bukkit.getOnlinePlayers()) {
						long expires = getConfig().getLong("users." + player.getName() + ".jail.duration");
						if (expires != 0) {
							if (System.currentTimeMillis() >= expires) {
								getConfig().set("users." + player.getName() + ".jail", null);
								saveConfig();
								punishmentCommands.releaseFromJail(player);
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("").create());
								new BukkitRunnable() {
									public void run() {
										MySQLUtils.set("DELETE FROM player_jail WHERE uuid=?", player.getUniqueId().toString().replace("-", ""));
									}
								}.runTaskAsynchronously(Core.this);
							}
							else {
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("§cRangaistusta jäljellä: §4" + 
										CoreUtils.getDaysAndHoursAndMinsFromMillis(expires - System.currentTimeMillis())).create());
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
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("").create());
								new BukkitRunnable() {
									public void run() {
										MySQLUtils.set("DELETE FROM player_mute WHERE uuid=?", player.getUniqueId().toString().replace("-", ""));
									}
								}.runTaskAsynchronously(Core.this);
							}
							else {
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("§cRangaistusta jäljellä: §4" + 
										CoreUtils.getDaysAndHoursAndMinsFromMillis(expires - System.currentTimeMillis())).create());
							}
						}
					}
				}
			}.runTaskTimer(this, 40, 40);
		}
		
		new BukkitRunnable() {
			String previousMessage1 = null;
			String previousMessage2 = null;
			Random random = new Random();
			int i = 0;
			public void run() {
				
				// autobroadcast
				
				int time = getConfig().getInt("autobroadcast.time");
				
				if (i >= time) {
					if (time != 0) {
						List<String> messages = getConfig().getStringList("autobroadcast.messages");
						if (!messages.isEmpty()) {
							String prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("autobroadcast.prefix", ""));
							String message = null;
							for (int c = 0; c < 10; c++) {
								int index = random.nextInt(messages.size());
								message = ChatColor.translateAlternateColorCodes('&', messages.get(index));
								if ((previousMessage1 != null && message.equals(previousMessage1)) || (previousMessage2 != null && message.equals(previousMessage2))) {
									continue;
								}
								break;
							}
							previousMessage2 = previousMessage1;
							previousMessage1 = message;
							if (message != null) {
								for (Player player : Bukkit.getOnlinePlayers()) {
									if (SettingsUtils.getSetting(player, "show_autobroadcast")) {
										player.sendMessage(prefix + message);
									}
								}
							}
						}
					}
					i = 0;
				}
				i++;
			}
		}.runTaskTimer(this, 0, 1200);
		
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
					player.setPlayerListHeaderFooter(s, "§a mc.royalkingdom.fi "); // TODO
				}
			}
		}.runTaskTimer(this, 20, 2);
		
		new BukkitRunnable() {
			public void run() {
				
				// aika configiin
				
				getConfig().set("time", ingameTime);
				saveConfig();
			}
		}.runTaskTimer(this, 36000, 36000);
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