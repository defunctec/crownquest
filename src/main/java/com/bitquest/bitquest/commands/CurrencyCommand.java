package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CurrencyCommand extends CommandAction {
  private CrownQuest crownQuest;

  public CurrencyCommand(CrownQuest plugin) {
    crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    // CHANGE CURRENCY BY CROWNJAKE09
    if (cmd.getName().equalsIgnoreCase("currency")) {

      if ((args[0].equalsIgnoreCase("ems"))
          || (args[0].equalsIgnoreCase("emerald"))
          || (args[0].equalsIgnoreCase("emeralds"))) {
        CrownQuest.REDIS.set("currency" + player.getUniqueId().toString(), "emerald");
        player.sendMessage(
            ChatColor.GREEN
                + "Currency changed to "
                + CrownQuest.REDIS.get("currency" + player.getUniqueId().toString()));

      } else if (args[0].equalsIgnoreCase(CrownQuest.DENOMINATION_NAME)) {
        CrownQuest.REDIS.set(
            "currency" + player.getUniqueId().toString(), CrownQuest.DENOMINATION_NAME);
        player.sendMessage(
            ChatColor.GREEN
                + "Currency changed to "
                + CrownQuest.REDIS.get("currency" + player.getUniqueId().toString()));

      } else {
        player.sendMessage(ChatColor.RED + "There was a problem changing your currency.");
      }
      try {
        crownQuest.updateScoreboard(player);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    }
    return false;
  }
}
