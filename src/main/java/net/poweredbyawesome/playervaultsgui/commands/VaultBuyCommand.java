package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.spacedelta.shared.command.Command;
import net.spacedelta.shared.command.CommandBuilder;
import net.spacedelta.shared.core.SDPlugin;
import net.spacedelta.shared.util.UtilNumber;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultBuyCommand extends Command {

    public VaultBuyCommand(SDPlugin plugin) {
        super(plugin, CommandBuilder.build("pvbuy")
                .description("Buy a vault")
                .permission("playervaults.gui.buy")
                .usage("<vault number>")
                .requirePlayer());
    }

    @Override
    public void onCommand(CommandSender commandSender, String usedLabel, String[] args) {
        Player player = (Player) commandSender;
        Integer vaultNumber = UtilNumber.fromString(args[0]);
        if (vaultNumber == null) {
            usage(commandSender, usedLabel);
            return;
        }

        PlayerVaultsGUI guiPlugin = (PlayerVaultsGUI) plugin;

        if (vaultNumber <= plugin.getConfig().getConfigurationSection("vaults").getKeys(false).size()) {
            if (guiPlugin.getPlayerVaults().canOpenVault(player, vaultNumber - 1)) {

                if (((PlayerVaultsGUI) plugin).chargeUser(player, vaultNumber.toString())) {
                    guiPlugin.addPermission(player, vaultNumber.toString());
                    player.sendMessage(plugin.getMessage("buySuccess", vaultNumber));
                    return;
                }

                player.sendMessage(plugin.getMessage("noVaultAccess", vaultNumber));
            }

        } else {
            commandSender.sendMessage(ChatColor.RED + "You cannot buy that vault as it exceeds the limit");
        }

    }


}
