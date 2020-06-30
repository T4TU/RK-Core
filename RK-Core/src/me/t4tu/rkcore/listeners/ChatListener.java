package me.t4tu.rkcore.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatListener implements Listener {
	
	private Core core;
	private boolean disabled;
	private Map<String, Integer> cooldownCounter;
	
	public ChatListener(Core core) {
		this.core = core;
		disabled = false;
		cooldownCounter = new HashMap<String, Integer>();
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		
		e.setCancelled(true);
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		String name = player.getName();
		
		// afk
		
		CoreUtils.setAfkCounter(player, 0);
		
		// jos vankilassa
		
		if (core.getConfig().getBoolean("users." + name + ".jail.jailed")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Et voi lähettää viestejä ollessassi vankilasssa!");
			return;
		}
		
		// jos hiljennetty
		
		if (core.getConfig().getBoolean("users." + name + ".mute.muted")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Sinut on hiljennetty!");
			return;
		}
		
		// postin lähetys
		
		if (core.getCoreCommands().getMailWritingPlayers().containsKey(player.getName())) {
			String sender = player.getUniqueId().toString();
			String receiver = core.getCoreCommands().getMailWritingPlayers().get(player.getName()).split("§")[0];
			String subject = core.getCoreCommands().getMailWritingPlayers().get(player.getName()).substring(receiver.length() + 1);
			String message = e.getMessage();
			core.getCoreCommands().getMailWritingPlayers().remove(player.getName());
			new BukkitRunnable() {
				public void run() {
					MySQLUtils.set("INSERT INTO player_mails (sender, receiver, subject, message) VALUES (?, ?, ?, ?)", sender, receiver, 
							subject, message);
					String to = CoreUtils.uuidToName(receiver);
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
					player.sendMessage(tc2 + "Lähetettiin viesti pelaajalle " + tc1 + to + tc2 + "!");
					Player p = Bukkit.getPlayer(to);
					if (p != null) {
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
						p.sendMessage("");
						p.sendMessage(tc2 + " Sinulla on lukemattomia viestejä postilaatikossasi! Voit lukea ne komennolla " 
								+ tc1 + "/posti" + tc2 + ".");
						p.sendMessage("");
					}
					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (CoreUtils.hasRank(pl, "valvoja") && core.getCoreCommands().getSpyPlayers().contains(pl.getName())) {
							pl.sendMessage("§8§l" + player.getName() + "§7§l ▶ §8§l" + p.getName() + "§8 (posti) §7" + message);
						}
					}
				}
			}.runTaskAsynchronously(core);
			return;
		}
		
		// jos chat-viestit piilotettu
		
		if (!SettingsUtils.getSetting(player, "show_chat")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Olet poistanut chat-viestit käytöstä asetuksistasi!");
			return;
		}
		
		// jos chat pois käytöstä
		
		if (disabled && !CoreUtils.hasRank(player, "valvoja")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Chat on väliaikaisesti hiljennetty!");
			return;
		}
		
		// chatin nopeusrajoitus (maks. 3 viestiä 4 sekunnin sisällä)
		
		if (!CoreUtils.hasRank(player, "ritari")) {
			int i = 0;
			if (cooldownCounter.containsKey(name)) {
				i = cooldownCounter.get(name);
			}
			if (i >= 3) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Et voi lähettää viestejä noin nopeasti!");
				return;
			}
			else {
				cooldownCounter.put(name, i + 1);
				new BukkitRunnable() {
					public void run() {
						if (cooldownCounter.containsKey(name) && cooldownCounter.get(name) > 0) {
							cooldownCounter.put(name, cooldownCounter.get(name) - 1);
						}
					}
				}.runTaskLater(core, 80);
			}
		}
		
		// etuliitteet yms.
		
		String chatMessage = e.getMessage();
		String chatPrefix = CoreUtils.translateHexColors('&', core.getConfig().getString("users." + name + ".chat_prefix"));
		String chatColor = CoreUtils.translateHexColors('&', core.getConfig().getString("users." + name + ".chat_color"));
		String chatName = chatColor + name;
		String chatStatus = core.getConfig().getString("users." + name + ".status");
		if (CoreUtils.hasRank(player, "valvoja")) {
			chatMessage = CoreUtils.translateHexColors('&', chatMessage);
		}
		else if (CoreUtils.hasRank(player, "ritari")) {
			chatMessage = CoreUtils.translateFormatCodes('&', chatMessage);
		}
		if (core.getConfig().contains("users." + name + ".chat_nick")) {
			chatName = chatColor + "*" + core.getConfig().getString("users." + name + ".chat_nick");
		}
		
		// makrot
		
		for (String macro : macros.keySet()) {
			chatMessage = chatMessage.replace(macro, macros.get(macro));
		}
		
		// rakennetaan viesti
		
		List<String> notes = core.getConfig().getStringList("users." + name + ".notes");
		String notesInfo = "§c§lHuomautukset:";
		for (String note : notes) {
			notesInfo = notesInfo + "\n§c " + CoreUtils.translateHexColors('&', note);
		}
		
		HoverEvent noteHoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
				TextComponent.fromLegacyText(notesInfo));
		ClickEvent noteClickEvent = new ClickEvent(Action.RUN_COMMAND, "/huomautus " + name);
		boolean hasDescription = CoreUtils.getPrefixDescription(chatPrefix) != null;
		HoverEvent prefixHoverEvent = null;
		if (hasDescription) {
			prefixHoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
					TextComponent.fromLegacyText(CoreUtils.getPrefixDescription(chatPrefix)));
		}
		HoverEvent nameHoverEvent = new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
				TextComponent.fromLegacyText(chatColor + "§o" + chatStatus));
		ClickEvent nameClickEvent = new ClickEvent(Action.RUN_COMMAND, "/pelaaja " + name);
		
		TextComponent baseComponent = new TextComponent();
		TextComponent baseComponentWithNotes = new TextComponent();
		BaseComponent[] noteComponents = TextComponent.fromLegacyText("§c§l[!]§r ");
		BaseComponent[] prefixComponents = TextComponent.fromLegacyText(chatPrefix);
		BaseComponent[] nameComponents = TextComponent.fromLegacyText(chatName);
		BaseComponent[] colonComponents = TextComponent.fromLegacyText(chatColor + ": ");
		BaseComponent[] messageComponents = TextComponent.fromLegacyText(chatMessage);
		
		for (BaseComponent component : noteComponents) {
			component.setHoverEvent(noteHoverEvent);
			component.setClickEvent(noteClickEvent);
			baseComponentWithNotes.addExtra(component);
		}
		for (BaseComponent component : prefixComponents) {
			if (hasDescription) {
				component.setHoverEvent(prefixHoverEvent);
			}
			baseComponent.addExtra(component);
			baseComponentWithNotes.addExtra(component);
		}
		for (BaseComponent component : nameComponents) {
			component.setHoverEvent(nameHoverEvent);
			component.setClickEvent(nameClickEvent);
			baseComponent.addExtra(component);
			baseComponentWithNotes.addExtra(component);
		}
		for (BaseComponent component : colonComponents) {
			baseComponent.addExtra(component);
			baseComponentWithNotes.addExtra(component);
		}
		for (BaseComponent component : messageComponents) {
			baseComponent.addExtra(component);
			baseComponentWithNotes.addExtra(component);
		}
		
		// jos mainittu pelaaja AFK
		
		boolean hasNotes = !core.getConfig().getStringList("users." + name + ".notes").isEmpty();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (chatMessage.toLowerCase().contains(p.getName().toLowerCase())) {
				if (SettingsUtils.getSetting(p, "play_sound_mentioned")) {
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				}
				if (CoreUtils.getAfkCounter().containsKey(p.getName()) && CoreUtils.getAfkCounter().get(p.getName()) == -1 && SettingsUtils.getSetting(player, "show_afk_chat_notification")) {
					player.sendMessage(tc3 + "Huomautus: " + p.getName() + " on AFK-tilassa!");
				}
			}
		}
		
		// lähetetään
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!core.getConfig().getBoolean("users." + p.getName() + ".jail.jailattu")) {
				if (SettingsUtils.getSetting(p, "show_chat")) {
					if (hasNotes && CoreUtils.hasRank(p, "valvoja")) {
						p.spigot().sendMessage(baseComponentWithNotes);
					}
					else {
						p.spigot().sendMessage(baseComponent);
					}
				}
				if (chatMessage.toLowerCase().contains(p.getName().toLowerCase())) {
					if (SettingsUtils.getSetting(p, "play_sound_mentioned")) {
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					}
				}
			}
		}
		
		e.setMessage(ChatColor.stripColor(chatMessage));
		
		Bukkit.getConsoleSender().sendMessage("CHAT > " + player.getName() + ": " + e.getMessage());
	}
	
	// TODO lisää makroja
	
	private Map<String, String> macros = new HashMap<String, String>(); {
		macros.put("*shrug*", "¯\\_(ツ)_/¯");
		macros.put("*kk*", "кк");
	}
}