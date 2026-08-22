# Sistema de Gestão de Pedidos — E-commerce

> **Projeto Integrador** da Unidade Curricular: *Desenvolvimento Back-end*
> 
> **Curso:** Superior de Tecnologia em Análise e Desenvolvimento de Sistemas (Turma CSTADS601)
> 
> **Instituição:** Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"

---

### Equipe / Squad: Swift

| Nome | Papel na Aula 01 | Módulos sob Responsabilidade (Aula 02) |
| :--- | :--- | :--- |
| **Lucas Nunes Patracão** | Responsável do dia | Módulo de Pedido, Itens do Pedido e Utilitários (`feature/gestao-pedido`, `feature/classe-utilitaria`) |
| **Rafael Rubiá Oliveira Cardoso** | Desenvolvedor / Colaborador | Módulo de Cadastro de Produtos e Cadastro de Clientes (`feature/cadastro-cliente`, `feature/classe-utilitaria`) |

---

### Descrição do Desafio
O objetivo principal deste projeto é conceber e implementar uma solução completa de **Back-end para Gestão de Pedidos em E-commerce**.  
A aplicação engloba a modelagem de domínio, regras de negócio e infraestrutura para administrar o cadastro de clientes e catálogo de produtos, gerenciar todo o ciclo de vida dos pedidos e processar transações financeiras com múltiplos métodos de pagamento, garantindo confiabilidade, segurança e alta manutenibilidade do código.

---

### Funcionalidades Previstas
*   **Gerenciamento de Produtos:** Cadastro, atualização, consulta e controle de disponibilidade.
*   **Gerenciamento de Clientes:** Cadastro, edição de dados cadastrais e histórico de compras.
*   **Gestão de Pedidos:** Abertura, inclusão de itens, cálculo de totais, alteração de status e cancelamento.
*   **Processamento de Pagamentos:** Suporte a múltiplos métodos (Cartão de Crédito, Boleto Bancário e Pix).
*   **Garantia de Qualidade:** Testes automatizados unitários e de integração com relatórios de cobertura.
*   **Integração Contínua (CI/CD):** Pipeline para execução de build e testes automatizados.
*   **API RESTful:** Endpoints bem estruturados para integração e consumo por aplicações Front-end/Mobile.

---

### Tecnologias
*   **Linguagem:** Java (versão LTS)
*   **Gerenciador de Build e Dependências:** Apache Maven
*   **Controle de Versão e Colaboração:** Git & GitHub
*   *(Demais ferramentas e frameworks integrados ao longo do semestre: JUnit 5, Spring Boot, Banco de Dados Relacional, GitHub Actions, etc.)*

---

### Regras de Negócio e Constantes (Aula 03)
Definimos e documentamos as seguintes regras e limites financeiros para o funcionamento da nossa aplicação:

| Constante | Valor Adotado | Regra de Negócio |
| :--- | :--- | :--- |
| `VALOR_POR_QUILO` | R$ 7,50 | Preço cobrado por quilo (ou fração) transportado. |
| `FRETE_MINIMO` | R$ 15,00 | Valor mínimo de frete, independente do peso total do carrinho. |
| `VALOR_FRETE_GRATIS` | R$ 300,00 | A partir deste subtotal, o valor do frete é zerado. |
| `TAXA_DESCONTO` | 10% (0.10) | Percentual de desconto padrão aplicado sobre o subtotal da compra. |
| `DESCONTO_MAXIMO` | R$ 50,00 | Teto máximo de desconto concedido por pedido. |

---

### Dívidas Técnicas Registradas
1. **Precisão de Valores Monetários (`double` vs `BigDecimal`):** Os valores monetários de subtotal, frete e desconto estão temporariamente utilizando o tipo primitivo `double` para fins de simplificação didática das operações matemáticas nativas (`Math`). Estamos cientes de que dízimas binárias geram inconsistências de arredondamento em cálculos acumulados. Essa dívida técnica será formalmente quitada na **Aula 04** com a refatoração e migração de todo o domínio financeiro para o tipo `BigDecimal`.

---

### Fluxo de Trabalho e Versionamento Combinado
Para assegurar a integridade da branch principal (`main`), estabelecemos o seguinte fluxo colaborativo:

