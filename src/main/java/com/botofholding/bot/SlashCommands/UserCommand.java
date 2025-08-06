package com.botofholding.bot.SlashCommands;

import com.botofholding.bot.SlashCommands.Parsers.UserParser;
import com.botofholding.bot.Service.ApiClient;
import com.botofholding.bot.Utility.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UserCommand extends SubcommandDispatcherCommand<UserParser> {

    @Autowired
    public UserCommand(Collection<UserParser> userParsers, ApiClient apiClient) {
        super(CommandConstants.CMD_USER, userParsers, apiClient); // Call the super constructor
    }

}
