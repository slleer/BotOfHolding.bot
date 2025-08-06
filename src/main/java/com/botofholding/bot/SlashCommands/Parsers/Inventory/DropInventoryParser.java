package com.botofholding.bot.SlashCommands.Parsers.Inventory;

import com.botofholding.bot.Domain.Entities.AutocompleteSelection;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.InventoryParser;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.MessageFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class DropInventoryParser implements InventoryParser {

    private static final Logger logger = LoggerFactory.getLogger(DropInventoryParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_DROP;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_INVENTORY_DROP;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        logger.info("Attempting to drop item from active container");
        // 1. Get the item selection using our new centralized utility method.
        Mono<AutocompleteSelection> selectionMono = EventUtility.getAutocompleteSelection(event, getSubCommandName(), CommandConstants.OPTION_ITEM);

        // 2. Get the quantity to drop, defaulting to 1.
        Mono<Integer> quantityMono = EventUtility.getOptionValueAsLong(event, getSubCommandName(), CommandConstants.OPTION_QUANTITY)
                .map(Long::intValue)
                .defaultIfEmpty(1);

        Mono<Boolean> dropChildrenMono = Mono.just(EventUtility.getOptionValueAsOptionalBoolean(event, getSubCommandName(), CommandConstants.OPTION_DROP_CHILDREN).orElse(false));

        // 4. Get the user's ephemeral setting for replies.
        Mono<Boolean> ephemeralMono = getEphemeralSetting(apiClient);


        Mono<Reply> replyMono = Mono.zip(selectionMono, quantityMono, dropChildrenMono, ephemeralMono)
                .flatMap(tuple -> {
                    AutocompleteSelection selection = tuple.getT1();
                    Integer quantity = tuple.getT2();
                    Boolean dropChildren = tuple.getT3();
                    boolean useEphemeral = tuple.getT4();
                    logger.info("Making call to apiClient.dropItemFromActiveContainer with values: id={}, name='{}', quantity={}.", selection.id(), selection.name(), quantity);
                    return apiClient.dropItemFromActiveContainer(selection.id(), selection.name(), dropChildren, quantity)
                            .map(updatedContainer -> new Reply(MessageFormatter.formatDropInventoryContainerReply(updatedContainer, selection.name(), quantity), useEphemeral));
                });
        return replyMono.flatMap(reply -> event.reply(reply.message()).withEphemeral(reply.isEphemeral()))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }
}
