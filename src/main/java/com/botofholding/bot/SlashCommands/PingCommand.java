package com.botofholding.bot.SlashCommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PingCommand implements SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(PingCommand.class);

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // The primary action of the ping command is to reply.
        // If event.reply() itself fails, that error will propagate up the reactive chain.
        logger.info("Executing ping command for user {}", event.getInteraction().getUser().getUsername());
        return event.reply("Pong!")
                .doOnSuccess(v -> logger.info("Successfully replied to ping command for user {}",
                        event.getInteraction().getUser().getUsername()))
                .doOnError(e -> logger.error("Failed to reply during ping command execution for user {}: {}",
                        event.getInteraction().getUser().getUsername(), e.getMessage()));
    }

    @Override
    public Mono<Void> handleErrors(ChatInputInteractionEvent event, Throwable error) {

        // This method is now called by SlashCommandListener if execute() fails.
        // It has the event, so it CAN reply to the user.
        logger.error("Error during PingCommand execution for user {}: {}",
                event.getInteraction().getUser().getUsername(),
                error.getMessage(),
                error); // Log the full error with stack trace

        // Send a user-facing error message.
        return event.reply("Oops! The 'ping' command encountered an issue. Please try again later.")
                .withEphemeral(true);
    }
}
