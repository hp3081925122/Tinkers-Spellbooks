package org.hp.tinkers_spellbooks.item;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import org.hp.tinkers_spellbooks.client.StaffClientHelper;
import org.hp.tinkers_spellbooks.stats.SpellbookToolStats;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.TooltipBuilder;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ModifiableStaffItem extends ModifiableItem {
    public ModifiableStaffItem(Item.Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getCount() > 1) {
            return InteractionResultHolder.pass(itemStack);
        }
        ToolStack tool = ToolStack.from(itemStack);
        if (tool.isBroken()) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!player.isShiftKeyDown()) {
            SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
            SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
            if (selectionOption != null && !selectionOption.spellData.equals(SpellData.EMPTY)) {
                SpellData spellData = selectionOption.spellData;
                int spellLevel = spellData.getSpell().getLevelFor(spellData.getLevel(), player);
                if (level.isClientSide()) {
                    if (StaffClientHelper.canClientCast(player, spellData, spellLevel)) {
                        return InteractionResultHolder.consume(itemStack);
                    }
                    return InteractionResultHolder.pass(itemStack);
                }
                String castingSlot = hand == InteractionHand.MAIN_HAND ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND;
                if (spellData.getSpell().attemptInitiateCast(itemStack, spellLevel, level, player, selectionOption.getCastSource(), true, castingSlot)) {
                    return InteractionResultHolder.consume(itemStack);
                }
                return InteractionResultHolder.fail(itemStack);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        if (activeModifier != ModifierEntry.EMPTY) {
            return activeModifier.getHook(ModifierHooks.GENERAL_INTERACT).getUseDuration(tool, activeModifier);
        }
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        if (activeModifier != ModifierEntry.EMPTY) {
            return activeModifier.getHook(ModifierHooks.GENERAL_INTERACT).getUseAction(tool, activeModifier);
        }
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        ToolStack tool = ToolStack.from(stack);
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        if (activeModifier != ModifierEntry.EMPTY) {
            super.releaseUsing(stack, level, entity, timeLeft);
            return;
        }
        entity.stopUsingItem();
        Utils.releaseUsingHelper(entity, stack, timeLeft);
    }

    @Override
    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        TooltipBuilder builder = new TooltipBuilder(tool, tooltips);
        if (tool.hasTag(TinkerTags.Items.DURABILITY)) {
            builder.addDurability();
        }
        builder.addWithAttribute(ToolStats.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE);
        builder.add(ToolStats.ATTACK_SPEED);
        builder.add(SpellbookToolStats.SPELL_SLOTS);
        builder.add(SpellbookToolStats.SPELL_POWER);
        builder.add(SpellbookToolStats.MAX_MANA);
        builder.add(SpellbookToolStats.COOLDOWN_REDUCTION);
        builder.add(SpellbookToolStats.CAST_TIME_REDUCTION);
        builder.addAllFreeSlots();
        for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(ModifierHooks.TOOLTIP).addTooltip(tool, entry, player, tooltips, key, tooltipFlag);
        }
        TooltipUtil.addAttributes(this, tool, player, tooltips, TooltipUtil.SHOW_MELEE_ATTRIBUTES, EquipmentSlot.MAINHAND);
        return tooltips;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StaffClientHelper.initStaffClient(consumer));
    }
}
