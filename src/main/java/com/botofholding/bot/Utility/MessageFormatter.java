package com.botofholding.bot.Utility;

import java.math.BigDecimal;

import com.botofholding.bot.Domain.DTOs.Response.*;
import com.botofholding.bot.Utility.config.ContainerDisplayOptions;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

public final class MessageFormatter {

    private static final int DISCORD_CHAR_LIMIT = 1950; // Safety margin for message length
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
    private static final Function<String, String> SUB_BULLET_INDENT = indent -> "  " + indent;


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

    /**
     * Appends content to the last chunk in a list of StringBuilders. If the content would exceed
     * the character limit, it creates a new chunk, prepends the original header, and adds the content there.
     *
     * @param chunks The list of message chunks being built.
     * @param content The new string content to append.
     * @param header The header to use if a new chunk is created.
     */
    private static void appendWithSplitting(List<StringBuilder> chunks, String content, String header) {
        StringBuilder lastChunk = chunks.get(chunks.size() - 1);
        if (lastChunk.length() + content.length() > DISCORD_CHAR_LIMIT) {
            StringBuilder newChunk = new StringBuilder();
            // Start the new message with the original header for context.
            if (header != null && !header.isBlank()) {
                newChunk.append(header);
            }
            newChunk.append(content);
            chunks.add(newChunk);
        } else {
            lastChunk.append(content);
        }
    }

    /**
     * A wrapper around {@link #appendWithSplitting} that first formats a line of text.
     *
     * @param chunks The list of message chunks being built.
     * @param prefix The string to prepend to the line (e.g., indent).
     * @param label The text for the label part.
     * @param value The text for the value part.
     * @param labelFormatter A function to style the label.
     * @param valueFormatter A function to style the value.
     * @param header The header to use if a new chunk is created.
     */
    private static void appendLineWithSplitting(
            List<StringBuilder> chunks, String prefix, String label, String value,
            Function<String, String> labelFormatter, Function<String, String> valueFormatter, String header
    ) {
        if (value != null && !value.trim().isEmpty()) {
            String line = prefix + labelFormatter.apply(label) + " " + valueFormatter.apply(value) + "\n";
            appendWithSplitting(chunks, line, header);
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

    public static List<String> formatGetContainerReply(ContainerSummaryDto containerDto) {
        // Default options: show everything.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(containerDto, SUBHEADER_FORMATTER.apply("Found Container:"), BULLET, options);
    }

    public static List<String> formatUseContainerReply(ContainerSummaryDto containerDto) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(containerDto, SUBHEADER_FORMATTER.apply("Using Container:"), BULLET, options);
    }

    public static List<String> formatAddContainerReply(ContainerSummaryDto containerDto) {
        // On creation, the item list is always empty, so don't display it.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder()
                .displayItems(false)
                .build();
        return formatContainerReply(containerDto, SUBHEADER_FORMATTER.apply("New Container:"), BULLET, options);
    }

    public static List<String> formatActiveContainerReply(ContainerSummaryDto activeContainer) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().build();
        return formatContainerReply(activeContainer, SUBHEADER_FORMATTER.apply("Active Container:"), BULLET, options);
    }

    public static List<String> formatAddInventoryContainerReply(ContainerSummaryDto containerDto, String successMessage) {
        // TODO - decide if the add, drop, and modify formatter messages should be combined due to similar logic.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().displayStatus(false).build();
        return formatContainerReply(containerDto, SUBHEADER_FORMATTER.apply(successMessage), BULLET, options);
    }

