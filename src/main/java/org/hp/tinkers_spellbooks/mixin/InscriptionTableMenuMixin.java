package org.hp.tinkers_spellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.compat.ModularSpellbookSlot;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InscriptionTableMenu.class)
public abstract class InscriptionTableMenuMixin extends AbstractContainerMenu {
    @Shadow(remap = false)
    @Final
    private Player player;

    @Shadow(remap = false)
    @Final
    @Mutable
    private Slot spellBookSlot;

    @Shadow(remap = false)
    @Final
    private Slot scrollSlot;

    @Shadow(remap = false)
    @Final
    private Slot resultSlot;

    @Shadow(remap = false)
    private int selectedSpellIndex;

    @Shadow(remap = false)
    private boolean fromCurioSlot;

    @Shadow(remap = false)
    @Final
    protected Container spellbookContainer;

    protected InscriptionTableMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"), remap = false)
    private void tsb$replaceSpellbookSlot(int containerId, Inventory inv, ContainerLevelAccess access, CallbackInfo ci) {
        int slotIndex = this.spellBookSlot.index;
        ModularSpellbookSlot replacement = new ModularSpellbookSlot(
                this.spellbookContainer,
                0,
                this.spellBookSlot.x,
                this.spellBookSlot.y,
                (InscriptionTableMenu) (Object) this,
                this.player,
                () -> this.fromCurioSlot
        );
        replacement.index = slotIndex;
        this.spellBookSlot = replacement;
        this.slots.set(slotIndex, replacement);
    }

    @Inject(method = "doInscription", at = @At("HEAD"), cancellable = true, remap = false)
    private void tsb$doModularInscription(int selectedIndex, CallbackInfo ci) {
        ItemStack spellBookItemStack = this.spellBookSlot.getItem();
        ItemStack scrollItemStack = this.scrollSlot.getItem();
        if (SpellContainerSyncHelper.isModularSpellbook(spellBookItemStack) && scrollItemStack.getItem() instanceof Scroll) {
            ISpellContainer bookContainer = ISpellContainer.get(spellBookItemStack);
            ISpellContainer scrollContainer = ISpellContainer.get(scrollItemStack);
            if (bookContainer != null && scrollContainer != null) {
                SpellData scrollSlotData = scrollContainer.getSpellAtIndex(0);
                ISpellContainerMutable mutableBookContainer = bookContainer.mutableCopy();
                if (mutableBookContainer.addSpellAtIndex(scrollSlotData.getSpell(), scrollSlotData.getLevel(), selectedIndex, false)) {
                    this.scrollSlot.remove(1);
                    ISpellContainer.set(spellBookItemStack, mutableBookContainer.toImmutable());
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private void tsb$setupModularResultSlot(CallbackInfo ci) {
        ItemStack spellBookStack = this.spellBookSlot.getItem();
        if (SpellContainerSyncHelper.isModularSpellbook(spellBookStack)) {
            ItemStack resultStack = ItemStack.EMPTY;
            ISpellContainer spellList = ISpellContainer.get(spellBookStack);
            if (spellList != null && this.selectedSpellIndex >= 0) {
                SpellData spellData = spellList.getSpellAtIndex(this.selectedSpellIndex);
                if (spellData != SpellData.EMPTY && spellData.canRemove()) {
                    resultStack = new ItemStack(ItemRegistry.SCROLL.get());
                    resultStack.setCount(1);
                    ISpellContainer.createScrollContainer(spellData.getSpell(), spellData.getLevel(), resultStack);
                }
            }
            if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
                this.resultSlot.set(resultStack);
            }
            ci.cancel();
        }
    }
}
