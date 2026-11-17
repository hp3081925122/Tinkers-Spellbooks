package org.hp.tinkers_spellbooks.compat;

import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.hp.tinkers_spellbooks.config.SpellbooksConfig;
import org.hp.tinkers_spellbooks.item.ModifiableSpellbookItem;
import org.hp.tinkers_spellbooks.item.ModifiableStaffItem;
import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import java.util.Optional;

public final class SpellContainerSyncHelper {
    public static final ResourceLocation KEY_ANVIL_BONUS_SLOTS = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "anvil_bonus_slots");

    public static boolean isModularSpellbook(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ModifiableSpellbookItem;
    }

    public static boolean isModularStaff(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ModifiableStaffItem;
    }

    public static boolean isModularMagicTool(ItemStack stack) {
        return isModularSpellbook(stack) || isModularStaff(stack);
    }

    public static int computeTargetMaxSlots(IToolStackView tool) {
        boolean isSpellbook = tool.getItem() instanceof ModifiableSpellbookItem;
        int cap = isSpellbook ? SpellbooksConfig.MAX_SPELLBOOK_SLOTS.get() : SpellbooksConfig.MAX_STAFF_SLOTS.get();
        int statSlots = Math.round(tool.getStats().get(SpellbookToolStats.SPELL_SLOTS));
        int anvilBonus = tool.getPersistentData().getInt(KEY_ANVIL_BONUS_SLOTS);
        return Mth.clamp(statSlots + anvilBonus, 1, cap);
    }

    public static int findHighestOccupiedSlot(ISpellContainer container) {
        if (container == null || container.isEmpty()) {
            return -1;
        }
        int highest = -1;
        for (SpellSlot slot : container.getActiveSpells()) {
            if (slot != null && slot.index() > highest) {
                highest = slot.index();
            }
        }
        return highest;
    }

    public static ISpellContainer readFromRestrictedTag(RestrictedCompoundTag tag) {
        if (!tag.contains(ISpellContainer.NBT, Tag.TAG_COMPOUND)) {
            return null;
        }
        Tag raw = tag.get(ISpellContainer.NBT);
        if (raw == null) {
            return null;
        }
        return SpellContainer.CODEC.parse(NbtOps.INSTANCE, raw).result().orElse(null);
    }

    public static void writeToRestrictedTag(RestrictedCompoundTag tag, ISpellContainer container) {
        Optional<Tag> encoded = SpellContainer.CODEC.encodeStart(NbtOps.INSTANCE, container).result();
        encoded.ifPresent(value -> tag.put(ISpellContainer.NBT, value));
    }

    public static void syncRawSpellContainer(IToolStackView tool, RestrictedCompoundTag tag) {
        if (!(tool.getItem() instanceof ModifiableSpellbookItem) && !(tool.getItem() instanceof ModifiableStaffItem)) {
            return;
        }
        boolean mustEquip = tool.getItem() instanceof ModifiableSpellbookItem;
        int computedMax = computeTargetMaxSlots(tool);
        ISpellContainer existing = readFromRestrictedTag(tag);
        if (existing == null) {
            writeToRestrictedTag(tag, ISpellContainer.create(computedMax, true, mustEquip));
            return;
        }
        int safeMax = Math.max(computedMax, findHighestOccupiedSlot(existing) + 1);
        if (existing.getMaxSpellCount() == safeMax && existing.mustEquip() == mustEquip && existing.isSpellWheel()) {
            return;
        }
        SpellSlot[] preserved = new SpellSlot[safeMax];
        for (SpellSlot active : existing.getActiveSpells()) {
            if (active != null && active.index() >= 0 && active.index() < safeMax) {
                preserved[active.index()] = active;
            }
        }
        SpellContainer updated = new SpellContainer(safeMax, true, mustEquip, existing.isImproved(), preserved);
        writeToRestrictedTag(tag, updated);
    }

    public static ItemStack tryUpgradeSpellSlotsInAnvil(ItemStack baseStack, SpellSlotUpgradeItem upgradeItem) {
        if (!isModularSpellbook(baseStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = baseStack.copy();
        copy.setCount(1);
        ToolStack tool = ToolStack.from(copy);
        int currentMax = computeTargetMaxSlots(tool);
        int maxAllowed = Math.min(upgradeItem.maxSlots(), SpellbooksConfig.MAX_SPELLBOOK_SLOTS.get());
        if (currentMax >= maxAllowed) {
            return ItemStack.EMPTY;
        }
        ModDataNBT persistent = tool.getPersistentData();
        persistent.putInt(KEY_ANVIL_BONUS_SLOTS, persistent.getInt(KEY_ANVIL_BONUS_SLOTS) + 1);
        tool.rebuildStats();
        return copy;
    }

    public static ItemStack tryInscribeScrollOnSpellbookInAnvil(ItemStack baseStack, ItemStack modifierStack) {
        if (!isModularSpellbook(baseStack) || !(modifierStack.getItem() instanceof Scroll)) {
            return ItemStack.EMPTY;
        }
        if (!ISpellContainer.isSpellContainer(modifierStack)) {
            return ItemStack.EMPTY;
        }
        SpellData scrollSpell = ISpellContainer.get(modifierStack).getSpellAtIndex(0);
        if (scrollSpell == SpellData.EMPTY) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = baseStack.copy();
        copy.setCount(1);
        ToolStack tool = ToolStack.from(copy);
        int maxSlots = computeTargetMaxSlots(tool);
        ISpellContainer current = ISpellContainer.isSpellContainer(copy)
                ? ISpellContainer.get(copy)
                : ISpellContainer.create(maxSlots, true, true);
        int nextSlot = current.getNextAvailableIndex();
        if (nextSlot < 0 || nextSlot >= current.getMaxSpellCount()) {
            return ItemStack.EMPTY;
        }
        ISpellContainerMutable mutable = current.mutableCopy();
        if (!mutable.addSpellAtIndex(scrollSpell.getSpell(), scrollSpell.getLevel(), nextSlot, false)) {
            return ItemStack.EMPTY;
        }
        ISpellContainer.set(copy, mutable.toImmutable());
        return copy;
    }

    public static ItemStack handleModularShriving(ItemStack baseStack) {
        if (!isModularMagicTool(baseStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = baseStack.copy();
        copy.setCount(1);
        boolean changed = false;
        if (ISpellContainer.isSpellContainer(copy)) {
            ISpellContainer current = ISpellContainer.get(copy);
            if (!current.isEmpty()) {
                ToolStack tool = ToolStack.from(copy);
                boolean mustEquip = isModularSpellbook(copy);
                ISpellContainer.set(copy, ISpellContainer.create(computeTargetMaxSlots(tool), true, mustEquip));
                changed = true;
            }
        }
        if (UpgradeData.hasUpgradeData(copy)) {
            UpgradeData.removeUpgradeData(copy);
            changed = true;
        }
        if (!changed) {
            return ItemStack.EMPTY;
        }
        CompoundTag nbt = copy.getTag();
        if (nbt != null) {
            ToolStack.from(copy).rebuildStats();
        }
        return copy;
    }

    private SpellContainerSyncHelper() {
    }
}
