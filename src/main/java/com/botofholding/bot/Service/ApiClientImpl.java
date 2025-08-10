package com.botofholding.bot.Service;

import com.botofholding.bot.Domain.DTOs.Request.*;
import com.botofholding.bot.Domain.Entities.ApiResponsePayload;
import com.botofholding.bot.Domain.DTOs.Response.*;
import com.botofholding.bot.Exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ApiClientImpl implements ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClientImpl.class);
    private final WebClient webClient;
    private volatile String botAuthToken; // Store the token here

    public ApiClientImpl(WebClient.Builder webClientBuilder, @Value("${api.base-url}") String baseUrl) {
        // This WebClient is now perfectly configured with both authentication and impersonation.
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(addAuthorizationHeader())
                .filter(addImpersonationHeaders())
                .build();
    }

    /**
     * This method is called by Spring after the bean is constructed.
     * It's the perfect place to fetch the initial token.
     */
    @PostConstruct
    public void initializeToken() {
        this.webClient.get()
                .uri("/auth/bot-token")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("token"))
                .doOnSuccess(token -> {
                    this.botAuthToken = token;
                    logger.info("Successfully fetched API token for bot.");
                })
                .doOnError(error -> logger.error("FATAL: Could not fetch API token for bot.", error))
                .block(); // Use .block() here because the bot cannot function without the token.
    }

    /**
     * A filter function that adds the impersonation headers (the "actor")
     * to every request by reading from the reactive context.
     */
    private ExchangeFilterFunction addImpersonationHeaders() {
        return (request, next) -> Mono.deferContextual(contextView -> {
            ClientRequest newRequest = ClientRequest.from(request)
                    .headers(headers -> {
                        contextView.getOrEmpty("user.id").ifPresent(id -> headers.set("X-On-Behalf-Of-User-ID", id.toString()));
                        contextView.getOrEmpty("user.name").ifPresent(name -> headers.set("X-On-Behalf-Of-User-Name", name.toString()));
                        contextView.getOrEmpty("user.globalName").ifPresent(gName -> headers.set("X-On-Behalf-Of-Global-Name", gName.toString()));
                    }).build();
            return next.exchange(newRequest);
        });
    }

    /**
     * A filter function that adds the stored Bearer token to every outgoing request.
     */
    private ExchangeFilterFunction addAuthorizationHeader() {
        return (clientRequest, next) -> {
            // Don't add the auth header to the token request itself
            if (clientRequest.url().getPath().equals("/api/auth/bot-token")) {
                return next.exchange(clientRequest);
            }

            if (botAuthToken != null) {
                return next.exchange(ClientRequest.from(clientRequest)
                        .headers(headers -> headers.setBearerAuth(botAuthToken))
                        .build());
            }
            // If the token is somehow null, fail the request.
            return Mono.error(new IllegalStateException("API token is not available."));
        };
    }

    // =================================================================
    // USER & SETTINGS API CALLS
    // =================================================================

    @Override
    public Mono<ApiResponsePayload<BohUserDto>> getMyProfile() {
        logger.debug("Attempting to retrieve current user's profile from /api/users/me");
        return webClient.get()
                .uri("/users/me")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<BohUserDto>>() {})
                .flatMap(this::handleApiResponseWithPayload);
    }

    @Override
    public Mono<ApiResponsePayload<BohUserDto>> updateMyProfile(UserRequestDto dto) {
        logger.debug("Attempting to update current user's profile via /api/users/me");
        return webClient.put()
                .uri("/users/me")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<BohUserDto>>() {})
                .flatMap(this::handleApiResponseWithPayload);
    }

    @Override
    public Mono<UserSettingsDto> getMySettings() {
        logger.debug("Attempting to retrieve current user's settings from /api/users/me/settings");
        return webClient.get()
                .uri("/users/me/settings")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<UserSettingsDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<UserSettingsDto> updateMySettings(UserSettingsUpdateRequestDto dto) {
        logger.debug("Attempting to update current user's settings via /api/users/me/settings");
        return webClient.put()
                .uri("/users/me/settings")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<UserSettingsDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    // =================================================================
    // CONTAINER API CALLS
    // =================================================================

    @Override
    public Mono<ContainerSummaryDto> createContainer(ContainerRequestDto dto, Long targetOwnerId, String targetOwnerType, String targetOwnerName) {
        logger.debug("Attempting to create a new container with name: {}", dto.getContainerName());
        return webClient.post()
                .uri("/containers")
                .headers(headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(targetOwnerId));
                    headers.set("X-Target-Owner-Type", targetOwnerType);
                    if (targetOwnerName != null) {
                        headers.set("X-Target-Owner-Name", targetOwnerName);
                    }
                })
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<ContainerSummaryDto> getContainerById(Long containerId, Long ownerId, String ownerType, String ownerName) {
        logger.debug("Finding container with ID '{}' for owner {} of type {}.", containerId, ownerId, ownerType);
        return webClient.get()
                .uri("/containers/{id}", containerId)

                .headers(headers -> {
                    // The target can now be a GUILD or a USER.
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<List<ContainerSummaryDto>> findContainers(String name, Long ownerId, String ownerType, String ownerName) {
        logger.debug("Finding containers with name '{}' for owner {} of type {}.", name, ownerId, ownerType);
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/containers");
                    // [REFACTORED] Only add the 'name' query parameter if it's not null or blank.
                    if (name != null && !name.isBlank()) {
                        uriBuilder.queryParam("name", name);
                    }
                    return uriBuilder.build();
                })
                .headers(headers -> {
                    // The target can now be a GUILD or a USER.
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<ContainerSummaryDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<ContainerSummaryDto> useContainerById(Long containerId, Long ownerId, String ownerType, String ownerName) {
        return webClient.put()
                .uri("/containers/{id}/activate", containerId)
                .headers(headers -> {
                    // The target can now be a GUILD or a USER.
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponse);

    }

    @Override
    public Mono<ContainerSummaryDto> useContainerByName(String containerName, String ownerPriority, Long ownerId, String ownerType, String ownerName) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/containers/activate")
                        .queryParam("name", containerName)
                        .build()
                )
                .headers(headers -> {
                    // The target can now be a GUILD or a USER.
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<ContainerSummaryDto> getActiveContainer() {
        return webClient.get()
                .uri("/containers/active")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<DeletedEntityDto> deleteContainer(String containerName, Long containerId, Long ownerId, String ownerType, String ownerName) {
        return webClient.delete()
                .uri( uriBuilder -> uriBuilder
                        .path("/containers/{id}")
                        .queryParam("name", containerName)
                        .build(containerId)
                )
                .headers( headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<DeletedEntityDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<ApiResponsePayload<ContainerSummaryDto>> addItemToActiveContainer(AddItemRequestDto dto, Long ownerId, String ownerType, String ownerName) {
        logger.debug("Attempting to add item {} (qty: {}) to active container", dto.getItemId(), dto.getQuantity());
        return webClient.post()
                .uri("/containers/active/items")
                .headers( headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponseWithPayload);
    }

    @Override
    public Mono<ApiResponsePayload<ContainerSummaryDto>> dropItemFromActiveContainer(Long itemId, String itemName, Boolean dropChildren, Integer itemQuantity) {
        return webClient.delete()
                .uri(uriBuilder -> {
                    uriBuilder.path("/containers/active/items");
                    if (itemId != null) {
                        logger.debug("itemId: {}", itemId);
                        uriBuilder.queryParam("id", itemId);
                    }
                    if (itemName != null) {
                        uriBuilder.queryParam("name", itemName);
                    }
                    if (dropChildren != null) {
                        uriBuilder.queryParam("dropChildren", dropChildren);
                    }
                    uriBuilder.queryParam("quantity", itemQuantity);;
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponseWithPayload);

    }

    @Override
    public Mono<ApiResponsePayload<ContainerSummaryDto>> modifyItemInActiveContainer(ModifyItemRequestDto dto) {
        return webClient.patch()
                .uri("/containers/active/items")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ContainerSummaryDto>>() {})
                .flatMap(this::handleApiResponseWithPayload);
    }


    // =================================================================
    // ITEM API CALLS
    // =================================================================

    @Override
    public Mono<ItemSummaryDto> getItemById(Long itemId, Long ownerId, String ownerType, String ownerName) {
        return webClient.get()
                .uri("/items/{id}", itemId)
                .headers(headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<ItemSummaryDto>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<List<ItemSummaryDto>> findItems(String name, Long ownerId, String ownerType, String ownerName) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/items");
                    if (name != null && !name.isBlank()) {
                        uriBuilder.queryParam("name", name);
                    }
                    return uriBuilder.build();
                })
                .headers(headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<ItemSummaryDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    // =================================================================
    // AUTOCOMPLETE API CALLS
    // =================================================================

    @Override
    public Mono<List<AutoCompleteDto>> autocompleteContainers(String prefix, Long ownerId, String ownerType, String ownerName) {
        logger.debug("Autocompleting containers with prefix '{}' for owner {} of type {}", prefix, ownerName, ownerType);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/containers/autocomplete")
                        .queryParam("prefix", prefix)
                        .build())
                .headers(headers -> {
                    // The target can now be a GUILD or a USER.
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<AutoCompleteDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<List<AutoCompleteDto>> autocompleteItems(String prefix, Long ownerId, String ownerType, String ownerName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/autocomplete")
                        .queryParam("prefix", prefix)
                        .build())
                .headers(headers -> {
                    headers.set("X-Target-Owner-ID", String.valueOf(ownerId));
                    headers.set("X-Target-Owner-Type", ownerType);
                    if (ownerName != null) {
                        headers.set("X-Target-Owner-Name", ownerName);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<AutoCompleteDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<List<AutoCompleteDto>> autocompleteActiveContainerItems(String prefix) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/containers/active/items/autocomplete");
                    uriBuilder.queryParam("prefix", prefix);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<AutoCompleteDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    @Override
    public Mono<List<AutoCompleteDto>> autocompleteParentActiveContainerItems(String prefix) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/containers/active/parents/autocomplete");
                    uriBuilder.queryParam("prefix", prefix);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StandardApiResponse<List<AutoCompleteDto>>>() {})
                .flatMap(this::handleApiResponse);
    }

    // =================================================================
    // HELPER METHODS
    // =================================================================

     /**
     * A generic, reusable helper to handle the StandardApiResponse wrapper.
     * If the response is successful, it unwraps the data.
     * If the response indicates a business logic failure, it converts it to a reactive error.
     * @param apiResponse The response from the API.
     * @return A Mono containing the data on success, or a Mono.error on failure.
     * @param <T> The type of the data within the response.
     */
    private <T> Mono<T> handleApiResponse(StandardApiResponse<T> apiResponse) {
        if (apiResponse.isSuccess()) {
            logger.info(apiResponse.getMessage());
            return Mono.justOrEmpty(apiResponse.getData());
        } else {
            return Mono.error(new ApiException(apiResponse.getMessage() != null ? apiResponse.getMessage() : "An unknown API error occurred."));
        }
    }

    /**
     * A generic helper to handle a StandardApiResponse and wrap the result in an ApiResponsePayload.
     * This is used for endpoints where the success message is needed by the caller.
     * @param apiResponse The response from the API.
     * @return A Mono containing the ApiResponsePayload on success, or a Mono.error on failure.
     * @param <T> The type of the data within the response.
     */
    private <T> Mono<ApiResponsePayload<T>> handleApiResponseWithPayload(StandardApiResponse<T> apiResponse) {
        if (apiResponse.isSuccess()) {
            logger.info(apiResponse.getMessage());
            return Mono.just(new ApiResponsePayload<>(apiResponse.getData(), apiResponse.getMessage()));
        } else {
            return Mono.error(new ApiException(apiResponse.getMessage() != null ? apiResponse.getMessage() : "An unknown API error occurred."));
        }
    }
}