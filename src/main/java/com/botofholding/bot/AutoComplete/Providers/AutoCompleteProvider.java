package com.botofholding.bot.AutoComplete.Providers;

import com.botofholding.bot.Domain.DTOs.Request.AutoCompleteRequestDto;
import com.botofholding.bot.Domain.DTOs.Response.AutoCompleteDto;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public interface AutoCompleteProvider {

    String getSubCommandName();

    String getOptionName();

    /**
     * This method is responsible for calling the correct back-end API endpoint.
     *
     * @param request The standardized request DTO containing the search prefix and owner context.
     * @return A Mono emitting a list of DTOs from the API.
     */
    Mono<List<AutoCompleteDto>> fetchSuggestions(AutoCompleteRequestDto request);

    default Mono<Void> handle(ChatInputAutoCompleteEvent event) {
        LoggerFactory.getLogger(getClass())
                .debug("Fetching autocomplete suggestions for command '{}', option '{}' for user {}",
                        getSubCommandName(),
                        getOptionName(),
                        EventUtility.getInvokingUserTag(event));

        return buildRequestDto(event)
                .flatMap(this::fetchSuggestions) // Calls the concrete implementation
                .flatMap(response -> {
                    List<ApplicationCommandOptionChoiceData> suggestions = response.stream()
                            .limit(CommandConstants.DISCORD_CHOICE_LIMIT)
                            .map(dto -> ApplicationCommandOptionChoiceData
                                    .builder()
                                    .name(buildOptionName(dto))
                                    .value(buildOptionValue(dto))
                                    .build()
                            ).collect(Collectors.toList());
                    return event.respondWithSuggestions(suggestions);
                })
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }

    /**
     * Provides a default implementation for building the standard autocomplete request DTO.
     */
    default Mono<AutoCompleteRequestDto> buildRequestDto(ChatInputAutoCompleteEvent event) {
        String currentInput = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        return EventUtility.buildGuildTargetOwner(event).map(targetOwner ->
                new AutoCompleteRequestDto(currentInput, targetOwner)
        );
    }

    /**
     * Provides a default, reusable implementation for building the display name
     * for an autocomplete choice using the pattern '<label> - (<description>)'.
     * or just <label> if there's no description.
     */
    default String buildOptionName(AutoCompleteDto dto) {
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            return String.format("%s - (%s)", dto.getLabel(), dto.getDescription());
        }
        // Fallback to just the label if there's no description.
        return dto.getLabel();
    }

    /**
     * Provides a default, reusable implementation for building the value
     * for an autocomplete choice using the pattern '<label>:<id>'.
     */
    default String buildOptionValue(AutoCompleteDto dto) {
        return String.format("%s:%d",
                dto.getLabel(),
                dto.getId());
    }
}
