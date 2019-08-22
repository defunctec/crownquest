package com.crownquest.crownquest.events;

import com.crownquest.crownquest.CrownQuest;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerEvents implements Listener {
  CrownQuest crownQuest;

  public ServerEvents(CrownQuest plugin) {
    crownQuest = plugin;
  }

  @EventHandler
  public void onServerListPing(ServerListPingEvent event) {

    event.setMotd(
        ChatColor.GOLD
            + ChatColor.BOLD.toString()
            + CrownQuest.SERVERDISPLAY_NAME
            + ChatColor.GRAY
            + ChatColor.BOLD.toString()
            + "Quest"
            + ChatColor.RESET
            + " - The server that runs on "
            + CrownQuest.DENOMINATION_NAME);
  }
}
