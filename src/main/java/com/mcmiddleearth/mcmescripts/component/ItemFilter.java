package com.mcmiddleearth.mcmescripts.component;

import com.google.common.base.Joiner;
import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemFilter {

    private final AbstractMap.SimpleEntry<ItemPropertyState, Material> material;
    private final AbstractMap.SimpleEntry<ItemPropertyState, Integer> quantity;
    private final AbstractMap.SimpleEntry<ItemPropertyState, Integer> damage;
    private final AbstractMap.SimpleEntry<ItemPropertyState, String> name;
    private final AbstractMap.SimpleEntry<ItemPropertyState, List<String>> lore;
    private final AbstractMap.SimpleEntry<ItemPropertyState, Set<AttributeModifier>> attributeModifiers;
    private final AbstractMap.SimpleEntry<ItemPropertyState, Set<WrappedEnchantment>> enchantments;
    private final AbstractMap.SimpleEntry<ItemPropertyState, Integer> customModelData;
    private final AbstractMap.SimpleEntry<ItemPropertyState, EquipmentSlot> equipmentSlot;

    public ItemFilter(AbstractMap.SimpleEntry<ItemPropertyState, Material> material, AbstractMap.SimpleEntry<ItemPropertyState, Integer> quantity, AbstractMap.SimpleEntry<ItemPropertyState, Integer> damage, AbstractMap.SimpleEntry<ItemPropertyState, String> name, AbstractMap.SimpleEntry<ItemPropertyState, List<String>> lore, AbstractMap.SimpleEntry<ItemPropertyState, Set<AttributeModifier>> attributeModifiers, AbstractMap.SimpleEntry<ItemPropertyState, Set<WrappedEnchantment>> enchantments, AbstractMap.SimpleEntry<ItemPropertyState, Integer> customModelData, AbstractMap.SimpleEntry<ItemPropertyState, EquipmentSlot> equipmentSlot) {
        this.material = material;
        this.quantity = quantity;
        this.damage = damage;
        this.name = name;
        this.lore = lore;
        this.attributeModifiers = attributeModifiers;
        this.enchantments = enchantments;
        this.customModelData = customModelData;
        this.equipmentSlot = equipmentSlot;
    }

    public boolean match(ItemStack itemCheck, Inventory inventory) {
        if(itemCheck==null) {
            return false;
        }
        if (material != null) {
            if (material.getKey() == ItemPropertyState.PRESENT) {
                if (material.getValue() != itemCheck.getType()) {
                    return false;
                }
            } else if (material.getKey() == ItemPropertyState.NOT_PRESENT) {
                if (material.getValue() == itemCheck.getType()) {
                    return false;
                }
            }
        }

        if (quantity != null) {
            if (quantity.getKey() == ItemPropertyState.PRESENT) {
                if (quantity.getValue() != itemCheck.getAmount()) {
                    return false;
                }
            } else if (quantity.getKey() == ItemPropertyState.NOT_PRESENT) {
                if (quantity.getValue() == itemCheck.getAmount()) {
                    return false;
                }
            }
        }

        ItemMeta itemMeta = itemCheck.getItemMeta();

        if (damage != null) {
            if (damage.getKey() == ItemPropertyState.PRESENT) {
                if(itemMeta instanceof Damageable) {
                    Damageable  itemDamageMeta = (Damageable) itemMeta;
                    if (damage.getValue() != itemDamageMeta.getDamage()) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            else if (damage.getKey() == ItemPropertyState.NOT_PRESENT) {
                if(itemMeta instanceof Damageable) {
                    Damageable  itemDamageMeta = (Damageable) itemMeta;
                    if (damage.getValue() == itemDamageMeta.getDamage()) {
                        return false;
                    }
                }
            }
        }

        if (name != null) {
            if (name.getKey() == ItemPropertyState.PRESENT) {
                Component displayName = itemMeta.displayName();
                if (displayName == null) {
                    return false;
                }
                if (!name.getValue().equals(displayName.examinableName())) {
                    return false;
                }
            } else if (name.getKey() == ItemPropertyState.NOT_PRESENT) {
                Component displayName = itemMeta.displayName();
                if (displayName == null) {
                    return false;
                }
                if (!name.getValue().equals(displayName.examinableName())) {
                    return false;
                }
            }
        }

        if (lore != null) {
            if (lore.getKey() == ItemPropertyState.PRESENT) {
                List<Component> lore = itemCheck.lore();
                if (lore == null) {
                    return false;
                }
                List<String> actualLore = new ArrayList<>();
                for (Component component : lore) {
                    String examinableName = component.examinableName();
                    actualLore.add(examinableName);
                }
                if (!this.lore.getValue().equals(actualLore)) {
                    return false;
                }
            }
        }

        if (attributeModifiers != null) {
            if (attributeModifiers.getKey() == ItemPropertyState.PRESENT) {
                // TODO
            }
        }

        if (enchantments != null) {
            if (enchantments.getKey() == ItemPropertyState.PRESENT) {
                // TODO
            }
        }

        if (customModelData != null) {
            if (customModelData.getKey() == ItemPropertyState.PRESENT) {
                if (customModelData.getValue() != itemMeta.getCustomModelData()) {
                    return false;
                }
            }
            else if (customModelData.getKey() == ItemPropertyState.NOT_PRESENT) {
                if (customModelData.getValue() == itemMeta.getCustomModelData()) {
                    return false;
                }
            }
        }

        if (equipmentSlot != null && inventory instanceof EntityEquipment) {
            if (equipmentSlot.getKey() == ItemPropertyState.PRESENT) {
                return ((EntityEquipment)inventory).getItem(equipmentSlot.getValue()) == itemCheck;
            }
            else if (equipmentSlot.getKey() == ItemPropertyState.NOT_PRESENT) {
                return ((EntityEquipment)inventory).getItem(equipmentSlot.getValue()) != itemCheck;
            }
        }

        return true;
    }

    public Descriptor getDescriptor() {
        Descriptor descriptor = new Descriptor("Material: "+this.material.getKey().name()+" - "+this.material.getValue().name())
                .addLine("Quantity: "+(this.quantity == null? null:this.quantity.getKey().name()+" - "+this.quantity.getValue()))
                .addLine("Damage: "+(this.damage == null? null:this.damage.getKey().name()+" - "+this.damage.getValue()))
                .addLine("Name: "+(this.name == null? null:this.name.getKey().name()+" - "+this.name.getValue()))
                .addLine("CMD: "+(this.customModelData == null? null:this.customModelData.getKey().name()+" - "+ this.customModelData.getValue()))
                .addLine("Equipment slot: "+(this.equipmentSlot == null? null:this.equipmentSlot.getKey().name()+" - "+ this.equipmentSlot.getValue().name()))
                .addLine("Lore: "+(this.lore == null? null:this.lore.getKey().name()+" - "+ Joiner.on(" ").join(this.lore.getValue())))
                .addLine("Attribute modifiers: "+(this.attributeModifiers == null? null:this.attributeModifiers.getKey().name()+" - "+Joiner.on(" ").join(this.attributeModifiers.getValue())))
                .addLine("Attribute modifiers: "+(this.enchantments == null? null:this.enchantments.getKey().name()+" - "+Joiner.on(" ").join(this.enchantments.getValue())));
        return descriptor;
    }
}
