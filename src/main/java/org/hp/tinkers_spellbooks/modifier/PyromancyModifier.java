package org.hp.tinkers_spellbooks.modifier;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class PyromancyModifier extends MagicAttributeModifier implements MeleeHitModifierHook {
    public PyromancyModifier() {
        super(List.of(
                new Bonus(AttributeRegistry.FIRE_SPELL_POWER, 0.12, AttributeModifier.Operation.MULTIPLY_BASE)
        ));
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target != null && damageDealt > 0.0f && !target.fireImmune()) {
            target.setSecondsOnFire(Math.max(3, Math.round(4.0f * modifier.getEffectiveLevel())));
        }
    }
}
