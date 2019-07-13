package me.t4tu.rkcore.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.meowj.langutils.lang.LanguageHelper;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.parties.Party;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import me.t4tu.rkmobs.Mob;
import me.t4tu.rkmobs.Mobs;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CoreListener implements Listener {
	
	private Core core;
	private boolean maintenanceMode;
	private List<String> stunCooldown;
	private List<String> cannonCooldown;
	private List<Location> fireSpreadCooldown;
	private List<Villager> npcLook;
	
	public CoreListener(Core core) {
		this.core = core;
		maintenanceMode = false;
		stunCooldown = new ArrayList<String>();
		cannonCooldown = new ArrayList<String>();
		fireSpreadCooldown = new ArrayList<Location>();
		npcLook = new ArrayList<Villager>();
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	
	public boolean getMaintenanceMode() {
		return maintenanceMode;
	}
	
	public void setMaintenanceMode(boolean maintenanceMode) {
		this.maintenanceMode = maintenanceMode;
	}
	
	public List<Location> getFireSpreadCooldown() {
		return fireSpreadCooldown;
	}
	
	public List<Villager> getNPCLook() {
		return npcLook;
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerCommandPreprocess
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		
		Player player = e.getPlayer();
		String message = e.getMessage().substring(1).toLowerCase();
		
		if (core.getConfig().getBoolean("users." + player.getName() + ".jail.jailed")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää komentoja ollessassi vankilasssa!");
			e.setCancelled(true);
			return;
		}
		
		if (core.getCoreCommands().getMailWritingPlayers().containsKey(player.getName())) {
			core.getCoreCommands().getMailWritingPlayers().remove(player.getName());
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Peruutettiin viestin kirjoittaminen komennon suorittamisen takia!");
			e.setCancelled(true);
			return;
		}
		
		if (!message.equalsIgnoreCase("afk") && !message.startsWith("afk ")) {
			CoreUtils.setAfkCounter(player, 0);
		}
		
		if (message.equalsIgnoreCase("tps") || message.startsWith("tps ")) {
			message = "ping";
			e.setMessage("/ping");
		}
		
		// TODO lisää "tarkoititkohan?"-ehdotuksia
		
		if (message.equalsIgnoreCase("tpa") || message.startsWith("tpa ")) {
			player.sendMessage(tc3 + "Tuntematon komento! Etsitkö kenties komentoa " + tc4 + "/kaveri tp" + tc3 + "?");
			e.setCancelled(true);
			return;
		}
		
		if (message.equalsIgnoreCase("ontime") || message.startsWith("ontime ")) {
			player.sendMessage(tc3 + "Tuntematon komento! Etsitkö kenties komentoa " + tc4 + "/pelaaja" + tc3 + "?");
			e.setCancelled(true);
			return;
		}
		
		if (message.equalsIgnoreCase("arkkitehti") && CoreUtils.hasRank(player, "arkkitehti")) {
			if (CoreUtils.getBuilderPowers().contains(player.getName())) {
				CoreUtils.getBuilderPowers().remove(player.getName());
				player.sendMessage(tc2 + "Rakennusoikeudet: §cdeaktivoitu");
				player.updateCommands();
				// TODO
			}
			else {
				CoreUtils.getBuilderPowers().add(player.getName());
				player.sendMessage(tc2 + "Rakennusoikeudet: §aaktivoitu");
				player.updateCommands();
				// TODO
			}
			e.setCancelled(true);
			return;
		}
		
		if (message.equalsIgnoreCase("ylläpitäjä") && CoreUtils.hasRank(player, "ylläpitäjä")) {
			if (CoreUtils.getAdminPowers().contains(player.getName())) {
				CoreUtils.getAdminPowers().remove(player.getName());
				player.sendMessage(tc2 + "Ylläpito-oikeudet: §cdeaktivoitu");
				player.updateCommands();
				// TODO
			}
			else {
				CoreUtils.getAdminPowers().add(player.getName());
				player.sendMessage(tc2 + "Ylläpito-oikeudet: §aaktivoitu");
				player.updateCommands();
				// TODO
			}
			e.setCancelled(true);
			return;
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (CoreUtils.hasRank(p, "ylläpitäjä") && core.getCoreCommands().getCommandSpyPlayers().contains(p.getName())) {
				p.sendMessage("§8" + player.getName() + ": " + e.getMessage());
			}
		}
		
		if (!CoreUtils.hasAdminPowers(player) && !CoreUtils.hasBuilderPowers(player)) {
			for (String command : CoreUtils.getRegisteredCommandsWithTabCompletion()) {
				if (message.equalsIgnoreCase(command) || message.startsWith(command + " ")) {
					return;
				}
			}
			if (CoreUtils.hasRank(player, "valvoja") || CoreUtils.hasRank(player, "arkkitehti")) {
				for (String command : CoreUtils.getRegisteredCommands()) {
					if (message.equalsIgnoreCase(command) || message.startsWith(command + " ")) {
						return;
					}
				}
				for (String command : CoreUtils.getRegisteredStaffCommands()) {
					if (message.equalsIgnoreCase(command) || message.startsWith(command + " ")) {
						return;
					}
				}
			}
			player.sendMessage(CoreUtils.getNoPermissionString());
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerCommandSend
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerCommandSend(PlayerCommandSendEvent e) {
		Player player = e.getPlayer();
		if (!CoreUtils.hasAdminPowers(player) && !CoreUtils.hasBuilderPowers(player)) {
			Iterator<String> iterator = e.getCommands().iterator();
			while (iterator.hasNext()) {
				String command = iterator.next();
				if (CoreUtils.hasRank(player, "valvoja") || CoreUtils.hasRank(player, "arkkitehti")) {
					if (!CoreUtils.getRegisteredCommandsWithTabCompletion().contains(command) && !CoreUtils.getRegisteredCommands().contains(command) &&
							!CoreUtils.getRegisteredStaffCommands().contains(command)) {
						iterator.remove();
					}
				}
				else {
					if (!CoreUtils.getRegisteredCommandsWithTabCompletion().contains(command)) {
						iterator.remove();
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerPreLogin
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
		
		// TODO async
		
		String name = e.getName();
		String uuid = e.getUniqueId().toString();
		String uuidWithoutDashes = uuid.replace("-", "");
		String ip = e.getAddress().toString().substring(1);
		
		// jos IP-osoite estetty
		
		if (!core.isNoSQL() && MySQLUtils.contains("SELECT * FROM ipbans WHERE ip=?", ip)) {
			String joined_uuids = MySQLUtils.get("SELECT * FROM ipbans WHERE ip=?", ip).getStringNotNull(0, "joined_uuids");
			if (!joined_uuids.contains(uuid)) {
				joined_uuids = joined_uuids + uuid + ";";
				MySQLUtils.set("UPDATE ipbans SET joined_uuids=? WHERE ip=?", joined_uuids, ip);
			}
			e.disallow(Result.KICK_OTHER, "§c§m--------------------------------\n§c\n§cPalvelimelle liittyminen tästä "
					+ "IP-osoitteesta on estetty.\n§c\n§c§m--------------------------------");
			return;
		}
		
		// jos palvelin on huoltotilassa
		
		List<String> whitelisted = new ArrayList<String>();
		for (OfflinePlayer offlinePlayer : Bukkit.getWhitelistedPlayers()) {
			whitelisted.add(offlinePlayer.getName());
		}
		
		if (core.getConfig().getBoolean("maintenance-mode") && !whitelisted.contains(name)) {
			e.disallow(Result.KICK_OTHER, "§c§m----------§c§l Huoltokatko §c§m----------\n§c\n§cPalvelimella suoritetaan "
					+ "huoltotoimenpiteitä, ja tämän takia palvelimelle liittyminen on väliaikaisesti estetty."
					+ "\n§c\n§c§m--------------------------------");
			return;
		}
		
		if (core.isNoSQL()) {
			return;
		}
		
		// päivitetään pelaajan nimi UUID:n perusteella, mikäli nimi on muuttunut TODO
		
		MySQLUtils.set("UPDATE player_info SET name=?, ip=? WHERE uuid=?", name, ip, uuid);
		MySQLUtils.set("UPDATE player_stats SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE player_homes SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE player_settings SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE player_professions SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE player_history SET name=? WHERE uuid=?", name, uuidWithoutDashes);
		MySQLUtils.set("UPDATE player_ban SET name=? WHERE uuid=?", name, uuidWithoutDashes);
		MySQLUtils.set("UPDATE player_mute SET name=? WHERE uuid=?", name, uuidWithoutDashes);
		MySQLUtils.set("UPDATE player_jail SET name=? WHERE uuid=?", name, uuidWithoutDashes);
		MySQLUtils.set("UPDATE player_fines SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE player_notes SET name=? WHERE uuid=?", name, uuid);
		MySQLUtils.set("UPDATE guilds SET leader_name=? WHERE leader_uuid=?", name, uuid);
		
		// estetään useampaa "samannimistä" pelaajaa aiheuttamasta sotkuja
		
		Random random = new Random();
		
		String unknownName = "tuntematon#" + (random.nextInt(9000) + 1000);
		
		MySQLUtils.set("UPDATE player_info SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_stats SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_homes SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_settings SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_professions SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_history SET name=? WHERE uuid!=? AND name=?", unknownName, uuidWithoutDashes, name);
		MySQLUtils.set("UPDATE player_ban SET name=? WHERE uuid!=? AND name=?", unknownName, uuidWithoutDashes, name);
		MySQLUtils.set("UPDATE player_mute SET name=? WHERE uuid!=? AND name=?", unknownName, uuidWithoutDashes, name);
		MySQLUtils.set("UPDATE player_jail SET name=? WHERE uuid!=? AND name=?", unknownName, uuidWithoutDashes, name);
		MySQLUtils.set("UPDATE player_fines SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE player_notes SET name=? WHERE uuid!=? AND name=?", unknownName, uuid, name);
		MySQLUtils.set("UPDATE guilds SET leader_name=? WHERE leader_uuid!=? AND leader_name=?", unknownName, uuid, name);
		
		// jos porttikiellossa
		
		if (MySQLUtils.contains("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes)) {
			MySQLResult banData = MySQLUtils.get("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes);
			boolean unbanned = false;
			long banDuration = banData.getLong(0, "duration");
			if (banDuration != 0) {
				if (System.currentTimeMillis() >= banDuration) {
					// porttikielto vanhentunut, poistetaan
					MySQLUtils.set("DELETE FROM player_ban WHERE uuid=?", uuidWithoutDashes);
					unbanned = true;
				}
			}
			if (!unbanned) {
				// porttikielto voimassa, estetään liittyminen
				if (banDuration != 0) {
					e.disallow(Result.KICK_OTHER, "§c§m--------------------------------\n§c \n§cSinulle on annettu porttikielto "
							+ "tälle palvelimelle seuraavalla syyllä:\n§c \n§c§o" + banData.getString(0, "reason") 
							+ "\n§c \n§c \n§7Aikaa jäjellä: " + CoreUtils.getDaysAndHoursAndMinsFromMillis(banDuration - System.currentTimeMillis()) 
							+ ".\n§c \n§c§m--------------------------------");
				}
				else {
					e.disallow(Result.KICK_OTHER, "§c§m--------------------------------\n§c \n§cSinulle on annettu porttikielto "
							+ "tälle palvelimelle seuraavalla syyllä:\n§c \n§c§o" + banData.getString(0, "reason") 
							+ "\n§c \n§c \n§7Tämä porttikielto on ikuinen." 
							+ "\n§c \n§c§m--------------------------------");
				}
				return;
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerJoin
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		
		e.setJoinMessage(null);
		
		if (core.isNoSQL()) {
			Player player = e.getPlayer();
			String name = player.getName();
			core.getConfig().set("users." + name + ".chat_prefix", "");
			core.getConfig().set("users." + name + ".chat_color", "&7");
			core.getConfig().set("users." + name + ".rank", "default");
			core.getConfig().set("users." + name + ".status", "<tilaviesti>");
			core.saveConfig();
			for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage("§7" + name + " liittyi palvelimelle.");
			}
			CoreUtils.updatePermissions(player);
			CoreUtils.updateTabForAll();
			CoreUtils.updateVanish();
			CoreUtils.setAfkCounter(player, 0);
			player.sendMessage("§cEi yhteyttä MySQL-tietokantaan! Palvelin on rajoitetussa tilassa.");
			return;
		}
		
		new BukkitRunnable() {
			
			boolean muted = false;
			boolean jailed = false;
			boolean firstTimeJoining = false;
			
			Player player = e.getPlayer();
			String name = player.getName();
			String uuid = player.getUniqueId().toString();
			String uuidWithoutDashes = uuid.replace("-", "");
			String ip = player.getAddress().toString().substring(1).split(":")[0];
			
			public void run() {
				
				// jos pelaaja on hiljennetty
				
				if (MySQLUtils.contains("SELECT * FROM player_mute WHERE uuid=?", uuidWithoutDashes)) {
					MySQLResult muteData = MySQLUtils.get("SELECT * FROM player_mute WHERE uuid=?", uuidWithoutDashes);
					boolean unmuted = false;
					long muteDuration = muteData.getLong(0, "duration");
					if (muteDuration != 0) {
						if (System.currentTimeMillis() >= muteDuration) {
							// hiljennys vanhentunut, poistetaan
							MySQLUtils.set("DELETE FROM player_mute WHERE uuid=?", uuidWithoutDashes);
							unmuted = true;
							new BukkitRunnable() {
								public void run() {
									core.getPunishmentCommands().sendUnmuteInfo(player);
								}
							}.runTask(core);
						}
					}
					if (!unmuted) {
						// hiljennys voimassa, asetetaan tiedot configiin
						core.getConfig().set("users." + name + ".mute.muted", true);
						core.getConfig().set("users." + name + ".mute.reason", muteData.getString(0, "reason"));
						core.getConfig().set("users." + name + ".mute.duration", muteData.getLong(0, "duration"));
						core.saveConfig();
						muted = true;
						new BukkitRunnable() {
							public void run() {
								core.getPunishmentCommands().sendMuteInfo(player, muteData.getString(0, "reason"), muteData.getLong(0, "duration"));
							}
						}.runTask(core);
					}
				}
				
				// jos pelaaja vankilassa
				
				if (MySQLUtils.contains("SELECT * FROM player_jail WHERE uuid=?", uuidWithoutDashes)) {
					MySQLResult jailData = MySQLUtils.get("SELECT * FROM player_jail WHERE uuid=?", uuidWithoutDashes);
					boolean unjailed = false;
					long jailDuration = jailData.getLong(0, "duration");
					if (jailDuration != 0) {
						if (System.currentTimeMillis() >= jailDuration) {
							// vankilatuomio vanhentunut, poistetaan
							MySQLUtils.set("DELETE FROM player_jail WHERE uuid=?", uuidWithoutDashes);
							unjailed = true;
							new BukkitRunnable() {
								public void run() {
									core.getPunishmentCommands().releaseFromJail(player);
								}
							}.runTask(core);
						}
					}
					if (!unjailed) {
						// vankilatuomio voimassa, asetetaan tiedot configiin
						core.getConfig().set("users." + name + ".jail.jailed", true);
						core.getConfig().set("users." + name + ".jail.reason", jailData.getString(0, "reason"));
						core.getConfig().set("users." + name + ".jail.duration", jailData.getLong(0, "duration"));
						core.saveConfig();
						jailed = true;
						new BukkitRunnable() {
							public void run() {
								core.getPunishmentCommands().teleportToJail(player, jailData.getString(0, "reason"), jailData.getLong(0, "duration"));
							}
						}.runTask(core);
					}
				}
				
				// asetetaan pelaajan tiedot, jos ensimmäinen liittymiskerta
				
				if (!MySQLUtils.contains("SELECT * FROM player_info WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_info (name, uuid, ip, last_seen) "
							+ "VALUES (?, ?, ?, " + System.currentTimeMillis() + ")", name, uuid, ip);
					MySQLUtils.set("UPDATE global SET uniquejoins=uniquejoins+1");
					firstTimeJoining = true;
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_stats WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_stats (name, uuid, friends) VALUES "
							+ "(?, ?, ?)", name, uuid, "");
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_homes WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_homes (name, uuid) VALUES (?, ?)", name, uuid);
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_settings WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_settings (name, uuid) VALUES (?, ?)", name, uuid);
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_chests WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_chests (uuid) VALUES (?)", uuid);
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_cosmetics WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_cosmetics (uuid) VALUES (?)", uuid);
				}
				
				if (!MySQLUtils.contains("SELECT * FROM player_story WHERE uuid=?", uuid)) {
					MySQLUtils.set("INSERT INTO player_story (uuid) VALUES (?)", uuid);
				}
				
				// asetetaan tiedot configiin
				
				MySQLResult infoData = MySQLUtils.get("SELECT * FROM player_info WHERE uuid=?", uuid);
				MySQLResult statsData = MySQLUtils.get("SELECT * FROM player_stats WHERE uuid=?", uuid);
				core.getConfig().set("users." + name + ".chat_prefix", infoData.getStringNotNull(0, "chat_prefix"));
				core.getConfig().set("users." + name + ".chat_color", infoData.getStringNotNull(0, "chat_color"));
				core.getConfig().set("users." + name + ".chat_nick", infoData.getString(0, "chat_nick"));
				core.getConfig().set("users." + name + ".rank", infoData.getString(0, "rank"));
				core.getConfig().set("users." + name + ".status", statsData.getString(0, "status"));
				core.saveConfig();
				
				// pelaajaennätys ja -määrä
				
				MySQLResult globalData = MySQLUtils.get("SELECT * FROM global");
				int playerRecord = globalData.getInt(0, "record");
				if (Bukkit.getOnlinePlayers().size() > playerRecord) {
					MySQLUtils.set("UPDATE global SET record=" + Bukkit.getOnlinePlayers().size());
				}
				MySQLUtils.set("UPDATE global SET online=" + Bukkit.getOnlinePlayers().size());
				
				// liittymisviesti
				
				List<String> friends = CoreUtils.getFriendsUuids(name);
				
				for (Player p : Bukkit.getOnlinePlayers()) {
					String friendPrefix = "";
					if (friends.contains(p.getUniqueId().toString())) {
						friendPrefix = "§l";
						if (SettingsUtils.getSetting(p, "play_sound_friends")) {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 2);
						}
					}
					if (firstTimeJoining) {
						p.sendMessage("§7" + friendPrefix + name + " liittyi palvelimelle ensimmäistä kertaa.");
					}
					else if (jailed) {
						p.sendMessage("§7" + friendPrefix + name + " liittyi palvelimelle vangittuna.");
					}
					else if (muted) {
						p.sendMessage("§7" + friendPrefix + name + " liittyi palvelimelle hiljennettynä.");
					}
					else {
						p.sendMessage("§7" + friendPrefix + name + " liittyi palvelimelle.");
					}
				}
				
				// päivitetään asetukset configiin
				
				SettingsUtils.reloadSettings(player);
				
				// päivitetään permissionit
				
				CoreUtils.updatePermissions(player);
				
				// päivitetään mahd. huomautukset configiin
				
				CoreUtils.updateNotes(player);
				
				// päivitetään pelaajalista
				
				CoreUtils.updateTabForAll();
				
				// päivitetään vanish
				
				CoreUtils.updateVanish();
				
				// afk
				
				CoreUtils.setAfkCounter(player, 0);
				
				// tutoriaali suoritettu
				
				boolean finishedTutorial = core.getTutorial().hasFinishedTutorial(player);
				
				if (finishedTutorial) {
					
					// tervetuloviesti (sekunnin viiveellä)
					
					new BukkitRunnable() {
						public void run() {
							player.sendTitle("§a§lRoyal Kingdom", "§7Tervetuloa takaisin, " + name, 20, 40, 20);
						}
					}.runTaskLaterAsynchronously(core, 20);
					
					// MOTD
					
					if (!core.getConfig().getBoolean("motd.seen." + name) && core.getConfig().contains("motd.motd")) {
						new BukkitRunnable() {
							public void run() {
								player.performCommand("motd");
							}
						}.runTask(core);
					}
					
					// tiketit
					
					if (CoreUtils.hasRank(player, "valvoja")) {
						int counter = 0;
						if (core.getConfig().getConfigurationSection("tickets") == null) {
							return;
						}
						for (String s : core.getConfig().getConfigurationSection("tickets").getKeys(false)) {
							if (!core.getConfig().getBoolean("tickets." + s + ".suljettu")) {
								counter++;
							}
						}
						if (counter > 0) {
							player.sendMessage("");
							player.sendMessage(" §6§lHuomio! §eYhteensä §6" + counter + "§e tikettiä on hoitamatta!");
							player.sendMessage("");
						}
					}
					
					// posti
					
					new BukkitRunnable() {
						public void run() {
							MySQLResult mailData = MySQLUtils.get("SELECT * FROM player_mails WHERE receiver=?", uuid);
							if (mailData != null) {
								boolean unreadMessages = false;
								for (int i = 0; i < mailData.getRows(); i++) {
									if (!mailData.getBoolean(i, "seen")) {
										unreadMessages = true;
									}
								}
								if (unreadMessages) {
									player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
									player.sendMessage("");
									player.sendMessage(tc2 + " Sinulla on lukemattomia viestejä postilaatikossasi! Voit lukea ne komennolla " 
											+ tc1 + "/posti" + tc2 + ".");
									player.sendMessage("");
								}
							}
						}
					}.runTaskLaterAsynchronously(core, 80);
					
					// grief-ilmoitukset
					
					new BukkitRunnable() {
						public void run() {
							int i = core.getConfig().getInt("ticketinfo." + name);
							if (i > 0) {
								player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								player.sendMessage("");
								player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketit" + tc2 + "§m----------");
								player.sendMessage("");
								player.sendMessage(tc2 + " Henkilökuntamme on poissa ollessasi käsitellyt yhden tai useamman tiketeistäsi.");
								player.sendMessage("");
							}
							else if (i < 0) {
								player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
								player.sendMessage("");
								player.sendMessage(tc2 + "§m----------" + tc1 + " Tiketit" + tc2 + "§m----------");
								player.sendMessage("");
								player.sendMessage(tc2 + " Henkilökuntamme on poissa ollessasi avannut uudelleen yhden tai useamman tiketeistäsi.");
								player.sendMessage("");
							}
							core.getConfig().set("ticketinfo." + name, null);
							core.saveConfig();
						}
					}.runTaskLater(core, 100);
					
					// sakkomaksut
					
					new BukkitRunnable() {
						public void run() {
							MySQLResult finesData = MySQLUtils.get("SELECT * FROM player_fines WHERE uuid=?", uuid);
							if (finesData != null) {
								int amount = 0;
								for (int i = 0; i < finesData.getRows(); i++) {
									amount += finesData.getInt(i, "amount");
								}
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage("");
								player.sendMessage("§c§m---------------§4 Sakkomaksut §c§m---------------");
								player.sendMessage("");
								player.sendMessage("§c Sinulla on yhteensä §4" + amount + "£§c edestä maksamattomia sakkoja!");
								player.sendMessage("");
								player.sendMessage("§c Voit maksaa sakkosi komennolla §4/sakot");
								player.sendMessage("");
							}
						}
					}.runTaskLaterAsynchronously(core, 120);
				}
				else {
					Location startpoint = CoreUtils.loadLocation(core, "startpoint");
					if (startpoint != null) {
						new BukkitRunnable() {
							public void run() {
								player.teleport(startpoint);
								player.setGameMode(GameMode.SURVIVAL);
							}
						}.runTask(core);
					}
				}
				
				new BukkitRunnable() {
					public void run() {
						if (CoreUtils.hasRank(player, "valvoja")) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vanhentuneetsakot console");
						}
					}
				}.runTaskLater(core, 140);
			}
		}.runTaskAsynchronously(core);
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerQuit
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		
		e.setQuitMessage(null);
		
		if (core.getCoreCommands().getTravelingPlayers().contains(e.getPlayer().getName())) {
			e.getPlayer().setGameMode(GameMode.SURVIVAL);
			e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
		}
		
		if (core.isNoSQL()) {
			Player player = e.getPlayer();
			String name = player.getName();
			core.getConfig().set("users." + name, null);
			core.saveConfig();
			core.getCoreCommands().getVanishedPlayers().remove(player.getName());
			core.getCoreCommands().getGodPlayers().remove(player.getName());
			core.getCoreCommands().getSpyPlayers().remove(player.getName());
			core.getCoreCommands().getCommandSpyPlayers().remove(player.getName());
			core.getCoreCommands().getMailWritingPlayers().remove(player.getName());
			CoreUtils.getBuilderPowers().remove(player.getName());
			CoreUtils.getAdminPowers().remove(player.getName());
			CoreUtils.getAfkCounter().remove(player.getName());
			CoreUtils.getHaltAfkCounter().remove(player.getName());
			core.getCoreCommands().getPermissions().remove(player.getName());
			CoreUtils.updateVanish();
			Party party = core.getPartyManager().getPartyOfPlayer(player);
			if (party != null) {
				ListIterator<String> iterator = party.getMembers().listIterator();
				while (iterator.hasNext()) {
					String member = iterator.next();
					if (member.equals(player.getUniqueId().toString())) {
						iterator.remove();
						for (Player playerMember : party.getMembersAsPlayers()) {
							playerMember.sendMessage(tc1 + player.getName() + tc2 + " poistui partysta!");
						}
						if (party.getMembersAsPlayers().isEmpty()) {
							core.getPartyManager().getParties().remove(party);
						}
						break;
					}
				}
			}
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage("§7" + name + " poistui palvelimelta.");
			}
			return;
		}
		
		new BukkitRunnable() {
			
			boolean banned = false;
			
			Player player = e.getPlayer();
			String name = player.getName();
			String uuid = player.getUniqueId().toString();
			String uuidWithoutDashes = uuid.replace("-", "");
			
			public void run() {
				
				// tarkastetaan oliko porttikiellossa
				
				if (MySQLUtils.contains("SELECT * FROM player_ban WHERE uuid=?", uuidWithoutDashes)) {
					banned = true;
				}
				
				// viimeksi nähty
				
				MySQLUtils.set("UPDATE player_info SET last_seen=" + System.currentTimeMillis() + " WHERE uuid=?", uuid);
				
				// pelaajamäärä
				
				MySQLUtils.set("UPDATE global SET online=" + Bukkit.getOnlinePlayers().size());
				
				// poistetaan tiedot configista
				
				core.getConfig().set("users." + name, null);
				core.saveConfig();
				
				// poista mail/afk yms.
				
				core.getCoreCommands().getVanishedPlayers().remove(player.getName());
				core.getCoreCommands().getGodPlayers().remove(player.getName());
				core.getCoreCommands().getSpyPlayers().remove(player.getName());
				core.getCoreCommands().getCommandSpyPlayers().remove(player.getName());
				core.getCoreCommands().getMailWritingPlayers().remove(player.getName());
				core.getCoreCommands().getTravelingPlayers().remove(player.getName());
				CoreUtils.getBuilderPowers().remove(player.getName());
				CoreUtils.getAdminPowers().remove(player.getName());
				CoreUtils.getAfkCounter().remove(player.getName());
				CoreUtils.getHaltAfkCounter().remove(player.getName());
				
				// poistetaan permissionit
				
				core.getCoreCommands().getPermissions().remove(player.getName());
				
				// päivitetään vanish
				
				CoreUtils.updateVanish();
				
				// poistetaan partysta
				
				Party party = core.getPartyManager().getPartyOfPlayer(player);
				if (party != null) {
					ListIterator<String> iterator = party.getMembers().listIterator();
					while (iterator.hasNext()) {
						String member = iterator.next();
						if (member.equals(player.getUniqueId().toString())) {
							iterator.remove();
							for (Player playerMember : party.getMembersAsPlayers()) {
								playerMember.sendMessage(tc1 + player.getName() + tc2 + " poistui partysta!");
							}
							if (party.getMembersAsPlayers().isEmpty()) {
								core.getPartyManager().getParties().remove(party);
							}
							break;
						}
					}
				}
				
				// liittymisviesti
				
				List<String> friends = CoreUtils.getFriendsUuids(name);
				
				for (Player p : Bukkit.getOnlinePlayers()) {
					String friendPrefix = "";
					if (friends.contains(p.getUniqueId().toString())) {
						friendPrefix = "§l";
					}
					if (!banned) {
						p.sendMessage("§7" + friendPrefix + name + " poistui palvelimelta.");
					}
				}
			}
		}.runTaskAsynchronously(core);
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerDropItem
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (e.getPlayer().getName().equals("evokki0075") && e.getPlayer().getGameMode() == GameMode.CREATIVE && core.getCoreCommands().isEvokkiModeEnabled()) {
			e.getItemDrop().remove();
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onGamemodeChange
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player player = e.getPlayer();
		if (((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SPECTATOR) && e.getNewGameMode() == GameMode.CREATIVE) || 
				(player.getGameMode() == GameMode.CREATIVE && (e.getNewGameMode() == GameMode.SURVIVAL || e.getNewGameMode() == GameMode.ADVENTURE || e.getNewGameMode() == GameMode.SPECTATOR))) {
			if (core.getConfig().contains("inventories." + player.getUniqueId().toString())) {
				ItemStack[] contents = CoreUtils.loadInventory(core, "inventories." + player.getUniqueId().toString());
				CoreUtils.setInventory(core, "inventories." + player.getUniqueId().toString(), player.getInventory().getContents());
				player.getInventory().setContents(contents);
			}
			else {
				CoreUtils.setInventory(core, "inventories." + player.getUniqueId().toString(), player.getInventory().getContents());
				player.getInventory().clear();
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onEntityDismount
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onEntityDismount(EntityDismountEvent e) {
		if (e.getDismounted().getType() == EntityType.ARROW) {
			e.getDismounted().remove();
			new BukkitRunnable() {
				public void run() {
					e.getEntity().teleport(e.getEntity().getLocation().add(0, 1.5, 0));
				}
			}.runTask(core);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onEntityDismount
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (core.getCoreCommands().getTravelingPlayers().contains(e.getPlayer().getName())) {
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockIgnite
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.LAVA || e.getCause() == IgniteCause.LIGHTNING) {
			e.setCancelled(true);
			return;
		}
		if (e.getCause() == IgniteCause.SPREAD && (fireSpreadCooldown.size() > 500 || fireSpreadCooldown.contains(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
		new BukkitRunnable() {
			public void run() {
				if (e.getBlock().getType() == Material.FIRE && e.getBlock().getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK) {
					e.getBlock().setType(Material.AIR);
					if (!fireSpreadCooldown.contains(e.getBlock().getLocation())) {
						fireSpreadCooldown.add(e.getBlock().getLocation());
						new BukkitRunnable() {
							public void run() {
								fireSpreadCooldown.remove(e.getBlock().getLocation());
							}
						}.runTaskLater(core, 2400);
					}
				}
			}
		}.runTaskLater(core, 300);
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockBurn
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent e) {
		e.setCancelled(true);
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onSignChange
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (CoreUtils.hasRank(e.getPlayer(), "ritari")) {
			e.setLine(0, ChatColor.translateAlternateColorCodes('&', e.getLine(0)));
			e.setLine(1, ChatColor.translateAlternateColorCodes('&', e.getLine(1)));
			e.setLine(2, ChatColor.translateAlternateColorCodes('&', e.getLine(2)));
			e.setLine(3, ChatColor.translateAlternateColorCodes('&', e.getLine(3)));
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerEditBook
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent e) {
		if (e.isSigning() && CoreUtils.hasRank(e.getPlayer(), "ritari")) {
			BookMeta meta = e.getNewBookMeta();
			for (int i = 1; i <= meta.getPageCount(); i++) {
				meta.setPage(i, ChatColor.translateAlternateColorCodes('&', meta.getPage(i)));
			}
			e.setNewBookMeta(meta);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onArmorStandManipulate
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (!e.getRightClicked().isVisible()) {
			e.setCancelled(true);
			return;
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onVillagerCareerChange
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onVillagerCareerChange(VillagerCareerChangeEvent e) {
		if (CoreUtils.isNPC(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerFoodLevelChange
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			if (core.getCoreCommands().getGodPlayers().contains(e.getEntity().getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onEntityDamage
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (core.getCoreCommands().getGodPlayers().contains(e.getEntity().getName())) {
				e.setCancelled(true);
			}
			if (core.getTutorial().getPlayersInTutorial().contains(e.getEntity().getName())) {
				e.setCancelled(true);
			}
		}
		if (e.getEntity() instanceof ArmorStand) {
			ArmorStand a = (ArmorStand) e.getEntity();
			if (a.isCustomNameVisible() && !a.hasAI()) {
				e.setCancelled(true);
			}
		}
		if (CoreUtils.isNPC(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onEntityDamageByEntity
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			
			Player player = (Player) e.getDamager();
			
			// veriroiskeet
			
			if (e.getEntity() instanceof LivingEntity && e.getEntityType() != EntityType.ARMOR_STAND) {
				if (!e.isCancelled()) {
					float a = (float) ((LivingEntity) e.getEntity()).getEyeHeight(false);
					Location location = e.getEntity().getLocation().add(0, a - 0.1f, 0);
					BlockData data = Material.REDSTONE_BLOCK.createBlockData();
					location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 10, 0.2f, 0.3f, 0.2f, 0.05f, data);
				}
			}
			
			// tainnutus
			
			if (e.getEntity() instanceof Player) {
				if (!e.isCancelled()) {
					
					Vector dir1 = e.getDamager().getLocation().getDirection();
					Vector dir2 = e.getEntity().getLocation().getDirection();
					
					if (dir1.dot(dir2) > 0) {
						
						Player victim = (Player) e.getEntity();
						ItemStack weapon = player.getInventory().getItemInMainHand();
						
						if (player.getVelocity().getY() < -0.1 && !stunCooldown.contains(player.getName()) && CoreUtils.isNotAir(weapon)) {
							
							int ticks = 0;
							
							if (stunningItems1.contains(weapon.getType())) {
								ticks = 60;
							}
							else if (stunningItems2.contains(weapon.getType())) {
								ticks = 80;
							}
							else if (stunningItems3.contains(weapon.getType())) {
								ticks = 100;
							}
							
							if (ticks != 0) {
								victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
								victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 0));
								victim.sendMessage("§c§oSinua iskettiin takaraivoon, ja tainnut hetkeksi...");
								victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 2);
								player.sendMessage("§7§oIskit vastustajaasi takaraivoon ja tainnutit hänet hetkeksi...");
								player.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 2);
								stunCooldown.add(player.getName());
								new BukkitRunnable() {
									public void run() {
										stunCooldown.remove(player.getName());
									}
								}.runTaskLater(core, 100);
							}
						}
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerRespawn
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (core.getConfig().getBoolean("users." + e.getPlayer().getName() + ".jail.jailed") && core.getConfig().contains("jail")) {
			Location location = CoreUtils.loadLocation(core, "jail");
			e.setRespawnLocation(location);
		}
		else if (core.getConfig().contains("spawn")) {
			Location spawn = CoreUtils.loadLocation(core, "spawn");
			e.setRespawnLocation(spawn);
		}
		else {
			e.setRespawnLocation(e.getPlayer().getWorld().getSpawnLocation());
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerDeath
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		
		Player player = e.getEntity();
		
		e.setDeathMessage(null);
		
		if (CoreUtils.hasRank(player, "arkkitehti") || CoreUtils.hasRank(player, "valvoja")) {
			CoreUtils.setLocation(core, "users." + player.getName() + ".back", player.getLocation());
		}
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			e.setKeepInventory(true);
			e.setKeepLevel(true);
			e.setDroppedExp(0);
		}
		
		List<String> deathNotes = new ArrayList<String>();
		
		if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() != null) {
			if (player.getKiller() != null) {
				for (String note : deathNotesKiller) {
					try {
						String text = note.split("<>")[0];
						DamageCause cause = DamageCause.valueOf(note.split("<>")[1]);
						if (cause.equals(player.getLastDamageCause().getCause())) {
							String using = "";
							ItemStack itemInHand = player.getKiller().getInventory().getItemInMainHand();
							if (CoreUtils.isNotAir(itemInHand)) {
								String weapon = LanguageHelper.getItemDisplayName(itemInHand, "fi_FI");
								using = " käyttäen asetta " + weapon;
							}
							deathNotes.add(text.replace("<victim>", player.getName()).replace("<killer>", player.getKiller().getName())
									.replace("<using>", using));
						}
					}
					catch (ArrayIndexOutOfBoundsException ex) {
					}
					catch (IllegalArgumentException ex) {
					}
				}
			}
			else {
				for (String note : deathNotesNoKiller) {
					try {
						String text = note.split("<>")[0];
						DamageCause cause = DamageCause.valueOf(note.split("<>")[1]);
						if (cause.equals(player.getLastDamageCause().getCause())) {
							if (cause == DamageCause.ENTITY_ATTACK && text.contains("<killer>")) {
								EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) player.getLastDamageCause();
								String killer = LanguageHelper.getEntityName(event.getDamager(), "fi_fi");
								if (event.getDamager().getCustomName() != null) {
									killer = event.getDamager().getCustomName();
									if (Bukkit.getPluginManager().getPlugin("RK-Mobs") != null) {
										Mob mob = Mobs.getMobManager().getMob(event.getDamager());
										if (mob != null) {
											killer = mob.getDisplayName();
										}
									}
								}
								deathNotes.add(text.replace("<victim>", player.getName()).replace("<killer>", killer));
							}
							else if (cause == DamageCause.ENTITY_EXPLOSION && text.contains("<killer>")) {
								EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) player.getLastDamageCause();
								String killer = LanguageHelper.getEntityName(event.getDamager(), "fi_fi");
								if (event.getDamager().getCustomName() != null) {
									killer = event.getDamager().getCustomName();
									if (Bukkit.getPluginManager().getPlugin("RK-Mobs") != null) {
										Mob mob = Mobs.getMobManager().getMob(event.getDamager());
										if (mob != null) {
											killer = mob.getDisplayName();
										}
									}
								}
								deathNotes.add(text.replace("<victim>", player.getName()).replace("<killer>", killer));
							}
							else if (cause == DamageCause.PROJECTILE && text.contains("<killer>")) {
								EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) player.getLastDamageCause();
								Projectile projectile = (Projectile) event.getDamager();
								ProjectileSource source = projectile.getShooter();
								if (source instanceof Entity) {
									Entity shooter = (Entity) source;
									String killer = LanguageHelper.getEntityName(shooter, "fi_fi");
									if (shooter.getCustomName() != null) {
										killer = shooter.getCustomName();
										if (Bukkit.getPluginManager().getPlugin("RK-Mobs") != null) {
											Mob mob = Mobs.getMobManager().getMob(shooter);
											if (mob != null) {
												killer = mob.getDisplayName();
											}
										}
									}
									deathNotes.add(text.replace("<victim>", player.getName()).replace("<killer>", killer));
								}
							}
							else {
								deathNotes.add(text.replace("<victim>", player.getName()));
							}
						}
					}
					catch (ArrayIndexOutOfBoundsException ex) {
					}
					catch (IllegalArgumentException ex) {
					}
				}
			}
		}
		
		String deathNote = "§7" + player.getName() + " kuoli.";
		if (player.getKiller() != null) {
			deathNote = "§7" + player.getKiller().getName() + "§7 tappoi pelaajan " + player.getName() + "§7.";
		}
		
		if (!deathNotes.isEmpty()) {
			Random random = new Random();
			deathNote = deathNotes.get(random.nextInt(deathNotes.size()));
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (SettingsUtils.getSetting(p, "show_death_messages")) {
				p.sendMessage(deathNote);
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerTeleport
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (CoreUtils.hasRank(e.getPlayer(), "arkkitehti") || CoreUtils.hasRank(e.getPlayer(), "valvoja")) {
			if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName()) || e.getFrom().distance(e.getTo()) > 10) {
				CoreUtils.setLocation(core, "users." + e.getPlayer().getName() + ".back", e.getFrom());
			}
		}
		if (e.getCause() == TeleportCause.SPECTATE) {
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerMove
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			core.getCoreCommands().getTeleportingPlayers().remove(e.getPlayer().getName());
		}
		if (e.getFrom().getPitch() != e.getTo().getPitch() || e.getFrom().getYaw() != e.getTo().getYaw()) {
			CoreUtils.setAfkCounter(e.getPlayer(), 0);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockPlace
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (core.getConfig().getBoolean("users." + e.getPlayer().getName() + ".jail.jailed")) {
			e.setCancelled(true);
			return;
		}
		for (int i = 0; i < 3; i++) {
			for (Location location : core.getCoreCommands().getTardisBlocks(i)) {
				Location blockLocation = e.getBlockPlaced().getLocation();
				blockLocation.add(0.5, 0, 0.5);
				if (location.equals(blockLocation)) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onBlockBreak
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (core.getConfig().getBoolean("users." + e.getPlayer().getName() + ".jail.jailed")) {
			e.setCancelled(true);
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerInteract
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		Player player = e.getPlayer();
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		// jail
		
		if (core.getConfig().getBoolean("users." + player.getName() + ".jail.jailed")) {
			e.setCancelled(true);
			return;
		}
		
		// powertoolit
		
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			ListIterator<String> iterator = core.getCoreCommands().getPowerTools().listIterator();
			while (iterator.hasNext()) {
				String powerTool = iterator.next();
				try {
					String owner = powerTool.split("§")[0];
					String item = powerTool.split("§")[1];
					String command = powerTool.split("§")[2];
					ItemStack heldItem = player.getInventory().getItemInMainHand();
					if (owner.equalsIgnoreCase(player.getName()) && item.equalsIgnoreCase(heldItem.getType() + "")) {
						player.performCommand(command);
						e.setCancelled(true);
					}
				}
				catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}
		
		// portti
		
		if (CoreUtils.getDisplayName(e.getItem()).equals("§6Porttityökalu") && CoreUtils.hasRank(e.getPlayer(), "ylläpitäjä") && e.getHand() == EquipmentSlot.HAND) {
			e.setCancelled(true);
			if (e.getClickedBlock() != null) {
				if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
					core.getCoreCommands().setBlockOne(e.getClickedBlock());
					player.sendMessage(tc2 + "Asetettiin porttityökalun piste " + tc1 + "1" + tc2 + "!");
				}
				else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					core.getCoreCommands().setBlockTwo(e.getClickedBlock());
					player.sendMessage(tc2 + "Asetettiin porttityökalun piste " + tc1 + "2" + tc2 + "!");
				}
			}
		}
		
		// portin nappi
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
			Block block = e.getClickedBlock();
			if (block.getType().toString().contains("BUTTON")) {
				if (core.getConfig().getConfigurationSection("gates") != null) {
					for (String s : core.getConfig().getConfigurationSection("gates").getKeys(false)) {
						if (core.getConfig().getConfigurationSection("gates." + s + ".buttons") != null) {
							for (String s2 : core.getConfig().getConfigurationSection("gates." + s + ".buttons").getKeys(false)) {
								Location location = CoreUtils.loadLocation(core, "gates." + s + ".buttons." + s2);
								if (block.getLocation().equals(location)) {
									Location l1 = CoreUtils.loadLocation(core, "gates." + s + ".location-1");
									Location l2 = CoreUtils.loadLocation(core, "gates." + s + ".location-2");
									int status = core.getConfig().getInt("gates." + s + ".status");
									if (l1.getWorld() == l2.getWorld() && l1.getBlockX() <= l2.getBlockX() && l1.getBlockY() <= l2.getBlockY() && l1.getBlockZ() <= l2.getBlockZ()) {
										if (status == 0) {
											core.getConfig().set("gates." + s + ".status", 2);
											core.saveConfig();
											new BukkitRunnable() {
												int y = l1.getBlockY();
												public void run() {
													for (int x = l1.getBlockX(); x <= l2.getBlockX(); x++) {
														for (int z = l1.getBlockZ(); z <= l2.getBlockZ(); z++) {
															Block block = l1.getWorld().getBlockAt(x, y, z);
															if (s.startsWith("-")) {
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
														core.getConfig().set("gates." + s + ".status", 1);
														core.saveConfig();
													}
													else {
														y++;
													}
												}
											}.runTaskTimer(core, 15, 15);
										}
										else if (status == 1) {
											core.getConfig().set("gates." + s + ".status", 2);
											core.saveConfig();
											new BukkitRunnable() {
												int y = l2.getBlockY();
												public void run() {
													for (int x = l1.getBlockX(); x <= l2.getBlockX(); x++) {
														for (int z = l1.getBlockZ(); z <= l2.getBlockZ(); z++) {
															Block block = l1.getWorld().getBlockAt(x, y, z);
															if (s.startsWith("-")) {
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
														core.getConfig().set("gates." + s + ".status", 0);
														core.saveConfig();
													}
													else {
														y--;
													}
												}
											}.runTaskTimer(core, 15, 15);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// tuolit
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
			Block block = e.getClickedBlock();
			if (block.getType().toString().contains("STAIRS")) {
				boolean c = false;
				Stairs stairs = (Stairs) block.getBlockData();
				if (stairs.getHalf() == Half.BOTTOM) {
					if (stairs.getFacing() == BlockFace.NORTH || stairs.getFacing() == BlockFace.SOUTH) {
						String sideblock1 = block.getLocation().add(1, 0, 0).getBlock().getType().toString();
						String sideblock2 = block.getLocation().add(-1, 0, 0).getBlock().getType().toString();
						if ((sideblock1.contains("SIGN") || sideblock1.contains("TRAPDOOR") || sideblock1.contains("FENCE_GATE")) && 
								(sideblock2.contains("SIGN") || sideblock2.contains("TRAPDOOR") || sideblock2.contains("FENCE_GATE"))) {
							c = true;
						}
					}
					else if (stairs.getFacing() == BlockFace.EAST || stairs.getFacing() == BlockFace.WEST) {
						String sideblock1 = block.getLocation().add(0, 0, 1).getBlock().getType().toString();
						String sideblock2 = block.getLocation().add(0, 0, -1).getBlock().getType().toString();
						if ((sideblock1.contains("SIGN") || sideblock1.contains("TRAPDOOR") || sideblock1.contains("FENCE_GATE")) && 
								(sideblock2.contains("SIGN") || sideblock2.contains("TRAPDOOR") || sideblock2.contains("FENCE_GATE"))) {
							c = true;
						}
					}
					if (c && !player.isSneaking() && SettingsUtils.getSetting(player, "use_chairs")) {
						e.setCancelled(true);
						Arrow a = player.getWorld().spawnArrow(block.getLocation().add(0.5, -0.1, 0.5), new Vector(0, 0.00001, 0), 0, 0);
						a.setBounce(false);
						a.setGravity(false);
						a.setSilent(true);
						a.addPassenger(player);
					}
				}
			}
		}
		
		// komentokuutiot
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
			Block block = e.getClickedBlock();
			if (block.getType().toString().contains("SIGN") || block.getType().toString().contains("BUTTON")) {
				Location location = block.getLocation();
				String key = location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ();
				if (core.getConfig().contains("command-blocks." + key)) {
					String s = core.getConfig().getString("command-blocks." + key).replace("{name}", player.getName());
					String[] commands = s.split("&&");
					for (String command : commands) {
						if (command.startsWith("console:")) {
							command = command.substring(8);
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
						}
						else {
							player.performCommand(command);
						}
					}
				}
			}
		}
		
		if (e.getAction() == Action.PHYSICAL) {
			Block block = e.getClickedBlock();
			if (block.getType().toString().contains("PRESSURE_PLATE")) {
				Location location = block.getLocation();
				String key = location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ();
				if (core.getConfig().contains("command-blocks." + key)) {
					String s = core.getConfig().getString("command-blocks." + key).replace("{name}", player.getName());
					String[] commands = s.split("&&");
					for (String command : commands) {
						if (command.startsWith("console:")) {
							command = command.substring(8);
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
						}
						else {
							player.performCommand(command);
						}
					}
				}
			}
		}
		
		// tykit
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
			Block block = e.getClickedBlock();
			if (block.getType().toString().contains("BUTTON")) {
				Location location = block.getLocation();
				String cannon = location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ() + "/";
				List<String> cannons = core.getConfig().getStringList("cannons");
				ListIterator<String> iterator = cannons.listIterator();
				while (iterator.hasNext()) {
					String next = iterator.next();
					if (next.startsWith(cannon) && !cannonCooldown.contains(next)) {
						String[] data = next.split("/");
						try {
							int length = Integer.parseInt(data[4]);
							Directional button = (Directional) block.getBlockData();
							int modX = button.getFacing().getOppositeFace().getModX();
							int modZ = button.getFacing().getOppositeFace().getModZ();
							double yOffset = 0.5;
							Location barrelLocation = location.clone().add(0.5 + modX * (length + 1), yOffset, 0.5 + modZ * (length + 1));
							if (barrelLocation.getBlock().getType().isSolid()) {
								length++;
								yOffset += 0.5;
							}
							barrelLocation.getWorld().playSound(barrelLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5, 1);
							location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.getBlockX() + 0.5 + modX * (length + 1), location.getBlockY() + yOffset, 
									location.getBlockZ() + 0.5 + modZ * (length + 1), 1, 0, 0, 0);
							location.getWorld().spawnParticle(Particle.CLOUD, location.getBlockX() + 0.5 + modX * (length + 3), location.getBlockY() + yOffset, 
									location.getBlockZ() + 0.5 + modZ * (length + 3), 30, Math.abs(modX), 0, Math.abs(modZ), 0.05);
							cannonCooldown.add(next);
							new BukkitRunnable() {
								public void run() {
									cannonCooldown.remove(next);
									location.getWorld().playSound(location, Sound.UI_BUTTON_CLICK, 1, 1.2f);
								}
							}.runTaskLater(core, 120);
						}
						catch (NumberFormatException ex) { }
						break;
					}
				}
			}
		}
		
		// hevostallit
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Location clickedLocation = e.getClickedBlock().getLocation();
				if (core.getConfig().getConfigurationSection("stables") != null) {
					for (String id : core.getConfig().getConfigurationSection("stables").getKeys(false)) {
						if (core.getConfig().getConfigurationSection("stables." + id + ".signs") != null) {
							for (String identifier : core.getConfig().getConfigurationSection("stables." + id + ".signs").getKeys(false)) {
								Location location = CoreUtils.loadLocation(core, "stables." + id + ".signs." + identifier);
								if (location.equals(clickedLocation)) {
									if (core.getConfig().getBoolean("stables." + id + ".in-use")) {
										if (core.getConfig().getString("stables." + id + ".uuid").equals(player.getUniqueId().toString())) {
											core.getConfig().set("stables." + id + ".in-use", false);
											CoreUtils.spawnHorse(core, "stables." + id + ".horse", player.getLocation());
											player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1);
											player.sendMessage(tc2 + "Otettiin hevonen tallista!");
										}
										else {
											player.sendMessage(tc3 + "Tämä ei ole sinun tallisi!");
										}
									}
									else {
										AbstractHorse abstractHorse = null;
										for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
											if (entity instanceof AbstractHorse) {
												if (((AbstractHorse) entity).isLeashed() && ((AbstractHorse) entity).getLeashHolder().equals(player)) {
													abstractHorse = (AbstractHorse) entity;
												}
											}
										}
										if (abstractHorse != null) {
											core.getConfig().set("stables." + id + ".in-use", true);
											core.getConfig().set("stables." + id + ".name", player.getName());
											core.getConfig().set("stables." + id + ".uuid", player.getUniqueId().toString());
											CoreUtils.saveHorse(core, "stables." + id + ".horse", abstractHorse);
											abstractHorse.remove();
											player.getInventory().addItem(new ItemStack(Material.LEAD));
											player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1);
											player.sendMessage(tc2 + "Asetettiin hevonen talliin!");
										}
										else {
											player.sendMessage(tc3 + "Pidä haluamaasi hevosta talutushihnassa ja klikkaa tätä kylttiä asettaaksesi hevosen talliin!");
										}
									}
									core.saveConfig();
									core.getCoreCommands().updateStableSigns();
								}
							}
						}
					}
				}
			}
			
		}
		
		// TARDIS
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Location clickedLocation = e.getClickedBlock().getLocation().add(0.5, 0, 0.5);
			String[] names = {"T4TU_", "Ahishi", "evokki0075"};
			for (int i = 0; i < 3; i++) {
				String name = names[i];
				for (Location location : core.getCoreCommands().getTardisBlocks(i)) {
					if (location.equals(clickedLocation)) {
						e.setCancelled(true);
						if ((CoreUtils.hasRank(player, "ylläpitäjä") || CoreUtils.getDisplayName(e.getItem()).equals("§9§lTARDIS§bin avain")) && core.getCoreCommands().canTardisMove(i)) {
							Location interiorLocation = CoreUtils.loadLocation(core, "tardis." + name + ".interior-location");
							if (interiorLocation != null) {
								player.teleport(interiorLocation);
								player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.5f, 1);
							}
							else {
								player.playSound(e.getClickedBlock().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 2);
							}
						}
						else {
							player.playSound(e.getClickedBlock().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 2);
						}
						return;
					}
				}
			}
		}
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.IRON_DOOR) {
				String[] names = {"T4TU_", "Ahishi", "evokki0075"};
				for (int i = 0; i < 3; i++) {
					String name = names[i];
					Location location = CoreUtils.loadLocation(core, "tardis." + name + ".interior-location");
					if (location != null && location.distance(e.getClickedBlock().getLocation()) < 4) {
						e.setCancelled(true);
						Location currentLocation = CoreUtils.loadLocation(core, "tardis." + name + ".current-location");
						if (currentLocation != null && core.getCoreCommands().canTardisMove(i)) {
							currentLocation.add(1, 0, 0);
							currentLocation.setPitch(0);
							currentLocation.setYaw(270);
							player.teleport(currentLocation);
							player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.5f, 1);
						}
						else {
							player.playSound(e.getClickedBlock().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 2);
						}
						return;
					}
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////
	//
	//          onPlayerInteractEntity
	//
	///////////////////////////////////////////////////////////////
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		
		Player player = e.getPlayer();
		Entity clickedEntity = e.getRightClicked();
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		
		if (CoreUtils.isNPC(clickedEntity)) {
			e.setCancelled(true);
		}
		
		if (clickedEntity instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) clickedEntity;
			ItemStack item = frame.getItem();
			if (item != null && item.getType().toString().contains("MUSIC_DISC_")) {
				if (e.getHand() == EquipmentSlot.HAND) {
					Location musicShopLocation = CoreUtils.loadLocation(core, "music-shop");
					if (musicShopLocation != null && musicShopLocation.getWorld().getName().equals(player.getLocation().getWorld().getName())) {
						if (musicShopLocation.distance(player.getLocation()) <= 20) {
							e.setCancelled(true);
							for (Sound sound : Sound.values()) {
								if (sound.toString().contains("MUSIC_DISC_")) {
									player.stopSound(sound);
								}
							}
							Sound sound = Sound.valueOf(item.getType().toString());
							player.playSound(frame.getLocation(), sound, 10, 1);
							player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(tc2 + "Esikuunnellaan: " + tc1 + CoreUtils.firstUpperCase(sound.toString().substring(11).toLowerCase())));
							new BukkitRunnable() {
								public void run() {
									player.stopSound(sound);
								}
							}.runTaskLater(core, 600);
						}
					}
				}
			}
		}
		
		if (clickedEntity instanceof Player) {
			if (e.getHand() == EquipmentSlot.HAND) {
				Player target = (Player) clickedEntity;
				if (player.isSneaking()) {
					Party party = core.getPartyManager().getPartyOfPlayer(player);
					if (party != null) {
						if (party.getMembers().contains(target.getUniqueId().toString())) {
							player.openInventory(target.getInventory());
							target.sendMessage("§7§o" + player.getName() + " avasi reppusi.");
						}
					}
				}
			}
		}
		
		if (!core.getCoreCommands().getTravelingPlayers().contains(player.getName()) && CoreUtils.isNPC(clickedEntity)) {
			if (e.getHand() == EquipmentSlot.HAND) {
				if (core.getConfig().getConfigurationSection("trips") != null && 
						!core.getConfig().getConfigurationSection("trips").getKeys(false).isEmpty()) {
					for (String s : core.getConfig().getConfigurationSection("trips").getKeys(false)) {
						String npcName = ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("trips." + s + ".npc-name"));
						if (CoreUtils.isNPCAndNamed(clickedEntity, npcName)) {
							String message = ChatColor.translateAlternateColorCodes('&', core.getConfig().getString("trips." + s + ".initial-message"));
							player.sendMessage("");
							player.sendMessage(message);
							player.sendMessage("");
							player.spigot().sendMessage(CoreUtils.getAcceptDeny("§7 > ", "§7 / ", "§7 < ", "§aKyllä", "§cEi", 
									"§7Klikkaa vastataksesi \"§aKyllä§7\"", "§7Klikkaa vastataksesi \"§cEi§7\"", 
									"/matka confirm " + s, "/matka deny " + s));
							player.sendMessage("");
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
							break;
						}
					}
				}
			}
		}
	}
	
	private List<String> deathNotesKiller = Arrays.asList(
			"§7<killer>§7 räjäytti taivaan tuuliin pelaajan <victim>§7.<>BLOCK_EXPLOSION", 
			"§7<killer>§7 tappoi pelaajan <victim>§7.<>CUSTOM", 
			"§7<killer>§7 hukutti pelaajan <victim>§7.<>DROWNING", 
			"§7<killer>§7 tappoi pelaajan <victim>§7<using>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 murhasi pelaajan <victim>§7<using>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 listi pelaajan <victim>§7<using>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 nujersi kaksintaistelussa pelaajan <victim>§7<using>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 päästi hengestään pelaajan <victim>§7<using>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 räjäytti taivaan tuuliin pelaajan <victim>§7.<>ENTITY_EXPLOSION", 
			"§7<killer>§7 viilsi miekallaan pelaajan <victim>§7.<>ENTITY_SWEEP_ATTACK", 
			"§7<killer>§7 tönäisi kuolemaan pelaajan <victim>§7.<>FALL", 
			"§7<killer>§7 kärvensi elävältä pelaajan <victim>§7.<>FIRE", 
			"§7<killer>§7 kärvensi elävältä pelaajan <victim>§7.<>FIRE_TICK", 
			"§7<killer>§7 tönäisi laavaan pelaajan <victim>§7.<>LAVA", 
			"§7<killer>§7 tappoi taikuudella pelaajan <victim>§7.<>MAGIC", 
			"§7<killer>§7 myrkytti pelaajan <victim>§7.<>POISON", 
			"§7<killer>§7 ampui pelaajan <victim>§7.<>PROJECTILE", 
			"§7<killer>§7 antoi pelaajan <victim>§7 maistaa omaa lääkettään.<>THORNS", 
			"§7<killer>§7 tönäisi pelaajan <victim>§7 tyhjyyteen.<>VOID", 
			"§7<killer>§7 näivetti elävältä pelaajan <victim>§7.<>WITHER"
			);
	
	private List<String> deathNotesNoKiller = Arrays.asList(
			"§7<victim>§7 räjähti taivaan tuuliin.<>BLOCK_EXPLOSION", 
			"§7<victim>§7 ei kestänyt lohikäärmeen tulista henkäystä.<>DRAGON_BREATH", 
			"§7<victim>§7 onnistui hukuttamaan itsensä.<>DROWNING", 
			"§7<victim>§7 hukkui.<>DROWNING", 
			"§7<killer>§7 tappoi pelaajan <victim>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 murhasi pelaajan <victim>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 listi pelaajan <victim>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 päästi hengestään pelaajan <victim>§7.<>ENTITY_ATTACK", 
			"§7<killer>§7 räjäytti taivaan tuuliin pelaajan <victim>§7.<>ENTITY_EXPLOSION", 
			"§7<victim>§7 putosi kuolemaansa.<>FALL", 
			"§7<victim>§7 lätsähti maahan kuin pannukakku.<>FALL", 
			"§7<victim>§7 hyppäsi kuolemaansa.<>FALL", 
			"§7<victim>§7 litistyi palikoiden väliin.<>FALLING_BLOCK", 
			"§7<victim>§7 litistyi putoavan flyygelin alle.<>FALLING_BLOCK", 
			"§7<victim>§7 käveli suoraan tuleen.<>FIRE", 
			"§7<victim>§7 ei kestänyt liekkien kuumuutta.<>FIRE", 
			"§7<victim>§7 kärventyi elävältä.<>FIRE", 
			"§7<victim>§7 ei kestänyt liekkien kuumuutta.<>FIRE_TICK", 
			"§7<victim>§7 kärventyi elävältä.<>FIRE_TICK", 
			"§7<victim>§7 päätti ottaa pienen laavakylvyn.<>LAVA", 
			"§7<victim>§7 päätti kylpeä laavassa.<>LAVA", 
			"§7<victim>§7 sai ihmeellisen päähänpiston kylpeä laavassa.<>LAVA", 
			"§7<victim>§7 lätsähti päin seinää.<>FLY_INTO_WALL", 
			"§7<victim>§7 poltti jalkansa tanssiessaan magmakuution päällä.<>HOT_FLOOR", 
			"§7<victim>§7 muuttui eläväksi ukkosenjohdattimeksi.<>LIGHTNING", 
			"§7Pelaajaan <victim>§7 iski salama kuin kirkkaalta taivaalta.<>LIGHTNING", 
			"§7<victim>§7 myrkytettiin kuoliaaksi.<>POISON", 
			"§7<killer>§7 ampui pelaajan <victim>§7.<>PROJECTILE", 
			"§7<victim>§7 nääntyi nälkään.<>STARVATION", 
			"§7<victim>§7 tukehtui seinän sisään.<>SUFFOCATION", 
			"§7<victim>§7 riisti oman henkensä.<>SUICIDE", 
			"§7<victim>§7 sai maistaa omaa lääkettään.<>THORNS", 
			"§7<victim>§7 hyppäsi tyhjyyteen.<>VOID", 
			"§7<victim>§7 näiveentyi hengiltä.<>WITHER"
			);
	
	private List<Material> stunningItems1 = Arrays.asList(
			Material.WOODEN_SWORD, 
			Material.STONE_SWORD, 
			Material.WOODEN_AXE, 
			Material.STONE_AXE, 
			Material.WOODEN_PICKAXE, 
			Material.STONE_PICKAXE, 
			Material.WOODEN_SHOVEL, 
			Material.STONE_SHOVEL, 
			Material.WOODEN_HOE, 
			Material.STONE_HOE, 
			Material.STICK
			);
	
	private List<Material> stunningItems2 = Arrays.asList(
			Material.GOLDEN_SWORD, 
			Material.IRON_SWORD, 
			Material.GOLDEN_AXE, 
			Material.IRON_AXE, 
			Material.GOLDEN_PICKAXE, 
			Material.IRON_PICKAXE, 
			Material.GOLDEN_SHOVEL, 
			Material.IRON_SHOVEL, 
			Material.GOLDEN_HOE, 
			Material.IRON_HOE
			);
	
	private List<Material> stunningItems3 = Arrays.asList(
			Material.DIAMOND_SWORD, 
			Material.DIAMOND_AXE, 
			Material.DIAMOND_PICKAXE, 
			Material.DIAMOND_SHOVEL, 
			Material.DIAMOND_HOE, 
			Material.BLAZE_ROD
			);
}