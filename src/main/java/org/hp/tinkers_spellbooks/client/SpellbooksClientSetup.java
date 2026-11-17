package org.hp.tinkers_spellbooks.client;

import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.hp.tinkers_spellbooks.registry.SpellbookItems;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class SpellbooksClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TinkerItemProperties.registerToolProperties(SpellbookItems.ARCANE_STAFF.get());
            TinkerItemProperties.registerToolProperties(SpellbookItems.MODULAR_SPELLBOOK.get());
            CuriosRendererRegistry.register(SpellbookItems.MODULAR_SPELLBOOK.get(), SpellBookCurioRenderer::new);
            SpellbookBookSections.register();
        });
    }

    private SpellbooksClientSetup() {
    }
}
