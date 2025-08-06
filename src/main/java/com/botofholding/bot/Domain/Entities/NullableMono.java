package com.botofholding.bot.Domain.Entities;

import lombok.Getter;

@Getter
public class NullableMono {
    public int guide;
    public Boolean boolValue;
    public String stringValue;
    public Long longValue;

    public NullableMono(Boolean boolVal) {
        guide = 1;
        this.boolValue = boolVal;
    }

    public NullableMono(String strVal) {
        guide = 2;
        this.stringValue = strVal;
    }

    public NullableMono(Long longVal) {
        guide = 3;
        this.longValue = longVal;
    }

    public Object getValue() {
        return switch (guide) {
            case 1 -> boolValue;
            case 2 -> stringValue;
            case 3 -> longValue;
            default -> null;
        };
    }
}
