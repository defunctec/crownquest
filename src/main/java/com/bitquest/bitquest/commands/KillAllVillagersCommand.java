package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillAllVillagersCommand extends CommandAction {
  private CrownQuest crownQuest;

  public KillAllVillagersCommand(CrownQuest plugin) {
    this.crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    crownQuest.killAllVillagers();
    return true;
  }
}
