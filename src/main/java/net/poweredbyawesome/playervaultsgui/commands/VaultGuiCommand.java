package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.poweredbyawesome.playervaultsgui.WindowManager;
import net.spacedelta.shared.command.Command;
import net.spacedelta.shared.command.CommandBuilder;
import net.spacedelta.shared.core.SDPlugin;
import org.bukkit.command.CommandSender;

public class VaultGuiCommand extends Command {

    public VaultGuiCommand(SDPlugin plugin) {
        super(plugin, CommandBuilder.build("pvgui")
                .description("Open Vault GUI")
                .permission("playervaults.gui.open")
                .requirePlayer());
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        new WindowManager((PlayerVaultsGUI) plugin, asPlayer(sender)).openVaultGUI();
    }

}
