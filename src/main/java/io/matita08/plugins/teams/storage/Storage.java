package io.matita08.plugins.teams.storage;

import io.matita08.plugins.teams.data.Player;
import io.matita08.plugins.teams.data.Team;
import io.matita08.plugins.teams.services.Service;
import org.bukkit.configuration.ConfigurationSection;

public interface Storage extends Service {
   void init(ConfigurationSection config);
   Player loadPlayer(String id);
   Team loadTeam(String name);
   void savePlayer(Player player);
   void saveTeam(Team team);
   boolean teamExists(String name);
   void deleteTeam(String name);
}
