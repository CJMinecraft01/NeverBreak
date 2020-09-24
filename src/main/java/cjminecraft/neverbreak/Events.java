package cjminecraft.neverbreak;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = NeverBreak.MODID)
public class Events {

    public static final NeverBreakEnchantment NEVER_BREAK = new NeverBreakEnchantment();

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(NEVER_BREAK);
    }

    public static boolean hasNeverBreakEnchantment(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(NEVER_BREAK, stack) > 0;
    }

    @SubscribeEvent
    public static void onItemDestroy(PlayerDestroyItemEvent event) {
        if (event.getOriginal() == null)
            return;
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getOriginal())) {
            ItemStack stack = event.getOriginal();
            stack.setItemDamage(stack.getMaxDamage());
            if (event.getHand() != null)
                event.getEntityPlayer().setHeldItem(event.getHand(), stack);
            else {
                event.setCanceled(true);
                if (event.getEntityPlayer().getHeldItemMainhand() == ItemStack.EMPTY)
                    event.getEntityPlayer().setHeldItem(EnumHand.MAIN_HAND, stack);
                else
                    event.getEntityPlayer().setHeldItem(EnumHand.OFF_HAND, stack);
            }
        }
    }

    @SubscribeEvent
    public static void onHoeUse(UseHoeEvent event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getEntityPlayer().getHeldItemMainhand()) && event.getEntityPlayer().getHeldItemMainhand().getItemDamage() >= event.getEntityPlayer().getHeldItemMainhand().getMaxDamage() - 1)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getEntityPlayer().getHeldItemMainhand()) && event.getEntityPlayer().getHeldItemMainhand().getItemDamage() >= event.getEntityPlayer().getHeldItemMainhand().getMaxDamage() - 1)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getEntityPlayer().getHeldItemMainhand()) && event.getEntityPlayer().getHeldItemMainhand().getItemDamage() >= event.getEntityPlayer().getHeldItemMainhand().getMaxDamage() - 1)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative() && hasNeverBreakEnchantment(event.getPlayer().getHeldItemMainhand()) && event.getPlayer().getHeldItemMainhand().getItemDamage() >= event.getPlayer().getHeldItemMainhand().getMaxDamage() - 1)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getEntityPlayer().getHeldItemMainhand()) && event.getEntityPlayer().getHeldItemMainhand().getItemDamage() >= event.getEntityPlayer().getHeldItemMainhand().getMaxDamage() - 2) {
            event.damageRodBy(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getBow()) && event.getBow().getItemDamage() >= event.getBow().getMaxDamage() - 1)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getItemStack()) && (event.getItemStack().getItemDamage() >= event.getItemStack().getMaxDamage() - 1 && !(event.getItemStack().getItem() instanceof ItemArmor)))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && ((EntityPlayer) event.getEntityLiving()).isCreative())
            return;
        AtomicBoolean hasNeverBreakEnchant = new AtomicBoolean(false);
        event.getEntityLiving().getEquipmentAndArmor().forEach(stack -> {
            if (hasNeverBreakEnchantment(stack))
                hasNeverBreakEnchant.set(true);
        });
        if (hasNeverBreakEnchant.get()) {
            event.setCanceled(true);

            EntityLivingBase entity = event.getEntityLiving();
            DamageSource source = event.getSource();

            float damageAmount = event.getAmount();

            List<ItemStack> armourList = new ArrayList<>();

            // Apply armour reductions
            if (!source.isUnblockable()) {
                float totalArmourValue = 0.0F;
                float totalArmourToughness = 0.0F;
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (damageAmount > 0) {
                        float damage = damageAmount / 4.0F;
                        if (damage < 1.0F)
                            damage = 1.0F;

                        InventoryPlayer inventory = player.inventory;
                        for (int i = 0; i < inventory.armorInventory.size(); ++i) {
                            ItemStack stack = inventory.armorInventory.get(i);
                            if (stack.getItem() instanceof ItemArmor) {
                                if (!hasNeverBreakEnchantment(stack) || stack.getItemDamage() < stack.getMaxDamage() - damage) {
                                    stack.damageItem((int) damage, player);
                                    ItemArmor.ArmorMaterial armourMaterial = ((ItemArmor) stack.getItem()).getArmorMaterial();
                                    totalArmourValue += armourMaterial.getDamageReductionAmount(EntityEquipmentSlot.values()[i + 2]);
                                    totalArmourToughness += armourMaterial.getToughness();
                                    armourList.add(stack);
                                }
                            }
                        }
                    }
                }
                damageAmount = CombatRules.getDamageAfterAbsorb(damageAmount, totalArmourValue, totalArmourToughness);
            } else {
                Iterator<ItemStack> iterator = entity.getArmorInventoryList().iterator();
                float totalArmourValue = 0.0F;
                float totalArmourToughness = 0.0F;
                if (damageAmount > 0) {
                    float damage = damageAmount / 4.0F;
                    if (damage < 1.0F)
                        damage = 1.0F;

                    for (int i = 0; iterator.hasNext(); i++) {
                        ItemStack stack = iterator.next();
                        if (stack.getItem() instanceof ItemArmor) {
                            if (!hasNeverBreakEnchantment(stack) || stack.getItemDamage() < stack.getMaxDamage() - damage) {
                                stack.damageItem((int) damage, entity);
                                ItemArmor.ArmorMaterial armourMaterial = ((ItemArmor) stack.getItem()).getArmorMaterial();
                                totalArmourValue += armourMaterial.getDamageReductionAmount(EntityEquipmentSlot.values()[i + 2]);
                                totalArmourToughness += armourMaterial.getToughness();
                                armourList.add(stack);
                            }
                        }
                    }
                }
                damageAmount = CombatRules.getDamageAfterAbsorb(damageAmount, totalArmourValue, totalArmourToughness);
            }

            // Apply potion reductions

            if (!source.isDamageAbsolute()) {
                float damage = damageAmount;
                if (entity.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
                    int i = (entity.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    int j = 25 - i;
                    float f1 = damage * j;
                    damage = f1 / 25.0F;
                }

                if (damage <= 0.0F) {
                    damageAmount = 0.0F;
                } else {
                    int k = EnchantmentHelper.getEnchantmentModifierDamage(armourList, source);
                    if (k > 0) {
                        damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
                    }

                    damageAmount = damage;
                }
            }

            float f = damageAmount;
            damageAmount = Math.max(damageAmount - entity.getAbsorptionAmount(), 0.0F);
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (f - damageAmount));
            damageAmount = net.minecraftforge.common.ForgeHooks.onLivingDamage(entity, source, damageAmount);

            if (damageAmount != 0.0F)
            {
                float f1 = entity.getHealth();
                entity.getCombatTracker().trackDamage(source, f1, damageAmount);
                entity.setHealth(f1 - damageAmount); // Forge: moved to fix MC-121048
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() - damageAmount);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getItemStack()) && (event.getItemStack().getItemDamage() >= event.getItemStack().getMaxDamage() - 1 && !(event.getItemStack().getItem() instanceof ItemArmor)))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityRightClicked(PlayerInteractEvent.EntityInteract event) {
        if (!event.getEntityPlayer().isCreative() && hasNeverBreakEnchantment(event.getItemStack()) && (event.getItemStack().getItemDamage() >= event.getItemStack().getMaxDamage() - 1 && !(event.getItemStack().getItem() instanceof ItemArmor)))
            event.setCanceled(true);
    }
}
