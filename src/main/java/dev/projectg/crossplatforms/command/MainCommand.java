package dev.projectg.crossplatforms.command;

import com.google.common.collect.ImmutableMap;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.form.MenuUtils;
import dev.projectg.crossplatforms.form.java.JavaMenuRegistry;
import dev.projectg.crossplatforms.reloadable.ReloadableRegistry;
import dev.projectg.crossplatforms.form.bedrock.BedrockFormRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class MainCommand implements CommandExecutor {

    private static final Map<Logger.Level, ChatColor> LOGGER_COLORS = ImmutableMap.of(
            Logger.Level.INFO, ChatColor.RESET,
            Logger.Level.WARN, ChatColor.GOLD,
            Logger.Level.SEVERE, ChatColor.RED);

    private static final String[] HELP = {
            "/ghub - Opens the default form if one exists. If not, shows the help page",
            "/ghub - Opens the help page",
            "/ghub form <form> - Open a form with the defined name",
            "/ghub form <form> <player> - Sends a form to a given player",
            "/ghub reload - reloads the selector"
    };

    private static final String NO_PERMISSION = "Sorry, you don't have permission to run that command!";
    private static final String UNKNOWN = "Sorry, that's an unknown command!";

    private final BedrockFormRegistry bedrockFormRegistry;
    private final JavaMenuRegistry javaMenuRegistry;

    public MainCommand(BedrockFormRegistry bedrockFormRegistry, JavaMenuRegistry javaMenuRegistry) {
        this.bedrockFormRegistry = bedrockFormRegistry;
        this.javaMenuRegistry = javaMenuRegistry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player || commandSender instanceof ConsoleCommandSender)) {
            return false;
        }
        // todo: cleanup
        if (args.length == 0) {
            // send the default form, help if console
            sendForm(commandSender, BedrockFormRegistry.DEFAULT);
            return true;
        }

        // At least one arg
        switch (args[0]) {
            case "reload":
                if (commandSender.hasPermission("geyserhub.reload")) {
                    if (!ReloadableRegistry.reloadAll()) {
                        sendMessage(commandSender, Logger.Level.SEVERE, "There was an error reloading something! Please check the server console for further information.");
                    }
                } else {
                    sendMessage(commandSender, Logger.Level.SEVERE, NO_PERMISSION);
                }
                break;
            case "help":
                sendHelp(commandSender);
                break;
            case "form":
                if (commandSender.hasPermission("geyserhub.form")) {
                    if (args.length == 1) {
                        sendMessage(commandSender, Logger.Level.SEVERE, "Please specify a form to open! Specify a form with \"/ghub form <form>\"");
                    } else if (args.length == 2) {
                        sendForm(commandSender, args[1]);
                    } else if (args.length == 3) {
                        if (commandSender.hasPermission("geyserhub.form.others")) {
                            Player target = Bukkit.getServer().getPlayer(args[2]);
                            if (target == null) {
                                sendMessage(commandSender, Logger.Level.SEVERE, "That player doesn't exist!");
                            } else {
                                sendForm(target, args[1]);
                                sendMessage(commandSender, Logger.Level.INFO, "Made " + target.getName() + " open form: " + args[1]);
                            }
                        } else {
                            sendMessage(commandSender, Logger.Level.SEVERE, NO_PERMISSION);
                        }
                    } else {
                        sendMessage(commandSender, Logger.Level.SEVERE, "Too many command arguments!");
                    }
                } else {
                    sendMessage(commandSender, Logger.Level.SEVERE, NO_PERMISSION);
                }
                break;
            default:
                sendMessage(commandSender, Logger.Level.SEVERE, UNKNOWN);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender commandSender) {
        // todo: only show players with the given permissions certain entries? not sure if it can be integrated any way into spigot command completions
        commandSender.sendMessage(HELP);
    }

    /**
     * send a form to a command sender. if the commandsender is a console then it will just send the help page.
     * @param commandSender the command sender.
     * @param formName the form name to send
     */
    private void sendForm(@Nonnull CommandSender commandSender, @Nonnull String formName) {
        if (commandSender instanceof Player) {
            MenuUtils.sendForm((Player) commandSender, bedrockFormRegistry, javaMenuRegistry, formName);
        } else if (commandSender instanceof ConsoleCommandSender) {
            sendHelp(commandSender);
        }
    }

    public static void sendMessage(@Nonnull CommandSender sender, @Nonnull Logger.Level level, @Nonnull String message) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(level);
        Objects.requireNonNull(message);

        if (sender instanceof ConsoleCommandSender) {
            Logger.getLogger().log(level, message);
        } else {
            sender.sendMessage("[GeyserHub] " + LOGGER_COLORS.getOrDefault(level, ChatColor.RESET) + message);
        }
    }
}