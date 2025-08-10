package com.botofholding.bot.Domain.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContainerItemSummaryDto {
    private Long containerItemId;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private String userNote;
    private LocalDateTime lastModified;
    private List<ContainerItemSummaryDto> children;
    private ContainerItemSummaryDto parent;
}