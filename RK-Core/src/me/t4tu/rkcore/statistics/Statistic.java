package me.t4tu.rkcore.statistics;

public enum Statistic {
	
	PLAYERS_ONLINE(1), STAFF_ONLINE(2), PLAYERS_JOINED(3), MONEY_EARNED_FROM_PROFESSION(4), MONEY_USED_IN_SHOP(5), MONEY_USED_IN_MARKET_STALL(6), MONEY_EARNED_FROM_MARKET_STALL(7), 
	PVP_KILLS(8), PVP_DEATHS(9);
	
	private int id;
	
	private Statistic(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static Statistic getById(int id) {
		for (Statistic statistic : values()) {
			if (statistic.getId() == id) {
				return statistic;
			}
		}
		return null;
	}
}