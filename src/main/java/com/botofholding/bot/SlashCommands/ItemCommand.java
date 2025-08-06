package com.botofholding.bot.SlashCommands;

import com.botofholding.bot.SlashCommands.Parsers.ItemParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ItemCommand extends SubcommandDispatcherCommand<ItemParser> {

    protected ItemCommand(Collection<ItemParser> subParsers, ApiClient apiClient) {
        super(CommandConstants.CMD_ITEM, subParsers, apiClient);
    }
}
