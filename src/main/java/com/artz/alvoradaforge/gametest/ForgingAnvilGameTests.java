package com.artz.alvoradaforge.gametest;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.entity.ForgingAnvilBlockEntity;
import com.artz.alvoradaforge.registry.ModBlocks;
import com.artz.alvoradaforge.registry.ModDataComponents;
import com.artz.alvoradaforge.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AlvoradaForge.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ForgingAnvilGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
    private static final BlockPos ANVIL_POS = new BlockPos(1, 1, 1);

    private ForgingAnvilGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void completesRecipeAndCollectsResult(GameTestHelper helper) {
        ForgingAnvilBlockEntity anvil = placeAnvil(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.giveExperienceLevels(30);

        ItemStack base = new ItemStack(Items.IRON_SWORD);
        ItemStack additions = new ItemStack(Items.DIAMOND, 2);
        anvil.insert(player, base);
        anvil.insert(player, additions);
        anvil.insert(player, additions);

        helper.assertTrue(anvil.isActive(), "A receita deveria iniciar depois da segunda unidade de diamante");
        helper.assertValueEqual(anvil.getRequiredHits(), 5, "Quantidade de golpes da receita");
        helper.assertValueEqual(anvil.getDisplayedItem(ForgingAnvilBlockEntity.ADDITION_SLOT).getCount(), 2,
                "Quantidade armazenada no slot de adicao");

        ItemStack hammerStack = new ItemStack(ModItems.NETHERITE_HAMMER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hammerStack);
        anvil.strike(player, hammerStack, ModItems.NETHERITE_HAMMER.get(), InteractionHand.MAIN_HAND);
        int progressAfterFirstHit = anvil.getProgress();
        anvil.strike(player, hammerStack, ModItems.NETHERITE_HAMMER.get(), InteractionHand.MAIN_HAND);
        helper.assertValueEqual(progressAfterFirstHit, 2, "Progresso apos o primeiro golpe");
        helper.assertValueEqual(anvil.getProgress(), 2, "Um segundo pacote no mesmo tick deve ser ignorado");

        helper.runAtTickTime(2, () -> anvil.strike(
                player, hammerStack, ModItems.NETHERITE_HAMMER.get(), InteractionHand.MAIN_HAND));
        helper.runAtTickTime(4, () -> {
            anvil.strike(player, hammerStack, ModItems.NETHERITE_HAMMER.get(), InteractionHand.MAIN_HAND);
            ItemStack result = anvil.getDisplayedItem(ForgingAnvilBlockEntity.RESULT_SLOT);
            helper.assertTrue(result.is(Items.DIAMOND_SWORD), "O resultado deveria ser uma espada de diamante");
            helper.assertTrue(result.has(ModDataComponents.FORGING_QUALITY.get()),
                    "O resultado deveria guardar a qualidade de forja");
            helper.assertFalse(anvil.isActive(), "A receita deve encerrar ao atingir o progresso necessario");
            helper.assertTrue(anvil.takeResult(player), "O resultado deveria poder ser retirado");
            helper.assertTrue(player.getInventory().contains(stack -> stack.is(Items.DIAMOND_SWORD)),
                    "A espada pronta deveria entrar no inventario do jogador");
            helper.assertFalse(anvil.hasResult(), "O slot de resultado deveria ficar vazio depois da retirada");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 10)
    public static void cancelReturnsInputs(GameTestHelper helper) {
        ForgingAnvilBlockEntity anvil = placeAnvil(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack base = new ItemStack(Items.IRON_SWORD);
        ItemStack addition = new ItemStack(Items.DIAMOND);

        anvil.insert(player, base);
        anvil.insert(player, addition);
        anvil.cancelAndReturn(player);

        helper.assertTrue(anvil.getDisplayedItem(ForgingAnvilBlockEntity.BASE_SLOT).isEmpty(),
                "O slot base deveria ficar vazio depois do cancelamento");
        helper.assertTrue(anvil.getDisplayedItem(ForgingAnvilBlockEntity.ADDITION_SLOT).isEmpty(),
                "O slot adicional deveria ficar vazio depois do cancelamento");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.IRON_SWORD)),
                "O item base deveria ser devolvido");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.DIAMOND)),
                "O material deveria ser devolvido");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 10)
    public static void persistsInventoryAndActiveRecipe(GameTestHelper helper) {
        ForgingAnvilBlockEntity anvil = placeAnvil(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        anvil.insert(player, new ItemStack(Items.IRON_SWORD));
        ItemStack additions = new ItemStack(Items.DIAMOND, 2);
        anvil.insert(player, additions);
        anvil.insert(player, additions);

        CompoundTag saved = anvil.saveWithoutMetadata(helper.getLevel().registryAccess());
        ForgingAnvilBlockEntity restored = new ForgingAnvilBlockEntity(
                helper.absolutePos(ANVIL_POS), ModBlocks.FORGING_ANVIL.get().defaultBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());

        helper.assertTrue(restored.isActive(), "A receita ativa deveria sobreviver ao carregamento NBT");
        helper.assertValueEqual(restored.getRequiredHits(), 5, "Golpes necessarios restaurados");
        helper.assertTrue(restored.getDisplayedItem(ForgingAnvilBlockEntity.BASE_SLOT).is(Items.IRON_SWORD),
                "Item base restaurado");
        helper.assertValueEqual(restored.getDisplayedItem(ForgingAnvilBlockEntity.ADDITION_SLOT).getCount(), 2,
                "Quantidade de material restaurada");
        helper.assertValueEqual(restored.getOwner(), player.getUUID(), "Dono da forja restaurado");
        helper.succeed();
    }

    private static ForgingAnvilBlockEntity placeAnvil(GameTestHelper helper) {
        helper.setBlock(ANVIL_POS, ModBlocks.FORGING_ANVIL.get());
        return helper.getBlockEntity(ANVIL_POS);
    }
}
