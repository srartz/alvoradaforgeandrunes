# Stonecrafting e integração da família Zéfiro

Este documento define o contrato da primeira entrega do Stonecrafting e o caminho seguro para integrar worldgen e a quinta família de runas no código atual (NeoForge 1.21.1).

## Lapidador de Bancada implementado

O `LapidarySawBlock` é uma estação sem menu. Toda interação que altera inventário, tanque ou progresso ocorre no servidor. O `LapidarySawBlockEntity` persiste e sincroniza:

- slot 0: Pedra-Ley Bruta ou Coração de Geode Ancestral;
- slot 1: item da tag `alvoradaforge:lapidary_abrasives`;
- slot 2: item da tag `alvoradaforge:lapidary_saw_blades`;
- slot 3: resultado;
- reservatório: 0 a 4 usos de água;
- estado do corte: ativo, progresso e duração necessária.

Fluxo de interação:

1. Clique com Pedra-Ley Bruta/Coração, abrasivo, lâmina e balde de água, em qualquer ordem.
2. O corte começa automaticamente quando a combinação é válida.
3. Pedra-Ley leva 300 ticks (15 s), consome 1 abrasivo, 1 uso de água e 1 de durabilidade da lâmina.
4. Coração Ancestral leva 600 ticks (30 s), exige a lâmina de diamante e consome 4 de durabilidade.
5. Clique no resultado para recolher. Agachar + clicar devolve os três insumos e cancela o progresso sem consumi-los.
6. Um balde abastece quatro cortes. Um balde vazio só recupera a água quando o reservatório ainda está completamente cheio; água parcialmente usada não pode ser recuperada.

O processamento emite som periódico de abrasão, respingos de refrigeração, som de conclusão e partículas diferentes para a pedra comum e ancestral. O renderer mostra pedra, abrasivo, lâmina girando e resultado diretamente no mundo.

A receita shapeless antiga de `rune_stone` foi removida. Os modelos atuais dos itens novos reutilizam modelos vanilla como placeholders; devem ser substituídos por texturas próprias antes da entrega visual final.

## Integração do minério ao worldgen

Os itens de processo já registrados são:

| ID | Função |
| --- | --- |
| `raw_ley_stone` | insumo comum do Lapidador |
| `ancient_geode_heart` | insumo ancestral raro |
| `quartz_dust` | abrasivo comum |
| `diamond_dust` | abrasivo superior |
| `iron_saw_blade` | 64 de durabilidade; não corta Coração Ancestral |
| `diamond_saw_blade` | 256 de durabilidade; habilita o corte ancestral |

Próxima etapa recomendada:

1. Registrar `ley_stone_ore` e `deepslate_ley_stone_ore` em `ModBlocks`, com seus `BlockItem` em `ModItems`.
2. Registrar `ancient_geode_core` como bloco separado. Ele deve dropar `ancient_geode_heart`; não registrar o coração como minério comum evita que Fortuna banalize o recurso lendário.
3. Criar loot tables: Pedra-Ley solta 1 `raw_ley_stone` com bônus de Fortuna; o núcleo ancestral solta exatamente 1 coração e ignora Fortuna. Toque Suave pode devolver o bloco em ambos os casos.
4. Criar configured/placed features e adicioná-las por biome modifier na etapa `underground_ores`.
5. Parâmetros iniciais para balanceamento de grind:
   - Pedra-Ley: deepslate entre Y -64 e -24, distribuição triangular, veios de 2–4, 3 tentativas por chunk;
   - núcleo ancestral: Y -64 a -40, tamanho 1, `rarity_filter` de 1/96 chunks e somente em biomas do Overworld permitidos por uma tag própria;
   - manter os números em configuração comum para que o SMP possa calibrar sem recompilar.
6. Adicionar GameTests para loot com/sem Fortuna e um teste estatístico separado para o placed feature. Worldgen novo só aparece em chunks ainda não gerados.

O Moinho de Pigmento deverá ser a fonte de `quartz_dust` e `diamond_dust`. Não devem ser criadas receitas shapeless de gema → pó, pois elas contornariam a estação física.

## Integração da quinta família: Zéfiro

### IDs estáveis

Adicionar `ZEPHYR(0xFFFFE36A)` a `RuneFamily`. As dez entradas devem ser anexadas ao fim de `RuneType`, preservando os ordinais 0–39 já gravados nos payloads e itens existentes:

