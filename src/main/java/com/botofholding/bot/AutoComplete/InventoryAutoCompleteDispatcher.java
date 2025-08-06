package com.botofholding.bot.AutoComplete;

import com.botofholding.bot.AutoComplete.Providers.InventoryProvider;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class InventoryAutoCompleteDispatcher extends AutoCompleteDispatcher<InventoryProvider> {
    @Autowired
    public InventoryAutoCompleteDispatcher(Collection<InventoryProvider> providers) {
        // This dispatcher handles all autocomplete options for the /inventory command.
        super(CommandConstants.CMD_INVENTORY, providers);
    }
}