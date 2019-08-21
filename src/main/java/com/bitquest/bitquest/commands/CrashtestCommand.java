package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrashtestCommand extends CommandAction {
  private CrownQuest crownQuest;

  public CrashtestCommand(CrownQuest plugin) {
    this.crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    crownQuest.crashtest();
    return true;
  }
}
