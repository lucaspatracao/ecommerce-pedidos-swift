# Back-end — E-commerce Pedidos Swift

Projeto de back-end em Java para gestão de pedidos de e-commerce, desenvolvido no contexto da disciplina de Desenvolvimento Back-end da Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe".

## Squad

- Lucas Nunes Patracão
- Rafael Rubiá Oliveira Cardoso

## Objetivo do projeto

O sistema simula a gestão básica de um e-commerce, com foco em modelagem de domínio, regras de negócio, encapsulamento, herança, composição e exemplos didáticos de camadas organizadas em Java.

Funcionalidades implementadas (exemplos):

- cadastro de produtos com validação e controle de estoque;
- cadastro de clientes com validação de campos básicos (CPF, e-mail);
- criação de pedidos e associação de itens ao pedido;
- cálculo de subtotais e total do pedido usando `BigDecimal` no domínio;
- criação da hierarquia de Pessoa com a nova classe `Funcionario` e a extensão de domínio com `Cliente`;
- abstração de pagamento com `FormaPagamento`, e implementações concretas `Pix`, `Boleto` e `CartaoCredito` usando herança e polimorfismo;
- regras de negócio demonstrativas (situação do pedido, baixa de estoque, processamento de pagamentos);
- utilitários para geração de número de pedido, cálculo de frete e formatação de recibo (`PedidoUtils`).

## Tecnologias

- Java 17
- Maven
- JUnit 5
- Spring Boot 3
- Estrutura de camadas com `model`, `service`, `repository` e `controller`

## Estrutura do projeto

```text
back-end/
├── ecommerce-pedidos-swift/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/ecommerce/pedidos/
│   │   │           ├── model/        # Pessoa, Cliente, Funcionario, Produto, Pedido, ItemPedido, FormaPagamento, Pix, Boleto, CartaoCredito
│   │   │           ├── service/      # PagamentoService
│   │   │           ├── repository/   # FormaPagamentoRepository
│   │   │           ├── controller/   # PagamentoController
│   │   │           └── swift/        # Aplicacao e utilitários
│   │   └── test/
│   │       └── java/
│   │           └── com/ecommerce/pedidos/model/   # testes de herança e pagamento
│   └── target/
└── README.md
```

## Status atual

A versão atual do back-end já incorpora importantes evoluções:

- Classes de domínio implementadas com validações e encapsulamento (`Produto`, `Cliente`, `Pedido`, `ItemPedido`, `Pessoa`, `Funcionario`).
- Uso de `BigDecimal` nas entidades para representar valores monetários (evita problemas de precisão no domínio).
- `Pedido.getItens()` retorna uma lista imutável (proteção contra modificações externas).
- `FormaPagamento` passa a ser uma abstração com atributo comum (`valor`, `dataDoPagamento`, `situacao`) e `processar()` como método abstrato.
- `Pix`, `Boleto` e `CartaoCredito` sobrescrevem `processar()` e reaproveitam o `getResumo()` com `super.getResumo()`.
- `PagamentoService` e `FormaPagamentoRepository` organizam a camada de regras de negócio e acesso ao dado em memória.
- `Aplicacao` demonstra a aplicação de herança com `Funcionario` e o polimorfismo com a lista de pagamentos de `FormaPagamento`.

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

- O modelo de pagamento foi estendido com herança e polimorfismo (`FormaPagamento` + filhas).
- A estrutura de aplicação agora contempla convenções de camada em estilo Spring Boot, com `repository`, `service` e `controller`.
- A classe `Aplicacao` continua como exemplo de uso manual para demonstração; a evolução natural é a exposição de uma API REST e a integração com persistência.

## Roadmap de desenvolvimento (atualizado)

- [x] Estrutura inicial do projeto
- [x] Modelagem de domínio básica (Produto, Cliente, Pedido, ItemPedido)
- [x] Simulação de compra, controle de estoque e validações (Aula 04 e 05)
- [x] Hierarquia de classes de pagamento com `FormaPagamento`, `Pix`, `Boleto` e `CartaoCredito` (Aula 06)
- [ ] Tratamento avançado de exceções e validações adicionais
- [ ] Testes automatizados mais completos
- [ ] Persistência com banco de dados
- [ ] API REST e integração front-end