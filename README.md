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

### Funcionalidades

* **Gerenciamento de Produtos:** cadastro, atualização, consulta, exclusão e controle de disponibilidade e estoque.
* **Gerenciamento de Clientes:** cadastro com CPF validado por dígitos verificadores, edição e exclusão.
* **Gestão de Pedidos:** abertura com vários itens, cálculo de totais, baixa de estoque transacional, cancelamento e exclusão.
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
* **Testes:** JUnit 5, Mockito, MockMvc, H2 em memória (modo MySQL) e **JaCoCo** (cobertura)
* **Front-end:** React + Vite (Axios para a API)
* **Controle de versão:** Git & GitHub
* **CI/CD:** GitHub Actions — previsto no roadmap

---

### Como Rodar o Projeto

#### Pré-requisitos

| Ferramenta | Versão |
| :--- | :--- |
| JDK | 17 |
| Maven | 3.9+ |
| MySQL | 8.x |
| Node.js | 18+ (para o front-end) |

#### 1. Criar o banco de desenvolvimento (uma vez)

O perfil `dev` usa um banco **novo e dedicado**, chamado `ecommerce_swift`. As tabelas **não** são criadas à mão: o Flyway cria tudo na primeira subida da API.

```powershell
mysql -u root -p -e "CREATE DATABASE ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### 2. Configurar a conexão (variáveis de ambiente)

A API lê a conexão de três variáveis. **Nenhuma senha fica no repositório**: `DB_PASSWORD` não tem valor padrão, e sem ela a aplicação não sobe.

| Variável | Obrigatória? | Exemplo |
| :--- | :--- | :--- |
| `DB_URL` | Não no perfil `dev` (padrão: `localhost:3306/ecommerce_swift`) | `jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | Sim | `root` |
| `DB_PASSWORD` | Sim | *(a sua senha local)* |

**Opção A: variáveis na sessão do PowerShell** (valem até fechar o terminal):

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua-senha-local"

# Só se o MySQL NÃO estiver em localhost:3306. Exemplo: instância na porta 3307
$env:DB_URL = "jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
```

**Opção B: arquivo `.env`** (não precisa repetir a cada terminal). Copie o modelo e preencha a senha:

```powershell
cd back-end\ecommerce-pedidos-swift
Copy-Item .env.example .env
notepad .env
```

O `.env` está no `.gitignore` e nunca é versionado. Se a mesma variável existir no PowerShell e no `.env`, vale a do PowerShell.

#### 3. Subir o back-end

```powershell
cd back-end\ecommerce-pedidos-swift
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

> `dev` já é o perfil padrão (`spring.profiles.default=dev`), então `mvn spring-boot:run` também funciona. O parâmetro explícito deixa claro qual perfil está ativo. No PowerShell, as **aspas** são obrigatórias por causa do `-D` com ponto.

Na primeira subida o log mostra `Successfully applied 1 migration`. A API fica em:

```text
http://localhost:8080/api
```

#### 4. Subir o front-end

Em outro terminal:

```powershell
cd front-end
npm install
npm run dev
```

Acesse o endereço exibido pelo Vite (normalmente `http://localhost:5173`). O back-end libera CORS para essa origem.

#### 5. Rodar os testes

```powershell
cd back-end\ecommerce-pedidos-swift
mvn clean test
```

* Os testes usam o perfil `test`: **H2 em memória** no modo MySQL, com as **mesmas migrations** do Flyway. Não precisam de MySQL nem de variáveis de ambiente.
* O **JaCoCo** exige no mínimo **70% das linhas cobertas** no projeto inteiro. Abaixo disso, `mvn clean test` **falha**.
* Relatório de cobertura: abra `back-end/ecommerce-pedidos-swift/target/site/jacoco/index.html` no navegador.

---

### Perfis de Execução

| Perfil | Banco | Para quê | Arquivo |
| :--- | :--- | :--- | :--- |
| `dev` (padrão) | MySQL `ecommerce_swift` | Desenvolvimento local; mostra o SQL formatado no console (`show-sql`, `format_sql`) | `src/main/resources/application-dev.properties` |
| `test` | H2 em memória, `MODE=MySQL` | Testes automatizados (`@ActiveProfiles("test")`) | `src/test/resources/application-test.properties` |

Configurações comuns (`application.properties`): `ddl-auto=validate` (o Hibernate só confere o schema criado pelo Flyway; nunca cria tabelas), `open-in-view=false` e leitura de `${DB_URL}`, `${DB_USERNAME}` e `${DB_PASSWORD}`.

---

### Arquitetura (Aula 14)

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

* **DTOs** (`dto/`): os *requests* têm Bean Validation (`@NotBlank`, `@Email`, `@Positive`...). Os *responses* fixam o JSON que o front-end consome, sem expor ids nem relações do banco.
* **Erros** (`ApiExceptionHandler`): todo erro volta como `{ "erro": "mensagem" }`, que é a chave lida pelo front.
* **Transação**: criar um pedido baixa o estoque de todos os itens numa transação só. Se qualquer item falhar, nada é gravado (rollback).
* **Consultas**: `@Query` em JPQL (cliente por CPF, estoque crítico, receita via `SUM`) e `JOIN FETCH` no `PedidoRepository`, que carrega pedido, cliente, itens, produtos e pagamento numa única consulta e evita o problema N+1.

---

### API REST

Base: `http://localhost:8080/api`

