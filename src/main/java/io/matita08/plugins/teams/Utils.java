package io.matita08.plugins.teams;

import lombok.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class Utils {
   private static final Method playerGetName;
   
   static {
      Method getNameMethod = null;
      Method[] methods = Player.class.getDeclaredMethods();
      for(Method method : methods) {
         if(method.getName().toLowerCase().contains("name")) {
            getNameMethod = method;
            break;
         }
      }
      playerGetName = getNameMethod;
   }
   
   @SneakyThrows
   public static String getPlayerName(Player player) {
      return playerGetName.invoke(player).toString();
   }
}
