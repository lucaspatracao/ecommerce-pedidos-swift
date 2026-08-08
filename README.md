# Sistema de Gestão de Pedidos — E-commerce

> **Projeto Integrador** da Unidade Curricular: *Desenvolvimento Back-end* > **Curso:** Superior de Tecnologia em Análise e Desenvolvimento de Sistemas (Turma CSTADS601)  
> **Instituição:** Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"

---

## Equipe / Squad: Swift

| Nome | Papel na Aula 01 |
|---|---|
| **Lucas Nunes Patracão** | Responsável do dia |
| **Rafael Rubiá Oliveira Cardoso** | Desenvolvedor / Colaborador |

---

## Descrição do desafio

O objetivo principal deste projeto é conceber e implementar uma solução completa de **Back-end para Gestão de Pedidos em E-commerce**. 

A aplicação engloba a modelagem de domínio, regras de negócio e infraestrutura para administrar o cadastro de clientes e catálogo de produtos, gerenciar todo o ciclo de vida dos pedidos e processar transações financeiras com múltiplos métodos de pagamento, garantindo confiabilidade, segurança e alta manutenibilidade do código.

---

## Funcionalidades previstas

- [ ] **Gerenciamento de Produtos:** Cadastro, atualização, consulta e controle de disponibilidade.
- [ ] **Gerenciamento de Clientes:** Cadastro, edição de dados cadastrais e histórico de compras.
- [ ] **Gestão de Pedidos:** Abertura, inclusão de itens, cálculo de totais, alteração de status e cancelamento.
- [ ] **Processamento de Pagamentos:** Suporte a múltiplos métodos (Cartão de Crédito, Boleto Bancário e Pix).
- [ ] **Garantia de Qualidade:** Testes automatizados unitários e de integração com relatórios de cobertura.
- [ ] **Integração Contínua (CI/CD):** Pipeline para execução de build e testes automatizados.
- [ ] **API RESTful:** Endpoints bem estruturados para integração e consumo por aplicações Front-end/Mobile.

---

## Tecnologias

- **Linguagem:** Java (versão LTS)
- **Gerenciador de Build e Dependências:** Apache Maven
- **Controle de Versão e Colaboração:** Git & GitHub
- _(Demais ferramentas e frameworks integrados ao longo do semestre: JUnit 5, Spring Boot, Banco de Dados Relacional, GitHub Actions, etc.)_

---

## Estrutura de pastas


```

ecommerce-pedidos-swift/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/senai/ecommerce/
│   │           ├── modelo/         # Entidades e classes de domínio
│   │           ├── servico/        # Regras de negócio e casos de uso
│   │           ├── repositorio/    # Acesso a dados e persistência (DAO/Repository)
│   │           └── util/           # Classes utilitárias e auxiliares
│   └── test/
│       └── java/
│           └── com/senai/ecommerce/  # Suíte de testes unitários e de integração
├── pom.xml                         # Configuração e dependências do Maven
├── README.md                       # Documentação principal do repositório
└── .gitignore                      # Arquivos e diretórios ignorados pelo Git

```

---

## Como rodar o projeto

> *Seção em construção. As instruções detalhadas de configuração, compilação e execução da aplicação serão adicionadas nas próximas etapas do desenvolvimento.*

---

## Roadmap do projeto (por aula)

| Aula | Entrega Prevista | Status |
|:---:|---|:---:|
| **01** | Repositório criado, estruturado, com README e commit inicial | 🟢 Concluído |
| **02** | Fluxo de branches e primeiro Pull Request revisado | ⏳ Pendente |
| **03** | Classe utilitária (`Utils`) do domínio | ⏳ Pendente |
| **04** | Classes de domínio inicial (`Produto`, `Cliente`, `Pedido`, `ItemPedido`) | ⏳ Pendente |
| **05** | Encapsulamento e abstração aplicados | ⏳ Pendente |
| **06** | Hierarquia de formas de pagamento (herança) | ⏳ Pendente |
| **07** | Relacionamentos entre classes do domínio | ⏳ Pendente |
| **08** | Módulo de pagamento polimórfico | ⏳ Pendente |
| **09** | Tratamento de exceções e validações | ⏳ Pendente |
| **10** | Suíte de testes unitários | ⏳ Pendente |
| **11** | Suíte de testes de integração + relatório de cobertura | ⏳ Pendente |
| **12** | Persistência: conexão, operações Create e Read | ⏳ Pendente |
| **13** | Persistência: operações Update, Delete e padrão DAO/Repository | ⏳ Pendente |
| **14** | Migração e estruturação com Spring Boot | ⏳ Pendente |
| **15** | API REST completa + pipeline de CI/CD | ⏳ Pendente |
| **16** | Entrega final, documentação consolidada e apresentação | ⏳ Pendente |

---

## Combinado da equipe (ética e convivência)

1. **Transparência e Comunicação:** Alinhamento contínuo sobre o andamento das tarefas via GitHub e canal oficial de comunicação da equipe.
2. **Comprometimento com Prazos:** Cumprimento rigoroso do cronograma de entregas estabelecido no roadmap das aulas.
3. **Qualidade de Código:** Revisão criteriosa em todos os Pull Requests (Code Review) antes de realizar o *merge* na branch principal (`main`).

---

## Licença

Projeto estritamente acadêmico — **Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"**. Todos os direitos reservados aos autores e à instituição.