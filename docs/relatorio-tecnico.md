# Relatório técnico: Sistema de Gestão de Pedidos

**Squad Swift:** Lucas Nunes Patracão e Rafael Rubiá Oliveira Cardoso
**Projeto Integrador:** Desenvolvimento Back-end, CST em Análise e Desenvolvimento de Sistemas (CSTADS601), Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"

Os números deste relatório vêm do `mvn clean verify` e do histórico do Git da branch `docs/entrega-final`.

## 1. Problema e escopo

Uma loja virtual precisa controlar clientes, catálogo, pedidos e pagamentos sem vender o que não tem em estoque e sem perder o histórico de vendas.

**Dentro do escopo:**

* API REST em Spring Boot com CRUD de clientes (CPF validado, e-mail único) e de produtos (estoque, versão otimista).
* Soft delete de clientes e produtos.
* Pedidos com vários itens e baixa de estoque transacional, além de cancelamento com devolução de estoque.
* Pagamento por Pix, Boleto ou Cartão (Vale-Compras existe no domínio, sem tela).
* Dashboard e histórico.
* Interface administrativa em React consumindo a API.
* Testes automatizados e pipeline de CI.

**Fora do escopo:** autenticação e autorização, gateway de pagamento real (o `GatewayPagamentoSimulado` autoriza tudo) e deploy em nuvem. A aplicação roda só localmente.

## 2. Arquitetura e decisões

Camadas: `controller` → `servico` (interface + `*Impl` com `@Transactional`) → `modelo` (entidades com as regras) → `repositorio` (Spring Data JPA) → MySQL 8. Os diagramas estão em [arquitetura.md](arquitetura.md).

| Decisão | Motivo |
| :--- | :--- |
| **JPA + Flyway, com `ddl-auto=validate`** | O schema é versionado em migrations SQL (`V1` a `V3`), revisáveis em PR e iguais em todo ambiente. O Hibernate só *confere* o schema; nunca cria tabelas. Um mapeamento errado quebra os testes antes de chegar ao MySQL. |
| **`InheritanceType.JOINED` em `FormaPagamento`** | Cada forma (Pix, Boleto, CartaoCredito, ValeCompras) tem campos próprios. Com JOINED, os campos comuns ficam numa tabela e os específicos em uma por subclasse: sem colunas nulas de outros tipos e com `NOT NULL` possível em cada uma. O serviço trabalha só com a abstração (polimorfismo, sem `instanceof`). |
| **DTOs (`record`) separados das entidades** | O JSON é um contrato fixo: renomear um atributo da entidade não quebra o front, e as relações do banco não vazam. Os *requests* carregam a Bean Validation (`@NotBlank`, `@Email`...), e o erro de validação volta campo a campo. |
| **Número do pedido por contador no banco** | `sequencia_pedido` (migration V3) guarda um contador por ano, lido com `PESSIMISTIC_WRITE` na mesma transação do pedido. Pedidos simultâneos esperam a vez e recebem números seguidos, e um pedido que falha não consome número. Uma tabela, e não `CREATE SEQUENCE`, porque o MySQL 8 não tem sequences. |
| **CORS centralizado (`config/CorsConfig`)** | Um `WebMvcConfigurer` libera `/api/**` para `localhost:5173` e `127.0.0.1:5173` e expõe o cabeçalho `Location`. Antes havia um `@CrossOrigin` repetido em 6 controllers, e uma origem nova exigiria mexer em todos. |
| **Erro padronizado (`ErroResponse`)** | Toda falha volta como `{ status, erro, mensagem, caminho, campos }` pelo `ApiExceptionHandler`. O controller fica sem `try/catch`, e o 500 nunca expõe classe, SQL ou stack trace. |
| **Bloqueio pessimista no produto durante a venda** | Duas vendas da última unidade acontecem uma depois da outra; a segunda recebe 409 com o saldo real. |

## 3. Qualidade

