package com.botofholding.bot.SlashCommands.Parsers.User;


import com.botofholding.bot.Domain.DTOs.Request.UserRequestDto;
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
public class UpdateUserParser implements UserParser, RequestBodyParser<UserRequestDto> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_USER_UPDATE;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_USER_UPDATE;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        logger.info("Executing '{}' command for user {}", getContext(), EventUtility.getInvokingUserTag(event));

        Mono<Boolean> userEphemeralMono = getEphemeralSetting(apiClient);

        Mono<String> replyMono = buildRequestDto(event) // This now returns Mono<UserRequestDto>
                .flatMap(apiClient::updateMyProfile)
                .map(apiResponse -> MessageFormatter.formatUserReply(apiResponse, "Updated"));

        return Mono.zip(userEphemeralMono, replyMono)
                .flatMap(tuple -> {
                    boolean isEphemeral = tuple.getT1();
                    String message = tuple.getT2();
                    return event.reply(message).withEphemeral(isEphemeral);
                })
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }

    @Override
    public Mono<UserRequestDto> buildRequestDto(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(EventUtility.getInvokingUser(event))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Error retrieving User from event")))
                .map(targetUser -> {
                    String username = targetUser.getUsername();
                    String userTag = targetUser.getTag();
                    Optional<String> userGlobalName = targetUser.getGlobalName();
                    return new UserRequestDto(username, userTag, userGlobalName);
                });
    }

}
