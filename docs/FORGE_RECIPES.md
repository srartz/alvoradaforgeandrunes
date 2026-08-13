# Receitas da Alvorada Forge

As receitas são arquivos JSON de datapack colocados em:

`data/<namespace>/alvorada_forge/<nome>.json`

Elas são recarregadas com `/reload`. Receitas que apontam para itens de mods não
instalados são ignoradas individualmente e aparecem como aviso no log.

Enquanto a estação temporária for a bigorna vanilla, cada receita aceita uma ou
duas entradas. A primeira entrada sempre precisa ter `count: 1`; a segunda pode
consumir várias unidades. Quando a bigorna 3D for adicionada, o gerenciador de
receitas poderá ser ligado ao inventário próprio do novo bloco.

## Exemplo

```json
{
  "priority": 10,
  "inputs": [
    {
      "item": "mod_de_materiais:lamina_aquecida",
      "count": 1
    },
    {
      "tag": "c:ingots/steel",
      "count": 3
    }
  ],
  "result": {
    "id": "mod_de_armas:espada_longa",
    "count": 1
  },
  "experience_cost": 8,
  "hammering": 7,
  "cycle_ticks": 36,
  "quality": "perfect",
  "benefits": ["durability", "weapon"],
  "copy_input_components": false,
  "anvil_damage_chance": 0.12,
  "bonuses": {
    "durability_multiplier": 1.75,
    "mining_speed_multiplier": 1.0,
    "attack_damage": 2.5,
    "attack_speed": 0.4,
    "armor": 0.0,
    "armor_toughness": 0.0
  }
}
```

## Campos

- `priority`: receitas maiores vencem quando mais de uma combinação corresponde.
- `inputs`: aceita ingredientes por `item`, `tag` ou alternativas no campo
  `ingredient`; nesta fase são permitidas uma ou duas entradas.
- `result.id` e `result.count`: item criado, inclusive de outro mod.
- `experience_cost`: níveis cobrados pela bigorna; o mínimo atual é 1 por uma
  limitação da bigorna vanilla. Na bigorna física, os níveis são cobrados apenas
  quando o último golpe termina o item.
- `hammering`: pontos de progresso necessários para concluir a peça. Martelos
  avançados podem produzir dois pontos por golpe.
- `cycle_ticks`: duração inicial do percurso completo da barra; ela acelera
  conforme a peça se aproxima da conclusão.
- `quality`: `poor`, `well`, `expert`, `perfect` ou `master`.
- `benefits`: `durability`, `tool`, `weapon`, `armor`, `auto` ou `none`.
- `copy_input_components`: copia nome, encantamentos e demais componentes da
  primeira entrada para o resultado.
- `anvil_damage_chance`: chance entre `0.0` e `1.0` de danificar a bigorna.
- `bonuses`: substitui os bônus padrão da qualidade apenas para esta receita.

Os bônus padrão seguem o sistema-base: qualidade altera durabilidade, velocidade
de mineração, dano/velocidade de ataque, armadura e resistência da armadura. Os
valores efetivos são gravados no item criado; assim eles continuam funcionando
no cliente e no servidor mesmo quando o resultado pertence a outro mod.

## Bigorna física e martelos

A Bigorna de Forja não possui interface. Obtenha-a durante o desenvolvimento com:

`/give @s alvoradaforge:forging_anvil`

Clique nela com cada ingrediente para deixá-lo visível sobre o bloco. Quando a
combinação estiver completa, equipe um martelo e acerte a bigorna quando o
marcador estiver no centro dourado da barra. A média da precisão determina a
qualidade final. Clique com a mão vazia para recolher o resultado. Agache e
clique com a mão vazia para cancelar e recuperar ingredientes.

Os martelos de cobre, ferro, ouro, diamante e netherita são feitos na bancada com
três unidades do material na linha superior e duas varetas no centro. Cada um
possui durabilidade, precisão, velocidade de controle e força próprias.
