package com.botofholding.bot.AutoComplete.Providers;

import com.botofholding.bot.Domain.DTOs.Request.AutoCompleteRequestDto;
import com.botofholding.bot.Domain.DTOs.Response.AutoCompleteDto;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class AbstractActiveParentItemNameProvider implements AutoCompleteProvider{

    private final ApiClient apiClient;

    public AbstractActiveParentItemNameProvider(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<List<AutoCompleteDto>> fetchSuggestions(AutoCompleteRequestDto request) {
        return apiClient.autocompleteParentActiveContainerItems(request.getPrefix());
    }
}
