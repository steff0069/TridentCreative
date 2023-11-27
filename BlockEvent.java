package ro.tridentmc.tridentcreative.events;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ro.tridentmc.tridentcreative.files.CustomConfig;
import ro.tridentmc.tridentcreative.TridentCreative;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockEvent implements Listener {

    private Set<Location> blocksWithSpecificLore;
    private final CustomConfig customConfig;

    private final TridentCreative plugin;

    private boolean enabledrops;

    public BlockEvent(CustomConfig customConfig, TridentCreative plugin) {
        this.customConfig = customConfig;
        this.plugin = plugin;
        this.blocksWithSpecificLore = new HashSet<>();
    }

    public void reload(){
        enabledrops = customConfig.options().getBoolean("drop-items");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();
        ItemMeta meta = item.getItemMeta();

        if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "itemmeta"), PersistentDataType.STRING)) {
            blocksWithSpecificLore.add(block.getLocation());
            saveBlockSet();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (blocksWithSpecificLore.contains(block.getLocation())) {
            event.setDropItems(true);
        }
    }

    public void saveBlockSet() {
        CustomConfig config = plugin.blockLocationYML;
        if (config != null) {
            List<Location> locations = new ArrayList<>(blocksWithSpecificLore);

            int size = locations.size();
            for (int i = 0; i < size; i++) {
                config.options().set("blocksWithSpecificLore." + i, locations.get(i));
            }
            config.saveFile();
            config.reload();
        }
    }

    public void loadBlockSet() {
        CustomConfig config = plugin.blockLocationYML;
        if (config != null) {
            ConfigurationSection section = config.options().getConfigurationSection("blocksWithSpecificLore");

            if (section == null) return;

            section.getKeys(false).forEach(id -> {
                String key = "blocksWithSpecificLore." + id;
                Location location = config.options().getLocation(key);
            });
        }

    }
}
