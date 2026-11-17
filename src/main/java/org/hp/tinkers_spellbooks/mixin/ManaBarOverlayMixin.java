package org.hp.tinkers_spellbooks.mixin;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.world.entity.player.Player;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ManaBarOverlay.class, remap = false)
public abstract class ManaBarOverlayMixin {
    @Inject(method = "shouldShowManaBar", at = @At("HEAD"), cancellable = true)
    private static void tsb$showManaBarForModularStaff(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null || player.isSpectator()) {
            return;
        }
        if (ClientConfigs.MANA_BAR_DISPLAY.get() != ManaBarOverlay.Display.Never && player.isHolding(SpellContainerSyncHelper::isModularStaff)) {
            cir.setReturnValue(true);
        }
    }
}
