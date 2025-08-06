package com.botofholding.bot.AutoComplete.Providers;

import com.botofholding.bot.Domain.DTOs.Request.AutoCompleteRequestDto;
import com.botofholding.bot.Domain.DTOs.Response.AutoCompleteDto;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class AbstractActiveItemNameProvider implements AutoCompleteProvider {

    private final ApiClient apiClient;

    public AbstractActiveItemNameProvider(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_ITEM;
    }

    @Override
    public Mono<List<AutoCompleteDto>> fetchSuggestions(AutoCompleteRequestDto request) {
        return apiClient.autocompleteActiveContainerItems(request.getPrefix());
    }

}
