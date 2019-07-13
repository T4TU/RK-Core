package me.t4tu.rkcore.utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;

public class MapAnimation {
	
	private int width;
	private int height;
	private Location upperLeftCorner;
	private List<short[]> frames;
	private Core core;
	
	public MapAnimation(int width, int height, Location upperLeftCorner, List<short[]> frames, Core core) {
		this.width = width;
		this.height = height;
		this.upperLeftCorner = upperLeftCorner;
		this.frames = frames;
		this.core = core;
	}
	
	public void prepareFirstFrame(Player player) {
		if (frames.isEmpty()) {
			return;
		}
		int i = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Location currentLocation = upperLeftCorner.add(x, -y, 0);
				ItemFrame itemFrame = getItemFrameAt(currentLocation);
				if (itemFrame != null) {
					int mapId = frames.get(0)[i];
					if (mapId != -1) {
						CoreUtils.sendItemFrameMapPacket(player, itemFrame, mapId);
					}
				}
				upperLeftCorner.subtract(x, -y, 0);
				i++;
			}
		}
	}
	
	public void play(Player player, int period) {
		new BukkitRunnable() {
			int frame = 0;
			public void run() {
				if (frame >= frames.size() || player == null || !player.isOnline() || player.isDead()) {
					cancel();
					return;
				}
				int i = 0;
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						Location currentLocation = upperLeftCorner.add(x, -y, 0);
						ItemFrame itemFrame = getItemFrameAt(currentLocation);
						if (itemFrame != null) {
							int mapId = frames.get(frame)[i];
							if (mapId != -1) {
								CoreUtils.sendItemFrameMapPacket(player, itemFrame, mapId);
							}
						}
						upperLeftCorner.subtract(x, -y, 0);
						i++;
					}
				}
				frame++;
			}
		}.runTaskTimer(core, 0, period);
	}
	
	private ItemFrame getItemFrameAt(Location location) {
		for (ItemFrame frame : location.getWorld().getEntitiesByClass(ItemFrame.class)) {
			if (frame.getLocation().getBlockX() == location.getBlockX() && frame.getLocation().getBlockY() == location.getBlockY() && frame.getLocation().getBlockZ() == location.getBlockZ()) {
				return frame;
			}
		}
		return null;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public List<short[]> getFrames() {
		return frames;
	}
}