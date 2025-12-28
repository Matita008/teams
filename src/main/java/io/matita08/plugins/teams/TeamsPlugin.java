package io.matita08.plugins.teams;

import io.matita08.plugins.teams.services.Service;
import lombok.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class TeamsPlugin extends JavaPlugin {
   @Getter private static TeamsPlugin instance;
   private final List<Service> services = new ArrayList<>();
   
   @Override
   public void onEnable() {
      instance = this;
   }
   
   @Override
   public void onDisable() {
      services.forEach(Service::unload);
      services.clear();
   }
   
   public static void loadService(Service service) {
      instance.services.add(service);
      service.load();
   }
}
