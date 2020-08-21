package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.poweredbyawesome.playervaultsgui.WindowManager;
import net.spacedelta.shared.command.Command;
import net.spacedelta.shared.command.CommandBuilder;
import net.spacedelta.shared.util.UtilNumber;
import net.spacedelta.shared.util.UtilPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultAdminCommand extends Command {

    public VaultAdminCommand(PlayerVaultsGUI plugin) {
        super(plugin, CommandBuilder.build("pvadmin")
                .description("Give vaults")
                .permission("playervaults.gui.admin")
                .usage("[add | take]", "[player]", "[amount]"));
    }

    @Override
    public void onCommand(CommandSender commandSender, String usedLabel, String[] args) {
        PlayerVaultsGUI guiPlugin = (PlayerVaultsGUI) plugin;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("players") && isPlayer(commandSender)) {
                new WindowManager(guiPlugin, asPlayer(commandSender)).openPlayersWindow();
            }
        }

        if (args.length >= 3) {
            Player target = UtilPlayer.getP(args[1]);
            if (target == null || UtilNumber.fromString(args[2]) == null) {
                usage(commandSender, usedLabel);
                return;
            }

            int vault = UtilNumber.fromString(args[2]);
            int maxVault = guiPlugin.getMaxVaults(target);

            if (args[0].equalsIgnoreCase("add")) {
                guiPlugin.addPermission(target, String.valueOf(vault + maxVault));
                commandSender.sendMessage(ChatColor.GREEN + String.format("Added %s vaults to %s", vault, target.getName()));
                return;
            } else if (args[0].equalsIgnoreCase("take") && maxVault != 0) {
                guiPlugin.addPermission(target, String.valueOf(vault + maxVault));
                commandSender.sendMessage(ChatColor.GREEN + String.format("Taken %s vaults from %s", vault, target.getName()));
                return;
            }
        }

        usage(commandSender, usedLabel);
    }

}

