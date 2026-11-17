package org.hp.tinkers_spellbooks.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.hp.tinkers_spellbooks.registry.SpellbookItems;
import org.hp.tinkers_spellbooks.stats.ArcaneBindingMaterialStats;
import org.hp.tinkers_spellbooks.stats.BookCoverMaterialStats;
import org.hp.tinkers_spellbooks.stats.MaterialFallbackGenerator;
import org.hp.tinkers_spellbooks.stats.SpellPagesMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHandleMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHeadMaterialStats;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.book.transformer.BookTransformer;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.book.TinkerBook;
import slimeknights.tconstruct.library.client.book.content.AbstractMaterialContent;
import slimeknights.tconstruct.library.client.book.sectiontransformer.materials.TierRangeMaterialSectionTransformer;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpellbookBookSections {
    private static final ResourceLocation PAGE_ID = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "spellbook_material");
    private static final ResourceLocation LANGUAGE = ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "book/materials/language.lang");
    private static final ResourceLocation LOAD_MODIFIERS = ResourceLocation.fromNamespaceAndPath("tconstruct", "load_modifiers");
    private static final String GROUP_UPGRADES = "group_spellcasting_upgrades";
    private static final String GROUP_ABILITIES = "group_spellcasting_abilities";

    public static void register() {
        for (MaterialStatsId stat : MaterialFallbackGenerator.ALL_STAT_IDS) {
            TierRangeMaterialSectionTransformer.registerMaterialType(stat, (variant, detailed) -> new Page(variant, detailed, stat), stat);
        }
        add(TinkerBook.MATERIALS_AND_YOU, 0, 1, true, false);
        add(TinkerBook.PUNY_SMELTING, 2, 2, true, false);
        add(TinkerBook.MIGHTY_SMELTING, 3, 3, true, false);
        add(TinkerBook.ENCYCLOPEDIA, 0, Short.MAX_VALUE, false, true);
        prependTransformer(TinkerBook.PUNY_SMELTING);
        prependTransformer(TinkerBook.MIGHTY_SMELTING);
        prependTransformer(TinkerBook.ENCYCLOPEDIA);
    }

    private static void prependTransformer(BookData book) {
        List<BookTransformer> transformers = ObfuscationReflectionHelper.getPrivateValue(BookData.class, book, "transformers");
        if (transformers != null) {
            transformers.add(0, SpellcastingModifierGroupTransformer.INSTANCE);
        } else {
            book.addTransformer(SpellcastingModifierGroupTransformer.INSTANCE);
        }
    }

    private static void add(BookData book, int minTier, int maxTier, boolean excludeDistant, boolean detailed) {
        book.addRepository(new Repository(minTier, maxTier, excludeDistant, detailed));
    }

    private static String sectionName(MaterialStatsId stat) {
        return ("tsb_" + stat.getPath()).replace('/', '_').toLowerCase();
    }

    private static String iconItemFor(MaterialStatsId stat) {
        if (StaffHeadMaterialStats.ID.equals(stat)) {
            return SpellbookItems.STAFF_HEAD.getId().toString();
        }
        if (StaffHandleMaterialStats.ID.equals(stat)) {
            return SpellbookItems.STAFF_HANDLE.getId().toString();
        }
        if (BookCoverMaterialStats.ID.equals(stat)) {
            return SpellbookItems.BOOK_COVER.getId().toString();
        }
        if (SpellPagesMaterialStats.ID.equals(stat)) {
            return SpellbookItems.SPELL_PAGES.getId().toString();
        }
        if (ArcaneBindingMaterialStats.ID.equals(stat)) {
            return SpellbookItems.ARCANE_BINDING.getId().toString();
        }
        return SpellbookItems.MODULAR_SPELLBOOK.getId().toString();
    }

    private static class Repository extends BookRepository {
        private final int minTier;
        private final int maxTier;
        private final boolean excludeDistant;
        private final boolean detailed;

        private Repository(int minTier, int maxTier, boolean excludeDistant, boolean detailed) {
            this.minTier = minTier;
            this.maxTier = maxTier;
            this.excludeDistant = excludeDistant;
            this.detailed = detailed;
        }

        private boolean hasMaterial(MaterialStatsId stat) {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            for (IMaterial material : MaterialRegistry.getMaterials()) {
                int tier = material.getTier();
                if (tier < this.minTier || tier > this.maxTier) {
                    continue;
                }
                if (this.excludeDistant && registry.isInTag(material.getIdentifier(), TinkerTags.Materials.DISTANT)) {
                    continue;
                }
                if (registry.getMaterialStats(material.getIdentifier(), stat).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<SectionData> getSections() {
            JsonArray sections = new JsonArray();
            for (MaterialStatsId stat : MaterialFallbackGenerator.ALL_STAT_IDS) {
                if (!this.hasMaterial(stat)) {
                    continue;
                }
                JsonObject tier = new JsonObject();
                tier.addProperty("min", this.minTier);
                tier.addProperty("max", this.maxTier);
                JsonObject materialTier = new JsonObject();
                materialTier.addProperty("type", stat.toString());
                materialTier.addProperty("detailed", this.detailed);
                materialTier.add("tier", tier);
                if (this.excludeDistant) {
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty("type", "mantle:inverted");
                    predicate.addProperty("inverted_type", "mantle:tag");
                    predicate.addProperty("tag", TinkerTags.Materials.DISTANT.location().toString());
                    materialTier.add("predicate", predicate);
                }
                JsonObject extraData = new JsonObject();
                extraData.add("tconstruct:material_tier", materialTier);
                JsonObject icon = new JsonObject();
                icon.addProperty("item", iconItemFor(stat));
                JsonObject section = new JsonObject();
                section.addProperty("name", sectionName(stat));
                section.addProperty("data", "no-load");
                section.add("extraData", extraData);
                section.add("icon", icon);
                sections.add(section);
            }
            return new ArrayList<>(Arrays.asList(BookLoader.getGson().fromJson(sections, SectionData[].class)));
        }

        @Nullable
        @Override
        public ResourceLocation getResourceLocation(@Nullable String path, boolean safe) {
            if ("language.lang".equals(path)) {
                return LANGUAGE;
            }
            return safe ? ResourceLocation.parse("") : null;
        }

        @Override
        public Optional<Resource> getLocation(@Nullable ResourceLocation location) {
            if (!LANGUAGE.equals(location)) {
                return Optional.empty();
            }
            StringBuilder lang = new StringBuilder();
            for (MaterialStatsId stat : MaterialFallbackGenerator.ALL_STAT_IDS) {
                lang.append(sectionName(stat)).append('=').append(I18n.get("stat." + stat.getNamespace() + "." + stat.getPath())).append('\n');
            }
            lang.append("upgrades.").append(GROUP_UPGRADES).append('=').append(I18n.get("book.tinkers_spellbooks.group_spellcasting_upgrades")).append('\n');
            lang.append("abilities.").append(GROUP_ABILITIES).append('=').append(I18n.get("book.tinkers_spellbooks.group_spellcasting_abilities")).append('\n');
            byte[] bytes = lang.toString().getBytes(StandardCharsets.UTF_8);
            return Optional.of(new Resource(Minecraft.getInstance().getVanillaPackResources(), () -> new ByteArrayInputStream(bytes)));
        }

        @Override
        public String resourceToString(@Nullable Resource resource, boolean skipComments) {
            if (resource == null) {
                return "";
            }
            try (var stream = resource.open()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        }
    }

    private static class Page extends AbstractMaterialContent {
        private final transient MaterialStatsId stat;

        private Page(MaterialVariantId variant, boolean detailed, MaterialStatsId stat) {
            super(variant, detailed);
            this.stat = stat;
        }

        @Override
        public ResourceLocation getId() {
            return PAGE_ID;
        }

        @Override
        protected MaterialStatsId getStatType(int index) {
            return index == 0 ? this.stat : null;
        }

        @Override
        protected int getStatRows() {
            return 1;
        }

        @Override
        protected boolean supportsStatType(MaterialStatsId stat) {
            return stat.equals(this.stat);
        }

        @Override
        protected String getTextKey(MaterialId material) {
            String base = "material." + material.getNamespace() + "." + material.getPath() + (this.detailed ? ".encyclopedia" : ".flavor");
            String specific = base + "." + this.stat.getPath();
            return I18n.exists(specific) ? specific : base;
        }
    }

    private static final class SpellcastingModifierGroupTransformer extends BookTransformer {
        private static final SpellcastingModifierGroupTransformer INSTANCE = new SpellcastingModifierGroupTransformer();

        @Override
        public void transform(BookData book) {
            book.strings.put("upgrades." + GROUP_UPGRADES, I18n.get("book.tinkers_spellbooks.group_spellcasting_upgrades"));
            book.strings.put("abilities." + GROUP_ABILITIES, I18n.get("book.tinkers_spellbooks.group_spellcasting_abilities"));
            addModifierGroup(book.findSection("upgrades"), GROUP_UPGRADES, "tinkers_spellbooks:upgrades", "upgrades/spellcasting", "spellcasting");
            addModifierGroup(book.findSection("abilities"), GROUP_ABILITIES, "tinkers_spellbooks:abilities", "abilities/spellcasting", "spellcasting");
        }

        private static void addModifierGroup(@Nullable SectionData section, String groupName, String tag, String path, String prefix) {
            if (section == null || section.pages.stream().anyMatch(page -> groupName.equals(page.name))) {
                return;
            }
            JsonObject loadModifiers = new JsonObject();
            loadModifiers.addProperty("tag", tag);
            loadModifiers.addProperty("path", path);
            loadModifiers.addProperty("prefix", prefix);
            Map<ResourceLocation, JsonElement> extraData = new HashMap<>();
            extraData.put(LOAD_MODIFIERS, loadModifiers);
            PageData page = new PageData(false);
            page.name = groupName;
            page.data = "";
            page.extraData = extraData;
            page.parent = section;
            page.source = section.source;
            page.load();
            section.pages.add(page);
        }
    }

    private SpellbookBookSections() {
    }
}