* **Testes:** 97 de unidade (Surefire, `mvn test`, sem Spring) e 71 de integração (Failsafe, `@Tag("integracao")`, contexto Spring + MockMvc + H2 no modo MySQL com as mesmas migrations). Nenhuma falha. Cobrem domínio, fluxo de compra, transação e concorrência, persistência (CASCADE, RESTRICT, UNIQUE, CHECK), a migration V3, consultas (`JOIN FETCH` medido pela contagem de SQL) e a API: status, `Location`, 204 e corpo de erro em 400, 404, 409 e 500.
* **Cobertura (JaCoCo, unidade + integração):** **96,9% das linhas** (896/925) e **90,9% dos ramos** (281/309). O build falha abaixo de 85% das linhas e 65% dos ramos no total, ou abaixo de 70% das linhas nos pacotes `modelo` e `excecao` só com os testes de unidade.
* **CI (GitHub Actions):** em todo PR e push na `main`. O job `backend` roda `mvn -B verify` com Java 17 e publica o relatório e o `.jar`; o job `frontend` roda `npm ci`, lint e build com Node 22. Sem MySQL e sem segredos.
* **Prova de que o CI reprova:** o PR #24 (`test/ci-vermelho`) trazia um teste com `assertEquals(1, 2)`, e o pipeline ficou vermelho como esperado. O PR, porém, foi **mergeado por engano**, e a `main` passou a falhar. A correção foi um `git revert` do merge, sem force push, no início da branch `docs/entrega-final`. A lição é que a regra "PR vermelho não é integrado" precisa ser imposta pelo GitHub: proteção da `main` exigindo o check `CI` antes do merge.

## 4. Dificuldades e como foram resolvidas

| Dificuldade | Causa | Solução (commit) |
| :--- | :--- | :--- |
| `Name for argument of type [java.lang.String] not specified` no PUT e no DELETE | O projeto não herda de `spring-boot-starter-parent`, então o `javac` não recebia `-parameters` e o Spring não sabia o nome do `@PathVariable`. | `maven.compiler.parameters=true` no `pom.xml`, nome explícito em todos os `@PathVariable` e teste de regressão com MockMvc (`402d446`, 26/11/2026). |
| Endereço do cliente gravado como `[object Object]` | A API devolve `endereco` como objeto `{ logradouro, cidade, cep }`, mas o request espera texto. Ao editar, o front reenviava o objeto, e o JavaScript o convertia em texto. | O front passou a extrair o `logradouro` antes de enviar (`normalizarPayloadCliente` em `clienteApi.js` e `extrairEnderecoTexto` no `ClienteForm`) (`5415c60`, 28/10/2026). |
| Colisão do número do pedido | O número era sorteado e sorteado de novo se já existisse. Dois pedidos simultâneos podiam sortear o mesmo e um falhava no `UNIQUE`. | Contador por ano com bloqueio de linha (seção 2), migration V3 que renumera os pedidos antigos e testes com 8 pedidos simultâneos (`67580c2`, 27/11/2026). |
| Botões ficando azuis ao clicar | O CSS do Bootstrap definia `.btn-primary`/`.btn-danger` em azul, com o **mesmo nome** das classes do projeto, nos estados `:active`/`:focus`. Nenhum componente do Bootstrap era usado. | Bootstrap removido do `package.json` e do `main.jsx`; os estilos ficaram só nos tokens do projeto (`14bd1fd`, 02/10/2026). |

## 5. Limitações conhecidas e próximos passos

**Limitações (estado atual):**

1. Sem autenticação: qualquer cliente HTTP chama a API.
2. Gateway de pagamento simulado (autoriza tudo).
3. Sem deploy em nuvem; o pipeline é só de integração contínua.
4. O front ainda não tem tela para recursos que a API oferece: inativar/reativar e listar inativos, cancelar pedido, remover item, excluir pedido, pedidos por cliente, filtros e paginação.
5. A `baseURL` do front é fixa (`http://localhost:8080/api`).
6. Sem as variáveis do banco, a API falha com o placeholder literal (`Access denied for user '${DB_USERNAME}'`), e não com uma mensagem clara.
7. `PedidoUtils` usa `double` (didático, Aula 03); o número do pedido tem 5 dígitos por ano.

**Próximos passos:**

1. Proteger a `main` no GitHub, exigindo o check `CI` e revisão antes do merge.
2. Spring Security com login.
3. Telas para os recursos já existentes na API.
4. `VITE_API_URL` no front.
5. Deploy gratuito (proposta em [arquitetura.md](arquitetura.md#próximos-passos-proposta-não-implementada)).
6. Falhar cedo, com mensagem clara, quando `DB_USERNAME`/`DB_PASSWORD` não estiverem definidas.
