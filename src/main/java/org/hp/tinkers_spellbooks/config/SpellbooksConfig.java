package org.hp.tinkers_spellbooks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SpellbooksConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.BooleanValue AUTO_GENERATE_FALLBACK_STATS;
    public static final ForgeConfigSpec.IntValue MAX_SPELLBOOK_SLOTS;
    public static final ForgeConfigSpec.IntValue MAX_STAFF_SLOTS;
    public static final ForgeConfigSpec.BooleanValue CONSUME_STAFF_DURABILITY_ON_CAST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");
        AUTO_GENERATE_FALLBACK_STATS = builder
                .comment("Automatically derive magical part stats for Tinkers materials that do not explicitly define them. / 自动为未显式定义魔法部件属性的匠魂材料生成兼容属性。")
                .define("autoGenerateFallbackStats", true);
        MAX_SPELLBOOK_SLOTS = builder
                .comment("Maximum spell slots allowed on a Modular Spellbook (Inscription Table UI supports up to 15). / 模块化魔法书允许的最大法术槽位数（抄写台界面上限为 15）。")
                .defineInRange("maxSpellbookSlots", 15, 1, 15);
        MAX_STAFF_SLOTS = builder
                .comment("Maximum imbued spell slots allowed on an Arcane Staff. / 模块化奥术法杖允许的最大法术灌注槽位数。")
                .defineInRange("maxStaffSlots", 6, 1, 15);
        CONSUME_STAFF_DURABILITY_ON_CAST = builder
                .comment("Whether casting a spell with the Arcane Staff consumes 1 durability. / 使用模块化奥术法杖施法时是否消耗 1 点耐久。")
                .define("consumeStaffDurabilityOnCast", true);
        builder.pop();
        COMMON_SPEC = builder.build();
    }

    private SpellbooksConfig() {
    }
}
