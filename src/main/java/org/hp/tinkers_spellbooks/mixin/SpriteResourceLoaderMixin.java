package org.hp.tinkers_spellbooks.mixin;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.hp.tinkers_spellbooks.client.GeneratedSpellbookSpriteSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteResourceLoader.class)
public abstract class SpriteResourceLoaderMixin {
    private static final ResourceLocation BLOCKS_ATLAS = ResourceLocation.fromNamespaceAndPath("minecraft", "blocks");

    @Shadow
    @Final
    private List<SpriteSource> sources;

    @Inject(method = "load", at = @At("RETURN"))
    private static void tsb$appendMaterialSpriteSource(ResourceManager manager, ResourceLocation atlas, CallbackInfoReturnable<SpriteResourceLoader> cir) {
        if (BLOCKS_ATLAS.equals(atlas)) {
            SpriteResourceLoaderMixin loader = (SpriteResourceLoaderMixin) (Object) cir.getReturnValue();
            loader.sources.add(new GeneratedSpellbookSpriteSource());
        }
    }
}
