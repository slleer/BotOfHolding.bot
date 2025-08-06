package com.botofholding.bot.Domain.DTOs.Response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsDto {
    private Long id;
    private Long ownerDiscordId;
    private String ownerType;
    private boolean ephemeralContainer;
    private boolean ephemeralUser;
    private boolean ephemeralItem;
}
