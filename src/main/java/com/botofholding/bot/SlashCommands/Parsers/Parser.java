package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.DTOs.Response.StandardApiResponse;
import com.botofholding.bot.Exception.ApiException;
import com.botofholding.bot.Exception.ReplyException;
import com.botofholding.bot.Service.ApiClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

public interface Parser {

    String getSubCommandName();

    String getContext();


    Mono<Void> execute(ChatInputInteractionEvent event, ApiClient apiClient);

    /**
     * A centralized method to generate a user-friendly error message from any exception.
     * This is the single source of truth for all user-facing error messages.
     * @param error The exception that was thrown.
     * @return A user-facing error message string.
     */
    default String generateUserFacingErrorMessage(Throwable error) {
        // 1. Handle custom, user-facing exceptions first.
        // These messages are designed to be shown directly to the user.
        if (error instanceof ApiException || error instanceof ReplyException) {
            return "❌ " + error.getMessage();
        }

        // 2. Handle API HTTP error responses (e.g., 404, 500).
        if (error instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) error;
            try {
                // Attempt to parse the structured error response from the API
                ParameterizedTypeReference<StandardApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
                StandardApiResponse<Object> apiResponse = ex.getResponseBodyAs(responseType);

                if (apiResponse != null && apiResponse.getMessage() != null && !apiResponse.getMessage().isBlank()) {
                    return "❌ " + apiResponse.getMessage();
                }
                // Fallback if parsing works but message is empty
                return "An API error occurred (Status: " + ex.getStatusCode().value() + ").";
            } catch (Exception parseError) {
                // Fallback if the response body can't be parsed as a StandardApiResponse
                if (ex.getStatusCode().is5xxServerError()) {
                    return "Sorry, I'm having trouble communicating with my services right now. Please try again in a moment.";
                }
                return "An unexpected API error occurred (Status: " + ex.getStatusCode().value() + ").";
            }
        }

        // 3. Handle low-level network connection errors.
        if (error.getCause() instanceof ConnectException) {
            return "Could not connect to the Bot-O-Holding API. The service may be temporarily unavailable.";
        }

        // 4. Fallback for any other unexpected errors (bugs).
        return "An unexpected internal error occurred. The developers have been notified.";
    }
}
