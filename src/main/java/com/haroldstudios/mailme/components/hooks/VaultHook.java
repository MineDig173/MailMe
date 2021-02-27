/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.haroldstudios.mailme.components.hooks;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.MailType;
import com.haroldstudios.mailme.utils.ConfigValue;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public final class VaultHook {

    private static Economy econ = null;
    private static Permission perms = null;
    private final MailMe plugin;

    public VaultHook(final MailMe plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> rspp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            plugin.getLogger().log(Level.WARNING, "No Vault Economy Plugin found!");
        } else {
            econ = rsp.getProvider();
        }
        if (rspp == null) {
            plugin.getLogger().log(Level.WARNING, "No Vault Permissions Plugin found!");
        } else {
            perms = rspp.getProvider();
        }
    }


    /**
     * Attempts to use economy for sending mail
     * @param player Player to attempt with

    /**
     * Attempts to use economy for sending mail
     * @param player Player to attempt with
     * @param amount Amount to try and remove from balance
     * @return If transaction was success
     */
    public boolean attemptTransaction(Player player, double amount) {
        if (econ == null) return true;
        EconomyResponse r = econ.withdrawPlayer(player, amount);
        if(!r.transactionSuccess()) {
            player.sendMessage(plugin.getLocale().getMessage(player, "cmd.vault-insufficient-funds"));
            return false;
        } else {
            String msg = plugin.getLocale().getMessage(player, "cmd.vault");
            msg = msg.replaceAll("@amount", String.valueOf(r.amount));
            player.sendMessage(msg);
        }
        return true;
    }

    public boolean attemptTransaction(Player player, MailType type) {


        double r;

        switch (type) {
            case ITEM:
                r = ConfigValue.COST_ITEM;
                break;
            case BOOK:
                r =ConfigValue.COST_BOOK;
                break;
            default:
                r = ConfigValue.COST_MESSAGE;
                break;
        }

       return attemptTransaction(player, r);
    }

    public boolean hasPermission(OfflinePlayer player, String permission) {
        if (perms == null) return true;
        return perms.playerHas(Bukkit.getWorlds().get(0).getName(), player, permission);
    }
}