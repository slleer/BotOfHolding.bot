package com.botofholding.bot.Utility.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A configuration object to control which fields of a container are displayed in a formatted message.
 * Use the static {@link Builder} class to construct an instance.
 */
@Getter
public class ContainerDisplayOptions {

    private final boolean displayOwner;
    private final boolean displayDescription;
    private final boolean displayType;
    private final boolean displayStatus;
    private final boolean displayLastActive;
    private final boolean displayItemsLabel;
    private final boolean displayItems;
    private final boolean displayNote;
    private final boolean displayContainerItemId;
    private final boolean displayItemId;
    private final boolean displayLastModified;

    private ContainerDisplayOptions(Builder builder) {
        this.displayOwner = builder.displayOwner;
        this.displayDescription = builder.displayDescription;
        this.displayType = builder.displayType;
        this.displayStatus = builder.displayStatus;
        this.displayItemsLabel = builder.displayItemsLabel;
        this.displayItems = builder.displayItems;
        this.displayNote = builder.displayNote;
        this.displayLastActive = builder.displayLastActive;;
        this.displayContainerItemId = builder.displayContainerItemId;
        this.displayItemId = builder.displayItemId;
        this.displayLastModified = builder.displayLastModified;
    }

    /**
     * Creates a new builder with default settings (all fields displayed).
     * @return A new instance of the Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating {@link ContainerDisplayOptions} instances.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private boolean displayOwner = true;
        private boolean displayDescription = true;
        private boolean displayType = true;
        private boolean displayStatus = true;
        private boolean displayItemsLabel = true;
        private boolean displayItems = true;
        private boolean displayLastActive = true;
        private boolean displayNote = true;
        private boolean displayContainerItemId = true;
        private boolean displayItemId = true;
        private boolean displayLastModified = true;

        public Builder displayOwner(boolean displayOwner) {
            this.displayOwner = displayOwner;
            return this;
        }

        public Builder displayDescription(boolean displayDescription) {
            this.displayDescription = displayDescription;
            return this;
        }

        public Builder displayType(boolean displayType) {
            this.displayType = displayType;
            return this;
        }

        public Builder displayStatus(boolean displayStatus) {
            this.displayStatus = displayStatus;
            return this;
        }

        public Builder displayItemsLabel(boolean displayItemsLabel) {
            this.displayItemsLabel = displayItemsLabel;
            return this;
        }

        public Builder displayItems(boolean displayItems) {
            this.displayItems = displayItems;
            return this;
        }

        public Builder displayLastActive(boolean displayLastActive) {
            this.displayLastActive = displayLastActive;
            return this;
        }

        public Builder displayNote(boolean displayNote) {
            this.displayNote = displayNote;
            return this;
        }

        public Builder displayContainerItemId(boolean displayContainerItemId) {
            this.displayContainerItemId = displayContainerItemId;
            return this;
        }

        public Builder displayItemId(boolean displayItemId) {
            this.displayItemId = displayItemId;
            return this;
        }

        public Builder displayLastModified(boolean displayLastModified) {
            this.displayLastModified = displayLastModified;
            return this;
        }


        public ContainerDisplayOptions build() {
            return new ContainerDisplayOptions(this);
        }

    }
}