package com.botofholding.bot.SlashCommands.Parsers.User;


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


@Component
public class GetUserParser implements UserParser {

    private static final Logger logger = LoggerFactory.getLogger(GetUserParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_USER_GET;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_USER_GET;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        logger.info("Executing '{}' command for user {}", getContext(), EventUtility.getInvokingUserTag(event));

        Mono<Boolean> userEphemeralMono = getEphemeralSetting(apiClient);

        Mono<String> replyMono = apiClient.getMyProfile()
                .map(payload -> {
                    logger.info("Successfully retrieved user data for Discord ID: {} with message '{}'.", payload.data().getDiscordId(), payload.message());
                    return MessageFormatter.formatUserReply(payload.data(), payload.message());
                });

        return Mono.zip(userEphemeralMono, replyMono)
                .flatMap(tuple -> {
                    boolean isEphemeral = tuple.getT1();
                    String message = tuple.getT2();
                    return event.reply(message).withEphemeral(isEphemeral);
                })
                // [CRITICAL] This is where we add the user's info to the context.
                // This must be done for any chain that calls the ApiClient.
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, event.getInteraction().getUser()))
                .then();
    }
}
