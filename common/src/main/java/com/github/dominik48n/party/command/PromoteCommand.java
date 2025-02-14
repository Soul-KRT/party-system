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
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

public class PromoteCommand extends PartyCommand {
    PromoteCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.promote");
            return;
        }

        Optional<Party> party;
        try {
            party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
        } catch (final JsonProcessingException e) {
            party = Optional.empty();
        }
        if (party.isEmpty()) {
            player.sendMessage("command.not_in_party");
            return;
        }

        if (!party.get().isLeader(player.uniqueId())) {
            player.sendMessage("command.promote.not_leader");
            return;
        }

        String name = args[0];
        if (player.name().equalsIgnoreCase(name)) {
            player.sendMessage("command.promote.self");
            return;
        }

        Optional<PartyPlayer> target;
        try {
            target = PartyAPI.get().onlinePlayerProvider().get(name);
        } catch (final JsonProcessingException e) {
            target = Optional.empty();
        }
        if (target.isEmpty() || !party.get().members().contains(target.get().uniqueId())) {
            player.sendMessage("command.not_in_your_party");
            return;
        }

        name = target.get().name();

        try {
            PartyAPI.get().changePartyLeader(party.get().id(), player.uniqueId(), target.get().uniqueId(), target.get().memberLimit());
        } catch (final JsonProcessingException e) {
            player.sendMessage("general.error");
            return;
        }
        PartyAPI.get().sendMessageToParty(party.get(), "party.new_leader", name);
        PartyAPI.get().clearPartyRequest(player.name());

        player.sendMessage("command.promote.changed", name);
    }

    @Override
    @NotNull List<String> tabComplete(@NotNull PartyPlayer player, @NotNull String[] args) {
        if (args.length > 1) return Collections.emptyList();
        return this.getPartyMemberNames(player);
    }
}
