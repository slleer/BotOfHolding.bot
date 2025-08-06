package com.botofholding.bot.AutoComplete.Providers;

import com.botofholding.bot.Domain.DTOs.Request.AutoCompleteRequestDto;
import com.botofholding.bot.Domain.DTOs.Response.AutoCompleteDto;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class AbstractContainerNameProvider implements AutoCompleteProvider {

    private final ApiClient apiClient;

    public AbstractContainerNameProvider(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_NAME;
    }

    @Override
    public Mono<List<AutoCompleteDto>> fetchSuggestions(AutoCompleteRequestDto request) {
        TargetOwner targetOwner = request.getTargetOwner();
        return apiClient.autocompleteContainers(
                request.getPrefix(),
                targetOwner.ownerId(),
                targetOwner.ownerType(),
                targetOwner.ownerName()
        );
    }
}
