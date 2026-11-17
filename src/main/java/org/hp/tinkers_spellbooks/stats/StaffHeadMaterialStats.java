package org.hp.tinkers_spellbooks.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record StaffHeadMaterialStats(int durability, float meleeAttack, float spellPower, float spellSlots) implements IRepairableMaterialStats.ScaledTooltip {
    public static final MaterialStatsId ID = new MaterialStatsId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "staff_head"));
    public static final MaterialStatType<StaffHeadMaterialStats> TYPE = new MaterialStatType<>(
            ID,
            new StaffHeadMaterialStats(120, 1.5f, 0.05f, 1.0f),
            RecordLoadable.create(
                    IRepairableMaterialStats.DURABILITY_FIELD,
                    FloatLoadable.FROM_ZERO.defaultField("melee_attack", 1.5f, true, StaffHeadMaterialStats::meleeAttack),
                    FloatLoadable.ANY.defaultField("spell_power", 0.05f, true, StaffHeadMaterialStats::spellPower),
                    FloatLoadable.FROM_ZERO.defaultField("spell_slots", 1.0f, true, StaffHeadMaterialStats::spellSlots),
                    StaffHeadMaterialStats::new
            )
    );

    @Override
    public MaterialStatType<?> getType() {
        return TYPE;
    }

    @Override
    public List<Component> getLocalizedInfo(float scale) {
        return List.of(
                ToolStats.DURABILITY.formatValue(this.durability * scale),
                ToolStats.ATTACK_DAMAGE.formatValue(this.meleeAttack * scale),
                SpellbookToolStats.SPELL_POWER.formatValue(this.spellPower * scale),
                SpellbookToolStats.SPELL_SLOTS.formatValue(Math.max(1.0f, Math.round(this.spellSlots * scale)))
        );
    }

    @Override
    public List<Component> getLocalizedDescriptions() {
        return List.of(
                ToolStats.DURABILITY.getDescription(),
                ToolStats.ATTACK_DAMAGE.getDescription(),
                SpellbookToolStats.SPELL_POWER.getDescription(),
                SpellbookToolStats.SPELL_SLOTS.getDescription()
        );
    }

    @Override
    public void apply(ModifierStatsBuilder builder, float scale) {
        ToolStats.DURABILITY.update(builder, this.durability * scale);
        ToolStats.ATTACK_DAMAGE.update(builder, this.meleeAttack * scale);
        SpellbookToolStats.SPELL_POWER.add(builder, this.spellPower * scale);
        SpellbookToolStats.SPELL_SLOTS.update(builder, Math.max(1.0f, Math.round(this.spellSlots * scale)));
    }
}
