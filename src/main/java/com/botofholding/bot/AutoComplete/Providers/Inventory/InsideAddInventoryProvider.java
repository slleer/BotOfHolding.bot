package com.botofholding.bot.AutoComplete.Providers.Inventory;

import com.botofholding.bot.AutoComplete.Providers.AbstractActiveParentItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.stereotype.Component;

@Component
public class InsideAddInventoryProvider extends AbstractActiveParentItemNameProvider implements InventoryProvider {

    public InsideAddInventoryProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_ADD;
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_INVENTORY_ADD_PARENT;
    }

}
