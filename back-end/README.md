# Back-end — E-commerce Pedidos Swift

Projeto de back-end em Java para gestão de pedidos de e-commerce, desenvolvido no contexto da disciplina de Desenvolvimento Back-end da Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe".

## Squad

- Lucas Nunes Patracão
- Rafael Rubiá Oliveira Cardoso

## Objetivo do projeto

O sistema simula a gestão básica de um e-commerce, com foco em modelagem de domínio, regras de negócio e exemplos didáticos de boas práticas de encapsulamento e validação.

Funcionalidades implementadas (exemplos):

- cadastro de produtos com validação e controle de estoque;
- cadastro de clientes com validação de campos básicos (CPF, e-mail);
- criação de pedidos e associação de itens ao pedido;
- cálculo de subtotais e total do pedido usando `BigDecimal` no domínio;
- regras de negócio demonstrativas (situação do pedido, baixa de estoque);
- utilitários para geração de número de pedido, cálculo de frete e formatação de recibo (classe `PedidoUtils`).

## Tecnologias

- Java 17
- Maven
- JUnit 5

## Estrutura do projeto

```text
back-end/
├── ecommerce-pedidos-swift/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/ecommerce/pedidos/
│   │   │           ├── model/        # classes de domínio (Produto, Cliente, Pedido, ItemPedido, SituacaoPedido)
│   │   │           └── swift/
│   │   │               └── util/     # utilitários (PedidoUtils)
│   │   └── test/
│   └── target/
└── README.md
```

## Status atual

A versão atual do back-end já incorpora importantes evoluções:

- Classes de domínio implementadas com validações e encapsulamento (`Produto`, `Cliente`, `Pedido`, `ItemPedido`).
- Uso de `BigDecimal` nas entidades para representar valores monetários (evita problemas de precisão no domínio).
- `Pedido.getItens()` retorna uma lista imutável (proteção contra modificações externas).
- `PedidoUtils` contém utilitários para geração de número de pedido, cálculos de frete e montagem de recibos; atualmente usa tipos primitivos (`double`) internamente para cálculos utilitários — essa parte permanece como dívida técnica a ser migrada para `BigDecimal`.
- `Aplicacao` (classe principal) contém demonstrações de validação, encapsulamento e tratamento de erros (ex.: tentativas de criar produtos inválidos, baixas de estoque inválidas, validação de e-mail).

## Como executar

1. Navegue até o diretório do projeto Maven Back-end:

```bash
cd back-end/ecommerce-pedidos-swift
```

2. Compile o projeto utilizando o Apache Maven:

```bash
mvn clean compile
```

3. Execute a aplicação de demonstração (`Aplicacao`):

```bash
mvn exec:java -Dexec.mainClass="com.ecommerce.pedidos.swift.Aplicacao"
```

4. Para executar os testes:

```bash
mvn test
```

## Observações e próximas tarefas

- Embora o domínio já utilize `BigDecimal`, a classe utilitária `PedidoUtils` ainda opera com `double` e formatações com `String`. Planeja-se migrar as operações financeiras utilitárias para `BigDecimal` para garantir consistência em toda a aplicação.
- A classe `Aplicacao` é um exemplo de uso manual para demonstração; a evolução natural é expor uma API REST e integrar persistência.

## Roadmap de desenvolvimento (atualizado)

- [x] Estrutura inicial do projeto
- [x] Modelagem de domínio básica (Produto, Cliente, Pedido, ItemPedido)
- [x] Simulação de compra, controle de estoque e validações (Aula 04 e 05)
- [ ] Tratamento avançado de exceções e validações adicionais
- [ ] Testes automatizados mais completos
- [ ] Persistência com banco de dados
- [ ] API REST e integração front-end