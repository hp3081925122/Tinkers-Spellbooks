package org.hp.tinkers_spellbooks.modifier;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.hp.tinkers_spellbooks.item.ModifiableStaffItem;
import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;
import slimeknights.mantle.data.registry.IdAwareComponentRegistry;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SpellcastingCoreModifier extends NoLevelsModifier implements RawDataModifierHook, ValidateModifierHook, AttributesModifierHook, ICurioMagicModifier {
    private static final UUID STAFF_SPELL_POWER_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a10001");
    private static final UUID STAFF_MAX_MANA_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a10002");
    private static final UUID STAFF_COOLDOWN_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a10003");
    private static final UUID STAFF_CAST_TIME_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a10004");
    private static final UUID STAFF_OFFHAND_SPELL_POWER_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a20001");
    private static final UUID STAFF_OFFHAND_MAX_MANA_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a20002");
    private static final UUID STAFF_OFFHAND_COOLDOWN_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a20003");
    private static final UUID STAFF_OFFHAND_CAST_TIME_UUID = UUID.fromString("92f3b5c1-44d1-4a7a-9b11-10d4f8a20004");

    @Override
    public boolean shouldDisplay(boolean advanced) {
        return false;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.RAW_DATA, ModifierHooks.VALIDATE, ModifierHooks.ATTRIBUTES);
    }

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag tag) {
        SpellContainerSyncHelper.syncRawSpellContainer(tool, tag);
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag tag) {
    }

    @Override
    @Nullable
    public Component validate(IToolStackView tool, ModifierEntry modifier) {
        if (!(tool instanceof ToolStack toolStack)) {
            return null;
        }
        ISpellContainer container = SpellContainerSyncHelper.readFromRestrictedTag(toolStack.getRestrictedNBT());
        if (container == null || container.isEmpty()) {
            return null;
        }
        int requiredSlots = SpellContainerSyncHelper.findHighestOccupiedSlot(container) + 1;
        int targetMax = SpellContainerSyncHelper.computeTargetMaxSlots(tool);
        if (requiredSlots > targetMax) {
            return Component.translatable("tooltip.tinkers_spellbooks.error.spells_exceed_capacity", requiredSlots, targetMax);
        }
        return null;
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (tool.isBroken() || slot.getType() != EquipmentSlot.Type.HAND || !(tool.getItem() instanceof ModifiableStaffItem)) {
            return;
        }
        if (slot == EquipmentSlot.MAINHAND) {
            applyToolMagicStats(tool.getStats(), "tinkers_spellbooks.staff", STAFF_SPELL_POWER_UUID, STAFF_MAX_MANA_UUID, STAFF_COOLDOWN_UUID, STAFF_CAST_TIME_UUID, consumer);
        } else {
            applyToolMagicStats(tool.getStats(), "tinkers_spellbooks.staff_offhand", STAFF_OFFHAND_SPELL_POWER_UUID, STAFF_OFFHAND_MAX_MANA_UUID, STAFF_OFFHAND_COOLDOWN_UUID, STAFF_OFFHAND_CAST_TIME_UUID, consumer);
        }
    }

    @Override
    public void addCurioAttributes(IToolStackView tool, ModifierEntry entry, UUID slotUuid, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (tool.isBroken()) {
            return;
        }
        UUID powerId = deriveUuid(slotUuid, "spellbook_power");
        UUID manaId = deriveUuid(slotUuid, "spellbook_mana");
        UUID cdrId = deriveUuid(slotUuid, "spellbook_cdr");
        UUID castId = deriveUuid(slotUuid, "spellbook_cast");
        applyToolMagicStats(tool.getStats(), "tinkers_spellbooks.spellbook", powerId, manaId, cdrId, castId, consumer);
    }

    private static void applyToolMagicStats(
            StatsNBT stats,
            String prefix,
            UUID powerUuid,
            UUID manaUuid,
            UUID cdrUuid,
            UUID castUuid,
            BiConsumer<Attribute, AttributeModifier> consumer
    ) {
        float spellPower = stats.get(SpellbookToolStats.SPELL_POWER);
        if (Math.abs(spellPower) > 1.0E-4f) {
            consumer.accept(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(powerUuid, prefix + ".spell_power", spellPower, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        float maxMana = stats.get(SpellbookToolStats.MAX_MANA);
        if (Math.abs(maxMana) > 1.0E-4f) {
            consumer.accept(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(manaUuid, prefix + ".max_mana", maxMana, AttributeModifier.Operation.ADDITION));
        }
        float cdr = stats.get(SpellbookToolStats.COOLDOWN_REDUCTION);
        if (Math.abs(cdr) > 1.0E-4f) {
            consumer.accept(AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(cdrUuid, prefix + ".cooldown_reduction", cdr, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        float castReduction = stats.get(SpellbookToolStats.CAST_TIME_REDUCTION);
        if (Math.abs(castReduction) > 1.0E-4f) {
            consumer.accept(AttributeRegistry.CAST_TIME_REDUCTION.get(), new AttributeModifier(castUuid, prefix + ".cast_time_reduction", castReduction, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    public static UUID deriveUuid(UUID base, String salt) {
        return UUID.nameUUIDFromBytes((base.toString() + ":" + salt).getBytes(StandardCharsets.UTF_8));
    }
}
