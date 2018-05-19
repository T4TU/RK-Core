package me.t4tu.rkcore.utils;

import java.util.ArrayList;
import java.util.List;

public class TeleportRequest {
	
	private static List<TeleportRequest> teleportRequests = new ArrayList<TeleportRequest>();
	
	private String from;
	private String to;
	
	public TeleportRequest(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public static List<TeleportRequest> getTeleportRequests() {
		return teleportRequests;
	}
	
	public static List<TeleportRequest> getTeleportRequestsFrom(String from) {
		List<TeleportRequest> requests = new ArrayList<TeleportRequest>();
		for (TeleportRequest request : teleportRequests) {
			if (request.getFrom().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static List<TeleportRequest> getTeleportRequestsTo(String from) {
		List<TeleportRequest> requests = new ArrayList<TeleportRequest>();
		for (TeleportRequest request : teleportRequests) {
			if (request.getTo().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static boolean hasSentRequestTo(String from, String to) {
		for (TeleportRequest request : getTeleportRequestsFrom(from)) {
			if (request.getTo().equalsIgnoreCase(to)) {
				return true;
			}
		}
		return false;
	}
}