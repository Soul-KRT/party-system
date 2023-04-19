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

package com.github.dominik48n.party.api;

import com.github.dominik48n.party.api.player.OnlinePlayerProvider;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

public class DefaultPartyProvider<TUser> implements PartyProvider {

    private final @NotNull OnlinePlayerProvider onlinePlayerProvider;

    private final @NotNull RedisManager redisManager;

    public DefaultPartyProvider(final @NotNull RedisManager redisManager, final @NotNull UserManager<TUser> userManager) {
        this.onlinePlayerProvider = new DefaultOnlinePlayersProvider<>(redisManager, userManager);
        this.redisManager = redisManager;

        PartyAPI.set(this);
    }

    @Override
    public @NotNull OnlinePlayerProvider onlinePlayerProvider() {
        return this.onlinePlayerProvider;
    }

    @Override
    public void createPartyRequest(final @NotNull String source, final @NotNull String target, final int expires) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.setex("request:" + source + ":" + target, expires, "");
        }
    }

    @Override
    public boolean existsPartyRequest(final @NotNull String source, final @NotNull String target) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            return jedis.exists("request:" + source + ":" + target);
        }
    }
}
