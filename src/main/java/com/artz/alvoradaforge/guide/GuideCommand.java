package com.artz.alvoradaforge.guide;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class GuideCommand {
    private GuideCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("alvorada")
                .then(Commands.literal("guide")
                        .executes(context -> giveGuide(context.getSource().getPlayerOrException()))));
    }

    private static int giveGuide(ServerPlayer player) {
        ItemStack guide = createGuide();
        if (!player.addItem(guide)) {
            Block.popResource(player.level(), player.blockPosition(), guide);
        }
        player.displayClientMessage(Component.translatable("command.alvoradaforge.guide_received")
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    public static ItemStack createGuide() {
        ItemStack guide = new ItemStack(Items.WRITTEN_BOOK);
        guide.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("Compêndio da Alvorada"),
                "Mestres da Forja",
                0,
                pages(),
                true
        ));
        guide.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return guide;
    }

    private static List<Filterable<Component>> pages() {
        return List.of(
                page("COMPÊNDIO DA ALVORADA",
                        "Forja, lapidação e runas sem menus.\n\nEste livro explica estações, receitas e como usar cada material.\n\nRecupere outra cópia com:\n/alvorada guide"),
                page("LAPIDADOR",
                        "Receita da bancada:\n\n  I\nCSC\nSSS\n\nI = Lingote de Ferro\nC = Lingote de Cobre\nS = Pedra Lisa\n\nA estação transforma pedra bruta em bases para runas."),
                page("INSUMOS DE LAPIDAÇÃO",
                        "Pedra-Ley Bruta:\nOrigem mineral profunda. Produz Pedra Rúnica Vazia.\n\nCoração de Geode Ancestral:\nRelíquia extremamente rara. Produz Pedra Rúnica Ancestral e exige lâmina de diamante."),
                page("ABRASIVOS",
                        "Pó de Quartzo:\nAbrasivo comum para cortes regulares.\n\nPó de Diamante:\nAbrasivo superior, com brilho e faíscas próprias.\n\nCada corte consome uma unidade. No Eco-Forge, os pós serão preparados no Moinho de Pigmento."),
                page("LÂMINA DE FERRO",
                        "Receita:\nIII\n I \nIII\n\nI = Lingote de Ferro\n\nDurabilidade: 64 cortes comuns.\n\nNão consegue suportar a energia de um Coração de Geode Ancestral."),
                page("LÂMINA DE DIAMANTE",
                        "Receita:\nDDD\n I \nDDD\n\nD = Diamante\nI = Lingote de Ferro\n\nDurabilidade: 256.\nUm corte ancestral desgasta 4 pontos."),
                page("COMO LAPIDAR",
                        "1. Insira a pedra.\n2. Insira um abrasivo.\n3. Instale uma lâmina.\n4. Abasteça com balde de água.\n5. Aguarde o corte.\n6. Clique para recolher.\n\nAgache + clique para cancelar e devolver os insumos."),
                page("REFRIGERAÇÃO",
                        "Um balde fornece 4 usos. Cada corte seca uma carga.\n\nPedra-Ley: 15 segundos e 1 desgaste.\n\nCoração Ancestral: 30 segundos e 4 desgastes.\n\nUm reservatório só pode ser drenado se ainda estiver completamente cheio."),
                page("BIGORNA DE FORJA",
                        "Receita:\nIII\nABA\nIII\n\nI = Lingote de Ferro\nA = Bigorna\nB = Alto-forno\n\nColoque os materiais e golpeie com um Martelo de Forja quando a barra alcançar o alvo."),
                page("QUALIDADE DA FORJA",
                        "A precisão produz:\n• Mal forjado\n• Bem forjado\n• Especializada\n• Perfeita\n• Obra-prima\n\nA qualidade fica salva no item e altera seus atributos. Golpes mais centrais produzem resultados melhores."),
                page("MARTELOS",
                        "Todos usam o formato:\nMMM\n S \n S \n\nM = Cobre, Ferro, Ouro, Diamante ou Netherita\nS = Graveto\n\nMateriais superiores melhoram controle, precisão, poder e durabilidade de maneiras diferentes."),
                page("MESA RÚNICA",
                        "Receita:\nADA\nOEO\nO O\n\nA = Ametista\nD = Ardósia polida\nO = Tábuas de Carvalho Escuro\nE = Mesa de Encantamentos\n\nUse Pedra Rúnica, pena e tinta. Desenhe o glifo em um único traço."),
                page("TINTAS RÚNICAS",
                        "Brasa: tinta + vermelho + pó luminoso.\nMaré: tinta + azul + bolsa de tinta brilhante.\nVerdejante: tinta + verde + bagas luminosas.\nVazio: tinta + roxo + pérola do End.\n\nCada receita produz 2 tintas e define a família do glifo."),
                page("RUNAS ANCESTRAIS",
                        "Runas de nível 8–10 exigem Pedra Rúnica Ancestral, Pena Ancestral, Tinta Ancestral e Mesa Ancestral.\n\nMesa Ancestral:\nGAG / OTO / GAG\nG ouro, A pedra ancestral, O obsidiana chorona, T mesa rúnica."),
                page("RUPTURA RÚNICA",
                        "A Mesa de Ruptura abre Pedras Rúnicas Misteriosas com um Martelo de Forja.\n\nReceita:\nAMA / OSO / O O\nA ametista, M martelo de ferro, O obsidiana, S mesa de ferraria.\n\nRunas lendárias são extremamente raras."),
                page("ESTUDO E PROGRESSÃO",
                        "Algumas receitas e runas exigem reputação e manuais dos Anões ou Kobolds.\n\nLeia os manuais com atenção: o conhecimento adquirido fica registrado no jogador.\n\nUse /alvorada progression para consultar seu avanço."),
                page("DICAS DO MESTRE",
                        "• Água, abrasivo e lâmina são recursos reais.\n• Não quebre uma estação com itens dentro.\n• Precisão vale mais que velocidade.\n• Tier 8–10 usa materiais ancestrais.\n• Todos os resultados importantes são validados pelo servidor.")
        );
    }

    private static Filterable<Component> page(String title, String body) {
        Component content = Component.literal(title)
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE)
                .append(Component.literal("\n\n" + body).withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
        return Filterable.passThrough(content);
    }
}
