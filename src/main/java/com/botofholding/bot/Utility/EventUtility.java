package com.botofholding.bot.Utility;

import com.botofholding.bot.Domain.Entities.AutocompleteSelection;
import com.botofholding.bot.Domain.Entities.TargetOwner;
import com.botofholding.bot.Domain.Enum.OwnerType;
import com.botofholding.bot.Exception.ReplyException;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.Optional;

public final class EventUtility {

    private static final Logger logger = LoggerFactory.getLogger(EventUtility.class);

    private EventUtility() {}
    /**
     * [NEW & WORLD-CLASS]
     * A reusable helper to determine the target owner of a command based on a
     * common "server-owned" boolean option pattern.
     *
     * @param event The interaction event.
     * @param subcommandName The name of the subcommand where the option resides.
     * @return A Mono emitting a TargetOwner record.
     */
    public static Mono<TargetOwner> determineTargetOwner(ChatInputInteractionEvent event, String subcommandName, String optionName, OwnerTypeExtractor extractor) {
        // 1. Use the provided extractor "strategy" to determine the owner type. Default to USER.
        Mono<OwnerType> ownerTypeMono = extractor.extract(event, subcommandName, optionName)
                .defaultIfEmpty(OwnerType.USER);

        // 2. Use flatMap to reactively build the target owner context.
        return ownerTypeMono.flatMap(ownerType -> {
            logger.debug("Determining the targetOwner from ownerType '{}'.", ownerType);
            if (ownerType == OwnerType.GUILD) {
                // User explicitly requested a GUILD owner. We must enforce this.
                return getGuildId(event)
                        .switchIfEmpty(Mono.error(new ReplyException("Cannot create a server-owned entity in a private message.")))
                        .flatMap(snowflake -> getGuildNameOrDefault(event)
                                .map(guildName -> new TargetOwner(snowflake.asLong(), guildName, "GUILD"))
                        );
            } else { // ownerType is USER
                return buildUserTargetOwner(event);
            }
        });
    }

    /**
     * Builds a TargetOwner context for the Guild where the command was invoked or User if not invoked in a server.
     * @param event The interaction event.
     * @return A Mono emitting a TargetOwner record for the guild or invoking user if no guild.
     */
    public static Mono<TargetOwner> buildGuildTargetOwner(InteractionCreateEvent event) {
         return getGuildId(event)
                .flatMap(snowflake ->
                        getGuildNameOrDefault(event)
                                .map(guildName -> new TargetOwner(snowflake.asLong(), guildName, "GUILD"))
                )
                .switchIfEmpty(buildUserTargetOwner(event));
    }

    /**
     * Builds a TargetOwner context for the invoking user.
     * @param event The interaction event.
     * @return A Mono emitting a TargetOwner record for the user.
     */
    public static Mono<TargetOwner> buildUserTargetOwner(InteractionCreateEvent event) {
        User user = getInvokingUser(event);
        TargetOwner owner = new TargetOwner(user.getId().asLong(), user.getUsername(), "USER");
        return Mono.just(owner);
    }

    /**
     * Safely extracts the name of a subcommand or subcommand group from the event's options.
     * @param options A list of ApplicationCommandInteractionOption objects.
     * @return An Optional containing the subcommand name, or empty if no subcommand option is present.
     */
    public static Optional<String> extractSubcommandName(List<ApplicationCommandInteractionOption> options) {
        return options.stream()
                .filter(opt -> opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND || opt.getType() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP)
                .findFirst()
                .map(ApplicationCommandInteractionOption::getName);
    }

    /**
     * Extracts a specific target User from a subcommand's option. This is fully reactive and safe.
     * @param event The interaction event.
     * @param subcommandName The name of the subcommand (e.g., "user").
     * @param optionName The name of the option containing the user (e.g., "target_user").
     * @return A Mono emitting the target User, or an empty Mono if not found.
     */
    public static Mono<User> getTargetUserFromOption(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        return Mono.justOrEmpty(
                        event.getOptions().stream()
                                .filter(subcommand -> subcommand.getName().equalsIgnoreCase(subcommandName))
                                .findFirst()
                                .flatMap(subcommand -> subcommand.getOption(optionName))
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                )
                .flatMap(ApplicationCommandInteractionOptionValue::asUser);
    }

