package ro.tridentmc.guitest;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
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
import java.util.Arrays;
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
    private int nextPageSlot;
    private int previousPageSlot;
    private String materialNextPage;
    private String materialPreviousPage;
    private String materialCloseGui;
    private int closeGuiSlot;
    private List<String> blockLore;
    private int amountToGive;
    private static final int ITEMS_PER_PAGE = 28;
    private String fillerGlass;

    public void reload() {
        material = config.options().getStringList("material");
        guiTitle = config.options().getString("gui-title");
        guiRows = config.options().getInt("gui-rows");
        nextPageSlot = config.options().getInt("next-page.slot");
        previousPageSlot = config.options().getInt("previous-page.slot");
        materialNextPage = config.options().getString("next-page.material");
        materialPreviousPage = config.options().getString("previous-page.material");
        closeGuiSlot = config.options().getInt("close-gui.slot");
        materialCloseGui = config.options().getString("close-gui.material");
        blockLore = config.options().getStringList("lore");
        amountToGive = config.options().getInt("amount-to-give");
        fillerGlass = config.options().getString("FILLER-GLASS");
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        ChestGui gui = new ChestGui(guiRows, ChatColor.translateAlternateColorCodes('&', guiTitle));
        PaginatedPane pages = new PaginatedPane(0,0,9, guiRows - 1);

        List<ItemStack> itemStacks = new ArrayList<>();

        for (String materialString : material) {
            Material materialEnum = getMaterialFromString(materialString);

            if (materialEnum != null) {
                ItemStack itemStack = new ItemStack(materialEnum);
                ItemMeta meta = itemStack.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "itemmeta"), PersistentDataType.STRING, "creative");

                // Apply shared lore to the item
                List<String> translatedLore = new ArrayList<>();
                for (String loreLine : blockLore) {
                    String translatedLine = ChatColor.translateAlternateColorCodes('&', loreLine);
                    translatedLine = translatedLine.replace("%player%", player.getName()); // Replace with actual player name
                    translatedLore.add(translatedLine);
                }
                meta.setLore(translatedLore);

                itemStack.setItemMeta(meta);
                itemStacks.add(itemStack);
            } else {
                plugin.getLogger().warning("Invalid material in the list: " + materialString);
            }
        }

        int itemsPerPage = gui.getRows() * 9;

        int totalPages = (int) Math.ceil((double) itemStacks.size() / itemsPerPage);

        for (int i = 0; i < totalPages; i++) {
            List<ItemStack> itemsForPage = itemStacks.stream()
                    .skip(i * itemsPerPage)
                    .limit(itemsPerPage)
                    .collect(Collectors.toList());

            pages.addPane(i, createPage(itemsForPage));
        }


        pages.populateWithItemStacks(itemStacks);
        pages.setOnClick(inventoryClickEvent -> {
            if (inventoryClickEvent.getView().getTitle().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', guiTitle))) {
                if (inventoryClickEvent.getClickedInventory() != null && inventoryClickEvent.getCurrentItem() != null) {
                    if (inventoryClickEvent.getClickedInventory().equals(inventoryClickEvent.getView())) ;
                    ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
                    ItemMeta itemMeta = clickedItem.getItemMeta();


                    if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "itemmeta"), PersistentDataType.STRING)){
                        Player p = (Player) inventoryClickEvent.getWhoClicked();


                        // Create a new ItemStack with 64 quantity of the clicked item
                        ItemStack newItemStack = new ItemStack(clickedItem.getType(), amountToGive);
                        newItemStack.setItemMeta(itemMeta);


                        // Give the new ItemStack to the player
                        p.getInventory().addItem(newItemStack);
                        inventoryClickEvent.setCancelled(true);
                    }else{
                        inventoryClickEvent.setCancelled(true);
                    }

                    if (inventoryClickEvent.getCurrentItem() == null) {
                        return;
                    }


                }
                inventoryClickEvent.setCancelled(true);
            }
        });

        gui.addPane(pages);

        OutlinePane background = new OutlinePane(0, 5, 9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.valueOf(fillerGlass))));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        background.setOnClick(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        });

        gui.addPane(background);


        StaticPane navigation = new StaticPane(0,5,9,1);

        int maxPages1 = pages.getPages();
        int currentPage1 = pages.getPage();

        System.out.println(pages.getPage());
        System.out.println(pages.getPages());

        if (currentPage1 == maxPages1){
            navigation.addItem(new GuiItem(new ItemStack(Material.valueOf(fillerGlass)), event -> {
                event.setCancelled(true);
            }), nextPageSlot, 0);
        }else{
            navigation.addItem(new GuiItem(new ItemStack(Material.valueOf(materialNextPage)), event -> {
                final int maxPages = pages.getPages();
                int nextPage = pages.getPage() + 1;
                if (nextPage > maxPages) nextPage = maxPages;
                pages.setPage(nextPage);
                gui.update();
                event.setCancelled(true);
            }), nextPageSlot, 0);

        }

        navigation.addItem(new GuiItem(new ItemStack(Material.valueOf(materialPreviousPage)), event -> {
            int previousPage = pages.getPage() - 1;
            if (previousPage < 0) previousPage = 0;
            pages.setPage(previousPage);
            gui.update();
            event.setCancelled(true);
        }), previousPageSlot, 0);

        navigation.addItem(new GuiItem(new ItemStack(Material.valueOf(materialCloseGui)), event ->
                event.getWhoClicked().closeInventory()), closeGuiSlot, 0);

        gui.addPane(navigation);
        gui.show(player);

        return true;
    }

    private Material getMaterialFromString(String materialString) {
        try {
            return Material.valueOf(materialString);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material in the list, replacing it with STONE");
            e.printStackTrace();
            return Material.STONE;
        }
    }

    private Pane createPage(List<ItemStack> items) {
        PaginatedPane page = new PaginatedPane(0, 0, 9, guiRows - 1, Pane.Priority.LOWEST);
        page.populateWithItemStacks(items);
        return page;
    }

}
