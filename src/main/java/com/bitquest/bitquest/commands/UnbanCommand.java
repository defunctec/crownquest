package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCommand extends CommandAction {
  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length == 1) {
      String playerName = args[0];
      if (CrownQuest.REDIS.exists("uuid:" + playerName)) {
        String uuid = CrownQuest.REDIS.get("uuid:" + playerName);
        CrownQuest.REDIS.srem("banlist", uuid);
        sender.sendMessage(
            ChatColor.GREEN
                + "Player "
                + ChatColor.BLUE
                + playerName
                + ChatColor.GREEN
                + " has been unbanned.");

      } else {
        player.sendMessage(ChatColor.RED + "Usage: /unban <player>");
        return true;
      }
    }
    return false;
  }
}
