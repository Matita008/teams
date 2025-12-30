package io.matita08.plugins.teams.storage.implementations;

import io.matita08.plugins.teams.TeamsPlugin;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2db extends Database {
   @Getter private Connection connection;
   
   @Override
   @SneakyThrows
   public void init(ConfigurationSection config) {
      Class.forName("org.h2.Driver"); //needed to make sure the driver is loaded
      String url = "jdbc:h2:" + config.getString("path", TeamsPlugin.getInstance().getDataFolder().getCanonicalPath() + "/db/h2");
      connection = DriverManager.getConnection(url);
   }
   
   @Override
   public void unload() {
      super.unload();
      close(connection);
   }
}
