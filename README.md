# Sistema de Gestão de Pedidos — E-commerce

[![CI](https://github.com/lucaspatracao/ecommerce-pedidos-swift/actions/workflows/ci.yml/badge.svg)](https://github.com/lucaspatracao/ecommerce-pedidos-swift/actions/workflows/ci.yml)

API REST em Spring Boot + interface React para cadastrar clientes e produtos, registrar pedidos com baixa de estoque transacional e pagá-los por Pix, Boleto ou Cartão.

📐 [Arquitetura e diagramas](docs/arquitetura.md) · 📄 [Relatório técnico](docs/relatorio-tecnico.md) · 📝 [Exercícios da Aula 15](docs/aula-15-exercicios.md) · 🎤 [Roteiro do pitch](docs/pitch.md)

---

### Pré-requisitos

| Ferramenta | Versão | Observação |
| :--- | :--- | :--- |
| JDK | 17 ou mais novo | O código compila com `--release 17`. O CI usa o 17 (Temurin); o build também passa com JDK mais novo. |
| Maven | 3.9+ | Não há Maven Wrapper (`mvnw`) no repositório: o `mvn` precisa estar no PATH. |
| MySQL | 8.x | Só para rodar a aplicação. Os testes **não** usam MySQL. |
| Node.js | 20.19+ ou 22.13+ | Exigência do Vite 8 e do ESLint 10. O CI usa o Node 22. |

---

### Rodando em 5 minutos

Os comandos abaixo são para o PowerShell, a partir da raiz do repositório.

**1. Banco (uma vez).** O Flyway cria as tabelas na primeira subida. Só o banco vazio precisa existir:

```powershell
mysql -u root -p -e "CREATE DATABASE ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

> No Windows, o instalador do MySQL **não** coloca o `mysql` no PATH. Se aparecer "mysql não é reconhecido", use o caminho completo (`& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "..."`) ou rode o `CREATE DATABASE` no MySQL Workbench.

**2. Variáveis.** A API lê `DB_USERNAME` e `DB_PASSWORD` (obrigatórias) e `DB_URL` (opcional; o padrão é `localhost:3306/ecommerce_swift`). Nenhuma senha fica no repositório. Escolha **uma** das opções:

```powershell
# Opção A: só neste terminal
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua-senha-local"
# Só se o MySQL não estiver em localhost:3306:
# $env:DB_URL = "jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

# Opção B: arquivo .env (fica no .gitignore). Preencha DB_PASSWORD no arquivo.
Copy-Item back-end\ecommerce-pedidos-swift\.env.example back-end\ecommerce-pedidos-swift\.env
notepad back-end\ecommerce-pedidos-swift\.env
```

> O `.env` é lido da **pasta em que a API é iniciada** (`spring.config.import=optional:file:.env[.properties]`). Por isso o passo 3 sobe a API de dentro de `back-end\ecommerce-pedidos-swift`. Se a variável existir no terminal e no `.env`, vale a do terminal.

**3. API** (porta 8080):

```powershell
cd back-end\ecommerce-pedidos-swift
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Na primeira subida, o log mostra `Successfully applied 3 migrations`. A API responde em `http://localhost:8080/api` (teste com `http://localhost:8080/api/dashboard`). No PowerShell, as aspas no `-D` são obrigatórias. `dev` já é o perfil padrão.

**4. Front-end** (porta 5173), em outro terminal:

```powershell
cd front-end
npm ci
npm run dev
```

Abra `http://localhost:5173`. Use `npm ci`, e não `npm install`: ele instala exatamente as versões do `package-lock.json`, como o CI, e não altera o lockfile.

#### Problemas comuns

| Sintoma | Causa | Solução |
| :--- | :--- | :--- |
| `Access denied for user '${DB_USERNAME}'@'localhost'` | As variáveis não foram definidas (o texto `${...}` chega literal ao MySQL), ou o `.env` está em outra pasta | Faça o passo 2 no **mesmo** terminal, ou confira se o `.env` está em `back-end\ecommerce-pedidos-swift` e se a API foi iniciada dali |
| `Access denied for user 'root'@'localhost'` | Senha errada em `DB_PASSWORD` | Corrija a senha |
| `Communications link failure` | MySQL parado ou em outra porta | Inicie o serviço MySQL ou ajuste `DB_URL` |
| `Unknown database 'ecommerce_swift'` | Passo 1 não feito | Crie o banco |
| Front mostra erro ao carregar os dados | API fora do ar | Suba a API (passo 3) antes de usar a tela |

---

### Testes e cobertura

```powershell
cd back-end\ecommerce-pedidos-swift
mvn clean test     # só a suíte de unidade (rápida, sem Spring)
mvn clean verify   # unidade + integração (Spring, H2 e Flyway) + metas de cobertura
```

* **Não precisa de MySQL nem de variáveis:** os testes de integração (`@Tag("integracao")`, rodados pelo Failsafe no `verify`) usam o perfil `test`, com **H2 em memória** no modo MySQL e as mesmas migrations do Flyway.
* **Números do `mvn clean verify` atual:** 97 testes de unidade e 71 de integração, nenhuma falha. Cobertura total de **96,9% das linhas** (896/925) e **90,9% dos ramos** (281/309).
* **Metas do JaCoCo** (o build **falha** abaixo delas): pacotes `modelo` e `excecao` com 70% das linhas nos testes de unidade (`mvn test`); projeto inteiro com 85% das linhas e 65% dos ramos (`mvn verify`). Ficam fora da medição só a classe `main` e os `record` de DTO.
* **Relatórios:** `back-end/ecommerce-pedidos-swift/target/site/jacoco/` (unidade), `jacoco-it/` (integração) e `jacoco-total/` (as duas somadas). Abra o `index.html`.
* **Front-end:** `cd front-end` · `npm run lint` · `npm run build`.
* **CI:** os mesmos comandos rodam no GitHub Actions a cada push na `main` e em todo PR (veja [Integração contínua](#integração-contínua)).

---

### Rotas da API

Base: `http://localhost:8080/api`. Os recursos usam os identificadores naturais: `cpf` (cliente), `codigo` (produto) e `numero` (pedido).

Regras que a API segue (Aula 15):

* **Endereços são substantivos:** a ação fica no método HTTP, não na URL (nada de `/cancelar` ou `/inativar`).
* **POST que cria responde `201 Created`** com o cabeçalho `Location` apontando para o recurso novo e o DTO no corpo.
* **DELETE responde `204 No Content`**, sem corpo.
* **Mudança parcial usa `PATCH`** (só os campos enviados mudam); `PUT` substitui o recurso inteiro.
* **Erro nunca vem com 200:** todo erro tem status 4xx/5xx e o [corpo padronizado](#corpo-de-erro-padronizado).

| Método | Endereço | O que faz | Sucesso | Erro |
| :--- | :--- | :--- | :---: | :--- |
| GET | `/clientes?situacao=&pagina=&tamanho=` | Lista clientes (padrão: ativos) | 200 | 400 |
| GET | `/clientes/{cpf}` | Busca cliente ativo | 200 | 404 |
| GET | `/clientes/{cpf}/pedidos?pagina=&tamanho=` | Pedidos do cliente, inclusive inativo | 200 | 404 |
| POST | `/clientes` | Cadastra cliente | 201 + `Location` | 400, 409 (CPF ou e-mail já cadastrado) |
| PUT | `/clientes/{cpf}` | Atualiza nome e contato (o CPF não muda) | 200 | 400, 404, 409 (e-mail em uso) |
| PATCH | `/clientes/{cpf}` | `{"ativo": false}` inativa; `{"ativo": true}` reativa | 200 | 400, 404 |
| DELETE | `/clientes/{cpf}` | Exclui cliente sem pedidos | 204 | 404, 409 (tem pedidos: inative-o) |
| GET | `/produtos?situacao=&nome=&pagina=&tamanho=` | Lista produtos (padrão: ativos) | 200 | 400 |
| GET | `/produtos/{codigo}` | Busca produto ativo | 200 | 404 |
| POST | `/produtos` | Cadastra produto | 201 + `Location` | 400, 409 (código já cadastrado) |
| PUT | `/produtos/{codigo}` | Atualiza produto (inclusive inativo); `versao` opcional | 200 | 400, 404, 409 (código duplicado / versão desatualizada) |
| PATCH | `/produtos/{codigo}` | `{"ativo": false}` inativa; `{"ativo": true}` reativa | 200 | 400, 404 |
| PATCH | `/produtos/{codigo}/estoque` | Altera só o estoque: `{"quantidade": n}` | 200 | 400, 404 |
| DELETE | `/produtos/{codigo}` | Soft delete (inativa) | 204 | 404 |
| DELETE | `/produtos/{codigo}?definitivo=true` | Exclui produto nunca vendido | 204 | 404, 409 (já vendido: inative-o) |
| GET | `/pedidos?situacao=&pagina=&tamanho=` | Lista pedidos (mais recentes primeiro) | 200 | 400 |
| GET | `/pedidos/{numero}` | Busca pedido completo | 200 | 404 |
| POST | `/pedidos` | Cria pedido e baixa o estoque | 201 + `Location` | 400, 404 (cliente/produto), 409 (estoque insuficiente / produto inativo) |
| PATCH | `/pedidos/{numero}` | `{"situacao": "CANCELADO"}` cancela (devolve estoque e estorna pagamento); `ABERTO` não muda nada | 200 | 400, 404, 409 (já cancelado / transição inválida) |
| DELETE | `/pedidos/{numero}` | Exclui pedido em aberto (devolve estoque) | 204 | 404, 409 (pago ou cancelado) |
| DELETE | `/pedidos/{numero}/itens/{itemId}` | Remove item de pedido aberto (devolve estoque) | 204 | 404, 409 (pedido não está aberto) |
| POST | `/pedidos/{numero}/pagamento` | Paga o pedido | 201 + `Location` | 400, 404, 409 (já pago / recusado / valor ≠ total) |
| GET | `/pedidos/{numero}/pagamento` | Pagamento do pedido | 200 | 404 (pedido inexistente ou sem pagamento) |
| GET | `/pagamentos` | Lista todos os pagamentos | 200 | — |
| GET | `/dashboard` | Indicadores (contagens de ativos) | 200 | — |
| GET | `/historico` | Últimos 10 pedidos | 200 | — |

Parâmetros das listagens: `situacao=ativos|inativos|todos` (clientes e produtos) ou `aberto|pago|cancelado` (pedidos); `pagina` começa em 0 e `tamanho` vai de 1 a 500 (padrão 20). Sem `pagina` nem `tamanho`, a lista vem inteira, como o front espera.

Exemplo de criação de pedido:

```http
POST /api/pedidos
Content-Type: application/json

{ "clienteCpf": "78617768916", "itens": [ { "codigo": "CAM-001", "quantidade": 2 } ] }
```

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/pedidos/PED-2026-00042

{ "numero": "PED-2026-00042", "situacao": "ABERTO", "valorTotal": 99.80, ... }
```

Exemplo de pagamento. O pedido vem do endereço; os campos usados dependem do `tipo`:

```json
POST /api/pedidos/PED-2026-00042/pagamento
{ "tipo": "PIX", "valor": "99.80", "chave": "cliente@email.com", "tipoChave": "EMAIL" }
{ "tipo": "BOLETO", "valor": "99.80", "vencimento": "2026-12-31", "codigo": "34191.79001" }
{ "tipo": "CARTAO", "valor": "99.80", "numero": "**** 4242", "bandeira": "Visa", "parcelas": 3 }
```

#### Corpo de erro padronizado

Toda resposta de erro tem o mesmo formato (`dto/ErroResponse`), montado pelo `ApiExceptionHandler`:

| Campo | Conteúdo |
| :--- | :--- |
| `status` | o mesmo código da linha de status HTTP |
| `erro` | rótulo do status: "Requisição inválida" (400), "Não encontrado" (404), "Conflito" (409), "Erro interno" (500) |
| `mensagem` | texto de negócio, pronto para mostrar ao usuário |
| `caminho` | endereço da requisição que falhou |
| `campos` | só em erro de validação: lista de `{ campo, mensagem }` |

Conflito (409):

```json
{
  "status": 409,
  "erro": "Conflito",
  "mensagem": "Estoque insuficiente de Camiseta Básica: disponível 2, solicitado 5",
  "caminho": "/api/pedidos"
}
```

Validação (400), com a mensagem de cada campo:

```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Há campos inválidos. Corrija-os e tente novamente.",
  "caminho": "/api/clientes",
  "campos": [
    { "campo": "email", "mensagem": "E-mail inválido." },
    { "campo": "nome", "mensagem": "Nome é obrigatório." }
  ]
}
```

Erro inesperado (500): sempre `"mensagem": "Erro interno. Tente novamente mais tarde."`. Nome de classe, SQL e stack trace ficam só no log do servidor.

No front-end, `src/api/clienteApi.js` transforma esse corpo num `ErroApi` (`message` = `mensagem`, `campos` = `{ email: "E-mail inválido.", ... }`), e cada formulário mostra a mensagem junto do campo.

> Os CPFs deste README e dos testes são **sintéticos**, gerados com dígitos verificadores válidos apenas para exemplo. Não use CPFs reais em testes.

---

### Squad e créditos

**Squad Swift**

| Nome | Papel |
| :--- | :--- |
| **Lucas Nunes Patracão** | Responsável do dia |
| **Rafael Rubiá Oliveira Cardoso** | Desenvolvedor / Colaborador |

> **Projeto Integrador** da Unidade Curricular: *Desenvolvimento Back-end*
>
> **Curso:** Superior de Tecnologia em Análise e Desenvolvimento de Sistemas (Turma CSTADS601)
>
> **Instituição:** Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"

---

### Descrição do Desafio

O objetivo principal deste projeto é conceber e implementar uma solução completa de **Gestão de Pedidos para E-commerce**, contemplando Back-end, API REST e uma interface Front-end integrada.

A aplicação engloba a modelagem de domínio, regras de negócio e infraestrutura para administrar o cadastro de clientes e catálogo de produtos, gerenciar o ciclo de vida dos pedidos e processar transações financeiras com múltiplos métodos de pagamento.

A solução também possui uma interface administrativa para consumo e apresentação dos dados disponibilizados pela API, mantendo o **Back-end como fonte de verdade do sistema**.

---

### Funcionalidades

* **Gerenciamento de Produtos:** cadastro, atualização completa (com versão otimista) e parcial (estoque), consulta com filtros e paginação, soft delete (inativar/reativar) e exclusão definitiva de produto nunca vendido.
* **Gerenciamento de Clientes:** cadastro com CPF validado por dígitos verificadores e e-mail único, edição, soft delete e exclusão de cliente sem pedidos.
* **Gestão de Pedidos:** abertura com vários itens, cálculo de totais, baixa de estoque transacional, remoção de item, cancelamento e exclusão de pedido em aberto.
* **Processamento de Pagamentos:** Cartão de Crédito, Boleto, Pix (e Vale-Compras no domínio), com arquitetura polimórfica.
* **Dashboard e Histórico:** totais, receita, estoque crítico e pedidos recentes.
* **Interface Administrativa:** front-end React consumindo a API real.
* **Garantia de Qualidade:** testes unitários, de integração e de API, com cobertura mínima verificada no build.

---

### Tecnologias

* **Linguagem:** Java 17 (LTS)
* **Build e dependências:** Apache Maven
* **Framework:** Spring Boot 3.3 (Web, Validation, Data JPA)
* **Persistência:** Hibernate/JPA, **MySQL 8** (desenvolvimento) e **Flyway** (migrations)
* **Testes:** JUnit 5, Mockito, MockMvc, H2 em memória (modo MySQL), Surefire/Failsafe e **JaCoCo** (cobertura)
* **Front-end:** React + Vite (Axios para a API)
* **Controle de versão:** Git & GitHub
* **CI:** GitHub Actions (testes, cobertura, lint e build em todo PR). Não há deploy (CD) em nuvem.

---

### Perfis de Execução

| Perfil | Banco | Para quê | Arquivo |
| :--- | :--- | :--- | :--- |
| `dev` (padrão) | MySQL `ecommerce_swift` | Desenvolvimento local; mostra o SQL formatado no console (`show-sql`, `format_sql`) | `src/main/resources/application-dev.properties` |
| `test` | H2 em memória, `MODE=MySQL` | Testes automatizados (`@ActiveProfiles("test")`) | `src/test/resources/application-test.properties` |

Configurações comuns (`application.properties`): `ddl-auto=validate` (o Hibernate só confere o schema criado pelo Flyway; nunca cria tabelas), `open-in-view=false` e leitura de `${DB_URL}`, `${DB_USERNAME}` e `${DB_PASSWORD}`.

---

### Integração contínua

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda no GitHub Actions a cada **push na `main`** e em **todo pull request**. O selo no topo deste README mostra o resultado da última execução na `main`.

| Job | Ambiente | O que roda | Artefatos |
| :--- | :--- | :--- | :--- |
| `backend` | Ubuntu · Java 17 (Temurin) · cache do Maven | `mvn -B verify`: testes de unidade, testes de integração e metas de cobertura do JaCoCo (85% das linhas e 65% dos ramos) | `jacoco` (relatório `jacoco-total/`) e `aplicacao-jar` (o `.jar` gerado) |
| `frontend` | Ubuntu · Node 22 · cache do npm | `npm ci`, `npm run lint` e `npm run build` | — |

* **Sem segredos e sem MySQL:** os testes de integração usam o perfil `test` (H2 em memória com as mesmas migrations do Flyway).
* **Nunca** `mvn package` nem `-DskipTests`: o objetivo do pipeline é justamente rodar os testes.
* Os dois jobs rodam em paralelo. Um push novo no mesmo PR cancela a execução anterior.

> **Regra da equipe:** PR com pipeline vermelho **não é integrado**. Quem abriu o PR corrige e faz um novo push. Ninguém faz merge "para arrumar depois", nem desliga ou pula testes para o selo ficar verde.

---

### Arquitetura (resumo)

Diagramas completos (camadas, domínio, sequência e execução) em [docs/arquitetura.md](docs/arquitetura.md).

```text
Front-end (React)
    ↓  HTTP + JSON
controller   → recebe a requisição, aplica @Valid e devolve DTOs
    ↓
servico      → interfaces (ClienteService...) + implementações (...Impl) com @Transactional
    ↓
modelo       → entidades JPA com as regras de negócio (Pedido calcula o total, Produto controla o estoque)
    ↓
repositorio  → interfaces Spring Data JPA (CRUD + @Query JPQL + JOIN FETCH)
    ↓
MySQL (schema versionado pelo Flyway)
```

* **DTOs** (`dto/`): os *requests* têm Bean Validation (`@NotBlank`, `@Email`, `@Positive`...). Os *responses* fixam o JSON que o front-end consome, sem expor as relações do banco. Clientes, produtos e pedidos são identificados por CPF, código e número; só o item do pedido expõe o `id`, usado para removê-lo.
* **Erros** (`ApiExceptionHandler`): todo erro volta no [corpo padronizado](#corpo-de-erro-padronizado) `{ status, erro, mensagem, caminho, campos }`; o front mostra a `mensagem` e, na validação, a mensagem de cada campo.
* **CORS** (`config/CorsConfig`): liberado num lugar só para o front do Vite (`localhost:5173` e `127.0.0.1:5173`).
* **Transação**: criar um pedido baixa o estoque de todos os itens numa transação só. Se qualquer item falhar, nada é gravado (rollback).
* **Consultas**: `@Query` em JPQL (cliente por CPF, estoque crítico, receita via `SUM`) e `JOIN FETCH` no `PedidoRepository`, que carrega pedido, cliente, itens, produtos e pagamento numa única consulta e evita o problema N+1.

---
### CRUD Completo, Integridade Referencial e Transações (Aula 13)

As regras vieram da branch `feature/crud-completo` (PR #18), que as implementou com JDBC manual. Nesta versão elas rodam sobre **Spring Data JPA + Flyway**, com o mesmo comportamento na API.

| Entidade | Create | Read | Update | Delete |
| :--- | :--- | :--- | :--- | :--- |
| Produto | ✅ | ativos por padrão, inativos sob pedido, busca por nome, paginação | completo (com versão otimista) e parcial (só estoque) | soft delete; exclusão definitiva só se nunca foi vendido |
| Cliente | ✅ (CPF e e-mail únicos) | ativos por padrão, por CPF, paginação | ✅ | exclusão definitiva (bloqueada se tiver pedidos) + soft delete |
| Pedido | ✅ transacional | por número, por cliente, por situação, paginação | situação (pagar / cancelar) | só pedido `ABERTO`; pago ou cancelado nunca é excluído |
| ItemPedido | ✅ junto com o pedido | ✅ junto com o pedido | — (o preço é histórico) | remover item de pedido aberto (devolve o estoque) |

#### Políticas de exclusão

| Relacionamento | Política | Por quê |
| :--- | :--- | :--- |
| Pedido → ItemPedido | `ON DELETE CASCADE` (+ `orphanRemoval` no JPA) | Composição: o item não existe sem o pedido. |
| Cliente → Pedido | `RESTRICT` + soft delete do cliente | O histórico de vendas não some com o cadastro. Cliente com pedidos é **inativado**. |
| Produto → ItemPedido | `RESTRICT` + soft delete do produto | Pedido antigo precisa do produto vendido. O `DELETE` da API **inativa** o produto. |
| Pedido pago | regra no serviço | Pedido pago é **cancelado** (estoque volta, pagamento estornado), nunca excluído. |

| Situação | O que acontece |
| :--- | :--- |
| Excluir pedido com itens | Só para pedido `ABERTO`: os itens saem em cascata e o estoque reservado volta, na mesma transação. |
| Excluir cliente com pedidos | **409**: "Este cliente possui pedidos e não pode ser excluído. Inative-o..." |
| Excluir produto que está em pedidos | `DELETE /api/produtos/{codigo}` inativa (204). Com `?definitivo=true` → **409**. |
| Excluir pedido já pago | **409**: "Pedido já pago não pode ser excluído. Cancele o pedido..." Use `PATCH /api/pedidos/{numero}` com `{"situacao": "CANCELADO"}`. |

#### Soft delete

* A coluna `ativo` existe em `produto` (desde a V1) e em `cliente` (V2).
* Listagens e buscas por CPF/código mostram só ativos. Inativos são pedidos de forma **explícita**: `?situacao=inativos|todos`. O enum `Visibilidade` (`ATIVOS`, `INATIVOS`, `TODOS`) faz esse filtro no serviço.
* Pedidos carregam cliente e produtos **sem** o filtro, de propósito: um pedido antigo continua mostrando o produto e o cliente, mesmo inativos, e o preço cobrado (`item_pedido.preco_praticado`).
* No front, o campo "Disponível" do produto é o próprio soft delete: desmarcado, o produto sai da listagem padrão.

#### Transação e concorrência

* `PedidoServiceImpl.criar` é `@Transactional`: pedido, itens e baixa de estoque são gravados juntos. Qualquer exceção (estoque insuficiente, produto inexistente) desfaz tudo.
* Os produtos da venda são lidos **com bloqueio** (`@Lock(PESSIMISTIC_WRITE)`, ou seja `SELECT ... FOR UPDATE`): duas vendas simultâneas do mesmo produto acontecem uma depois da outra, o estoque nunca fica negativo e a mensagem de estoque insuficiente mostra o saldo real.
* `Produto` tem `@Version`: o `PUT` aceita `versao` e recusa com **409** uma versão desatualizada.
* **Número do pedido** (`PED-ano-NNNNN`, ex.: `PED-2026-00042`): vem de um contador por ano no banco (tabela `sequencia_pedido`), lido com bloqueio na mesma transação do pedido. Pedidos simultâneos recebem números seguidos e diferentes, sem sorteio; se o pedido falhar, o número não é consumido. É uma tabela, e não `CREATE SEQUENCE`, porque o MySQL 8 não tem sequences.
* Cancelamento, pagamento, exclusão de pedido e remoção de item também são transacionais.

#### Migrations (Flyway)

Ficam em `back-end/ecommerce-pedidos-swift/src/main/resources/db/migration/`:

| Versão | Conteúdo |
| :--- | :--- |
| `V1__esquema_inicial.sql` | Tabelas de cliente, produto (com `ativo` e `versao`), pedido, itens e formas de pagamento (herança JOINED) |
| `V2__soft_delete_integridade_e_auditoria.sql` | `cliente.ativo`, e-mail único, `ON DELETE CASCADE` dos itens, produto único por pedido, `atualizado_em` (auditoria) e índices |
| `V3__sequencia_numero_pedido.sql` | Contador `sequencia_pedido` do número do pedido; renumera, no mesmo formato e na ordem de criação, os pedidos gravados antes com número sorteado |

Uma migration aplicada **nunca** é editada (o Flyway confere o checksum): mudanças de schema entram sempre como uma nova `V4__...`, `V5__...`. O SQL precisa rodar no MySQL 8 e no H2 em `MODE=MySQL`.

> **Banco criado pela versão JDBC:** quem rodou a `feature/crud-completo` tem tabelas criadas pelo executor de migrations antigo (`db/migracao`), que o Flyway não reconhece. Crie o banco `ecommerce_swift` de novo (veja "Como Rodar") antes de subir esta versão.

---

### Domínio e Conceitos por Aula

#### Utils e boas práticas (Aula 03)

`util/Utils` é `final`, tem construtor privado e só métodos estáticos: validação de **CPF** (dígitos verificadores, aceita máscara), **e-mail** e texto obrigatório. As entidades a usam em vez de repetir validações. `util/PedidoUtils` mantém as regras de cálculo definidas na aula:

| Constante | Valor Adotado | Regra de Negócio |
| :--- | :--- | :--- |
| `VALOR_POR_QUILO` | R$ 7,50 | Preço cobrado por quilo (ou fração) transportado. |
| `FRETE_MINIMO` | R$ 15,00 | Valor mínimo de frete, independente do peso total do carrinho. |
| `VALOR_FRETE_GRATIS` | R$ 300,00 | A partir deste subtotal, o valor do frete é zerado. |
| `TAXA_DESCONTO` | 10% (0.10) | Percentual de desconto padrão aplicado sobre o subtotal da compra. |
| `DESCONTO_MAXIMO` | R$ 50,00 | Teto máximo de desconto concedido por pedido. |

#### Encapsulamento e abstração (Aulas 04–05)

Atributos privados e **sem setters públicos**. As mudanças passam por métodos de intenção que validam antes de alterar: `Cliente.atualizarContato`, `Produto.atualizarDados`, `Produto.baixarEstoque`, `FormaPagamento.estornar`. As regras ficam na entidade: `Pedido.calcularValorTotal()` e `ItemPedido.calcularSubtotal()`.

#### Herança (Aula 06)

* `Pessoa` (abstrata, `@MappedSuperclass`) → `Cliente` (entidade) e `Funcionario`.
* `FormaPagamento` (abstrata, `processar()` abstrato) → `CartaoCredito`, `Boleto`, `Pix`, `ValeCompras`. No banco: herança **JOINED** (tabela `forma_pagamento` + uma tabela por subclasse).

#### Relacionamentos (Aula 07)

| Relacionamento | Multiplicidade | Mapeamento JPA |
| :--- | :--- | :--- |
| `Pedido` → `Cliente` | N : 1 | `@ManyToOne(fetch = LAZY)` |
| `Pedido` → `ItemPedido` | 1 : N | `@OneToMany(mappedBy, cascade = ALL, orphanRemoval)` |
| `ItemPedido` → `Produto` | N : 1 | `@ManyToOne(fetch = LAZY)` |
| `Pedido` → `FormaPagamento` | 1 : 0..1 | `@OneToOne(cascade = ALL)` + `UNIQUE` |
| `Cliente` → `Endereco` | 1 : 0..1 | `@Embeddable` (colunas na tabela `cliente`) |
| `Categoria` → `Produto` | 1 : N | Agregação apenas no domínio |

Itens repetidos do mesmo produto são somados num único `ItemPedido`, e o preço praticado é copiado do produto no momento da compra (histórico preservado).

#### Polimorfismo e princípio aberto/fechado (Aula 08)

O serviço de pagamento trabalha só com a abstração `FormaPagamento`, sem `instanceof` nem `switch` por tipo:

* `FabricaFormaPagamento` usa um **mapa** `tipo → construtor` no lugar do `switch`;
* `processar()`, `descreverMetodo()` e `dadosEspecificos()` são implementados por cada subclasse;
* adicionar uma nova forma de pagamento = nova subclasse + uma entrada no mapa, sem alterar `Pedido` nem o serviço.

#### Exceções (Aula 09)

Todas as exceções de negócio são **unchecked** (`RuntimeException`): o erro sobe até o `ApiExceptionHandler`, e o `@Transactional` só faz rollback automático em exceções unchecked.

| Exceção | Quando | HTTP |
| :--- | :--- | :---: |
| `NegocioException` | raiz das exceções de negócio | 400 |
| `RecursoNaoEncontradoException` (um handler só) e as subclasses `Cliente`/`Produto`/`Pedido`/`PagamentoNaoEncontradoException` | recurso inexistente | 404 |
| `ConflitoException` | produto vendido, cliente com pedidos, transição inválida | 409 |
| `RegistroDuplicadoException` | CPF, e-mail ou código já cadastrado | 409 |
| `EstoqueInsuficienteException` (código, solicitada, disponível) | estoque não basta | 409 |
| `PedidoInvalidoException` / `PedidoJaPagoException` | pagar pedido vazio, cancelado ou já pago | 409 |
| `PagamentoRecusadoException` (forma, motivo) | limite do cartão ou gateway recusou | 409 |
| `IllegalArgumentException` / Bean Validation | dado inválido | 400 |
| `IllegalStateException` | operação incompatível com o estado | 409 |
| qualquer outra | erro inesperado (detalhe só no log) | 500 |

#### Testes (Aulas 10, 11 e 14)

| Tipo | Exemplos | Suíte |
| :--- | :--- | :---: |
| Unitários | `ProdutoTest`, `ClienteTest`, `PedidoTest`, `ItemPedidoTest`, `FormasPagamentoTest`, `UtilsTest`, `PedidoUtilsTest`, `ParametrosConsultaTest` | `mvn test` |
| Integração (fluxo) | `FluxoCompraIntegracaoTest`: cadastro → pedido → pagamento com classes reais; Mockito só no `GatewayPagamento` (sistema externo) | `mvn verify` |
| Transação | `TransacaoPedidoTest`: falha no 2º item desfaz a baixa do 1º; duas vendas simultâneas da última unidade, só uma passa | `mvn verify` |
| Persistência | `PersistenciaIntegracaoTest`: soft delete, RESTRICT, CASCADE, UNIQUE, CHECK, versão otimista e auditoria conferidos direto no banco | `mvn verify` |
| Número do pedido | `NumeroPedidoIntegracaoTest`: pedidos seguidos e simultâneos sem número repetido; `MigracaoNumeroPedidoTest`: a V3 sobre pedidos antigos | `mvn verify` |
| Consultas | `RepositoriosConsultaTest`: `@Query` e prova do `JOIN FETCH` contando os SQLs | `mvn verify` |
| API | `ApiIntegracaoTest` e `CrudCompletoApiTest`: MockMvc com rotas e regras de negócio; `StatusHttpApiTest`: 201 + `Location` em cada POST e 204 em cada DELETE; `ErroPadronizadoApiTest`: corpo de erro em 400 (com `campos`), 404, 409 e 500 sem vazar detalhes | `mvn verify` |

---

### Estrutura de Pastas

```text
ecommerce-pedidos-swift/
├── back-end/ecommerce-pedidos-swift/
│   ├── .env.example                      # modelo das variáveis (sem senha)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/ecommerce/pedidos/
│       │   ├── controller/               # REST + ApiExceptionHandler
│       │   ├── dto/                      # requests (validação) e responses (contrato JSON)
│       │   ├── excecao/                  # hierarquia de exceções de negócio
│       │   ├── modelo/                   # entidades e regras de negócio
│       │   ├── repositorio/              # interfaces Spring Data JPA
│       │   ├── servico/                  # interfaces + implementações (@Transactional)
│       │   └── util/                     # Utils e PedidoUtils
│       ├── main/resources/
│       │   ├── application.properties
│       │   ├── application-dev.properties
│       │   └── db/migration/             # V1 a V3 (Flyway)
│       └── test/
│           ├── java/...                  # unitários, integração (@Tag "integracao") e API
│           └── resources/application-test.properties
├── front-end/                            # React + Vite (veja front-end/README.md)
│   └── src/                              # api/, components/, pages/, utils/
├── docs/                                 # arquitetura, relatório técnico, exercícios da Aula 15 e pitch
├── .github/workflows/ci.yml              # pipeline do GitHub Actions
├── .gitignore
└── README.md
```

---

### Roadmap do Projeto (Status por Aula)

| Aula | Entrega Prevista | Status |
| :---: | :--- | :---: |
| **01** | Repositório criado, estruturado, com README e commit inicial | 🟢 Concluído |
| **02** | Fluxo de branches e primeiro Pull Request revisado | 🟢 Concluído |
| **03** | Classes utilitárias (`Utils`, `PedidoUtils`) usadas pelo domínio | 🟢 Concluído |
| **04** | Classes de domínio (Produto, Cliente, Pedido, ItemPedido) | 🟢 Concluído |
| **05** | Encapsulamento e abstração aplicados | 🟢 Concluído |
| **06** | Herança (`Pessoa`, `FormaPagamento`) | 🟢 Concluído |
| **07** | Relacionamentos entre classes do domínio | 🟢 Concluído |
| **08** | Módulo de pagamento polimórfico | 🟢 Concluído |
| **09** | Tratamento de exceções e validações | 🟢 Concluído |
| **10** | Suíte de testes unitários | 🟢 Concluído |
| **11** | Testes de integração + cobertura (JaCoCo: 85% das linhas e 65% dos ramos no total) | 🟢 Concluído |
| **12** | Persistência: conexão, Create e Read (Spring Data JPA) | 🟢 Concluído |
| **13** | Persistência: CRUD completo, soft delete, integridade referencial e transações | 🟢 Concluído |
| **14** | Spring Boot: camadas, DTOs, validação, perfis, Flyway, `@Query`, `JOIN FETCH` | 🟢 Concluído |
| **15** | API REST (endereços, status, erro padronizado) + pipeline de CI no GitHub Actions | 🟢 Concluído |
| **16** | Entrega final, documentação consolidada e apresentação | 🟡 Em andamento |


---

### Dívidas Técnicas Registradas

1. **`PedidoUtils` com `double`:** os cálculos de frete e desconto da Aula 03 continuam em `double` por fins didáticos. O domínio (preços, totais, pagamentos) já usa `BigDecimal`.
2. **Autenticação e autorização:** a API ainda não exige login.
3. **Gateway de pagamento simulado:** `GatewayPagamentoSimulado` autoriza tudo. Uma integração real implementaria a interface `GatewayPagamento`.
4. **Sem deploy em nuvem:** o pipeline faz só integração contínua; a aplicação roda apenas localmente. Proposta de publicação em [docs/arquitetura.md](docs/arquitetura.md#próximos-passos-proposta-não-implementada).
5. **Front-end parcial em relação à API:** soft delete, cancelamento e paginação existem na API, mas ainda não têm tela.
6. **Número do pedido acima de 99.999 por ano:** o formato tem 5 dígitos; o 100.000º pedido do ano sairia com 6 (`PED-2026-100000`). Folga suficiente para o escopo acadêmico.
7. **Falha pouco clara sem as variáveis do banco:** sem `DB_USERNAME`/`DB_PASSWORD`, a API não sobe e o erro mostra o placeholder literal (`${DB_USERNAME}`). Ver "Problemas comuns".

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

   Commits devem conter apenas uma ideia, utilizando mensagens padronizadas (Conventional Commits):

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

6. **Segredos fora do Git:** senhas e tokens ficam em variáveis de ambiente ou no `.env` (ignorado pelo Git), nunca em arquivos versionados.

---

### Combinado da Equipe (Ética e Convivência)

1. **Transparência e Comunicação:** Alinhamento contínuo sobre o andamento das tarefas via GitHub e canal oficial de comunicação da equipe.

2. **Comprometimento com Prazos:** Cumprimento rigoroso do cronograma de entregas estabelecido no roadmap das aulas.

3. **Qualidade de Código:** Revisão criteriosa em todos os Pull Requests (*Code Review*) antes de realizar o *merge* na branch principal (`main`).

---

### Licença

Projeto estritamente acadêmico — **Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"**. Todos os direitos reservados aos autores e à instituição.
