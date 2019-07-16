package me.t4tu.rkcore.statistics;

public class StatisticsResult {
	
	private StatisticsEntry statisticsEntry;
	private long time;
	
	public StatisticsResult(StatisticsEntry statisticsEntry, long time) {
		this.statisticsEntry = statisticsEntry;
		this.time = time;
	}
	
	public StatisticsEntry getStatisticsEntry() {
		return statisticsEntry;
	}
	
	public long getTime() {
		return time;
	}
}