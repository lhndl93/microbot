package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

public enum ProtectionMode {
    PROTECT_PATCH("Protect Patch"),
    PAY_TO_PROTECT("Pay to Protect");

    private final String name;

    ProtectionMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
