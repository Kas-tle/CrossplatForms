package dev.projectg.crossplatforms.bungeecord;

import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import dev.projectg.crossplatforms.Constants;
import dev.projectg.crossplatforms.CrossplatForms;
import dev.projectg.crossplatforms.CrossplatFormsBootstrap;
import dev.projectg.crossplatforms.JavaUtilLogger;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.action.ActionSerializer;
import dev.projectg.crossplatforms.bungeecord.handler.BungeeCommandOrigin;
import dev.projectg.crossplatforms.bungeecord.handler.BungeeCordHandler;
import dev.projectg.crossplatforms.command.CommandOrigin;
import dev.projectg.crossplatforms.config.ConfigId;
import dev.projectg.crossplatforms.config.ConfigManager;
import dev.projectg.crossplatforms.handler.BasicPlaceholders;
import dev.projectg.crossplatforms.handler.Placeholders;
import dev.projectg.crossplatforms.interfacing.Interfacer;
import dev.projectg.crossplatforms.interfacing.NoMenusInterfacer;
import dev.projectg.crossplatforms.proxy.CloseMenuAction;
import dev.projectg.crossplatforms.proxy.LuckPermsHook;
import dev.projectg.crossplatforms.proxy.PermissionHook;
import dev.projectg.crossplatforms.proxy.ProtocolizeInterfacer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.CustomChart;

public class CrossplatFormsBungeeCord extends Plugin implements CrossplatFormsBootstrap {

    private static final int BSTATS_ID = 14706;
    public static final BungeeComponentSerializer COMPONENT_SERIALIZER = BungeeComponentSerializer.get();

    static {
        // load information from build.properties
        Constants.fetch();
        Constants.setId("crossplatformsbungee");
    }

    private CrossplatForms crossplatForms;
    private BungeeAudiences audiences;
    private Metrics metrics;
    private boolean protocolizePresent;

    @Override
    public void onEnable() {
        Logger logger = new JavaUtilLogger(getLogger());
        if (crossplatForms != null) {
            logger.warn("Bukkit reloading is NOT supported!");
        }
        metrics = new Metrics(this, BSTATS_ID);
        audiences = BungeeAudiences.create(this);

        BungeeCordHandler serverHandler = new BungeeCordHandler(
            this,
            audiences,
            pluginPresent("LuckPerms") ? new LuckPermsHook() : PermissionHook.empty()
        );

        BungeeCommandManager<CommandOrigin> commandManager;
        try {
            commandManager = new BungeeCommandManager<>(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    (BungeeCommandOrigin::new),
                    origin -> (CommandSender) origin.getHandle()
            );
        } catch (Exception e) {
            logger.severe("Failed to create CommandManager, stopping");
            e.printStackTrace();
            return;
        }

        logger.warn("CrossplatForms-BungeeCord does not yet support placeholder plugins, only %player_name% and %player_uuid% will work (typically).");
        Placeholders placeholders = new BasicPlaceholders();

        protocolizePresent = getProxy().getPluginManager().getPlugin("Protocolize") != null;

        crossplatForms = new CrossplatForms(
                logger,
                getDataFolder().toPath(),
                serverHandler,
                "formsb",
                commandManager,
                placeholders,
                this
        );

        if (!crossplatForms.isSuccess()) {
            return;
        }

        getProxy().getPluginManager().registerListener(this, serverHandler); // events for catching proxy commands
    }

    @Override
    public void preConfigLoad(ConfigManager configManager) {
        if (protocolizePresent) {
            configManager.register(ConfigId.JAVA_MENUS);
        }

        ActionSerializer actionSerializer = configManager.getActionSerializer();
        actionSerializer.simpleGenericAction(ServerAction.TYPE, String.class, ServerAction.class);
        actionSerializer.simpleMenuAction(CloseMenuAction.TYPE, String.class, CloseMenuAction.class);
    }

    @Override
    public Interfacer interfaceManager() {
        if (protocolizePresent) {
            return new ProtocolizeInterfacer();
        } else {
            return new NoMenusInterfacer();
        }
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }

        getProxy().getPluginManager().unregisterListeners(this);
    }

    @Override
    public void addCustomChart(CustomChart chart) {
        metrics.addCustomChart(chart);
    }

    public boolean pluginPresent(String id) {
        return getProxy().getPluginManager().getPlugin(id) != null;
    }
}
