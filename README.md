# Sistema de Gestão de Pedidos — E-commerce

> **Projeto Integrador** da Unidade Curricular: *Desenvolvimento Back-end*
>
> **Curso:** Superior de Tecnologia em Análise e Desenvolvimento de Sistemas (Turma CSTADS601)
>
> **Instituição:** Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"

---

### Equipe / Squad: Swift

| Nome | Papel |
| :--- | :--- |
| **Lucas Nunes Patracão** | Responsável do dia |
| **Rafael Rubiá Oliveira Cardoso** | Desenvolvedor / Colaborador |

---

### Descrição do Desafio

O objetivo principal deste projeto é conceber e implementar uma solução completa de **Gestão de Pedidos para E-commerce**, contemplando Back-end, API REST e uma interface Front-end integrada.

A aplicação engloba a modelagem de domínio, regras de negócio e infraestrutura para administrar o cadastro de clientes e catálogo de produtos, gerenciar o ciclo de vida dos pedidos e processar transações financeiras com múltiplos métodos de pagamento.

A solução também possui uma interface administrativa para consumo e apresentação dos dados disponibilizados pela API, mantendo o **Back-end como fonte de verdade do sistema**.

---

### Funcionalidades Previstas

* **Gerenciamento de Produtos:** Cadastro, atualização, consulta e controle de disponibilidade.
* **Gerenciamento de Clientes:** Cadastro, edição de dados cadastrais e histórico de compras.
* **Gestão de Pedidos:** Abertura, inclusão de itens, cálculo de totais, alteração de status e cancelamento.
* **Processamento de Pagamentos:** Suporte a múltiplos métodos (Cartão de Crédito, Boleto Bancário, Pix e Vale Compras), com arquitetura polimórfica.
* **Dashboard:** Visualização de informações gerais do sistema, pedidos recentes, clientes, produtos e indicadores disponíveis.
* **Interface Administrativa:** Interface web para consulta e gerenciamento das informações do sistema.
* **Integração Front-end / Back-end:** Consumo dos endpoints reais da API REST pelo Front-end.
* **Garantia de Qualidade:** Testes automatizados unitários e de integração com relatórios de cobertura.
* **Integração Contínua (CI/CD):** Pipeline para execução de build e testes automatizados.
* **API RESTful:** Endpoints estruturados para integração e consumo pela aplicação Front-end e futuras aplicações.

---

### Pagamento Polimórfico e Princípio Aberto/Fechado

A camada de pagamentos foi estruturada com um contrato comum para desacoplar o pedido da implementação concreta de cada meio de pagamento.

- `ProcessadorPagamento` define o contrato mínimo para processar o valor e expor comprovante/descrição.
- `FormaPagamento` atua como base comum da hierarquia e mantém os atributos compartilhados.
- `Pix`, `Boleto`, `CartaoCredito` e `ValeCompras` implementam o processamento específico de cada modalidade.
- `Pedido` recebe qualquer `ProcessadorPagamento` em vez de depender de tipos concretos, reduzindo acoplamento e facilitando extensão.

Esse desenho permite adicionar novas formas de pagamento sem alterar a lógica central do pedido, respeitando o princípio aberto/fechado (OCP).

---

### Tecnologias

* **Linguagem:** Java 17 (versão LTS)
* **Gerenciador de Build e Dependências:** Apache Maven
* **Framework Web:** Spring Boot
* **API:** REST
* **Testes:** JUnit 5
* **Front-end:** React
* **Build / Desenvolvimento Front-end:** Vite
* **Controle de Versão e Colaboração:** Git & GitHub
* **Banco de Dados:** MySQL 8 via JDBC (`mysql-connector-j`), com migrations versionadas
* **CI/CD:** GitHub Actions — previsto no roadmap

---

### Regras de Negócio e Constantes (Aula 03)

Foram definidas e documentadas as seguintes regras e limites financeiros para o funcionamento da aplicação:

| Constante | Valor Adotado | Regra de Negócio |
| :--- | :--- | :--- |
| `VALOR_POR_QUILO` | R$ 7,50 | Preço cobrado por quilo (ou fração) transportado. |
| `FRETE_MINIMO` | R$ 15,00 | Valor mínimo de frete, independente do peso total do carrinho. |
| `VALOR_FRETE_GRATIS` | R$ 300,00 | A partir deste subtotal, o valor do frete é zerado. |
| `TAXA_DESCONTO` | 10% (0.10) | Percentual de desconto padrão aplicado sobre o subtotal da compra. |
| `DESCONTO_MAXIMO` | R$ 50,00 | Teto máximo de desconto concedido por pedido. |

