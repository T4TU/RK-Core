package me.t4tu.rkcore.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.Core;

public class MySQLUtils {
	
	private static Core core;
	private static Connection connection;
	private static int queriesPerMinute = -1;
	private static int queriesCount = 0;
	
	public static void setCore(Core plugin) {
		core = plugin;
	}
	
	public static void startCounterClock() {
		new BukkitRunnable() {
			public void run() {
				queriesPerMinute = queriesCount;
				queriesCount = 0;
			}
		}.runTaskTimer(core, 0, 1200);
	}
	
	public static int getQueriesPerMinute() {
		return queriesPerMinute;
	}
	
	public static synchronized boolean openConnection() {
		String host = CoreUtils.getMySqlHost();
		String username = CoreUtils.getMySqlUsername();
		String password = CoreUtils.getMySqlPassword();
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + host + "?useSSL=false", username, password);
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean contains(String query, List<String> strings) {
		return get(query, strings) != null;
	}
	
	public static boolean contains(String query, String... strings) {
		return contains(query, Arrays.asList(strings));
	}
	
	public static MySQLResult get(String query, List<String> strings) {
		// TODO
		query = query.replace(" rank", " placeholder");
		queriesCount++;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			for (int i = 0 ; i < strings.size() ; i++) {
				preparedStatement.setString(i + 1, strings.get(i));
			}
			ResultSet results = preparedStatement.executeQuery();
			List<Map<String, String>> resultRows = new ArrayList<Map<String, String>>();
			while (results.next()) {
				Map<String, String> resultColumns = new HashMap<String, String>();
				for (int i = 1; i <= results.getMetaData().getColumnCount(); i++) {
					String value = results.getString(i);
					if (value != null && value.equals("")) {
						value = null;
					}
					// TODO
					if (results.getMetaData().getColumnName(i).equals("placeholder")) {
						resultColumns.put("rank", value);
					}
					else {
						resultColumns.put(results.getMetaData().getColumnName(i), value);
					}
				}
				resultRows.add(resultColumns);
			}
			preparedStatement.close();
			if (!resultRows.isEmpty()) {
				return new MySQLResult(resultRows);
			}
			else {
				return null;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static MySQLResult get(String query, String... strings) {
		return get(query, Arrays.asList(strings));
	}
	
	public static int set(String query, List<String> strings) {
		// TODO
		query = query.replace(" rank", " placeholder");
		queriesCount++;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			for (int i = 0 ; i < strings.size() ; i++) {
				preparedStatement.setString(i + 1, strings.get(i));
			}
			int i = preparedStatement.executeUpdate();
			preparedStatement.close();
			return i;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static int set(String query, String... strings) {
		return set(query, Arrays.asList(strings));
	}
}