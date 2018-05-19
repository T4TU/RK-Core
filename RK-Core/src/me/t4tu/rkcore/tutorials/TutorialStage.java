package me.t4tu.rkcore.tutorials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import me.t4tu.rkcore.Core;

public class TutorialStage {
	
	private String title;
	private String subtitle;
	private Location location;
	
	public TutorialStage(String title, String subtitle, Location location) {
		this.title = title;
		this.subtitle = subtitle;
		this.location = location;
	}
	
	public String getTitle() {
		if (title == null) {
			return "";
		}
		return title;
	}
	
	public String getSubtitle() {
		if (subtitle == null) {
			return "";
		}
		return subtitle;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public static List<TutorialStage> loadTutorialStages(Core core) {
		
		List<TutorialStage> tutorialStages = new ArrayList<TutorialStage>();
		
		if (core.getConfig().getConfigurationSection("tutorial") != null) {
			for (String stage : core.getConfig().getConfigurationSection("tutorial").getKeys(false)) {
				String title = core.getConfig().getString("tutorial." + stage + ".title");
				String subtitle = core.getConfig().getString("tutorial." + stage + ".subtitle");
				Location location = (Location) core.getConfig().get("tutorial." + stage + ".location");
				if (location != null) {
					tutorialStages.add(new TutorialStage(title, subtitle, location));
				}
			}
		}
		
		return tutorialStages;
	}
}