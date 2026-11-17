package org.hp.tinkers_spellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.hp.tinkers_spellbooks.modifier.GrimoireExpansionModifier;
import org.hp.tinkers_spellbooks.modifier.MagicAttributeModifier;
import org.hp.tinkers_spellbooks.modifier.MagicAttributeModifier.Bonus;
import org.hp.tinkers_spellbooks.modifier.PyromancyModifier;
import org.hp.tinkers_spellbooks.modifier.SpellcastingCoreModifier;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

import java.util.List;

public final class SpellbookModifiers {
    public static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TinkersSpellbooks.MOD_ID);

    @SuppressWarnings("deprecation")
    public static final StaticModifier<SpellcastingCoreModifier> SPELLCASTING_CORE = MODIFIERS.register("spellcasting_core", SpellcastingCoreModifier::new);

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> MANA_WEAVE = MODIFIERS.register("mana_weave", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.MAX_MANA, 25.0, AttributeModifier.Operation.ADDITION),
            new Bonus(AttributeRegistry.MANA_REGEN, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> TOUGH_HIDE = MODIFIERS.register("tough_hide", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.SPELL_RESIST, 0.05, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ARCANE_RESONANCE = MODIFIERS.register("arcane_resonance", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.SPELL_POWER, 0.05, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> CRYOMANCY = MODIFIERS.register("cryomancy", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.ICE_SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> HEMOMANCY = MODIFIERS.register("hemomancy", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.BLOOD_SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> SANCTITY = MODIFIERS.register("sanctity", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.HOLY_SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> MITHRIL_CHANNELING = MODIFIERS.register("mithril_channeling", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.COOLDOWN_REDUCTION, 0.08, AttributeModifier.Operation.MULTIPLY_BASE),
            new Bonus(AttributeRegistry.CAST_TIME_REDUCTION, 0.08, AttributeModifier.Operation.MULTIPLY_BASE),
            new Bonus(AttributeRegistry.MAX_MANA, 40.0, AttributeModifier.Operation.ADDITION)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<PyromancyModifier> PYROMANCY = MODIFIERS.register("pyromancy", PyromancyModifier::new);

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ENDER_MASTERY = MODIFIERS.register("ender_mastery", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.ENDER_SPELL_POWER, 0.12, AttributeModifier.Operation.MULTIPLY_BASE),
            new Bonus(AttributeRegistry.SPELL_RESIST, 0.06, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<GrimoireExpansionModifier> ELDRITCH_INSIGHT = MODIFIERS.register("eldritch_insight", () -> new GrimoireExpansionModifier(1.0f, List.of(
            new Bonus(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.12, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> FIRE_FOCUS = MODIFIERS.register("fire_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.FIRE_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ICE_FOCUS = MODIFIERS.register("ice_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.ICE_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> LIGHTNING_FOCUS = MODIFIERS.register("lightning_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.LIGHTNING_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> HOLY_FOCUS = MODIFIERS.register("holy_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.HOLY_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ENDER_FOCUS = MODIFIERS.register("ender_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.ENDER_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> BLOOD_FOCUS = MODIFIERS.register("blood_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.BLOOD_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> EVOCATION_FOCUS = MODIFIERS.register("evocation_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.EVOCATION_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> NATURE_FOCUS = MODIFIERS.register("nature_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.NATURE_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ELDRITCH_FOCUS = MODIFIERS.register("eldritch_focus", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> MANA_RESERVOIR = MODIFIERS.register("mana_reservoir", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.MAX_MANA, 50.0, AttributeModifier.Operation.ADDITION)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> QUICK_CHANT = MODIFIERS.register("quick_chant", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.COOLDOWN_REDUCTION, 0.06, AttributeModifier.Operation.MULTIPLY_BASE),
            new Bonus(AttributeRegistry.CAST_TIME_REDUCTION, 0.06, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> WARDED_SCRIPT = MODIFIERS.register("warded_script", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.SPELL_RESIST, 0.06, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<GrimoireExpansionModifier> GRIMOIRE_EXPANSION = MODIFIERS.register("grimoire_expansion", () -> new GrimoireExpansionModifier(2.0f, List.of()));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<MagicAttributeModifier> ARCANE_OVERDRIVE = MODIFIERS.register("arcane_overdrive", () -> new MagicAttributeModifier(List.of(
            new Bonus(AttributeRegistry.SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE),
            new Bonus(AttributeRegistry.MANA_REGEN, 0.12, AttributeModifier.Operation.MULTIPLY_BASE)
    )));

    @SuppressWarnings("deprecation")
    public static final StaticModifier<NoLevelsModifier> ARCHMAGE_INSIGHT = MODIFIERS.register("archmage_insight", NoLevelsModifier::new);

    public static void register(IEventBus bus) {
        MODIFIERS.register(bus);
    }

    private SpellbookModifiers() {
    }
}
