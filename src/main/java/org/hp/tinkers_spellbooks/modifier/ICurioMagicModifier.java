package org.hp.tinkers_spellbooks.modifier;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.UUID;
import java.util.function.BiConsumer;

public interface ICurioMagicModifier {
    void addCurioAttributes(IToolStackView tool, ModifierEntry entry, UUID slotUuid, BiConsumer<Attribute, AttributeModifier> consumer);
}
