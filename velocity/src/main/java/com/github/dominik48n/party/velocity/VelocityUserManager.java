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

package com.github.dominik48n.party.velocity;

import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class VelocityUserManager extends UserManager<Player> {

    private final @NotNull ProxyPluginConfig config;
    private final @NotNull ProxyServer server;

    VelocityUserManager(final @NotNull RedisManager redisManager, final @NotNull ProxyPluginConfig config, final @NotNull ProxyServer server) {
        super(redisManager);
        this.config = config;
        this.server = server;
    }

    @Override
    public void sendMessageToLocalUser(final @NotNull UUID uniqueId, final @NotNull Component component) {
        this.server.getPlayer(uniqueId).ifPresent(player -> player.sendMessage(component));
    }

    @Override
    protected void sendMessage(final @NotNull Player player, final @NotNull Component component) {
        player.sendMessage(component);
    }

    @Override
    protected @NotNull String playerName(final @NotNull Player player) {
        return player.getUsername();
    }

    @Override
    protected @NotNull UUID playerUUID(final @NotNull Player player) {
        return player.getUniqueId();
    }

    @Override
    protected @NotNull MessageConfig messageConfig() {
        return this.config.messageConfig();
    }
}
