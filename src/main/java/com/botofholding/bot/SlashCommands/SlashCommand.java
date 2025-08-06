package com.botofholding.bot.SlashCommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface SlashCommand {

    String getName();

    Mono<Void> execute(ChatInputInteractionEvent event);

    Mono<Void> handleErrors(ChatInputInteractionEvent event, Throwable error);
}
