package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import com.crownquest.crownquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletCommand extends CommandAction {
  private CrownQuest crownQuest;

  public WalletCommand(CrownQuest plugin) {
    crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {

    try {
      User user = new User(crownQuest.db_con, player.getUniqueId());
      crownQuest.sendWalletInfo(player, user);
      crownQuest.updateScoreboard(player);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }

    return true;
  }
}
