package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.Entities.OwnerContext;
import com.botofholding.bot.Domain.DTOs.Response.UserSettingsDto;
import com.botofholding.bot.Service.ApiClient;
import reactor.core.publisher.Mono;

/**
 * A reusable "capability" interface for any parser that needs to fetch a
 * user's personal ephemeral reply setting from the API.
 */
public interface EphemeralSettingProvider {


    /**
     * Defines how to get the correct boolean flag from the user's settings.
     *
     * @param settings The DTO containing all user settings.
     * @return The specific boolean value for whether replies should be ephemeral.
     */
    boolean extractEphemeralSetting(UserSettingsDto settings);

    /**
     * A reusable helper to fetch settings and extract the correct boolean flag.
     */
    default Mono<Boolean> getEphemeralSetting(ApiClient apiClient) {
        return apiClient.getMySettings().map(this::extractEphemeralSetting);
    }

    /**
     * A centralized helper to determine if a reply should be ephemeral based on a
     * consistent business rule: guild-owned entity replies are always public.
     * @param entityOwnerType The owner type from the DTO (e.g., "GUILD", "USER").
     * @param userEphemeralSetting The user's ephemeral setting.
     * @return true if the reply should be ephemeral, false otherwise.
     */
    default boolean isReplyEphemeral(String entityOwnerType, boolean userEphemeralSetting) {
        return !"GUILD".equalsIgnoreCase(entityOwnerType) && userEphemeralSetting;
    }
}