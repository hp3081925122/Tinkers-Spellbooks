package org.hp.tinkers_spellbooks.compat;

import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public class ModularSpellbookSlot extends Slot {
    private final InscriptionTableMenu menu;
    private final Player player;
    private final BooleanSupplier fromCurioSupplier;

    public ModularSpellbookSlot(Container container, int slot, int x, int y, InscriptionTableMenu menu, Player player, BooleanSupplier fromCurioSupplier) {
        super(container, slot, x, y);
        this.menu = menu;
        this.player = player;
        this.fromCurioSupplier = fromCurioSupplier;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof ISpellbook;
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);
        if (this.fromCurioSupplier.getAsBoolean() && this.player != null) {
            Utils.setPlayerSpellbookStack(this.player, stack);
        }
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.menu.setSelectedSpell(-1);
        super.onTake(player, stack);
    }
}
