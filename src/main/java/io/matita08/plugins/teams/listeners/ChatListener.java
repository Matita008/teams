package io.matita08.plugins.teams.listeners;

import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.data.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
   @EventHandler
   public void onChat(AsyncPlayerChatEvent event){
      if(event.isCancelled()) return;
      
      Player p = Player.loadPlayer(event.getPlayer());
      if(p.getTeam() == null) return;
      if(p.isTeamChat()) {
         event.setCancelled(true);
         TeamsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(TeamsPlugin.getInstance(), () -> p.getTeam().broadcast(event.getMessage()));
      } else event.setMessage(ChatColor.AQUA + "[" + p.getTeam().getName() + "] " + ChatColor.RESET + event.getMessage());
   }
}
