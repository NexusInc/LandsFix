package ru.landsproject.landsfix.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.landsproject.landsfix.LandsFix;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainHandler implements Listener {
    //BlockPhysicsEvent
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onBlockPhysics(BlockPhysicsEvent e) {
        Block block = e.getBlock();
        if(block.getType() != Material.END_GATEWAY) return;

        Bukkit.getScheduler().runTaskLater(LandsFix.getInstance(), ()-> {
            Location location = block.getLocation();
            BlockVector3 min = BlockVector3.at(location.getBlockX() - 1, location.getBlockY() - 2, location.getBlockZ() - 1);
            BlockVector3 max = BlockVector3.at((location.getBlockX() + 1), location.getBlockY() + 2, (location.getBlockZ() + 1));

            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(block.getWorld()), min, max);
            region.forEach(blockVector3 -> {
                Block block_founded = block.getWorld().getBlockAt(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
                if(block_founded.getType() == Material.BEDROCK) {
                    block_founded.setType(Material.AIR);
                }
            });

            block.setType(Material.AIR);
        }, 5);
    }
    //BlockPhysicsEvent
    //onEntityPickupItem
    private final Cache<UUID, Boolean> cooldownPickup = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onEntityPickupItem(EntityPickupItemEvent e) {
        Entity rawEntity = e.getEntity();
        if(!(rawEntity instanceof Player)) return;
        Player player = (Player) rawEntity;
        ItemStack itemStack = e.getItem().getItemStack();
        if(!itemStack.getType().name().contains("SHULKER_BOX")) return;

        if(cooldownPickup.getIfPresent(player.getUniqueId()) != null) {
            e.setCancelled(true);
            return;
        }

        int inner = 0;
        for(ItemStack rawItem : player.getInventory().getContents()) {
            if(rawItem == null) continue;
            if(!rawItem.getType().name().contains("SHULKER_BOX")) continue;
            inner++;
        }
        for(ItemStack rawItem : player.getInventory().getExtraContents()) {
            if(rawItem == null) continue;
            if(!rawItem.getType().name().contains("SHULKER_BOX")) continue;
            inner++;
        }
        inner++;

        if(inner > LandsFix.getInstance().getConfiguration().getInt("settings.shulker.limit")) {
            e.setCancelled(true);
            cooldownPickup.put(player.getUniqueId(), true);x
            player.sendMessage(LandsFix.getInstance().getConfiguration().getString("messages.more-shulkers"));
        }
    }
    //onEntityPickupItem

    //InventoryClickEvent
    @EventHandler(priority = EventPriority.MONITOR)
    private void onInventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        ItemStack itemStack = e.getCurrentItem();
        if(itemStack == null) return;
        if(inventory.getType() != InventoryType.ENDER_CHEST) return;
        if(itemStack.getType().name().contains("SHULKER_BOX")) {
            e.setCancelled(true);
        }
    }
    //InventoryClickEvent
    //InventoryCloseEvent
    @EventHandler
    private void onPlayerClose(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        Player player = (Player) e.getPlayer();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack rawItem = inventory.getItem(i);
            if (rawItem == null) continue;
            if (!rawItem.getType().name().contains("SHULKER_BOX")) continue;
            player.getInventory().addItem(rawItem.clone());
            rawItem.setAmount(0);
        }
    }
    //InventoryCloseEvent
}
