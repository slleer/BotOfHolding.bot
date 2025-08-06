package com.botofholding.bot.Utility;

public final class CommandConstants {
    private CommandConstants() {}

    // --- Generic, Reusable Option Names ---
    // Use these when the option's meaning is identical across commands.
    public static final String OPTION_NAME = "name";
    public static final String OPTION_DESCRIPTION = "description";
    public static final String OPTION_TYPE = "type";
    public static final String OPTION_SERVER_OWNED = "server-owned";
    public static final String OPTION_ID = "id";
    public static final String OPTION_PRIORITIZE = "prioritize";
    public static final String OPTION_QUANTITY = "quantity";
    public static final String OPTION_PARENT = "inside-this-item";
    public static final String OPTION_DROP_CHILDREN = "drop-contents";
    public static final int DISCORD_CHOICE_LIMIT = 25;

    // --- Generic, Reusable Entity based options ---
    public static final String OPTION_ITEM = "item";

    // [NEW] --- Top-Level Command Names ---
    public static final String CMD_USER = "user";
    public static final String CMD_CONTAINER = "container";
    public static final String CMD_ITEM = "item";
    public static final String CMD_INVENTORY = "inventory";

    // --- User Command ---
    public static final String SUBCMD_USER_GET = "get";
    public static final String CONTEXT_USER_GET = "user-get";

    public static final String SUBCMD_USER_UPDATE = "update";
    public static final String CONTEXT_USER_UPDATE = "user-update";

    public static final String SUBCMD_USER_SETTINGS = "settings";
    public static final String CONTEXT_USER_SETTINGS = "user-settings";
    public static final String OPTION_USER_SETTINGS_HIDE_CONTAINER = "hide-container";
    public static final String OPTION_USER_SETTINGS_HIDE_USER = "hide-user";
    public static final String OPTION_USER_SETTINGS_HIDE_ITEM = "hide-item";

    // --- Container Command ---
    public static final String SUBCMD_CONTAINER_ADD = "new";
    public static final String CONTEXT_CONTAINER_ADD = "container-new";
    public static final String OPTION_CONTAINER_ADD_SET_AS_ACTIVE = "set-as-active";

    public static final String SUBCMD_CONTAINER_ACTIVATE = "set-active";
    public static final String CONTEXT_CONTAINER_ACTIVATE = "container-set-active";

    public static final String SUBCMD_CONTAINER_FIND_ACTIVE = "find-active";
    public static final String CONTEXT_CONTAINER_FIND_ACTIVE = "container-find-active";

    public static final String SUBCMD_CONTAINER_FIND = "find";
    public static final String CONTEXT_CONTAINER_FIND = "container-find";

    public static final String SUBCMD_CONTAINER_DELETE = "delete";
    public static final String CONTEXT_CONTAINER_DELETE = "container-delete";

    // --- Item Command ---
    public static final String SUBCMD_ITEM_FIND = "find";
    public static final String CONTEXT_ITEM_FIND = "item-find";

    public static final String SUBCMD_ITEM_NEW = "new";
    public static final String CONTEXT_ITEM_NEW = "item-new";

    // --- Inventory Command ---
    public static final String SUBCMD_INVENTORY_ADD = "add";
    public static final String CONTEXT_INVENTORY_ADD = "inventory-add";
    public static final String OPTION_NOTE = "note";

    public static final String SUBCMD_INVENTORY_DROP = "drop";
    public static final String CONTEXT_INVENTORY_DROP = "inventory-drop";


}