---

### Estado Atual da Integração

O projeto encontra-se com o **Front-end conectado à API real do Back-end**, sem utilização de dados mockados no fluxo principal da aplicação.

O Back-end é executado utilizando Spring Boot e disponibiliza a API através de:

```text
http://localhost:8080/api
```

O Front-end utiliza essa API como fonte de dados para as principais áreas do sistema:

* Clientes
* Produtos
* Pedidos
* Pagamentos
* Histórico de compras
* Dashboard

O estado da interface é determinado pelos dados efetivamente retornados pela API.

Quando não existem registros, a interface apresenta estados vazios, por exemplo:

```text
Nenhum cliente cadastrado ainda.
Nenhum produto cadastrado ainda.
Nenhum pedido registrado ainda.
```

Da mesma forma, falhas de comunicação com a API são apresentadas ao usuário por meio de mensagens de erro amigáveis.

Não são utilizados registros fictícios apenas para preencher tabelas, cards, indicadores ou outras áreas da interface.

---

### Front-end

O Front-end foi estruturado como uma interface administrativa para gerenciamento do sistema de e-commerce.

A interface possui:

* Dashboard
* Gestão de Clientes
* Gestão de Produtos
* Gestão de Pedidos
* Pagamentos
* Histórico de compras
* Navegação lateral
* Busca
* Estados de carregamento
* Estados vazios
* Mensagens de erro
* Atualização dos dados

A identidade visual utiliza como base o **laranja associado à marca Swift**, combinado com branco, preto e tons neutros.

A proposta visual prioriza:

* Interface limpa e profissional.
* Hierarquia visual clara.
* Cards e tabelas organizados.
* Uso consistente da cor principal.
* Feedback visual para sucesso, erro e estados do sistema.
* Responsividade e organização dos componentes.
* Ausência de dados simulados na aplicação real.

O Front-end não deve criar ou inventar informações que não tenham sido retornadas pela API.

---

### Dívidas Técnicas Registradas

1. **Precisão de Valores Monetários (`double` vs `BigDecimal`):** Os valores monetários de subtotal, frete e desconto foram inicialmente implementados utilizando `double` para fins didáticos. A utilização de `BigDecimal` permanece como referência para a evolução da camada financeira, evitando inconsistências de arredondamento em operações monetárias.

2. **Persistência:** Implementada com MySQL/JDBC e padrão Repository (Aula 13). Os registros de pagamento (`FormaPagamentoRepository`) ainda ficam em memória.

3. **Autenticação e Autorização:** O projeto ainda não possui um mecanismo completo de autenticação e autorização de usuários.

4. **Fluxo Completo de Pedidos:** A integração visual e a comunicação com a API já estão estruturadas, porém o ciclo completo de criação, edição, inclusão de itens, pagamento e alteração de status continuará sendo desenvolvido conforme as próximas etapas do projeto.

---

### Relacionamentos (Aula 07)

| Relacionamento | Tipo | Multiplicidade | Regra no código |
| :--- | :--- | :--- | :--- |
| `Pedido` → `Cliente` | Associação | `1 : 1` | Todo pedido exige um cliente no construtor. |
| `Pedido` → `ItemPedido` | Composição | `1 : 0..*` | O pedido cria os itens e expõe somente uma lista imutável. |
| `ItemPedido` → `Produto` | Associação | `1 : 1` | O produto é obrigatório e fica final no item. |
| `Pedido` → `FormaPagamento` | Associação opcional | `1 : 0..1` | O pagamento só é guardado após processamento aprovado. |
| `Categoria` → `Produto` | Agregação | `1 : 0..*` | A categoria mantém produtos, mas não os cria nem destrói. |
| `Cliente` → `Endereco` | Associação opcional | `1 : 0..1` | O endereço pode ser nulo e é validado quando informado. |

Itens repetidos do mesmo produto são consolidados em um único `ItemPedido`. A nova quantidade é somada, o estoque é baixado somente pela quantidade adicional e o preço praticado permanece o da primeira inclusão. Essa decisão evita linhas duplicadas no pedido e preserva o preço histórico da transação.

