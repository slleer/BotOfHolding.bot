package com.botofholding.bot.SlashCommands.Parsers.Container;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.Entities.Reply;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.SlashCommands.Parsers.ContainerParser;
import com.botofholding.bot.SlashCommands.Parsers.OwnerContextProvider;
import com.botofholding.bot.Utility.CommandConstants;
import com.botofholding.bot.Utility.EventUtility;
import com.botofholding.bot.Utility.ReplyUtility;
import com.botofholding.bot.Utility.MessageFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DeleteContainerParser implements ContainerParser, OwnerContextProvider {
    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_CONTAINER_DELETE;
    }

    @Override
    public String getContext() {
        return CommandConstants.CONTEXT_CONTAINER_DELETE;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient) {

        Mono<OwnerContext> ownerContextMono = getOwnerContext(event, apiClient);
        Mono<String> containerToDelete = EventUtility.getOptionValueAsString(event, getSubCommandName(), CommandConstants.OPTION_NAME);
        Mono<Long> containerIdMono = EventUtility.getOptionValueAsLong(event, getSubCommandName(), CommandConstants.OPTION_ID);

        Mono<Reply> replyMono = Mono.zip(ownerContextMono, containerToDelete, containerIdMono)
                .flatMap(tuple -> {
                    OwnerContext context = tuple.getT1();
                    String containerName = tuple.getT2();
                    Long containerId = tuple.getT3();

                    return apiClient.deleteContainer(containerName, containerId, context.ownerId(), context.ownerType(), context.ownerName())
                            .map(deletedDto -> new Reply(MessageFormatter.formatDeletedEntityReply(deletedDto), context.useEphemeral()));
                });

        return replyMono
                .flatMap(reply -> ReplyUtility.sendMultiPartReply(event, reply.message(), reply.isEphemeral()))
                .contextWrite(ctx -> EventUtility.addUserContext(ctx, EventUtility.getInvokingUser(event)))
                .then();
    }
}
