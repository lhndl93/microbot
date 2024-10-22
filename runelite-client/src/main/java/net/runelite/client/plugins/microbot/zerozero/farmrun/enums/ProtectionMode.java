package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

public enum ProtectionMode {
    USE_COMPOST("Use Compost"),
    PROTECT_PATCH_WITH_ITEMS("Protect with items"),
    NO_PROTECTION("Don't protect"),;

    private final String name;

    ProtectionMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
