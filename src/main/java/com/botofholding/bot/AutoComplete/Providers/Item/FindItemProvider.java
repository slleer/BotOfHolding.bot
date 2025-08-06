package com.botofholding.bot.AutoComplete.Providers.Item;

import com.botofholding.bot.AutoComplete.Providers.AbstractItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.ItemProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindItemProvider extends AbstractItemNameProvider implements ItemProvider {

    @Autowired
    public FindItemProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_NAME;
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_ITEM_FIND;
    }

}
