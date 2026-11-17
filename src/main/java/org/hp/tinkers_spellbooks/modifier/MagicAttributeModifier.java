package org.hp.tinkers_spellbooks.modifier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.hp.tinkers_spellbooks.item.ModifiableStaffItem;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MagicAttributeModifier extends Modifier implements AttributesModifierHook, ICurioMagicModifier {
    private final List<Bonus> bonuses;

    public MagicAttributeModifier(List<Bonus> bonuses) {
        this.bonuses = List.copyOf(bonuses);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ATTRIBUTES);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (tool.isBroken() || slot.getType() != EquipmentSlot.Type.HAND || !(tool.getItem() instanceof ModifiableStaffItem)) {
            return;
        }
        String slotKey = slot == EquipmentSlot.MAINHAND ? "hand" : "offhand";
        float effectiveLevel = modifier.getEffectiveLevel();
        for (int i = 0; i < this.bonuses.size(); i++) {
            Bonus bonus = this.bonuses.get(i);
            double value = bonus.amountPerLevel() * effectiveLevel;
            if (Math.abs(value) > 1.0E-5) {
                UUID uuid = UUID.nameUUIDFromBytes((this.getId() + ":" + slotKey + ":" + i).getBytes(StandardCharsets.UTF_8));
                consumer.accept(bonus.attribute().get(), new AttributeModifier(uuid, this.getId() + "." + slotKey + "." + i, value, bonus.operation()));
            }
        }
    }

    @Override
    public void addCurioAttributes(IToolStackView tool, ModifierEntry entry, UUID slotUuid, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (tool.isBroken()) {
            return;
        }
        float effectiveLevel = entry.getEffectiveLevel();
        for (int i = 0; i < this.bonuses.size(); i++) {
            Bonus bonus = this.bonuses.get(i);
            double value = bonus.amountPerLevel() * effectiveLevel;
            if (Math.abs(value) > 1.0E-5) {
                UUID uuid = SpellcastingCoreModifier.deriveUuid(slotUuid, this.getId() + ":curio:" + i);
                consumer.accept(bonus.attribute().get(), new AttributeModifier(uuid, this.getId() + ".curio." + i, value, bonus.operation()));
            }
        }
    }

    public record Bonus(Supplier<Attribute> attribute, double amountPerLevel, AttributeModifier.Operation operation) {
    }
}
