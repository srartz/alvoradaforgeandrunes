package com.artz.alvoradaforge.progression;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.artz.alvoradaforge.forging.ForgeQuality;
import com.artz.alvoradaforge.registry.ModDataComponents;

/** Commands intended for administrators, command blocks and Custom NPCs quest rewards. */
public final class ProgressionCommands {
    private ProgressionCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("alvorada")
                .then(Commands.literal("progression")
                        .executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> show(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("reputation")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("faction", StringArgumentType.word())
                                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                                        .suggest(new String[]{"dwarves", "kobolds"}, builder))
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(-1000, 1000))
                                                        .executes(ProgressionCommands::addReputation))))))
                .then(Commands.literal("knowledge")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("knowledge", StringArgumentType.word())
                                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                                        .suggest(knowledgeNames(), builder))
                                                .executes(context -> changeKnowledge(context, true)))))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("knowledge", StringArgumentType.word())
                                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                                        .suggest(knowledgeNames(), builder))
                                                .executes(context -> changeKnowledge(context, false))))))
                .then(Commands.literal("check")
                        .then(Commands.literal("forge_quality")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("minimum", StringArgumentType.word())
                                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                                        .suggest(new String[]{"poor", "well", "expert", "perfect", "master"}, builder))
                                                .executes(ProgressionCommands::checkForgeQuality))))
                        .then(Commands.literal("rune_accuracy")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("minimum", IntegerArgumentType.integer(0, 100))
                                                .executes(ProgressionCommands::checkRuneAccuracy))))));
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        String knowledge = PlayerProgression.knowledge(player).stream()
                .map(Knowledge::serializedName)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
        source.sendSuccess(() -> Component.translatable(
                "command.alvoradaforge.progression",
                player.getDisplayName(),
                PlayerProgression.reputation(player, Faction.DWARVES),
                PlayerProgression.reputation(player, Faction.KOBOLDS),
                knowledge
        ), false);
        return 1;
    }

    private static int addReputation(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Faction faction;
        try {
            faction = Faction.fromName(StringArgumentType.getString(context, "faction"));
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.translatable("command.alvoradaforge.invalid_faction"));
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        for (ServerPlayer player : players) {
            PlayerProgression.addReputation(player, faction, amount);
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "command.alvoradaforge.reputation_changed", players.size(), faction.serializedName(), amount), true);
        return players.size();
    }

    private static int changeKnowledge(CommandContext<CommandSourceStack> context, boolean grant)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Knowledge knowledge;
        try {
            knowledge = Knowledge.fromName(StringArgumentType.getString(context, "knowledge"));
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.translatable("command.alvoradaforge.invalid_knowledge"));
            return 0;
        }
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        int changed = 0;
        for (ServerPlayer player : players) {
            if (grant ? PlayerProgression.grant(player, knowledge) : PlayerProgression.revoke(player, knowledge)) {
                changed++;
            }
        }
        int result = changed;
        context.getSource().sendSuccess(() -> Component.translatable(
                grant ? "command.alvoradaforge.knowledge_granted" : "command.alvoradaforge.knowledge_revoked",
                knowledge.serializedName(), result), true);
        return changed;
    }

    private static String[] knowledgeNames() {
        return java.util.Arrays.stream(Knowledge.values()).map(Knowledge::serializedName).toArray(String[]::new);
    }

    private static int checkForgeQuality(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ForgeQuality actual = player.getMainHandItem().get(ModDataComponents.FORGING_QUALITY.get());
        ForgeQuality minimum;
        try {
            minimum = ForgeQuality.fromName(StringArgumentType.getString(context, "minimum"));
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.translatable("command.alvoradaforge.invalid_quality"));
            return 0;
        }
        boolean passed = actual != null && actual.ordinal() >= minimum.ordinal();
        context.getSource().sendSuccess(() -> Component.translatable(
                passed ? "command.alvoradaforge.check_passed" : "command.alvoradaforge.check_failed"), false);
        return passed ? 1 : 0;
    }

    private static int checkRuneAccuracy(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        Integer accuracy = player.getMainHandItem().get(ModDataComponents.RUNE_ACCURACY.get());
        int minimum = IntegerArgumentType.getInteger(context, "minimum");
        boolean passed = accuracy != null && accuracy >= minimum;
        context.getSource().sendSuccess(() -> Component.translatable(
                passed ? "command.alvoradaforge.check_passed" : "command.alvoradaforge.check_failed"), false);
        return passed ? 1 : 0;
    }
}
