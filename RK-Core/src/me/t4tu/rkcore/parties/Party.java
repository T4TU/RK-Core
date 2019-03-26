package me.t4tu.rkcore.parties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Party {
	
	private List<String> members;
	
	public Party(String creator) {
		members = new ArrayList<String>();
		members.add(creator);
	}
	
	public List<String> getMembers() {
		return members;
	}
	
	public List<Player> getMembersAsPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (String uuid : members) {
			Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if (player != null) {
				players.add(player);
			}
		}
		return players;
	}
}