| Tier | Enum/ID | Nome pt-BR | Papel principal |
| ---: | --- | --- | --- |
| 1 | `BREEZE` / `breeze` | Brisa | mobilidade leve |
| 2 | `GUST` / `gust` | Sopro | repulsão curta |
| 3 | `GALE` / `gale` | Ventania | velocidade de ataque |
| 4 | `THUNDER` / `thunder` | Trovão | primeiro proc elétrico |
| 5 | `ELECTRIC_CURRENT` / `electric_current` | Corrente | corrente para 2 alvos |
| 6 | `LIGHTNING` / `lightning` | Raio | corrente para 3 alvos |
| 7 | `CYCLONE` / `cyclone` | Ciclone | ataque e repulsão em área |
| 8 | `HURRICANE` / `hurricane` | Furacão | mobilidade ancestral e área |
| 9 | `MAGNETIC_STORM` / `magnetic_storm` | Tempestade Magnética | atração/repulsão e cadeia |
| 10 | `SUPERNOVA` / `supernova` | Supernova | descarga radial com cooldown |

Não usar `CURRENT`: esse enum e o ID `current_rune` já pertencem à família Maré. `ELECTRIC_CURRENT` evita colisão de registro e continua produzindo o nome de componente `electric_current`, compatível com o `RuneType.valueOf(...)` atual.

### Registros de item e tinta

O laço existente em `ModItems` registrará automaticamente os dez `RuneItem` após as entradas serem adicionadas ao enum. Acrescentar explicitamente apenas as tintas:

```java
public static final DeferredItem<RuneInkItem> ZEPHYR_INK =
        registerInk("zephyr_ink", RuneFamily.ZEPHYR, 7);
public static final DeferredItem<RuneInkItem> ANCESTRAL_ZEPHYR_INK =
        registerInk("ancestral_zephyr_ink", RuneFamily.ZEPHYR, 10);
```

Também é necessário:

- incluir as duas tintas na aba criativa;
- criar modelos/texturas para 10 runas e 2 tintas;
- adicionar `rune.alvoradaforge.*` e `item.alvoradaforge.*` em `pt_br` e `en_us`;
- mudar o GameTest de contagem de 40 para 50 e verificar dez runas em cada família;
- adicionar `case ZEPHYR -> zephyrPattern(type.tier())` ao switch exaustivo de `RunePatternValidator`;
- manter tiers 8–10 restritos à Pedra Rúnica Ancestral/Tinta Ancestral, aproveitando a validação já existente da Mesa Ancestral;
- decidir conscientemente se a roleta de `MysteryRuneStoneBlock` deve incluir Zéfiro. Hoje ela usa `RuneFamily.values()` e passará a incluir a família automaticamente.

### Efeitos próprios de vento e eletricidade

O `RuneType` atual modela apenas dois encantamentos vanilla e um efeito passivo. Isso não representa cadeia elétrica, cooldown ou repulsão de forma segura. A integração deve adicionar um perfil próprio, por exemplo `ZephyrEffect(chainTargets, chainDamage, knockback, attackSpeed, cooldownTicks)`, e um serviço `ZephyrRuneEvents` servidor-autoritativo.

O serviço deve:

1. Em `ItemAttributeModifierEvent`, adicionar modificadores com IDs estáveis para `ATTACK_SPEED` no item em mão e `MOVEMENT_SPEED` em armaduras apropriadas.
2. Em evento de dano pós-acerto, ler `ModDataComponents.RUNE_TYPE` da arma, validar família Zéfiro e procurar os alvos vivos mais próximos em uma AABB limitada.
3. Aplicar a cadeia com um `DamageType` próprio (`alvoradaforge:zephyr_chain`) e ignorar esse tipo no próprio listener, impedindo recursão infinita.
4. Guardar cooldown por jogador em attachment persistente/sincronizado para Ciclone, Tempestade Magnética e Supernova.
5. Escalar dano, alcance e chance pela precisão de inscrição (`RUNE_ACCURACY`), nunca confiar em dados enviados pelo cliente.
6. Limitar o número de alvos e ordenar por distância para manter custo previsível em SMP.

Faixa inicial sugerida: Brisa/Gale dão 4–10% de velocidade de ataque; Trovão causa 2 pontos elétricos; Corrente/Raio saltam para 2/3 alvos com perda de 20% por salto; Ciclone/Furacão trabalham com raio de 3/4 blocos; Supernova usa raio 6, no máximo 8 alvos e cooldown de 20 s. Esses valores devem ficar em configuração de servidor.

## Critérios de aceite da próxima etapa

- todos os 50 itens de runa têm IDs únicos e padrões válidos;
- nenhuma runa tier 8–10 pode ser criada na mesa comum;
- efeitos elétricos não executam no cliente e não recursam;
- cada proc possui limite de alvos e cooldown testados;
- Pedra-Ley e núcleos ancestrais aparecem apenas nas faixas/configurações definidas;
- a Pedra Rúnica Vazia e a Ancestral não possuem receita de bancada alternativa.
