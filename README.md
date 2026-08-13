# AlvoradaForge

Mod de Minecraft criado para o Alvorada SMP. O projeto terá um sistema de forja
para criação de armas e armaduras, com runas capazes de adicionar efeitos e
modificadores aos equipamentos.

## Ambiente

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Gradle Wrapper incluído no repositório

## Estrutura

- `com.artz.alvoradaforge`: inicialização do mod.
- `registry`: registros de blocos, itens e demais conteúdos.
- `forging`: regras e serviços do sistema de forja.
- `rune`: tipos, efeitos e aplicação de runas.
- `equipment`: armas, armaduras e seus atributos.
- `client`: telas, renderização e integrações exclusivas do cliente.
- `data`: componentes persistentes e serialização.

Os pacotes dos sistemas serão adicionados conforme cada funcionalidade for
implementada, evitando classes vazias e dependências prematuras.

## Desenvolvimento

```powershell
.\gradlew.bat build
```

Para iniciar o cliente de desenvolvimento:

```powershell
.\gradlew.bat runClient
```

## Forja configurável

A Bigorna de Forja é um bloco físico sem interface. Durante o desenvolvimento,
use `/give @s alvoradaforge:forging_anvil`. Os ingredientes são colocados e
renderizados sobre ela com clique direito; depois, martelos craftáveis acionam
um minigame de precisão até a peça ficar pronta. As receitas aceitam itens e tags
de qualquer mod e são recarregadas com `/reload`.

O formato completo e os bônus de qualidade estão documentados em
[`docs/FORGE_RECIPES.md`](docs/FORGE_RECIPES.md).

## Configuração de servidor e runas

O mod cria automaticamente `config/alvoradaforge/forgeable_items.json`. O dono
do servidor pode adicionar IDs de itens e materiais de qualquer mod e aplicar a
mudança com `/reload`.

A Mesa de Inscrição Rúnica recebe uma pedra vazia, uma pena e uma tinta
especial. O jogador precisa copiar o glifo com o mouse; a forma é validada no
servidor antes da runa ser entregue. As quatro tintas dão acesso a 40 runas,
com dez níveis de complexidade por família. Consulte o fluxo completo em
[`docs/RUNES_AND_CONFIG.md`](docs/RUNES_AND_CONFIG.md).
