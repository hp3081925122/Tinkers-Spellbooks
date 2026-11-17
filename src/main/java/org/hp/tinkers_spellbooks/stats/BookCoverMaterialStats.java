package org.hp.tinkers_spellbooks.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

public record BookCoverMaterialStats(int durability, float maxMana, float spellPower, float cooldownReduction) implements IMaterialStats.ScaledTooltip {
    public static final MaterialStatsId ID = new MaterialStatsId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "book_cover"));
    public static final MaterialStatType<BookCoverMaterialStats> TYPE = new MaterialStatType<>(
            ID,
            new BookCoverMaterialStats(140, 40.0f, 0.04f, 0.02f),
            RecordLoadable.create(
                    IntLoadable.FROM_ZERO.defaultField("durability", 140, true, BookCoverMaterialStats::durability),
                    FloatLoadable.FROM_ZERO.defaultField("max_mana", 40.0f, true, BookCoverMaterialStats::maxMana),
                    FloatLoadable.ANY.defaultField("spell_power", 0.04f, true, BookCoverMaterialStats::spellPower),
                    FloatLoadable.ANY.defaultField("cooldown_reduction", 0.02f, true, BookCoverMaterialStats::cooldownReduction),
                    BookCoverMaterialStats::new
            )
    );

    @Override
    public MaterialStatType<?> getType() {
        return TYPE;
    }

    @Override
    public List<Component> getLocalizedInfo(float scale) {
        return List.of(
                SpellbookToolStats.MAX_MANA.formatValue(this.maxMana * scale),
                SpellbookToolStats.SPELL_POWER.formatValue(this.spellPower * scale),
                SpellbookToolStats.COOLDOWN_REDUCTION.formatValue(this.cooldownReduction * scale)
        );
    }

    @Override
    public List<Component> getLocalizedDescriptions() {
        return List.of(
                SpellbookToolStats.MAX_MANA.getDescription(),
                SpellbookToolStats.SPELL_POWER.getDescription(),
                SpellbookToolStats.COOLDOWN_REDUCTION.getDescription()
        );
    }

    @Override
    public void apply(ModifierStatsBuilder builder, float scale) {
        SpellbookToolStats.MAX_MANA.add(builder, this.maxMana * scale);
        SpellbookToolStats.SPELL_POWER.add(builder, this.spellPower * scale);
        SpellbookToolStats.COOLDOWN_REDUCTION.add(builder, this.cooldownReduction * scale);
    }
}
