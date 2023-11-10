package ro.tridentmc.tridentcreative.events;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockBreak implements Listener {
    private Player p;
    String replaced = PlaceholderAPI.setPlaceholders(p, "%player_name%");
    // Common lore condition for all blocks
    private String commonLore = ChatColor.WHITE + "Detinator: " + ChatColor.AQUA + replaced;


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        // Check if the broken block has the common lore
        if (hasSpecificLore(brokenBlock, commonLore)) {
            // Cancel the block break event to prevent the item from dropping
            event.setCancelled(true);

        }
    }

    private boolean hasSpecificLore(Block block, String expectedLore) {
        // Get the block's state and check for lore
        ItemStack itemStack = new ItemStack(block.getType());
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null && itemMeta.hasLore()) {
            // Check if the lore matches the expected lore
            return itemMeta.getLore().contains(expectedLore);
        }

        return false;
    }
}
