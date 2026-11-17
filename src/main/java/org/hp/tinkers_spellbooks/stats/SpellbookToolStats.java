package org.hp.tinkers_spellbooks.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public final class SpellbookToolStats {
    public static final FloatToolStat SPELL_POWER = ToolStats.registerConditional(
            new PercentDisplayToolStat(id("spell_power"), 0xB968FF, 0.0f, -0.9f, 10.0f));
    public static final FloatToolStat MAX_MANA = ToolStats.registerConditional(
            new FloatToolStat(id("max_mana"), 0x4CE2FF, 0.0f, 0.0f, 100000.0f));
    public static final FloatToolStat COOLDOWN_REDUCTION = ToolStats.registerConditional(
            new PercentDisplayToolStat(id("cooldown_reduction"), 0x5CE68A, 0.0f, -0.9f, 0.9f));
    public static final FloatToolStat CAST_TIME_REDUCTION = ToolStats.registerConditional(
            new PercentDisplayToolStat(id("cast_time_reduction"), 0xFFB84D, 0.0f, -0.9f, 0.9f));
    public static final FloatToolStat SPELL_SLOTS = ToolStats.registerConditional(
            new IntegerDisplayToolStat(id("spell_slots"), 0xFFD95E, 1.0f, 1.0f, 15.0f));

    private static boolean initialized = false;

    public static void init() {
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static ToolStatId id(String name) {
        return new ToolStatId(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, name));
    }

    private SpellbookToolStats() {
    }

    public static class PercentDisplayToolStat extends FloatToolStat {
        public PercentDisplayToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue) {
            super(name, color, defaultValue, minValue, maxValue);
        }

        @Override
        public Component formatValue(float value) {
            return IToolStat.formatNumberPercent(this.getTranslationKey(), this.getColor(), value);
        }
    }

    public static class IntegerDisplayToolStat extends FloatToolStat {
        public IntegerDisplayToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue) {
            super(name, color, defaultValue, minValue, maxValue);
        }

        @Override
        public Component formatValue(float value) {
            return IToolStat.formatInteger(this.getTranslationKey(), this.getColor(), Math.round(value));
        }
    }
}
