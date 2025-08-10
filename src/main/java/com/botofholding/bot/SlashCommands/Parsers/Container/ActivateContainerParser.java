package com.botofholding.bot.SlashCommands.Parsers.Container;

import com.botofholding.bot.Domain.DTOs.Response.ContainerSummaryDto;
import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.SlashCommands.Parsers.ByNameParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.ContainerParser;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.MessageFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ActivateContainerParser implements ContainerParser, ByNameParser {

    private static final Logger logger = LoggerFactory.getLogger(ActivateContainerParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_CONTAINER_ACTIVATE;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_CONTAINER_ACTIVATE;
    }

    // TODO rewrite this to use a single api call with id being a nullable parameter.
    //  Since they both return a single container on success there is no reason to have
    //  such complex logic, look at AddInventoryParser for example
    @Override
    public Mono<Reply> fetchByIdAndFormat(Long objectId, ApiClient apiClient, Mono<OwnerContext> ownerContextMono) {
        logger.debug("Autocomplete value detected. Fetching container by ID: {}", objectId);
        return ownerContextMono.flatMap(owner ->
                apiClient.useContainerById(objectId, owner.ownerId(), owner.ownerType(), owner.ownerName())
                        .map(container -> createReply(container, owner))
        );
    }

    /**
     * Fetches containers by name and formats the reply.
     */
    @Override
    public Mono<Reply> fetchByNameAndFormat(ChatInputInteractionEvent event, String objectName, ApiClient apiClient, Mono<OwnerContext> ownerContextMono) {
        logger.debug("Manual input detected. Searching for container by name.");
        Mono<String> ownerPriority = EventUtility.getOptionValueAsString(event, getSubCommandName(), CommandConstants.OPTION_PRIORITIZE);
        return Mono.zip(ownerContextMono, ownerPriority)
                .flatMap(tuple -> {
                    OwnerContext owner = tuple.getT1();
                    String ownerPriorityValue = tuple.getT2();
                    return apiClient.useContainerByName(objectName, ownerPriorityValue, owner.ownerId(), owner.ownerType(), owner.ownerName())
                            .map(container -> createReply(container, owner));
                }
        );
    }
    /**
     * A private helper to create a Reply, ensuring guild-owned container replies are always public.
     */
    private Reply createReply(ContainerSummaryDto container, OwnerContext owner) {
        List<String> message = MessageFormatter.formatActiveContainerReply(container);
        // Use the centralized helper to determine the ephemeral state.
        return new Reply(message, isReplyEphemeral(container.getOwnerType(), owner.useEphemeral()));
    }

}
