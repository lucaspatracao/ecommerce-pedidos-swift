# Arquitetura e execução

Diagramas do Sistema de Gestão de Pedidos (squad Swift). Todos em Mermaid, que o GitHub desenha direto nesta página. Os nomes de classes, rotas e arquivos são os do código atual.

## 1. Arquitetura em camadas

```mermaid
flowchart TD
    FE["Front-end React + Vite<br/>front-end/src/api/clienteApi.js (Axios)"]

    subgraph API["Back-end Spring Boot · porta 8080 · prefixo /api"]
        CORS["config/CorsConfig<br/>(WebMvcConfigurer: localhost:5173 e 127.0.0.1:5173)"]
        CTRL["controller/<br/>ClienteController · ProdutoController · PedidoController<br/>PagamentoController · DashboardController · HistoricoController"]
        EH["controller/ApiExceptionHandler<br/>(@RestControllerAdvice → ErroResponse)"]
        DTO["dto/<br/>*Request (Bean Validation) · *Response · ErroResponse"]
        SVC["servico/<br/>interfaces ClienteService, ProdutoService, PedidoService, PagamentoService, DashboardService<br/>+ implementações *ServiceImpl (@Transactional)"]
        DOM["modelo/<br/>entidades JPA com as regras de negócio"]
        REP["repositorio/<br/>interfaces Spring Data JPA (@Query, JOIN FETCH, @Lock)"]
    end

    DB[("MySQL 8<br/>schema versionado pelo Flyway (V1 a V3)")]

    FE -- "HTTP + JSON" --> CORS --> CTRL
    CTRL -- "recebe/devolve" --> DTO
    CTRL -- "delega" --> SVC
    SVC --> DOM
    SVC --> REP
    REP --> DB
    CTRL -. "exceções sobem" .-> EH
    SVC -. "exceções sobem" .-> EH
    EH -- "4xx/5xx + ErroResponse" --> FE
```

* Os controllers só traduzem HTTP: recebem o DTO validado (`@Valid`), chamam o serviço e devolvem DTO. Não acessam repositório nem têm `try/catch`.
* As regras ficam no domínio (ex.: `Pedido.pagarCom`, `Produto.baixarEstoque`) e na orquestração dos serviços.
* Toda exceção vira resposta no `ApiExceptionHandler`, com o corpo `{ status, erro, mensagem, caminho, campos }`.

## 2. Modelo de domínio

```mermaid
classDiagram
    direction LR

    class Pessoa {
        <<abstract · @MappedSuperclass>>
        nome
        documento
    }
    class Cliente {
        <<@Entity>>
        id
        email
        telefone
        ativo
    }
    class Funcionario {
        <<classe comum, não persistida>>
        cargo
    }
    class Endereco {
        <<@Embeddable>>
        logradouro
        cidade
        cep
    }
    class Produto {
        <<@Entity>>
        id
        codigo
        nome
        preco
        quantidadeEmEstoque
        ativo
        versao (@Version)
    }
    class Pedido {
        <<@Entity>>
        id
        numero
        data
        situacao: SituacaoPedido
    }
    class ItemPedido {
        <<@Entity>>
        id
        quantidade
        precoPraticado
    }
    class FormaPagamento {
        <<abstract · @Inheritance(JOINED)>>
        id
        valor
        dataDoPagamento
        situacao: SituacaoPagamento
    }
    class Pix
    class Boleto
    class CartaoCredito
    class ValeCompras
    class SequenciaPedido {
        <<@Entity · contador por ano>>
        ano
    }

    Pessoa <|-- Cliente
    Pessoa <|-- Funcionario
    Cliente *-- Endereco : embutido
    Cliente "1" <-- "0..*" Pedido : cliente_id
    Pedido "1" *-- "1..*" ItemPedido : cascade ALL + orphanRemoval
    ItemPedido "0..*" --> "1" Produto : produto_id
    Pedido "1" --> "0..1" FormaPagamento : forma_pagamento_id (unique)
    FormaPagamento <|-- Pix
    FormaPagamento <|-- Boleto
    FormaPagamento <|-- CartaoCredito
    FormaPagamento <|-- ValeCompras
```

Notas que vêm do código:

* `Pessoa` é `@MappedSuperclass`: não tem tabela própria, e os campos dela vão para a tabela `cliente`. `Funcionario` herda de `Pessoa`, mas **não** é entidade (não tem tabela nem rota).
* `FormaPagamento` usa `InheritanceType.JOINED`: uma tabela com os campos comuns e uma tabela por subclasse.
* Um pedido nasce com pelo menos um item (`POST /api/pedidos` com `itens` vazio → 400). Depois de criado, o item pode ser removido enquanto o pedido estiver `ABERTO`.
* O pagamento é 0..1: o pedido nasce sem pagamento e recebe no máximo um (`forma_pagamento_id` é `unique`).
* `SequenciaPedido` não se liga às outras entidades por chave: é o contador que gera o `numero` do pedido (migration V3).

