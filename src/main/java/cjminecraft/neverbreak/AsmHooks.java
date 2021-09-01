package cjminecraft.neverbreak;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.MathHelper;

public class AsmHooks {

    public static boolean isBlocking(LivingEntity entity) {
        return entity.isUsingItem() && !entity.getUseItem().isEmpty() && Events.shouldApplyNeverBreak(entity.getUseItem());
    }

    public static boolean hurtCurrentlyUsedShield(PlayerEntity player, float amount) {
        if (player.getUseItem().isShield(player)) {
            if (amount >= 3F && Events.hasNeverBreakEnchantment(player.getUseItem())) {
                int damage = 1 + MathHelper.floor(amount);
                if (player.getUseItem().getDamageValue() + damage >= player.getUseItem().getMaxDamage() - 1) {
                    if (!player.level.isClientSide)
                        player.awardStat(Stats.ITEM_USED.get(player.getUseItem().getItem()));
                    player.getUseItem().setDamageValue(player.getUseItem().getMaxDamage() - 1);
                    player.getCooldowns().addCooldown(player.getUseItem().getItem(), 100);
                    player.stopUsingItem();
                    return true;
                }
            }
        }
        return false;
    }

}
