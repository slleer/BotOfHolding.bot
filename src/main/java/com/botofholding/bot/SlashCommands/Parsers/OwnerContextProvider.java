package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface OwnerContextProvider extends EphemeralSettingProvider {

    /**
     * Builds a comprehensive OwnerContext by combining the target owner (user or guild)
     * with the user's ephemeral reply preference. This is the standard way to prepare
     * context for API calls.
     *
     * @param event The interaction event.
     * @param apiClient The API client.
     * @return A Mono emitting the appropriate OwnerContext.
     */
    default Mono<OwnerContext> getOwnerContext(ChatInputInteractionEvent event, ApiClient apiClient) {
        // 1. Determine the target owner (the user, or the guild if in a server).
        Mono<TargetOwner> targetOwnerMono = EventUtility.buildGuildTargetOwner(event);

        // 2. Fetch the user's settings to determine if the reply should be ephemeral.
        Mono<Boolean> useEphemeralMono = getEphemeralSetting(apiClient);

        // 3. Combine the target owner and the ephemeral setting into a single OwnerContext.
        return Mono.zip(targetOwnerMono, useEphemeralMono)
                .map(tuple -> new OwnerContext(
                        tuple.getT1().ownerId(),
                        tuple.getT1().ownerName(),
                        tuple.getT1().ownerType(),
                        tuple.getT2()
                ));
    }

}