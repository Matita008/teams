package io.matita08.plugins.teams.storage.implementations;

import io.matita08.plugins.teams.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public record CommonQuery(String query) {
   public static final CommonQuery CREATE = q("CREATE TABLE IF NOT EXISTS " + Config.getTablePrefix() + "%s (%s)");
   public static final CommonQuery SELECT = q("SELECT %s FROM " + Config.getTablePrefix() + "%s WHERE %s");
   public static final CommonQuery INSERT = q("INSERT INTO " + Config.getTablePrefix() + "%s (%s) VALUES (%s)");
   public static final CommonQuery UPDATE = q("UPDATE " + Config.getTablePrefix() + "%s SET %s WHERE %s");
   public static final CommonQuery DELETE = q("DELETE FROM " + Config.getTablePrefix() + "%s WHERE %s");
   
   public static final CommonQuery CHECK = q(SELECT, "1", "%s", "%s = ?");
   
   public static final CommonQuery CREATE_PLAYER_TABLE = q(CREATE, "players", "uuid VARCHAR(36) PRIMARY KEY, team VARCHAR(64)");
   public static final CommonQuery CREATE_TEAM_TABLE = q(CREATE, "teams", "name VARCHAR(64) PRIMARY KEY");
   public static final CommonQuery CREATE_ALLY_TABLE = q(CREATE, "ally", "team1 VARCHAR(64), team2 VARCHAR(64), PRIMARY KEY (team1, team2)");
   public static final CommonQuery CREATE_ENEMY_TABLE = q(CREATE, "enemy", "team1 VARCHAR(64), team2 VARCHAR(64), PRIMARY KEY (team1, team2)");
   public static final CommonQuery GET_PLAYERS_BY_TEAM = q(SELECT, "uuid", "players", "team = ?");
   public static final CommonQuery CHECK_PLAYER = q(CHECK, "players", "uuid");
   public static final CommonQuery CHECK_TEAM = q(CHECK, "teams", "name");
   public static final CommonQuery DELETE_TEAM = q(DELETE, "teams", "team = '%s'");
   public static final CommonQuery DELETE_PLAYER_TEAM = q(DELETE, "teams", "%s");
   
   private static CommonQuery q(String s) {
      return new CommonQuery(s);
   }
   
   private static CommonQuery q(CommonQuery q, String... args) {
      return q(q.format(args));
   }
   
   public String format(String... args) {
      return String.format(query, (Object[])args);
   }
   
   public PreparedStatement get(Connection conn) throws SQLException {return conn.prepareStatement(query);}
}
