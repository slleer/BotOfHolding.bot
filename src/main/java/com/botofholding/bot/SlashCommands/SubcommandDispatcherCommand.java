package com.botofholding.bot.SlashCommands;

import com.botofholding.bot.SlashCommands.Parsers.Parser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SubcommandDispatcherCommand<P extends Parser> implements SlashCommand {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ApiClient apiClient;
    private final Map<String, P> subParsersMap;
    private final String commandName;

    protected SubcommandDispatcherCommand(String commandName, Collection<P> subParsers, ApiClient apiClient) {
        this.commandName = commandName;
        this.apiClient = apiClient;
        this.subParsersMap = subParsers.stream()
                .collect(Collectors.toMap(
                        parser -> parser.getSubCommandName().toLowerCase(),
                        parser -> parser,
                        // This merge function handles cases where two providers accidentally register for the same key.
                        (existing, replacement) -> {
                            logger.warn("Duplicate registration for subcommand '{}'. Using {} and ignoring {}.",
                                    existing.getSubCommandName(),
                                    existing.getClass().getSimpleName(),
                                    replacement.getClass().getSimpleName());
                            return existing;
                        }
                ));

    }

    @Override
    public String getName() {
        return commandName;
    }
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {

        Optional<String> subCommandNameOpt = EventUtility.extractSubcommandName(event.getOptions());

        if (subCommandNameOpt.isEmpty()) {
            logger.warn("{} command called without subcommands by user {}", commandName, EventUtility.getInvokingUserTag(event));
            return event.reply(getNoSubcommandMessage())
                    .withEphemeral(true);
        }

        String subCommandName = subCommandNameOpt.get();
        logger.debug("{} command invoked with subcommand: '{}' by user {}", commandName, subCommandName, event.getInteraction().getUser().getUsername());


        return Mono.justOrEmpty(subParsersMap.get(subCommandName.toLowerCase())) // Find the first matching parser
                .flatMap(parser -> {
                    logger.debug("Dispatching to parser: {} for command {} and subcommand {}",
                            parser.getClass().getSimpleName(), commandName, subCommandName);
                    // [FIX] We use .thenReturn(true) to transform the successful completion of the
                    // parser's Mono<Void> into a Mono<Boolean> that emits a value.
                    // This prevents the stream from being considered "empty" on success,
                    // which would incorrectly trigger the switchIfEmpty block.
                    return parser.execute(event, apiClient).thenReturn(true);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // This block executes if no parser was found for the subcommand.
                    logger.warn("No parser found for command '{}', subcommand '{}'", commandName, subCommandName);
                    return event.reply(getUnknownSubcommandMessage(subCommandName))
                            .withEphemeral(true)
                            .thenReturn(true); // Ensure this branch also emits to match types.
                }))
                .then(); // Finally, convert the Mono<Boolean> back to the required Mono<Void>.
    }

    @Override
    public Mono<Void> handleErrors(ChatInputInteractionEvent event, Throwable error) {
        // This is the central error handler for the dispatcher itself, called by SlashCommandListener.
        // Errors from individual parsers are handled by the GlobalCommandExceptionHandlerAspect.
        logger.error("{} command central error handler invoked for user {}: {}",
                commandName,
                EventUtility.getInvokingUserTag(event),
                error.getMessage(),
                error);

        return event.reply(getGenericErrorMessage())
                .withEphemeral(true)
                .onErrorResume(replyError -> { // Fallback if sending the error reply itself fails
                    logger.error("Critical: Failed to send error reply from {}'s handleErrors for user {}: {}",
                            commandName,
                            EventUtility.getInvokingUserTag(event), replyError.getMessage());
                    return Mono.empty(); // Absorb if even this fails
                });
    }

    protected String getNoSubcommandMessage() {
        return String.format("Please specify what you want to %s (e.g., /%s <subcommand> ...).", this.commandName, this.commandName);
    }

    protected String getUnknownSubcommandMessage(String subCommandName) {
        return String.format("Sorry, I don't know how to %s '%s'.", this.commandName, subCommandName);
    }

    protected String getGenericErrorMessage() {
        return String.format("An unexpected error occurred while processing the '%s' command. Please try again.", this.commandName);
    }
}
