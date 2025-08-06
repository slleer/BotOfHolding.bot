// Create a new file: bot/src/main/java/com/botofholding/bot/Exception/AutoCompleteExceptionHandlerAspect.java
package com.botofholding.bot.Exception;

import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Component
public class AutoCompleteExceptionHandlerAspect {

    private static final Logger logger = LoggerFactory.getLogger(AutoCompleteExceptionHandlerAspect.class);

    /**
     * This pointcut targets the `handle` method of any class that implements
     * the AutoCompleteProvider interface.
     */
    @Pointcut("execution(* com.botofholding.bot.AutoComplete.Providers.AutoCompleteProvider+.handle(..))")
    public void autoCompleteHandlePointcut() {}

    /**
     * This "Around" advice wraps the targeted method call, providing centralized
     * and graceful error handling for all autocomplete providers.
     */
    @Around("autoCompleteHandlePointcut()")
    public Object handleAutoCompleteExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        ChatInputAutoCompleteEvent event = findArgument(joinPoint, ChatInputAutoCompleteEvent.class);
        if (event == null) {
            logger.error("Could not find ChatInputAutoCompleteEvent in method signature for {}. Cannot apply exception handling.", joinPoint.getSignature().toShortString());
            return joinPoint.proceed();
        }

        Mono<Void> providerMono = (Mono<Void>) joinPoint.proceed();

        // Attach the centralized error handler.
        return providerMono.onErrorResume(error -> {
            String commandName = event.getCommandName();
            String optionName = event.getFocusedOption().getName();
            String userTag = EventUtility.getInvokingUserTag(event);

            logger.error("Error fetching autocomplete suggestions for command '{}', option '{}' for user {}: {}",
                    commandName,
                    optionName,
                    userTag,
                    error.getMessage(),
                    error);

            // For autocomplete, the safest and best UX is to simply return nothing.
            // This prevents the interaction from failing and just shows no suggestions.
            return Mono.empty();
        });
    }

    private <T> T findArgument(ProceedingJoinPoint joinPoint, Class<T> type) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElse(null);
    }
}