package com.botofholding.bot.Exception;

/**
 * A custom, unchecked exception used to signal that a command's execution
 * should be halted and a specific, user-facing message should be sent as an
 * ephemeral reply.
 * <p>
 * This is intended for expected "sad paths" (e.g., "resource not found")
 * and should not be used for unexpected technical errors, which should be
 * allowed to propagate to the main error handlers.
 */
public class ReplyException extends RuntimeException {
    public ReplyException(String message) {
        super(message);
    }
}