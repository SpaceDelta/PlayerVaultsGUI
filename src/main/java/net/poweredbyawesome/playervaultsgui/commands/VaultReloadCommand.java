package net.poweredbyawesome.playervaultsgui.commands;

import net.spacedelta.shared.command.Command;
import net.spacedelta.shared.command.CommandBuilder;
import net.spacedelta.shared.core.SDPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VaultReloadCommand extends Command {

    public VaultReloadCommand(SDPlugin plugin) {
        super(plugin, CommandBuilder.build("reload")
                .description("Reload plugin")
                .permission("playervaults.gui.admin"));
    }

    @Override
    public void onCommand(CommandSender sender, String usedLabel, String[] args) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Reloaded");
    }

}
