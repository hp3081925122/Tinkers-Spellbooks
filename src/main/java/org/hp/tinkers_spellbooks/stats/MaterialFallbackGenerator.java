package org.hp.tinkers_spellbooks.stats;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.hp.tinkers_spellbooks.config.SpellbooksConfig;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MaterialFallbackGenerator {
    public static final List<MaterialStatsId> ALL_STAT_IDS = List.of(
            StaffHeadMaterialStats.ID,
            StaffHandleMaterialStats.ID,
            BookCoverMaterialStats.ID,
            SpellPagesMaterialStats.ID,
            ArcaneBindingMaterialStats.ID
    );

    public static void registerStatTypes(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            registry.registerStatType(StaffHeadMaterialStats.TYPE, MaterialRegistry.MELEE_HARVEST);
            registry.registerStatType(StaffHandleMaterialStats.TYPE, MaterialRegistry.MELEE_HARVEST);
            registry.registerStatType(BookCoverMaterialStats.TYPE, MaterialRegistry.ARMOR);
            registry.registerStatType(SpellPagesMaterialStats.TYPE, MaterialRegistry.RANGED);
            registry.registerStatType(ArcaneBindingMaterialStats.TYPE, MaterialRegistry.MELEE_HARVEST);
        });
    }

    public static MaterialStatsId getFallbackTextureStat(MaterialStatsId id) {
        if (StaffHeadMaterialStats.ID.equals(id)) {
            return HeadMaterialStats.ID;
        }
        if (StaffHandleMaterialStats.ID.equals(id)) {
            return HandleMaterialStats.ID;
        }
        if (BookCoverMaterialStats.ID.equals(id)) {
            return PlatingMaterialStats.CHESTPLATE.getId();
        }
        if (SpellPagesMaterialStats.ID.equals(id)) {
            return StatlessMaterialStats.MAILLE.getIdentifier();
        }
        return StatlessMaterialStats.BINDING.getIdentifier();
    }

    public static Map<MaterialId, Map<MaterialStatsId, IMaterialStats>> populateMissingStats(Map<MaterialId, Map<MaterialStatsId, IMaterialStats>> original) {
        if (!SpellbooksConfig.AUTO_GENERATE_FALLBACK_STATS.get() || original.isEmpty()) {
            return original;
        }
        Map<MaterialId, Map<MaterialStatsId, IMaterialStats>> result = new HashMap<>(original.size());
        for (Map.Entry<MaterialId, Map<MaterialStatsId, IMaterialStats>> entry : original.entrySet()) {
            Map<MaterialStatsId, IMaterialStats> existing = entry.getValue();
            if (existing.isEmpty()) {
                result.put(entry.getKey(), existing);
                continue;
            }
            Map<MaterialStatsId, IMaterialStats> mutable = new HashMap<>(existing);
            HeadMaterialStats head = getTyped(existing, HeadMaterialStats.ID, HeadMaterialStats.class);
            LimbMaterialStats limb = getTyped(existing, LimbMaterialStats.ID, LimbMaterialStats.class);
            HandleMaterialStats handle = getTyped(existing, HandleMaterialStats.ID, HandleMaterialStats.class);
            GripMaterialStats grip = getTyped(existing, GripMaterialStats.ID, GripMaterialStats.class);
            PlatingMaterialStats chest = getTyped(existing, PlatingMaterialStats.CHESTPLATE.getId(), PlatingMaterialStats.class);
            boolean hasBinding = existing.containsKey(StatlessMaterialStats.BINDING.getIdentifier());
            boolean hasMaille = existing.containsKey(StatlessMaterialStats.MAILLE.getIdentifier()) || existing.containsKey(StatlessMaterialStats.CUIRASS.getIdentifier());
            boolean hasBowstring = existing.containsKey(StatlessMaterialStats.BOWSTRING.getIdentifier());

            if (!mutable.containsKey(StaffHeadMaterialStats.ID) && (head != null || limb != null || chest != null)) {
                int durability = head != null ? head.durability() : (limb != null ? limb.durability() : Math.max(80, chest.durability()));
                float attack = head != null ? Math.max(1.0f, round2(head.attack() * 0.85f)) : 1.5f;
                float powerSource = head != null ? head.attack() * 0.02f + head.miningSpeed() * 0.006f
                        : (limb != null ? 0.06f + Math.max(0.0f, limb.velocity() + limb.drawSpeed()) * 0.15f : 0.04f + chest.armor() * 0.012f);
                float spellPower = round2(Mth.clamp(powerSource, 0.03f, 0.18f));
                float slots = (durability >= 750 || (head != null && head.attack() >= 4.0f)) ? 2.0f : 1.0f;
                mutable.put(StaffHeadMaterialStats.ID, new StaffHeadMaterialStats(Math.max(40, durability), attack, spellPower, slots));
            }

            if (!mutable.containsKey(StaffHandleMaterialStats.ID) && (handle != null || grip != null || head != null || limb != null)) {
                float durPct = handle != null ? handle.durability() : (grip != null ? grip.durability() : 0.0f);
                float speedPct = handle != null ? handle.meleeSpeed() : 0.0f;
                float cdr = handle != null ? round2(Mth.clamp(0.03f + handle.miningSpeed() * 0.2f, -0.05f, 0.12f))
                        : (grip != null ? round2(Mth.clamp(0.03f + grip.accuracy() * 0.15f, -0.05f, 0.10f)) : 0.03f);
                float castRed = handle != null ? round2(Mth.clamp(0.03f + handle.meleeSpeed() * 0.2f, -0.05f, 0.12f)) : 0.03f;
                mutable.put(StaffHandleMaterialStats.ID, new StaffHandleMaterialStats(durPct, speedPct, cdr, castRed));
            }

            if (!mutable.containsKey(BookCoverMaterialStats.ID) && (chest != null || head != null || hasMaille || limb != null || hasBowstring)) {
                int durability = chest != null ? Math.max(80, chest.durability()) : (head != null ? Math.max(60, head.durability()) : (limb != null ? Math.max(60, limb.durability()) : 160));
                float manaBase = chest != null ? 25.0f + chest.armor() * 10.0f + chest.toughness() * 12.0f
                        : (head != null ? 20.0f + head.durability() * 0.08f : 45.0f);
                float maxMana = Math.round(Mth.clamp(manaBase, 20.0f, 150.0f) / 5.0f) * 5.0f;
                float spellPower = chest != null ? round2(Mth.clamp(0.02f + chest.armor() * 0.01f + chest.toughness() * 0.01f, 0.02f, 0.12f))
                        : (head != null ? round2(Mth.clamp(0.02f + head.attack() * 0.015f, 0.02f, 0.12f)) : 0.04f);
                float cdr = round2(Mth.clamp(durability >= 600 ? 0.05f : 0.02f, 0.0f, 0.08f));
                mutable.put(BookCoverMaterialStats.ID, new BookCoverMaterialStats(durability, maxMana, spellPower, cdr));
            }

            if (!mutable.containsKey(SpellPagesMaterialStats.ID) && (hasMaille || hasBowstring || hasBinding || limb != null || head != null)) {
                int qualityScore = head != null ? head.durability() : (limb != null ? limb.durability() : (hasMaille ? 320 : 220));
                float slots = qualityScore >= 900 ? 8.0f : (qualityScore >= 500 ? 7.0f : (qualityScore >= 250 ? 6.0f : 5.0f));
                float maxMana = Math.round(Mth.clamp(15.0f + qualityScore * 0.05f, 15.0f, 80.0f) / 5.0f) * 5.0f;
                float castRed = limb != null ? round2(Mth.clamp(0.03f + Math.max(0.0f, limb.drawSpeed()) * 0.18f, 0.01f, 0.10f)) : 0.03f;
                mutable.put(SpellPagesMaterialStats.ID, new SpellPagesMaterialStats(slots, maxMana, castRed));
            }

            if (!mutable.containsKey(ArcaneBindingMaterialStats.ID) && (hasBinding || hasBowstring || hasMaille || handle != null || head != null)) {
                int qualityScore = head != null ? head.durability() : (limb != null ? limb.durability() : 250);
                float maxMana = Math.round(Mth.clamp(15.0f + qualityScore * 0.04f, 15.0f, 60.0f) / 5.0f) * 5.0f;
                float spellPower = round2(Mth.clamp(0.01f + qualityScore * 0.00005f, 0.02f, 0.07f));
                float cdr = round2(Mth.clamp(0.01f + qualityScore * 0.00004f, 0.02f, 0.06f));
                mutable.put(ArcaneBindingMaterialStats.ID, new ArcaneBindingMaterialStats(maxMana, spellPower, cdr));
            }

            result.put(entry.getKey(), ImmutableMap.copyOf(mutable));
        }
        return result;
    }

    private static <T extends IMaterialStats> T getTyped(Map<MaterialStatsId, IMaterialStats> map, MaterialStatsId id, Class<T> clazz) {
        IMaterialStats stats = map.get(id);
        return clazz.isInstance(stats) ? clazz.cast(stats) : null;
    }

    private static float round2(float value) {
        return Math.round(value * 100.0f) / 100.0f;
    }

    private MaterialFallbackGenerator() {
    }
}
