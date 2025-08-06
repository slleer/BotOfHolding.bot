package com.botofholding.bot.Domain.Enum;

import java.util.stream.Stream;

public enum OwnerType {
    USER(0),
    GUILD(1);

    private final int code;

    OwnerType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * A robust, static lookup method to find an OwnerType by its integer code.
     * This is the recommended way to convert an int to an OwnerType.
     *
     * @param code The integer code to look up (e.g., 0 for USER).
     * @return The matching OwnerType constant.
     * @throws IllegalArgumentException if no OwnerType with the given code exists.
     */
    public static OwnerType fromCode(int code) {
        return Stream.of(OwnerType.values())
                .filter(targetEnum -> targetEnum.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown OwnerType code: " + code));
    }
}