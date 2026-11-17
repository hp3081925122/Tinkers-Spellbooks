package org.hp.tinkers_spellbooks.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.hp.tinkers_spellbooks.stats.ArcaneBindingMaterialStats;
import org.hp.tinkers_spellbooks.stats.BookCoverMaterialStats;
import org.hp.tinkers_spellbooks.stats.MaterialFallbackGenerator;
import org.hp.tinkers_spellbooks.stats.SpellPagesMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHandleMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHeadMaterialStats;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.client.materials.MaterialGeneratorInfo;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneratedSpellbookSpriteSource implements SpriteSource {
    private static final String MATERIAL_FOLDER = "tinkering/materials";
    private static final List<PartSpriteSpec> SPECS = List.of(
            new PartSpriteSpec(res("item/tool/parts/staff_head"), StaffHeadMaterialStats.ID, false),
            new PartSpriteSpec(res("item/tool/parts/staff_handle"), StaffHandleMaterialStats.ID, false),
            new PartSpriteSpec(res("item/tool/parts/book_cover"), BookCoverMaterialStats.ID, false),
            new PartSpriteSpec(res("item/tool/parts/spell_pages"), SpellPagesMaterialStats.ID, false),
            new PartSpriteSpec(res("item/tool/parts/arcane_binding"), ArcaneBindingMaterialStats.ID, false)
    );

    private static ResourceLocation res(String path) {
        return ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, path);
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        GreyToSpriteTransformer.init();
        MaterialPartTextureGenerator.runCallbacks(null, manager);
        Map<ResourceLocation, Resource> resources = manager.listResources(MATERIAL_FOLDER, id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            this.generateForMaterial(manager, output, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public net.minecraft.client.renderer.texture.atlas.SpriteSourceType type() {
        return SpriteSources.SINGLE_FILE;
    }

    private void generateForMaterial(ResourceManager manager, Output output, ResourceLocation file, Resource resource) {
        try {
            JsonObject json;
            try (var reader = resource.openAsReader()) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonObject generatorJson = json.getAsJsonObject("generator");
            if (generatorJson == null) {
                return;
            }
            MaterialGeneratorInfo generator = MaterialGeneratorInfo.LOADABLE.deserialize(generatorJson);
            ResourceLocation materialTexture = this.materialTexture(file, json);
            String textureSuffix = this.textureSuffix(materialTexture);
            Set<MaterialStatsId> supportedStats = this.supportedStats(generatorJson);
            boolean ignoreStats = generatorJson.has("ignore_material_stats") && generatorJson.get("ignore_material_stats").getAsBoolean();
            boolean unknown = "tconstruct_unknown".equals(textureSuffix);

            for (PartSpriteSpec spec : SPECS) {
                MaterialStatsId fallbackStat = MaterialFallbackGenerator.getFallbackTextureStat(spec.statType());
                boolean supported = (unknown && spec.toolLayer())
                        || ignoreStats
                        || supportedStats.contains(spec.statType())
                        || supportedStats.contains(fallbackStat);
                if (!supported) {
                    continue;
                }
                ResourceLocation outputId = spec.path().withSuffix("_" + textureSuffix);
                if (this.hasTexture(manager, outputId)) {
                    continue;
                }
                NativeImage source = this.loadSource(manager, spec.path(), json);
                if (source == null) {
                    continue;
                }
                NativeImage transformed = null;
                try {
                    transformed = generator.getTransformer().transformCopy(source, true);
                    JsonObject metadataJson = generator.getTransformer().animationMeta(source);
                    AnimationMetadataSection metadata = metadataJson == null ? AnimationMetadataSection.EMPTY : AnimationMetadataSection.SERIALIZER.fromJson(metadataJson);
                    output.add(outputId, new ImageSupplier(outputId, transformed, metadata));
                    transformed = null;
                } finally {
                    source.close();
                    if (transformed != null) {
                        transformed.close();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private NativeImage loadSource(ResourceManager manager, ResourceLocation part, JsonObject material) {
        JsonArray fallbacks = material.has("fallbacks") ? material.getAsJsonArray("fallbacks") : new JsonArray();
        for (JsonElement fallback : fallbacks) {
            NativeImage image = this.readImage(manager, part.withSuffix("_" + fallback.getAsString()));
            if (image != null) {
                return image;
            }
        }
        return this.readImage(manager, part);
    }

    private NativeImage readImage(ResourceManager manager, ResourceLocation id) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
        Resource resource = manager.getResource(texture).orElse(null);
        if (resource == null) {
            return null;
        }
        try (var input = resource.open()) {
            return NativeImage.read(input);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean hasTexture(ResourceManager manager, ResourceLocation id) {
        return manager.getResource(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png")).isPresent();
    }

    private ResourceLocation materialTexture(ResourceLocation file, JsonObject material) {
        if (material.has("texture")) {
            return ResourceLocation.parse(material.get("texture").getAsString());
        }
        String path = file.getPath();
        String prefix = MATERIAL_FOLDER + "/";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        int split = path.lastIndexOf('/');
        if (split >= 0) {
            path = path.substring(0, split) + "_" + path.substring(split + 1);
        }
        return ResourceLocation.fromNamespaceAndPath(file.getNamespace(), path);
    }

    private String textureSuffix(ResourceLocation texture) {
        return "minecraft".equals(texture.getNamespace()) ? texture.getPath() : texture.getNamespace() + "_" + texture.getPath();
    }

    private Set<MaterialStatsId> supportedStats(JsonObject generator) {
        Set<MaterialStatsId> stats = new HashSet<>();
        JsonArray values = generator.getAsJsonArray("supported_stats");
        if (values != null) {
            for (JsonElement value : values) {
                String raw = value.getAsString();
                ResourceLocation id = ResourceLocation.parse(raw.contains(":") ? raw : "tconstruct:" + raw);
                stats.add(new MaterialStatsId(id.getNamespace(), id.getPath()));
            }
        }
        return stats;
    }

    private record PartSpriteSpec(ResourceLocation path, MaterialStatsId statType, boolean toolLayer) {
    }

    private static final class ImageSupplier implements SpriteSupplier {
        private final ResourceLocation id;
        private NativeImage image;
        private final AnimationMetadataSection metadata;

        private ImageSupplier(ResourceLocation id, NativeImage image, AnimationMetadataSection metadata) {
            this.id = id;
            this.image = image;
            this.metadata = metadata;
        }

        @Override
        public SpriteContents get() {
            NativeImage current = this.image;
            this.image = null;
            if (current == null) {
                return null;
            }
            return new SpriteContents(this.id, metadata.calculateFrameSize(current.getWidth(), current.getHeight()), current, this.metadata);
        }

        @Override
        public void discard() {
            if (this.image != null) {
                this.image.close();
                this.image = null;
            }
        }
    }
}
