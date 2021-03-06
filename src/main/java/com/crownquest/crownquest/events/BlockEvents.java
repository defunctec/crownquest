package com.crownquest.crownquest.events;

import com.crownquest.crownquest.CrownQuest;
import java.util.List;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockEvents implements Listener {

	CrownQuest crownQuest;

	public BlockEvents(CrownQuest plugin) {

		crownQuest = plugin;
	}

	@EventHandler
	void onBlockCatchFire(BlockIgniteEvent event) {
		if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
			if (event.getPlayer() != null) {
				if (!crownQuest.canBuild(event.getBlock().getLocation(), event.getPlayer())) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
				}
			}
		} else if (event.getCause().equals(IgniteCause.SPREAD)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	void onBlockBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	void onTrample(PlayerInteractEvent event) {
		// This is related to trampling
		if (event.getAction().equals(Action.PHYSICAL)) {
			// Get the soil block
			Block soilBlock = event.getClickedBlock();
			// Check if the block is SOIL
			if (soilBlock.getType() == Material.SOIL) {
				// Check if moderator
				if (!crownQuest.isModerator(event.getPlayer())) {
					// If the player can't build there cancel it
					if (!crownQuest.canBuild(soilBlock.getLocation(), event.getPlayer())) {
						event.setCancelled(true);
					} else
						event.setCancelled(false);
				}
			}
		}
	}

	@EventHandler
	void onBlockBreak(BlockBreakEvent event) {
		// If block is bedrock, cancel the event
		Block b = event.getBlock();
		Material m = b.getType();
		if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_the_end")
				|| event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_nether")) {
			if (crownQuest.isModerator(event.getPlayer())) {
				event.setCancelled(false);
			} else {
				event.setCancelled(true);
			}
		} else if (m.equals(Material.BEDROCK) || m.equals(Material.COMMAND) || m.equals(Material.COMMAND_CHAIN)
				|| m.equals(Material.COMMAND_REPEATING)) {
			event.setCancelled(true);
			// If player is in a no-build zone, cancel the event
		} else if (!crownQuest.canBuild(b.getLocation(), event.getPlayer())) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

	@EventHandler
	void onBlockPlace(BlockPlaceEvent event) {
		// set clan
		// first, we check if the player has permission to build
		Block b = event.getBlock();
		Material m = b.getType();
		if (!crownQuest.canBuild(b.getLocation(), event.getPlayer())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not place blocks here!");
		} else if (m.equals(Material.BEDROCK) || m.equals(Material.COMMAND) || m.equals(Material.COMMAND_CHAIN)
				|| m.equals(Material.COMMAND_REPEATING)) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "Placing that block is not allowed!");
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

	@EventHandler
	void onPistonExtends(BlockPistonExtendEvent event) {
		Block piston = event.getBlock();
		List<Block> blocks = event.getBlocks();
		BlockFace direction = event.getDirection();

		String tempchunk = "";
		if (event.getBlock().getLocation().getWorld().getName().equals("world")) {
			tempchunk = "chunk";
		} // end world lmao @crownjake09
		else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")) {
			tempchunk = "netherchunk";
		} // end nether @crownjake09

		if (!blocks.isEmpty()) {
			Block lastBlock = blocks.get(blocks.size() - 1);
			Block nextBlock = lastBlock.getRelative(direction);

			Chunk pistonChunk = piston.getChunk();
			Chunk blockChunk = nextBlock.getChunk();

			String owner1, owner2;
			if ((owner2 = CrownQuest.REDIS
					.get(tempchunk + "" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
				if ((owner1 = CrownQuest.REDIS
						.get(tempchunk + "" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) != null) {
					if (!owner1.equals(owner2)) {
						event.setCancelled(true);
					}
				} else {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	void onPistonRetract(BlockPistonRetractEvent event) {
		Block piston = event.getBlock();
		BlockFace direction = event.getDirection();
		Block nextBlock = piston.getRelative(direction, -2); // Direction is
																// inverted?
		String tempchunk = "";
		if (event.getBlock().getLocation().getWorld().getName().equals("world")) {
			tempchunk = "chunk";
		} // end world lmao @crownjake09
		else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")) {
			tempchunk = "netherchunk";
		} // end nether @crownjake09

		if (event.isSticky()) {
			Chunk pistonChunk = piston.getChunk();
			Chunk blockChunk = nextBlock.getChunk();

			String owner1, owner2;
			if ((owner2 = CrownQuest.REDIS
					.get(tempchunk + "" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
				if ((owner1 = CrownQuest.REDIS
						.get(tempchunk + "" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) != null) {
					if (!owner1.equals(owner2)) {
						event.setCancelled(true);
						piston.getRelative(event.getDirection()).setType(Material.AIR);
					}
				} else {
					event.setCancelled(true);
					piston.getRelative(event.getDirection()).setType(Material.AIR);
				}
			}
		}
	}
}
