package io.matita08.plugins.teams.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CacheManager implements Listener {
   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event){
      Player player = event.getPlayer();
      io.matita08.plugins.teams.data.Player.unloadPlayer(player);
   }
   
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event){
      io.matita08.plugins.teams.data.Player.loadPlayer(event.getPlayer());
   }
}