    public static List<String> formatDropInventoryContainerReply(ContainerSummaryDto updatedContainer, String successMessage) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().displayStatus(false).build();
        return formatContainerReply(updatedContainer, SUBHEADER_FORMATTER.apply(successMessage), BULLET, options);
    }

    public static List<String> formatModifyInventoryContainerReply(ContainerSummaryDto containerDto, String successMessage) {
        ContainerDisplayOptions options = ContainerDisplayOptions.builder().displayStatus(false).build();
        return formatContainerReply(containerDto, SUBHEADER_FORMATTER.apply(successMessage), BULLET, options);
    }


    // TODO - implement system to deal with the 2000 character limit using either editReply, createFollowup, or threads with createMessage. Most likely not editReply (seems more suited to deferred responses).
    private static List<String> formatContainerReply(ContainerSummaryDto containerDto, String header, String bullet, ContainerDisplayOptions options) {
        List<StringBuilder> chunks = new ArrayList<>();
        chunks.add(new StringBuilder()); // Start with the first chunk

        String initialLine = header + bullet + bold(containerDto.getContainerName()) + "\n";
        String continuedHeader = header + " (cont.)";
        appendWithSplitting(chunks, initialLine, header);

        String subBullet = SUB_BULLET_INDENT.apply(bullet.contains(".") ? BULLET : bullet);

        if (options.isDisplayOwner()) {
            appendLineWithSplitting(chunks, subBullet, "Owner", containerDto.getOwnerDisplayName(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }
        if (options.isDisplayContainerId() && containerDto.getContainerId() != null) {
            appendLineWithSplitting(chunks, subBullet, "Id", String.valueOf(containerDto.getContainerId()), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }
        if (options.isDisplayDescription() && containerDto.getContainerDescription() != null) {
            appendLineWithSplitting(chunks, subBullet, "Description", containerDto.getContainerDescription(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }
        if (options.isDisplayType() && containerDto.getContainerTypeName() != null) {
            appendLineWithSplitting(chunks, subBullet, "Type", containerDto.getContainerTypeName(), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }
        if (options.isDisplayStatus() && containerDto.isActive()){
            appendLineWithSplitting(chunks, subBullet, "Status", "Active", BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }
        if (options.isDisplayLastActive() && containerDto.getLastActiveDateTime() != null) {
            appendLineWithSplitting(chunks, subBullet, "Last Active", containerDto.getLastActiveDateTime().format(DATE_TIME_FORMATTER), BOLD_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
        }

        if (options.isDisplayItems() && containerDto.getItems() != null && !containerDto.getItems().isEmpty()) {
//            sb.append(subBullet).append(bold("Contents:")).append("\n");

            formatChildrenContainerItems(containerDto.getItems(), options, chunks, subBullet, continuedHeader);
//            containerDto.getItems().stream()
//                .sorted(java.util.Comparator.comparing(ContainerItemSummaryDto::getItemName))
//                .forEach(item -> {
//                    sb.append(subBullet).append(item.getItemName());
//                    String x2_SubBullet = SUB_BULLET_INDENT.apply(subBullet);
//                    if (item.getQuantity() > 1) {
//                        sb.append(" ").append(bold(String.format("x%d", item.getQuantity())));
//                    }
//                    sb.append("\n");
//                    if (options.isDisplayNote() && item.getUserNote() != null && !item.getUserNote().isBlank()) {
//                        appendLineWithSplitting(chunks, x2_SubBullet, "-# ", item.getUserNote(), EMPTY_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER, continuedHeader);
//                    }
//                    if (options.isDisplayChildrenItems() && item.getChildren() != null && !item.getChildren().isEmpty()) {
//                        formatContainerItems(item.getChildren(), options, sb, x2_SubBullet);
//                    }
//                    if (options.isDisplayContainerItemId()) {
//                        appendLineWithSplitting(chunks, x2_SubBullet, "Id", String.valueOf(item.getContainerItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
//                    }
//                    if (options.isDisplayItemId()) {
//                        appendLineWithSplitting(chunks, x2_SubBullet, "Item Id", String.valueOf(item.getItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
//                    }
//                    if (options.isDisplayLastModified()){
//                        appendLineWithSplitting(chunks, x2_SubBullet, "Last Modified", item.getLastModified().format(DATE_TIME_FORMATTER), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
//                    }
//                });
        }
        return chunks.stream().map(StringBuilder::toString).filter(s -> !s.isEmpty()).toList();
    }

    private static void formatChildrenContainerItems(List<ContainerItemSummaryDto> items, ContainerDisplayOptions options, List<StringBuilder> chunks, String subBullet, String continuedHeader) {

        items.stream()
                .sorted(java.util.Comparator.comparing(ContainerItemSummaryDto::getItemName))
                .forEach(item -> {
                    String initialChildLine = subBullet + item.getItemName() + (item.getQuantity() > 1 ? " " + bold(String.format("x%d", item.getQuantity())) : "") + "\n";
                    // The item's main line was being calculated but never added to the output.
                    appendWithSplitting(chunks, initialChildLine, continuedHeader);
                    String x2_SubBullet = SUB_BULLET_INDENT.apply(subBullet);
                    if (options.isDisplayNote() && item.getUserNote() != null && !item.getUserNote().isBlank()) {
                        appendLineWithSplitting(chunks, x2_SubBullet, "-# ", item.getUserNote(), EMPTY_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER, continuedHeader);
                    }
                    if (options.isDisplayChildrenItems() && item.getChildren() != null && !item.getChildren().isEmpty()) {
                        formatChildrenContainerItems(item.getChildren(), options, chunks, x2_SubBullet, continuedHeader);
                    }
                    if (options.isDisplayContainerItemId()) {
                        appendLineWithSplitting(chunks, x2_SubBullet, "Id", String.valueOf(item.getContainerItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
                    }
                    if (options.isDisplayItemId()) {
                        appendLineWithSplitting(chunks, x2_SubBullet, "Item Id", String.valueOf(item.getItemId()), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
                    }
                    if (options.isDisplayLastModified()){
                        appendLineWithSplitting(chunks, x2_SubBullet, "Last Modified", item.getLastModified().format(DATE_TIME_FORMATTER), PLAIN_LABEL_FORMATTER, PLAIN_VALUE_FORMATTER, continuedHeader);
                    }
                });

    }

    public static List<String> formatContainerReply(List<ContainerSummaryDto> dtos) {
        if (dtos.isEmpty()) {
            // This case is handled in the parser, but it's good practice to be defensive.
            return List.of(HEADER_FORMATTER.apply("No Container(s) Found."));
        }
        // For a list view, we want to show everything for each container.
        ContainerDisplayOptions options = ContainerDisplayOptions.builder()
                .displayItems(false)
                .displayStatus(false)
                .build();

        if (dtos.size() == 1) {
            return formatContainerReply(dtos.get(0), SUBHEADER_FORMATTER.apply("Found Container:"), BULLET, options);
        }

        List<StringBuilder> chunks = new ArrayList<>();
        chunks.add(new StringBuilder());
        String header = SUBHEADER_FORMATTER.apply("Found Containers:");
        String continuedHeader = header + " (cont.)";
        appendWithSplitting(chunks, header, continuedHeader);

        for (int i = 0; i < dtos.size(); i++) {
            String bullet = (i + 1) + ". ";
            List<String> formattedContainer = formatContainerReply(dtos.get(i), EMPTY, bullet, options);
            for (String part : formattedContainer) {
                appendWithSplitting(chunks, part, continuedHeader);
            }
        }
        return chunks.stream().map(StringBuilder::toString).filter(s -> !s.isEmpty()).toList();
    }

    public static String formatSettingsUpdateReply(UserSettingsDto data) {
        StringBuilder sb = new StringBuilder(HEADER_FORMATTER.apply("Updated Settings:"));
        appendLine(sb, BULLET_INDENTED, "Hide User Command Responses", data.isEphemeralUser() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        appendLine(sb, BULLET_INDENTED, "Hide Container Command Responses", data.isEphemeralContainer() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        appendLine(sb, BULLET_INDENTED, "Hide Item Command Responses", data.isEphemeralItem() ? "True" : "False", BOLD_LABEL_FORMATTER, ITALIC_VALUE_FORMATTER);
        return sb.toString();
    }

    public static List<String> formatGetItemReply(ItemSummaryDto itemDto) {
        return List.of(formatSingleItem(itemDto, HEADER_FORMATTER.apply("Found Item:"), BULLET));
    }

    /**
     * Formats a single item's details into a string. This is a private helper
     * to support the public-facing formatters.
     *
     * @param itemDto The item to format.
     * @param header  The header to prepend.
     * @param bullet  The bullet style to use.
     * @return A formatted string representing the item.
     */
    private static String formatSingleItem(ItemSummaryDto itemDto, String header, String bullet) {
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

    public static List<String> formatItemReply(List<ItemSummaryDto> dtos) {
        if (dtos.isEmpty()) {
            // This case is handled in the parser, but it's good practice to be defensive.
            return List.of(HEADER_FORMATTER.apply("No items found."));
        }
        if (dtos.size() == 1) {
            return formatGetItemReply(dtos.get(0));
        }

        List<StringBuilder> chunks = new ArrayList<>();
        chunks.add(new StringBuilder());
        String header = HEADER_FORMATTER.apply("Found Items:");
        String continuedHeader = HEADER_FORMATTER.apply("Found Items (cont.):");
        appendWithSplitting(chunks, header, continuedHeader);

        for (int i = 0; i < dtos.size(); i++) {
            String bullet = (i + 1) + ". ";
            // This reuses the existing logic perfectly and efficiently.
            String formattedItem = formatSingleItem(dtos.get(i), EMPTY, bullet);
            appendWithSplitting(chunks, formattedItem + "\n", continuedHeader);
        }
        return chunks.stream().map(StringBuilder::toString).filter(s -> !s.isEmpty()).toList();
    }

    public static List<String> formatDeletedEntityReply(DeletedEntityDto dto) {
        String message = HEADER_FORMATTER.apply(String.format("Deleted %s:", dto.getEntityType()))
                + BULLET + bold(dto.getName()) + "\n"
                + BULLET_INDENTED + "ID: " + dto.getId() + "\n";
        return List.of(message);
    }

}
