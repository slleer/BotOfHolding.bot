package com.botofholding.bot.Utility;

import java.math.BigDecimal;
import com.botofholding.bot.Domain.DTOs.Response.*;
import com.botofholding.bot.Utility.config.ContainerDisplayOptions;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class MessageFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");
    private static final String EMPTY = "";
    private static final String BULLET = "- ";
    private static final String BULLET_INDENTED = "  - ";
    private static final String BULLET_2X_INDENTED = "    - ";
    private static final String BULLET_3X_INDENTED = "      - ";

    // --- Formatting Strategies ---
    private static final Function<String, String> HEADER_FORMATTER = header -> "# " + header + "\n";
    private static final Function<String, String> SUBHEADER_FORMATTER = subheader -> "## " + subheader + "\n";
    private static final Function<String, String> SUBTEXT_FORMATTER = subtext -> "-# " + subtext + "\n";
    private static final Function<String, String> PLAIN_LABEL_FORMATTER = label -> label + ":";
    private static final Function<String, String> EMPTY_LABEL_FORMATTER = label -> label;
    private static final Function<String, String> BOLD_LABEL_FORMATTER = label -> bold(label + ":");
    private static final Function<String, String> PLAIN_VALUE_FORMATTER = value -> value;
    private static final Function<String, String> ITALIC_VALUE_FORMATTER = MessageFormatter::italic;
    private static final Function<String, String> SPOILER_ITALIC_VALUE_FORMATTER = value -> spoiler(italic(value));
    private static final Function<String, String> BOLD_ITALIC_VALUE_FORMATTER = MessageFormatter::boldItalic;


    private MessageFormatter(){}
    /**
     * A private helper to format a Float value by removing unnecessary trailing zeros.
     * For example, 5.0f becomes "5" and 5.50f becomes "5.5".
     * @param value The float value to format.
     * @return A string representation of the number without trailing zeros.
     */
    private static String formatFloat(Float value) {
        return new BigDecimal(String.valueOf(value)).stripTrailingZeros().toPlainString();
    }

    /**
     * Wraps the given text in asterisks for italics.
     */
    private static String italic(String text) {
        return "*" + text + "*";
    }

    /**
     * Wraps the given text in double asterisks for bold.
     */
    private static String bold(String text) {
        return "**" + text + "**";
    }

    /**
     * Wraps the given text in *** for bold italics
     */
    private static String boldItalic(String text) {
        return "***" + text + "***";
    }

    /**
     * Wraps the given text in double vertical bars for a spoiler.
     */
    private static String spoiler(String text) {
        return "||" + text + "||";
    }

    //private static

    /**
     * Appends a fully formatted line to a StringBuilder using formatting strategies.
     * This is the core method for building consistent, styled replies.
     * It gracefully handles null or empty values by not appending the line.
     *
     * @param sb The StringBuilder to append to.
     * @param prefix The string to prepend to the line (e.g., newline, indent).
     * @param label The text for the label part.
     * @param value The text for the value part.
     * @param labelFormatter A function to style the label.
     * @param valueFormatter A function to style the value.
     */
    private static void appendLine(
            StringBuilder sb,
            String prefix,
            String label,
            String value,
            Function<String, String> labelFormatter,
            Function<String, String> valueFormatter
    ) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(prefix)
              .append(labelFormatter.apply(label))
              .append(" ")
              .append(valueFormatter.apply(value))
              .append("\n");
        }
    }

    public static String formatUserReply(BohUserDto userDto, String context) {
        String displayName = userDto.getBohGlobalUserName() != null ? userDto.getBohGlobalUserName() : userDto.getBohUserName();
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER_FORMATTER.apply(context + " User:")).append(BULLET).append(italic(displayName)).append("\n");
        appendLine(sb, BULLET_INDENTED, "Discord Id:", String.valueOf(userDto.getDiscordId()), BOLD_LABEL_FORMATTER, SPOILER_ITALIC_VALUE_FORMATTER);
        appendLine(sb, BULLET_INDENTED, "Last Active:", userDto.getLastActive() != null ? userDto.getLastActive().format(DATE_TIME_FORMATTER) : "N/A", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        return sb.toString();
    }

    public static String formatUserReply(BohUserWithPrimaryContainerResponseDto userDto, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatUserReply(userDto, context));
        if (userDto.getPrimaryContainerName() != null) {
            appendLine(sb, "\n", "Primary Container", userDto.getPrimaryContainerName(), BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        }
        return sb.toString();
    }

    public static String formatGetContainerReply(ContainerSummaryDto containerDto) {
        // Default options: show everything.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(containerDto, HEADER_FORMATTER.apply("Found Container:"), BULLET, options);
    }

    public static String formatUseContainerReply(ContainerSummaryDto containerDto) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(containerDto, HEADER_FORMATTER.apply("Using Container:"), BULLET, options);
    }

    public static String formatAddContainerReply(ContainerSummaryDto containerDto) {
        // On creation, the item list is always empty, so don't display it.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder()
                .displayItems(false)
                .build();
        return formatContainerReply(containerDto, HEADER_FORMATTER.apply("New Container:"), BULLET, options);
    }

    public static String formatActiveContainerReply(ContainerSummaryDto activeContainer) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(activeContainer, HEADER_FORMATTER.apply("Active Container:"), BULLET, options);
    }

    public static String formatAddInventoryContainerReply(ContainerSummaryDto containerDto) {
        // After adding an item, the user definitely wants to see the updated item list.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(containerDto, HEADER_FORMATTER.apply("Item Added to Container:"), BULLET, options);
    }

    public static String formatDropInventoryContainerReply(ContainerSummaryDto updatedContainer, String itemName, int quantity) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(updatedContainer, HEADER_FORMATTER.apply(quantity + "x " + itemName + " Dropped from Container:"), BULLET, options);
    }

    private static String formatContainerReply(ContainerSummaryDto containerDto, String header, String bullet, ContainerDisplayOptions options) {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append(bullet).append(bold(containerDto.getContainerName())).append("\n");

        if (options.isDisplayOwner()) {
            appendLine(sb, BULLET_INDENTED, "Owner", containerDto.getOwnerDisplayName(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }

        if (options.isDisplayDescription() && containerDto.getContainerDescription() != null) {
            appendLine(sb, BULLET_INDENTED, "Description", containerDto.getContainerDescription(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }
        if (options.isDisplayType() && containerDto.getContainerTypeName() != null) {
            appendLine(sb, BULLET_INDENTED, "Type", containerDto.getContainerTypeName(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }
        if (options.isDisplayStatus() && containerDto.isActive()){
            appendLine(sb, BULLET_INDENTED, "Status", "Active", BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }
        if (options.isDisplayLastActive() && containerDto.getLastActiveDateTime() != null) {
            appendLine(sb, BULLET_INDENTED, "Last Active", containerDto.getLastActiveDateTime().format(DATE_TIME_FORMATTER), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }

        if (options.isDisplayItems() && containerDto.getItems() != null && !containerDto.getItems().isEmpty()) {
//            sb.append(BULLET_INDENTED).append(bold("Contents:")).append("\n");

            containerDto.getItems().stream()
                    .sorted(java.util.Comparator.comparing(ContainerItemSummaryDto::getLastModified).reversed())
                    .forEach(item -> {
                        sb.append(BULLET_INDENTED).append(item.getItemName());
                        if (item.getQuantity() > 1) {
                            sb.append(" ").append(bold(String.format("x%d", item.getQuantity())));
                        }
                        sb.append("\n");
                        if (options.isDisplayNote() && item.getUserNote() != null && !item.getUserNote().isBlank()) {
                            appendLine(sb, BULLET_2X_INDENTED, "-# ", item.getUserNote(), EMPTY_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
                        } else {
                            sb.append("\n");
                        }
                        if (options.isDisplayContainerItemId()) {
                            appendLine(sb, BULLET_3X_INDENTED, "Id", String.valueOf(item.getContainerItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
                        }
                        if (options.isDisplayItemId()) {
                            appendLine(sb, BULLET_3X_INDENTED, "Item Id", String.valueOf(item.getItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
                        }
                        if (options.isDisplayLastModified()){
                            appendLine(sb, BULLET_3X_INDENTED, "Last Modified", item.getLastModified().format(DATE_TIME_FORMATTER), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
                        }
                    });
        }
        return sb.toString();
    }

    public static String formatContainerReply(List<ContainerSummaryDto> dtos) {
        if (dtos.isEmpty()) {
            // This case is handled in the parser, but it's good practice to be defensive.
            return HEADER_FORMATTER.apply("No Container/s Found.");
        }
        // For a list view, we want to show everything for each container.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();

        if (dtos.size() == 1) {
            return formatContainerReply(dtos.get(0), HEADER_FORMATTER.apply("Found Container:"), BULLET, options);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER_FORMATTER.apply("Found Containers:"));

        for (int i = 0; i < dtos.size(); i++) {
            String bullet = (i + 1) + ". ";
            // This reuses the existing logic perfectly and efficiently.
            // To switch back to numbered bullets, change BULLET to bullet
            sb.append(formatContainerReply(dtos.get(i), EMPTY, BULLET, options));
        }
        return sb.toString();
    }

    public static String formatSettingsUpdateReply(UserSettingsDto data) {
        StringBuilder sb = new StringBuilder(HEADER_FORMATTER.apply("Updated Settings:"));
        appendLine(sb, BULLET_INDENTED, "Hide User Command Responses", data.isEphemeralUser() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        appendLine(sb, BULLET_INDENTED, "Hide Container Command Responses", data.isEphemeralContainer() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        appendLine(sb, BULLET_INDENTED, "Hide Item Command Responses", data.isEphemeralItem() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        return sb.toString();
    }

    public static String formatGetItemReply(ItemSummaryDto itemDto) {
        return formatItemReply(itemDto, HEADER_FORMATTER.apply("Found Item:"), BULLET);
    }
    public static String formatItemReply(ItemSummaryDto itemDto, String header, String bullet) {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append(bullet).append(bold(itemDto.getItemName())).append("\n");

        appendLine(sb, BULLET_INDENTED, "Item Id", String.valueOf(itemDto.getItemId()), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);

        if (!"SYSTEM".equalsIgnoreCase(itemDto.getOwnerDisplayName())) {
            appendLine(sb, BULLET_INDENTED, "Owner", itemDto.getOwnerDisplayName(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }

        if (itemDto.getItemDescription() != null) {
            appendLine(sb, BULLET_INDENTED, "Description", itemDto.getItemDescription(), BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        }

        if (itemDto.getWeight() != null) {
            String weightValue = formatFloat(itemDto.getWeight());
            if (itemDto.getWeightUnit() != null && !itemDto.getWeightUnit().isBlank()) {
                weightValue += " " + italic(itemDto.getWeightUnit().trim());
            }
            appendLine(sb, BULLET_INDENTED, "Weight", weightValue, BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }
        if (itemDto.getValue() != null) {
            String value = formatFloat(itemDto.getValue());
            if (itemDto.getValueUnit() != null && !itemDto.getValueUnit().isBlank()) {
                value += " " + italic(itemDto.getValueUnit().trim());
            }
            appendLine(sb, BULLET_INDENTED, "Value", value, BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER);
        }

        return sb.toString();
    }

    public static String formatItemReply(List<ItemSummaryDto> dtos) {
        if (dtos.isEmpty()) {
            // This case is handled in the parser, but it's good practice to be defensive.
            return HEADER_FORMATTER.apply("No items found.");
        }
        if (dtos.size() == 1) {
            return formatItemReply(dtos.get(0), HEADER_FORMATTER.apply("Found Item:"), BULLET);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER_FORMATTER.apply("Found Items:"));
        for (int i = 0; i < dtos.size(); i++) {
            String bullet = (i + 1) + ". ";
            // This reuses the existing logic perfectly and efficiently.
            sb.append(formatItemReply(dtos.get(i), EMPTY, bullet)).append("\n");
        }
        return sb.toString();
    }

    public static String formatDeletedEntityReply(DeletedEntityDto dto) {
        return HEADER_FORMATTER.apply(String.format("Deleted %s:", dto.getEntityType()))
                + BULLET + bold(dto.getName()) + "\n"
                + BULLET_INDENTED + "ID: " + dto.getId() + "\n";
    }

}
