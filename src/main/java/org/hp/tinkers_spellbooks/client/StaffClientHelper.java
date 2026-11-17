package org.hp.tinkers_spellbooks.client;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.hp.tinkers_spellbooks.item.ModifiableSpellbookItem;
import org.hp.tinkers_spellbooks.item.ModifiableStaffItem;
import slimeknights.tconstruct.library.client.item.ModifiableItemClientExtension;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class StaffClientHelper {
    public static void initStaffClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ModifiableItemClientExtension() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            @Nullable
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return StaffArmPose.STAFF_ARM_POS;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ModularGeoItemRenderer<ModifiableStaffItem>("arcane_staff", 0);
                }
                return this.renderer;
            }
        });
    }

    public static void initSpellbookClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ModularGeoItemRenderer<ModifiableSpellbookItem>("modular_spellbook", 2);
                }
                return this.renderer;
            }
        });
    }

    public static boolean canClientCast(Player player, SpellData spellData, int spellLevel) {
        if (ClientMagicData.isCasting()) {
            return true;
        }
        if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellLevel)) {
            return false;
        }
        if (ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())) {
            return false;
        }
        return ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell());
    }

    public static void appendSpellbookSpellsTooltip(ItemStack itemStack, List<Component> lines) {
        Player rawPlayer = MinecraftInstanceHelper.getPlayer();
        if (!(rawPlayer instanceof LocalPlayer player) || !ISpellContainer.isSpellContainer(itemStack)) {
            return;
        }
        ISpellContainer spellList = ISpellContainer.get(itemStack);
        lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count", spellList.getMaxSpellCount()).withStyle(ChatFormatting.GRAY));
        List<SpellSlot> activeSpellSlots = spellList.getActiveSpells();
        if (activeSpellSlots.isEmpty()) {
            return;
        }
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.irons_spellbooks.press_to_cast", Component.keybind("key.irons_spellbooks.spellbook_cast")).withStyle(ChatFormatting.GOLD));
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_tooltip").withStyle(ChatFormatting.GRAY));
        SpellSelectionManager spellSelectionManager = ClientMagicData.getSpellSelectionManager();
        for (int i = 0; i < activeSpellSlots.size(); i++) {
            MutableComponent spellText = TooltipsUtils.getTitleComponent(activeSpellSlots.get(i).spellData(), player).setStyle(Style.EMPTY);
            SpellSelectionManager.SelectionOption option = spellSelectionManager.getSpellSlot(spellSelectionManager.getSelectionIndex());
            if (Utils.getPlayerSpellbookStack(player) == itemStack && option != null && option.slot.equals(Curios.SPELLBOOK_SLOT) && option.slotIndex == i) {
                List<MutableComponent> shiftMessage = TooltipsUtils.formatActiveSpellTooltip(itemStack, spellSelectionManager.getSelectedSpellData(), CastSource.SPELLBOOK, player);
                if (!shiftMessage.isEmpty()) {
                    shiftMessage.remove(0);
                }
                TooltipsUtils.addShiftTooltip(lines, Component.literal("> ").append(spellText).withStyle(ChatFormatting.YELLOW), shiftMessage.stream().map(component -> Component.literal(" ").append(component)).collect(Collectors.toList()));
            } else {
                lines.add(Component.literal(" ").append(spellText.withStyle(Style.EMPTY.withColor(0x8888FE))));
            }
        }
    }

    private StaffClientHelper() {
    }
}
