package io.matita08.plugins.teams.storage.implementations;

import io.matita08.plugins.teams.Config;
import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.data.Player;
import io.matita08.plugins.teams.data.Team;
import io.matita08.plugins.teams.storage.Storage;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class H2db implements Storage {
   private Connection connection;
   private PreparedStatement insertPlayerStmt;
   private PreparedStatement updatePlayerStmt;
   private PreparedStatement loadPlayerStmt;
   private PreparedStatement checkPlayerStmt;
   private PreparedStatement insertTeamStmt;
   private PreparedStatement checkTeamStmt;
   private PreparedStatement getPlayersStmt;
   private PreparedStatement loadAllyStmt;
   private PreparedStatement loadEnemyStmt;
   private PreparedStatement deleteAllyStmt;
   private PreparedStatement deleteEnemyStmt;
   private PreparedStatement insertAllyStmt;
   private PreparedStatement insertEnemyStmt;
   private static final String INSERT_PLAYER = "INSERT INTO " + Config.getTablePrefix() + "players (uuid, team) VALUES (?, ?)";
   private static final String UPDATE_PLAYER = "UPDATE " + Config.getTablePrefix() + "players SET team = ? WHERE uuid = ?";
   private static final String LOAD_PLAYER = "SELECT team FROM " + Config.getTablePrefix() + "players WHERE uuid = ?";
   private static final String INSERT_TEAM = "INSERT INTO " + Config.getTablePrefix() + "teams (name) VALUES (?)";
   private static final String LOAD_ALLY = "SELECT team2 FROM " + Config.getTablePrefix() + "ally WHERE team1 = ?";
   private static final String LOAD_ENEMY = "SELECT team2 FROM " + Config.getTablePrefix() + "enemy WHERE team1 = ?";
   private static final String DELETE_ALLY = "DELETE FROM " + Config.getTablePrefix() + "ally WHERE team1 = ?";
   private static final String DELETE_ENEMY = "DELETE FROM " + Config.getTablePrefix() + "enemy WHERE team1 = ?";
   private static final String INSERT_ALLY = "INSERT INTO " + Config.getTablePrefix() + "ally (team1, team2) VALUES (?, ?)";
   private static final String INSERT_ENEMY = "INSERT INTO " + Config.getTablePrefix() + "enemy (team1, team2) VALUES (?, ?)";
   
   @Override
   @SneakyThrows
   public void init(ConfigurationSection config) {
      Class.forName("org.h2.Driver"); //needed to make sure the driver is loaded
      String url = "jdbc:h2:" + config.getString("path", TeamsPlugin.getInstance().getDataFolder().getCanonicalPath() + "/db/h2");
      connection = DriverManager.getConnection(url);
   }
   
   @Override
   @SneakyThrows
   public Player loadPlayer(String id) {
      loadPlayerStmt.setString(1, id);
      ResultSet rs = loadPlayerStmt.executeQuery();
      if(rs.next()) {
         return Player.load(id, Team.getTeam(rs.getString("team")));
      }
      return Player.load(id);
   }
   
   @Override
   @SneakyThrows
   public Team loadTeam(String name) {
      if(name != null && teamExists(name)) {
         Team team = Team.loadTeam(name);
         loadAllyStmt.setString(1, name);
         ResultSet allyRs = loadAllyStmt.executeQuery();
         while(allyRs.next()) {
            team.addAlly(allyRs.getString("team2"));
         }
         loadEnemyStmt.setString(1, name);
         ResultSet enemyRs = loadEnemyStmt.executeQuery();
         while(enemyRs.next()) {
            team.addEnemy(enemyRs.getString("team2"));
         }
         
         
         
         return team;
      }
      return null;
   }
   
   @Override
   @SneakyThrows
   public void savePlayer(Player player) {
      checkPlayerStmt.setString(1, player.getUUID());
      ResultSet rs = checkPlayerStmt.executeQuery();
      String team = player.getTeam() == null ? null : player.getTeam().getName();
      if(rs.next()) {
         updatePlayerStmt.setString(1, team);
         updatePlayerStmt.setString(2, player.getUUID());
         updatePlayerStmt.executeUpdate();
      } else {
         insertPlayerStmt.setString(1, player.getUUID());
         insertPlayerStmt.setString(2, team);
         insertPlayerStmt.executeUpdate();
      }
   }
   
   @Override
   @SneakyThrows
   public void saveTeam(Team team) {
      checkTeamStmt.setString(1, team.getName());
      ResultSet rs = checkTeamStmt.executeQuery();
      if(!rs.next()) {
         insertTeamStmt.setString(1, team.getName());
         insertTeamStmt.executeUpdate();
      }
      
      deleteAllyStmt.setString(1, team.getName());
      deleteAllyStmt.executeUpdate();
      deleteEnemyStmt.setString(1, team.getName());
      deleteEnemyStmt.executeUpdate();
      
      // Batch insert allies
      for (String ally: team.getAlly()) {
         insertAllyStmt.setString(1, team.getName());
         insertAllyStmt.setString(2, ally);
         insertAllyStmt.addBatch();
      }
      if(!team.getAlly().isEmpty()) {
         insertAllyStmt.executeBatch();
      }
      
      // Batch insert enemies
      for (String enemy: team.getEnemy()) {
         insertEnemyStmt.setString(1, team.getName());
         insertEnemyStmt.setString(2, enemy);
         insertEnemyStmt.addBatch();
      }
      if(!team.getEnemy().isEmpty()) {
         insertEnemyStmt.executeBatch();
      }
   }
   
   @Override
   @SneakyThrows
   public boolean teamExists(String name) {
      checkTeamStmt.setString(1, name);
      ResultSet rs = checkTeamStmt.executeQuery();
      return rs.next();
   }
   
   @Override
   @SneakyThrows
   public void deleteTeam(String name) {
      // Delete allies and enemies relationships
      deleteAllyStmt.setString(1, name);
      deleteAllyStmt.executeUpdate();
      deleteEnemyStmt.setString(1, name);
      deleteEnemyStmt.executeUpdate();
      
      // Delete team from the teams table
      connection.createStatement().execute(CommonQuery.DELETE_TEAM.format(name.replace("'", "\\'")));
      
      // Update players that belonged to this team
      connection.createStatement().execute("UPDATE " + Config.getTablePrefix() + "players SET team = NULL WHERE team = '" + name + "'");
      
      // Delete team from memory
       Team.deleteTeam(name);
   }
   
   @Override
   @SneakyThrows
   public void load() {
      connection.createStatement().execute(CommonQuery.CREATE_PLAYER_TABLE.query());
      connection.createStatement().execute(CommonQuery.CREATE_TEAM_TABLE.query());
      connection.createStatement().execute(CommonQuery.CREATE_ALLY_TABLE.query());
      connection.createStatement().execute(CommonQuery.CREATE_ENEMY_TABLE.query());
      
      insertPlayerStmt = connection.prepareStatement(INSERT_PLAYER);
      updatePlayerStmt = connection.prepareStatement(UPDATE_PLAYER);
      loadPlayerStmt = connection.prepareStatement(LOAD_PLAYER);
      checkPlayerStmt = CommonQuery.CHECK_PLAYER.get(connection);
      insertTeamStmt = connection.prepareStatement(INSERT_TEAM);
      checkTeamStmt = CommonQuery.CHECK_TEAM.get(connection);
      loadAllyStmt = connection.prepareStatement(LOAD_ALLY);
      loadEnemyStmt = connection.prepareStatement(LOAD_ENEMY);
      deleteAllyStmt = connection.prepareStatement(DELETE_ALLY);
      deleteEnemyStmt = connection.prepareStatement(DELETE_ENEMY);
      insertAllyStmt = connection.prepareStatement(INSERT_ALLY);
      insertEnemyStmt = connection.prepareStatement(INSERT_ENEMY);
      getPlayersStmt = CommonQuery.GET_PLAYERS_BY_TEAM.get(connection);
   }
   
   @Override
   public void unload() {
      close(insertPlayerStmt, updatePlayerStmt, loadPlayerStmt, checkPlayerStmt, insertTeamStmt, checkTeamStmt);
      close(loadAllyStmt, loadEnemyStmt, deleteAllyStmt, deleteEnemyStmt, insertAllyStmt, insertEnemyStmt, connection);
   }
   
   private static void close(AutoCloseable... toClose) {
      for(AutoCloseable closeable: toClose) {
         if(closeable != null) {
            try {
               closeable.close();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      }
   }
}
