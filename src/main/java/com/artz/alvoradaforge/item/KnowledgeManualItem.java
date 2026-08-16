package com.artz.alvoradaforge.item;

import com.artz.alvoradaforge.progression.Knowledge;
import com.artz.alvoradaforge.progression.PlayerProgression;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Quest reward that permanently teaches one piece of faction knowledge. */
public final class KnowledgeManualItem extends Item {
    private final Knowledge knowledge;

    public KnowledgeManualItem(Properties properties, Knowledge knowledge) {
        super(properties);
        this.knowledge = knowledge;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (PlayerProgression.knows(player, knowledge)) {
            player.displayClientMessage(Component.translatable("message.alvoradaforge.knowledge_known")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!PlayerProgression.canLearn(player, knowledge)) {
            player.displayClientMessage(Component.translatable(
                    "message.alvoradaforge.reputation_required",
                    knowledge.requiredReputation(),
                    Component.translatable("faction.alvoradaforge." + knowledge.faction().serializedName())
            ).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        PlayerProgression.grant(player, knowledge);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(Component.translatable(
                "message.alvoradaforge.knowledge_learned",
                Component.translatable("knowledge.alvoradaforge." + knowledge.serializedName())
        ).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.alvoradaforge.quest_reward").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.manual_reputation",
                knowledge.requiredReputation(),
                Component.translatable("faction.alvoradaforge." + knowledge.faction().serializedName()))
                .withStyle(ChatFormatting.GRAY));
    }
}
