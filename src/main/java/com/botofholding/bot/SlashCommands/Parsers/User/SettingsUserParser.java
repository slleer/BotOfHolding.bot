package com.botofholding.bot.SlashCommands.Parsers.User;

import com.botofholding.bot.Domain.DTOs.Request.UserSettingsUpdateRequestDto;
import com.botofholding.bot.SlashCommands.Parsers.RequestBodyParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.UserParser;
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
public class SettingsUserParser implements UserParser, RequestBodyParser<UserSettingsUpdateRequestDto> {

    private final static Logger logger = LoggerFactory.getLogger(SettingsUserParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_USER_SETTINGS;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_USER_SETTINGS;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        logger.info("Executing '{}' command for user {}", getContext(), EventUtility.getInvokingUserTag(event));

        return buildRequestDto(event)
                .flatMap(apiClient::updateMySettings)
                .flatMap(apiResponse -> event
                        .reply(MessageFormatter.formatSettingsUpdateReply(apiResponse))
                        .withEphemeral(extractEphemeralSetting(apiResponse)))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }

    @Override
    public Mono<UserSettingsUpdateRequestDto> buildRequestDto(ChatInputInteractionEvent event) {

        Mono<Optional<Boolean>> ephemeralContainerMono
                = getOptionalSetting(event, CommandConstants.OPTION_USER_SETTINGS_HIDE_CONTAINER);
        Mono<Optional<Boolean>> ephemeralUserMono
                = getOptionalSetting(event, CommandConstants.OPTION_USER_SETTINGS_HIDE_USER);
        Mono<Optional<Boolean>> ephemeralItemMono
                = getOptionalSetting(event, CommandConstants.OPTION_USER_SETTINGS_HIDE_ITEM);

        return Mono.zip(ephemeralContainerMono, ephemeralUserMono, ephemeralItemMono)
                .map(tuple -> {
                    UserSettingsUpdateRequestDto dto = new UserSettingsUpdateRequestDto();
                    tuple.getT1().ifPresent(dto::setEphemeralContainer);
                    tuple.getT2().ifPresent(dto::setEphemeralUser);
                    tuple.getT3().ifPresent(dto::setEphemeralItem);
                    return dto;
                });
    }

    private Mono<Optional<Boolean>> getOptionalSetting(ChatInputInteractionEvent event, String optionName) {
        return Mono.just(EventUtility.getOptionValueAsOptionalBoolean(event, getSubCommandName(), optionName));
    }
}
