package me.t4tu.rkcore.utils;

public class MySQLCreator {
	
	private static final String[] queries = {
			"CREATE TABLE global (record int(11) NOT NULL, online int(11) NOT NULL, uniquejoins int(11) NOT NULL)", 
			"INSERT INTO global (record, online, uniquejoins) VALUES (0, 0, 0)", 
			"CREATE TABLE guilds (id int(11) NOT NULL, guild_name varchar(48) NOT NULL, guild_description varchar(256) NOT NULL, leader_name varchar(16) NOT NULL, leader_uuid varchar(40) NOT NULL, members text NOT NULL)", 
			"CREATE TABLE ipbans (ip varchar(40) NOT NULL, note varchar(256) NOT NULL, joined_uuids text NOT NULL)", 
			"CREATE TABLE player_info (id int(11) NOT NULL, name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, ip varchar(40) NOT NULL, chat_prefix varchar(40) NOT NULL DEFAULT '', chat_color varchar(10) NOT NULL DEFAULT '&7', chat_nick varchar(40) NOT NULL DEFAULT '', placeholder varchar(40) NOT NULL DEFAULT 'default', seconds bigint(20) NOT NULL DEFAULT '0', last_seen bigint(20) NOT NULL DEFAULT '0', nick_last_changed bigint(20) NOT NULL DEFAULT '0')", 
			"CREATE TABLE player_stats (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, money int(11) NOT NULL DEFAULT '0', profession int(11) NOT NULL DEFAULT '0', profession_last_changed bigint(20) NOT NULL DEFAULT '0', visited_1 tinyint(1) NOT NULL DEFAULT '0', visited_2 tinyint(1) NOT NULL DEFAULT '0', visited_3 tinyint(1) NOT NULL DEFAULT '0', visited_nether tinyint(1) NOT NULL DEFAULT '0', visited_end tinyint(1) NOT NULL DEFAULT '0', status varchar(256) NOT NULL DEFAULT '<Ei tilaviestiä>', skulls varchar(256) NOT NULL DEFAULT '', friends text NOT NULL)", 
			"CREATE TABLE player_homes (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, home_1 varchar(64) NOT NULL DEFAULT '', home_1_name varchar(32) NOT NULL DEFAULT '', home_2 varchar(64) NOT NULL DEFAULT '', home_2_name varchar(32) NOT NULL DEFAULT '', home_3 varchar(64) NOT NULL DEFAULT '', home_3_name varchar(32) NOT NULL DEFAULT '', home_4 varchar(64) NOT NULL DEFAULT '', home_4_name varchar(32) NOT NULL DEFAULT '', home_5 varchar(64) NOT NULL DEFAULT '', home_5_name varchar(32) NOT NULL DEFAULT '', home_6 varchar(64) NOT NULL DEFAULT '', home_6_name varchar(32) NOT NULL DEFAULT '', home_7 varchar(64) NOT NULL DEFAULT '', "
			+ "home_7_name varchar(32) NOT NULL DEFAULT '', home_8 varchar(64) NOT NULL DEFAULT '', home_8_name varchar(32) NOT NULL DEFAULT '', home_9 varchar(64) NOT NULL DEFAULT '', home_9_name varchar(32) NOT NULL DEFAULT '', home_10 varchar(64) NOT NULL DEFAULT '', home_10_name varchar(32) NOT NULL DEFAULT '', home_11 varchar(64) NOT NULL DEFAULT '', home_11_name varchar(32) NOT NULL DEFAULT '', home_12 varchar(64) NOT NULL DEFAULT '', home_12_name varchar(32) NOT NULL DEFAULT '', home_13 varchar(64) NOT NULL DEFAULT '', home_13_name varchar(32) NOT NULL DEFAULT '', home_14 varchar(64) NOT NULL DEFAULT '', home_14_name varchar(32) NOT NULL DEFAULT '')", 
			"CREATE TABLE player_mails (id int(11) NOT NULL, sender varchar(40) NOT NULL, receiver varchar(40) NOT NULL, subject varchar(256) NOT NULL, message varchar(256) NOT NULL, seen tinyint(1) NOT NULL DEFAULT '0')", 
			"CREATE TABLE player_story (uuid varchar(40) NOT NULL, testi int(11) NOT NULL DEFAULT '0', testi2 int(11) NOT NULL DEFAULT '0')", 
			"CREATE TABLE player_tutorial (uuid varchar(40) NOT NULL)", 
			"CREATE TABLE player_ban (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, banner varchar(16) NOT NULL, reason varchar(256) NOT NULL, duration bigint(20) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE player_jail (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, jailer varchar(16) NOT NULL, reason varchar(256) NOT NULL, duration bigint(20) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE player_mute (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, muter varchar(16) NOT NULL, reason varchar(256) NOT NULL, duration bigint(20) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE player_fines (id int(11) NOT NULL, name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, amount int(11) NOT NULL, giver varchar(16) NOT NULL, reason varchar(256) NOT NULL, duration bigint(20) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE player_notes (id int(11) NOT NULL, name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, note varchar(256) NOT NULL, giver varchar(16) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE player_history (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, type varchar(16) NOT NULL, giver varchar(16) NOT NULL, reason varchar(256) NOT NULL DEFAULT '', time bigint(20) NOT NULL, duration bigint(20) NOT NULL DEFAULT '0')", 
			"CREATE TABLE player_professions (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, data varchar(512) NOT NULL DEFAULT '1,0,0,0;2,0,0,0;3,0,0,0;4,0,0,0;5,0,0,0;6,0,0,0;7,0,0,0;8,0,0,0;9,0,0,0')", 
			"CREATE TABLE player_chests (uuid varchar(40) NOT NULL)", 
			"CREATE TABLE player_cosmetics (uuid varchar(40) NOT NULL, TESTI1 tinyint(1) NOT NULL DEFAULT '0')", 
			"CREATE TABLE player_rewards (uuid varchar(40) NOT NULL, last_daily_reward bigint(20) NOT NULL)", 
			"CREATE TABLE player_settings (name varchar(16) NOT NULL, uuid varchar(40) NOT NULL, show_chat tinyint(1) NOT NULL DEFAULT '1', play_sound_mentioned tinyint(1) NOT NULL DEFAULT '1', show_msg tinyint(1) NOT NULL DEFAULT '1', play_sound_msg tinyint(1) NOT NULL DEFAULT '1', show_friend_requests tinyint(1) NOT NULL DEFAULT '1', play_sound_friends tinyint(1) NOT NULL DEFAULT '1', show_teleport_requests tinyint(1) NOT NULL DEFAULT '1', show_guild_requests tinyint(1) NOT NULL DEFAULT '1', show_party_requests tinyint(1) NOT NULL DEFAULT '1', show_bought_items tinyint(1) NOT NULL DEFAULT '1', show_death_messages tinyint(1) NOT NULL DEFAULT '1', show_afk tinyint(1) NOT NULL DEFAULT '0', show_afk_chat_notifications tinyint(1) NOT NULL DEFAULT '1', show_friend_status tinyint(1) NOT NULL DEFAULT '1', show_autobroadcast tinyint(1) NOT NULL DEFAULT '1', use_chairs tinyint(1) NOT NULL DEFAULT '1', show_lock_info tinyint(1) NOT NULL DEFAULT '1')", 
			"CREATE TABLE statistics_simple (statistic int(11) NOT NULL, value int(11) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE statistics_player (statistic int(11) NOT NULL, value int(11) NOT NULL, player varchar(40) NOT NULL, time bigint(20) NOT NULL)", 
			"CREATE TABLE statistics_player_complex (statistic int(11) NOT NULL, value int(11) NOT NULL, player varchar(40) NOT NULL, data int(11) NOT NULL, time bigint(20) NOT NULL)", 
			"ALTER TABLE guilds ADD PRIMARY KEY (id)", 
			"ALTER TABLE player_info ADD PRIMARY KEY (id)", 
			"ALTER TABLE player_mails ADD PRIMARY KEY (id)", 
			"ALTER TABLE player_fines ADD PRIMARY KEY (id)", 
			"ALTER TABLE player_notes ADD PRIMARY KEY (id)", 
			"ALTER TABLE guilds MODIFY id int(11) NOT NULL AUTO_INCREMENT", 
			"ALTER TABLE player_info MODIFY id int(11) NOT NULL AUTO_INCREMENT", 
			"ALTER TABLE player_mails MODIFY id int(11) NOT NULL AUTO_INCREMENT", 
			"ALTER TABLE player_fines MODIFY id int(11) NOT NULL AUTO_INCREMENT", 
			"ALTER TABLE player_notes MODIFY id int(11) NOT NULL AUTO_INCREMENT"};
	
	public static void setupMySQL() {
		for (String query : queries) {
			MySQLUtils.set(query);
		}
	}
}