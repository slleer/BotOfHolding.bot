package com.botofholding.bot.SlashCommands.Parsers.Container;

import com.botofholding.bot.Domain.DTOs.Response.UserSettingsDto;
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

@Component
public class FindActiveContainerParser implements ContainerParser {
    private static final Logger logger = LoggerFactory.getLogger(FindActiveContainerParser.class);

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_CONTAINER_FIND_ACTIVE;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_CONTAINER_FIND_ACTIVE;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {
        logger.info("Executing '{}' command for user {}", getContext(), EventUtility.getInvokingUserTag(event));
        Mono<Boolean> userEphemeralMono = getEphemeralSetting(apiClient);
        return userEphemeralMono
                .flatMap( userEphemeral ->
                        apiClient.getActiveContainer().flatMap(activeContainer -> {
                            String message = MessageFormatter.formatActiveContainerReply(activeContainer);
                            return event.reply(message).withEphemeral(userEphemeral);
                        })
                )
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, event.getInteraction().getUser()))
                .then();
    }
    @Override
    public boolean extractEphemeralSetting(UserSettingsDto settings) {
        return settings.isEphemeralContainer();
    }
}
