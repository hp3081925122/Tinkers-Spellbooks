package org.hp.tinkers_spellbooks.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.materials.MaterialGeneratorInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ModularGeoTextureCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MaterialVariantId DEFAULT_STAFF_HEAD = new MaterialId(TinkersSpellbooks.MOD_ID, "pyrium");
    private static final MaterialVariantId DEFAULT_STAFF_HANDLE = new MaterialId("tconstruct", "wood");
    private static final MaterialVariantId DEFAULT_STAFF_BINDING = new MaterialId(TinkersSpellbooks.MOD_ID, "arcane_ingot");

    private static final MaterialVariantId DEFAULT_BOOK_COVER = new MaterialId(TinkersSpellbooks.MOD_ID, "dragonskin");
    private static final MaterialVariantId DEFAULT_BOOK_PAGES = new MaterialId(TinkersSpellbooks.MOD_ID, "arcane_cloth");
    private static final MaterialVariantId DEFAULT_BOOK_BINDING = new MaterialId(TinkersSpellbooks.MOD_ID, "mithril");

    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<MaterialVariantId, Optional<ISpriteTransformer>> TRANSFORMER_CACHE = new ConcurrentHashMap<>();

    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) ModularGeoTextureCache::clearAll);
    }

    public static void clearAll(ResourceManager resourceManager) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getTextureManager() != null) {
            for (ResourceLocation loc : TEXTURE_CACHE.values()) {
                mc.getTextureManager().release(loc);
            }
        }
        TEXTURE_CACHE.clear();
        TRANSFORMER_CACHE.clear();
    }

    public static ResourceLocation getTexture(ItemStack stack, String toolName, ResourceLocation fallback) {
        if (stack == null || stack.isEmpty()) {
            return fallback;
        }
        boolean isStaff = "arcane_staff".equals(toolName);
        MaterialVariantId def0 = isStaff ? DEFAULT_STAFF_HEAD : DEFAULT_BOOK_COVER;
        MaterialVariantId def1 = isStaff ? DEFAULT_STAFF_HANDLE : DEFAULT_BOOK_PAGES;
        MaterialVariantId def2 = isStaff ? DEFAULT_STAFF_BINDING : DEFAULT_BOOK_BINDING;

        MaterialVariantId mat0 = def0;
        MaterialVariantId mat1 = def1;
        MaterialVariantId mat2 = def2;
        boolean broken = false;

        try {
            ToolStack tool = ToolStack.from(stack);
            broken = tool.isBroken();
            MaterialNBT materials = tool.getMaterials();
            if (materials.size() > 0) {
                mat0 = nonUnknownOrDefault(materials.get(0), def0);
            }
            if (materials.size() > 1) {
                mat1 = nonUnknownOrDefault(materials.get(1), def1);
            }
            if (materials.size() > 2) {
                mat2 = nonUnknownOrDefault(materials.get(2), def2);
            }
        } catch (Exception ignored) {
            return fallback;
        }

        String rawKey = toolName + "|" + mat0 + "|" + mat1 + "|" + mat2 + "|" + (broken ? "1" : "0");
        ResourceLocation cached = TEXTURE_CACHE.get(rawKey);
        if (cached != null) {
            return cached;
        }

        ResourceLocation generated = buildCompositeTexture(toolName, mat0, mat1, mat2, broken, rawKey);
        if (generated == null) {
            return fallback;
        }
        TEXTURE_CACHE.put(rawKey, generated);
        LOGGER.debug("[TinkersSpellbooks] Synthesized dynamic 64x64 Geo texture {} for key={}", generated, rawKey);
        return generated;
    }

    public static int getGlowTint(ItemStack stack, int materialIndex) {
        if (stack == null || stack.isEmpty()) {
            return 0xFFFFFF;
        }
        try {
            ToolStack tool = ToolStack.from(stack);
            MaterialNBT materials = tool.getMaterials();
            if (materialIndex >= 0 && materialIndex < materials.size()) {
                MaterialVariantId variant = materials.get(materialIndex).getVariant();
                Optional<MaterialRenderInfo> info = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(variant);
                if (info.isPresent()) {
                    int color = info.get().vertexColor() & 0xFFFFFF;
                    if (color != 0) {
                        int r = Mth.clamp(((color >> 16) & 0xFF) + 45, 0, 255);
                        int g = Mth.clamp(((color >> 8) & 0xFF) + 45, 0, 255);
                        int b = Mth.clamp((color & 0xFF) + 45, 0, 255);
                        return (r << 16) | (g << 8) | b;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 0xFFE8A0;
    }

    public static boolean isBroken(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        try {
            return ToolStack.from(stack).isBroken();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static MaterialVariantId nonUnknownOrDefault(MaterialVariant variant, MaterialVariantId fallback) {
        if (variant == null || variant.isUnknown()) {
            return fallback;
        }
        MaterialVariantId id = variant.getVariant();
        if ("tconstruct:unknown".equals(id.toString())) {
            return fallback;
        }
        return id;
    }

    private static ResourceLocation buildCompositeTexture(String toolName, MaterialVariantId mat0, MaterialVariantId mat1, MaterialVariantId mat2, boolean broken, String rawKey) {
        Minecraft mc = Minecraft.getInstance();
        ResourceManager manager = mc.getResourceManager();
        ResourceLocation baseLoc = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "textures/item/geo/" + toolName + ".png");
        Optional<Resource> baseRes = manager.getResource(baseLoc);
        if (baseRes.isEmpty()) {
            return null;
        }

        try (var in = baseRes.get().open(); NativeImage base = NativeImage.read(in)) {
            if (base.getWidth() < 64 || base.getHeight() < 64) {
                return null;
            }
            NativeImage q0 = extractQuadrant(base, 0, 0);
            NativeImage q1 = extractQuadrant(base, 32, 0);
            NativeImage q2 = extractQuadrant(base, 0, 32);

            NativeImage t0 = null;
            NativeImage t1 = null;
            NativeImage t2 = null;
            NativeImage composite = new NativeImage(64, 64, false);
            boolean registered = false;
            try {
                composite.fillRect(0, 0, 64, 64, 0);
                t0 = applyMaterialTransform(manager, q0, mat0);
                t1 = applyMaterialTransform(manager, q1, mat1);
                t2 = applyMaterialTransform(manager, q2, mat2);

                blitQuadrant(composite, t0, 0, 0, broken ? 0.55f : 1.0f);
                blitQuadrant(composite, t1, 32, 0, 1.0f);
                blitQuadrant(composite, t2, 0, 32, broken ? 0.7f : 1.0f);

                String safeHash = Integer.toUnsignedString(rawKey.hashCode(), 16).toLowerCase(Locale.ROOT);
                ResourceLocation dynId = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "dynamic/geo/" + toolName + "/" + safeHash);
                mc.getTextureManager().register(dynId, new DynamicTexture(composite));
                registered = true;
                return dynId;
            } finally {
                q0.close();
                q1.close();
                q2.close();
                if (t0 != null) {
                    t0.close();
                }
                if (t1 != null) {
                    t1.close();
                }
                if (t2 != null) {
                    t2.close();
                }
                if (!registered) {
                    composite.close();
                }
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static NativeImage extractQuadrant(NativeImage src, int ox, int oy) {
        NativeImage out = new NativeImage(32, 32, false);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                out.setPixelRGBA(x, y, src.getPixelRGBA(ox + x, oy + y));
            }
        }
        return out;
    }

    private static void blitQuadrant(NativeImage dst, NativeImage quad, int ox, int oy, float brightnessMul) {
        int w = Math.min(32, quad.getWidth());
        int h = Math.min(32, quad.getHeight());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int abgr = quad.getPixelRGBA(x, y);
                int a = FastColor.ABGR32.alpha(abgr);
                if (a == 0) {
                    continue;
                }
                if (brightnessMul != 1.0f) {
                    int r = Mth.clamp(Math.round(FastColor.ABGR32.red(abgr) * brightnessMul), 0, 255);
                    int g = Mth.clamp(Math.round(FastColor.ABGR32.green(abgr) * brightnessMul), 0, 255);
                    int b = Mth.clamp(Math.round(FastColor.ABGR32.blue(abgr) * brightnessMul), 0, 255);
                    abgr = FastColor.ABGR32.color(a, b, g, r);
                }
                dst.setPixelRGBA(ox + x, oy + y, abgr);
            }
        }
    }

    private static NativeImage applyMaterialTransform(ResourceManager manager, NativeImage quad, MaterialVariantId variant) {
        ISpriteTransformer transformer = getOrLoadTransformer(manager, variant).orElse(null);
        if (transformer != null) {
            try {
                return transformer.transformCopy(quad, false);
            } catch (Exception ignored) {
            }
        }
        int tintRgb = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(variant)
                .map(MaterialRenderInfo::vertexColor)
                .map(c -> c & 0xFFFFFF)
                .orElse(0xB0B0B0);
        return tintFallbackRamp(quad, tintRgb);
    }

    private static NativeImage tintFallbackRamp(NativeImage quad, int rgb) {
        NativeImage out = new NativeImage(quad.getWidth(), quad.getHeight(), false);
        float tr = ((rgb >> 16) & 0xFF) / 255.0f;
        float tg = ((rgb >> 8) & 0xFF) / 255.0f;
        float tb = (rgb & 0xFF) / 255.0f;
        for (int y = 0; y < quad.getHeight(); y++) {
            for (int x = 0; x < quad.getWidth(); x++) {
                int abgr = quad.getPixelRGBA(x, y);
                int a = FastColor.ABGR32.alpha(abgr);
                if (a == 0) {
                    out.setPixelRGBA(x, y, 0);
                    continue;
                }
                float lum = FastColor.ABGR32.red(abgr) / 255.0f;
                int r;
                int g;
                int b;
                if (lum <= 0.72f) {
                    float f = lum / 0.72f;
                    r = Mth.clamp(Math.round(255.0f * tr * (0.22f + 0.78f * f)), 0, 255);
                    g = Mth.clamp(Math.round(255.0f * tg * (0.22f + 0.78f * f)), 0, 255);
                    b = Mth.clamp(Math.round(255.0f * tb * (0.22f + 0.78f * f)), 0, 255);
                } else {
                    float hi = (lum - 0.72f) / 0.28f;
                    r = Mth.clamp(Math.round(255.0f * Mth.lerp(hi * 0.65f, tr, 1.0f)), 0, 255);
                    g = Mth.clamp(Math.round(255.0f * Mth.lerp(hi * 0.65f, tg, 1.0f)), 0, 255);
                    b = Mth.clamp(Math.round(255.0f * Mth.lerp(hi * 0.65f, tb, 1.0f)), 0, 255);
                }
                out.setPixelRGBA(x, y, FastColor.ABGR32.color(a, b, g, r));
            }
        }
        return out;
    }

    private static Optional<ISpriteTransformer> getOrLoadTransformer(ResourceManager manager, MaterialVariantId variant) {
        return TRANSFORMER_CACHE.computeIfAbsent(variant, key -> loadTransformerFromResource(manager, key));
    }

    private static Optional<ISpriteTransformer> loadTransformerFromResource(ResourceManager manager, MaterialVariantId variant) {
        GreyToSpriteTransformer.init();
        ResourceLocation loc = variant.getLocation('_');
        Optional<ISpriteTransformer> exact = readGeneratorJson(manager, ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "tinkering/materials/" + loc.getPath() + ".json"));
        if (exact.isPresent()) {
            return exact;
        }
        MaterialId baseId = variant.getId();
        return readGeneratorJson(manager, ResourceLocation.fromNamespaceAndPath(baseId.getNamespace(), "tinkering/materials/" + baseId.getPath() + ".json"));
    }

    private static Optional<ISpriteTransformer> readGeneratorJson(ResourceManager manager, ResourceLocation jsonPath) {
        Optional<Resource> res = manager.getResource(jsonPath);
        if (res.isEmpty()) {
            return Optional.empty();
        }
        try (var reader = res.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject gen = root.getAsJsonObject("generator");
            if (gen == null) {
                return Optional.empty();
            }
            MaterialGeneratorInfo info = MaterialGeneratorInfo.LOADABLE.deserialize(gen);
            return Optional.ofNullable(info.getTransformer());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private ModularGeoTextureCache() {
    }
}
