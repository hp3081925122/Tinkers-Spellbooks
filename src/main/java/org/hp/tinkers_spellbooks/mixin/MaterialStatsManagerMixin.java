package org.hp.tinkers_spellbooks.mixin;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.hp.tinkers_spellbooks.stats.MaterialFallbackGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;

import java.util.Map;

@Mixin(value = MaterialStatsManager.class, remap = false)
public abstract class MaterialStatsManagerMixin {
    @Shadow
    private Map<MaterialId, Map<MaterialStatsId, IMaterialStats>> materialToStatsPerType;

    @Inject(method = "finishLoad", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"))
    private void tsb$populateFallbackStats(Map<ResourceLocation, Map<ResourceLocation, JsonObject>> map, ResourceManager manager, CallbackInfo ci) {
        this.materialToStatsPerType = MaterialFallbackGenerator.populateMissingStats(this.materialToStatsPerType);
    }
}
