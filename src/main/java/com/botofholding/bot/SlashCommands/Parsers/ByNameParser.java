package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.ReplyUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public interface ByNameParser extends Parser, OwnerContextProvider {

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
                .flatMap(reply -> ReplyUtility.sendMultiPartReply(event, reply.message(), reply.isEphemeral()))
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

    Mono<Reply> fetchByIdAndFormat(Long objectId, ApiClient apiClient, Mono<OwnerContext> ownerContextMono);
    Mono<Reply> fetchByNameAndFormat(ChatInputInteractionEvent event, String objectName, ApiClient apiClient, Mono<OwnerContext> ownerContextMono);
}
