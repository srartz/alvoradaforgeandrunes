package com.artz.alvoradaforge.gametest;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.registry.ModItems;
import com.artz.alvoradaforge.rune.RuneFamily;
import com.artz.alvoradaforge.rune.RunePatternValidator;
import com.artz.alvoradaforge.rune.RuneType;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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

    private static byte[] pack(RuneType type) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (RunePatternValidator.Point point : RunePatternValidator.pattern(type)) {
            output.write(point.x());
            output.write(point.y());
        }
        return output.toByteArray();
    }
}
