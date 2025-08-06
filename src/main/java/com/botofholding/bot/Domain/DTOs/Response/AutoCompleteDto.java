package com.botofholding.bot.Domain.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteDto {
    private Long id;
    private String label;
    private String description;
}
