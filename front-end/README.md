# Front-end — Painel Administrativo (React + Vite)

Guia rápido do front-end. A visão geral do projeto, a equipe, a API completa e o roadmap estão no [README da raiz](../README.md).

## Stack

React 19 · React Router 7 · Axios · Vite 8 · ESLint (com `eslint-plugin-react-hooks`)

Requer **Node.js 20.19+ ou 22.13+** (exigência do Vite 8 e do ESLint 10).

## Rodar (PowerShell)

O back-end precisa estar no ar em `http://localhost:8080/api` (veja o README da raiz). Em outro terminal:

```powershell
cd front-end
npm ci
npm run dev        # abre em http://localhost:5173 (o back-end libera CORS para essa origem)
```

| Comando | O que faz |
| :--- | :--- |
| `npm run dev` | servidor de desenvolvimento com recarga automática |
| `npm run build` | gera a versão de produção em `dist/` |
| `npm run preview` | serve o `dist/` localmente para conferir o build |
| `npm run lint` | ESLint; deve terminar com **0 problemas** (sem `eslint-disable`) |

## Telas

| Rota | Tela | API usada |
| :--- | :--- | :--- |
| `/` | Dashboard: totais, receita, estoque crítico e pedidos recentes | `GET /dashboard` |
| `/clientes` | Cadastro, edição, busca e exclusão de clientes | `GET/POST/PUT/DELETE /clientes` |
| `/produtos` | Cadastro, edição, busca e exclusão (inativação) de produtos | `GET/POST/PUT/DELETE /produtos` |
| `/pedidos` | Lista de pedidos | `GET /pedidos` |
| `/pedidos/novo` | Novo pedido: cliente, itens, subtotal e total, com validação de estoque | `POST /pedidos` |
| `/pedido/:id` (`:id` = número do pedido) | Detalhe do pedido e pagamento (Pix, Boleto, Cartão de crédito) | `GET /pedidos/{numero}`, `POST /pedidos/{numero}/pagamento` |
| `/pagamentos` | Pagamentos registrados | `GET /pagamentos` |
| `/historico` | Histórico de compras (últimos pedidos) | `GET /historico` |

## Estrutura

```text
src/
├── api/clienteApi.js          # todas as chamadas HTTP (Axios); erros viram `ErroApi` (mensagem + campos do corpo de erro padronizado)
├── components/                # ClienteForm, PagamentoForm, ErroCampo (mensagem de validação junto do campo)
├── pages/                     # uma página por rota
├── utils/                     # formatarFormaPagamento (nome legível de cada forma)
├── App.jsx                    # rotas, menu lateral e carga dos dados (Promise.allSettled)
├── App.css / index.css        # tokens de cor, espaçamento e componentes visuais
└── main.jsx
```

## Regras da interface

* O back-end é a fonte de verdade: a interface só mostra o que a API devolve. Sem registros, aparece o estado vazio ("Nenhum cliente cadastrado ainda."); sem API, uma mensagem de erro.
* Toda mensagem de erro exibida vem do campo `erro` da resposta da API.
* Uma fonte que falha não zera as outras: cada uma é carregada e aplicada de forma independente.
* Produto "Disponível" desmarcado ou excluído é **inativado** (soft delete) e sai da listagem, que mostra só ativos.
* Tabelas de Clientes e Produtos ficam sem rolagem horizontal de 1100 px a 1920 px: abaixo de 1680 px o formulário passa para cima da tabela.
* Identidade visual: laranja da marca Swift com branco, preto e tons neutros.

## Ainda não usado pela interface

A API já oferece, mas o front ainda não tem tela para: inativar/reativar (`PATCH {"ativo"}`) e listar inativos, filtros e paginação, estoque parcial (`PATCH`), cancelar pedido (`PATCH {"situacao"}`), remover item, excluir pedido e pedidos por cliente. Esses itens estão em "Dívidas Técnicas Registradas" no README da raiz.
