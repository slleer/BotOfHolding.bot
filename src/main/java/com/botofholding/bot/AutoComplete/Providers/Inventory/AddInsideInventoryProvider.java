package com.botofholding.bot.AutoComplete.Providers.Inventory;

import com.botofholding.bot.AutoComplete.Providers.AbstractActiveParentItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.stereotype.Component;

@Component
public class AddInsideInventoryProvider extends AbstractActiveParentItemNameProvider implements InventoryProvider {

    public AddInsideInventoryProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_PARENT;
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_ADD;
    }
}
