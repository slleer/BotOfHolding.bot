package com.botofholding.bot.Domain.Entities;

import java.util.List;

public record Reply(List<String> message, boolean isEphemeral) {}

