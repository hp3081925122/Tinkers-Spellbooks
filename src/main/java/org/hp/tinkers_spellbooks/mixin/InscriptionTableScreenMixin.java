package org.hp.tinkers_spellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableScreen;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InscriptionTableScreen.class)
public abstract class InscriptionTableScreenMixin extends AbstractContainerScreen<InscriptionTableMenu> {
    @Shadow(remap = false)
    private boolean isDirty;

    @Shadow(remap = false)
    private int selectedSpellIndex;

    @Shadow(remap = false)
    protected abstract void setSelectedIndex(int index);

    @Shadow(remap = false)
    protected abstract void resetSelectedSpell();

    protected InscriptionTableScreenMixin(InscriptionTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "isSpellBookSlotted", at = @At("HEAD"), cancellable = true, remap = false)
    private void tsb$isModularSpellBookSlotted(CallbackInfoReturnable<Boolean> cir) {
        if (SpellContainerSyncHelper.isModularSpellbook(this.menu.getSpellBookSlot().getItem())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onSpellBookSlotChanged", at = @At("HEAD"), cancellable = true, remap = false)
    private void tsb$onModularSpellBookSlotChanged(CallbackInfo ci) {
        ItemStack spellBookStack = this.menu.getSpellBookSlot().getItem();
        if (SpellContainerSyncHelper.isModularSpellbook(spellBookStack)) {
            this.isDirty = true;
            ISpellContainer container = ISpellContainer.get(spellBookStack);
            if (container == null || container.getMaxSpellCount() <= this.selectedSpellIndex) {
                this.resetSelectedSpell();
            }
            ci.cancel();
        }
    }

    @Inject(method = "onInscription", at = @At("HEAD"), cancellable = true, remap = false)
    private void tsb$onModularInscription(CallbackInfo ci) {
        ItemStack bookStack = this.menu.getSpellBookSlot().getItem();
        if (!SpellContainerSyncHelper.isModularSpellbook(bookStack)) {
            return;
        }
        ItemStack scrollStack = this.menu.getScrollSlot().getItem();
        if (scrollStack.getItem() instanceof Scroll) {
            ISpellContainer bookContainer = ISpellContainer.get(bookStack);
            if (bookContainer != null) {
                int slotCount = Math.min(15, bookContainer.getMaxSpellCount());
                if (slotCount > 0) {
                    if (this.selectedSpellIndex < 0 || this.selectedSpellIndex >= slotCount || bookContainer.getSpellAtIndex(this.selectedSpellIndex) != SpellData.EMPTY) {
                        for (int i = Math.max(0, this.selectedSpellIndex + 1); i < slotCount; i++) {
                            if (bookContainer.getSpellAtIndex(i) == SpellData.EMPTY) {
                                this.setSelectedIndex(i);
                                break;
                            }
                        }
                        if (this.selectedSpellIndex < 0 || bookContainer.getSpellAtIndex(this.selectedSpellIndex) != SpellData.EMPTY) {
                            for (int i = 0; i < slotCount; i++) {
                                if (bookContainer.getSpellAtIndex(i) == SpellData.EMPTY) {
                                    this.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                    this.setSelectedIndex(Mth.clamp(this.selectedSpellIndex, 0, slotCount - 1));
                    if (bookContainer.getSpellAtIndex(this.selectedSpellIndex) == SpellData.EMPTY && this.minecraft != null && this.minecraft.gameMode != null) {
                        this.isDirty = true;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0f));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
                    }
                }
            }
        }
        ci.cancel();
    }
}
