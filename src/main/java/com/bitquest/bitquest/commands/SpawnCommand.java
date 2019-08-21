package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandAction {
  private CrownQuest crownQuest;

  public SpawnCommand(CrownQuest plugin) {
    this.crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    crownQuest.teleportToSpawn(player);
    return true;
  }
}
