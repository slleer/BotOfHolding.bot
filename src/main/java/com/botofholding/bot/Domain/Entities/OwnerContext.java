package com.botofholding.bot.Domain.Entities;

/**
 * A record that combines the identity of a command's target owner with
 * the business logic decision on how to handle the reply (e.g., ephemeral).
 * This is a reusable model for many command parsers.
 *
 * @param ownerId The Discord ID of the owner (User or Guild).
 * @param ownerName The display name of the owner.
 * @param ownerType The type of owner ("USER" or "GUILD").
 * @param useEphemeral Whether the reply to the user should be ephemeral.
 */
public record OwnerContext(Long ownerId, String ownerName, String ownerType, boolean useEphemeral) {}