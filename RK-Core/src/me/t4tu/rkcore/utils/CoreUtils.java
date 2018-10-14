package me.t4tu.rkcore.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftMetaBook;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.t4tu.rkcore.Core;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;

public class CoreUtils {
	
	public static final long TIME_OFFSET = 10800000;
	public static final double INGAME_TIME_SPEED_MULTIPLIER = 36;
	public static final int SECONDS_TO_HOME_1 = 0; // 0 tuntia
	public static final int SECONDS_TO_HOME_2 = 18000; // 5 tuntia
	public static final int SECONDS_TO_HOME_3 = 72000; // 20 tuntia
	public static final int SECONDS_TO_HOME_4 = 1800000; // 500 tuntia TODO
	
	private static Core core;
	private static List<String> registeredCommandsNoTabComplete = new ArrayList<String>();
	private static List<String> registeredCommandsTabComplete = new ArrayList<String>();
	private static List<String> registeredCommandsStaff = new ArrayList<String>();
	private static List<String> adminPowers = new ArrayList<String>();
	private static List<String> builderPowers = new ArrayList<String>();
	private static Map<String, Integer> afkCounter = new HashMap<String, Integer>();
	private static List<String> haltAfkCounter = new ArrayList<String>();
	
	// TODO
	public static HashMap<String, HashMap<String, String>> disguiseTeams = new HashMap<String, HashMap<String, String>>();
	
	public static void setCore(Core plugin) {
		core = plugin;
	}
	
