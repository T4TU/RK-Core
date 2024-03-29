package me.t4tu.rkcore.tutorials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;

public class Tutorial implements Listener {
	
	private Core core;
	private List<String> playersInTutorial;
	private List<TutorialStage> tutorialStages;
	private Location startLocation;
	
	public Tutorial(Core core) {
		this.core = core;
		playersInTutorial = new ArrayList<String>();
		tutorialStages = TutorialStage.loadTutorialStages(core);
		startLocation = loadStartLocation();
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	
	public List<String> getPlayersInTutorial() {
		return playersInTutorial;
	}
	
	public void reload() {
		tutorialStages = TutorialStage.loadTutorialStages(core);
		startLocation = loadStartLocation();
	}
	
	public boolean hasFinishedTutorial(Player player) {
		MySQLResult tutorialData = MySQLUtils.get("SELECT * FROM player_tutorial WHERE uuid=?", player.getUniqueId().toString());
		if (tutorialData != null) {
			return true;
		}
		return false;
	}
	
	public void setFinishedTutorial(Player player, boolean finished) {
		if (finished) {
			if (!hasFinishedTutorial(player)) {
				MySQLUtils.set("INSERT INTO player_tutorial (uuid) VALUES (?)", player.getUniqueId().toString());
			}
		}
		else {
			MySQLUtils.set("DELETE FROM player_tutorial WHERE uuid=?", player.getUniqueId().toString());
		}
	}
	
	public void startTutorial(Player player) {
		if (!playersInTutorial.contains(player.getName()) && startLocation != null && !tutorialStages.isEmpty()) {
			playersInTutorial.add(player.getName());
			new BukkitRunnable() {
				int i = 0;
				int c = 0;
				ArmorStand guide = null;
				public void run() {
					if (!player.isOnline()) {
						guide.remove();
						playersInTutorial.remove(player.getName());
						cancel();
						return;
					}
					if (i == 0) {
						guide = spawnGuideAt(startLocation);
						player.teleport(guide);
						player.setGameMode(GameMode.SPECTATOR);
						player.setSpectatorTarget(guide);
					}
					if (i > 0 && i < 200) {
						Location location = guide.getLocation().add(0.1, 0, 0);
						guide.teleport(location);
						player.setGameMode(GameMode.SPECTATOR);
						player.setSpectatorTarget(guide);
					}
					if (i == 10) {
						player.sendTitle("", "§aSaammeko esitellä...", 20, 40, 20);
					}
					if (i == 100) {
						player.sendTitle("§a§lRoyal Kingdom", "", 20, 40, 20);
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 0.1f);
					}
					if (i == 200 + 100 * tutorialStages.size()) {
						guide.remove();
						player.setGameMode(GameMode.SURVIVAL);
					}
					if (i >= 260 + 100 * tutorialStages.size()) {
						playersInTutorial.remove(player.getName());
						new BukkitRunnable() {
							public void run() {
								setFinishedTutorial(player, true);
							}
						}.runTaskAsynchronously(core);
						cancel();
						if (!core.getConfig().getBoolean("motd.seen." + player.getName()) && core.getConfig().contains("motd.motd")) {
							new BukkitRunnable() {
								public void run() {
									player.performCommand("motd");
								}
							}.runTask(core);
						}
						return;
					}
					if (i >= 200 && i % 100 == 0 && c < tutorialStages.size()) {
						TutorialStage stage = tutorialStages.get(c);
						if (c == 0 || !tutorialStages.get(c - 1).getLocation().equals(stage.getLocation())) {
							guide.remove();
							player.teleport(stage.getLocation());
							guide = spawnGuideAt(stage.getLocation());
							player.setGameMode(GameMode.SPECTATOR);
							player.setSpectatorTarget(guide);
						}
						player.sendTitle(stage.getTitle(), stage.getSubtitle(), 10, 70, 10);
						c++;
					}
					i++;
				}
			}.runTaskTimer(core, 0, 1);
		}
	}
	
	private Location loadStartLocation() {
		return CoreUtils.loadLocation(core, "tutorial-start-point");
	}
	
	private ArmorStand spawnGuideAt(Location location) {
		ArmorStand guide = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		guide.setGravity(false);
		guide.setMarker(true);
		guide.setSmall(true);
		guide.setVisible(false);
		guide.setBasePlate(false);
		guide.setRemoveWhenFarAway(false);
		return guide;
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (playersInTutorial.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (playersInTutorial.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää komentoja esittelyn aikana!");
		}
	}
}