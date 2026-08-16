# Progressão do SMP e integração com NPCs

O sistema de progressão não depende de um mod específico de NPC. Reputação e
conhecimentos ficam persistidos no jogador, enquanto comandos com retorno de
sucesso permitem que Custom NPCs, command blocks ou scripts de quests entreguem
recompensas e validem objetivos.

## Facções e conhecimentos

- `dwarves`: reputação dos Anões.
- `kobolds`: reputação dos Kobolds.
- `dwarven_tempering`: exige 10 de reputação Anã e libera forja de Netherita.
- `dwarven_masterwork`: exige 50 de reputação Anã e libera receitas de mestre.
- `kobold_advanced_runes`: exige 10 de reputação Kobold e libera runas 8–9.
- `kobold_legendary_runes`: exige 50 de reputação Kobold e libera runas 10.

Os manuais não possuem receita. Eles devem ser entregues como recompensa de
quest e são consumidos quando estudados. A tentativa falha caso a reputação seja
insuficiente.

## Comandos para quests

```text
/alvorada progression [jogador]
/alvorada reputation add <jogadores> <dwarves|kobolds> <quantidade>
/alvorada knowledge grant <jogadores> <conhecimento>
/alvorada knowledge revoke <jogadores> <conhecimento>
/alvorada check forge_quality <jogador> <poor|well|expert|perfect|master>
/alvorada check rune_accuracy <jogador> <0..100>
```

Os dois comandos `check` retornam `1` quando o item na mão principal atende ao
objetivo e `0` quando não atende. Isso permite exigir uma peça Perfeita/Obra-
Prima ou uma runa com pelo menos 90% de precisão antes de avançar uma quest.

## Conteúdo exclusivo de quests

- Lingote de Aço Mitril.
- Cabo de Martelo Reforçado.
- Pedra Rúnica Ancestral.
- Quatro Tintas Ancestrais.
- Quatro manuais de conhecimento.

As tintas normais permitem desenhar runas de nível 1–7 somente na mesa comum.
A Mesa Rúnica Ancestral exige Pedra Rúnica Ancestral, Pena Ancestral e Tinta
Ancestral, permitindo selecionar apenas os níveis 8–10. O servidor ainda exige
os respectivos conhecimentos Kobold. A precisão da inscrição é gravada na runa
e transferida ao equipamento quando aplicada.

A Mesa de Ruptura Rúnica usa um martelo integrado para abrir Pedras Rúnicas
Misteriosas. Ela sorteia qualquer uma das 40 runas, com apenas `0,0001%` de
chance total para nível 10.

## Requisitos em receitas de forja

Receitas de datapack e entradas completas de `forgeable_items.json` aceitam:

```json
"requirements": {
  "knowledge": "dwarven_masterwork",
  "faction": "dwarves",
  "reputation": 50
}
```

Receitas bloqueadas não aparecem como resultado na bigorna vanilla e não
iniciam na Bigorna de Forja até que o jogador cumpra os requisitos.
