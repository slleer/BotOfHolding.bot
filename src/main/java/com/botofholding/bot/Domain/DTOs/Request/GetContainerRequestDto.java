package com.botofholding.bot.Domain.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetContainerRequestDto {
    private String containerName;
    private Long ownerDiscordId;
    private String ownerType;
}
