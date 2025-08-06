package com.botofholding.bot.SlashCommands;

import com.botofholding.bot.SlashCommands.Parsers.InventoryParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class InventoryCommand extends SubcommandDispatcherCommand<InventoryParser> {

    @Autowired
    public InventoryCommand(Collection<InventoryParser> inventoryParsers, ApiClient apiClient) {
        super(CommandConstants.CMD_INVENTORY, inventoryParsers, apiClient);
    }
}
