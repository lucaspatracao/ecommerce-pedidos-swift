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
* **Processamento de Pagamentos:** Suporte a múltiplos métodos (Cartão de Crédito, Boleto Bancário e Pix).
* **Dashboard:** Visualização de informações gerais do sistema, pedidos recentes, clientes, produtos e indicadores disponíveis.
* **Interface Administrativa:** Interface web para consulta e gerenciamento das informações do sistema.
* **Integração Front-end / Back-end:** Consumo dos endpoints reais da API REST pelo Front-end.
* **Garantia de Qualidade:** Testes automatizados unitários e de integração com relatórios de cobertura.
* **Integração Contínua (CI/CD):** Pipeline para execução de build e testes automatizados.
* **API RESTful:** Endpoints estruturados para integração e consumo pela aplicação Front-end e futuras aplicações.

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
* **Banco de Dados:** Em evolução conforme as etapas de persistência do projeto
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

2. **Persistência:** A camada de persistência ainda está em evolução de acordo com as etapas previstas no roadmap do projeto.

3. **Autenticação e Autorização:** O projeto ainda não possui um mecanismo completo de autenticação e autorização de usuários.

4. **Fluxo Completo de Pedidos:** A integração visual e a comunicação com a API já estão estruturadas, porém o ciclo completo de criação, edição, inclusão de itens, pagamento e alteração de status continuará sendo desenvolvido conforme as próximas etapas do projeto.

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
│       │           ├── model/
│       │           ├── service/
│       │           ├── repository/
│       │           ├── controller/
│       │           └── swift/
│       │               └── Aplicacao.java
│       └── test/
│           └── java/
│               └── com/ecommerce/pedidos/model/
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

Navegue até o diretório que contém o `pom.xml`:

```bash
cd back-end
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
| **07** | Relacionamentos entre classes do domínio | ⏳ Pendente |
| **08** | Módulo de pagamento polimórfico | ⏳ Pendente |
| **09** | Tratamento de exceções e validações | ⏳ Pendente |
| **10** | Suíte de testes unitários | ⏳ Pendente |
| **11** | Suíte de testes de integração + relatório de cobertura | ⏳ Pendente |
| **12** | Persistência: conexão, operações Create e Read | ⏳ Pendente |
| **13** | Persistência: operações Update, Delete e padrão DAO/Repository | ⏳ Pendente |
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