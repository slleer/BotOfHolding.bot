package com.botofholding.bot.Domain.DTOs.Request;

import com.botofholding.bot.Domain.Entities.TargetOwner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteRequestDto {
    String prefix;
    TargetOwner targetOwner;
}
