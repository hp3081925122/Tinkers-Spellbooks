package org.hp.tinkers_spellbooks;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.hp.tinkers_spellbooks.client.ModularGeoTextureCache;
import org.hp.tinkers_spellbooks.client.SpellbooksClientSetup;
import org.hp.tinkers_spellbooks.config.SpellbooksConfig;
import org.hp.tinkers_spellbooks.event.SpellcastingModEvents;
import org.hp.tinkers_spellbooks.registry.SpellbookItems;
import org.hp.tinkers_spellbooks.registry.SpellbookModifiers;
import org.hp.tinkers_spellbooks.stats.MaterialFallbackGenerator;
import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;

@Mod(TinkersSpellbooks.MOD_ID)
public final class TinkersSpellbooks {
    public static final String MOD_ID = "tinkers_spellbooks";

    public TinkersSpellbooks(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, SpellbooksConfig.COMMON_SPEC);
        SpellbookToolStats.init();
        SpellbookItems.register(modBus);
        SpellbookModifiers.register(modBus);
        modBus.addListener(MaterialFallbackGenerator::registerStatTypes);
        MinecraftForge.EVENT_BUS.register(SpellcastingModEvents.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modBus.addListener(SpellbooksClientSetup::onClientSetup);
            modBus.addListener(ModularGeoTextureCache::onRegisterReloadListeners);
        });
    }
}
