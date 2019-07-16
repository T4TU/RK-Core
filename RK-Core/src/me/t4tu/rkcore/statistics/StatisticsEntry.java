package me.t4tu.rkcore.statistics;

import me.t4tu.rkcore.utils.MySQLUtils;

public class StatisticsEntry {
	
	private static final String TABLE_NAME = "statistics_simple";
	
	protected Statistic statistic;
	protected int value;
	
	public StatisticsEntry(Statistic statistic, int value) {
		this.statistic = statistic;
		this.value = value;
	}
	
	public void saveToDatabase() {
		MySQLUtils.set("INSERT INTO " + TABLE_NAME + " (statistic, value, time) VALUES (?, ?, ?)", statistic.getId() + "", value + "", System.currentTimeMillis() + "");
	}
	
	public boolean compare(StatisticsEntry entry) {
		if (entry.getStatistic() == statistic) {
			return true;
		}
		return false;
	}
	
	public void incrementFrom(StatisticsEntry entry) {
		value += entry.getValue();
	}
	
	public Statistic getStatistic() {
		return statistic;
	}
	
	public void setStatistic(Statistic statistic) {
		this.statistic = statistic;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
}