**Status da Aula 07:** concluída na branch `feature/relacionamentos`, com composição, associação, agregação, controle de estoque, cancelamento e validações demonstrados em `Aplicacao.java`.

---

### CRUD Completo, Integridade Referencial e Transações (Aula 13)

A persistência saiu da memória e passou para o **MySQL 8**, usando **JDBC puro** (`mysql-connector-j` + `ConexaoFactory`), a mesma biblioteca já adotada na Aula 12. Nenhum ORM foi adicionado.

> **Adaptação registrada:** o enunciado da tarefa descrevia um backend em Swift. O backend real deste repositório é **Java 17 + Spring Boot** ("swift" é o nome da squad). Os conceitos foram aplicados de forma idiomática em Java/JDBC: o "enum de erro que conforma `Error`" virou `RepositorioException` com um `enum Tipo`; os "protocolos" viraram `interface`s; a transação usa `setAutoCommit(false)` com `commit`/`rollback` explícitos.

#### Visão geral das funcionalidades

| Entidade | Create | Read | Update | Delete |
| :--- | :--- | :--- | :--- | :--- |
| Produto | ✅ | ✅ ativos por padrão, inativos sob pedido, busca por nome, paginação | ✅ completo (com versão otimista) e parcial (só estoque) | Soft delete (inativar/reativar). Exclusão definitiva só se nunca foi vendido |
| Cliente | ✅ | ✅ ativos por padrão, por CPF, por e-mail, paginação | ✅ | Exclusão definitiva (bloqueada se tiver pedidos) + soft delete |
| Pedido | ✅ finalização transacional | ✅ por número, por cliente, por situação, paginação | ✅ situação (pagar / cancelar) | Só pedido `ABERTO`. Pago/cancelado nunca é excluído |
| ItemPedido | ✅ junto com o pedido | ✅ junto com o pedido | — (o preço é histórico) | ✅ remover item de pedido aberto (devolve o estoque) |

Regras aplicadas em **todo** UPDATE/DELETE:

- Sempre com `WHERE id = ?` e parâmetros (`PreparedStatement`). Nunca há concatenação de valores no SQL.
- Objeto sem `id` não pode ser atualizado nem removido: lança `RepositorioException` do tipo `VALIDACAO`.
- As **linhas afetadas são verificadas**. Zero linhas não é erro para o banco (o comando rodou, só não encontrou nenhuma linha); é a aplicação que decide que isso significa "não existe". Por isso vira `RepositorioException.naoEncontrado(...)` → **HTTP 404**, em vez de ser ignorado.
- Erros do MySQL nunca sobem crus: `TradutorErroSql` converte **1451 / SQLSTATE 23000** (linha referenciada por FK) em mensagem de negócio, sem citar tabela nem constraint. Também traduz 1062 (duplicado) e 1452 (referência inexistente) → **HTTP 409**.

#### Políticas de exclusão (integridade referencial)

Definidas na migration `V2__politicas_integridade_referencial.sql`:

| Relacionamento | Política | Justificativa de negócio |
| :--- | :--- | :--- |
| Pedido → ItemPedido | `ON DELETE CASCADE` | Composição: o item não existe sem o pedido. Excluir um pedido (em aberto) leva os itens junto. |
| Cliente → Pedido | `ON DELETE RESTRICT` + soft delete do cliente | O histórico de vendas não pode sumir com o cadastro. Cliente com pedidos é **inativado**, não excluído. |
| Produto → ItemPedido | `ON DELETE RESTRICT` + soft delete do produto | Pedido antigo precisa do produto vendido. O `DELETE` da API **inativa** o produto. |
| Pedido pago/cancelado | Regra na camada de serviço | Pedido pago é **cancelado** (a situação muda e o estoque volta), nunca excluído. |

Respostas às situações da aula:

| Situação | O que acontece |
| :--- | :--- |
| Excluir pedido com itens | Permitido só para pedido `ABERTO`: os itens saem em cascata e o estoque reservado é devolvido, na mesma transação. |
| Excluir cliente com pedidos | **409**: "Este cliente possui pedidos e não pode ser excluído. Inative o cadastro para preservar o histórico de compras." |
| Excluir produto que está em pedidos | `DELETE /api/produtos/{codigo}` inativa (204). Com `?definitivo=true` → **409**: "Este produto já foi vendido e não pode ser excluído. Inative-o para retirá-lo do catálogo." |
| Excluir produto nunca vendido | `DELETE /api/produtos/{codigo}?definitivo=true` remove de fato (204). |
| Excluir pedido já pago | **409**: "Pedido já pago não pode ser excluído. Cancele o pedido para preservar o histórico de vendas." Use `POST /api/pedidos/{numero}/cancelar`. |