    public static Mono<OwnerType> getOwnerTypeFromBooleanOption(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        // This is now much simpler and more robust.
        return Mono.justOrEmpty(getOptionValueAsOptionalBoolean(event, subcommandName, optionName))
                .map(isChecked -> isChecked ? OwnerType.GUILD : OwnerType.USER);
    }

    public static Mono<OwnerType> getOwnerTypeFromSingleChoice(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        return Mono.justOrEmpty(getOptionValue(event, subcommandName, optionName).isPresent() ? OwnerType.GUILD : OwnerType.USER).defaultIfEmpty(OwnerType.USER);
    }

    /**
     * An OwnerTypeExtractor strategy that parses a string option ("GUILD" or "USER").
     */
    public static Mono<OwnerType> getOwnerTypeFromStringOption(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        return getOptionValueAsString(event, subcommandName, optionName)
                .flatMap(value -> {
                    try {
                        return Mono.just(OwnerType.valueOf(value.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid value '{}' for OwnerType option '{}'. Ignoring.", value, optionName);
                        return Mono.empty(); // Invalid value, treat as not present.
                    }
                });
    }

    public static Mono<String> getOptionValueAsString(ChatInputInteractionEvent event, String subcommandName, String optionName){
        Optional<String> stringOptional = getOptionValue(event, subcommandName, optionName)
                .flatMap(value -> {
                    try {
                        return Optional.of(value.asString());
                    } catch (IllegalStateException e) {
                        return Optional.empty();
                    }
                });

        return Mono.justOrEmpty(stringOptional);
    }

    /**
     * Extracts a boolean value from a command option.
     * @param event The interaction event.
     * @param subcommandName the name of the subcommand (new, find, etc.)
     * @param optionName the name of the option to extract the boolean from
     * @return an Optional containing the boolean value, or empty if not found.
     */
    public static Optional<Boolean> getOptionValueAsOptionalBoolean(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        return getOptionValue(event, subcommandName, optionName)
                .flatMap(value -> Optional.of(value.asBoolean())); // -> Optional<boolean>
    }

    public static Optional<ApplicationCommandInteractionOptionValue> getOptionValue(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        return event.getOptions().stream()
                .filter(subcommand -> subcommand.getName().equalsIgnoreCase(subcommandName))
                .findFirst()
                .flatMap(subcommand -> subcommand.getOption(optionName))
                .flatMap(ApplicationCommandInteractionOption::getValue);
    }

    public static Mono<Long> getOptionValueAsLong(ChatInputInteractionEvent event, String subcommandName, String optionName) {
        Optional<Long> longOptional = getOptionValue(event, subcommandName, optionName)
                .flatMap(value -> Optional.of(value.asLong()));
        return Mono.justOrEmpty(longOptional);
    }

    /**
     * Extracts an autocomplete option value from an event and parses it into an AutocompleteSelection.
     * This is the standard way to handle item inputs, as it gracefully handles both users
     * selecting from the autocomplete list and users typing in a new item name.
     *
     * @param event The interaction event.
     * @param subCommandName The name of the subcommand being executed.
     * @param optionName The name of the option to extract.
     * @return A Mono emitting the parsed AutocompleteSelection, or an empty Mono if the option is not present.
     */
    public static Mono<AutocompleteSelection> getAutocompleteSelection(ChatInputInteractionEvent event, String subCommandName, String optionName) {
        // This chain gets the string value, then tries to parse it as a selection,
        // falling back to a new selection with a null ID if parsing fails.
        return getOptionValueAsString(event, subCommandName, optionName)
                .flatMap(name -> parseAutocompleteSelection(name)
                        .switchIfEmpty(Mono.just(new AutocompleteSelection(name, null))));
    }

    /**
     * [WORLD-CLASS] Parses a string from an autocomplete option that is expected to be in the "name:id" format.
     *
     * @param value The raw string value from the autocomplete option.
     * @return A Mono emitting the parsed {@link AutocompleteSelection}, or an empty Mono if parsing fails.
     */
    public static Mono<AutocompleteSelection> parseAutocompleteSelection(String value) {
        if (value == null || value.isBlank()) {
            return Mono.empty();
        }
        int lastColon = value.lastIndexOf(':');
        if (lastColon <= 0 || lastColon == value.length() - 1) {
            // No colon, colon at the start, or colon at the end.
            return Mono.empty();
        }

        try {
            String name = value.substring(0, lastColon);
            long id = Long.parseLong(value.substring(lastColon + 1));
            return Mono.just(new AutocompleteSelection(name, id));
        } catch (NumberFormatException e) {
            // The part after the colon is not a valid long.
            logger.warn("Failed to parse ID from autocomplete value: '{}'", value, e);
            return Mono.empty();
        }
    }

    /**
     * Gets the name of the guild (server) an event was created in, defaults to an empty string.
     * @param event The interaction event.
     * @return The guild name as Mono<String> or empty if not present.
     */
    public static Mono<String> getGuildNameOrDefault(InteractionCreateEvent event) {
        return event.getInteraction().getGuild().map(Guild::getName).defaultIfEmpty("");
    }

    public static Context addUserContext(Context ctx, User user) {
        return ctx.put("user.id", user.getId().asString())
                .put("user.name", user.getUsername())
                .put("user.globalName", user.getGlobalName().orElse(user.getTag()));
    }

    /**
     * Gets the user who invoked the command or autocomplete event.
     * @param event The interaction event, can be for a command or autocomplete.
     * @return The invoking User.
     */
    public static User getInvokingUser(InteractionCreateEvent event) {
        return event.getInteraction().getUser();
    }

    /**
     * Extracts the guild ID from the event.
     * @param event The interaction event.
     * @return The guild ID as Mono<Snowflake> or empty if not present.
     */
    public static Mono<Snowflake> getGuildId(InteractionCreateEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getGuildId());
    }

    /**
     *
     * Extracts the guild ID as Mono<Long> or defaults to 0 if not present.
     * @param event The interaction event.
     * @return Mono<Long>
     */
    public static Mono<Long> getGuildIdAsLongOrDefault(InteractionCreateEvent event) {
        return getGuildId(event).map(Snowflake::asLong).defaultIfEmpty(0L);
    }

    /**
     *
     * Extracts the invoking user's Discord ID.
     * @param event The interaction event.
     * @return The invoking user's Discord ID as Long.
     */
    public static Long getInvokingUserDiscordId(InteractionCreateEvent event) {
        return getInvokingUser(event).getId().asLong();
    }

    /**
     * [REFACTORED & WORLD-CLASS]
     * Extracts the invoking user's Discord tag (e.g., Username#1234).
     * @param event The interaction event.
     * @return The invoking user's Discord tag as String.
     */
    public static String getInvokingUserTag(InteractionCreateEvent event) {
        return getInvokingUser(event).getTag();
    }

    public static String getInvokingUserUserName(InteractionCreateEvent event) {
        return getInvokingUser(event).getUsername();
    }

    public static Mono<String> getInvokingUserGlobalName(InteractionCreateEvent event) {
        return Mono.justOrEmpty(getInvokingUser(event).getGlobalName());
    }

    /**
     * Checks if the invoking user is a bot.
     * @param event the interaction event
     * @return True if the invoking user is a bot, false otherwise.
     */
    public static boolean isBot(InteractionCreateEvent event) {
        return getInvokingUser(event).isBot();
    }

    /**
     * Extracts the guild nickname of the invoking user if available.
     * @param event the interaction event
     * @return The guild nickname as Mono<String>
     */
    public static Mono<String> getInvokingUserGuildNickname(InteractionCreateEvent event) {
        return getGuildId(event) // Returns Mono<Snowflake>
                .flatMap(guildId -> getInvokingUser(event).asMember(guildId)) // Returns Mono<Member>
                .flatMap(member -> Mono.justOrEmpty(member.getNickname()));
    }

    /**
     * Returns the invoking user's display name with the following order of precedence:
     * Guild Nickname > Global Name > Username
     * @param event the interaction event
     * @return The invoking user's display name as Mono<String>
     */
    public static Mono<String> getInvokingUserDisplayName(InteractionCreateEvent event) {
        return getInvokingUserGuildNickname(event)
                .switchIfEmpty(getInvokingUserGlobalName(event))
                .switchIfEmpty(Mono.fromSupplier(() -> getInvokingUserUserName(event)));
    }

    public static Mono<User> getMessageAuthor(Message eventMessage) {
        return Mono.justOrEmpty(eventMessage.getAuthor());
    }

    /**
     * Checks if the message author is a bot.
     * @param eventMessage the message to check
     * @return True if the message author is a bot, false otherwise. If no author is present, returns false.
     */
    public static boolean isBot(Message eventMessage) {
        return eventMessage.getAuthor().map(User::isBot).orElse(false);
    }

}