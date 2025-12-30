package io.matita08.plugins.teams;

import com.google.common.base.Verify;
import io.matita08.plugins.teams.commands.TeamCommand;
import io.matita08.plugins.teams.listeners.CacheManager;
import io.matita08.plugins.teams.listeners.ChatListener;
import io.matita08.plugins.teams.services.Service;
import io.matita08.plugins.teams.storage.Storage;
import lombok.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TeamsPlugin extends JavaPlugin {
   @Getter private static TeamsPlugin instance;
   @Getter private static Logger log;
   private final List<Service> services = new ArrayList<>();
   
   @Override
   public void onEnable() {
      instance = this;
      log = getLogger();
      saveDefaultConfig();
      
      Config.init((YamlConfiguration) getConfig());
      
      loadListeners();
      loadCommands();
      
      log.info("[TeamsPlugin] Plugin has enabled successfully!.");
   }
   
   @Override
   public void onDisable() {
      //Ensure db is the last to be disabled
      services.stream().filter(s -> !(s instanceof Storage)).forEach(Service::unload);
      services.stream().filter(s -> s instanceof Storage).forEach(Service::unload);
      services.clear();
      log.info("[TeamsPlugin] Plugin has been disabled successfully!.");
   }
   
   public static void loadService(Service service) {
      if(service == null) {
         log.log(Level.WARNING, "Please report this to the devs (error: Service is null)", new NullPointerException());
         return;
      }
      instance.services.add(service);
      service.load();
   }
   
   private void loadListeners(){
      getServer().getPluginManager().registerEvents(new ChatListener(), this);
      getServer().getPluginManager().registerEvents(new CacheManager(), this);
   }
   
   private void loadCommands() {
      Verify.verifyNotNull(getCommand("team")).setExecutor(new TeamCommand());
   }
}
