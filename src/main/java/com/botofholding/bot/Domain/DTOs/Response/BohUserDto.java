package com.botofholding.bot.Domain.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BohUserDto {
    private Long discordId;
    private String bohUserName;
    private String bohUserTag;
    private String bohGlobalUserName;
    private LocalDateTime lastActive;

    public String getDisplayName() {
        return Optional.ofNullable(bohGlobalUserName).orElse(bohUserName);
    }
}
        