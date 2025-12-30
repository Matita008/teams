package io.matita08.plugins.teams;

import io.matita08.plugins.teams.storage.StorageManager;
import lombok.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Config {
   @Getter private static String tablePrefix;
   @Getter private static List<Pattern> nameBlacklist;
   @Getter private static int maxPlayer;
   
   public static void init(YamlConfiguration config) {
      tablePrefix = config.getString("db.prefix", "teams_");
      StorageManager.init(config.getConfigurationSection("db"));
      
      List<String> nameBlacklistStrings = config.getStringList("name.block");
      nameBlacklist = new ArrayList<>();
      nameBlacklistStrings.forEach(s -> {
         try {
            nameBlacklist.add(Pattern.compile(s));
         } catch (PatternSyntaxException e) {
            TeamsPlugin.getLog().warning("Invalid blacklist pattern found: " + s + ", skipping...");
         }
      });
      
      maxPlayer = Math.min(config.getInt("teams.max-player", 20), 2);
   }
}
