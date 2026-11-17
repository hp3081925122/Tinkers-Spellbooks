package org.hp.tinkers_spellbooks.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.ILecternPlaceable;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import org.hp.tinkers_spellbooks.client.StaffClientHelper;
import org.hp.tinkers_spellbooks.compat.SpellContainerSyncHelper;
import org.hp.tinkers_spellbooks.modifier.ICurioMagicModifier;
import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.TooltipBuilder;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ModifiableSpellbookItem extends ModifiableItem implements ICurioItem, ISpellbook, ILecternPlaceable, GeoItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.modular_spellbook.idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ModifiableSpellbookItem(Item.Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return !ToolStack.from(stack).isBroken();
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundRegistry.EQUIP_SPELL_BOOK.get(), 1.0f, 1.0f);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !Curios.SPELLBOOK_SLOT.equals(slotContext.identifier()) || TooltipUtil.isDisplay(stack)) {
            return ImmutableMultimap.of();
        }
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        for (ModifierEntry entry : tool.getModifierList()) {
            if (entry.getModifier() instanceof ICurioMagicModifier curioModifier) {
                curioModifier.addCurioAttributes(tool, entry, uuid, builder::put);
            }
        }
        return builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(context.getPlayer(), level, pos, state, context.getItemInHand())
                    ? InteractionResult.sidedSuccess(level.isClientSide)
                    : InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        ISpellContainer spellbookData = ISpellContainer.get(stack);
        if (spellbookData != null && !spellbookData.isEmpty()) {
            return spellbookData.getActiveSpells().stream().map(slot -> {
                int color = slot.getSpell().getSchoolType().getDisplayName().getStyle().getColor() != null
                        ? slot.getSpell().getSchoolType().getDisplayName().getStyle().getColor().getValue()
                        : 0x332266;
                color = RenderHelper.colorLerp(0.6f, color, 0);
                Style titleStyle = Style.EMPTY.withColor(color).withUnderlined(true).withBold(true);
                MutableComponent title = Component.translatable(slot.getSpell().getComponentId()).withStyle(titleStyle);
                MutableComponent desc = Component.translatable(slot.getSpell().getComponentId() + ".guide").withStyle(ChatFormatting.BLACK);
                return (Component) Component.literal("").append(title).append("\n\n").append(desc);
            }).toList();
        }
        return List.of(Component.translatable("ui.irons_spellbooks.empty_spellbook_lectern").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipKey key = SafeClientAccess.getTooltipKey();
        TooltipUtil.addInformation(this, stack, level, tooltip, key, flag);
        if (key == TooltipKey.NORMAL && !TooltipUtil.isDisplay(stack)) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StaffClientHelper.appendSpellbookSpellsTooltip(stack, tooltip));
        }
    }

    @Override
    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        TooltipBuilder builder = new TooltipBuilder(tool, tooltips);
        if (tool.hasTag(TinkerTags.Items.DURABILITY)) {
            builder.addDurability();
        }
        int totalSlots = SpellContainerSyncHelper.computeTargetMaxSlots(tool);
        tooltips.add(SpellbookToolStats.SPELL_SLOTS.formatValue((float) totalSlots));
        builder.add(SpellbookToolStats.SPELL_POWER);
        builder.add(SpellbookToolStats.MAX_MANA);
        builder.add(SpellbookToolStats.COOLDOWN_REDUCTION);
        builder.add(SpellbookToolStats.CAST_TIME_REDUCTION);
        builder.addAllFreeSlots();
        for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(ModifierHooks.TOOLTIP).addTooltip(tool, entry, player, tooltips, key, tooltipFlag);
        }
        return tooltips;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> state.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StaffClientHelper.initSpellbookClient(consumer));
    }
}
