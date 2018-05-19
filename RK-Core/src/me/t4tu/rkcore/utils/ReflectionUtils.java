package me.t4tu.rkcore.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.md_5.bungee.api.ChatMessageType;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;

public class ReflectionUtils {
	
	private static Field aField;
	private static Field bField;
	private static net.minecraft.server.v1_12_R1.Scoreboard board = new net.minecraft.server.v1_12_R1.Scoreboard();
	public static ScoreboardTeam vankilassa = new ScoreboardTeam(board, "5vankilassa");
	public static ScoreboardTeam def = new ScoreboardTeam(board, "6default");
	public static ScoreboardTeam ritari = new ScoreboardTeam(board, "5ritari");
	public static ScoreboardTeam aatelinen = new ScoreboardTeam(board, "4aatelinen");
	public static ScoreboardTeam arkkitehti = new ScoreboardTeam(board, "3arkkitehti");
	public static ScoreboardTeam valvoja = new ScoreboardTeam(board, "2valvoja");
	public static ScoreboardTeam moderaattori = new ScoreboardTeam(board, "1moderaattori");
	public static ScoreboardTeam ylläpitäjä = new ScoreboardTeam(board, "0ylläpitäjä");
	public static ScoreboardTeam upsidedown = new ScoreboardTeam(board, "upsidedown");
	
	public static void loadScoreboardTeams() {
		
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		Team t0 = scoreboard.getTeam("5vankilassa");
		if (t0 == null) {
			t0 = scoreboard.registerNewTeam("5vankilassa");
		}
		t0.setPrefix("§4§l ✖ §8§m");
		
		Team t1 = scoreboard.getTeam("6default");
		if (t1 == null) {
			t1 = scoreboard.registerNewTeam("6default");
		}
		t1.setPrefix("§7 ");
		
		Team t2 = scoreboard.getTeam("5ritari");
		if (t2 == null) {
			t2 = scoreboard.registerNewTeam("5ritari");
		}
		t2.setPrefix("§2§l ▶ §7");
		
		Team t3 = scoreboard.getTeam("4aatelinen");
		if (t3 == null) {
			t3 = scoreboard.registerNewTeam("4aatelinen");
		}
		t3.setPrefix("§6§l ▶ §7");
		
		Team t4 = scoreboard.getTeam("3arkkitehti");
		if (t4 == null) {
			t4 = scoreboard.registerNewTeam("3arkkitehti");
		}
		t4.setPrefix("§e ✸ §7");
		
		Team t5 = scoreboard.getTeam("2valvoja");
		if (t5 == null) {
			t5 = scoreboard.registerNewTeam("2valvoja");
		}
		t5.setPrefix("§c ✸ §7");
		
		Team t6 = scoreboard.getTeam("1moderaattori");
		if (t6 == null) {
			t6 = scoreboard.registerNewTeam("1moderaattori");
		}
		t6.setPrefix("§c ✸ §7");
		
		Team t7 = scoreboard.getTeam("0ylläpitäjä");
		if (t7 == null) {
			t7 = scoreboard.registerNewTeam("0ylläpitäjä");
		}
		t7.setPrefix("§4 ✸ §7");
		
		Team t8 = scoreboard.getTeam("upsidedown");
		if (t8 == null) {
			t8 = scoreboard.registerNewTeam("upsidedown");
		}
		t8.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
	}
	
	public static void sendEntityDestroyPacket(Player p, int id) {
		Constructor<?> con;
		try {
			con = getNMSClass("PacketPlayOutEntityDestroy").getConstructor(int[].class);
			int[] i = {id};
			Object packet = con.newInstance(i);
			sendPacket(p, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendChatPacket(Player p, String s, ChatMessageType type) {
		Constructor<?> con;
		try {
			Object chat = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, s);
			con = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), getNMSClass("ChatMessageType"));
			Object packet = con.newInstance(chat, net.minecraft.server.v1_12_R1.ChatMessageType.valueOf(type.toString().replace("ACTION_BAR", "GAME_INFO")));
			sendPacket(p, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendTabHeaderFooterPacket(Player p, String s, String s2) {
		Constructor<?> con;
		try {
			Object c = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, s);
			Object c2 = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, s2);
			con = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
			Object packet = con.newInstance();
			try {
				if (aField == null) {
					aField = packet.getClass().getDeclaredField("a");
					aField.setAccessible(true);
				}
				aField.set(packet, c);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
			try {
				if (bField == null) {
					bField = packet.getClass().getDeclaredField("b");
					bField.setAccessible(true);
				}
				bField.set(packet, c2);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
			sendPacket(p, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendScoreboardTeamPacket(Player p, String s, ScoreboardTeam t) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(t, Arrays.asList(s), 3);
		CraftPlayer pl = (CraftPlayer) p;
		pl.getHandle().playerConnection.sendPacket(packet);
	}
	
	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getPing(Player player) {
		try {
			Object o = player.getClass().getMethod("getHandle").invoke(player);
			return o.getClass().getField("ping").getInt(o);
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static double getTPS() {
		try {
			Object server = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
			double[] tps = (double[]) server.getClass().getField("recentTps").get(server);
			return tps[0];
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}