| Método | Rota | Descrição | Sucesso | Erros comuns |
| :--- | :--- | :--- | :---: | :--- |
| GET | `/clientes` | Lista clientes | 200 | |
| GET | `/clientes/{cpf}` | Busca cliente | 200 | 404 |
| POST | `/clientes` | Cadastra cliente | 201 | 400 (validação/CPF), 409 (CPF duplicado) |
| PUT | `/clientes/{cpf}` | Atualiza nome e contato | 200 | 400, 404 |
| DELETE | `/clientes/{cpf}` | Exclui cliente sem pedidos | 204 | 404, 409 (tem pedidos) |
| GET | `/produtos` | Lista produtos | 200 | |
| GET | `/produtos/{codigo}` | Busca produto | 200 | 404 |
| POST | `/produtos` | Cadastra produto | 201 | 400, 409 (código duplicado) |
| PUT | `/produtos/{codigo}` | Atualiza produto | 200 | 400, 404, 409 |
| DELETE | `/produtos/{codigo}` | Exclui produto nunca vendido | 204 | 404, 409 (já vendido: desative-o) |
| GET | `/pedidos` | Lista pedidos (mais recentes primeiro) | 200 | |
| GET | `/pedidos/{numero}` | Busca pedido completo | 200 | 404 |
| POST | `/pedidos` | Cria pedido e baixa o estoque | 201 | 400, 404, 409 (estoque/produto inativo) |
| PUT | `/pedidos/{numero}/status` | `CANCELADO` devolve estoque e estorna pagamento | 200 | 400, 404, 409 |
| DELETE | `/pedidos/{numero}` | Exclui pedido não pago (aberto devolve estoque) | 204 | 404, 409 (pago) |
| GET | `/pagamentos` | Lista pagamentos | 200 | |
| POST | `/pagamentos` | Paga um pedido aberto | 201 | 400, 404, 409 (recusado/valor ≠ total) |
| GET | `/dashboard` | Indicadores | 200 | |
| GET | `/historico` | Últimos 10 pedidos | 200 | |

Exemplo de criação de pedido:

```json
POST /api/pedidos
{ "clienteCpf": "78617768916", "itens": [ { "codigo": "CAM-001", "quantidade": 2 } ] }
```

Exemplo de pagamento (campos usados dependem do `tipo`):

```json
POST /api/pagamentos
{ "tipo": "PIX", "valor": "99.80", "chave": "cliente@email.com", "tipoChave": "EMAIL", "pedidoNumero": "PED-2026-01234" }
{ "tipo": "BOLETO", "valor": "99.80", "vencimento": "2026-12-31", "codigo": "34191.79001", "pedidoNumero": "..." }
{ "tipo": "CARTAO", "valor": "99.80", "numero": "**** 4242", "bandeira": "Visa", "parcelas": 3, "pedidoNumero": "..." }
```

> Os CPFs deste README e dos testes são **sintéticos**, gerados com dígitos verificadores válidos apenas para exemplo. Não use CPFs reais em testes.

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
| `RecursoNaoEncontradoException` / `ClienteNaoEncontradoException` | cliente, produto ou pedido inexistente | 404 |
| `ConflitoException` | duplicado, produto vendido, cliente com pedidos | 409 |
| `EstoqueInsuficienteException` (código, solicitada, disponível) | estoque não basta | 409 |
| `PedidoInvalidoException` | pagar pedido vazio ou já pago | 409 |
| `PagamentoRecusadoException` (forma, motivo) | limite do cartão ou gateway recusou | 409 |
| `IllegalArgumentException` / Bean Validation | dado inválido | 400 |
| `IllegalStateException` | operação incompatível com o estado | 409 |
| qualquer outra | erro inesperado (detalhe só no log) | 500 |

#### Testes (Aulas 10, 11 e 14)

| Tipo | Exemplos | Spring? |
| :--- | :--- | :---: |
| Unitários | `ProdutoTest`, `ClienteTest`, `PedidoTest`, `ItemPedidoTest`, `FormasPagamentoTest`, `UtilsTest` | Não |
| Integração (fluxo) | `FluxoCompraIntegracaoTest`: cadastro → pedido → pagamento com classes reais; Mockito só no `GatewayPagamento` (sistema externo) | Sim |
| Transação | `TransacaoPedidoTest`: falha no 2º item desfaz a baixa de estoque do 1º | Sim |
| Consultas | `RepositoriosConsultaTest`: `@Query` e prova do `JOIN FETCH` contando os SQLs | Sim |
| API | `ApiIntegracaoTest`: MockMvc com rotas, status e corpo `{ "erro" }` | Sim |

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
│       │   └── db/migration/             # V1__esquema_inicial.sql (Flyway)
│       └── test/
│           ├── java/...                  # unitários, integração e API
│           └── resources/application-test.properties
├── front-end/                            # React + Vite (src/api, src/pages, src/components)
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
| **11** | Testes de integração + cobertura (JaCoCo ≥ 70%) | 🟢 Concluído |
| **12** | Persistência: conexão, Create e Read (Spring Data JPA) | 🟢 Concluído |
| **13** | Persistência: Update, Delete e Repository | 🟢 Concluído |
| **14** | Spring Boot: camadas, DTOs, validação, perfis, Flyway, `@Query`, `JOIN FETCH` | 🟢 Concluído |
| **15** | API REST completa + integração com Front-end + pipeline de CI/CD | 🟡 Em desenvolvimento |
| **16** | Entrega final, documentação consolidada e apresentação | ⏳ Pendente |

---

### Dívidas Técnicas Registradas

1. **`PedidoUtils` com `double`:** os cálculos de frete e desconto da Aula 03 continuam em `double` por fins didáticos. O domínio (preços, totais, pagamentos) já usa `BigDecimal`.
2. **Autenticação e autorização:** a API ainda não exige login.
3. **Gateway de pagamento simulado:** `GatewayPagamentoSimulado` autoriza tudo. Uma integração real implementaria a interface `GatewayPagamento`.
4. **CI/CD:** pipeline do GitHub Actions ainda não configurado.

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
