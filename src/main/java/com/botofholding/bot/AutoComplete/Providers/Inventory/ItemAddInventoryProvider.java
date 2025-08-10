package com.botofholding.bot.AutoComplete.Providers.Inventory;

import com.botofholding.bot.AutoComplete.Providers.AbstractItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemAddInventoryProvider extends AbstractItemNameProvider implements InventoryProvider {

    @Autowired
    public ItemAddInventoryProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_ADD;
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_ITEM;
    }
}
