package com.artz.alvoradaforge.gametest;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.registry.ModItems;
import com.artz.alvoradaforge.rune.RuneFamily;
import com.artz.alvoradaforge.rune.RunePatternValidator;
import com.artz.alvoradaforge.rune.RuneType;
import com.artz.alvoradaforge.registry.ModDataComponents;
import com.artz.alvoradaforge.rune.RuneService;
import com.artz.alvoradaforge.block.MysteryRuneStoneBlock;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import com.artz.alvoradaforge.registry.ModBlocks;
import com.artz.alvoradaforge.item.RuneItem;
import net.minecraft.world.item.Items;
import com.artz.alvoradaforge.block.entity.RuneTableBlockEntity;
import com.artz.alvoradaforge.block.entity.RuneBreakerTableBlockEntity;
import net.minecraft.server.level.ServerPlayer;

@GameTestHolder(AlvoradaForge.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RuneGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private RuneGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void registersFortyDistinctRunes(GameTestHelper helper) {
        helper.assertTrue(RuneType.values().length == 40, "Deveriam existir exatamente 40 tipos de runa");
        Set<String> itemIds = new HashSet<>();
        for (RuneType type : RuneType.values()) {
            String id = BuiltInRegistries.ITEM.getKey(ModItems.runeItem(type).get()).toString();
            helper.assertTrue(itemIds.add(id), "ID de item runico duplicado: " + id);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void acceptsExactPatternForEveryRune(GameTestHelper helper) {
        for (RuneType type : RuneType.values()) {
            RunePatternValidator.Result result = RunePatternValidator.validate(type, pack(type));
            helper.assertTrue(result.success(), "O molde exato deveria validar para " + type.serializedName());
            helper.assertTrue(result.accuracy() == 100, "O molde exato deveria ter 100% de precisao");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void legendaryRunesAreMostDifficult(GameTestHelper helper) {
        for (RuneFamily family : RuneFamily.values()) {
            RuneType first = family.runes().getFirst();
            RuneType legendary = family.runes().getLast();
            helper.assertTrue(legendary.tier() == 10, "A ultima runa da familia deve ser nivel 10");
            helper.assertTrue(legendary.passingDistance() < first.passingDistance(),
                    "A runa lendaria deve aceitar menos desvio");
            helper.assertTrue(RunePatternValidator.pattern(legendary).size()
                            > RunePatternValidator.pattern(first).size(),
                    "A runa lendaria deve possuir um desenho mais complexo");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void successfulInscriptionStoresAccuracy(GameTestHelper helper) {
        ItemStack rune = RuneService.createInscribedRune(RuneType.EMBER, 100);
        helper.assertTrue(rune.is(ModItems.EMBER_RUNE.get()), "O servico deve criar a runa solicitada");
        helper.assertValueEqual(rune.get(ModDataComponents.RUNE_ACCURACY.get()), 100,
                "A precisao deve ficar gravada na runa");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void mysteryStoneUsesWeightedRuneRoulette(GameTestHelper helper) {
        int[] tiers = new int[10];
        RandomSource random = RandomSource.create(0xA17A0ADAL);
        for (int roll = 0; roll < 50_000; roll++) {
            RuneType result = MysteryRuneStoneBlock.roll(random);
            tiers[result.tier() - 1]++;
        }
        helper.assertTrue(tiers[0] > tiers[4], "Runas de nivel um devem ser mais comuns que nivel cinco");
        helper.assertTrue(tiers[4] > tiers[9], "Runas de nivel cinco devem ser mais comuns que lendarias");
        helper.assertTrue(MysteryRuneStoneBlock.tierChance(10) == 0.0001,
                "A chance lendaria total deve ser de 0,0001 por cento");

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.MYSTERY_RUNE_STONE.get());
        List<ItemStack> wrongToolDrops = Block.getDrops(
                helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null, null,
                new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(wrongToolDrops.isEmpty(), "Ferramentas comuns nao devem abrir a pedra misteriosa");
        List<ItemStack> hammerDrops = Block.getDrops(
                helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null, null,
                new ItemStack(ModItems.IRON_HAMMER.get()));
        helper.assertTrue(hammerDrops.size() == 1 && hammerDrops.getFirst().getItem() instanceof RuneItem,
                "Um martelo de forja deve revelar exatamente uma runa");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void normalAndAncestralTablesRejectEachOthersMaterials(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos normalPos = new BlockPos(1, 1, 1);
        helper.setBlock(normalPos, ModBlocks.RUNE_TABLE.get());
        RuneTableBlockEntity normal = helper.getBlockEntity(normalPos);
        normal.interact(player, new ItemStack(ModItems.ANCESTRAL_RUNE_STONE.get()));
        normal.interact(player, new ItemStack(ModItems.ANCESTRAL_EMBER_INK.get()));
        helper.assertTrue(normal.getDisplayedItem(RuneTableBlockEntity.STONE_SLOT).isEmpty(),
                "Mesa comum deve recusar pedra ancestral");
        helper.assertTrue(normal.getDisplayedItem(RuneTableBlockEntity.INK_SLOT).isEmpty(),
                "Mesa comum deve recusar tinta ancestral");

        BlockPos ancestralPos = new BlockPos(3, 1, 1);
        helper.setBlock(ancestralPos, ModBlocks.ANCESTRAL_RUNE_TABLE.get());
        RuneTableBlockEntity ancestral = helper.getBlockEntity(ancestralPos);
        ancestral.interact(player, new ItemStack(ModItems.RUNE_STONE.get()));
        ancestral.interact(player, new ItemStack(Items.FEATHER));
        helper.assertTrue(ancestral.getDisplayedItem(RuneTableBlockEntity.STONE_SLOT).isEmpty(),
                "Mesa ancestral deve recusar pedra comum");
        helper.assertTrue(ancestral.getDisplayedItem(RuneTableBlockEntity.FEATHER_SLOT).isEmpty(),
                "Mesa ancestral deve recusar pena comum");
        ancestral.interact(player, new ItemStack(ModItems.ANCESTRAL_EMBER_INK.get()));
        ancestral.interact(player, new ItemStack(ModItems.ANCESTRAL_RUNE_STONE.get()));
        helper.assertFalse(ancestral.getDisplayedItem(RuneTableBlockEntity.INK_SLOT).isEmpty(),
                "Mesa ancestral deve aceitar tinta ancestral");
        helper.assertFalse(ancestral.getDisplayedItem(RuneTableBlockEntity.STONE_SLOT).isEmpty(),
                "Mesa ancestral deve aceitar pedra ancestral");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void breakerTableTurnsMysteryStoneIntoRune(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.RUNE_BREAKER_TABLE.get());
        RuneBreakerTableBlockEntity table = helper.getBlockEntity(pos);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        table.insertStone(player, new ItemStack(ModItems.MYSTERY_RUNE_STONE.get()));
        helper.assertTrue(table.hasStone(), "Pedra misteriosa deve ficar visivel na bancada");
        table.crack(player, ItemStack.EMPTY);
        helper.assertFalse(table.hasStone(), "Pedra deve ser consumida pela ruptura");
        helper.assertTrue(table.hasResult(), "Ruptura deve produzir uma runa");
        helper.assertTrue(table.getDisplayedItem(RuneBreakerTableBlockEntity.RESULT_SLOT).getItem() instanceof RuneItem,
                "Resultado da ruptura deve ser um item de runa");
        helper.succeed();
    }

    private static byte[] pack(RuneType type) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (RunePatternValidator.Point point : RunePatternValidator.pattern(type)) {
            output.write(point.x());
            output.write(point.y());
        }
        return output.toByteArray();
    }
}
