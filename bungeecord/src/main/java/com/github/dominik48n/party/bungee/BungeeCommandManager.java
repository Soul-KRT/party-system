/*
 * Copyright 2023 Dominik48N
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dominik48n.party.bungee;

import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.command.CommandManager;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BungeeCommandManager extends Command implements TabExecutor {

   private final @NotNull UserManager<ProxiedPlayer> userManager;
   private final @NotNull MessageConfig messageConfig;
   final @NotNull CommandManager commandManager;

   public BungeeCommandManager(final @NotNull UserManager<ProxiedPlayer> userManager, final @NotNull PartyBungeePlugin plugin) {
      super("party");
      this.commandManager = new CommandManager() {
         @Override
         public List<String> getOnlineUserNamesAtPlayerServer(PartyPlayer partyPlayer) {
            ProxiedPlayer player = plugin.getProxy().getPlayer(partyPlayer.uniqueId());
            if (player == null) return Collections.emptyList();

            Server server = player.getServer();
            if (server == null) return Collections.emptyList();

            return server.getInfo()
                         .getPlayers()
                         .stream()
                         .map(ProxiedPlayer::getName)
                         .collect(Collectors.toList());
         }

         @Override
         public List<String> getPartyMemberNamesAtParty(@NotNull Party party) {
            List<String> memberNames = new ArrayList<>();
            for (UUID member : party.members()) {
               ProxiedPlayer memberPlayer = plugin.getProxy().getPlayer(member);
               if (memberPlayer == null) continue;
               memberNames.add(memberPlayer.getName());
            }

            return memberNames;
         }

         @Override
         public void runAsynchronous(final @NotNull Runnable runnable) {
            plugin.getProxy().getScheduler().runAsync(plugin, runnable);
         }

         @Override
         public @NotNull ProxyPluginConfig config() {
            return plugin.config();
         }

         @Override
         public @NotNull RedisManager redisManager() {
            return plugin.redisManager();
         }
      };
      this.userManager = userManager;
      this.messageConfig = plugin.config().messageConfig();

      if (plugin.config().databaseConfig().enabled()) {
         DatabaseAdapter.createFromConfig(plugin.config().databaseConfig()).ifPresentOrElse(
               databaseAdapter -> {
                  plugin.databaseAdapter(databaseAdapter);

                  plugin.getLogger().info("Connect to " + plugin.config().databaseConfig().type().name() + "...");
                  try {
                     databaseAdapter.connect();
                     plugin.getLogger().info("The connection to the database has been established.");

                     BungeeCommandManager.this.commandManager.addToggleCommand(databaseAdapter);
                  } catch (final Exception e) {
                     plugin.getLogger().log(
                           Level.SEVERE,
                           "The connection to the database could not be established, which is why the party settings cannot be activated.",
                           e
                     );
                  }
               },
               () -> plugin.getLogger().warning(
                     "An unsupported database system was specified, which is why the party settings cannot be activated."
               )
         );
      } else
         plugin.getLogger().warning("The database support is deactivated, which is why the settings cannot be activated.");
   }

   @Override
   public void execute(final CommandSender sender, final String[] args) {
      if (!(sender instanceof final ProxiedPlayer player)) {
         sender.sendMessage(TextComponent.fromLegacyText("This command is only available for players!"));
         return;
      }

      this.userManager.getPlayer(player).ifPresentOrElse(
            partyPlayer -> this.commandManager.execute(partyPlayer, args),
            () -> BungeeCommandManager.this.userManager.sendMessageToLocalUser(
                  player.getUniqueId(),
                  this.messageConfig.getMessage("command.user_not_loaded")
            )
      );
   }

   @Override
   public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
      if (!(sender instanceof final ProxiedPlayer player)) {
         return Collections.emptyList();
      }

      PartyPlayer partyPlayer = this.userManager.getPlayer(player).orElse(null);
      if (partyPlayer == null) return Collections.emptyList();

      return this.commandManager.tabComplete(partyPlayer, args);
   }
}