#### Soft delete e filtro nas consultas

- A migration `V3__soft_delete.sql` adiciona `ativo BOOLEAN NOT NULL DEFAULT TRUE` em `produto` e `cliente`.
- `inativar(id)` e `reativar(id)` existem nos repositórios e serviços.
- **Todas** as consultas de produto e cliente filtram `ativo = TRUE` por padrão. O filtro é centralizado: cada repositório monta seus SELECTs por um único método (`select(Visibilidade, condicao)`), e o enum `Visibilidade` (`ATIVOS`, `INATIVOS`, `TODOS`) gera a condição. Não dá para escrever uma consulta "esquecendo" o filtro.
- Buscar inativos é **explícito**: `?situacao=inativos|todos` na API, ou `Visibilidade.INATIVOS/TODOS` no código.
- Pedidos carregam cliente e produtos **sem** o filtro, de propósito: um pedido antigo continua exibindo o produto vendido e o cliente, mesmo inativos.
- O preço cobrado fica gravado em `item_pedido.preco_unitario`. Se o produto mudar de preço ou for inativado, o pedido antigo continua mostrando o valor da época (coberto por teste).
- **Adaptação:** o campo `ativo` do domínio já existia e é exibido no front como "Disponível". Agora ele é o próprio soft delete: produto cadastrado ou editado com "disponível" desmarcado sai da listagem padrão (aparece em `?situacao=inativos`).

#### Finalização do pedido (transação)

`PedidoService.criarPedido` é atômico, **tudo ou nada**:

```text
BEGIN (setAutoCommit(false))
  busca cliente ativo e produtos ativos
  monta o Pedido no domínio (valida, consolida itens, congela o preço)
  INSERT pedido
  para cada item:
      INSERT item_pedido
      UPDATE produto SET quantidade_em_estoque = quantidade_em_estoque - ?
             WHERE id = ? AND ativo = TRUE AND quantidade_em_estoque >= ?
      0 linhas afetadas → EstoqueInsuficienteException (antes do commit)
COMMIT                         ← qualquer exceção acima → ROLLBACK completo
```

- `GerenciadorTransacao` (interface) / `JdbcGerenciadorTransacao` (implementação) abre **uma** conexão, repassa **a mesma** para todos os repositórios (primeiro parâmetro de todo método), faz `commit` ou `rollback` e sempre fecha a conexão, inclusive no caminho de falha.
- A baixa de estoque é condicional no próprio `UPDATE`, então duas vendas simultâneas não deixam o estoque negativo (evita condição de corrida). A mensagem de estoque insuficiente relê o estoque com `SELECT ... FOR UPDATE`, para mostrar o valor realmente disponível naquele momento.
- Cancelamento, pagamento, exclusão de pedido e remoção de item também são transacionais (situação e estoque juntos).

#### Estrutura de repositórios

```text
repositorio/                          ← contratos (serviços dependem só disto)
├── ProdutoRepository                 salvar, atualizar, atualizarEstoque, baixarEstoque, devolverEstoque,
│                                     inativar, reativar, remover, buscarPorId, buscarPorIdParaAtualizacao,
│                                     buscarPorCodigo, listarTodos, listar(Visibilidade), buscarPorNome, contar
├── ClienteRepository                 salvar, atualizar, remover, inativar, reativar, buscarPorId,
│                                     buscarPorCpf, buscarPorEmail, listarTodos, listar, contar
├── PedidoRepository                  salvar, atualizarSituacao, remover, buscarPorId, buscarPorNumero,
│                                     listarTodos, listarPorCliente, listarPorSituacao, contar
├── ItemPedidoRepository              inserir, listarPorPedido, remover, contarPorPedido
├── GerenciadorTransacao              executarEmTransacao / executar
├── RepositorioException (+ Tipo)     erro de persistência tipado
├── Pagina, Visibilidade              paginação (LIMIT/OFFSET) e filtro de soft delete
└── mysql/                            ← implementações (só o PersistenciaConfig as conhece)
    ├── RepositorioJdbcBase           PreparedStatement, linhas afetadas, tradução de erros
    ├── ProdutoRepositoryMySql, ClienteRepositoryMySql, PedidoRepositoryMySql, ItemPedidoRepositoryMySql
    ├── JdbcGerenciadorTransacao, TradutorErroSql, Mapeadores
    └── ExecutorMigracoes             aplica db/migracao/V*.sql
```

