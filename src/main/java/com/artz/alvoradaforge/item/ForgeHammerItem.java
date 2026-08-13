package com.artz.alvoradaforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ForgeHammerItem extends Item {
    private final float precisionBonus;
    private final float controlMultiplier;
    private final int forgingPower;
    private final float speedupPerHit;
    private final float targetRelocationChance;

    public ForgeHammerItem(
            Properties properties,
            float precisionBonus,
            float controlMultiplier,
            int forgingPower,
            float speedupPerHit,
            float targetRelocationChance
    ) {
        super(properties);
        this.precisionBonus = precisionBonus;
        this.controlMultiplier = controlMultiplier;
        this.forgingPower = forgingPower;
        this.speedupPerHit = speedupPerHit;
        this.targetRelocationChance = targetRelocationChance;
    }

    public float precisionBonus() {
        return precisionBonus;
    }

    public float controlMultiplier() {
        return controlMultiplier;
    }

    public int forgingPower() {
        return forgingPower;
    }

    public float targetRelocationChance() {
        return targetRelocationChance;
    }

    public float speedupPerHit() {
        return speedupPerHit;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.alvoradaforge.hammer.precision", Math.round(precisionBonus * 100.0F))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.hammer.control", String.format("%.2f", controlMultiplier))
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.hammer.power", forgingPower)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.hammer.target_shift",
                        Math.round(targetRelocationChance * 100.0F))
                .withStyle(targetRelocationChance <= 0.10F ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.hammer.acceleration",
                        String.format("%.1f", speedupPerHit))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
