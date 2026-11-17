package org.hp.tinkers_spellbooks.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.EquipmentChangedPacket;
import io.redspace.ironsspellbooks.player.ServerPlayerEvents;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEvents.class, remap = false)
public abstract class ServerPlayerEventsMixin {
    @Inject(method = "onLivingEquipmentChangeEvent", at = @At("HEAD"), cancellable = true)
    private static void tsb$cancelCastOnModularStaffSwap(LivingEquipmentChangeEvent event, CallbackInfo ci) {
        if (event.getSlot().getType() != EquipmentSlot.Type.HAND) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        boolean fromStaff = SpellContainerSyncHelper.isModularStaff(event.getFrom());
        boolean toStaff = SpellContainerSyncHelper.isModularStaff(event.getTo());
        if (!fromStaff && !toStaff) {
            return;
        }
        if (fromStaff && toStaff && Utils.isSameItemSameComponentsIgnoreDurability(event.getFrom(), event.getTo())) {
            return;
        }
        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        if (magicData.isCasting()) {
            Utils.serverSideCancelCast(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, new EquipmentChangedPacket());
            ci.cancel();
        }
    }
}