## 3. Sequência do fluxo principal

O estoque **baixa na criação do pedido**, e não no pagamento. São duas transações separadas.

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário (front)
    participant PC as PedidoController
    participant PS as PedidoServiceImpl
    participant PG as PagamentoController
    participant GS as PagamentoServiceImpl
    participant GW as GatewayPagamento
    participant DB as MySQL

    U->>PC: POST /api/pedidos {clienteCpf, itens}
    PC->>PS: criar(request)
    rect rgb(255, 243, 224)
    note over PS,DB: Transação 1 (@Transactional em criar)
    PS->>DB: busca cliente ativo por CPF
    PS->>DB: trava o contador do ano (SELECT ... FOR UPDATE)
    loop cada item
        PS->>DB: busca produto COM BLOQUEIO (PESSIMISTIC_WRITE)
        PS->>PS: pedido.adicionarItem → produto.baixarEstoque<br/>(sem estoque → EstoqueInsuficienteException, 409)
    end
    PS->>DB: saveAndFlush(pedido), itens em cascata
    end
    PS-->>PC: PedidoResponse
    PC-->>U: 201 + Location /api/pedidos/{numero}

    U->>PG: POST /api/pedidos/{numero}/pagamento {tipo, valor, ...}
    PG->>GS: registrar(numero, request)
    rect rgb(224, 242, 255)
    note over GS,DB: Transação 2 (@Transactional em registrar)
    GS->>GS: FabricaFormaPagamento.criar (Pix, Boleto, Cartão...)
    GS->>DB: busca pedido por número
    GS->>GS: pedido.pagarCom: valor = total? pedido ABERTO?<br/>(já pago → PedidoJaPagoException, 409)
    GS->>GW: autorizar(pagamento)
    alt recusado
        GW-->>GS: false
        GS-->>PG: PagamentoRecusadoException → rollback, pedido continua ABERTO
    else autorizado
        GW-->>GS: true
        GS->>DB: commit: pedido PAGO + pagamento (cascade)
    end
    end
    GS-->>PG: detalhe do pagamento
    PG-->>U: 201 + Location /api/pedidos/{numero}/pagamento
```

* Se qualquer item falhar na transação 1, nada é gravado: nem estoque, nem pedido, nem o número do contador.
* Cancelar (`PATCH /api/pedidos/{numero}` com `{"situacao":"CANCELADO"}`) devolve o estoque e, se o pedido estava pago, estorna o pagamento.

## 4. Execução e CI

```mermaid
flowchart LR
    subgraph LOCAL["Máquina do desenvolvedor"]
        NAV["Navegador"]
        VITE["Front-end<br/>npm run dev · porta 5173"]
        BOOT["API Spring Boot<br/>mvn spring-boot:run · porta 8080<br/>perfil dev"]
        MY[("MySQL 8 · porta 3306<br/>banco ecommerce_swift")]
        NAV --> VITE
        NAV -- "/api (Axios)" --> BOOT
        BOOT --> MY
    end

    subgraph GH["GitHub Actions · .github/workflows/ci.yml<br/>push na main e todo pull_request"]
        direction TB
        BE["job backend<br/>ubuntu-latest · Java 17 Temurin<br/>mvn -B verify (H2, perfil test)<br/>artefatos: jacoco + .jar"]
        FJ["job frontend<br/>ubuntu-latest · Node 22<br/>npm ci · lint · build"]
    end

    DEV["git push / PR"] --> GH
```

* No CI não há MySQL nem segredos: os testes usam o perfil `test`, com H2 em memória (`MODE=MySQL`) e as mesmas migrations do Flyway.
* **Não existe deploy em nuvem.** O projeto roda só localmente. O pipeline faz integração contínua (testes e build), mas não publica a aplicação em nenhum servidor. Os artefatos ficam só para download na execução do Actions.

## Próximos passos (proposta, não implementada)

Uma opção viável e gratuita para publicar a aplicação:

1. **Front-end:** publicar o `front-end/dist/` no GitHub Pages, por um job a mais no workflow que rode só na `main`. Antes, a `baseURL` fixa (`http://localhost:8080/api` em `clienteApi.js`) teria de virar uma variável de build (`import.meta.env.VITE_API_URL`).
2. **Back-end:** gerar uma imagem Docker do `.jar` e publicar num serviço com plano gratuito para contêineres (ex.: Render), com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` como variáveis de ambiente do serviço. Nunca no repositório.
3. **Banco:** um MySQL gerenciado com plano gratuito (ex.: Aiven). As migrations do Flyway criam o schema na primeira subida.
4. **CORS:** incluir a origem publicada do front no `CorsConfig`.

Limites e condições dos planos gratuitos mudam com frequência. Confira antes de escolher. Planos gratuitos costumam "dormir" sem uso, então a primeira requisição pode demorar.