- `salvar` decide entre INSERT e UPDATE pelo id (`null` = novo).
- Convenções iguais em todos: conexão como primeiro parâmetro, `Optional` nas buscas, `RepositorioException` em toda falha.
- Controllers e serviços recebem as interfaces por injeção de dependência (`config/PersistenciaConfig`). Não há SQL fora de `repositorio/mysql`.
- O antigo `ProdutoDAO` foi migrado para `ProdutoRepositoryMySql` e removido.
- **Adaptação:** as entidades ganharam um `id` numérico (chave do banco), mas a API continua identificando os registros por CPF, código e número nas URLs, para não quebrar o front.
- `FormaPagamentoRepository` (pagamentos) continua em memória: os pagamentos não fazem parte do escopo da Aula 13. A situação `PAGO` do pedido, porém, é gravada no banco.

#### Desejáveis implementados

- **Atualização parcial:** `PATCH /api/produtos/{codigo}/estoque`.
- **Concorrência otimista:** coluna `produto.versao`. O `UPDATE ... WHERE id = ? AND versao = ?` recusa gravação com versão antiga → **409** (coberto por teste). O `PUT` aceita `versao` opcional no corpo.
- **Paginação:** `?pagina=0&tamanho=20` em produtos, clientes e pedidos (sem os parâmetros, devolve tudo, como antes).
- **Auditoria:** coluna `atualizado_em` com `ON UPDATE CURRENT_TIMESTAMP` em produto, cliente e pedido.

#### Endpoints novos/alterados e códigos de erro

| Método | Rota | Sucesso | Erros |
| :--- | :--- | :--- | :--- |
| GET | `/api/produtos?situacao=&nome=&pagina=&tamanho=` | 200 | 400 (parâmetro inválido) |
| GET | `/api/produtos/{codigo}` | 200 | 404 |
| PUT | `/api/produtos/{codigo}` | 200 | 404, 409 (código duplicado / versão desatualizada), 400 |
| PATCH | `/api/produtos/{codigo}/estoque` `{"quantidade": n}` | 200 | 404, 400 |
| DELETE | `/api/produtos/{codigo}` (inativa) | 204 | 404 |
| DELETE | `/api/produtos/{codigo}?definitivo=true` | 204 | 404, 409 (já vendido) |
| POST | `/api/produtos/{codigo}/inativar` · `/reativar` | 204 · 200 | 404 |
| GET | `/api/clientes?situacao=&pagina=&tamanho=` | 200 | 400 |
| GET | `/api/clientes/{cpf}` · `/api/clientes/{cpf}/pedidos` | 200 | 404 |
| POST | `/api/clientes` | 201 | 409 (CPF/e-mail duplicado), 400 |
| PUT | `/api/clientes/{cpf}` | 200 | 404, 409, 400 |
| DELETE | `/api/clientes/{cpf}` | 204 | 404, 409 (possui pedidos) |
| POST | `/api/clientes/{cpf}/inativar` · `/reativar` | 204 · 200 | 404 |
| GET | `/api/pedidos?situacao=ABERTO\|PAGO\|CANCELADO&pagina=&tamanho=` | 200 | 400 |
| GET | `/api/pedidos/{numero}` | 200 | 404 (antes devolvia 200 vazio) |
| POST | `/api/pedidos` | 201 | 404 (cliente/produto), 422 (estoque insuficiente), 400 |
| PUT | `/api/pedidos/{numero}/status` | 200 | 404, 409 |
| POST | `/api/pedidos/{numero}/cancelar` | 200 | 404, 409 (já cancelado) |
| DELETE | `/api/pedidos/{numero}` | 204 | 404, 409 (pago ou cancelado) |
| DELETE | `/api/pedidos/{numero}/itens/{itemId}` | 200 | 404, 409 (pedido não está aberto) |
| POST | `/api/pagamentos` | 201 | 404 (pedido), 409, 422 (pedido inválido / pagamento recusado) |

