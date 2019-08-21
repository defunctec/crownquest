package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import com.crownquest.crownquest.LegacyWallet;
import com.crownquest.crownquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpgradeWallet extends CommandAction {
  private CrownQuest crownQuest;

  public UpgradeWallet(CrownQuest plugin) {
    crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    LegacyWallet legacyWallet = new LegacyWallet(player.getUniqueId().toString());
    try {
      User user = new User(crownQuest.db_con, player.getUniqueId());
      LegacyWallet legacy_wallet = new LegacyWallet(player.getUniqueId().toString());

      Long balance = legacy_wallet.getBalance(2);
      if (balance > 0) {
        legacy_wallet.sendFrom(user.wallet.address, balance);
      } else {
        player.sendMessage(
            ChatColor.RED + "You don't have balance in your old account or its already migrated.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(
          ChatColor.RED + "Command failed. This incident was logged. Please try again later.");
    }

    return true;
  }
}
