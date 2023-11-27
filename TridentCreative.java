package ro.tridentmc.tridentcreative;

import org.bukkit.plugin.java.JavaPlugin;
import ro.tridentmc.tridentcreative.commands.TCreativeCommand;
import ro.tridentmc.tridentcreative.events.BlockEvent;
import ro.tridentmc.tridentcreative.events.ClickEvent;
import ro.tridentmc.tridentcreative.files.CustomConfig;

public final class TridentCreative extends JavaPlugin {
    private static TridentCreative instance;
    private BlockEvent blockEvent;
    public CustomConfig blockLocationYML;
    private CustomConfig configYml;
    private CustomConfig blockLocationYml;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // Setup config
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        this.configYml = new CustomConfig(this, "config");
        this.blockLocationYml = new CustomConfig(this, "blocklocation");

        // Initialize blockEvent after setting up the configuration
        blockEvent = new BlockEvent(blockLocationYml, this);

        // Ensure that config is initialized before calling loadBlockSet
        blockEvent.loadBlockSet();

        // Register the command and listener
        getCommand("TCreative").setExecutor(new TCreativeCommand(configYml, this));
        getServer().getPluginManager().registerEvents(new ClickEvent(configYml), this);
        getServer().getPluginManager().registerEvents(blockEvent, this);
    }

    @Override
    public void onDisable() {
        blockEvent.saveBlockSet();

    }

}