Falhas técnicas do banco devolvem **500** com mensagem genérica; o detalhe vai só para o log. Todas as respostas de erro seguem o formato `{"erro": "mensagem"}`, já consumido pelo front.

#### Migrations

Ficam em `back-end/ecommerce-pedidos-swift/src/main/resources/db/migracao/`:

| Versão | Conteúdo |
| :--- | :--- |
| `V1__esquema_inicial.sql` | Tabelas `cliente`, `produto`, `pedido`, `item_pedido` (com `preco_unitario`) |
| `V2__politicas_integridade_referencial.sql` | CASCADE / RESTRICT explícitos nas FKs |
| `V3__soft_delete.sql` | Coluna `ativo` em produto e cliente |
| `V4__concorrencia_e_auditoria.sql` | `versao` e `atualizado_em` |

**Aplicação automática:** ao subir a aplicação, o `ExecutorMigracoes` aplica as versões pendentes e as registra na tabela `schema_migracao`. O banco `ecommerce` é criado automaticamente (`createDatabaseIfNotExist=true`). Para desligar, use `db.migracoes.automaticas=false`.

**Aplicação manual** (alternativa):

```bash
cd back-end/ecommerce-pedidos-swift/src/main/resources/db/migracao
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ecommerce"
for f in V1__*.sql V2__*.sql V3__*.sql V4__*.sql; do mysql -u root -p ecommerce < "$f"; done
```

#### Como rodar os testes

```bash
cd back-end/ecommerce-pedidos-swift
mvn clean test                                  # tudo (unidade + integração com MySQL)
mvn clean test -DexcludedGroups=integracao      # só os testes que não usam banco
```

- Os testes de integração (tag `integracao`, pacote `integracao/`) usam um banco **separado**, `ecommerce_test`. Ele é criado se não existir, o esquema é recriado pelas próprias migrations e os dados são limpos após cada teste. A URL é configurável por `DB_TEST_URL`; usuário e senha vêm de `DB_USERNAME`/`DB_PASSWORD` ou do `application.properties`.
- Se o MySQL não estiver acessível, os testes de integração são **ignorados** (não falham).
- Os serviços também são testados sem banco, com repositórios em memória (`apoio/BancoEmMemoria`).

Cenários cobertos: atualizar e conferir o valor gravado; atualizar id inexistente (erro de domínio); excluir e confirmar vazio; RESTRICT com mensagem de negócio; CASCADE; soft delete fora das listagens; preço histórico; concorrência otimista; paginação; e os **4 cenários de transação**: sucesso completo, falha ao gravar o segundo item, falha ao baixar estoque e estoque insuficiente no meio do laço (venda concorrente em outra conexão). Nos cenários de falha, o teste confirma que nenhum pedido, item ou alteração de estoque permaneceu.

**Resultado atual:** 73 testes, 0 falhas (41 sem banco + 32 de integração).

**Status da Aula 13:** concluída na branch `feature/crud-completo`.

---

### Fluxo de Trabalho e Versionamento Combinado

Para assegurar a integridade da branch principal (`main`), estabelecemos o seguinte fluxo colaborativo:

1. **Partir de uma main atualizada:**

   ```bash
   git switch main && git pull
   ```

2. **Branch de Funcionalidade:**

   Criar uma branch com nome curto, em minúsculas e separado por hífens:

   ```bash
   git switch -c feature/nome-do-modulo
   ```

3. **Commits Pequenos e Atômicos:**

   Commits devem conter apenas uma ideia, utilizando mensagens padronizadas:

   ```text
   tipo: descrição curta no imperativo
   ```

   Exemplo:

   ```text
   feat: adiciona calculo de frete por peso
   ```

4. **Proteção da main e Revisão (Pull Request):**

   Publicar a branch:

   ```bash
   git push -u origin feature/nome-do-modulo
   ```

   Em seguida, abrir um Pull Request (PR).

   A branch `main` exige pelo menos **1 aprovação** de outro membro da equipe antes do merge.

5. **Code Review Construtivo:**

   O revisor deve analisar a aba *Files changed* do PR, comentando pontos fortes e sugerindo melhorias técnicas diretamente nas linhas de código antes de aprovar.

---

