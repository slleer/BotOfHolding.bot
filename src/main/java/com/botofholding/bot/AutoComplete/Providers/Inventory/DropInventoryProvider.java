package com.botofholding.bot.AutoComplete.Providers.Inventory;

import com.botofholding.bot.AutoComplete.Providers.AbstractActiveItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.stereotype.Component;

@Component
public class DropInventoryProvider extends AbstractActiveItemNameProvider implements InventoryProvider {

    public DropInventoryProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_DROP;
    }

}
