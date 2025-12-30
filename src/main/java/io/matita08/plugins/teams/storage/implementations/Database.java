package io.matita08.plugins.teams.storage.implementations;

import io.matita08.plugins.teams.data.Player;
import io.matita08.plugins.teams.data.Team;
import io.matita08.plugins.teams.storage.Storage;
import lombok.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public abstract class Database implements Storage {
   protected PreparedStatement insertPlayerStmt;
   protected PreparedStatement updatePlayerStmt;
   protected PreparedStatement loadPlayerStmt;
   protected PreparedStatement checkPlayerStmt;
   protected PreparedStatement getOwnerStmt;
   protected PreparedStatement insertTeamStmt;
   protected PreparedStatement checkTeamStmt;
   protected PreparedStatement deleteTeamStmt;
   protected PreparedStatement clearPlayerTeamStmt;
   protected PreparedStatement getPlayersStmt;
   protected PreparedStatement loadAllyStmt;
   protected PreparedStatement loadEnemyStmt;
   protected PreparedStatement deleteAllyStmt;
   protected PreparedStatement deleteAllyStmt2;
   protected PreparedStatement deleteEnemyStmt;
   protected PreparedStatement deleteEnemyStmt2;
   protected PreparedStatement insertAllyStmt;
   protected PreparedStatement insertEnemyStmt;
   
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
         getOwnerStmt.setString(1, name);
         ResultSet rs = getOwnerStmt.executeQuery();
         if(!rs.next()) return null;
         Team team = Team.loadTeam(name, Player.getPlayer(rs.getString("owner")));
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
         getPlayersStmt.setString(1, name);
         ResultSet playersRs = getPlayersStmt.executeQuery();
         while(playersRs.next()) {
            Player.load(playersRs.getString("uuid"), team);
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
      String team = player.getTeam() == null? null : player.getTeam().getName();
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
         insertTeamStmt.setString(2, team.getOwner().getUUID());
         insertTeamStmt.executeUpdate();
      }
      
      deleteAllyStmt.setString(1, team.getName());
      deleteAllyStmt.executeUpdate();
      deleteEnemyStmt.setString(1, team.getName());
      deleteEnemyStmt.executeUpdate();
      
      for (String ally: team.getAlly()) {
         insertAllyStmt.setString(1, team.getName());
         insertAllyStmt.setString(2, ally);
         insertAllyStmt.addBatch();
      }
      if(!team.getAlly().isEmpty()) {
         insertAllyStmt.executeBatch();
      }
      
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
      deleteAllyStmt.setString(1, name);
      deleteAllyStmt.executeUpdate();
      deleteEnemyStmt.setString(1, name);
      deleteEnemyStmt.executeUpdate();
      
      deleteAllyStmt2.setString(1, name);
      deleteAllyStmt2.executeUpdate();
      deleteEnemyStmt2.setString(1, name);
      deleteEnemyStmt2.executeUpdate();
      
      deleteTeamStmt.setString(1, name);
      deleteTeamStmt.executeUpdate();
      
      clearPlayerTeamStmt.setString(1, name);
      clearPlayerTeamStmt.executeUpdate();
   }
   
   @Override
   @SneakyThrows
   public void load() {
      {
         @Cleanup
         Statement stmt = getConnection().createStatement();
         stmt.execute(CommonQuery.CREATE_PLAYER_TABLE.query());
         stmt.execute(CommonQuery.CREATE_TEAM_TABLE.query());
         stmt.execute(CommonQuery.CREATE_ALLY_TABLE.query());
         stmt.execute(CommonQuery.CREATE_ENEMY_TABLE.query());
      }
      
      insertPlayerStmt = CommonQuery.INSERT_PLAYER.get(getConnection());
      updatePlayerStmt = CommonQuery.UPDATE_PLAYER.get(getConnection());
      loadPlayerStmt = CommonQuery.LOAD_PLAYER.get(getConnection());
      checkPlayerStmt = CommonQuery.CHECK_PLAYER.get(getConnection());
      getOwnerStmt = CommonQuery.GET_TEAM_OWNER.get(getConnection());
      insertTeamStmt = CommonQuery.INSERT_TEAM.get(getConnection());
      checkTeamStmt = CommonQuery.CHECK_TEAM.get(getConnection());
      deleteTeamStmt = CommonQuery.DELETE_TEAM.get(getConnection());
      clearPlayerTeamStmt = CommonQuery.DELETE_PLAYER_TEAM.get(getConnection());
      
      loadAllyStmt = getConnection().prepareStatement(CommonQuery.LOAD_RELATION.format("ally"));
      loadEnemyStmt = getConnection().prepareStatement(CommonQuery.LOAD_RELATION.format("enemy"));
      deleteAllyStmt = getConnection().prepareStatement(CommonQuery.DELETE_RELATIONS.format("ally"));
      deleteAllyStmt2 = getConnection().prepareStatement(CommonQuery.DELETE_RELATIONS2.format("ally"));
      deleteEnemyStmt = getConnection().prepareStatement(CommonQuery.DELETE_RELATIONS.format("enemy"));
      deleteEnemyStmt2 = getConnection().prepareStatement(CommonQuery.DELETE_RELATIONS2.format("enemy"));
      insertAllyStmt = getConnection().prepareStatement(CommonQuery.INSERT_RELATION.format("ally"));
      insertEnemyStmt = getConnection().prepareStatement(CommonQuery.INSERT_RELATION.format("enemy"));
      
      getPlayersStmt = CommonQuery.GET_PLAYERS_BY_TEAM.get(getConnection());
   }
   
   @Override
   public void unload() {
      close(insertPlayerStmt, updatePlayerStmt, loadPlayerStmt, checkPlayerStmt, insertTeamStmt, checkTeamStmt);
      close(deleteTeamStmt, clearPlayerTeamStmt, loadAllyStmt, loadEnemyStmt, deleteAllyStmt, deleteAllyStmt2);
      close(deleteEnemyStmt, deleteEnemyStmt2, insertAllyStmt, insertEnemyStmt, getOwnerStmt);
   }
   
   abstract Connection getConnection();
   
   protected void close(AutoCloseable... toClose) {
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
