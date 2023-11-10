package ro.tridentmc.tridentcreative.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ro.tridentmc.tridentcreative.TridentCreative;
import ro.tridentmc.tridentcreative.utils.ColorUtils;

import java.util.ArrayList;

public class Test implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        ItemStack grassblock = new ItemStack(Material.GRASS_BLOCK, 64);
        ItemMeta grassmeta = grassblock.getItemMeta();

        ArrayList<String> grasslore = new ArrayList<>();
        grasslore.add("");
        grasslore.add(ColorUtils.translateColorCodes("&fDetinator: " + ChatColor.GREEN + e.getPlayer().getName() + " "));
        grassmeta.setLore(grasslore);

        grassblock.setItemMeta(grassmeta);


        // /
        NamespacedKey trident = new NamespacedKey(TridentCreative.getPlugin(), "trident-creative");
        // /

        Player p = e.getPlayer();
        // /
        grassmeta.getPersistentDataContainer().set(trident, PersistentDataType.STRING, "Trident For Life!");
        grassblock.setItemMeta(grassmeta);

        PersistentDataContainer container = grassblock.getItemMeta().getPersistentDataContainer();
        if (container.has(trident, PersistentDataType.STRING)){

            String value = container.get(trident, PersistentDataType.STRING);
            e.setDropItems(false);
        } else if (!container.has(trident, PersistentDataType.STRING)){

            String value = container.get(trident, PersistentDataType.STRING);
            e.setDropItems(true);

        }



    }
}