1. **Partir de uma main atualizada:** Antes de iniciar qualquer tarefa, execute:
   ```bash
   git switch main && git pull
   ```
2. **Branch de Funcionalidade:** Crie uma branch com nome curto, em minúsculas e separado por hífens:
   ```bash
   git switch -c feature/nome-do-modulo
   ```
3. **Commits Pequenos e Atômicos:** Commits frequentes contendo apenas uma ideia, utilizando mensagens padronizadas em letras minúsculas:
   ```text
   tipo: descrição curta no imperativo
   Exemplo: feat: adiciona calculo de frete por peso
   ```
4. **Proteção da main e Revisão (Pull Request):** Publicar a branch (`git push -u origin feature/nome-do-modulo`) e abrir um Pull Request (PR). A branch `main` exige pelo menos **1 aprovação** de outro membro da equipe antes do merge.
5. **Code Review Construtivo:** O revisor deve analisar a aba *Files changed* do PR, comentando pontos fortes e sugerindo melhorias técnicas diretamente nas linhas de código antes de aprovar.

---

### Estrutura Real de Pastas (Back-end & Front-end)
A estrutura do repositório respeita a geração do projeto Maven da squad dentro do subdiretório `back-end` conforme seu caminho de desenvolvimento local:

```text
ecommerce-pedidos-swift/
├── back-end/
│   └── ecommerce-pedidos-swift/        # Diretório do projeto Maven Back-End
│       ├── src/
│       │   ├── main/
│       │   │   └── java/
│       │   │       └── com/ecommerce/pedidos/swift/
│       │   │           ├── modelo/         # Entidades e classes de domínio (Produto, Cliente)
│       │   │           ├── servico/        # Serviços e regras de negócio complexas
│       │   │           ├── repositorio/    # Camada de persistência (DAO / Repository)
│       │   │           ├── util/           # Classes utilitárias (PedidoUtils.java)
│       │   │           └── Aplicacao.java  # Classe de execução principal (Teste manual)
│       │   └── test/
│       │       └── java/
│       │           └── com/ecommerce/pedidos/swift/  # Estrutura de testes (JUnit 5)
│       └── pom.xml                     # Configuração de dependências do Maven Back-end
├── front-end/                          # Código da interface web
├── .gitignore                          # Arquivos ignorados pelo controle de versão
└── README.md                           # Este arquivo de documentação do projeto
```

---

### Como Rodar o Projeto (Aula 03)
Para executar a aplicação e testar manualmente a classe utilitária localmente:

1. Navegue até o diretório raiz do projeto Maven Back-end:
   ```bash
   cd back-end/ecommerce-pedidos-swift
   ```
2. Compile o projeto utilizando o Apache Maven:
   ```bash
   mvn clean compile
   ```
3. Execute a aplicação por meio da classe principal:
   ```bash\n   mvn exec:java -Dexec.mainClass=\"com.ecommerce.pedidos.swift.Aplicacao\"\n   ```

---

### Roadmap do Projeto (Status por Aula)

| Aula | Entrega Prevista | Status |
| :---: | :--- | :---: |
| **01** | Repositório criado, estruturado, com README e commit inicial | 🟢 Concluído |
| **02** | Fluxo de branches e primeiro Pull Request revisado | 🟢 Concluído |
| **03** | Classe utilitária (`PedidoUtils.java`) do domínio incorporada | 🟢 Concluído |
| **04** | Classes de domínio inicial (Produto, Cliente, Pedido, ItemPedido) | ⏳ Pendente |
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

### Combinado da Equipe (Ética e Convivência)
1. **Transparência e Comunicação:** Alinhamento contínuo sobre o andamento das tarefas via GitHub e canal oficial de comunicação da equipe.
2. **Comprometimento com Prazos:** Cumprimento rigoroso do cronograma de entregas estabelecido no roadmap das aulas.
3. **Qualidade de Código:** Revisão criteriosa em todos os Pull Requests (Code Review) antes de realizar o *merge* na branch principal (`main`).

---

### Licença
Projeto estritamente acadêmico — **Faculdade de Tecnologia SENAI "Antonio Adolpho Lobbe"**. Todos os direitos reservados aos autores e à instituição.
