/*
 * ******************************************************************************
 *  * Copyright (C) 2021, Contens
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  *
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * 3. Neither the name of the copyright holder nor the names of its contributors
 *  *    may be used to endorse or promote products derived from this software
 *  *    without specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *  *****************************************************************************
 */

package de.contens.trade.trade;

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.contens.trade.TradePlugin;
import de.contens.trade.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.IntStream;

/**
 * @author Contens
 * @created 21.03.2021
 */

public class Trade {

    private TradePlugin trade;
    private TradeMap tradeMap;

    private Inventory inventory;

    private Player player1;
    private Player player2;

    private int[] left = new int[] { 10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39 };
    private int[] right = new int[] { 14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43 };

    @Inject
    public Trade(@Assisted("player1") Player player1, @Assisted("player2") Player player2, TradePlugin trade, TradeMap tradeMap) {
        this.player1 = player1;
        this.player2 = player2;

        this.trade = trade;
        this.tradeMap = tradeMap;

        this.inventory = Bukkit.createInventory(null, 9 * 6, "§9Handel");

        player1.openInventory(inventory);
        player2.openInventory(inventory);

        ItemStack glass = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"));
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemStack tradeInfo = new ItemStack(Material.NAME_TAG);

        ItemMeta glassMeta = glass.getItemMeta();
        ItemMeta terracottaMeta = barrier.getItemMeta();
        ItemMeta tradeInfoMeta = tradeInfo.getItemMeta();

        glassMeta.setDisplayName(" ");
        terracottaMeta.setDisplayName("§cNoch nicht bereit");
        tradeInfoMeta.setDisplayName("§e" + player1.getName() + " §7│ §e" + player2.getName());

        glass.setDurability((short) 15);
        glass.setItemMeta(glassMeta);

        barrier.setItemMeta(terracottaMeta);

        tradeInfo.setItemMeta(tradeInfoMeta);

        IntStream.of(0, 1, 2, 3, 5, 6, 7, 8, 9, 13, 17, 18, 22, 26, 27, 31, 35, 36, 40, 44, 45, 46, 48, 49, 50, 52, 53).forEach(slot -> inventory.setItem(slot, glass));

        inventory.setItem(47, barrier);
        inventory.setItem(51, barrier);
        inventory.setItem(4, tradeInfo);

        tradeMap.put(player1.getName(), this);
        tradeMap.put(player2.getName(), this);
    }

    public void addItem(Player player, ItemStack itemStack, int clickedSlot) {
        int[] slots = player == player1 ? left : right;

        for (int i : slots) {
            if (inventory.getItem(i) == null) {
                if (i != slots[slots.length - 1]) {
                    inventory.setItem(i, itemStack);

                    player.getInventory().setItem(clickedSlot, null);

                    break;
                }
            }
        }
    }

    public void removeItem(Player player, int slot) {
        int[] slots = player == player1 ? left : right;

        if (Ints.asList(slots).contains(slot)) {
            ItemStack clicked = inventory.getItem(slot);

            if (clicked != null) {
                player.getInventory().addItem(clicked);

                inventory.setItem(slot, null);
            }
        }
    }

    private void remove() {
        tradeMap.remove(player1.getName());
        tradeMap.remove(player2.getName());

        player1.getOpenInventory().close();
        player2.getOpenInventory().close();
    }

    public void abort() {
        this.remove();

        for (int i : left) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null) {
                player1.getInventory().addItem(itemStack);
            }
        }

        for (int i : right) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null) {
                player2.getInventory().addItem(itemStack);
            }
        }

        player1.sendMessage(Messages.TRADE_ABORT.getMessage());
        player2.sendMessage(Messages.TRADE_ABORT.getMessage());
    }

    private void succeed() {
        this.remove();

        for (int i : left) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null) {
                player2.getInventory().addItem(itemStack);
            }
        }

        for (int i : right) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null) {
                player1.getInventory().addItem(itemStack);
            }
        }

        player1.sendMessage(Messages.TRADE_SUCCEED.getMessage());
        player2.sendMessage(Messages.TRADE_SUCCEED.getMessage());
    }

    public void handleConfirmation(Player player, int slot) {
        ItemStack clicked = inventory.getItem(slot);
        ItemMeta clickedMeta = inventory.getItem(slot).getItemMeta();

        if ((player == player1 && slot == 47) || (player == player2 && slot == 51)) {
            if (clicked.getType() == Material.BARRIER) {
                clicked.setType(Material.EMERALD);
                clickedMeta.setDisplayName("§aBereit");
                clicked.setItemMeta(clickedMeta);
            } else if (clicked.getType() == Material.EMERALD) {
                clicked.setType(Material.BARRIER);
                clickedMeta.setDisplayName("§cNoch nicht bereit");
                clicked.setItemMeta(clickedMeta);
            }
        }

        if (inventory.getItem(47).getType() == Material.EMERALD && inventory.getItem(51).getType() == Material.EMERALD) {
            this.succeed();
        }
    }

    public interface Factory {
        Trade createTrade(@Assisted("player1") Player player1, @Assisted("player2") Player player2);
    }
}
