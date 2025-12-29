package io.matita08.plugins.teams.commands;

import io.matita08.plugins.teams.Config;
import io.matita08.plugins.teams.TeamsPlugin;
import io.matita08.plugins.teams.data.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.matita08.plugins.teams.data.Player.getPlayer;
import static io.matita08.plugins.teams.data.Player.loadPlayer;
import static io.matita08.plugins.teams.data.Team.createTeam;

@SuppressWarnings("deprecation")//ChatColor is deprecated, idc. This stops the yellow spam
public class TeamCommand implements CommandExecutor {
   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if(args.length == 0) return help(sender);
      return switch(args[0]) {
         case "help" -> help(sender);
         case "create" -> create(sender, args);
         case "chat" ->  chat(sender);
         case "join" -> join(sender, args);
         case "spy" -> spy(sender);
         case "leave" -> leave(sender);
         case "ally" -> ally(sender, args);
         case "enemy" -> enemy(sender, args);
         case "info" -> info(sender, args);
         case "team" -> team(sender, args);
         default -> {
            sender.sendMessage(ChatColor.RED + "Comando non riconosciuto. " + ChatColor.BLUE + "/team help per informazioni");
            yield help(sender);
         }
      };
   }
   
   private static boolean help(CommandSender sender) {
      sender.sendMessage(ChatColor.GREEN + "Team Hel" + ChatColor.BLUE + "p guide:");
      sender.sendMessage(ChatColor.BLUE + "/team help" + ChatColor.RESET + " - Mostra questa guida");
      sender.sendMessage(ChatColor.BLUE + "/team create <team>" + ChatColor.RESET + " - Crea un team");
      sender.sendMessage(ChatColor.BLUE + "/team leave" + ChatColor.RESET + " - Abbandona il team corrente");
      sender.sendMessage(ChatColor.BLUE + "/team chat" + ChatColor.RESET + " - Cambia lo stato della chat team-only");
      sender.sendMessage(ChatColor.BLUE + "/team join <team>" + ChatColor.RESET + " - Entra in un team");
      sender.sendMessage(ChatColor.BLUE + "/team ally <team>" + ChatColor.RESET + " - Aggiungi/rimuovi un team agli alleati");
      sender.sendMessage(ChatColor.BLUE + "/team enemy <team>" + ChatColor.RESET + " - Aggiungi/rimuovi un team ai nemici");
      sender.sendMessage(ChatColor.BLUE + "/team info <player>" + ChatColor.RESET + " - Mostra i detttagli di un giocatore");
      sender.sendMessage(ChatColor.BLUE + "/team team <team>" + ChatColor.RESET + " - Mostra i detttagli di un team");
      if(sender.hasPermission("team.spy")) sender.sendMessage(ChatColor.YELLOW + "/team spy" + ChatColor.RESET + " - Mostra tutte le chat dei team");
      sender.sendMessage(ChatColor.GOLD + "----------------------");
      return true;
   }
   private static boolean info(CommandSender sender, String[] args) {
      if(args.length == 1) return false;
      String pname = args[1];
      
      io.matita08.plugins.teams.data.Player p = loadPlayer(Bukkit.getPlayer(pname));
      sender.sendMessage("Il giocatore " + p.getPlayer().getName() + (p.getTeam() == null ? " non è in nessun team" : "è nel team" + p.getTeam().getName()));
      
      args[0] = null;
      String name = Arrays.stream(args).filter(Objects::nonNull).map(s -> s + ' ').collect(Collectors.joining()).strip();
      
      Team team = Team.loadTeam(name);
      if(team == null) {
         sender.sendMessage(ChatColor.RED + "Team non trovato");
         return true;
      }
      
      sender.sendMessage(ChatColor.GOLD + "---------- Team info ---------");
      sender.sendMessage(ChatColor.BLUE + "Nome: " + team.getName());
      sender.sendMessage(ChatColor.GREEN + "Membri: ");
      team.getMembers().forEach(pl -> sender.sendMessage(ChatColor.YELLOW + "  - " + pl.getPlayer().name()));
      sender.sendMessage(ChatColor.GREEN + "Alleati: ");
      team.getAlly().forEach(t -> sender.sendMessage(ChatColor.DARK_AQUA + "  - " + t));
      sender.sendMessage(ChatColor.LIGHT_PURPLE + "Nemici: ");
      team.getEnemy().forEach(t -> sender.sendMessage(ChatColor.DARK_PURPLE + "  - " + t));
      sender.sendMessage(ChatColor.GOLD + "------------------------------");
      
      return true;
   }
   
   private static boolean team(CommandSender sender, String[] args) {
      return false;
   }
   
   private static boolean create(CommandSender sender, String[] args) {
      if(checkNoConsole(sender)) return true;
      Player player = (Player) sender;
      
      io.matita08.plugins.teams.data.Player pl = loadPlayer(player);
      
      if(pl.getTeam() != null) {
         sender.sendMessage(ChatColor.RED + "Sei già in un team");
         return true;
      }
      
      args[0] = null;
      String name = Arrays.stream(args).filter(Objects::nonNull).map(s -> s + ' ').collect(Collectors.joining()).strip();
      
      if(name.isBlank()) {
         sender.sendMessage(ChatColor.RED + "Per favore inserisci un nome");
         return true;
      }
      
      try {
         Config.getNameBlacklist().forEach(p ->{
            if(p.matcher(name).find()) throw new RuntimeException();
         });
      } catch (RuntimeException e) {
         sender.sendMessage(ChatColor.RED + "Nome non valido");
         TeamsPlugin.getLog().info(player.name() + "(" + player.getUniqueId() + ") has tried to create a team called " + name);
         return true;
      }
      
      createTeam(pl, name);
      sender.sendMessage(ChatColor.GREEN + "Team " + name + " creato con successo");
      
      return true;
   }
   
   private static boolean join(CommandSender sender, String[] args) {
      if(checkNoConsole(sender)) return true;
      Player player = (Player) sender;
      
      args[0] = null;
      String name = Arrays.stream(args).filter(Objects::nonNull).map(s -> s + ' ').collect(Collectors.joining()).strip();
      
      Team team = Team.loadTeam(name);
      if(team == null) {
         sender.sendMessage(ChatColor.RED + "Team non trovato");
         return true;
      }
      
      team.addMember(getPlayer(player));
      
      team.broadcast(ChatColor.GREEN + player.getName() + " si è aggiunto al team");
      
      return true;
   }
   
   private static boolean chat(CommandSender sender) {
      if(checkNoConsole(sender)) return true;
      io.matita08.plugins.teams.data.Player p = loadPlayer((Player) sender);
      if(p.getTeam() != null) {
         p.setTeamChat(!p.isTeamChat());
         sender.sendMessage(ChatColor.BLUE + "La team chat è stata " + (p.isTeamChat()? "attivata" : "disattivata"));
      } else sender.sendMessage(ChatColor.RED + "Non sei in un team");
      return true;
   }
   
   private static boolean spy(CommandSender sender) {
      if(checkNoConsole(sender)) return true;
      if(sender.hasPermission("team.spy")) {
         Player p = (Player) sender;
         if(io.matita08.plugins.teams.data.Team.spy.contains(p)) io.matita08.plugins.teams.data.Team.spy.remove(p);
         else io.matita08.plugins.teams.data.Team.spy.add(p);
         sender.sendMessage(ChatColor.GREEN + "You have toggled your spy status");
      } else {
         sender.sendMessage(ChatColor.RED + "Comando non riconosciuto");
         sender.sendMessage(ChatColor.YELLOW + "/team help per i comandi disponibili");
      }
      return true;
   }
   
   private static boolean leave(CommandSender sender)  {
      if(checkNoConsole(sender)) return true;
      io.matita08.plugins.teams.data.Player p = loadPlayer((Player) sender);
      if(p.getTeam() == null) {
         sender.sendMessage(ChatColor.RED + "Non sei in un team");
         return true;
      }
      
      p.getTeam().removeMember(p);
      sender.sendMessage(ChatColor.DARK_GREEN + "Hai lasciato il team");
      return true;
   }
   
   private static boolean ally(CommandSender sender, String[] args)  {
      if(checkNoConsole(sender)) return true;
      io.matita08.plugins.teams.data.Player p = loadPlayer((Player) sender);
      if(p.getTeam() == null) {
         sender.sendMessage(ChatColor.RED + "Non sei in un team");
         return true;
      }
      
      args[0] = null;
      String name = Arrays.stream(args).filter(Objects::nonNull).map(s -> s + ' ').collect(Collectors.joining()).strip();
      
      if(name.isBlank()) {
         sender.sendMessage(ChatColor.RED + "Per favore inserisci un nome");
         return true;
      }
      
      Team other = Team.loadTeam(name);
      if(other == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Team non trovato");
         return true;
      }
      
      if(p.getTeam().equals(other)) {
         sender.sendMessage(ChatColor.DARK_RED + "Impossibile allearsi con se stessi");
         return true;
      }
      
      if(p.getTeam().isEnemy(name)) {
         sender.sendMessage(ChatColor.DARK_RED + "Impossibile allearsi con un nemico");
         return true;
      }
      
      if(p.getTeam().isAlly(name)) {
         p.getTeam().removeAlly(name);
         p.getTeam().broadcast(ChatColor.GRAY + "Ora non siete più alleati di " + name);
      } else {
         p.getTeam().addAlly(other.getName());
         p.getTeam().broadcast(ChatColor.GOLD + "Ora siete alleati con " + name);
      }
      
      return true;
   }
   
   private static boolean enemy(CommandSender sender, String[] args)  {
      if(checkNoConsole(sender)) return true;
      io.matita08.plugins.teams.data.Player p = loadPlayer((Player) sender);
      if(p.getTeam() == null) {
         sender.sendMessage(ChatColor.RED + "Non sei in un team");
         return true;
      }
      
      args[0] = null;
      String name = Arrays.stream(args).filter(Objects::nonNull).map(s -> s + ' ').collect(Collectors.joining()).strip();
      
      if(name.isBlank()) {
         sender.sendMessage(ChatColor.RED + "Per favore inserisci un nome");
         return true;
      }
      
      Team other = Team.loadTeam(name);
      if(other == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Team non trovato");
         return true;
      }
      
      if(p.getTeam().equals(other)) {
         sender.sendMessage(ChatColor.DARK_RED + "Impossibile diventare nemici di se stessi");
         return true;
      }
      
      if(p.getTeam().isAlly(name)) {
         sender.sendMessage(ChatColor.DARK_RED + "Impossibile diventare nemici di un'alleato");
         return true;
      }
      
      if(p.getTeam().isEnemy(name)) {
         p.getTeam().removeEnemy(name);
         p.getTeam().broadcast(ChatColor.GRAY + "Ora non siete più nemici di " + name);
      } else {
         p.getTeam().addEnemy(other.getName());
         p.getTeam().broadcast(ChatColor.DARK_PURPLE + "Ora siete nemici di " + name);
      }
      return true;
   }
   
   public static boolean checkNoConsole(CommandSender sender){
      if(sender instanceof Player) return false;
      sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
      return true;
   }
}
