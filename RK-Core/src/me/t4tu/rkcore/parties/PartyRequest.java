package me.t4tu.rkcore.parties;

public class PartyRequest {
	
	private Party from;
	private String to;
	
	public PartyRequest(Party from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public Party getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
}