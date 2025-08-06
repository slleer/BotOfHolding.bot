package com.botofholding.bot.AutoComplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import reactor.core.publisher.Mono;

/**
 * Represents a top-level command that can handle autocomplete events.
 */
public interface AutoCompleteCommand {
    String getName();
    Mono<Void> handle(ChatInputAutoCompleteEvent event);
}