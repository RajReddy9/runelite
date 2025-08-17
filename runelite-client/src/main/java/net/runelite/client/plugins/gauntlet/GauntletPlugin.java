package net.runelite.client.plugins.gauntlet;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.api.gameval.NpcID;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.List;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "Gauntlet",
        description = "Gauntlet"
)
public class GauntletPlugin extends Plugin {

    static final String CONFIG_GROUP = "gauntlet";

    @Getter
    List<NPC> tornadoes;
    Set<Integer> TORNADO_IDS = Set.of(NpcID.CRYSTAL_HUNLLEF_CRYSTALS_HM, NpcID.CRYSTAL_HUNLLEF_CRYSTALS);

    private boolean inGauntlet = false;
    private boolean hunleffRanging;
    private final int HUNLEFF_SWAP_RANGE = 8755;
    private final int HUNLEFF_SWAP_MAGE = 8754;

    private final Set<Integer> HUNLEFF_NPC_IDS = new HashSet<>(Set.of(9021, 9022, 9023, 9035, 9036, 9037));

    @Inject
    private NpcOverlayService npcOverlayService;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TornadoOverlay tornadoOverlay;

    @Override
    protected void startUp() {
        npcOverlayService.registerHighlighter(this::highlightHunleff);
        overlayManager.add(tornadoOverlay);
        tornadoes = new ArrayList<>();
    }

    @Override
    protected void shutDown() {
        npcOverlayService.unregisterHighlighter(this::highlightHunleff);
        overlayManager.remove(tornadoOverlay);
    }

    @Subscribe
    void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.PLAYER_IN_GAUNTLET && event.getValue() == 1) {
            inGauntlet = true;
            hunleffRanging = true;
        }
    }

    @Subscribe
    void onAnimationChanged(AnimationChanged event) {
        if (!inGauntlet) {
            return;
        }
        if (event.getActor().getAnimation() == HUNLEFF_SWAP_RANGE) {
            hunleffRanging = true;
            npcOverlayService.rebuild();

        } else if (event.getActor().getAnimation() == HUNLEFF_SWAP_MAGE) {
            hunleffRanging = false;
            npcOverlayService.rebuild();
        }
    }

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        if (!inGauntlet) {
            return;
        }
        NPC npc = event.getNpc();
        if (TORNADO_IDS.contains(npc.getId())) {
            tornadoes.add(npc);
        }
    }

    @Subscribe
    void onNpcDespawned(NpcDespawned event) {
        if (!inGauntlet) {
            return;
        }
        NPC npc = event.getNpc();
        if (TORNADO_IDS.contains(npc.getId())) {
            tornadoes.removeIf(t -> t == npc);
        }
    }

    private HighlightedNpc highlightHunleff(NPC npc) {
        if (HUNLEFF_NPC_IDS.contains(npc.getId())) {
            return HighlightedNpc.builder().npc(npc).highlightColor(hunleffRanging ? Color.GREEN : Color.BLUE).outline(true).build();
        }
        return null;
    }

}