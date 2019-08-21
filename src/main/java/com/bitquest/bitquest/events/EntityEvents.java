package com.crownquest.crownquest.events;

import com.crownquest.crownquest.CrownQuest;
import com.crownquest.crownquest.LegacyWallet;
import com.crownquest.crownquest.User;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class EntityEvents implements Listener {
  CrownQuest crownQuest;
  StringBuilder rawwelcome = new StringBuilder();
  String PROBLEM_MESSAGE = "Can't join right now. Come back later";

  private static final List<Material> PROTECTED_BLOCKS =
      Arrays.asList(
          Material.CHEST,
          Material.ACACIA_DOOR,
          Material.BIRCH_DOOR,
          Material.DARK_OAK_DOOR,
          Material.JUNGLE_DOOR,
          Material.SPRUCE_DOOR,
          Material.WOOD_DOOR,
          Material.WOODEN_DOOR,
          Material.FURNACE,
          Material.BURNING_FURNACE,
          Material.ACACIA_FENCE_GATE,
          Material.BIRCH_FENCE_GATE,
          Material.DARK_OAK_FENCE_GATE,
          Material.FENCE_GATE,
          Material.JUNGLE_FENCE_GATE,
          Material.SPRUCE_FENCE_GATE,
          Material.DISPENSER,
          Material.DROPPER,
          Material.BREWING_STAND,
          Material.BLACK_SHULKER_BOX,
          Material.BLUE_SHULKER_BOX,
          Material.BROWN_SHULKER_BOX,
          Material.CYAN_SHULKER_BOX,
          Material.GRAY_SHULKER_BOX,
          Material.GREEN_SHULKER_BOX,
          Material.LIGHT_BLUE_SHULKER_BOX,
          Material.LIME_SHULKER_BOX,
          Material.MAGENTA_SHULKER_BOX,
          Material.ORANGE_SHULKER_BOX,
          Material.PINK_SHULKER_BOX,
          Material.PURPLE_SHULKER_BOX,
          Material.RED_SHULKER_BOX,
          Material.SILVER_SHULKER_BOX,
          Material.WHITE_SHULKER_BOX,
          Material.YELLOW_SHULKER_BOX);

  private static final List<EntityType> PROTECTED_ENTITIES =
      Arrays.asList(
          EntityType.ARMOR_STAND,
          EntityType.ITEM_FRAME,
          EntityType.PAINTING,
          EntityType.ENDER_CRYSTAL);

  private int pvar = 0; // pvp area variable @crownjake09

  public EntityEvents(CrownQuest plugin) {
    crownQuest = plugin;

    for (String line : crownQuest.getConfig().getStringList("welcomeMessage")) {
      for (ChatColor color : ChatColor.values()) {
        line = line.replaceAll("<" + color.name() + ">", color.toString());
      }
      // add links
      final Pattern pattern = Pattern.compile("<link>(.+?)</link>");
      final Matcher matcher = pattern.matcher(line);
      matcher.find();
      String link = matcher.group(1);
      // Right here we need to replace the link variable with a minecraft-compatible link
      line = line.replaceAll("<link>" + link + "<link>", link);

      rawwelcome.append(line);
    }
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    try {
      Player player = event.getPlayer();
      final User user = new User(crownQuest.db_con, player.getUniqueId());
      CrownQuest.REDIS.set("name:" + player.getUniqueId().toString(), player.getName());
      CrownQuest.REDIS.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
      if (CrownQuest.REDIS.sismember("banlist", event.getPlayer().getUniqueId().toString())) {
        System.out.println("kicking banned player " + event.getPlayer().getDisplayName());
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            "You are temporarily banned. Please contact crownquest@crownquest.co");
      }
      if (CrownQuest.REDIS.exists("rate_limit:" + event.getPlayer().getUniqueId()) == true) {
        Long ttl = CrownQuest.REDIS.ttl("rate_limit:" + event.getPlayer().getUniqueId());
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER, "Please try again in " + ttl + " seconds.");
      }

    } catch (Exception e) {
      e.printStackTrace();
      CrownQuest.REDIS.set("rate_limit:" + event.getPlayer().getUniqueId(), "1");
      CrownQuest.REDIS.expire("rate_limit:" + event.getPlayer().getUniqueId(), 60);
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          "The server is in limited capacity at this moment. Please try again later.");
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    final Player player = event.getPlayer();
    // On dev environment, admin gets op. In production, nobody gets op.

    player.setGameMode(GameMode.SURVIVAL);
    crownQuest.setTotalExperience(player);
    final String ip = player.getAddress().toString().split("/")[1].split(":")[0];
    System.out.println("User " + player.getName() + "logged in with IP " + ip);
    CrownQuest.REDIS.set("ip" + player.getUniqueId().toString(), ip);
    CrownQuest.REDIS.set("displayname:" + player.getUniqueId().toString(), player.getDisplayName());
    CrownQuest.REDIS.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
    CrownQuest.REDIS.set("rate_limit:" + event.getPlayer().getUniqueId(), "1");
    CrownQuest.REDIS.expire("rate_limit:" + event.getPlayer().getUniqueId(), 60);
    if (crownQuest.CROWNQUEST_ENV.equals("development") == true && crownQuest.ADMIN_UUID == null) {
      player.setOp(true);
    }
    if (crownQuest.isModerator(player)) {

      player.sendMessage(ChatColor.GREEN + "You are a moderator on this server.");

      String url = "https://live.blockcypher.com/crw-testnet/address/" + crownQuest.wallet.address;
      if (crownQuest.BLOCKCYPHER_CHAIN == "crw/main")
        url = "https://live.blockcypher.com/crw/address/" + crownQuest.wallet.address;
      if (crownQuest.BLOCKCYPHER_CHAIN == "doge/main")
        url = "https://live.blockcypher.com/doge/address/" + crownQuest.wallet.address;
      player.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.UNDERLINE + url);
    }

    String welcome = rawwelcome.toString();
    welcome = welcome.replace("<name>", player.getName());
    player.sendMessage(welcome);
    if (CrownQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
      String clan = CrownQuest.REDIS.get("clan:" + player.getUniqueId().toString());
      player.setPlayerListName(
          ChatColor.GOLD + "[" + clan + "] " + ChatColor.WHITE + player.getName());
      if (crownQuest.isModerator(player)) {
        player.setPlayerListName(
            ChatColor.RED
                + "[MOD]"
                + ChatColor.GOLD
                + "["
                + clan
                + "] "
                + ChatColor.WHITE
                + player.getName());
      }
    } else if ((!CrownQuest.REDIS.exists("clan:" + player.getUniqueId().toString()))
        && (crownQuest.isModerator(player))) {
      player.setPlayerListName(ChatColor.RED + "[MOD]" + ChatColor.WHITE + player.getName());
    }

    // Prints the user balance
    crownQuest.setTotalExperience(player);

    player.sendMessage(ChatColor.YELLOW + "     Welcome to " + crownQuest.SERVER_NAME + "! ");
    if (CrownQuest.REDIS.exists("crownquest:motd") == true) player.sendMessage(CrownQuest.REDIS.get("crownquest:motd"));
    try {
      player.sendMessage(
              "The loot pool is: "
                      + (int)
                      (crownQuest.wallet.getBalance(1)/crownQuest.DENOMINATION_FACTOR)
                      + " "
                      + crownQuest.DENOMINATION_NAME);
    } catch(Exception e) {
      e.printStackTrace();
    }


    CrownQuest.REDIS.zincrby("player:login", 1, player.getUniqueId().toString());
    // spawn pet
    if (CrownQuest.REDIS.exists("pet:" + player.getUniqueId().toString())) {
      crownQuest.spawnPet(player);
    }
    // check if user has a legacy wallet
    LegacyWallet legacyWallet = new LegacyWallet(player.getUniqueId().toString());
    if (legacyWallet.getBalance(5) > 0) {
      player.sendMessage(
          ChatColor.RED
              + "You have "
              + legacyWallet.getBalance(5)
              + " SAT in your old wallet. Use the /upgradewallet command to send them to your new one.");
    }
    crownQuest.updateScoreboard(player);
  }

  @EventHandler
  public void onExperienceChange(PlayerExpChangeEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    event.setAmount(0);
  }

  @EventHandler
  public void onEnchantItemEvent(EnchantItemEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    // Simply setting the cost to zero does not work. there are probably
    // checks downstream for this. Instead cancel out the cost.
    // None of this actually changes the crownquest xp anyway, so just make
    // things look correct for the user. This only works for the enchantment table,
    // not the anvil.
    event.getEnchanter().setLevel(event.getEnchanter().getLevel() + event.whichButton() + 1);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    // TODO: Check if zone is PvP only when chunks change
    // if ((crownQuest.isPvP(event.getPlayer().getLocation()) == true) && (pvar == 0)) {
    //     event.getPlayer().sendMessage(ChatColor.RED + "IN PVP ZONE");
    //     pvar++;
    // }

    if (event.getFrom().getChunk() != event.getTo().getChunk()) {
      event
          .getPlayer()
          .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false));

      pvar = 0;
      if (event.getFrom().getWorld().getName().endsWith("_end") == false
          && event.getFrom().getWorld().getName().endsWith("_nether") == false) {
        // announce new area
        String chunkname = "";
        if (event.getPlayer().getWorld().getName().equals("world")) {
          chunkname = "chunk";
        } else if (event.getPlayer().getWorld().getName().equals("world_nether")) {
          chunkname = "netherchunk";
        }

        int x1 = event.getFrom().getChunk().getX();
        int z1 = event.getFrom().getChunk().getZ();

        int x2 = event.getTo().getChunk().getX();
        int z2 = event.getTo().getChunk().getZ();
        String name1 = "the wilderness";
        String name2 = "the wilderness";
        String key1 = chunkname + "" + x1 + "," + z1 + "name";
        String key2 = chunkname + "" + x2 + "," + z2 + "name";
        if (crownQuest.landIsClaimed(event.getFrom())) {
          if (crownQuest.land_name_cache.containsKey(key1)) {
            name1 = crownQuest.land_name_cache.get(key1);
          } else {
            name1 = CrownQuest.REDIS.get(key1) != null ? CrownQuest.REDIS.get(key1) : "the wilderness";
            crownQuest.land_name_cache.put(key1, name1);
          }
        }
        if (crownQuest.landIsClaimed(event.getTo())) {
          name2 = CrownQuest.REDIS.get(key2) != null ? CrownQuest.REDIS.get(key2) : "the wilderness";
        }
        event.getPlayer().setGameMode(GameMode.SURVIVAL);


        if (!name1.equals(name2)) {

          if (name2.equals("the wilderness")) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[ " + name2 + " ]");
          } else {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "[ " + name2 + " ]");
          }
        }
      } else {
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
      }
    }
  }

  @EventHandler
  public void itemConsume(PlayerItemConsumeEvent event) {
    ItemStack item = event.getItem();
    if (item != null && item.hasItemMeta()) {
      if (item.getItemMeta() instanceof PotionMeta) {
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        PotionData potionData = potionMeta.getBasePotionData();
        if (potionData.getType() == PotionType.WATER) {
          Player player = event.getPlayer();
          if (player != null) {
            PlayerInventory inventory = player.getInventory();
            ItemStack helmet = inventory.getHelmet();
            if (helmet != null && helmet.getType() == Material.PUMPKIN) {
              Map<Enchantment, Integer> enchantments = helmet.getEnchantments();
              for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getKey().equals(Enchantment.BINDING_CURSE)) {
                  inventory.setHelmet(null);
                  player.getWorld().dropItemNaturally(player.getLocation(), helmet);
                  player.sendMessage(
                      "You are finally free of the "
                          + ChatColor.BOLD
                          + ChatColor.GOLD
                          + "Pumpkin "
                          + ChatColor.GRAY
                          + ChatColor.ITALIC
                          + "curse");
                }
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setKeepInventory(true);
    event.setKeepLevel(true);
    event.setDeathMessage(null);
  }

  @EventHandler
  void onEntityDeath(EntityDeathEvent e)
      throws IOException, ParseException, org.json.simple.parser.ParseException, SQLException {
    final LivingEntity entity = e.getEntity();

    final int level = (new Double(entity.getMaxHealth()).intValue()) - 1;

    if (entity instanceof Monster && e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
        final EntityDamageByEntityEvent damage =
            (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
        Player player=null;
        if(damage.getDamager() instanceof Player) player=(Player) damage.getDamager();
        if(damage.getDamager() instanceof Arrow && ((Arrow)damage.getDamager()).getShooter() instanceof  Player) player=(Player)((Arrow)damage.getDamager()).getShooter();
        if (player!=null) {

          Long money = crownQuest.LAND_PRICE;

          // Add EXP
          int exp = level * 4;
          crownQuest.REDIS.incrBy("experience.raw." + player.getUniqueId().toString(), exp);
          crownQuest.setTotalExperience(player);
          if (damage.getEntity() instanceof Wither) {

              final User user = new User(crownQuest.db_con, player.getUniqueId());

              if (crownQuest.wallet.payment(user.wallet.address, money)) {
                System.out.println("[loot] " + player.getDisplayName() + ": " + money);
                crownQuest.sendDiscordMessage(player.getDisplayName()+" killed "+damage.getEntity().getName()+" !!! A reward was paid: "+money/crownQuest.DENOMINATION_FACTOR+" "+crownQuest.DENOMINATION_NAME);
                crownQuest.announce(
                    ChatColor.GREEN
                        + player.getDisplayName()
                        + " got "
                        + ChatColor.BOLD
                        + money / crownQuest.DENOMINATION_FACTOR
                        + ChatColor.GREEN
                        + " "
                        + crownQuest.DENOMINATION_NAME
                        + " of loot!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 20, 1);
              }

          }
        }
    } else {
      e.setDroppedExp(0);
    }
  }

  String spawnKey(Location location) {
    return location.getWorld().getName()
        + location.getChunk().getX()
        + ","
        + location.getChunk().getZ()
        + "spawn";
  }

  // TODO: Right now, entity spawns are cancelled, then replaced with random mob spawns. Perhaps it
  // would be better to
  //          find a way to instead set the EntityType of the event. Is there any way to do that?
  // TODO: Magma Cubes don't get levels or custom names for some reason...
  @EventHandler
  void onEntitySpawn(org.bukkit.event.entity.CreatureSpawnEvent e) {
    // e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.GHAST);

    Chunk chunk = e.getLocation().getChunk();

    LivingEntity entity = e.getEntity();
    int maxlevel = 10;
    int minlevel = 1;
    int difficulty=10;

    if (e.getLocation().getWorld().getName().equals("world_nether")) {
      minlevel = 10;
      maxlevel = 50;
    } else if (e.getLocation().getWorld().getName().equals("world_end")) {
      minlevel = 50;
      maxlevel = 100;
    }
    int spawn_distance =
        (int) e.getLocation().getWorld().getSpawnLocation().distance(e.getLocation());

    EntityType entityType = entity.getType();
    // max level is 128
    int level = Math.min(maxlevel,CrownQuest.rand(minlevel, minlevel+(spawn_distance/1000)));

    if (entity instanceof  Giant) {
        entity.setMaxHealth(2858519);
        entity.setCustomName("Giant Terry");
    } else if (entity instanceof Monster) {
      crownQuest.createBossFight(e.getEntity().getLocation());

      // Disable mob spawners. Keep mob farmers away
      if (e.getSpawnReason() == SpawnReason.SPAWNER||spawn_distance<64) {
        e.setCancelled(true);
      } else {
        try {

          e.setCancelled(false);

          if (level < 1) level = 1;

          entity.setMetadata("level", new FixedMetadataValue(crownQuest, level));
          entity.setCustomName(
              String.format(
                  "%s lvl %d",
                  WordUtils.capitalizeFully(entityType.name().replace("_", " ")), level));
          if(entity instanceof Wither) {
            level=level+10;
            entity.setCustomName("Wither (Reward: "+Math.round((crownQuest.LAND_PRICE)/crownQuest.DENOMINATION_FACTOR)+" "+crownQuest.DENOMINATION_NAME+")");
          }
          entity.setMaxHealth(1 + level);

          entity.setHealth(1 + level);

          // add potion effects
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2), true);
          if (crownQuest.rand(1, 100) < level)
            entity.addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);


          // give random equipment
          if (entity instanceof Zombie
              || entity instanceof PigZombie
              || entity instanceof Skeleton) {
            useRandomEquipment(entity, level);
          }

          // some creepers are charged
          if (entity instanceof Creeper && CrownQuest.rand(0, 100) < level) {
            ((Creeper) entity).setPowered(true);
          }

          // pigzombies are always angry
          if (entity instanceof PigZombie) {
            PigZombie pigZombie = (PigZombie) entity;
            pigZombie.setAngry(true);
            pigZombie.setAngry(true);
          }

          // some skeletons are black
          if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) entity;
            ItemStack bow = new ItemStack(Material.BOW);
            randomEnchantItem(bow, level);
          }
          if (CrownQuest.CROWNQUEST_ENV.equals("development"))
            System.out.println(
                "[spawn mob] "
                    + entityType.name()
                    + " lvl "
                    + level
                    + " spawn distance: "
                    + spawn_distance);
          if (crownQuest.rand(1, 100) == 20 && crownQuest.spookyMode == true) {
            e.getLocation()
                .getWorld()
                .spawnEntity(
                    new Location(
                        e.getLocation().getWorld(),
                        e.getLocation().getX(),
                        80,
                        e.getLocation().getZ()),
                    EntityType.GHAST);
            e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.WITCH);
            e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.VILLAGER);
          }

        } catch (Exception e1) {
          System.out.println("Event failed. Shutting down...");
          e1.printStackTrace();
          Bukkit.shutdown();
        }
      }
    } else if (entity instanceof Ghast) {
      entity.setMaxHealth(level * 4);
      System.out.println(
          "[spawn ghast] "
              + entityType.name()
              + " lvl "
              + level
              + " spawn distance: "
              + spawn_distance
              + " maxhealth: "
              + entity.getMaxHealth());

    } else {
      e.setCancelled(false);
    }
  }

  @EventHandler
  void onEntityDamage(EntityDamageEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

    // damage by entity
    if (event instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
      Entity damager = damageEvent.getDamager();
      if (damager instanceof Player || (damager instanceof Arrow && ((Arrow) damager).getShooter() instanceof Player)) {
        // player damage
        Player player;

        if (damager instanceof Arrow) {
          Arrow arrow = (Arrow) damager;
          player = (Player) arrow.getShooter();
        } else {
          player = (Player) damager;
        }

        // Player vs. Protected entities
        if (PROTECTED_ENTITIES.contains(event.getEntity().getType())) {
          if (!crownQuest.canBuild(event.getEntity().getLocation(), player)) {
            event.setCancelled(true);
          }
        }

        // Player vs. Giant
          if (event.getEntity() instanceof Giant) {
              Vector v = damager.getLocation().toVector().subtract(event.getEntity().getLocation().toVector()).normalize();
              event.getEntity().setVelocity(v);

              event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(),EntityType.ZOMBIE);
              ((Giant) event.getEntity()).setTarget((Player)damager);
          }
        // Player vs. Animal in claimed location
        if (event.getEntity() instanceof Animals) {
          if (!crownQuest.canBuild(event.getEntity().getLocation(), player)) {
            event.setCancelled(true);
          }
        }
        // Player vs. Villager
        if (!crownQuest.isModerator(player) && event.getEntity() instanceof Villager) {
          event.setCancelled(true);

        } else if (event.getEntity() instanceof Player) {
          // PvP is off in overworld and nether

          if (event.getEntity().getWorld().getName().endsWith("_end")) {
            event.setCancelled(false);
          } else {
            event.setCancelled(true);
          }
        }
      } else {
        if (damageEvent.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
          Projectile p = (Projectile) damageEvent.getDamager(); // Cast projectile to
          if (p.getShooter() instanceof Ghast) {
            damageEvent.setDamage(200.0f);
          }
        }
      }
    }
  }

  public void useRandomEquipment(LivingEntity entity, int level) {

    // Gives random SWORD
    if (!(entity instanceof Skeleton)) {
      Material sword_material = null;
      if (CrownQuest.rand(0, 2) < level) sword_material = Material.WOOD_AXE;
      if (CrownQuest.rand(0, 4) < level) sword_material = Material.GOLD_AXE;
      if (CrownQuest.rand(0, 8) < level) sword_material = Material.IRON_AXE;
      if (CrownQuest.rand(0, 16) < level) sword_material = Material.DIAMOND_AXE;
      if (CrownQuest.rand(0, 32) < level) sword_material = Material.WOOD_SWORD;
      if (CrownQuest.rand(0, 64) < level) sword_material = Material.GOLD_SWORD;
      if (CrownQuest.rand(0, 128) < level) sword_material = Material.IRON_SWORD;
      if (CrownQuest.rand(0, 256) < level) sword_material = Material.DIAMOND_SWORD;
      if (sword_material != null) {
        ItemStack sword = new ItemStack(sword_material);
        randomEnchantItem(sword, level);

        entity.getEquipment().setItemInHand(sword);
      }
    }

    // Gives random HELMET
    Material helmet_material = null;

    if (CrownQuest.rand(0, 32) < level) helmet_material = Material.LEATHER_HELMET;

    if (CrownQuest.rand(0, 64) < level) helmet_material = Material.CHAINMAIL_HELMET;
    if (CrownQuest.rand(0, 128) < level) helmet_material = Material.IRON_HELMET;
    if (CrownQuest.rand(0, 256) < level) helmet_material = Material.DIAMOND_HELMET;
    if (helmet_material != null) {
      ItemStack helmet = new ItemStack(helmet_material);

      randomEnchantItem(helmet, level);

      entity.getEquipment().setHelmet(helmet);
    }

    // Gives random CHESTPLATE
    Material chestplate_material = null;
    if (CrownQuest.rand(0, 32) < level) chestplate_material = Material.LEATHER_CHESTPLATE;
    if (CrownQuest.rand(0, 64) < level) chestplate_material = Material.CHAINMAIL_CHESTPLATE;
    if (CrownQuest.rand(0, 128) < level) chestplate_material = Material.IRON_CHESTPLATE;
    if (CrownQuest.rand(0, 256) < level) chestplate_material = Material.DIAMOND_CHESTPLATE;

    if (chestplate_material != null) {
      ItemStack chest = new ItemStack(chestplate_material);
      randomEnchantItem(chest, level);

      entity.getEquipment().setChestplate(chest);
    }

    // Gives random Leggings
    Material leggings_material = null;
    if (CrownQuest.rand(0, 32) < level) leggings_material = Material.LEATHER_LEGGINGS;
    if (CrownQuest.rand(0, 64) < level) leggings_material = Material.CHAINMAIL_LEGGINGS;
    if (CrownQuest.rand(0, 128) < level) leggings_material = Material.IRON_LEGGINGS;
    if (CrownQuest.rand(0, 256) < level) leggings_material = Material.DIAMOND_LEGGINGS;
    if (leggings_material != null) {
      ItemStack leggings = new ItemStack(leggings_material);

      randomEnchantItem(leggings, level);

      entity.getEquipment().setLeggings(leggings);
    }

    // Gives Random BOOTS
    Material boot_material = null;
    if (CrownQuest.rand(0, 32) < level) boot_material = Material.LEATHER_BOOTS;

    if (CrownQuest.rand(0, 64) < level) boot_material = Material.CHAINMAIL_BOOTS;
    if (CrownQuest.rand(0, 128) < level) boot_material = Material.IRON_BOOTS;
    if (CrownQuest.rand(0, 256) < level) boot_material = Material.DIAMOND_BOOTS;
    if (boot_material != null) {
      ItemStack boots = new ItemStack(boot_material);

      randomEnchantItem(boots, level);

      entity.getEquipment().setBoots(boots);
    }
  }

  // Random Enchantment
  public static void randomEnchantItem(ItemStack item, int level) {
    ItemMeta meta = item.getItemMeta();
    Enchantment enchantment = null;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.ARROW_FIRE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.ARROW_DAMAGE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.ARROW_INFINITE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.ARROW_KNOCKBACK;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DAMAGE_ARTHROPODS;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DAMAGE_UNDEAD;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DAMAGE_ALL;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DIG_SPEED;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DURABILITY;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.FIRE_ASPECT;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.KNOCKBACK;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.LOOT_BONUS_BLOCKS;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.LOOT_BONUS_MOBS;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.LUCK;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.LURE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.OXYGEN;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.PROTECTION_EXPLOSIONS;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.PROTECTION_FALL;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.PROTECTION_PROJECTILE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.PROTECTION_FIRE;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.SILK_TOUCH;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.THORNS;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.WATER_WORKER;
    if (CrownQuest.rand(0, 128) < level) enchantment = Enchantment.DEPTH_STRIDER;

    if (enchantment != null) {
      meta.addEnchant(
          enchantment, CrownQuest.rand(enchantment.getStartLevel(), enchantment.getMaxLevel()), true);
      item.setItemMeta(meta);
    }
  }

  @EventHandler
  public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
    Player player = event.getPlayer();
    ArmorStand stand = event.getRightClicked();

    if (!crownQuest.canBuild(stand.getLocation(), player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Entity entity = event.getRightClicked();

    if (PROTECTED_ENTITIES.contains(entity.getType())) {
      if (!crownQuest.canBuild(entity.getLocation(), player)) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

    Block b = event.getClickedBlock();
    Player p = event.getPlayer();
    if (b != null && PROTECTED_BLOCKS.contains(b.getType())) {
      // If block's inventory has "public" in it, allow the player to interact with it.
      if (b.getState() instanceof InventoryHolder) {
        Inventory blockInventory = ((InventoryHolder) b.getState()).getInventory();
        if (blockInventory.getName().toLowerCase().contains("public")) {
          return;
        }
      }
      // If player doesn't have permission, disallow the player to interact with it.
      if (!crownQuest.canBuild(b.getLocation(), event.getPlayer())) {
        event.setCancelled(true);
        p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      }
    }
  }

  @EventHandler
  void onPlayerBucketFill(PlayerBucketFillEvent event) {
    Player p = event.getPlayer();
    if (!crownQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
      p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    if (crownQuest.REDIS.exists("pet:" + event.getPlayer().getUniqueId())) {
      crownQuest.spawnPet(event.getPlayer());
    }
  }

  @EventHandler
  void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    Player p = event.getPlayer();
    if (!crownQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
      p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onExplode(EntityExplodeEvent event) {
    event.setCancelled(true);
  }
}
