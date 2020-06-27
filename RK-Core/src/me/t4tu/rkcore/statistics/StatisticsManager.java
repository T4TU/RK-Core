package me.t4tu.rkcore.statistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import me.t4tu.rkcore.Core;
//import me.t4tu.rkcore.utils.MySQLResult;

public class StatisticsManager {
	
	private static Core core;
	
	private List<StatisticsEntry> cache;
	
	public StatisticsManager(Core core) {
		StatisticsManager.core = core;
		cache = new ArrayList<StatisticsEntry>();
	}
	
	public List<StatisticsEntry> getCache() {
		return cache;
	}
	
	public void saveCacheToDatabase() {
		List<StatisticsEntry> cacheCopy = new ArrayList<StatisticsEntry>();
		cacheCopy.addAll(cache);
		cache.clear();
		for (StatisticsEntry entry : cacheCopy) {
			entry.saveToDatabase();
		}
	}
	
	public void logStatistic(StatisticsEntry entry) {
		Iterator<StatisticsEntry> iterator = cache.iterator();
		while (iterator.hasNext()) {
			StatisticsEntry cacheEntry = iterator.next();
			if (entry.compare(cacheEntry)) {
				iterator.remove();
			}
		}
		cache.add(entry);
	}
	
	public void incrementStatistic(StatisticsEntry entry) {
		Iterator<StatisticsEntry> iterator = cache.iterator();
		while (iterator.hasNext()) {
			StatisticsEntry cacheEntry = iterator.next();
			if (entry.compare(cacheEntry)) {
				entry.incrementFrom(cacheEntry);
				iterator.remove();
			}
		}
		cache.add(entry);
	}
	
//	public List<StatisticsResult> parseStatistics(MySQLResult result) {
//		List<StatisticsResult> results = new ArrayList<StatisticsResult>();
//		if (result != null) {
//			boolean containsPlayer = result.getRow(0).containsKey("player");
//			boolean containsData = result.getRow(0).containsKey("data");
//			for (int i = 0; i < result.getRows(); i++) {
//				Statistic statistic = Statistic.getById(result.getInt(i, "statistic"));
//				StatisticsEntry entry;
//				if (containsPlayer && containsData) {
//					entry = new ComplexPlayerStatisticsEntry(statistic, result.getInt(i, "value"), 
//							result.getString(i, "player"), result.getInt(i, "data"));
//				}
//				else if (containsPlayer) {
//					entry = new PlayerStatisticsEntry(statistic, result.getInt(i, "value"), 
//							result.getString(i, "player"));
//				}
//				else {
//					entry = new StatisticsEntry(statistic, result.getInt(i, "value"));
//				}
//				long time = result.getLong(i, "time");
//				results.add(new StatisticsResult(entry, time));
//			}
//		}
//		return results;
//	}
//	
//	public List<StatisticsEntry> combineSimilar(List<StatisticsResult> results) {
//		List<StatisticsEntry> entries = new ArrayList<StatisticsEntry>();
//		for (StatisticsResult result : results) {
//			boolean b = true;
//			for (StatisticsEntry entry : entries) {
//				if (entry.compare(result.getStatisticsEntry())) {
//					entry.incrementFrom(result.getStatisticsEntry());
//					b = false;
//					break;
//				}
//			}
//			if (b) {
//				entries.add(result.getStatisticsEntry());
//			}
//		}
//		return entries;
//	}
//	
//	public List<StatisticsEntry> combineSimilarData(List<StatisticsResult> results) {
//		List<StatisticsEntry> entries = new ArrayList<StatisticsEntry>();
//		for (StatisticsResult result : results) {
//			boolean b = true;
//			for (StatisticsEntry entry : entries) {
//				if (result.getStatisticsEntry() instanceof ComplexPlayerStatisticsEntry) {
//					ComplexPlayerStatisticsEntry resultEntry = (ComplexPlayerStatisticsEntry) result.getStatisticsEntry();
//					ComplexPlayerStatisticsEntry e = (ComplexPlayerStatisticsEntry) entry;
//					if (e.getData() == resultEntry.getData()) {
//						entry.incrementFrom(result.getStatisticsEntry());
//						b = false;
//						break;
//					}
//				}
//			}
//			if (b) {
//				entries.add(result.getStatisticsEntry());
//			}
//		}
//		return entries;
//	}
//	
//	public List<StatisticsEntry> combineSimilarPlayers(List<StatisticsResult> results) {
//		List<StatisticsEntry> entries = new ArrayList<StatisticsEntry>();
//		for (StatisticsResult result : results) {
//			boolean b = true;
//			for (StatisticsEntry entry : entries) {
//				if (result.getStatisticsEntry() instanceof PlayerStatisticsEntry) {
//					PlayerStatisticsEntry resultEntry = (PlayerStatisticsEntry) result.getStatisticsEntry();
//					PlayerStatisticsEntry e = (PlayerStatisticsEntry) entry;
//					if (e.getUuid().equals(resultEntry.getUuid())) {
//						entry.incrementFrom(result.getStatisticsEntry());
//						b = false;
//						break;
//					}
//				}
//			}
//			if (b) {
//				entries.add(result.getStatisticsEntry());
//			}
//		}
//		return entries;
//	}
	
	public static void logStatistics(StatisticsEntry entry) {
		core.getStatisticsManager().logStatistic(entry);
	}
	
	public static void incrementStatistics(StatisticsEntry entry) {
		core.getStatisticsManager().incrementStatistic(entry);
	}
	
	public static class ValueSorter implements Comparator<StatisticsEntry> {
		
		public int compare(StatisticsEntry entry1, StatisticsEntry entry2) {
			return entry2.getValue() - entry1.getValue();
		}
	}
}