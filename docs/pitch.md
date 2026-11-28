# Roteiro do pitch técnico (5 minutos)

**Squad Swift:** Lucas Nunes Patracão e Rafael Rubiá Oliveira Cardoso

## Antes de começar (checklist, fora do tempo)

- [ ] MySQL no ar, com `DB_USERNAME`/`DB_PASSWORD` definidas no terminal da API.
- [ ] API rodando (`mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`) e `http://localhost:8080/api/dashboard` respondendo.
- [ ] Front rodando (`npm run dev`) e aberto em `http://localhost:5173`.
- [ ] Abas abertas: o app, o PR da Aula 15 (#23) com o CI verde, o PR de prova (#24) com o CI vermelho e o `docs/arquitetura.md` no GitHub.
- [ ] Dados da demo à mão: um CPF de teste **sintético** (gerado com dígitos verificadores válidos, nunca um CPF real) e um código de produto novo (ex.: `DEMO-01`).
- [ ] Plano B aberto numa aba: a pasta de prints (veja abaixo).

## Roteiro

| Tempo | Parte | O que mostrar e dizer |
| :---: | :--- | :--- |
| 0:00–0:30 | **Problema** | "Uma loja online precisa vender sem passar do estoque e sem perder o histórico. Fizemos uma API REST em Spring Boot com um painel em React que cuida de clientes, produtos, pedidos e pagamentos." |
| 0:30–2:30 | **Demo ao vivo** | **1. Cliente** (`/clientes`): cadastrar com e-mail sem "@" para mostrar a mensagem embaixo do campo (400 com `campos`); corrigir e salvar. **2. Produto** (`/produtos`): cadastrar `DEMO-01` com estoque 2. **3. Pedido** (`/pedidos/novo`): pedir 3 unidades → mensagem de estoque insuficiente (409); ajustar para 2 e salvar. **4. Pagamento** (detalhe do pedido): pagar com Pix → situação PAGO. **5. Dashboard** (`/`): totais, receita e o produto em estoque crítico. |
| 2:30–3:30 | **Arquitetura** | Abrir o `docs/arquitetura.md`. Camadas: controller só traduz HTTP, serviço com `@Transactional`, regras no domínio, Flyway versionando o banco. Mostrar a sequência: o estoque baixa na criação do pedido, numa transação com bloqueio; o pagamento é outra transação e, se recusado, nada muda. |
| 3:30–4:20 | **Qualidade e CI** | 168 testes (97 de unidade e 71 de integração), cobertura de 96,9% das linhas e 90,9% dos ramos, e o build falha abaixo da meta. Mostrar o PR #23 verde e o PR #24 vermelho (teste quebrado de propósito). |
| 4:20–5:00 | **Aprendizados** | 1) Padronizar o contrato (status HTTP e corpo de erro) facilitou o front. 2) Concorrência real: o número do pedido sorteado colidia e virou um contador com bloqueio. 3) O PR de prova foi mergeado por engano: o CI avisa, mas só a proteção da branch impede o merge. Próximo passo: exigir o check antes do merge. |

## Plano B (se a demo falhar)

1. **Não depurar ao vivo.** Dizer "o ambiente local caiu; vou mostrar a mesma sequência em prints" e seguir no tempo.
2. **Prints:** seguir a mesma sequência da demo com os prints abaixo. Como tirar cada um: [`prints/README.md`](prints/README.md).
3. **Sem app nenhum:** mostrar no GitHub o PR #23 com o CI verde (prova de que o build e os 168 testes passam) e o PR #24 vermelho, e depois os diagramas do `docs/arquitetura.md`.

### Prints, na ordem da demo

![Ponto de partida: dashboard com o banco vazio](prints/01-dashboard-vazio.png)

![1. Cliente: e-mail e telefone inválidos, com a mensagem embaixo de cada campo](prints/07-erro-validacao.png)

![1. Cliente: cadastro corrigido e salvo](prints/02-cliente-cadastrado.png)

![2. Produto: cadastrado no catálogo](prints/03-produto-cadastrado.png)

![3. Pedido: cliente, item e total antes de salvar](prints/04-novo-pedido.png)

![4. Pagamento: pedido pago com Pix](prints/05-pedido-pago.png)

![5. Dashboard: receita do pedido pago](prints/06-dashboard-com-receita.png)

![Qualidade e CI: PR com os dois jobs verdes](prints/09-ci-verde.png)

![Qualidade e CI: PR #24 com o job backend vermelho](prints/08-ci-vermelho.png)

## 5 perguntas prováveis da banca

1. **Por que o estoque baixa na criação do pedido, e não no pagamento?**
   Para reservar o produto: dois clientes não conseguem pedir a última unidade. Se o pedido for cancelado ou excluído em aberto, o estoque volta.

2. **O que acontece se duas pessoas comprarem a última unidade ao mesmo tempo?**
   O produto é lido com bloqueio pessimista (`PESSIMISTIC_WRITE`). A segunda venda espera a primeira terminar e recebe 409 com o saldo real. O `TransacaoPedidoTest` prova isso.

3. **Por que os testes não usam MySQL?**
   Usam H2 em memória no modo MySQL, com as mesmas migrations do Flyway e o Hibernate validando o schema. Assim o CI roda sem banco e sem segredos. O custo: algo específico do MySQL poderia passar. Testcontainers seria o passo seguinte.

4. **Por que DTO, e não devolver a entidade?**
   O JSON vira um contrato estável: mudar a entidade não quebra o front, e as relações do banco não vazam. Também é no DTO que ficam as validações de entrada.

5. **Está publicado em algum lugar? Tem login?**
   Não. Roda localmente e tem só integração contínua; não há deploy nem autenticação. Estão nas limitações do relatório técnico, com uma proposta gratuita de deploy em `docs/arquitetura.md`.
