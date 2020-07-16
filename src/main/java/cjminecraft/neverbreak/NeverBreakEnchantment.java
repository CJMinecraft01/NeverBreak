package cjminecraft.neverbreak;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

public class NeverBreakEnchantment extends Enchantment {
    public NeverBreakEnchantment() {
        super(Rarity.VERY_RARE, NeverBreak.ALL, EquipmentSlotType.values());
        this.setRegistryName(new ResourceLocation(NeverBreak.MODID, "never_break"));
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 20;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
