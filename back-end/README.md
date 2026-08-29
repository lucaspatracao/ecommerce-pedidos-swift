# Back-end — E-commerce Pedidos Swift

Projeto de back-end em Java para gestão de pedidos de e-commerce, desenvolvido no contexto da disciplina de Desenvolvimento Back-end da Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe".

## Squad

- Lucas Nunes Patracão
- Rafael Rubiá Oliveira Cardoso

## Objetivo do projeto

O sistema tem como finalidade simular a gestão de um e-commerce, incluindo:

- cadastro de produtos;
- cadastro de clientes;
- criação de pedidos;
- inclusão de itens no pedido;
- cálculo do valor total;
- controle de estoque;
- alteração de situação do pedido;
- demonstração de regras de negócio orientadas a objetos.

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
│   │   │       ├── App.java
│   │   │       └── com/
│   │   │           └── ecommerce/
│   │   │               └── pedidos/
│   │   │                   ├── modelo/
│   │   │                   │   ├── Cliente.java
│   │   │                   │   ├── ItemPedido.java
│   │   │                   │   ├── Pedido.java
│   │   │                   │   ├── Produto.java
│   │   │                   │   └── SituacaoPedido.java
│   │   │                   └── swift/
│   │   │                       └── util/
│   │   │                           └── PedidoUtils.java
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── ecommerce/
│   │                   └── pedidos/
│   │                       └── swift/
│   │                           └── AppTest.java
│   └── target/
└── README.md
```

## Status atual

O backend já conta com a estrutura inicial de domínio implementada, incluindo classes principais para:

- `Produto`
- `Cliente`
- `Pedido`
- `ItemPedido`
- `SituacaoPedido`
- `PedidoUtils`

Além disso, a aplicação principal já executa uma simulação de fluxo de pedido com estoque, subtotal e mudança de situação.

## Como executar

A partir da pasta do projeto:

```bash
cd back-end/ecommerce-pedidos-swift
mvn compile
```

Para rodar a aplicação manualmente:

```bash
cd src/main/java
javac App.java
java App
```

Se quiser executar os testes:

```bash
mvn test
```

## Observações

Este é um projeto acadêmico em evolução. A estrutura atual está pronta para continuar com as próximas etapas do back-end, como:

- validações e exceções;
- testes unitários mais completos;
- pagamentos;
- persistência e integração com banco de dados;
- API REST.

## Roadmap de desenvolvimento

- [x] Estrutura inicial do projeto
- [x] Modelagem de domínio básica
- [x] Simulação de compra e controle de estoque
- [ ] Tratamento de exceções e validações
- [ ] Testes automatizados
- [ ] Persistência com banco de dados
- [ ] API REST e integração front-end