package org.hp.tinkers_spellbooks.modifier;

import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;

public class GrimoireExpansionModifier extends MagicAttributeModifier implements ToolStatsModifierHook {
    private final float slotsPerLevel;

    public GrimoireExpansionModifier(float slotsPerLevel, List<Bonus> bonuses) {
        super(bonuses);
        this.slotsPerLevel = slotsPerLevel;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        SpellbookToolStats.SPELL_SLOTS.add(builder, this.slotsPerLevel * modifier.getEffectiveLevel());
    }
}
