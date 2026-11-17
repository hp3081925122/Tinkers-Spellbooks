package org.hp.tinkers_spellbooks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.hp.tinkers_spellbooks.TinkersSpellbooks;
import org.hp.tinkers_spellbooks.item.ModifiableSpellbookItem;
import org.hp.tinkers_spellbooks.item.ModifiableStaffItem;
import org.hp.tinkers_spellbooks.stats.ArcaneBindingMaterialStats;
import org.hp.tinkers_spellbooks.stats.BookCoverMaterialStats;
import org.hp.tinkers_spellbooks.stats.SpellPagesMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHandleMaterialStats;
import org.hp.tinkers_spellbooks.stats.StaffHeadMaterialStats;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;

public final class SpellbookItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TinkersSpellbooks.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersSpellbooks.MOD_ID);

    public static final ToolDefinition ARCANE_STAFF_DEFINITION = ToolDefinition.create(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "arcane_staff"));
    public static final ToolDefinition MODULAR_SPELLBOOK_DEFINITION = ToolDefinition.create(ResourceLocation.fromNamespaceAndPath(TinkersSpellbooks.MOD_ID, "modular_spellbook"));

    public static final RegistryObject<ModifiableStaffItem> ARCANE_STAFF = ITEMS.register(
            "arcane_staff",
            () -> new ModifiableStaffItem(new Item.Properties().stacksTo(1), ARCANE_STAFF_DEFINITION)
    );

    public static final RegistryObject<ModifiableSpellbookItem> MODULAR_SPELLBOOK = ITEMS.register(
            "modular_spellbook",
            () -> new ModifiableSpellbookItem(new Item.Properties().stacksTo(1), MODULAR_SPELLBOOK_DEFINITION)
    );

    public static final RegistryObject<ToolPartItem> STAFF_HEAD = ITEMS.register(
            "staff_head",
            () -> new ToolPartItem(new Item.Properties(), StaffHeadMaterialStats.ID)
    );
    public static final RegistryObject<ToolPartItem> STAFF_HANDLE = ITEMS.register(
            "staff_handle",
            () -> new ToolPartItem(new Item.Properties(), StaffHandleMaterialStats.ID)
    );
    public static final RegistryObject<ToolPartItem> BOOK_COVER = ITEMS.register(
            "book_cover",
            () -> new ToolPartItem(new Item.Properties(), BookCoverMaterialStats.ID)
    );
    public static final RegistryObject<ToolPartItem> SPELL_PAGES = ITEMS.register(
            "spell_pages",
            () -> new ToolPartItem(new Item.Properties(), SpellPagesMaterialStats.ID)
    );
    public static final RegistryObject<ToolPartItem> ARCANE_BINDING = ITEMS.register(
            "arcane_binding",
            () -> new ToolPartItem(new Item.Properties(), ArcaneBindingMaterialStats.ID)
    );

    public static final RegistryObject<Item> STAFF_HEAD_CAST = ITEMS.register("staff_head_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAFF_HEAD_SAND_CAST = ITEMS.register("staff_head_sand_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAFF_HEAD_RED_SAND_CAST = ITEMS.register("staff_head_red_sand_cast", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STAFF_HANDLE_CAST = ITEMS.register("staff_handle_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAFF_HANDLE_SAND_CAST = ITEMS.register("staff_handle_sand_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAFF_HANDLE_RED_SAND_CAST = ITEMS.register("staff_handle_red_sand_cast", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BOOK_COVER_CAST = ITEMS.register("book_cover_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BOOK_COVER_SAND_CAST = ITEMS.register("book_cover_sand_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BOOK_COVER_RED_SAND_CAST = ITEMS.register("book_cover_red_sand_cast", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SPELL_PAGES_CAST = ITEMS.register("spell_pages_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_PAGES_SAND_CAST = ITEMS.register("spell_pages_sand_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_PAGES_RED_SAND_CAST = ITEMS.register("spell_pages_red_sand_cast", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ARCANE_BINDING_CAST = ITEMS.register("arcane_binding_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ARCANE_BINDING_SAND_CAST = ITEMS.register("arcane_binding_sand_cast", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ARCANE_BINDING_RED_SAND_CAST = ITEMS.register("arcane_binding_red_sand_cast", () -> new Item(new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tinkers_spellbooks.main"))
            .icon(() -> MODULAR_SPELLBOOK.get().getRenderTool())
            .displayItems((params, output) -> {
                ToolBuildHandler.addVariants(output::accept, MODULAR_SPELLBOOK.get(), "");
                ToolBuildHandler.addVariants(output::accept, ARCANE_STAFF.get(), "");
                STAFF_HEAD.get().addVariants(output::accept, "");
                STAFF_HANDLE.get().addVariants(output::accept, "");
                BOOK_COVER.get().addVariants(output::accept, "");
                SPELL_PAGES.get().addVariants(output::accept, "");
                ARCANE_BINDING.get().addVariants(output::accept, "");
                acceptAllCasts(output::accept);
            })
            .build());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(SpellbookItems::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == TinkerTools.tabTools.get()) {
            ToolBuildHandler.addVariants(event::accept, MODULAR_SPELLBOOK.get(), "");
            ToolBuildHandler.addVariants(event::accept, ARCANE_STAFF.get(), "");
            return;
        }
        if (event.getTab() == TinkerToolParts.tabToolParts.get()) {
            STAFF_HEAD.get().addVariants(event::accept, "");
            STAFF_HANDLE.get().addVariants(event::accept, "");
            BOOK_COVER.get().addVariants(event::accept, "");
            SPELL_PAGES.get().addVariants(event::accept, "");
            ARCANE_BINDING.get().addVariants(event::accept, "");
            return;
        }
        if (event.getTab() == TinkerSmeltery.tabSmeltery.get()) {
            acceptAllCasts(event::accept);
        }
    }

    private static void acceptAllCasts(java.util.function.Consumer<net.minecraft.world.item.ItemStack> consumer) {
        consumer.accept(STAFF_HEAD_CAST.get().getDefaultInstance());
        consumer.accept(STAFF_HEAD_SAND_CAST.get().getDefaultInstance());
        consumer.accept(STAFF_HEAD_RED_SAND_CAST.get().getDefaultInstance());
        consumer.accept(STAFF_HANDLE_CAST.get().getDefaultInstance());
        consumer.accept(STAFF_HANDLE_SAND_CAST.get().getDefaultInstance());
        consumer.accept(STAFF_HANDLE_RED_SAND_CAST.get().getDefaultInstance());
        consumer.accept(BOOK_COVER_CAST.get().getDefaultInstance());
        consumer.accept(BOOK_COVER_SAND_CAST.get().getDefaultInstance());
        consumer.accept(BOOK_COVER_RED_SAND_CAST.get().getDefaultInstance());
        consumer.accept(SPELL_PAGES_CAST.get().getDefaultInstance());
        consumer.accept(SPELL_PAGES_SAND_CAST.get().getDefaultInstance());
        consumer.accept(SPELL_PAGES_RED_SAND_CAST.get().getDefaultInstance());
        consumer.accept(ARCANE_BINDING_CAST.get().getDefaultInstance());
        consumer.accept(ARCANE_BINDING_SAND_CAST.get().getDefaultInstance());
        consumer.accept(ARCANE_BINDING_RED_SAND_CAST.get().getDefaultInstance());
    }

    private SpellbookItems() {
    }
}
