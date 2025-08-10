package com.botofholding.bot.AutoComplete.Providers.Inventory;

import com.botofholding.bot.AutoComplete.Providers.AbstractActiveParentItemNameProvider;
import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InsideModifyInventoryProvider extends AbstractActiveParentItemNameProvider implements InventoryProvider {

    @Autowired
    public InsideModifyInventoryProvider(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public String getSubCommandName() {
        return CommandConstants.SUBCMD_INVENTORY_MODIFY;
    }

    @Override
    public String getOptionName() {
        return CommandConstants.OPTION_INVENTORY_MODIFY_MOVE_INSIDE;
    }
}
