package me.t4tu.rkcore.utils;

import java.util.ArrayList;
import java.util.List;

public class GuildRequest {
	
	private static List<GuildRequest> guildRequests = new ArrayList<GuildRequest>();
	
	private String from;
	private String to;
	
	public GuildRequest(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public static List<GuildRequest> getGuildRequests() {
		return guildRequests;
	}
	
	public static List<GuildRequest> getGuildRequestsFrom(String from) {
		List<GuildRequest> requests = new ArrayList<GuildRequest>();
		for (GuildRequest request : guildRequests) {
			if (request.getFrom().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static List<GuildRequest> getGuildRequestsTo(String from) {
		List<GuildRequest> requests = new ArrayList<GuildRequest>();
		for (GuildRequest request : guildRequests) {
			if (request.getTo().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static boolean hasSentRequestTo(String from, String to) {
		for (GuildRequest request : getGuildRequestsFrom(from)) {
			if (request.getTo().equalsIgnoreCase(to)) {
				return true;
			}
		}
		return false;
	}
}