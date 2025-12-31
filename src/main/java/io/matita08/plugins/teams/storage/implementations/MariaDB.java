package io.matita08.plugins.teams.storage.implementations;

import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;

public class MariaDB extends Database {
   @Getter private Connection connection;
   
   @Override
   @SneakyThrows
   public void init(ConfigurationSection config) {
      Class.forName("org.mariadb.jdbc.Driver"); //needed to make sure the driver is loaded
      String host = config.getString("host", "localhost");
      int port = config.getInt("port", 3306);
      if(port <= 0) port = 3306;
      String database = config.getString("database", "teams");
      String username = config.getString("username", "root");
      String password = config.getString("password", "");
      
      String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
      connection = DriverManager.getConnection(url, username, password);
   }
   
   @Override
   public void unload() {
      super.unload();
      close(connection);
   }
}