package io.matita08.plugins.teams.storage;

import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.data.Player;
import io.matita08.plugins.teams.storage.implementations.H2db;
import io.matita08.plugins.teams.storage.implementations.MariaDB;
import io.matita08.plugins.teams.storage.implementations.SQLitedb;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

public class StorageManager {
   @Getter private static Storage instance;
   
   public static void init(ConfigurationSection config) {
      switch(config.getString("type", "h2").toLowerCase()) {
         case "mariadb" -> instance = new MariaDB();
         case "sqlite" -> instance = new SQLitedb();
         default -> instance = new H2db();
      }
      instance.init(config);
      TeamsPlugin.loadService(instance);
   }
   
   public static void savePlayer(Player player) {
      instance.savePlayer(player);
   }
}
