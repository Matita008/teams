package io.matita08.plugins.teams.data;

import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.services.Service;
import io.matita08.plugins.teams.storage.StorageManager;
import lombok.*;
import org.bukkit.Bukkit;

import java.util.*;

public class Player {
   @Getter @Setter private Team team;
   @Getter private final org.bukkit.entity.Player player;
   private static final Set<Player> players = new HashSet<>();
   @Getter @Setter private boolean teamChat;
   
   private Player(org.bukkit.entity.Player player) {
      this.player = player;
      players.add(this);
   }
   
   public static Player load(String uuid, Team team) {
      Player p = new Player(Bukkit.getPlayer(UUID.fromString(uuid)));
      p.team = team;
      return p;
   }
   
   public static Player load(String uuid) { return load(uuid, null); }
   
   public static Player getPlayer(org.bukkit.entity.Player player) {
      return players.parallelStream().filter(p -> p.getPlayer().equals(player)).findFirst().orElse(null);
   }
   
   public static Player loadPlayer(org.bukkit.entity.Player player) {
      Player p = getPlayer(player);
      return p == null? StorageManager.getInstance().loadPlayer(player.getUniqueId().toString()) : p;
   }
   
   public static void unloadPlayer(org.bukkit.entity.Player p) {
      Player player = getPlayer(p);
      if(player == null) return;
      
      if(player.team == null) {
         savePlayer(player);
         return;
      }
      
      player.team.online--;
      if(player.team.online != 0) return;
      player.team.getMembers().forEach(Player::savePlayer);
   }
   
   public static void savePlayer(Player player) {
      players.remove(player);
      StorageManager.savePlayer(player);
   }
   
   public String getUUID() { return player.getUniqueId().toString(); }
   
   public boolean isOnline() { return player.isOnline(); }
   
   @Override
   public boolean equals(Object o) {
      if(!(o instanceof Player player1)) return false;
      return player.equals(player1.player);
   }
   
   @Override
   public int hashCode() {
      return player.hashCode();
   }
   
   static class PlayerCahceService implements Service{
      @Override
      public void unload() {
         players.forEach(Player::savePlayer);
         players.clear();
      }
   }
   
   static {
      TeamsPlugin.loadService(new PlayerCahceService());
   }
}
