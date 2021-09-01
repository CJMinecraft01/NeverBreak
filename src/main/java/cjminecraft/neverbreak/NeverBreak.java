package cjminecraft.neverbreak;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NeverBreak.MODID)
public class NeverBreak {
    public static final String MODID = "neverbreak";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    public static EnchantmentType ALL;

    public static NeverBreakEnchantment NEVER_BREAK;

    public NeverBreak() {
        ALL = EnchantmentType.create("ALL", item -> {
            for (EnchantmentType type : EnchantmentType.values())
                if (type != ALL && type.canEnchant(item))
                    return true;
            return false;
        });
        ForgeRegistries.ENCHANTMENTS.register(NEVER_BREAK = new NeverBreakEnchantment());
    }

}
