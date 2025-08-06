package com.botofholding.bot.Domain.DTOs.Request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContainerRequestDto {
    private String containerName;
    private String containerDescription;
    // Optional: if you want to specify the type on creation
    private String containerTypeName;
    private boolean isActive;

}
