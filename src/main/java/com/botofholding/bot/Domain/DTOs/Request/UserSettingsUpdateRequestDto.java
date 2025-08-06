package com.botofholding.bot.Domain.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsUpdateRequestDto {
    private Boolean ephemeralContainer;
    private Boolean ephemeralUser;
    private Boolean ephemeralItem;
}
