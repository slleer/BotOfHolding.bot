package com.botofholding.bot.Domain.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    private String bohUserName;
    private String bohUserTag;
    private String bohGlobalUserName;

    public UserRequestDto(String bohUserName, String bohUserTag) {
        this.bohUserName = bohUserName;
        this.bohUserTag = bohUserTag;
    }

    public UserRequestDto(String bohUserName, String bohUserTag, Optional<String> bohGlobalUserName) {
        this(bohUserName, bohUserTag);

        if (bohGlobalUserName.isPresent())
            this.bohGlobalUserName = bohGlobalUserName.get();
    }
}
