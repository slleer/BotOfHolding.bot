package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public interface ByNameParser extends Parser, EphemeralSettingProvider {

    @Override
    default Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        LoggerFactory.getLogger(getClass())
                .info("Executing '{}' command for user {}", getContext(), EventUtility.getInvokingUserTag(event));

        //1. Build the context ONCE. This holds the ephemeral setting.
        Mono<OwnerContext> ownerContextMono = getOwnerContext(event, apiClient);
        // 2. Get the required 'name' option from the command.
        Mono<String> nameOptionMono = EventUtility.getOptionValueAsString(event, getSubCommandName(), CommandConstants.OPTION_NAME)
                .defaultIfEmpty("");
        // 3. The main reactive chain, which produces a final Reply object.
        Mono<Reply> replyMono = nameOptionMono
                .flatMap(nameOptionValue -> fetchAndFormat(nameOptionValue, event, apiClient, ownerContextMono));

        // 4. Use the Reply object to send the final message to Discord.
        return replyMono
                .flatMap(reply -> event.reply(reply.message()).withEphemeral(reply.isEphemeral()))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, event.getInteraction().getUser()))
                .then(); // The AOP aspect will handle all errors.
    }

    /**
     * A shared dispatcher that decides whether to fetch by ID or by name.
     */
    default Mono<Reply> fetchAndFormat(String nameOptionValue, ChatInputInteractionEvent event, ApiClient apiClient, Mono<OwnerContext> ownerContextMono) {
        String[] parts = nameOptionValue.split(":", 2);

        if (parts.length == 2) {
            try {
                Long objectId = Long.parseLong(parts[1]);
                // Delegate to the specific implementation for fetching by ID
                return fetchByIdAndFormat(objectId, apiClient, ownerContextMono);
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(ByNameParser.class)
                        .warn("Value '{}' contained a colon but was not a valid ID. Searching by full name.", nameOptionValue);
            }
        }
        // Delegate to the specific implementation for fetching by name
        return fetchByNameAndFormat(event, nameOptionValue, apiClient, ownerContextMono);
    }


    /**
     * Defines how the OwnerContext is built, accommodating the differences
     * between different types of commands.
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

    Mono<Reply> fetchByIdAndFormat(Long objectId, ApiClient apiClient, Mono<OwnerContext> ownerContextMono);
    Mono<Reply> fetchByNameAndFormat(ChatInputInteractionEvent event, String objectName, ApiClient apiClient, Mono<OwnerContext> ownerContextMono);
}
