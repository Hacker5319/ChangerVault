package ru.fantastworld.changerVault;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VaultListener implements Listener {

    private final ChangerVault plugin;
    private final Random random = new Random();

    public VaultListener(ChangerVault plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVaultOpen(PlayerInteractEvent event) {
        if (!plugin.isActivated()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.VAULT) return;

        Player player = event.getPlayer();
        Vault vault = (Vault) event.getClickedBlock().getState();

        boolean isOminous = vault.getBlockData().getAsString().contains("ominous=true");
        boolean isActive = vault.getBlockData().getAsString().contains("vault_state=active");

        if (!isActive) return;
        if (vault.getRewardedPlayers().contains(player.getUniqueId())) return;

        String configType = isOminous ? "OMINOUS" : "COMMON";
        Map<Material, Double> overrides = plugin.getConfigManager().getOverridesForType(configType);
        if (overrides.isEmpty()) return;

        LootTable lootTable = vault.getLootTable();

        LootContext context = new LootContext.Builder(vault.getLocation())
                .lootedEntity(player)
                .killer(player)
                .build();

        Collection<ItemStack> generatedLoot = lootTable.populateLoot(random, context);
        List<ItemStack> finalLoot = new ArrayList<>();

        for (ItemStack item : generatedLoot) {
            if (item != null && item.getType() != Material.AIR) {
                finalLoot.add(item);
            }
        }

        for (Map.Entry<Material, Double> entry : overrides.entrySet()) {
            Material material = entry.getKey();
            double chance = entry.getValue();

            finalLoot.removeIf(item -> item.getType() == material);

            if (random.nextDouble() < chance) {
                finalLoot.add(new ItemStack(material, 1));
            }
        }

        vault.addRewardedPlayer(player.getUniqueId());
        vault.update();

        // Имитация открытия ларца

        Location vaultLoc = vault.getLocation();
        vault.getWorld().playSound(vaultLoc, Sound.BLOCK_VAULT_OPEN_SHUTTER, 1.0f, 1.0f);
        vault.getWorld().spawnParticle(Particle.BLOCK, vaultLoc.add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, vault.getBlockData());

        event.setCancelled(true);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= finalLoot.size()) {
                    this.cancel();
                    return;
                }

                ItemStack item = finalLoot.get(index);
                Location dropLoc = vaultLoc.clone().add(0.5, 1.2, 0.5);

                vault.getWorld().dropItemNaturally(dropLoc, item);
                vault.getWorld().playSound(vaultLoc, Sound.BLOCK_VAULT_EJECT_ITEM, 0.8f, 1.0f);
                vault.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, dropLoc, 5, 0.1, 0.1, 0.1, 0);

                index++;
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }
}