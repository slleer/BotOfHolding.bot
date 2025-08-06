package com.botofholding.bot.SlashCommands.Parsers.Inventory;

import com.botofholding.bot.Domain.DTOs.Request.AddItemRequestDto;
import com.botofholding.bot.Domain.Entities.AutocompleteSelection;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.InventoryParser;
import com.botofholding.bot.SlashCommands.Parsers.RequestBodyParser;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.MessageFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddInventoryParser implements InventoryParser, RequestBodyParser<AddItemRequestDto> {

    private static final Logger logger = LoggerFactory.getLogger(AddInventoryParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_ADD;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_INVENTORY_ADD;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        // 1. Build the request DTO using our dedicated method.
        Mono<AddItemRequestDto> requestDtoMono = buildRequestDto(event);

        // 2. Build the target owner context.
        Mono<TargetOwner> targetOwnerMono = EventUtility.buildGuildTargetOwner(event);

        // 3. Get the user's ephemeral setting for replies.
        Mono<Boolean> ephemeralMono = getEphemeralSetting(apiClient);

        // 4. Combine the DTO and setting, execute the API call, and format the reply.
        Mono<Reply> replyMono = Mono.zip(requestDtoMono, targetOwnerMono, ephemeralMono)
                .flatMap(tuple -> {
                    AddItemRequestDto requestDto = tuple.getT1();
                    TargetOwner targetOwner = tuple.getT2();
                    boolean useEphemeral = tuple.getT3();
                    return apiClient.addItemToActiveContainer(requestDto, targetOwner.ownerId(), targetOwner.ownerType(), targetOwner.ownerName())
                            .map(updatedContainer -> new Reply(MessageFormatter.formatAddInventoryContainerReply(updatedContainer), useEphemeral));
                });

        logger.info("About to reply after completing apiClient call");
        // 5. Send the reply to the user. The GlobalCommandExceptionHandlerAspect will handle any errors.
        return replyMono
                .flatMap(reply -> event.reply(reply.message()).withEphemeral(reply.isEphemeral()))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }

    @Override
    public Mono<AddItemRequestDto> buildRequestDto(ChatInputInteractionEvent event) {
        // Use the new, centralized utility method to get the item selection.
        Mono<AutocompleteSelection> selectionMono = EventUtility.getAutocompleteSelection(event, getSubCommandName(), CommandConstants.OPTION_ITEM);

        // (optional) Item to put added item inside, such as containers
        Mono<AutocompleteSelection> insideMono = EventUtility.getAutocompleteSelection(event, getSubCommandName(), CommandConstants.OPTION_PARENT)
                .defaultIfEmpty(new AutocompleteSelection("", null));

        Mono<Integer> quantityMono = EventUtility.getOptionValueAsLong(event, getSubCommandName(), CommandConstants.OPTION_QUANTITY)
                .map(Long::intValue)
                .defaultIfEmpty(1);

        Mono<String> noteMono = EventUtility.getOptionValueAsString(event, getSubCommandName(), CommandConstants.OPTION_NOTE).defaultIfEmpty("");

        logger.info("About to finish the buildRequestDto method");
        return Mono.zip(selectionMono, quantityMono, noteMono, insideMono)
                .map(tuple ->
                        new AddItemRequestDto(tuple.getT1().id(), tuple.getT1().name(), null, tuple.getT3(), tuple.getT2(), tuple.getT4().id(), tuple.getT4().name()));
    }
}
