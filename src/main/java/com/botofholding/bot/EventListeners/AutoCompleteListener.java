package com.botofholding.bot.EventListeners;

import com.botofholding.bot.AutoComplete.AutoCompleteCommand;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class AutoCompleteListener implements EventListener<ChatInputAutoCompleteEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AutoCompleteListener.class);
    private final Collection<AutoCompleteCommand> commands;

    public AutoCompleteListener(Collection<AutoCompleteCommand> commands) {
        this.commands = commands;
    }

    @Override
    public Class<ChatInputAutoCompleteEvent> getEventType() {
        return ChatInputAutoCompleteEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        logger.info("AutoCompleteListener parsing command {} for user {}", event.getCommandName(), EventUtility.getInvokingUserTag(event));
        // Find the correct AutoCompleteCommand (dispatcher) for the event's command name.
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equalsIgnoreCase(event.getCommandName()))
                .next()
                .flatMap(command -> {
                    logger.debug("Dispatching autocomplete event for command '{}' to {}", event.getCommandName(), command.getClass().getSimpleName());
                    return command.handle(event);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("No AutoCompleteCommand found for command '{}'", event.getCommandName());
                    return Mono.empty();
                }))
                .onErrorResume(this::handleErrors);
    }

    @Override
    public Mono<Void> handleErrors(Throwable error) {
        logger.error("Unexpected error in autocomplete listener");
        return Mono.empty();
    }
}
