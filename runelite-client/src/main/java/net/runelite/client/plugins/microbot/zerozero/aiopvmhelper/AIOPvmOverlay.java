package net.runelite.client.plugins.microbot.zerozero.aiopvmhelper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.models.PRAY_MODE;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class AIOPvmOverlay extends OverlayPanel {

    private final AIOPvmPlugin plugin;

    @Inject
    AIOPvmOverlay(AIOPvmPlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("BossAssist" + AIOPvmScript.version)
                    .color(Color.GREEN)
                    .build());


            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(plugin.aioPvmScript.currentBoss.toString())
                    .build());

            if (plugin.aioPvmScript.config.PRAYER_MODE() == PRAY_MODE.VISUAL) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(plugin.aioPvmScript.prayStyle.toString())
                        .build());
            }


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
