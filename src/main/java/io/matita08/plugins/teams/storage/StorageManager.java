package io.matita08.plugins.teams.storage;

import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.data.Player;
import io.matita08.plugins.teams.storage.implementations.H2db;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

public class StorageManager {
   @Getter private static Storage instance;
   
   public static void init(ConfigurationSection config) {
      Storage storage = createH2(config);//TODO
      TeamsPlugin.loadService(storage);
      instance = storage;
   }
   
   public static void savePlayer(Player player) {
      instance.savePlayer(player);
   }
   
   private static Storage createH2(ConfigurationSection config) {
      Storage storage = new H2db();
      storage.init(config);
      return storage;
   }
}
