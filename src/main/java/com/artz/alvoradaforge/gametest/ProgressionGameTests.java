package com.artz.alvoradaforge.gametest;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.progression.Faction;
import com.artz.alvoradaforge.progression.Knowledge;
import com.artz.alvoradaforge.progression.PlayerProgression;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.artz.alvoradaforge.forging.ForgeRecipe;
import com.artz.alvoradaforge.forging.ForgeRecipeManager;

@GameTestHolder(AlvoradaForge.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProgressionGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private ProgressionGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void reputationUnlocksFactionKnowledge(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertFalse(PlayerProgression.canLearn(player, Knowledge.DWARVEN_MASTERWORK),
                "Conhecimento de mestre deve exigir reputacao");
        PlayerProgression.addReputation(player, Faction.DWARVES, 50);
        helper.assertTrue(PlayerProgression.canLearn(player, Knowledge.DWARVEN_MASTERWORK),
                "Cinquenta pontos devem liberar o estudo de mestre");
        helper.assertTrue(PlayerProgression.grant(player, Knowledge.DWARVEN_MASTERWORK),
                "O primeiro estudo deve conceder conhecimento");
        helper.assertTrue(PlayerProgression.knows(player, Knowledge.DWARVEN_MASTERWORK),
                "Conhecimento concedido deve persistir no jogador");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void advancedRunesRequireKoboldKnowledge(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(PlayerProgression.canDrawRune(player, 7), "Runas comuns devem ser autonomas");
        helper.assertFalse(PlayerProgression.canDrawRune(player, 8), "Nivel oito deve exigir manual avancado");
        helper.assertFalse(PlayerProgression.canDrawRune(player, 10), "Nivel dez deve exigir codice lendario");
        PlayerProgression.grant(player, Knowledge.KOBOLD_ADVANCED_RUNES);
        helper.assertTrue(PlayerProgression.canDrawRune(player, 9), "Manual avancado deve liberar niveis oito e nove");
        helper.assertFalse(PlayerProgression.canDrawRune(player, 10), "Nivel dez continua bloqueado");
        PlayerProgression.grant(player, Knowledge.KOBOLD_LEGENDARY_RUNES);
        helper.assertTrue(PlayerProgression.canDrawRune(player, 10), "Codice lendario deve liberar nivel dez");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
    public static void forgeRecipeHonorsDwarvenRequirements(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ForgeRecipe recipe = ForgeRecipeManager.INSTANCE.byId("alvoradaforge:netherite_sword")
                .orElseThrow();
        helper.assertFalse(recipe.canUse(player), "Receita de netherita deve iniciar bloqueada");
        PlayerProgression.addReputation(player, Faction.DWARVES, 10);
        helper.assertFalse(recipe.canUse(player), "Reputacao sem conhecimento nao deve bastar");
        PlayerProgression.grant(player, Knowledge.DWARVEN_TEMPERING);
        helper.assertTrue(recipe.canUse(player), "Reputacao e conhecimento devem liberar a receita");
        helper.succeed();
    }
}
