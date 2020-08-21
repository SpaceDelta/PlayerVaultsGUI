package net.poweredbyawesome.playervaultsgui;

import com.google.common.collect.Maps;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import net.poweredbyawesome.playervaultsgui.commands.*;
import net.spacedelta.sdcore.SDCore;
import net.spacedelta.sdcore.module.impl.playervault.SDPlayerVaults;
import net.spacedelta.sdcore.module.impl.playervault.event.PlayerVaultCloseEvent;
import net.spacedelta.shared.command.CommandManager;
import net.spacedelta.shared.core.SDPlugin;
import net.spacedelta.shared.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;
import java.util.stream.Collectors;

public final class PlayerVaultsGUI extends SDPlugin implements Listener {

    private Economy economy;
    private Permission permissions;
    private ItemStack keyItem;

    private SDPlayerVaults playerVaults;

    @Override
    public void start() {
        reload();

        final SDCore sdCore = (SDCore) Bukkit.getPluginManager().getPlugin("SDCore");
        if (!sdCore.getSDPlayerVaults().isEnabled()) {
            throw new RuntimeException("player vaults module of SDCore not enabled");
        }
        this.playerVaults = sdCore.getSDPlayerVaults();

        registerListener(this);

        new CommandManager(this).registerCommands(
                new VaultAdminCommand(this),
                new VaultBuyCommand(this),
                new VaultGiveCommand(this),
                new VaultGuiCommand(this),
                new VaultReloadCommand(this)
        );
    }

    @Override
    public void end() {
    }

    @Override
    public void reload() {
        checkVault();
        reloadConfig();

        final FileConfiguration config = getConfig();

        // load msg
        Map<String, String> messages = Maps.newHashMap();
        config.getConfigurationSection("messages").getKeys(false)
                .forEach(key -> messages.put(key, color(config.getString("messages." + key))));
        setMessages(messages);

        // load key
        keyItem = new ItemBuilder(Material.valueOf(config.getString("key.item").toUpperCase()))
                .setName(color(config.getString("key.name")))
                .setLore(config.getStringList("key.lore").stream()
                        .map(super::color)
                        .collect(Collectors.toList()))
                .toItem();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent ev) {
        if (!getConfig().getBoolean("override-pv", false))
            return;

        if ((ev.getMessage().equalsIgnoreCase("/pv") || ev.getMessage().equalsIgnoreCase("/playervaults")
                || ev.getMessage().equalsIgnoreCase("/vault"))
                && ev.getPlayer().hasPermission("playervaults.gui.open")) {
            ev.setCancelled(true);
            new WindowManager(this, ev.getPlayer()).openVaultGUI();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        if (!ev.getPlayer().hasPlayedBefore() && getConfig().getBoolean("key.firstjoin")) {
            ev.getPlayer().getInventory().addItem(keyItem);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();

            if (player.getInventory().getItemInMainHand().isSimilar(keyItem) && player.hasPermission("playervaults.gui.key.use")) {
                event.setCancelled(true);

                if (getConfig().getBoolean("key.consume")) {
                    ItemStack key = keyItem.clone();
                    key.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

                    player.getInventory().setItemInMainHand(key);
                    player.updateInventory();
                    player.sendMessage(getMessage("consumeKey"));
                }

                new WindowManager(this, event.getPlayer()).openVaultGUI();
            }
        }
    }

    @EventHandler
    public void onVaultClose(PlayerVaultCloseEvent event) {
        if (getConfig().getBoolean("openOnVaultClose", false) && event.getPlayer().hasPermission("playervaults.gui.open")) {
            new WindowManager(this, event.getPlayer()).openVaultGUI();
        }
    }

    public boolean chargeUser(Player player, String vaultNum) {
        int cost = getConfig().getInt("vaults." + vaultNum + ".cost");
        EconomyResponse e = economy.withdrawPlayer(player, (cost <= 0) ? getConfig().getInt("defaultcost") : cost);
        return e.transactionSuccess();
    }

    public boolean addPermission(Player p, String vaultNum) {
        return permissions.playerAdd(null, p, "playervaults.amount." + vaultNum);
    }

    public boolean takePermission(Player p, String vaultNum) {
        return permissions.playerRemove(null, p, "playervaults.amount." + vaultNum);
    }

    public Permission getPerms() {
        return permissions;
    }

    public SDPlayerVaults getPlayerVaults() {
        return playerVaults;
    }

    public ItemStack getKeyItem() {
        return keyItem;
    }

    public int getMaxVaults(Player player) {
        return playerVaults.getMaxVaults(player);
    }

    private void checkVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            throw new RuntimeException("plugin cannot run without Vault");
        }

        RegisteredServiceProvider<Economy> econService = getServer().getServicesManager().getRegistration(Economy.class);
        if (econService != null) {
            economy = econService.getProvider();
        } else
            warn("no permission economy found");

        RegisteredServiceProvider<Permission> permService = getServer().getServicesManager().getRegistration(Permission.class);
        if (permService != null)
            permissions = permService.getProvider();
        else
            warn("no permission support found");
    }

}
