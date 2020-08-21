package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.spacedelta.shared.command.CommandBuilder;
import net.spacedelta.shared.core.SDPlugin;
import net.spacedelta.shared.util.UtilNumber;
import net.spacedelta.shared.util.UtilPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VaultGiveCommand extends net.spacedelta.shared.command.Command {

    public VaultGiveCommand(SDPlugin plugin) {
        super(plugin, CommandBuilder.build("pvgive")
                .description("Give a vault to a player")
                .permission("playervaults.gui.give")
                .usage("<player>", "<amount>"));
    }

    @Override
    public void onCommand(CommandSender commandSender, String usedLabel, String[] args) {
        Player player = UtilPlayer.getP(args[0]);
        if (player != null && UtilNumber.fromString(args[1]) != null) {
            final ItemStack key = ((PlayerVaultsGUI) plugin).getKeyItem().clone();
            key.setAmount(UtilNumber.fromString(args[1]));
            player.getInventory().addItem(key);
            return;
        }

        usage(commandSender, usedLabel);
    }

}
