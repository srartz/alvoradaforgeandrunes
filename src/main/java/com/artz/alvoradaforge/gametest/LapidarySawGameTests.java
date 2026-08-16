package com.artz.alvoradaforge.gametest;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.entity.LapidarySawBlockEntity;
import com.artz.alvoradaforge.guide.GuideCommand;
import com.artz.alvoradaforge.registry.ModBlocks;
import com.artz.alvoradaforge.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AlvoradaForge.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LapidarySawGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
    private static final BlockPos SAW_POS = new BlockPos(1, 1, 1);

    private LapidarySawGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void cutsLeyStoneAndConsumesPhysicalSupplies(GameTestHelper helper) {
        LapidarySawBlockEntity saw = placeSaw(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        saw.interact(player, new ItemStack(ModItems.RAW_LEY_STONE.get()));
        saw.interact(player, new ItemStack(ModItems.QUARTZ_DUST.get()));
        saw.interact(player, new ItemStack(ModItems.IRON_SAW_BLADE.get()));
        saw.interact(player, new ItemStack(Items.WATER_BUCKET));

        helper.assertTrue(saw.isProcessing(), "A serra deveria iniciar com todos os quatro insumos");
        for (int tick = 0; tick < 300; tick++) {
            LapidarySawBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(SAW_POS),
                    helper.getBlockState(SAW_POS), saw);
        }

        helper.assertTrue(saw.hasResult(), "A lapidacao deveria produzir um resultado");
        helper.assertTrue(saw.getDisplayedItem(LapidarySawBlockEntity.RESULT_SLOT).is(ModItems.RUNE_STONE.get()),
                "Pedra-Ley deveria produzir Pedra Runica Vazia");
        helper.assertValueEqual(saw.getWaterUses(), 3, "Um dos quatro usos de agua deve ser consumido");
        helper.assertValueEqual(saw.getDisplayedItem(LapidarySawBlockEntity.BLADE_SLOT).getDamageValue(), 1,
                "A lamina de ferro deve perder uma unidade de durabilidade");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void ancientHeartRequiresDiamondBlade(GameTestHelper helper) {
        LapidarySawBlockEntity saw = placeSaw(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        saw.interact(player, new ItemStack(ModItems.ANCIENT_GEODE_HEART.get()));
        saw.interact(player, new ItemStack(ModItems.DIAMOND_DUST.get()));
        saw.interact(player, new ItemStack(ModItems.IRON_SAW_BLADE.get()));
        saw.interact(player, new ItemStack(Items.WATER_BUCKET));

        helper.assertFalse(saw.isProcessing(), "Lamina de ferro nao pode cortar o Coracao Ancestral");
        helper.assertTrue(saw.getDisplayedItem(LapidarySawBlockEntity.STONE_SLOT)
                        .is(ModItems.ANCIENT_GEODE_HEART.get()),
                "A tentativa invalida nao pode consumir o coracao");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void persistsTankInventoryAndProgress(GameTestHelper helper) {
        LapidarySawBlockEntity saw = placeSaw(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        saw.interact(player, new ItemStack(ModItems.RAW_LEY_STONE.get()));
        saw.interact(player, new ItemStack(ModItems.QUARTZ_DUST.get()));
        saw.interact(player, new ItemStack(ModItems.IRON_SAW_BLADE.get()));
        saw.interact(player, new ItemStack(Items.WATER_BUCKET));
        for (int tick = 0; tick < 40; tick++) {
            LapidarySawBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(SAW_POS),
                    helper.getBlockState(SAW_POS), saw);
        }

        CompoundTag saved = saw.saveWithoutMetadata(helper.getLevel().registryAccess());
        LapidarySawBlockEntity restored = new LapidarySawBlockEntity(
                helper.absolutePos(SAW_POS), ModBlocks.LAPIDARY_SAW.get().defaultBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());

        helper.assertTrue(restored.isProcessing(), "Processo ativo deve sobreviver ao NBT");
        helper.assertValueEqual(restored.getProgress(), 40, "Progresso restaurado");
        helper.assertValueEqual(restored.getWaterUses(), 4, "Reservatorio restaurado");
        helper.assertTrue(restored.getDisplayedItem(LapidarySawBlockEntity.BLADE_SLOT)
                        .is(ModItems.IRON_SAW_BLADE.get()),
                "Lamina restaurada");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void guideBookContainsTheCompleteManual(GameTestHelper helper) {
        ItemStack guide = GuideCommand.createGuide();
        WrittenBookContent content = guide.get(DataComponents.WRITTEN_BOOK_CONTENT);
        helper.assertTrue(guide.is(Items.WRITTEN_BOOK), "O guia deve ser um livro escrito");
        helper.assertTrue(content != null, "O guia deve possuir conteudo de livro");
        helper.assertTrue(content.pages().size() >= 17, "O guia deve explicar todos os sistemas principais");
        helper.assertValueEqual(content.title().raw(), "Compêndio da Alvorada", "Titulo do guia");
        helper.assertValueEqual(guide.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), true,
                "O guia deve possuir brilho para ser facil de identificar");
        helper.succeed();
    }

    private static LapidarySawBlockEntity placeSaw(GameTestHelper helper) {
        helper.setBlock(SAW_POS, ModBlocks.LAPIDARY_SAW.get());
        return helper.getBlockEntity(SAW_POS);
    }
}
