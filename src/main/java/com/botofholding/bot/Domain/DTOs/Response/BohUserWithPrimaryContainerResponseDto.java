package com.botofholding.bot.Domain.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BohUserWithPrimaryContainerResponseDto extends BohUserDto {
    private String primaryContainerName;
}