### Estrutura Real de Pastas (Back-end & Front-end)

A estrutura atual do repositório separa o Back-end e o Front-end:

```text
ecommerce-pedidos-swift/
├── back-end/
│   └── ecommerce-pedidos-swift/
│       ├── src/
│       │   ├── main/
│       │   │   └── java/
│       │       └── com/ecommerce/pedidos/
│       │           ├── config/          (injeção das implementações MySQL)
│       │           ├── controller/
│       │           ├── excecao/
│       │           ├── modelo/
│       │           ├── repositorio/     (interfaces)
│       │           │   └── mysql/       (implementações JDBC)
│       │           ├── servico/
│       │           ├── util/            (ConexaoFactory)
│       │           └── swift/
│       │   └── resources/db/migracao/   (V1..V4 .sql)
│       └── test/
│           └── java/com/ecommerce/pedidos/
│               ├── modelo/  servico/  controller/  repositorio/mysql/
│               ├── apoio/               (repositórios em memória)
│               └── integracao/          (testes com MySQL)
│       └── pom.xml
│
├── front-end/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── ...
│   ├── public/
│   ├── package.json
│   └── vite.config.*
│
├── .gitignore
└── README.md
```

> A estrutura interna do `front-end/src` pode evoluir conforme a organização dos componentes e serviços da aplicação.

---

### Como Rodar o Projeto

#### Back-end

