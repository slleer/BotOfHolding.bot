package com.botofholding.bot.AutoComplete;

import com.botofholding.bot.AutoComplete.Providers.AutoCompleteProvider;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;


import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract dispatcher that routes an autocomplete event to the correct provider
 * based on the focused option's name. This mirrors the SubcommandDispatcherCommand pattern.
 */
public abstract class AutoCompleteDispatcher<P extends AutoCompleteProvider> implements AutoCompleteCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final String commandName;
    private final Map<String, P> providerMap;

    protected AutoCompleteDispatcher(String commandName, Collection<P> providers) {
        this.commandName = commandName;
        // Build a map for efficient, direct lookups. The key is a composite of "subcommand:option".
        this.providerMap = providers.stream()
                .peek(provider -> logger.info(
                        "Registering AutoCompleteProvider: {} for key: '{}'",
                        provider.getClass().getSimpleName(),
                        (provider.getSubCommandName() + ":" + provider.getOptionName()).toLowerCase()
                ))
                .collect(Collectors.toMap(
                        provider -> (provider.getSubCommandName() + ":" + provider.getOptionName()).toLowerCase(),
                        provider -> provider,
                        // This merge function handles cases where two providers accidentally register for the same key.
                        (existing, replacement) -> {
                            logger.warn("Duplicate AutoCompleteProvider registration for subcommand '{}' and option '{}'. Using {} and ignoring {}.",
                                    existing.getSubCommandName(),
                                    existing.getOptionName(),
                                    existing.getClass().getSimpleName(),
                                    replacement.getClass().getSimpleName());
                            return existing;
                        }
                ));
    }

    @Override
    public String getName() {
        return this.commandName;
    }

    @Override
    public Mono<Void> handle(ChatInputAutoCompleteEvent event) {

        String subCommandName = EventUtility.extractSubcommandName(event.getOptions()).orElse("");

        String optionName = event.getFocusedOption().getName();
        String lookupKey = (subCommandName + ":" + optionName).toLowerCase();

        logger.debug("Dispatching autocomplete for command '{}', lookup key: '{}'", commandName, lookupKey);

        AutoCompleteProvider provider = providerMap.get(lookupKey);

        if (provider != null) {
            logger.debug("Found provider {} for key '{}'", provider.getClass().getSimpleName(), lookupKey);
            return provider.handle(event);
        } else {
            logger.warn("No AutoCompleteProvider found for command '{}' with key '{}'", commandName, lookupKey);
            return Mono.empty();
        }
    }
}