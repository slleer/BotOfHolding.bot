package com.botofholding.bot.Utility;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public final class ReplyUtility {

    private ReplyUtility() {}

    /**
     * Sends a reply that may consist of multiple messages if the content is too long.
     * The first message is sent as a direct reply, and subsequent messages are sent as follow-ups.
     *
     * @param event The interaction event to reply to.
     * @param messages The list of message strings to send.
     * @param isEphemeral Whether the reply and follow-ups should be ephemeral.
     * @return A Mono<Void> that completes when all messages have been sent.
     */
    public static Mono<Void> sendMultiPartReply(ChatInputInteractionEvent event, List<String> messages, boolean isEphemeral) {
        if (messages == null || messages.isEmpty()) {
            return event.reply("An unexpected error occurred, and there is no message to display.").withEphemeral(true).then();
        }

        Mono<Void> replyMono = event.reply(messages.get(0)).withEphemeral(isEphemeral);

        return messages.size() > 1 ? replyMono.thenMany(Flux.fromIterable(messages.subList(1, messages.size()))
                .concatMap(message -> event.createFollowup(message).withEphemeral(isEphemeral))).then() : replyMono;
    }
}