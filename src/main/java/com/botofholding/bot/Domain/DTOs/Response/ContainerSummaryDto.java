package com.botofholding.bot.Domain.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContainerSummaryDto {
    private Long containerId;
    private String containerName;
    private String containerDescription;
    private String ownerDisplayName;
    private String ownerType;
    private String containerTypeName;
    private boolean active;
    private LocalDateTime lastActiveDateTime;
    private List<ContainerItemSummaryDto> items;

}
