package cjminecraft.neverbreak;

import com.google.common.collect.ImmutableList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = NeverBreak.MODID)
public class Events {

    public static boolean hasNeverBreakEnchantment(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(NeverBreak.NEVER_BREAK, stack) > 0;
    }

    public static boolean shouldApplyNeverBreak(ItemStack stack) {
        return hasNeverBreakEnchantment(stack) && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @SubscribeEvent
    public static void onItemDestroy(PlayerDestroyItemEvent event) {
        if (!event.getPlayer().isCreative() && hasNeverBreakEnchantment(event.getOriginal())) {
            ItemStack stack = event.getOriginal();
            stack.setDamageValue(stack.getMaxDamage());
            if (event.getHand() != null)
                event.getPlayer().setItemInHand(event.getHand(), stack);
            else {
                event.setCanceled(true);
                if (event.getPlayer().getMainHandItem() == ItemStack.EMPTY)
                    event.getPlayer().setItemInHand(Hand.MAIN_HAND, stack);
                else
                    event.getPlayer().setItemInHand(Hand.OFF_HAND, stack);
            }
        }
    }

    @SubscribeEvent
    public static void onHoeUse(UseHoeEvent event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getContext().getItemInHand()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getPlayer().getMainHandItem()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getPlayer().getMainHandItem()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getPlayer().getMainHandItem()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        // todo test (was -2 not -1)
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getPlayer().getMainHandItem())) {
            event.damageRodBy(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getBow()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getPlayer().isCreative() && shouldApplyNeverBreak(event.getItemStack()) && !(event.getItemStack().getItem() instanceof ArmorItem))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && ((PlayerEntity) event.getEntityLiving()).isCreative())
            return;
        // check if the player is blocking

        AtomicBoolean hasNeverBreakEnchant = new AtomicBoolean(false);
        event.getEntityLiving().getArmorSlots().forEach(stack -> {
            if (hasNeverBreakEnchantment(stack))
                hasNeverBreakEnchant.set(true);
        });
        if (hasNeverBreakEnchant.get()) {
            event.setCanceled(true);

            LivingEntity entity = event.getEntityLiving();
            DamageSource source = event.getSource();
            if (entity.isInvulnerableTo(source))
                return;

            float damageAmount = event.getAmount();

            List<ItemStack> armourList = new ArrayList<>();

            // Apply armour reductions
            if (!source.isBypassArmor()) {
                float totalArmourValue = 0.0F;
                float totalArmourToughness = 0.0F;
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;
                    if (damageAmount > 0) {
                        float damage = damageAmount / 4.0F;
                        if (damage < 1.0F)
                            damage = 1.0F;

                        PlayerInventory inventory = player.inventory;
                        for (int i = 0; i < inventory.armor.size(); ++i) {
                            ItemStack stack = inventory.armor.get(i);
                            if (stack.getItem() instanceof ArmorItem) {
                                if (!hasNeverBreakEnchantment(stack) || stack.getDamageValue() < stack.getMaxDamage() - damage) {
                                    int finalI = i;
                                    stack.hurtAndBreak((int) damage, player, p -> p.broadcastBreakEvent(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, finalI)));
                                    totalArmourValue += ((ArmorItem) stack.getItem()).getDefense();
                                    totalArmourToughness += ((ArmorItem) stack.getItem()).getToughness();
                                    armourList.add(stack);
                                }
                            }
                        }
                    }
                }
                damageAmount = CombatRules.getDamageAfterAbsorb(damageAmount, totalArmourValue, totalArmourToughness);
            } else {
                Iterator<ItemStack> iterator = entity.getArmorSlots().iterator();
                float totalArmourValue = 0.0F;
                float totalArmourToughness = 0.0F;
                if (damageAmount > 0) {
                    float damage = damageAmount / 4.0F;
                    if (damage < 1.0F)
                        damage = 1.0F;

                    for (int i = 0; iterator.hasNext(); i++) {
                        ItemStack stack = iterator.next();
                        if (stack.getItem() instanceof ArmorItem) {
                            if (!hasNeverBreakEnchantment(stack) || stack.getDamageValue() < stack.getMaxDamage() - damage) {
                                int finalI = i;
                                stack.hurtAndBreak((int) damage, entity, p -> p.broadcastBreakEvent(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, finalI)));
                                totalArmourValue += ((ArmorItem) stack.getItem()).getDefense();
                                totalArmourToughness += ((ArmorItem) stack.getItem()).getToughness();
                                armourList.add(stack);
                            }
                        }
                    }
                }
                damageAmount = CombatRules.getDamageAfterAbsorb(damageAmount, totalArmourValue, totalArmourToughness);
            }

            // Apply potion reductions

            if (!source.isBypassMagic()) {
                float damage = damageAmount;
                if (entity.hasEffect(Effects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
                    int i = (entity.getEffect(Effects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                    int j = 25 - i;
                    float f = damage * (float) j;
                    float f1 = damage;
                    damage = Math.max(f / 25.0F, 0.0F);
                    float f2 = f1 - damage;
                    if (f2 > 0.0F && f2 < 3.4028235E37F) {
                        if (entity instanceof ServerPlayerEntity) {
                            ((ServerPlayerEntity) entity).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                        } else if (source.getEntity() instanceof ServerPlayerEntity) {
                            ((ServerPlayerEntity) source.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                        }
                    }
                }

                if (damage <= 0.0F) {
                    damageAmount = 0.0F;
                } else {
                    int k = EnchantmentHelper.getDamageProtection(armourList, source);
                    if (k > 0) {
                        damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
                    }

                    damageAmount = damage;
                }
            }

            float f2 = Math.max(damageAmount - entity.getAbsorptionAmount(), 0.0F);

            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (damageAmount - f2));
            float f = damageAmount - f2;
            if (f > 0.0F && f < 3.4028235E37F && source.getEntity() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) source.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
            }

            f2 = net.minecraftforge.common.ForgeHooks.onLivingDamage(entity, source, f2);
            if (f2 != 0.0F) {
                float f1 = entity.getHealth();
                entity.getCombatTracker().recordDamage(source, f1, f2);
                entity.setHealth(f1 - f2); // Forge: moved to fix MC-121048
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() - f2);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getPlayer().isCreative() && hasNeverBreakEnchantment(event.getItemStack()) && (event.getItemStack().getDamageValue() >= event.getItemStack().getMaxDamage() - 1 && !(event.getItemStack().getItem() instanceof ArmorItem)))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityRightClicked(PlayerInteractEvent.EntityInteract event) {
        if (!event.getPlayer().isCreative() && hasNeverBreakEnchantment(event.getItemStack()) && (event.getItemStack().getDamageValue() >= event.getItemStack().getMaxDamage() - 1 && !(event.getItemStack().getItem() instanceof ArmorItem)))
            event.setCanceled(true);
    }
}
