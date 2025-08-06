package com.botofholding.bot.SlashCommands.Parsers;

import com.botofholding.bot.Domain.DTOs.Response.UserSettingsDto;

/**
 * The primary interface for all parsers that interact with Containers.
 */
public interface ContainerParser extends Parser, EphemeralSettingProvider {

    @Override
    default boolean extractEphemeralSetting(UserSettingsDto settings) {
        return settings.isEphemeralContainer();
    }
}
