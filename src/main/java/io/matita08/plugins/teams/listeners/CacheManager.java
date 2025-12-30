package io.matita08.plugins.teams.listeners;

import io.matita08.plugins.teams.data.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class CacheManager implements Listener {
   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event){
      org.bukkit.entity.Player player = event.getPlayer();
      Player.unloadPlayer(player);
   }
   
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event){
      Player p = Player.loadPlayer(event.getPlayer());
      Optional.ofNullable(p.getTeam()).ifPresent(t -> t.online++);
   }
}
