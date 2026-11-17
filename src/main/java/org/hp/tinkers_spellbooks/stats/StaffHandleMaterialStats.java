package org.hp.tinkers_spellbooks.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record StaffHandleMaterialStats(float durability, float meleeSpeed, float cooldownReduction, float castTimeReduction) implements IMaterialStats.ScaledTooltip {
    public static final MaterialStatsId ID = new MaterialStatsId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "staff_handle"));
    private static final String DURABILITY_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("durability"));
    private static final String ATTACK_SPEED_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("attack_speed"));
    public static final MaterialStatType<StaffHandleMaterialStats> TYPE = new MaterialStatType<>(
            ID,
            new StaffHandleMaterialStats(0.0f, 0.0f, 0.03f, 0.03f),
            RecordLoadable.create(
                    FloatLoadable.ANY.defaultField("durability", 0.0f, true, StaffHandleMaterialStats::durability),
                    FloatLoadable.ANY.defaultField("melee_speed", 0.0f, true, StaffHandleMaterialStats::meleeSpeed),
                    FloatLoadable.ANY.defaultField("cooldown_reduction", 0.03f, true, StaffHandleMaterialStats::cooldownReduction),
                    FloatLoadable.ANY.defaultField("cast_time_reduction", 0.03f, true, StaffHandleMaterialStats::castTimeReduction),
                    StaffHandleMaterialStats::new
            )
    );

    @Override
    public MaterialStatType<?> getType() {
        return TYPE;
    }

    @Override
    public List<Component> getLocalizedInfo(float scale) {
        return List.of(
                IToolStat.formatColoredPercentBoost(DURABILITY_PREFIX, this.durability * scale),
                IToolStat.formatColoredPercentBoost(ATTACK_SPEED_PREFIX, this.meleeSpeed * scale),
                SpellbookToolStats.COOLDOWN_REDUCTION.formatValue(this.cooldownReduction * scale),
                SpellbookToolStats.CAST_TIME_REDUCTION.formatValue(this.castTimeReduction * scale)
        );
    }

    @Override
    public List<Component> getLocalizedDescriptions() {
        return List.of(
                IMaterialStats.makeTooltip(TConstruct.getResource("handle.durability.description")),
                IMaterialStats.makeTooltip(TConstruct.getResource("handle.attack_speed.description")),
                SpellbookToolStats.COOLDOWN_REDUCTION.getDescription(),
                SpellbookToolStats.CAST_TIME_REDUCTION.getDescription()
        );
    }

    @Override
    public void apply(ModifierStatsBuilder builder, float scale) {
        ToolStats.DURABILITY.percent(builder, this.durability * scale);
        ToolStats.ATTACK_SPEED.percent(builder, this.meleeSpeed * scale);
        SpellbookToolStats.COOLDOWN_REDUCTION.add(builder, this.cooldownReduction * scale);
        SpellbookToolStats.CAST_TIME_REDUCTION.add(builder, this.castTimeReduction * scale);
    }
}
