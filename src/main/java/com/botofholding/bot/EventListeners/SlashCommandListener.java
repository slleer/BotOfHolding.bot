package com.botofholding.bot.EventListeners;

import com.botofholding.bot.SlashCommands.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class SlashCommandListener implements EventListener<ChatInputInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);
    private final Collection<SlashCommand> commands;

    public SlashCommandListener(Collection<SlashCommand> commands) {
        this.commands = commands;
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next() // Get the first (and hopefully only) matching command
                .flatMap(command -> command.execute(event)
                        .onErrorResume(error -> {
                            // Log that an error occurred in a command's execution
                            logger.warn("Command '{}' execution failed for user {}. Delegating to command's error handler.",
                                    command.getName(),
                                    event.getInteraction().getUser().getUsername(),
                                    error); // Log the original error
                            // Delegate to the command's own error handler, which now has the event
                            return command.handleErrors(event, error);
                        })
                );
    }

    @Override
    public Mono<Void> handleErrors(Throwable error) {
        logger.error("A top-level unhandled error occurred within SlashCommandListener processing: {}",
                error.getMessage(),
                error);
        // At this stage, replying to a specific event might be difficult or impossible
        // as we might not have the event context if the error was very early in the stream.
        return Mono.empty(); // Logged, and then we complete the reactive chain.
    }
}
