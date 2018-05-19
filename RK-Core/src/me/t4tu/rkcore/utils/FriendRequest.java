package me.t4tu.rkcore.utils;

import java.util.ArrayList;
import java.util.List;

public class FriendRequest {
	
	private static List<FriendRequest> friendRequests = new ArrayList<FriendRequest>();
	
	private String from;
	private String to;
	
	public FriendRequest(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public static List<FriendRequest> getFriendRequests() {
		return friendRequests;
	}
	
	public static List<FriendRequest> getFriendRequestsFrom(String from) {
		List<FriendRequest> requests = new ArrayList<FriendRequest>();
		for (FriendRequest request : friendRequests) {
			if (request.getFrom().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static List<FriendRequest> getFriendRequestsTo(String from) {
		List<FriendRequest> requests = new ArrayList<FriendRequest>();
		for (FriendRequest request : friendRequests) {
			if (request.getTo().equalsIgnoreCase(from)) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	public static boolean hasSentRequestTo(String from, String to) {
		for (FriendRequest request : getFriendRequestsFrom(from)) {
			if (request.getTo().equalsIgnoreCase(to)) {
				return true;
			}
		}
		return false;
	}
}