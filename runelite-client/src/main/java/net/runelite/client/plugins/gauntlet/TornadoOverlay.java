package net.runelite.client.plugins.gauntlet;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayLayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.BasicStroke;

@Singleton
@Slf4j
public class TornadoOverlay extends Overlay {

    private final Client client;
    private final GauntletPlugin plugin;

    @Inject
    public TornadoOverlay(Client client, GauntletPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        renderTornadoes(graphics);
        return null;
    }

    private boolean isDangerous(LocalPoint tornado) {
        WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
        if (playerPos == null) {
            return false;
        }
        LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
        if (playerPosLocal == null) {
            return false;
        }
        return playerPosLocal.distanceTo(tornado) <= 181;
    }

    private void renderTornadoes(Graphics2D graphics) {
        List<NPC> tornadoes = plugin.getTornadoes();
        for (NPC tornado : tornadoes) {
            Polygon polygon;
            WorldPoint worldPoint = tornado.getWorldLocation();
            if (worldPoint == null) {
                continue;
            }
            LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
            if (localPoint == null) {
                continue;
            }
            Color outlineColor = isDangerous(localPoint) ? Color.RED : Color.GREEN;
            Color fillColor = new Color(0, 0, 0, 50);
            polygon = Perspective.getCanvasTilePoly(client, localPoint);
            if (polygon == null) {
                continue;
            }
            OverlayUtil.renderPolygon(graphics, polygon, outlineColor, fillColor,
                    new BasicStroke(1));
        }
    }
}
