package com.botofholding.bot.Utility;

import com.botofholding.bot.Domain.Enum.OwnerType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface OwnerTypeExtractor {
    /**
     * A strategy for extracting an OwnerType from a command option.
     * @return A Mono emitting the determined OwnerType, or empty if not found.
     */
    Mono<OwnerType> extract(ChatInputInteractionEvent event, String subcommandName, String optionName);
}