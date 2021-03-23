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

package de.contens.trade;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.contens.trade.trade.TradeModule;
import de.contens.trade.trade.command.TradeCommand;
import de.contens.trade.trade.listener.InventoryClickListener;
import de.contens.trade.trade.listener.InventoryCloseListener;
import de.contens.trade.utils.nms.MinecraftVersion;
import de.contens.trade.utils.reflection.Reflection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Contens
 * @created 21.03.2021
 */

public class TradePlugin extends JavaPlugin {

    private static Logger logger;

    public HashMap<Player, Player> readyToTrade = new HashMap<>();

    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {

        // Check if server version is supported
        switch (Reflection.getVersion()) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
            case "v1_11_R1":
            case "v1_12_R1":
            case "v1_13_R1":
            case "v1_13_R2":
                break;
            default:
                getLog().warning("Diese Server Version wird nicht unterstützt: " + MinecraftVersion.formatVersion(Reflection.getVersion()).name().toLowerCase());
                getLog().warning("Das Plugin wird nur teilweise funktionieren, aber es werden Fehler erwartet!");
        }

        // Initialize Guice
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TradePlugin.class).toInstance(TradePlugin.this);
            }
        }, new TradeModule());

        // Load ACF command manager
        commandManager = new PaperCommandManager(this);

        // Register command
        commandManager.registerCommand(injector.getInstance(TradeCommand.class));

        // Register listeners
        Listener[] listeners = new Listener[] {
                injector.getInstance(InventoryClickListener.class),
                injector.getInstance(InventoryCloseListener.class)
        };

        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public static Logger getLog() {
        return logger;
    }
}
