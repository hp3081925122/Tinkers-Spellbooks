package org.hp.tinkers_spellbooks.event;

import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
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
            InteractionHand activeHand = getActiveStaffHand(player);
            if (activeHand != null) {
                ItemStack activeStaff = player.getItemInHand(activeHand);
                ToolDamageUtil.damageAnimated(ToolStack.from(activeStaff), 1, player, activeHand);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide() || event.getSlot().getType() != EquipmentSlot.Type.HAND) {
            return;
        }
        syncStaffOffhandAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        syncStaffOffhandAttributes(entity);
    }

    private static void syncStaffOffhandAttributes(LivingEntity entity) {
        ItemStack offHand = entity.getOffhandItem();
        if (!isUsableStaff(offHand)) {
            return;
        }
        if (isUsableStaff(entity.getMainHandItem())) {
            entity.getAttributes().removeAttributeModifiers(offHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        } else {
            entity.getAttributes().addTransientAttributeModifiers(offHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        }
    }

    @javax.annotation.Nullable
    private static InteractionHand getActiveStaffHand(Player player) {
        if (isUsableStaff(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (isUsableStaff(player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean isUsableStaff(ItemStack stack) {
        return SpellContainerSyncHelper.isModularStaff(stack) && !ToolStack.from(stack).isBroken();
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
        InteractionHand activeHand = getActiveStaffHand(player);
        if (activeHand != null) {
            total += ToolStack.from(player.getItemInHand(activeHand)).getModifierLevel(id);
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
