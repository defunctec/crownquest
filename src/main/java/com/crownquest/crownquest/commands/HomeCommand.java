package com.crownquest.crownquest.commands;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class HomeCommand extends CommandAction {
  private CrownQuest crownQuest;

  public HomeCommand(CrownQuest plugin) {
    crownQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (player.getBedSpawnLocation() != null && !player.hasMetadata("teleporting")) {
      // TODO: tp player home
      player.sendMessage(ChatColor.GREEN + "Teleporting...");
      player.setMetadata("teleporting", new FixedMetadataValue(crownQuest, true));
      World world = Bukkit.getWorld("world");

      final Location spawn = player.getBedSpawnLocation();

      Chunk c = spawn.getChunk();
      if (!c.isLoaded()) {
        c.load();
      }
      crownQuest
          .getServer()
          .getScheduler()
          .scheduleSyncDelayedTask(
              crownQuest,
              new Runnable() {

                public void run() {
                  if (player.hasMetadata("teleporting")) {
                    player.teleport(spawn);
                    player.removeMetadata("teleporting", crownQuest);
                  }
                }
              },
              60L);
    }
    return true;
  }
}
