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

package de.contens.trade.trade.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.google.inject.Inject;
import de.contens.trade.TradePlugin;
import de.contens.trade.trade.Trade;
import de.contens.trade.trade.TradeMap;
import de.contens.trade.utils.Messages;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Contens
 * @created 22.03.2021
 */

@CommandAlias("trade|handel")
public class TradeCommand extends BaseCommand {

    private TradePlugin trade;
    private TradeMap tradeMap;

    @Inject
    public TradeCommand(TradePlugin trade, TradeMap tradeMap) {
        this.trade = trade;
        this.tradeMap = tradeMap;
    }

    @Default
    @CommandPermission("trade.create")
    @Description("Handel mit einem anderen Spieler")
    @CommandCompletion("@players")
    @Syntax("<Spieler>")
    public void onTrade(Player player, OnlinePlayer tradePlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tradePlayer.getPlayer() == player) {
                    player.sendMessage(Messages.TRADE_SELF.getMessage());
                    return;
                }

                if (tradePlayer.getPlayer() == null) {
                    player.sendMessage(Messages.PLAYER_NOT_ONLINE.getMessage());
                    return;
                }

                if (!trade.readyToTrade.containsValue(player)) {

                    TextComponent tradeRequest = new TextComponent(Messages.TRADE_REQUEST.getMessage(player.getName()));

                    tradeRequest.setClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND, "/trade accept " + player.getName()
                            ));

                    tradeRequest.setHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.TRADE_ACCEPT_HOVER.getMessage()).create()
                            ));

                    tradePlayer.getPlayer().spigot().sendMessage(tradeRequest);

                    player.sendMessage(Messages.TRADE_MAKE_REQUEST.getMessage(tradePlayer.getPlayer().getName()));

                    trade.readyToTrade.put(tradePlayer.getPlayer(), player);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (trade.readyToTrade.containsValue(player)) {
                                trade.readyToTrade.remove(tradePlayer.getPlayer());

                                player.sendMessage(Messages.TRADE_IGNORE.getMessage(tradePlayer.getPlayer().getName()));
                            }
                        }
                    }.runTaskLaterAsynchronously(trade, 20 * 30);

                } else
                    player.sendMessage(Messages.TRADE_ALREADY_PENDING.getMessage());
            }
        }.runTaskAsynchronously(trade);
    }

    @CommandPermission("trade.accept")
    @Subcommand("accept")
    @Private
    public void onAccept(Player player, OnlinePlayer tradePlayer) {
        if (trade.readyToTrade.containsKey(player)) {
            if (tradePlayer.getPlayer() != player) {
                new Trade(player, tradePlayer.getPlayer(), trade, tradeMap);

                trade.readyToTrade.remove(player);
            }
        }
    }

}
