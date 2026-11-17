package org.hp.tinkers_spellbooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import javax.annotation.Nullable;

public class ModularGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    private final String toolName;
    private final int glowMaterialIndex;
    private final ResourceLocation glowTexture;

    public ModularGeoItemRenderer(String toolName, int glowMaterialIndex) {
        super(new DefaultedItemGeoModel<T>(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, toolName))
                .withAltTexture(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "geo/" + toolName)));
        this.toolName = toolName;
        this.glowMaterialIndex = glowMaterialIndex;
        this.glowTexture = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "textures/item/geo/" + toolName + "_glowmask.png");
        this.useAlternateGuiLighting();
        this.addRenderLayer(new GlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation fallback = super.getTextureLocation(animatable);
        return ModularGeoTextureCache.getTexture(this.currentItemStack, this.toolName, fallback);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    private final class GlowLayer extends GeoRenderLayer<T> {
        private GlowLayer(ModularGeoItemRenderer<T> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            ItemStack stack = ModularGeoItemRenderer.this.getCurrentItemStack();
            if (stack == null || stack.isEmpty() || ModularGeoTextureCache.isBroken(stack)) {
                return;
            }
            int rgb = ModularGeoTextureCache.getGlowTint(stack, ModularGeoItemRenderer.this.glowMaterialIndex);
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;
            RenderType eyes = RenderType.eyes(ModularGeoItemRenderer.this.glowTexture);
            VertexConsumer glowBuffer = bufferSource.getBuffer(eyes);
            poseStack.pushPose();
            ModularGeoItemRenderer.this.actuallyRender(poseStack, animatable, bakedModel, eyes, bufferSource, glowBuffer, true, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
            poseStack.popPose();
        }
    }
}
