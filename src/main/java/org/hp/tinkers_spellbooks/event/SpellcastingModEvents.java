package org.hp.tinkers_spellbooks.event;

import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.hp.tinkers_spellbooks.config.SpellbooksConfig;
import org.hp.tinkers_spellbooks.registry.SpellbookModifiers;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public final class SpellcastingModEvents {
    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        int resonanceLevels = countActiveModifierLevels(player, SpellbookModifiers.ARCANE_RESONANCE.getId());
        if (resonanceLevels > 0 && event.getManaCost() > 0) {
            float reduction = Math.min(0.50f, resonanceLevels * 0.05f);
            int reduced = Math.max(0, Math.round(event.getManaCost() * (1.0f - reduction)));
            event.setManaCost(reduced);
        }
        if (!player.level().isClientSide() && SpellbooksConfig.CONSUME_STAFF_DURABILITY_ON_CAST.get()) {
            ItemStack mainHand = player.getMainHandItem();
            if (SpellContainerSyncHelper.isModularStaff(mainHand)) {
                ToolStack tool = ToolStack.from(mainHand);
                if (!tool.isBroken()) {
                    ToolDamageUtil.damageAnimated(tool, 1, player, InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onModifySpellLevel(ModifySpellLevelEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        int archmageLevels = countActiveModifierLevels(player, SpellbookModifiers.ARCHMAGE_INSIGHT.getId());
        if (archmageLevels > 0) {
            event.addLevels(archmageLevels);
        }
    }

    private static int countActiveModifierLevels(Player player, ModifierId id) {
        int total = 0;
        ItemStack mainHand = player.getMainHandItem();
        if (SpellContainerSyncHelper.isModularStaff(mainHand)) {
            ToolStack staff = ToolStack.from(mainHand);
            if (!staff.isBroken()) {
                total += staff.getModifierLevel(id);
            }
        }
        ItemStack spellbook = Utils.getPlayerSpellbookStack(player);
        if (spellbook != null && SpellContainerSyncHelper.isModularSpellbook(spellbook)) {
            ToolStack book = ToolStack.from(spellbook);
            if (!book.isBroken()) {
                total += book.getModifierLevel(id);
            }
        }
        return total;
    }

    private SpellcastingModEvents() {
    }
}
