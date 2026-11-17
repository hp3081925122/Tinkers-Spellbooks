package org.hp.tinkers_spellbooks.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

public record SpellPagesMaterialStats(float spellSlots, float maxMana, float castTimeReduction) implements IMaterialStats.ScaledTooltip {
    public static final MaterialStatsId ID = new MaterialStatsId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "spell_pages"));
    public static final MaterialStatType<SpellPagesMaterialStats> TYPE = new MaterialStatType<>(
            ID,
            new SpellPagesMaterialStats(5.0f, 25.0f, 0.03f),
            RecordLoadable.create(
                    FloatLoadable.FROM_ZERO.defaultField("spell_slots", 5.0f, true, SpellPagesMaterialStats::spellSlots),
                    FloatLoadable.FROM_ZERO.defaultField("max_mana", 25.0f, true, SpellPagesMaterialStats::maxMana),
                    FloatLoadable.ANY.defaultField("cast_time_reduction", 0.03f, true, SpellPagesMaterialStats::castTimeReduction),
                    SpellPagesMaterialStats::new
            )
    );

    @Override
    public MaterialStatType<?> getType() {
        return TYPE;
    }

    @Override
    public List<Component> getLocalizedInfo(float scale) {
        return List.of(
                SpellbookToolStats.SPELL_SLOTS.formatValue(Math.max(1.0f, Math.round(this.spellSlots * scale))),
                SpellbookToolStats.MAX_MANA.formatValue(this.maxMana * scale),
                SpellbookToolStats.CAST_TIME_REDUCTION.formatValue(this.castTimeReduction * scale)
        );
    }

    @Override
    public List<Component> getLocalizedDescriptions() {
        return List.of(
                SpellbookToolStats.SPELL_SLOTS.getDescription(),
                SpellbookToolStats.MAX_MANA.getDescription(),
                SpellbookToolStats.CAST_TIME_REDUCTION.getDescription()
        );
    }

    @Override
    public void apply(ModifierStatsBuilder builder, float scale) {
        SpellbookToolStats.SPELL_SLOTS.update(builder, Math.max(1.0f, Math.round(this.spellSlots * scale)));
        SpellbookToolStats.MAX_MANA.add(builder, this.maxMana * scale);
        SpellbookToolStats.CAST_TIME_REDUCTION.add(builder, this.castTimeReduction * scale);
    }
}
