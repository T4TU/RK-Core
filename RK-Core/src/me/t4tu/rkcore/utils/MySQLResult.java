package me.t4tu.rkcore.utils;

import java.util.List;
import java.util.Map;

public class MySQLResult {
	
	private List<Map<String, String>> result;
	
	public MySQLResult(List<Map<String, String>> result) {
		this.result = result;
	}
	
	public List<Map<String, String>> getResult() {
		return result;
	}
	
	public int getRows() {
		return result.size();
	}
	
	public Map<String, String> getRow(int row) {
		try {
			return result.get(row);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public String getString(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				return data.get(column);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	public String getStringNotNull(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				if (data.get(column) != null) {
					return data.get(column);
				}
				else {
					return "";
				}
			}
			else {
				return "";
			}
		}
		else {
			return "";
		}
	}
	
	public int getInt(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				try {
					return Integer.parseInt(data.get(column));
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	public double getDouble(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				try {
					return Double.parseDouble(data.get(column));
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	public long getLong(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				try {
					return Long.parseLong(data.get(column));
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	public boolean getBoolean(int row, String column) {
		Map<String, String> data = getRow(row);
		if (data != null) {
			if (data.containsKey(column)) {
				if (data.get(column).equals("1") || data.get(column).equals("true")) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
}