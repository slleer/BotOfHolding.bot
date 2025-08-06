package com.botofholding.bot.SlashCommands;

import com.botofholding.bot.SlashCommands.Parsers.ContainerParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ContainerCommand extends SubcommandDispatcherCommand<ContainerParser> {

    @Autowired
    public ContainerCommand(Collection<ContainerParser> containerParsers, ApiClient apiClient) {
        super(CommandConstants.CMD_CONTAINER, containerParsers, apiClient); // Call super constructor with "get" and GetParsers
    }

}
