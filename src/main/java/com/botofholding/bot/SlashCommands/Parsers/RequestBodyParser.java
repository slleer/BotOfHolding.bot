package com.botofholding.bot.SlashCommands.Parsers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface RequestBodyParser<DTO_TYPE>  {

    Mono<DTO_TYPE> buildRequestDto(ChatInputInteractionEvent event);
}
