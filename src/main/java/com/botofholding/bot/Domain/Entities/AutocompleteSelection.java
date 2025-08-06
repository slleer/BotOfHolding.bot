package com.botofholding.bot.Domain.Entities;

/**
 * Represents a user's selection from an autocomplete list,
 * parsed from the standard "name:id" format.
 *
 * @param name The name part of the selection.
 * @param id   The ID part of the selection.
 */
public record AutocompleteSelection(String name, Long id) {}