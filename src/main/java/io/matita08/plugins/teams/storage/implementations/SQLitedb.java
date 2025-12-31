package io.matita08.plugins.teams.storage.implementations;

import io.matita08.plugins.teams.TeamsPlugin;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLitedb extends Database {
   @Getter private Connection connection;
   
   @Override
   @SneakyThrows
   public void init(ConfigurationSection config) {
      Class.forName("org.sqlite.JDBC"); //needed to make sure the driver is loaded
      boolean online = config.getBoolean("online", false);
      String url;
      if(online) {
         String host = config.getString("host", "localhost");
         int port = config.getInt("port", 3306);
         if(port <= 0) port = 3306;
         String database = config.getString("database", "teams");
         url = "jdbc:sqlite://" + host + ":" + port + "/" + database;
      } else {
         url = "jdbc:sqlite:" + config.getString("path", TeamsPlugin.getInstance().getDataFolder().getCanonicalPath() + "/db/sqlite.db");
      }
      String username = config.getString("username");
      String password = config.getString("password");
      connection = online && username != null? DriverManager.getConnection(url, username, password) : DriverManager.getConnection(url);
   }
   
   @Override
   public void unload() {
      super.unload();
      close(connection);
   }
}