Pré-requisito: MySQL 8 em execução (padrão `localhost:3306`, usuário e senha do `application.properties` ou das variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`). O banco e as tabelas são criados automaticamente na primeira execução.

Navegue até o diretório que contém o `pom.xml`:

```bash
cd back-end/ecommerce-pedidos-swift
```

Execute a aplicação Spring Boot:

```bash
mvn spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080/api
```

#### Front-end

Em outro terminal, navegue até o diretório:

```bash
cd front-end
```

Instale as dependências:

```bash
npm install
```

Execute o servidor de desenvolvimento:

```bash
npm run dev
```

O endereço exibido pelo Vite deverá ser utilizado para acessar a interface.

#### Execução completa

Para utilizar o sistema integrado, o **Back-end deve estar em execução antes ou simultaneamente ao Front-end**, pois a interface consome os dados disponibilizados pela API REST.

Fluxo:

```text
Front-end
    ↓
API REST
    ↓
Spring Boot
    ↓
Camada de domínio / serviços / persistência
```

---

### Roadmap do Projeto (Status por Aula)

| Aula | Entrega Prevista | Status |
| :---: | :--- | :---: |
| **01** | Repositório criado, estruturado, com README e commit inicial | 🟢 Concluído |
| **02** | Fluxo de branches e primeiro Pull Request revisado | 🟢 Concluído |
| **03** | Classe utilitária (`PedidoUtils.java`) do domínio incorporada | 🟢 Concluído |
| **04** | Classes de domínio inicial (Produto, Cliente, Pedido, ItemPedido) | 🟢 Concluído |
| **05** | Encapsulamento e abstração aplicados | 🟢 Concluído |
| **06** | Hierarquia de formas de pagamento (herança) | 🟢 Concluído |
| **07** | Relacionamentos entre classes do domínio | 🟢 Concluído |
| **08** | Módulo de pagamento polimórfico | 🟢 Concluído |
| **09** | Tratamento de exceções e validações | 🟢 Concluído |
| **10** | Suíte de testes unitários | 🟢 Concluído |
| **11** | Suíte de testes de integração + relatório de cobertura | ⏳ Pendente |
| **12** | Persistência: conexão, operações Create e Read | 🟢 Concluído |
| **13** | Persistência: operações Update, Delete e padrão DAO/Repository | 🟢 Concluído |
| **14** | Migração e estruturação com Spring Boot | 🟢 Concluído |
| **15** | API REST completa + integração com Front-end + pipeline de CI/CD | 🟡 Em desenvolvimento |
| **16** | Entrega final, documentação consolidada e apresentação | ⏳ Pendente |

---

### Próximos Passos

As próximas evoluções do projeto estão concentradas em:

1. **Completar os relacionamentos do domínio**, conforme os requisitos das aulas.
2. **Consolidar o módulo de pagamentos** utilizando polimorfismo.
3. **Implementar tratamento de exceções e validações** na API.
4. **Ampliar a cobertura de testes unitários e de integração.**
5. **Implementar e consolidar a persistência em banco de dados.**
6. **Completar as operações CRUD necessárias.**
7. **Finalizar o ciclo completo de pedidos e seus itens.**
8. **Consolidar a API REST para todos os módulos.**
9. **Evoluir a integração Front-end / Back-end.**
10. **Implementar o pipeline de CI/CD.**
11. **Realizar os ajustes finais de UX, documentação e apresentação.**

---

### Combinado da Equipe (Ética e Convivência)

1. **Transparência e Comunicação:** Alinhamento contínuo sobre o andamento das tarefas via GitHub e canal oficial de comunicação da equipe.

2. **Comprometimento com Prazos:** Cumprimento rigoroso do cronograma de entregas estabelecido no roadmap das aulas.

3. **Qualidade de Código:** Revisão criteriosa em todos os Pull Requests (*Code Review*) antes de realizar o *merge* na branch principal (`main`).

---

### Licença

Projeto estritamente acadêmico — **Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"**. Todos os direitos reservados aos autores e à instituição.

---

### Tratamento de Exceções de Negócio e Regras de Domínio

Durante a fase de refinamento do domínio, o projeto adotou uma hierarquia de exceções própria para separar erro técnico de erro de negócio.

#### Pacote de exceções

O pacote é `com.ecommerce.pedidos.excecao` e contém:

- `ECommerceException` — raiz do domínio, estende `Exception` e aceita mensagem e causa.
- `EstoqueInsuficienteException` — representa falta de estoque com dados do produto e da quantidade solicitada.
- `PagamentoRecusadoException` — representa recusa de processamento com a forma de pagamento e o motivo.
- `ClienteNaoEncontradoException` — identificador buscado do cliente.
- `PedidoInvalidoException` — representa pedido inválido para pagamento, como pedido sem itens ou fora do estado `ABERTO`.

#### Critério checked × unchecked

A equipe definiu um único critério para todo o projeto:

- `Estoque insuficiente` → `checked` (`ECommerceException` / `EstoqueInsuficienteException`)
  - Quem chamou pode tentar outro produto ou quantidade.
- `Pagamento recusado` → `checked` (`PagamentoRecusadoException`)
  - Quem chamou pode sugerir outra forma de pagamento.
- `Pedido já pago` → `unchecked` (`IllegalStateException`)
  - Erro de uso da classe, não uma situação de negócio recuperável no runtime.
- `Produto nulo em adicionarItem` → `unchecked` (`IllegalArgumentException`)
  - Defeito de quem chamou.
- `Preço negativo em Produto` → `unchecked` (`IllegalArgumentException`)
  - Nenhuma reação automática em runtime resolve isso.

#### Regras de negócio aplicadas

- `Produto.baixarEstoque(int quantidade)`:
  - quantidade negativa ou zero → `IllegalArgumentException`
  - quantidade maior que o estoque → `EstoqueInsuficienteException`
- `Pedido.adicionarItem(Produto produto, int quantidade)`:
  - produto nulo → `IllegalArgumentException`
  - quantidade inválida → `IllegalArgumentException`
  - pedido fora de `ABERTO` → `IllegalStateException`
  - estoque indisponível → `EstoqueInsuficienteException`
- `Pedido.pagar(ProcessadorPagamento processador)`:
  - processador nulo → `IllegalArgumentException`
  - pedido sem itens → `PedidoInvalidoException`
  - pedido fora de `ABERTO` → `PedidoInvalidoException`
  - processamento recusado → `PagamentoRecusadoException`
  - em caso de recusa, o pedido permanece `ABERTO` e não muda de situação.

#### Status da Aula 12

O backend do projeto foi modernizado e padronizado para atender aos requisitos da aula atual:

- os subpacotes foram unificados em Português, com `modelo`, `servico`, `repositorio`, `excecao`, `controller` e `swift` em uso consistente;
- a suíte de testes cobre os fluxos de domínio e foi ampliada com o teste de integração `PedidoIntegracaoTest`, isolando o ciclo de criação do pedido e processamento de pagamento;
- a persistência inicial foi configurada com a dependência oficial do MySQL e a classe `ConexaoFactory` via JDBC, preparadas para a etapa de armazenamento real em banco de dados.

A validação recomendada é:

```bash
git switch feat/aula-12-refatoracao
mvn clean test
```

---