	public static String getBaseColor() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("colors.base"));
	}
	
	public static String getHighlightColor() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("colors.highlight"));
	}
	
	public static String getErrorBaseColor() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("colors.error-base"));
	}
	
	public static String getErrorHighlightColor() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("colors.error-highlight"));
	}
	
	public static String getUsageString() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("messages.usage"));
	}
	
	public static String getNoPermissionString() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("messages.no-permission"));
	}
	
	public static String getPlayersOnlyString() {
		return ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("messages.players-only"));
	}
	
	public static String getMySqlHost() {
		return core.getConfig().getString("mysql.host");
	}
	
	public static String getMySqlUsername() {
		return core.getConfig().getString("mysql.username");
	}
	
	public static String getMySqlPassword() {
		return core.getConfig().getString("mysql.password");
	}
	
	public static List<String> getRegisteredCommands() {
		return registeredCommandsNoTabComplete;
	}
	
	public static List<String> getRegisteredCommandsWithTabCompletion() {
		return registeredCommandsTabComplete;
	}
	
	public static List<String> getRegisteredStaffCommands() {
		return registeredCommandsStaff;
	}
	
	public static List<String> getAdminPowers() {
		return adminPowers;
	}
	
	public static List<String> getBuilderPowers() {
		return builderPowers;
	}
	
	public static List<String> getVanishedPlayers() {
		return core.getCoreCommands().getVanishedPlayers();
	}
	
	public static boolean hasAdminPowers(CommandSender sender) {
		if (sender.isOp()) {
			return true;
		}
		if (adminPowers.contains(sender.getName())) {
			return true;
		}
		return false;
	}
	
	public static boolean hasBuilderPowers(CommandSender sender) {
		if (sender.isOp()) {
			return true;
		}
		if (builderPowers.contains(sender.getName())) {
			return true;
		}
		if (adminPowers.contains(sender.getName())) {
			return true;
		}
		return false;
	}
	
	public static Map<String, Integer> getAfkCounter() {
		return afkCounter;
	}
	
	public static void setAfkCounter(Player player, int count) {
		
		String tc2 = getBaseColor();
		
		int current = 0;
		
		if (afkCounter.containsKey(player.getName())) {
			current = afkCounter.get(player.getName());
		}
		
		if (count == -1 && current != -1) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (SettingsUtils.getSetting(p, "show_afk")) {
					p.sendMessage(tc2 + player.getName() + " on nyt AFK-tilassa.");
				}
			}
			if (!SettingsUtils.getSetting(player, "show_afk")) {
				player.sendMessage(tc2 + "Olet nyt AFK-tilassa!");
			}
		}
		else if (count != -1 && current == -1) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (SettingsUtils.getSetting(p, "show_afk")) {
					p.sendMessage(tc2 + player.getName() + " ei ole enää AFK-tilassa.");
				}
			}
			if (!SettingsUtils.getSetting(player, "show_afk")) {
				player.sendMessage(tc2 + "Et ole enää AFK-tilassa!");
			}
		}
		
		afkCounter.put(player.getName(), count);
	}
	
	public static List<String> getHaltAfkCounter() {
		return haltAfkCounter;
	}
	
	public static String nameToName(String name) {
		MySQLResult infoData = MySQLUtils.get("SELECT name FROM player_info WHERE name=?", name);
		if (infoData != null) {
			return infoData.getString(0, "name");
		}
		return null;
	}
	
	public static String nameToUuid(String name) {
		MySQLResult infoData = MySQLUtils.get("SELECT uuid FROM player_info WHERE name=?", name);
		if (infoData != null) {
			return infoData.getString(0, "uuid");
		}
		return null;
	}
	
	public static String uuidToName(String uuid) {
		MySQLResult infoData = MySQLUtils.get("SELECT name FROM player_info WHERE uuid=?", uuid);
		if (infoData != null) {
			return infoData.getString(0, "name");
		}
		return null;
	}
	
	public static List<String> getFriendsUuids(String name) {
		List<String> friends = new ArrayList<String>();
		MySQLResult friendsData = MySQLUtils.get("SELECT friends FROM player_stats WHERE name=?", name);
		if (friendsData != null && friendsData.getString(0, "friends") != null) {
			String[] friendsArray = friendsData.getString(0, "friends").split(";");
			for (String friend : friendsArray) {
				if (friend.length() > 0) {
					friends.add(friend);
				}
			}
		}
		return friends;
	}
	
	public static void setFriendsUuids(String name, List<String> friends) {
		String friendsString = "";
		for (String friend : friends) {
			if (friend.length() > 0) {
				friendsString += friend + ";";
			}
		}
		MySQLUtils.set("UPDATE player_stats SET friends=? WHERE name=?", friendsString, name);
	}
	
	public static List<String> getGuildMembersUuidsId(String id) {
		MySQLResult guildsData = MySQLUtils.get("SELECT members FROM guilds WHERE id=?", id);
		return getGuildMembersUuids(guildsData);
	}
	
	public static List<String> getGuildMembersUuids(String guild) {
		MySQLResult guildsData = MySQLUtils.get("SELECT members FROM guilds WHERE guild_name=?", guild);
		return getGuildMembersUuids(guildsData);
	}
	
	public static List<String> getGuildMembersUuids(MySQLResult guildsData) {
		List<String> members = new ArrayList<String>();
		if (guildsData != null && guildsData.getString(0, "members") != null) {
			String[] membersArray = guildsData.getString(0, "members").split(";");
			for (String member : membersArray) {
				if (member.length() > 0) {
					members.add(member);
				}
			}
		}
		return members;
	}
	
	public static void setGuildMembersUuids(String id, List<String> members) {
		String string = "";
		for (String member : members) {
			string += member + ";";
		}
		MySQLUtils.set("UPDATE guilds SET members=? WHERE id=?", string, id);
	}
	
	public static boolean hasAccessToHome(String name, int home) {
		MySQLResult infoData = MySQLUtils.get("SELECT seconds FROM player_info WHERE name=?", name);
		if (infoData != null) {
			int seconds = infoData.getInt(0, "seconds");
			if (home == 1 && seconds >= SECONDS_TO_HOME_1) {
				return true;
			}
			if (home == 2 && seconds >= SECONDS_TO_HOME_2) {
				return true;
			}
			if (home == 3 && seconds >= SECONDS_TO_HOME_3) {
				return true;
			}
			if (home == 4 && seconds >= SECONDS_TO_HOME_4) {
				return true;
			}
		}
		return false;
	}
	
	public static Location getHome(String name, int home) {
		MySQLResult homeData = MySQLUtils.get("SELECT * FROM player_homes WHERE name=?", name);
		if (homeData != null) {
			String homeString = homeData.getString(0, "home_" + home);
			if (homeString != null) {
				try {
					String worldString = homeString.split(";")[0];
					String xString = homeString.split(";")[1];
					String yString = homeString.split(";")[2];
					String zString = homeString.split(";")[3];
					String yawString = homeString.split(";")[4];
					String pitchString = homeString.split(";")[5];
					try {
						World world = Bukkit.getWorld(worldString);
						double x = Double.parseDouble(xString);
						double y = Double.parseDouble(yString);
						double z = Double.parseDouble(zString);
						float yaw = Float.parseFloat(yawString);
						float pitch = Float.parseFloat(pitchString);
						if (world != null) {
							return new Location(world, x, y, z, yaw, pitch);
						}
					}
					catch (NumberFormatException e) {
					}
				}
				catch (ArrayIndexOutOfBoundsException e) {
				}
			}
		}
		return null;
	}
	
	public static void setHome(Player player, int home) {
		if (home < 1 || home > 4) {
			return;
		}
		Location location = player.getLocation();
		String worldString = location.getWorld().getName();
		String xString = "" + location.getX();
		String yString = "" + location.getY();
		String zString = "" + location.getZ();
		String yawString = "" + location.getYaw();
		String pitchString = "" + location.getPitch();
		String homeString = worldString + ";" + xString + ";" + yString + ";" + zString + ";" + yawString + ";" + pitchString;
		MySQLUtils.set("UPDATE player_homes SET home_" + home + "=? WHERE name=?", homeString, player.getName());
	}
	
	public static void delHome(Player player, int home) {
		if (home < 1 || home > 4) {
			return;
		}
		MySQLUtils.set("UPDATE player_homes SET home_" + home + "=? WHERE name=?", "", player.getName());
	}
	
	public static ItemStack getHomeItem(Player player, int home) {
		
		ItemStack item = new ItemStack(Material.BED);
		ItemMeta meta = item.getItemMeta();
		
		if (home == 1) {
			item.setDurability((short) 0);
		}
		else if (home == 2) {
			item.setDurability((short) 14);
		}
		else if (home == 3) {
			item.setDurability((short) 13);
		}
		else if (home == 4) {
			item.setDurability((short) 11);
		}
		
		Location location = getHome(player.getName(), home);
		
		if (location != null) {
			int x = location.getBlockX();
			int y = location.getBlockY();
			int z = location.getBlockZ();
			meta.setDisplayName("§aKoti #" + home);
			meta.setLore(Arrays.asList("", "§7(" + x + ", " + y + ", " + z + ")", "",  "§7Teleporttaa tähän kotipisteeseen", 
					"§7klikkaamalla tästä.", "", "§a » Teleporttaa klikkaamalla!"));
		}
		else {
			if (hasAccessToHome(player.getName(), home)) {
				meta.setDisplayName("§eKoti #" + home);
				meta.setLore(Arrays.asList("", "§7Et ole vielä asettanut tätä", "§7kotipistettä! Aseta piste haluaamasi", 
						"§7sijaintiin, niin voit teleportata", "§7siihen myöhemmin.", "", "§e » Aseta kotipiste klikkaamalla!"));
			}
			else {
				float current = 0;
				MySQLResult infoData = MySQLUtils.get("SELECT seconds FROM player_info WHERE name=?", player.getName());
				if (infoData != null) {
					current = infoData.getInt(0, "seconds") / 60f / 60f;
				}
				int seconds = 0;
				if (home == 1) {
					seconds = SECONDS_TO_HOME_1;
				}
				else if (home == 2) {
					seconds = SECONDS_TO_HOME_2;
				}
				else if (home == 3) {
					seconds = SECONDS_TO_HOME_3;
				}
				else if (home == 4) {
					seconds = SECONDS_TO_HOME_4;
				}
				float max = seconds / 60f / 60f;
				meta.setDisplayName("§cKoti #" + home);
				meta.setLore(Arrays.asList("", "§7Et ole vielä ansainnut tätä kotipistettä!", "§7Sinun täytyy olla pelannut palvelimella", 
						"§7vähintään §c" + (int) max + " tunnin§7 verran voidaksesi", "§7käyttää tätä kotipistettä. Olet", 
						"§7pelannut palvelimella §c" + (int) current + "§7/§c" + (int) max + "h", "", "§c ✖ Lukittu!"));
			}
		}
		
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static void updateVanish() {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			for (Player pla : Bukkit.getOnlinePlayers()) {
				if (core.getCoreCommands().getVanishedPlayers().contains(pla.getName())) {
					pl.hidePlayer(core, pla);
				}
				else {
					pl.showPlayer(core, pla);
				}
			}
		}
	}
	
	public static void updatePermissions(Player player) {
		if (core.getCoreCommands().getPermissions().get(player.getName()) != null) {
			player.removeAttachment(core.getCoreCommands().getPermissions().get(player.getName()));
		}
		core.getCoreCommands().getPermissions().remove(player.getName());
		PermissionAttachment attachment = player.addAttachment(core);
		String uuid = player.getUniqueId().toString();
		if (core.getConfig().getConfigurationSection("permissions." + uuid) != null) {
			for (String perm : core.getConfig().getConfigurationSection("permissions." + uuid).getKeys(false)) {
				attachment.setPermission(perm.replace(";", "."), core.getConfig().getBoolean("permissions." + uuid + "." + perm));
			}
		}
		core.getCoreCommands().getPermissions().put(player.getName(), attachment);
	}
	
	public static void updateNotes(Player player) {
		core.getConfig().set("users." + player.getName() + ".notes", null);
		MySQLResult notesData = MySQLUtils.get("SELECT * FROM player_notes WHERE name=?", player.getName());
		if (notesData != null) {
			List<String> notes = new ArrayList<String>();
			for (int i = 0; i < notesData.getRows(); i++) {
				String text = notesData.getString(i, "note");
				String giver = notesData.getString(i, "giver");
				notes.add(giver + ": &o" + text);
			}
			core.getConfig().set("users." + player.getName() + ".notes", notes);
		}
		core.saveConfig();
	}
	
	public static void updateTab(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			String rank = core.getConfig().getString("users." + p.getName() + ".rank");
			if (rank == null) {
				continue;
			}
			boolean jailed = core.getConfig().getBoolean("users." + p.getName() + ".jail.jailed");
			if (jailed) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.vankilassa);
			}
			else if (rank.equalsIgnoreCase("ylläpitäjä")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.ylläpitäjä);
			}
			else if (rank.equalsIgnoreCase("moderaattori")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.moderaattori);
			}
			else if (rank.equalsIgnoreCase("valvoja")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.valvoja);
			}
			else if (rank.equalsIgnoreCase("arkkitehti")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.arkkitehti);
			}
			else if (rank.equalsIgnoreCase("aatelinen")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.aatelinen);
			}
			else if (rank.equalsIgnoreCase("ritari")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.ritari);
			}
			else {
				ReflectionUtils.sendScoreboardTeamPacket(player, p.getName(), ReflectionUtils.def);
			}
		}
		for (String s : disguiseTeams.keySet()) { // TODO tidy up some stuff...
			String n = disguiseTeams.get(s).get("name");
			String t = disguiseTeams.get(s).get("rank");
			if (n.equals("Dinnerbone") && t.equals("ylläpitäjä")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.upsidedown);
			}
			else if (t.equalsIgnoreCase("ylläpitäjä")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.ylläpitäjä);
			}
			else if (t.equalsIgnoreCase("moderaattori")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.moderaattori);
			}
			else if (t.equalsIgnoreCase("valvoja")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.valvoja);
			}
			else if (t.equalsIgnoreCase("arkkitehti")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.arkkitehti);
			}
			else if (t.equalsIgnoreCase("aatelinen")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.aatelinen);
			}
			else if (t.equalsIgnoreCase("ritari")) {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.ritari);
			}
			else {
				ReflectionUtils.sendScoreboardTeamPacket(player, n, ReflectionUtils.def);
			}
		}
	}
	
	public static void updateTabForAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			updateTab(player);
		}
	}
	
	public static String getPrefixDescription(String prefix) {
		if (prefix.equalsIgnoreCase("§7[«§4Ylläpitäjä§7»] ") || prefix.equalsIgnoreCase("§7[«§4Pääarkkitehti§7»] ") || 
				prefix.equalsIgnoreCase("§7[«§4Pääsuunnittelija§7»] ") || prefix.equalsIgnoreCase("§7[«§4Pääkehittäjä§7»] ")) {
			return "§4Kuvaus tulossa";
		}
		else if (prefix.equalsIgnoreCase("§7[«§cModeraattori§7»] ")) {
			return "§cKuvaus tulossa";
		}
		else if (prefix.equalsIgnoreCase("§7[«§cValvoja§7»] ")) {
			return "§cKuvaus tulossa";
		}
		else if (prefix.equalsIgnoreCase("§7[«§eArkkitehti§7»] ")) {
			return "§eKuvaus tulossa";
		}
		else if (prefix.equalsIgnoreCase("§7[«§6Aatelinen§7»] ")) {
			return "§6Kuvaus tulossa";
		}
		else if (prefix.equalsIgnoreCase("§7[«§2Ritari§7»] ")) {
			return "§2Kuvaus tulossa";
		}
		else {
			return "§7Tälle arvolle ei ole asetettu kuvausta";
		}
	}
	
	public static boolean hasRank(CommandSender sender, String rank) {
		if (sender.isOp()) {
			return true;
		}
		String exactRank = core.getConfig().getString("users." + sender.getName() + ".rank");
		if (exactRank == null) {
			return false;
		}
		if (rank == "ritari") {
			return exactRank.equalsIgnoreCase("ritari") || exactRank.equalsIgnoreCase("aatelinen") || 
					exactRank.equalsIgnoreCase("arkkitehti") || exactRank.equalsIgnoreCase("valvoja") || 
					exactRank.equalsIgnoreCase("moderaattori") || exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else if (rank == "aatelinen") {
			return exactRank.equalsIgnoreCase("aatelinen") || exactRank.equalsIgnoreCase("arkkitehti") || 
					exactRank.equalsIgnoreCase("valvoja") || exactRank.equalsIgnoreCase("moderaattori") || 
					exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else if (rank == "arkkitehti") {
			return exactRank.equalsIgnoreCase("arkkitehti") || exactRank.equalsIgnoreCase("moderaattori") || 
					exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else if (rank == "valvoja") {
			return exactRank.equalsIgnoreCase("valvoja") || exactRank.equalsIgnoreCase("moderaattori") || 
					exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else if (rank == "moderaattori") {
			return exactRank.equalsIgnoreCase("moderaattori") || exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else if (rank == "ylläpitäjä") {
			return exactRank.equalsIgnoreCase("ylläpitäjä");
		}
		else {
			return false;
		}
	}
	
	public static boolean hasRankSQL(String uuid, String rank) {
		MySQLResult infoData = MySQLUtils.get("SELECT rank FROM player_info WHERE uuid=?", uuid);
		if (infoData != null) {
			String exactRank = infoData.getStringNotNull(0, "rank");
			if (rank == "ritari") {
				return exactRank.equalsIgnoreCase("ritari") || exactRank.equalsIgnoreCase("aatelinen") || 
						exactRank.equalsIgnoreCase("arkkitehti") || exactRank.equalsIgnoreCase("valvoja") || 
						exactRank.equalsIgnoreCase("moderaattori") || exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else if (rank == "aatelinen") {
				return exactRank.equalsIgnoreCase("aatelinen") || exactRank.equalsIgnoreCase("arkkitehti") || 
						exactRank.equalsIgnoreCase("valvoja") || exactRank.equalsIgnoreCase("moderaattori") || 
						exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else if (rank == "arkkitehti") {
				return exactRank.equalsIgnoreCase("arkkitehti") || exactRank.equalsIgnoreCase("moderaattori") || 
						exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else if (rank == "valvoja") {
				return exactRank.equalsIgnoreCase("valvoja") || exactRank.equalsIgnoreCase("moderaattori") || 
						exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else if (rank == "moderaattori") {
				return exactRank.equalsIgnoreCase("moderaattori") || exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else if (rank == "ylläpitäjä") {
				return exactRank.equalsIgnoreCase("ylläpitäjä");
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public static TextComponent getVipNeededMessage() {
		return getVipNeededMessage("käyttää tätä komentoa");
	}
	
	public static TextComponent getVipNeededMessage(String action) {
		
		String tc3 = getErrorBaseColor();
		String tc4 = getErrorHighlightColor();
		
		// TODO
		ClickEvent clickEvent = new ClickEvent(Action.OPEN_URL, "https://esimerkki.fi");
		
		TextComponent baseComponent = new TextComponent("");
		BaseComponent[] mainComponents = TextComponent.fromLegacyText(tc3 + "Vain §2Ritareilla" + tc3 + " ja §6Aatelisilla" + tc3 + " on oikeus "
				+ action + "! Mikäli haluat tukea palvelimemme kehitystä, harkitse arvon ostamista ");
		BaseComponent[] linkComponents = new ComponentBuilder("kaupastamme").color(ChatColor.getByChar(tc4.charAt(1))).event(clickEvent).create();
		BaseComponent[] dotComponents = TextComponent.fromLegacyText(tc3 + ".");
		
		for (BaseComponent component : mainComponents) {
			baseComponent.addExtra(component);
		}
		for (BaseComponent component : linkComponents) {
			baseComponent.addExtra(component);
		}
		for (BaseComponent component : dotComponents) {
			baseComponent.addExtra(component);
		}
		
		return baseComponent;
	}
	
	public static String getBar(String color1, String color2, int length, int current, int max, String bar) {
		String bar1 = color1;
		String bar2 = color2;
		float percent = (float) current / max;
		int green = (int) (length * percent + 0.5);
		for (int x = 0; x < length; x++) {
			if (x < green) {
				bar1 = bar1 + bar;
			}
			else {
				bar2 = bar2 + bar;
			}
		}
		return bar1 + bar2;
	}
	
	// TODO
	public static boolean isNPCAndNamed(Entity e, String s) {
//		if (e.hasMetadata("NPC")) {
//			if (CitizensAPI.getNPCRegistry().isNPC(e)) {
//				NPC npc = CitizensAPI.getNPCRegistry().getNPC(e);
//				if (npc.getFullName().equals(s)) {
//					return true;
//				}
//			}
//		}
//		return false;
		if (e.hasMetadata("NPC") && e.getCustomName() != null && e.getCustomName().equals(s)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasEnoughRoom(Player p, ItemStack i, int a) {
		i = i.clone();
		int counter = 0;
		for (ItemStack is : p.getInventory().getStorageContents()) {
			if (isNotAir(is)) {
				if (is.isSimilar(i) && is.getAmount() <= i.getMaxStackSize()) {
					counter = counter + i.getMaxStackSize() - is.getAmount();
				}
			}
			else {
				counter = counter + i.getMaxStackSize();
			}
		}
		return counter >= a;
	}
	
	public static boolean hasEnoughRoom(Player p, ItemStack i, int a, ItemStack i2, int a2) {
		Inventory inventory = Bukkit.createInventory(null, 36);
		ItemStack[] contents = p.getInventory().getStorageContents().clone();
		inventory.setContents(contents);
		int c = 0;
		for (ItemStack stack : contents) {
			if (isNotAir(stack) && stack.isSimilar(i)) {
				c += stack.getAmount();
			}
		}
		int c2 = 0;
		for (ItemStack stack : contents) {
			if (isNotAir(stack) && stack.isSimilar(i2)) {
				c2 += stack.getAmount();
			}
		}
		i = i.clone();
		i.setAmount(a);
		inventory.addItem(i);
		i2 = i2.clone();
		i2.setAmount(a2);
		inventory.addItem(i2);
		contents = inventory.getContents();
		int nc = 0;
		for (ItemStack stack : contents) {
			if (isNotAir(stack) && stack.isSimilar(i)) {
				nc += stack.getAmount();
			}
		}
		int nc2 = 0;
		for (ItemStack stack : contents) {
			if (isNotAir(stack) && stack.isSimilar(i2)) {
				nc2 += stack.getAmount();
			}
		}
		return nc - c == a && nc2 - c2 == a2;
	}
	
	public static void removeItems(Inventory inventory, ItemStack item, int amount) {
		ItemStack[] contents = inventory.getContents();
		for (ItemStack stack : contents) {
			if (CoreUtils.isNotAir(stack) && stack.isSimilar(item) && amount > 0) {
				if (stack.getAmount() - amount >= 0) {
					stack.setAmount(stack.getAmount() - amount);
					amount = 0;
				}
				else {
					amount -= stack.getAmount();
					stack.setAmount(0);
				}
			}
		}
		inventory.setContents(contents);
	}
	
	public static ItemStack getItem(Material m, String s, List<String> l, int i) {
		ItemStack is = new ItemStack(m); {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(s);
			im.setLore(l);
			is.setItemMeta(im);
			is.setAmount(i);
		}
		return is;
	}
	
	public static ItemStack getItem(Material m, String s, List<String> l, int i, int d) {
		ItemStack is = new ItemStack(m); {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(s);
			im.setLore(l);
			is.setItemMeta(im);
			is.setDurability((short)d);
			is.setAmount(i);
		}
		return is;
	}
	
	public static ItemStack getItem(Material m, String s, List<String> l, int i, int d, boolean e, boolean h) {
		ItemStack is = new ItemStack(m); {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(s);
			im.setLore(l);
			if (e) {
				im.addEnchant(Enchantment.PROTECTION_FIRE, 1, true);
			}
			if (h) {
				im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
				im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				im.addItemFlags(ItemFlag.HIDE_DESTROYS);
				im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				im.addItemFlags(ItemFlag.HIDE_PLACED_ON);
				im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			}
			is.setItemMeta(im);
			is.setDurability((short)d);
			is.setAmount(i);
		}
		return is;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSkull(String s, List<String> l, String p) {
		ItemStack is = new ItemStack(Material.SKULL_ITEM); {
			is.setDurability((short)3);
			SkullMeta im = (SkullMeta)is.getItemMeta();
			im.setDisplayName(s);
			im.setLore(l);
			im.setOwner(p);
			is.setItemMeta(im);
		}
		return is;
	}
	
	public static ItemStack getBook(String title, String author, String... pages) {
		BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
		meta.setTitle(title);
		meta.setAuthor(author);
		meta.setPages(pages);
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		book.setItemMeta(meta);
		return book;
	}
	
	public static ItemStack getBookJson(String title, String author, BaseComponent... components) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setTitle(title);
		meta.setAuthor(author);
		try {
			@SuppressWarnings("unchecked")
			List<IChatBaseComponent> pages = (List<IChatBaseComponent>) CraftMetaBook.class.getDeclaredField("pages").get(meta);
			for (BaseComponent component : components) {
				IChatBaseComponent page = ChatSerializer.a(ComponentSerializer.toString(component));
				pages.add(page);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		book.setItemMeta(meta);
		return book;
	}
	
	public static void openBook(Player p, String title, String author, String... pages) {
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, getBook(title, author, pages));
		try {
			PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
			pc.getModifier().writeDefaults();
			ByteBuf bf = Unpooled.buffer(256);
			bf.setByte(0, (byte) 0);
			bf.writerIndex(1);
			pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
			pc.getStrings().write(0, "MC|BOpen");
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		p.getInventory().setItem(slot, old);
	}
	
	public static void openBookJson(Player p, String title, String author, BaseComponent...components) {
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, getBookJson(title, author, components));
		try {
			PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
			pc.getModifier().writeDefaults();
			ByteBuf bf = Unpooled.buffer(256);
			bf.setByte(0, (byte) 0);
			bf.writerIndex(1);
			pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
			pc.getStrings().write(0, "MC|BOpen");
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		p.getInventory().setItem(slot, old);
	}
	
	public static boolean isNotAir(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			return true;
		}
		return false;
	}
	
	public static boolean hasDisplayName(ItemStack item) {
		if (isNotAir(item) && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			return true;
		}
		return false;
	}
	
	public static String getDisplayName(ItemStack item) {
		if (hasDisplayName(item)) {
			return item.getItemMeta().getDisplayName();
		}
		return "";
	}
	
	public static void teleport(Player player, Location location) {
		if (core.getCoreCommands().getTeleportingPlayers().contains(player.getName())) {
			return;
		}
		core.getCoreCommands().getTeleportingPlayers().add(player.getName());
		player.sendMessage("§7§oTunnet taikuuden kihelmöivän vaikutuksen. Sinua teleportataan...");
		new BukkitRunnable() {
			int i = 0;
			public void run() {
				i++;
				player.sendTitle("", getBar("§a", "§7", 32, i, 80, "┃"), 0, 20, 0);
				if (!core.getCoreCommands().getTeleportingPlayers().contains(player.getName())) {
					core.getCoreCommands().getTeleportingPlayers().remove(player.getName());
					player.stopSound(Sound.BLOCK_PORTAL_TRIGGER);
					player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
					player.removePotionEffect(PotionEffectType.CONFUSION);
					player.sendMessage("§c§oTeleporttaaminen keskeytyi liikkumisen vuoksi!");
					player.sendTitle("", getBar("§c", "§7", 32, i, 80, "┃"), 0, 5, 5);
					cancel();
					return;
				}
				if (i == 1) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 210, 0, true, false));
					player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1, 0.3f);
				}
				if (i >= 80) {
					core.getCoreCommands().getTeleportingPlayers().remove(player.getName());
					player.teleport(location);
					player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 0.1f);
					player.removePotionEffect(PotionEffectType.CONFUSION);
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true, false));
					player.sendTitle("", getBar("§a", "§7", 32, i, 80, "┃"), 0, 1, 5);
					cancel();
				}
			}
		}.runTaskTimer(core, 0, 2);
	}
	
	public static Location loadLocation(Plugin plugin, String path) {
		FileConfiguration config = plugin.getConfig();
		try {
			String world = config.getString(path + ".world");
			double x = config.getDouble(path + ".x");
			double y = config.getDouble(path + ".y");
			double z = config.getDouble(path + ".z");
			double yaw = config.getDouble(path + ".yaw");
			double pitch = config.getDouble(path + ".pitch");
			return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static void setLocation(Plugin plugin, String path, Location location) {
		FileConfiguration config = plugin.getConfig();
		config.set(path + ".world", location.getWorld().getName());
		config.set(path + ".x", location.getX());
		config.set(path + ".y", location.getY());
		config.set(path + ".z", location.getZ());
		config.set(path + ".yaw", location.getYaw());
		config.set(path + ".pitch", location.getPitch());
		plugin.saveConfig();
	}
	
	public static long getMillisecondsFromStartOfDay(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		calendar.setTimeInMillis(millis);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return millis - calendar.getTimeInMillis();
	}
	
	public static String getFriendlyTimeString(long time) {
		try {
			
			SimpleDateFormat f1 = new SimpleDateFormat("HH");
			f1.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			SimpleDateFormat f2 = new SimpleDateFormat("mm");
			f2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			
			int hours = Integer.parseInt(f1.format(new Date(time)));
			int minutes = Integer.parseInt(f2.format(new Date(time)));
			int hoursChanger = 0;
			
			String beginString;
			if (minutes <= 8) {
				beginString = "tasan";
			}
			else if (minutes <= 23) {
				beginString = "varttia yli";
			}
			else if (minutes <= 38) {
				beginString = "puoli";
				hoursChanger = 1;
			}
			else if (minutes <= 53) {
				beginString = "varttia vaille";
				hoursChanger = 1;
			}
			else {
				beginString = "tasan";
				hoursChanger = 1;
			}
			
			String endString;
			if (hours >= 22) {
				endString = "yöllä";
			}
			else if (hours >= 16) {
				endString = "illalla";
			}
			else if (hours >= 10) {
				endString = "päivällä";
			}
			else if (hours >= 4) {
				endString = "aamulla";
			}
			else {
				endString = "yöllä";
			}
			
			int finalHours = hours + hoursChanger;
			String middleString;
			if (finalHours > 12) {
				finalHours -= 12;
			}
			if (finalHours == 0) {
				middleString = " kaksitoista ";
			}
			else if (finalHours == 1) {
				middleString = " yksi ";
			}
			else if (finalHours == 2) {
				middleString = " kaksi ";
			}
			else if (finalHours == 3) {
				middleString = " kolme ";
			}
			else if (finalHours == 4) {
				middleString = " neljä ";
			}
			else if (finalHours == 5) {
				middleString = " viisi ";
			}
			else if (finalHours == 6) {
				middleString = " kuusi ";
			}
			else if (finalHours == 7) {
				middleString = " seitsemän ";
			}
			else if (finalHours == 8) {
				middleString = " kahdeksan ";
			}
			else if (finalHours == 9) {
				middleString = " yhdeksän ";
			}
			else if (finalHours == 10) {
				middleString = " kymmenen ";
			}
			else if (finalHours == 11) {
				middleString = " yksitoista ";
			}
			else {
				middleString = " kaksitoista ";
			}
			
			return "§aKello on nyt §l" + beginString + middleString + "§a" + endString;
		} catch (NumberFormatException e) {
			return "<?>";
		}
	}
	
	public static String getFriendlyDateString(long time) {
		try {
			
			SimpleDateFormat f1 = new SimpleDateFormat("u");
			f1.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			SimpleDateFormat f2 = new SimpleDateFormat("dd");
			f2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			SimpleDateFormat f3 = new SimpleDateFormat("MM");
			f3.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			SimpleDateFormat f4 = new SimpleDateFormat("yyyy");
			f4.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			
			int dayOfWeek = Integer.parseInt(f1.format(new Date(time)));
			int day = Integer.parseInt(f2.format(new Date(time)));
			int month = Integer.parseInt(f3.format(new Date(time)));
			int year = Integer.parseInt(f4.format(new Date(time)));
			
			String dayOfWeekString;
			if (dayOfWeek == 1) {
				dayOfWeekString = "maanantai";
			}
			else if (dayOfWeek == 2) {
				dayOfWeekString = "tiistai";
			}
			else if (dayOfWeek == 3) {
				dayOfWeekString = "keskiviikko";
			}
			else if (dayOfWeek == 4) {
				dayOfWeekString = "torstai";
			}
			else if (dayOfWeek == 5) {
				dayOfWeekString = "perjantai";
			}
			else if (dayOfWeek == 6) {
				dayOfWeekString = "lauantai";
			}
			else {
				dayOfWeekString = "sunnuntai";
			}
			
			String monthString;
			if (month == 1) {
				monthString = "tammikuuta";
			}
			else if (month == 2) {
				monthString = "helmikuuta";
			}
			else if (month == 3) {
				monthString = "maaliskuuta";
			}
			else if (month == 4) {
				monthString = "huhtikuuta";
			}
			else if (month == 5) {
				monthString = "toukokuuta";
			}
			else if (month == 6) {
				monthString = "kesäkuuta";
			}
			else if (month == 7) {
				monthString = "heinäkuuta";
			}
			else if (month == 8) {
				monthString = "elokuuta";
			}
			else if (month == 9) {
				monthString = "syyskuuta";
			}
			else if (month == 10) {
				monthString = "lokakuuta";
			}
			else if (month == 11) {
				monthString = "marraskuuta";
			}
			else {
				monthString = "joulukuuta";
			}
			
			return dayOfWeekString + " " + day + ". " + monthString + " " + year;
		} catch (NumberFormatException e) {
			return "<?>";
		}
	}
	
	public static String firstUpperCase(String string) {
		String s1 = string.substring(1);
		String s2 = "" + string.charAt(0);
		return s2.toUpperCase() + s1;
	}
	
	public static String getHoursAndMinsFromMillis(long l) {
		
		l = l / 1000;
		int hours = 0;
		int mins = 0;
		
		while (l >= 3600) {
			l -= 3600;
			hours++;
		}
		
		while (l >= 60) {
			l -= 60;
			mins++;
		}
		
		String hourString;
		if (hours == 0) {
			hourString = "";
		}
		else if (hours == 1) {
			hourString = "1 tunti";
		}
		else {
			hourString = hours + " tuntia";
		}
		
		String minString;
		if (mins == 0) {
			minString = "";
		}
		else if (mins == 1) {
			minString = "1 minuutti";
		}
		else {
			minString = mins + " minuuttia";
		}
		
		String s = (hourString + " " + minString).trim();
		
		if (s.isEmpty()) {
			s = "0 minuuttia";
		}
		
		return s;
	}
	
	public static String getDaysAndHoursAndMinsFromMillis(long l) {
		
		l = l / 1000;
		int days = 0;
		int hours = 0;
		int mins = 0;
		
		while (l >= 86400) {
			l-= 86400;
			days++;
		}
		
		while (l >= 3600) {
			l -= 3600;
			hours++;
		}
		
		while (l >= 60) {
			l -= 60;
			mins++;
		}
		
		String dayString;
		if (days == 0) {
			dayString = "";
		}
		else if (days == 1) {
			dayString = "1 päivä";
		}
		else {
			dayString = days + " päivää";
		}
		
		String hourString;
		if (hours == 0) {
			hourString = "";
		}
		else if (hours == 1) {
			hourString = "1 tunti";
		}
		else {
			hourString = hours + " tuntia";
		}
		
		String minString;
		if (mins == 0) {
			minString = "";
		}
		else if (mins == 1) {
			minString = "1 minuutti";
		}
		else {
			minString = mins + " minuuttia";
		}
		
		String s = (dayString + " " + hourString + " " + minString).replace("  ", " ").trim();
		
		if (s.isEmpty()) {
			s = "0 minuuttia";
		}
		
		return s;
	}
	
	public static TextComponent getAcceptDeny(String beginning, String space, String acceptDescription, String denyDescription, 
			String acceptCommand, String denyCommand) {
		
		ClickEvent acceptClickEvent = new ClickEvent(Action.RUN_COMMAND, acceptCommand);
		ClickEvent denyClickEvent = new ClickEvent(Action.RUN_COMMAND, denyCommand);
		HoverEvent acceptHoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder(acceptDescription).create());
		HoverEvent denyHoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder(denyDescription).create());
		
		TextComponent baseComponent = new TextComponent(beginning);
		BaseComponent[] acceptComponents = new ComponentBuilder("§a§l[HYVÄKSY]")
				.event(acceptClickEvent).event(acceptHoverEvent).create();
		BaseComponent[] spaceComponents = new ComponentBuilder(space).create();
		BaseComponent[] denyComponents = new ComponentBuilder("§c§l[HYLKÄÄ]")
				.event(denyClickEvent).event(denyHoverEvent).create();
		
		for (BaseComponent component : acceptComponents) {
			baseComponent.addExtra(component);
		}
		for (BaseComponent component : spaceComponents) {
			baseComponent.addExtra(component);
		}
		for (BaseComponent component : denyComponents) {
			baseComponent.addExtra(component);
		}
		
		return baseComponent;
	}
	
	public static void sendPostASync(String urlString, Map<String, String> params) {
		new BukkitRunnable() {
			public void run() {
				try {
					
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					
					StringJoiner joiner = new StringJoiner("&");
					for (Map.Entry<String, String> entry : params.entrySet()) {
						joiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
					}
					byte[] out = joiner.toString().getBytes(StandardCharsets.UTF_8);
					
					connection.setDoOutput(true);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
					connection.setRequestProperty("User-Agent", "RK (2.0)");
					connection.getOutputStream().write(out);
					connection.getResponseCode();
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(core);
	}
	
	public static void sendJsonASync(String urlString, String json) {
		new BukkitRunnable() {
			public void run() {
				try {
					
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					
					byte[] out = json.getBytes(StandardCharsets.UTF_8);
					
					connection.setDoOutput(true);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
					connection.setRequestProperty("User-Agent", "RK (2.0)");
					connection.getOutputStream().write(out);
					connection.getResponseCode();
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(core);
	}
}