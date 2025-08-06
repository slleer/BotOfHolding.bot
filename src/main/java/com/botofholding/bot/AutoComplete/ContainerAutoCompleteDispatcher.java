package com.botofholding.bot.AutoComplete;

import com.botofholding.bot.AutoComplete.Providers.ContainerProvider;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ContainerAutoCompleteDispatcher extends AutoCompleteDispatcher<ContainerProvider> {
    @Autowired
    public ContainerAutoCompleteDispatcher(Collection<ContainerProvider> providers) {
        // This dispatcher handles all autocomplete options for the /container command.
        super(CommandConstants.CMD_CONTAINER, providers);
    }
}