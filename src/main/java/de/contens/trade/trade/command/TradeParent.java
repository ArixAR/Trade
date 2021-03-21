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

import com.google.inject.Inject;
import de.contens.trade.TradePlugin;
import de.contens.trade.command.ParentCommand;
import de.contens.trade.trade.TradeMap;
import de.contens.trade.trade.command.child.AcceptRequest;
import de.contens.trade.utils.Messages;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Contens
 * @created 21.03.2021
 */

public class TradeParent extends ParentCommand {

    private TradePlugin trade;
    private TradeMap tradeMap;

    @Inject
    public TradeParent(TradePlugin trade, TradeMap tradeMap) {
        super("handel", "Schicke eine Handelsanfrage an einen Spieler." ,"/handel <Spieler>", new AcceptRequest(trade, tradeMap));

        this.trade = trade;
    }

    @Override
    public void defaultScope(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;

        Player player = (Player) sender;
        Player tradePlayer = Bukkit.getPlayer(args[0]);

        if (args.length > 0) {
            if (tradePlayer == player) {
                player.sendMessage(Messages.TRADE_SELF.getMessage());
                return;
            }

            if (tradePlayer == null) {
                player.sendMessage(Messages.PLAYER_NOT_ONLINE.getMessage());
                return;
            }

            if (!trade.readyToTrade.containsValue(player)) {

                TextComponent tradeRequest = new TextComponent(Messages.TRADE_REQUEST.getMessage(player.getName()));

                tradeRequest.setClickEvent(
                        new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND, "/handel accept " + player.getName()
                        ));

                tradeRequest.setHoverEvent(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.TRADE_ACCEPT_HOVER.getMessage()).create()
                        ));

                tradePlayer.spigot().sendMessage(tradeRequest);

                player.sendMessage(Messages.TRADE_MAKE_REQUEST.getMessage(tradePlayer.getName()));

                trade.readyToTrade.put(tradePlayer, player);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (trade.readyToTrade.containsValue(player)) {
                            trade.readyToTrade.remove(tradePlayer);

                            player.sendMessage(Messages.TRADE_IGNORE.getMessage(tradePlayer.getName()));
                        }
                    }
                }.runTaskLaterAsynchronously(trade, 20 * 50);

            } else
                player.sendMessage(Messages.TRADE_ALREADY_PENDING.getMessage());

        } else
            player.sendMessage(Messages.TRADE_USAGE.getMessage());
    }
}
