package org.hp.tinkers_spellbooks.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ModularGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RenderStateShard.ShaderStateShard EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader);
    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("tinkers_spellbooks_emissive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING = new RenderStateShard.LayeringStateShard("tinkers_spellbooks_polygon_offset", () -> {
        RenderSystem.polygonOffset(-1.0F, -10.0F);
        RenderSystem.enablePolygonOffset();
    }, () -> {
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
    });
    private static final RenderStateShard.WriteMaskStateShard COLOR_ONLY_MASK = new RenderStateShard.WriteMaskStateShard(true, false);
    private static final RenderStateShard.OverlayStateShard OVERLAY_ENABLED = new RenderStateShard.OverlayStateShard(true);

    private static final Function<ResourceLocation, RenderType> GLOW_RENDER_TYPE = Util.memoize(texture -> {
        RenderStateShard.TextureStateShard texState = new RenderStateShard.TextureStateShard(texture, false, false);
        return RenderType.create(
                "tinkers_spellbooks_geo_glow",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(EMISSIVE_SHADER)
                        .setTextureState(texState)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLayeringState(POLYGON_OFFSET_LAYERING)
                        .setWriteMaskState(COLOR_ONLY_MASK)
                        .setOverlayState(OVERLAY_ENABLED)
                        .createCompositeState(false)
        );
    });

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
        LOGGER.debug("[TinkersSpellbooks] Initialized ModularGeoItemRenderer for {} with backface-culled cutout base pass and polygon-offset emissive glow layer (glowMaterialIndex={})", toolName, glowMaterialIndex);
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation fallback = super.getTextureLocation(animatable);
        return ModularGeoTextureCache.getTexture(this.currentItemStack, this.toolName, fallback);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
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
            RenderType emissiveType = GLOW_RENDER_TYPE.apply(ModularGeoItemRenderer.this.glowTexture);
            VertexConsumer glowBuffer = bufferSource.getBuffer(emissiveType);
            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveType, glowBuffer, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        }
    }
}
