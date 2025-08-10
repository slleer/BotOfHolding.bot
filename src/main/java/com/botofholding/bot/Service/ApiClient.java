package com.botofholding.bot.Service;

import com.botofholding.bot.Domain.DTOs.Request.*;
import com.botofholding.bot.Domain.DTOs.Response.*;
import com.botofholding.bot.Domain.Entities.ApiResponsePayload;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApiClient {

    // BOT
    //Mono<String>

    // USER & SETTINGS
    Mono<ApiResponsePayload<BohUserDto>> getMyProfile();
    Mono<ApiResponsePayload<BohUserDto>> updateMyProfile(UserRequestDto dto);
    Mono<UserSettingsDto> getMySettings();
    Mono<UserSettingsDto> updateMySettings(UserSettingsUpdateRequestDto dto);

    // CONTAINER
    Mono<ContainerSummaryDto> createContainer(ContainerRequestDto dto, Long targetOwnerId, String targetOwnerType, String targetOwnerName);
    Mono<ContainerSummaryDto> getContainerById(Long containerId, Long ownerId, String ownerType, String ownerName);
    Mono<List<ContainerSummaryDto>> findContainers(String name, Long ownerId, String ownerType, String ownerName);
    Mono<List<AutoCompleteDto>> autocompleteContainers(String prefix, Long ownerId, String ownerType, String ownerName);
    Mono<ContainerSummaryDto> useContainerById(Long containerId, Long ownerId, String ownerType, String ownerName);
    Mono<ContainerSummaryDto> useContainerByName(String containerName, String ownerPriority, Long ownerId, String ownerType, String ownerName);
    Mono<ContainerSummaryDto> getActiveContainer();
    Mono<DeletedEntityDto> deleteContainer(String containerName, Long containerId, Long ownerId, String ownerType, String ownerName);

    // ITEM
    Mono<ItemSummaryDto> getItemById(Long itemId, Long ownerId, String ownerType, String ownerName);
    Mono<List<ItemSummaryDto>> findItems(String name, Long ownerId, String ownerType, String ownerName);
    Mono<List<AutoCompleteDto>> autocompleteItems(String prefix, Long ownerId, String ownerType, String ownerName);
    Mono<List<AutoCompleteDto>> autocompleteActiveContainerItems(String prefix);
    Mono<List<AutoCompleteDto>> autocompleteParentActiveContainerItems(String prefix);

    // INVENTORY
    Mono<ApiResponsePayload<ContainerSummaryDto>> addItemToActiveContainer(AddItemRequestDto dto, Long ownerId, String ownerType, String ownerName);
    Mono<ApiResponsePayload<ContainerSummaryDto>> dropItemFromActiveContainer(Long itemId, String itemName, Boolean dropChildren, Integer itemQuantity);
    Mono<ApiResponsePayload<ContainerSummaryDto>> modifyItemInActiveContainer(ModifyItemRequestDto dto);
}
