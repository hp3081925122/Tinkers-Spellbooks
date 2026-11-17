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

public record ArcaneBindingMaterialStats(float maxMana, float spellPower, float cooldownReduction) implements IMaterialStats.ScaledTooltip {
    public static final MaterialStatsId ID = new MaterialStatsId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "arcane_binding"));
    public static final MaterialStatType<ArcaneBindingMaterialStats> TYPE = new MaterialStatType<>(
            ID,
            new ArcaneBindingMaterialStats(20.0f, 0.02f, 0.02f),
            RecordLoadable.create(
                    FloatLoadable.FROM_ZERO.defaultField("max_mana", 20.0f, true, ArcaneBindingMaterialStats::maxMana),
                    FloatLoadable.ANY.defaultField("spell_power", 0.02f, true, ArcaneBindingMaterialStats::spellPower),
                    FloatLoadable.ANY.defaultField("cooldown_reduction", 0.02f, true, ArcaneBindingMaterialStats::cooldownReduction),
                    ArcaneBindingMaterialStats::new
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
