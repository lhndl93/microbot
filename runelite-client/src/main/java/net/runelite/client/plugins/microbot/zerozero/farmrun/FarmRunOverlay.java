package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;
import java.awt.*;

public class FarmRunOverlay extends Overlay {

    private final Client client;

    @Inject
    public FarmRunOverlay(Client client) {
        this.client = client;
    }

    public void initialize() {
        // Add any necessary initialization code
    }

    public void shutdown() {
        // Add any necessary shutdown code
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setColor(Color.GREEN);
        graphics.drawString("Farm Run in Progress", 10, 10);
        return null;
    }
}
