package com.botofholding.bot.AutoComplete;

import com.botofholding.bot.AutoComplete.Providers.Item.FindItemProvider;
import com.botofholding.bot.AutoComplete.Providers.ItemProvider;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class ItemAutoCompleteDispatcher extends AutoCompleteDispatcher<ItemProvider> {
    @Autowired
    public ItemAutoCompleteDispatcher(Collection<ItemProvider> providers) {
        // This dispatcher handles all autocomplete options for the /item command.
        super(CommandConstants.CMD_ITEM, providers);
    }
}