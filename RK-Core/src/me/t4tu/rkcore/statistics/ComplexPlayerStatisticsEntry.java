package me.t4tu.rkcore.statistics;

import me.t4tu.rkcore.utils.MySQLUtils;

public class ComplexPlayerStatisticsEntry extends PlayerStatisticsEntry {
	
	private static final String TABLE_NAME = "statistics_player_complex";
	
	protected int data;
	
	public ComplexPlayerStatisticsEntry(Statistic statistic, int value, String uuid, int data) {
		super(statistic, value, uuid);
		this.data = data;
	}
	
	@Override
	public void saveToDatabase() {
		MySQLUtils.set("INSERT INTO " + TABLE_NAME + " (statistic, value, player, data, time) VALUES (?, ?, ?, ?, ?)", statistic.getId() + "", value + "", 
				uuid, data + "", System.currentTimeMillis() + "");
	}
	
	@Override
	public boolean compare(StatisticsEntry entry) {
		if (entry instanceof ComplexPlayerStatisticsEntry) {
			ComplexPlayerStatisticsEntry entry2 = (ComplexPlayerStatisticsEntry) entry;
			if (entry2.getStatistic() == statistic && entry2.getUuid().equals(uuid) && entry2.getData() == data) {
				return true;
			}
		}
		return false;
	}
	
	public int getData() {
		return data;
	}
	
	public void setData(int data) {
		this.data = data;
	}
}