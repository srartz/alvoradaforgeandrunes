# Configuração externa e runas

## Itens de outros mods na Bigorna de Forja

Na primeira inicialização, o AlvoradaForge cria:

`config/alvoradaforge/forgeable_items.json`

Cada entrada de `forgeable_items` transforma o item-base (ou preserva o mesmo
item para melhorá-lo) usando um material. Depois de editar o arquivo, execute
`/reload`. Entradas que usam IDs de mods ausentes são ignoradas e registradas no
log sem impedir o carregamento das demais.

Para a forma mais simples, basta listar IDs. Nesse atalho, cada item usa duas
lascas de ametista, custa três níveis e recebe os valores padrão:

```json
{
  "format": 1,
  "forgeable_items": [
    "minecraft:diamond_sword",
    "outro_mod:martelo_de_guerra"
  ]
}
```

Para controlar material, custo e bônus, use objetos completos:

```json
{
  "format": 1,
  "forgeable_items": [
    {
      "enabled": true,
      "item": "mod_de_armas:espada_longa",
      "material": "mod_de_materiais:lingote_de_aco",
      "material_count": 3,
      "experience_cost": 8,
      "hammering": 7,
      "cycle_ticks": 36,
      "quality": "perfect",
      "benefits": ["durability", "weapon"],
      "priority": 100
    }
  ]
}
```

Use `result` quando a forja deve trocar o item por outro ID. Sem esse campo, o
resultado mantém o mesmo ID e copia nome, dano, encantamentos e componentes do
item-base. `bonuses` aceita os mesmos campos documentados em
[`FORGE_RECIPES.md`](FORGE_RECIPES.md).

## Mesa de Inscrição Rúnica

Coloque na mesa, com clique direito:

1. uma Pedra Rúnica Vazia;
2. uma pena;
3. uma das quatro tintas especiais.

Quando os três materiais estiverem presentes, a tela de inscrição abre. Use as
setas para escolher uma das dez runas da família da tinta, segure o botão
esquerdo do mouse e cubra o glifo claro com um único traço. O servidor valida
tamanho, comprimento, forma e distância média. Um pequeno desvio é aceito; uma
tentativa reprovada consome os três materiais. Agache e clique para recuperar
os ingredientes antes de desenhar.

Para aplicar uma runa pronta, segure-a em uma mão, segure a armadura ou
ferramenta na outra e use a runa. Cada item aceita uma runa. Existem 40 runas,
divididas em quatro famílias:

- Brasa: Brasa, Faísca, Cinza Ardente, Chama, Labareda, Magma, Inferno,
  Fênix, Fogo Solar e Cataclismo.
- Maré: Maré, Orvalho, Correnteza, Onda, Coral, Geada, Tempestade, Leviatã,
  Oceano e Redemoinho.
- Verdejante: Verdejante, Broto, Raiz, Cipó, Espinho, Florescer, Bosque,
  Carvalho Ancestral, Gaia e Árvore do Mundo.
- Vazio: Vazio, Sombra, Eco, Crepúsculo, Noite, Eclipse, Fenda,
  Esquecimento, Cosmos e Singularidade.

Cada nível possui desenho, encantamentos e efeito passivo próprios. Cataclismo,
Redemoinho, Árvore do Mundo e Singularidade são as runas lendárias de nível 10:
seus glifos têm mais segmentos, toleram menos desvio e concedem efeitos mais
fortes. O tooltip da runa mostra os bônus para armadura e ferramenta.

O encantamento precisa ser compatível com o item segundo as regras do próprio
Minecraft. Isso mantém o sistema seguro para ferramentas e armaduras de outros
mods.
