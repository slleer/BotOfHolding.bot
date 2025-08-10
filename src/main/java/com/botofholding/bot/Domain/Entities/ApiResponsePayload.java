package com.botofholding.bot.Domain.Entities;

/**
 * A generic, immutable wrapper to carry both the data and the success message from an API response.
 */
public record ApiResponsePayload<T>(T data, String message) {}