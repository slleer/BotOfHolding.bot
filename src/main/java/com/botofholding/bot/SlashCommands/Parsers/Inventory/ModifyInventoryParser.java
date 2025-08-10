package com.botofholding.bot.SlashCommands.Parsers.Inventory;

import com.botofholding.bot.Domain.DTOs.Request.ModifyItemRequestDto;
import com.botofholding.bot.Domain.Entities.AutocompleteSelection;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.InventoryParser;
import com.botofholding.bot.SlashCommands.Parsers.RequestBodyParser;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.MessageFormatter;
import com.botofholding.bot.Utility.ReplyUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ModifyInventoryParser implements InventoryParser, RequestBodyParser<ModifyItemRequestDto> {

    private static final Logger logger = LoggerFactory.getLogger(ModifyInventoryParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_MODIFY;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_INVENTORY_MODIFY;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        Mono<ModifyItemRequestDto> requestDtoMono = buildRequestDto(event);
        Mono<Boolean> ephemeralMono = getEphemeralSetting(apiClient);

        Mono<Reply> replyMono = Mono.zip(requestDtoMono, ephemeralMono)
                .flatMap(tuple -> {
                    ModifyItemRequestDto requestDto = tuple.getT1();
                    boolean useEphemeral = tuple.getT2();
                    return apiClient.modifyItemInActiveContainer(requestDto)
                            .map(payload -> new Reply(MessageFormatter.formatModifyInventoryContainerReply(payload.data(), payload.message()), useEphemeral));
                });
        logger.info("Replying after apiClient call in ModifyInventoryParser");
        return replyMono
                .flatMap(reply -> ReplyUtility.sendMultiPartReply(event, reply.message(), reply.isEphemeral()))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }

    @Override
    public Mono<ModifyItemRequestDto> buildRequestDto(ChatInputInteractionEvent event) {
        Mono<AutocompleteSelection> selectionMono = EventUtility.getAutocompleteSelection(event, getSubCommandName(), CommandConstants.OPTION_ITEM);
        Mono<Optional<String>> noteMono = EventUtility.getOptionValueAsString(event, getSubCommandName(), CommandConstants.OPTION_NOTE).map(Optional::of);
//        Mono<Optional<Integer>> quantityMono = EventUtility.getOptionValueAsLong(event, getSubCommandName(), CommandConstants.OPTION_QUANTITY)
//                .map(Long::intValue).map(Optional::of);
        Mono<Boolean> moveToRootMono = Mono.just(EventUtility.getOptionValue(event, getSubCommandName(), CommandConstants.OPTION_INVENTORY_MODIFY_MOVE_TO_ROOT).isPresent());
        Mono<AutocompleteSelection> moveInsideMono = EventUtility.getAutocompleteSelection(event, getSubCommandName(), CommandConstants.OPTION_INVENTORY_MODIFY_MOVE_INSIDE)
                .defaultIfEmpty(new AutocompleteSelection("", null));

        return Mono.zip(selectionMono, noteMono, moveToRootMono, moveInsideMono)
                .flatMap(tuple -> {
                    AutocompleteSelection selection = tuple.getT1();
                    Optional<String> note = tuple.getT2();
                    boolean moveToRoot = tuple.getT3();
                    AutocompleteSelection moveInside = tuple.getT4();
                    ModifyItemRequestDto dto = new ModifyItemRequestDto();
                    dto.setContainerItemId(selection.id());
                    dto.setContainerName(selection.name());
                    note.ifPresent(dto::setNote);
                    dto.setMoveToRoot(moveToRoot);
                    dto.setNewParentId(moveInside.id());
                    dto.setNewParentName(moveInside.name());
                    logger.debug("selection: {}, note: {}, moveToRoot: {}, moveInside: {}", selection, note, moveToRoot, moveInside);
                    return Mono.just(dto);
                });
    }
}
