package io.matita08.plugins.teams.data;

import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.services.Service;
import io.matita08.plugins.teams.storage.StorageManager;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Team{
   private static final Map<String, Team> teams = new HashMap<>();
   @Getter private final String name;
   public static final List<org.bukkit.entity.Player> spy = new ArrayList<>();
   @Getter private final List<Player> members = new ArrayList<>();
   @Getter private final Set<String> ally = new HashSet<>();
   @Getter private final Set<String> enemy = new HashSet<>();
   @Getter private final Player owner;
   public int online = 0;
   
   public static boolean existTeam(String name) {
      if(name == null || name.isBlank() || teams.containsKey(name.strip())) return true;
      return StorageManager.getInstance().teamExists(name.strip());
   }
   
   public static Team createTeam(Player creator, String name) {
      name = name.trim();
      if(name.length() > 60) return null;
      if(existTeam(name)) return null;
      
      Team team = new Team(name,  creator);
      team.addMember(creator);
      teams.put(name, team);
      return team;
   }
   
   public void deleteTeam() {
      this.members.forEach(member -> member.setTeam(null));
      Bukkit.getScheduler().runTaskAsynchronously(TeamsPlugin.getInstance(), () -> StorageManager.getInstance().deleteTeam(name));
      teams.remove(name);
   }
   
   public static Team loadTeam(String teamName, Player creator) {
      if(teamName == null || teamName.isBlank()) return null;
      Team t = new Team(teamName, creator);
      teams.put(teamName, t);
      creator.setTeam(t);
      return t;
   }
   
   public static Team getTeam(String name) {
      return teams.get(name) == null ? StorageManager.getInstance().loadTeam(name) : teams.get(name);
   }
   
   public void addMember(Player player) {
      members.add(player);
      player.setTeam(this);
   }
   
   public void removeMember(Player player) {
      members.remove(player);
      player.setTeam(null);
   }
   
   public void addAlly(String teamName) {
      ally.add(teamName);
   }
   
   public void addEnemy(String teamName) {
      enemy.add(teamName);
   }
   
   public void removeAlly(String teamName) {
      ally.remove(teamName);
   }
   
   public void removeEnemy(String teamName) {
      enemy.remove(teamName);
   }
   
   public boolean isAlly(String teamName) {
      return ally.contains(teamName);
   }
   
   public boolean isEnemy(String teamName) {
      return enemy.contains(teamName);
   }
   
   @SuppressWarnings("deprecation")
   public void broadcast(String message) {
      for(Player player : members) {
         if(player.isOnline()) player.getPlayer().sendMessage(ChatColor.BLUE + "[Team chat] " + ChatColor.RESET + message);
      }
      for(org.bukkit.entity.Player player : spy) {
         if(player.isOnline()) player.sendMessage(ChatColor.BLUE + "[SPY] [" + name  + "]" + ChatColor.RESET + message);
      }
   }
   
   @Override
   public boolean equals(Object o) {
      if(!(o instanceof Team team)) return false;
      return name.equals(team.name);
   }
   
   @Override
   public int hashCode() {
      return name.hashCode();
   }
   
   @Override
   public String toString() {
      return "io.matita08.plugins.teams.data.Team{" +
          "enemy=" + getEnemy() +
          ", ally=" + getAlly() +
          ", members=" + getMembers() +
          ", name='" + getName() + '\'' +
          '}';
   }
   
   static class TeamCacheService implements Service {
      @Override
      public void unload() {
         teams.forEach((teamName, team) -> StorageManager.getInstance().saveTeam(team));
         teams.clear();
      }
   }
   
   static {
      TeamsPlugin.loadService(new TeamCacheService());
   }
}
