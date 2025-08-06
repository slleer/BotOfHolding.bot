package com.botofholding.bot.AutoComplete.Providers;

import com.botofholding.bot.Domain.DTOs.Request.AutoCompleteRequestDto;
import com.botofholding.bot.Domain.DTOs.Response.AutoCompleteDto;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * An abstract provider that contains the shared logic for fetching item suggestions.
 * Concrete implementations only need to specify which option they are for.
 */
public abstract class AbstractItemNameProvider implements AutoCompleteProvider {

    private final ApiClient apiClient;

    public AbstractItemNameProvider(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<List<AutoCompleteDto>> fetchSuggestions(AutoCompleteRequestDto request) {
        TargetOwner targetOwner = request.getTargetOwner();
        return apiClient.autocompleteItems(
                request.getPrefix(),
                targetOwner.ownerId(),
                targetOwner.ownerType(),
                targetOwner.ownerName()
        );
    }
}