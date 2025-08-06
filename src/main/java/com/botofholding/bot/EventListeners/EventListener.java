package com.botofholding.bot.EventListeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

import java.io.StringWriter;


public interface EventListener<T extends Event> {

    Class<T> getEventType();

    Mono<Void> execute(T event);

    Mono<Void> handleErrors(Throwable error);
}
