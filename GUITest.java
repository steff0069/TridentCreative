package ro.tridentmc.guitest;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GUI implements CommandExecutor {

    private Config config;
    private GUITest plugin;

    public GUI(Config config, GUITest plugin) {
        this.config = config;
        this.plugin = plugin;
        reload();
    }

    private String guiTitle;
    private int guiRows;
    private List<String> material;
    private List<String> lores;
    private int nextSlot;
    private int prevSlot;


    public void reload() {
        material = config.options().getStringList("material");
        guiTitle = config.options().getString("gui-title");
        guiRows = config.options().getInt("gui-rows");
        lores = config.options().getStringList("lore:");
        nextSlot = config.options().getInt("next-page-slot");
        prevSlot = config.options().getInt("previous-page-slot");
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        ChestGui gui = new ChestGui(guiRows, ChatColor.translateAlternateColorCodes('&', guiTitle));

        StaticPane pane = new StaticPane(0, 0, 9, 6);

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);

        int slotsOnPage = guiRows - 1;

        int itemsPerPage = 9 * slotsOnPage;
        int currentPage = 0;
        int maxItemsPerPage = guiRows * 9 - 9;

        for (int i = 0; i < material.size(); i++) {
            Material currentMaterial = Material.valueOf(material.get(i).toUpperCase());
            ItemStack itemStack = new ItemStack(currentMaterial);

            GuiItem item = new GuiItem(itemStack);

            ItemMeta meta = item.getItem().getItemMeta();

            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "itemmeta"), PersistentDataType.STRING, "creative");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "uncraftable"), PersistentDataType.BYTE, (byte) 1);

            List<String> modifiedLores = lores.stream()
                    .map(lore -> ChatColor.translateAlternateColorCodes('&', lore.replace("%player%", player.getName())))
                    .collect(Collectors.toList());

            meta.setLore(modifiedLores);
            itemStack.setItemMeta(meta);

            // Calculate the row and column based on the current index
            int row = i % (maxItemsPerPage / 9); // Rows are now based on maxItemsPerPage
            int column = i % 9; // Columns are still based on a single row

            // Add the item to the paginated pane
            pane.addItem(item, column, row);
        }

        GuiItem nextPageItem = new GuiItem(new ItemStack(Material.ARROW, 1, (short) 1),
                inventoryClickEvent -> {
                    paginatedPane.setPage(paginatedPane.getPage() + 1);
                    gui.update(); // Update the GUI after changing the page
                });

        GuiItem prevPageItem = new GuiItem(new ItemStack(Material.ARROW),
                inventoryClickEvent -> {
                    paginatedPane.setPage(paginatedPane.getPage() - 1);
                    gui.update(); // Update the GUI after changing the page
                });

        // Add the navigation items to the pane
        pane.addItem(nextPageItem, Slot.fromIndex(nextSlot));
        pane.addItem(prevPageItem, Slot.fromIndex(prevSlot));





        gui.setOnGlobalClick(inventoryClickEvent -> {
            if (inventoryClickEvent.getView().getTitle().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', guiTitle))) {
                if (inventoryClickEvent.getClickedInventory() != null && inventoryClickEvent.getCurrentItem() != null) {
                    if (inventoryClickEvent.getClickedInventory().equals(inventoryClickEvent.getView())) ;
                    ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
                    ItemMeta itemMeta = clickedItem.getItemMeta();


                    if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "itemmeta"), PersistentDataType.STRING)){
                        Player p = (Player) inventoryClickEvent.getWhoClicked();


                        // Create a new ItemStack with 64 quantity of the clicked item
                        ItemStack newItemStack = new ItemStack(clickedItem.getType(), 64);
                        newItemStack.setItemMeta(itemMeta);


                        // Give the new ItemStack to the player
                        p.getInventory().addItem(newItemStack);
                        inventoryClickEvent.setCancelled(true);
                    }

                    if (inventoryClickEvent.getCurrentItem() == null) {
                        return;
                    }


                }
            }
        });

        gui.setOnClose(inventoryCloseEvent -> {
        });
        paginatedPane.addPane(0, pane);
        gui.addPane(paginatedPane);
        gui.update();
        gui.show(player);



        return true;
    }
}
