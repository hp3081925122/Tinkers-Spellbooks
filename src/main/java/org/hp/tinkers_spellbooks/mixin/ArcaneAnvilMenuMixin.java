package org.hp.tinkers_spellbooks.mixin;

import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArcaneAnvilMenu.class)
public abstract class ArcaneAnvilMenuMixin extends ItemCombinerMenu {
    protected ArcaneAnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void tsb$enhanceModularAnvilResult(CallbackInfo ci) {
        ItemStack base = this.inputSlots.getItem(0);
        ItemStack modifier = this.inputSlots.getItem(1);
        if (base.isEmpty() || modifier.isEmpty()) {
            return;
        }
        if (SpellContainerSyncHelper.isModularMagicTool(base) && modifier.is(ItemRegistry.SHRIVING_STONE.get())) {
            ItemStack shrived = SpellContainerSyncHelper.handleModularShriving(base);
            this.resultSlots.setItem(0, shrived);
            return;
        }
        if (!this.resultSlots.getItem(0).isEmpty()) {
            return;
        }
        if (modifier.getItem() instanceof SpellSlotUpgradeItem upgradeItem) {
            ItemStack upgraded = SpellContainerSyncHelper.tryUpgradeSpellSlotsInAnvil(base, upgradeItem);
            if (!upgraded.isEmpty()) {
                this.resultSlots.setItem(0, upgraded);
                return;
            }
        }
        ItemStack inscribed = SpellContainerSyncHelper.tryInscribeScrollOnSpellbookInAnvil(base, modifier);
        if (!inscribed.isEmpty()) {
            this.resultSlots.setItem(0, inscribed);
        }
    }
}
