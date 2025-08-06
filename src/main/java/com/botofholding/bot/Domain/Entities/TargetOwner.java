package com.botofholding.bot.Domain.Entities;
/**
 * A simple record to hold the identity of a command's target owner,
 * as determined by parsing a Discord event.
 *
 * @param ownerId The Discord ID of the owner (User or Guild).
 * @param ownerName The display name of the owner.
 * @param ownerType The type of owner ("USER" or "GUILD").
 */
public record TargetOwner(Long ownerId, String ownerName, String ownerType) {}
