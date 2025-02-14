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

package com.github.dominik48n.party.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DenyCommand extends PartyCommand {
    DenyCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.deny");
            return;
        }

        final String name = args[0];
        if (!PartyAPI.get().existsPartyRequest(name, player.name())) {
            player.sendMessage("command.deny.no_request");
            return;
        }

        PartyAPI.get().removePartyRequest(name, player.name());
        player.sendMessage("command.deny.declined");

        try {
            PartyAPI.get().onlinePlayerProvider().get(name).ifPresent(sender -> sender.sendMessage("command.deny.other", player.name()));
        } catch (final JsonProcessingException ignored) {
        }
    }
    @Override
    @NotNull List<String> tabComplete(@NotNull PartyPlayer player, @NotNull String[] args) {
        if (args.length > 1) return Collections.emptyList();
        List<String> suggestions = StringUtils.getSuggestions(this.commandManager.getOnlineUserNamesAtPlayerServer(player), args[0]);
        suggestions.remove(player.name());

        return suggestions;
    }
}
