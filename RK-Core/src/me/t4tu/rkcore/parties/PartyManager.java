package me.t4tu.rkcore.parties;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class PartyManager {
	
	private List<Party> parties;
	private List<PartyRequest> partyRequests;
	
	public PartyManager() {
		parties = new ArrayList<Party>();
		partyRequests = new ArrayList<PartyRequest>();
	}
	
	public List<Party> getParties() {
		return parties;
	}
	
	public Party getPartyOfPlayer(Player player) {
		for (Party party : parties) {
			for (String uuid : party.getMembers()) {
				if (uuid.equals(player.getUniqueId().toString())) {
					return party;
				}
			}
		}
		return null;
	}
	
	public List<PartyRequest> getPartyRequests() {
		return partyRequests;
	}
	
	public List<PartyRequest> getPartyRequestsFrom(Party from) {
		List<PartyRequest> requests = new ArrayList<PartyRequest>();
		for (PartyRequest request : partyRequests) {
			if (request.getFrom().equals(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public List<PartyRequest> getPartyRequestsTo(String to) {
		List<PartyRequest> requests = new ArrayList<PartyRequest>();
		for (PartyRequest request : partyRequests) {
			if (request.getTo().equalsIgnoreCase(to)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public boolean hasSentRequestTo(Party from, String to) {
		for (PartyRequest request : getPartyRequestsFrom(from)) {
			if (request.getTo().equalsIgnoreCase(to)) {
				return true;
			}
		}
		return false;
	}
}