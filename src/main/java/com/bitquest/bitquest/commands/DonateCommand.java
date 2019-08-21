package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import com.crownquest.crownquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DonateCommand extends CommandAction {
  private CrownQuest crownQuest;

  public DonateCommand(CrownQuest plugin) {
    crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    if (args.length == 1) {
      if (System.getenv("DONATION_ADDRESS") != null) {
        try {
          final Long crws = Long.parseLong(args[0]);
          final Long sat = crws * crownQuest.DENOMINATION_FACTOR;
          final User user = new User(crownQuest.db_con, player.getUniqueId());
          final Long balance = user.wallet.getBalance(0);

            if (user.wallet.payment(System.getenv("DONATION_ADDRESS"), sat)) {
              player.sendMessage(ChatColor.GREEN + "Thanks for your support!");
              crownQuest.updateScoreboard(player);
            } else {
              player.sendMessage(ChatColor.RED + "Donation failed");
            }


          return true;
        } catch (Exception e) {
          System.out.println(e);
          player.sendMessage(ChatColor.RED + "Command failed.");
          return true;
        }
      } else {
        player.sendMessage(ChatColor.RED + "Donations are disabled in this server");
        return true;
      }

    } else {
      return false;
    }
  }
}
