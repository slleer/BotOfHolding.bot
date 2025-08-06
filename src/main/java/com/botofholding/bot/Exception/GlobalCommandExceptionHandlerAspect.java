package com.botofholding.bot.Exception;

import com.botofholding.bot.SlashCommands.Parsers.Parser;
import com.botofholding.bot.Utility.EventUtility;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
public class GlobalCommandExceptionHandlerAspect {

    private static final Logger logger = LoggerFactory.getLogger(GlobalCommandExceptionHandlerAspect.class);

    /**
     * This pointcut targets the `execute` method of any class that implements
     * the `Parser` interface.
     * This is a robust way to apply this logic to all your command parsers.
     */
    @Pointcut("execution(* com.botofholding.bot.SlashCommands.Parsers.Parser+.execute(..))")
    public void commandExecutionPointcut() {}

    /**
     * This "Around" advice wraps the targeted method call. It allows us to
     * execute the original command logic and then attach our centralized
     * error handling to the resulting Mono.
     */
    @Around("commandExecutionPointcut()")
    public Object handleCommandExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. Find the ChatInputInteractionEvent from the method arguments.
        ChatInputInteractionEvent event = findArgument(joinPoint, ChatInputInteractionEvent.class);
        if (event == null) {
            logger.error("Could not find ChatInputInteractionEvent in method signature for {}. Cannot apply exception handling.", joinPoint.getSignature().toShortString());
            return joinPoint.proceed(); // Proceed without our handler
        }

        // 2. Execute the original command's `execute` method.
        Mono<Void> commandMono = (Mono<Void>) joinPoint.proceed();

        // 3. Attach our SINGLE, CENTRALIZED error handler.
        return commandMono.onErrorResume(error -> {
            Parser parser = (Parser) joinPoint.getTarget();
            String context = parser.getContext();
            String userTag = EventUtility.getInvokingUserTag(event);

            logger.error("Global Error Handler caught an exception during '{}' command for user {}: {}",
                    context,
                    userTag,
                    error.getMessage(),
                    error);

            // Delegate message generation to the parser's default method.
            String userFacingMessage = parser.generateUserFacingErrorMessage(error);

            // Send the determined message as an ephemeral reply.
            return event.reply(userFacingMessage)
                    .withEphemeral(true)
                    .onErrorResume(replyError -> {
                        // This handles the critical case where we can't even send the error message.
                        logger.error("CRITICAL: Failed to send error reply to user {} for '{}' command: {}",
                                userTag,
                                context,
                                replyError.getMessage());
                        return Mono.empty(); // Swallow the error to prevent a crash loop.
                    })
                    .then();
        });
    }

    /**
     * Helper method to safely extract a specific argument type from the join point.
     */
    private <T> T findArgument(ProceedingJoinPoint joinPoint, Class<T> type) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElse(null);
    }
}