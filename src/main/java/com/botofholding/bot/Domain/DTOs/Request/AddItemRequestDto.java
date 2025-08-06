package com.botofholding.bot.Domain.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddItemRequestDto {
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private String userNote;
    private Integer quantity;
    private Long insideId;
    private String insideName;
}