package me.t4tu.rkcore.statistics;

import me.t4tu.rkcore.utils.MySQLUtils;

public class PlayerStatisticsEntry extends StatisticsEntry {
	
	private static final String TABLE_NAME = "statistics_player";
	
	protected String uuid;
	
	public PlayerStatisticsEntry(Statistic statistic, int value, String uuid) {
		super(statistic, value);
		this.uuid = uuid;
	}
	
	@Override
	public void saveToDatabase() {
		MySQLUtils.set("INSERT INTO " + TABLE_NAME + " (statistic, value, player, time) VALUES (?, ?, ?, ?)", statistic.getId() + "", value + "", 
				uuid, System.currentTimeMillis() + "");
	}
	
	@Override
	public boolean compare(StatisticsEntry entry) {
		if (entry instanceof PlayerStatisticsEntry) {
			PlayerStatisticsEntry entry2 = (PlayerStatisticsEntry) entry;
			if (entry2.getStatistic() == statistic && entry2.getUuid().equals(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}