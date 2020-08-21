package net.poweredbyawesome.playervaultsgui;

import de.themoep.inventorygui.*;
import net.poweredbyawesome.playervaultsgui.data.PlayerData;
import net.spacedelta.sdcore.util.ItemBuilder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WindowManager {

    private final PlayerData pd;
    private final Player p;
    private final PlayerVaultsGUI plugin;

    public WindowManager(PlayerVaultsGUI pl, Player p) {
        this.plugin = pl;
        this.p = p;
        this.pd = new PlayerData(pl, p.getUniqueId().toString());
    }

    public void openVaultGUI() {
        GuiElementGroup group = buildGroup();

        InventoryGui gui = new InventoryGui(plugin, p, plugin.getConfig().getString("gui.name"), buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL), GuiPageElement.PageAction.PREVIOUS, ChatColor.RED + "PREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.CHARCOAL), GuiPageElement.PageAction.NEXT, ChatColor.GREEN + "NEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(plugin.getConfig().getString("gui.fillitem")), 1));
        group.setFiller(gui.getFiller());
        gui.addElement(group);
        gui.show(p);
    }

    public void openPlayersWindow() {
        GuiElementGroup group = new GuiElementGroup('x');
        String filler = plugin.getConfig().getString("gui.fillitem");
        ItemBuilder playerHead = new ItemBuilder(Material.PLAYER_HEAD);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            playerHead.asPlayerHead(onlinePlayer.getName());
            group.addElement(new StaticGuiElement('x',
                    playerHead.build().clone(),
                    click -> {
                        p.closeInventory();
                        openPlayersGui(onlinePlayer.getUniqueId());
                        return true;
                    },
                    onlinePlayer.getDisplayName(),
                    ChatColor.AQUA + "Open players' vault"
            ));
        }

        InventoryGui gui = new InventoryGui(plugin, p, ChatColor.RED + "Online Players", buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, ChatColor.RED + "PREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.COAL, (byte) 1), GuiPageElement.PageAction.NEXT, ChatColor.GREEN + "NEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler)));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
    }

    public void openPlayersGui(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            p.sendMessage(plugin.getMessage("not-found", "that player"));
            return;
        }

        GuiElementGroup group = new GuiElementGroup('x');
        String unlocked = plugin.getConfig().getString("unlocked.item");
        String locked = plugin.getConfig().getString("locked.item");
        String filler = plugin.getConfig().getString("gui.fillitem");

        for (int vaultNum = 1; vaultNum <= 100; vaultNum++) {
            boolean hasPerm = (Bukkit.getPlayer(uuid) == null)
                    ? plugin.getPerms().playerHas(null, offlinePlayer, "playervaults.amount." + vaultNum)
                    : Bukkit.getPlayer(uuid).hasPermission("playervaults.amount." + vaultNum);

            if (hasPerm) {
                List<String> infos = new ArrayList<>();
                infos.add(plugin.getConfig().getString("unlocked.name"));
                infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), String.valueOf(vaultNum)));
                int finalVaultNum = vaultNum; //java so picky
                group.addElement(new StaticGuiElement('x',
                        new ItemStack(Material.valueOf(unlocked)),
                        click -> {
                            p.closeInventory();
                            plugin.runTaskLater(() -> p.performCommand("playervault " + offlinePlayer.getName() + " " + finalVaultNum), 5L);
                            return true;
                        },
                        infos.toArray((new String[0]))
                ));
            } else {
                break;
            }
        }

        InventoryGui gui = new InventoryGui(plugin, p,
                ChatColor.AQUA + offlinePlayer.getName() + "'s " + ChatColor.GREEN + " Vaults", buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL), GuiPageElement.PageAction.PREVIOUS, ChatColor.RED + "PREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.CHARCOAL), GuiPageElement.PageAction.NEXT, ChatColor.GREEN + "NEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler)));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
    }

    public GuiElementGroup buildGroup() { //TODO: Cleanup :(!
        GuiElementGroup group = new GuiElementGroup('x');
        String[] unlocked = plugin.getConfig().getString("unlocked.item").split(":");
        String[] locked = plugin.getConfig().getString("locked.item").split(":");
        if (!plugin.getConfig().getBoolean("disablePurchases")) {
            for (String finalVaultNum : plugin.getConfig().getConfigurationSection("vaults").getKeys(false)) {
                if (plugin.getPlayerVaults().canOpenVault(p, Integer.parseInt(finalVaultNum))) {
                    List<String> infos = new ArrayList<>();
                    infos.add(pd.getVaultName(finalVaultNum) != null ? pd.getVaultName(finalVaultNum) : plugin.getConfig().getString("unlocked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), finalVaultNum));
                    //TODO create button method
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(Material.valueOf(unlocked[0]), 1),
                            click -> createButton(finalVaultNum, click),
                            infos.toArray((new String[0]))
                    ));
                } else {
                    List<String> infos = new ArrayList<>();
                    infos.add(plugin.getConfig().getString("locked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("locked.lore"), finalVaultNum));
                    if (getCost(finalVaultNum) == 0) {
                        group.addElement(new StaticGuiElement('x',
                                new ItemStack(Material.valueOf(locked[0]), 1),
                                click -> {
                                    p.sendMessage(plugin.getMessage("vaultLocked", finalVaultNum));
                                    return true;
                                },
                                infos.toArray((new String[0]))
                        ));
                    } else {
                        group.addElement(new StaticGuiElement('x',
                                new ItemStack(Material.valueOf(locked[0]), 1),
                                click -> {
                                    if (!plugin.getPlayerVaults().canOpenVault(p, Integer.parseInt(finalVaultNum) - 1)) {
                                        p.sendMessage(plugin.getMessage("noVaultAccess", finalVaultNum));
                                        return true;
                                    }
                                    if (plugin.chargeUser(p, finalVaultNum)) {
                                        p.sendMessage(plugin.getMessage("buySuccess", finalVaultNum));

                                        if (plugin.addPermission(p, finalVaultNum)) {
                                            p.closeInventory();
                                            //wait for permissions to update first.
                                            plugin.runAsyncLater(this::openVaultGUI, 15);
                                        }
                                    } else {
                                        p.sendMessage(plugin.getMessage("insufficientFunds"));
                                    }
                                    return true;
                                },
                                infos.toArray((new String[0]))
                        ));
                    }
                }
            }
        } else { //Disabled purchases
            for (int vaultNum = 1; vaultNum <= 100; vaultNum++) {
                if (plugin.getPlayerVaults().canOpenVault(p, vaultNum)) {
                    String finalVaultNum = String.valueOf(vaultNum);
                    List<String> infos = new ArrayList<>();
                    infos.add(pd.getVaultName(finalVaultNum) != null ? pd.getVaultName(finalVaultNum) : plugin.getConfig().getString("unlocked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), String.valueOf(vaultNum)));
                    //TODO create button method
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(pd.getVaultItem(finalVaultNum) != null ? pd.getVaultItem(finalVaultNum) : Material.valueOf(unlocked[0]), 1),
                            click -> createButton(finalVaultNum, click),
                            infos.toArray((new String[0]))
                    ));
                } else {
                    break;
                }
            }
        }
        return group;
    }

    private boolean createButton(String finalVaultNum, GuiElement.Click click) {
        p.closeInventory();
        ClickType type = click.getType();
        if (type.isLeftClick()) {
            plugin.runTaskLater(() -> p.performCommand("playervault " + finalVaultNum), 5L);
        }

        if (plugin.getConfig().getBoolean("allowCustomization") && p.hasPermission("playervaults.gui.customize")) {
            if (type.name().equals("MIDDLE")) {
                new AnvilGUI(plugin, p, "Enter Vault Name", (player, reply) -> {
                    if (reply != null || reply.equals("")) {
                        pd.setVaultName(finalVaultNum, reply);
                        refresh();
                        return null;
                    }

                    p.sendMessage(plugin.getMessage("invalid", reply, "Name"));
                    return "Invalid Name";
                });
            }
            if (type.isRightClick()) {
                new AnvilGUI(plugin, p, "Enter Vault Item", (player, reply) -> {
                    if (Material.getMaterial(reply.toUpperCase()) != null) {
                        pd.setVaultItem(finalVaultNum, reply.toUpperCase());
                        refresh();
                        return null;
                    }

                    p.sendMessage(plugin.getMessage("invalid", reply, "Item"));
                    return "Invalid Name";
                });
            }
        }
        return true;
    }

    private void refresh() {
        openVaultGUI();
    }

    public void openPlayersGui(OfflinePlayer offlinePlayer) {
        openPlayersGui(offlinePlayer.getUniqueId());
    }

    public List<String> replaceStrings(List<String> lore, String vaultNum) {
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replace("<COST>", String.valueOf(getCost(vaultNum))).replace("<VAULTNUM>", vaultNum));
        }
        return lore;
    }

    public int getCost(String vaultNum) {
        int cost = plugin.getConfig().getInt("vaults." + vaultNum + ".cost");
        return (cost <= 0) ? plugin.getConfig().getInt("defaultcost") : cost;
    }

    public String[] buildMatrix(int i) {
        String defMatrix = " xxxxxxx ";
        if (!plugin.getConfig().getBoolean("defaultMatrix")) {
            return plugin.getConfig().getStringList("matrix").toArray(new String[0]);
        }
        List<String> matrix = new ArrayList<>();
        matrix.add("         ");
        matrix.add(defMatrix);
        if (i >= 8) {
            matrix.add(defMatrix);
        }
        if (i >= 15) {
            matrix.add(defMatrix);
        }
        if (i >= 22) {
            matrix.add(defMatrix);
            matrix.add("b       f");
            return matrix.toArray(new String[0]);
        }
        matrix.add("         ");
        return matrix.toArray(new String[0]);
    }
}