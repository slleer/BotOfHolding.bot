package com.botofholding.bot.SlashCommands.Parsers.Item;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Exception.ReplyException;
import com.botofholding.bot.SlashCommands.Parsers.ByNameParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.ItemParser;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.MessageFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FindItemParser implements ItemParser, ByNameParser {

    private static final Logger logger = LoggerFactory.getLogger(FindItemParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_ITEM_FIND;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_ITEM_FIND;
    }

    @Override
    public Mono<Reply> fetchByIdAndFormat(Long objectId, ApiClient apiClient, Mono<OwnerContext> ownerContextMono) {
        logger.debug("Autocomplete value detected. Fetching item by ID: {}", objectId);
        return ownerContextMono.flatMap(owner ->
                apiClient.getItemById(objectId, owner.ownerId(), owner.ownerType(), owner.ownerName())
                        .map(MessageFormatter::formatGetItemReply)
                        .map(message -> new Reply(message, owner.useEphemeral()))
        );
    }

    @Override
    public Mono<Reply> fetchByNameAndFormat(ChatInputInteractionEvent event, String objectName, ApiClient apiClient, Mono<OwnerContext> ownerContextMono) {
        logger.debug("Manual input detected. Searching for item by name: {}", objectName);
        return ownerContextMono.flatMap(owner ->
                apiClient.findItems(objectName, owner.ownerId(), owner.ownerType(), owner.ownerName())
                        .map(itemList -> {
                            if (itemList.isEmpty()) {
                                throw new ReplyException("Could not find an item with that name.");
                            }
                            return MessageFormatter.formatItemReply(itemList);
                        })
                        .map(message -> new Reply(message, owner.useEphemeral()))
        );
    }
}
