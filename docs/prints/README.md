# Prints da entrega

Esta pasta guarda os prints usados no plano B do [pitch](../pitch.md) e citados no [guia de uso](../guia-de-uso.md). Este README existe para a pasta ir para o Git mesmo antes de os prints serem tirados.

## Prints esperados (nesta ordem, com estes nomes exatos)

| Arquivo | O que capturar |
|---|---|
| `01-dashboard-vazio.png` | Dashboard com o banco vazio |
| `02-cliente-cadastrado.png` | Tela de Clientes com 1 cliente na tabela |
| `03-produto-cadastrado.png` | Tela de Produtos com 1 produto na tabela |
| `04-novo-pedido.png` | Novo Pedido com cliente selecionado, 1 item e o total |
| `05-pedido-pago.png` | Detalhe do pedido com situação PAGO |
| `06-dashboard-com-receita.png` | Dashboard com receita maior que R$ 0,00 |
| `07-erro-validacao.png` | Formulário de cliente enviado vazio, com as mensagens embaixo dos campos |
| `08-ci-vermelho.png` | Aba Checks do PR #24, com o job `backend` vermelho |
| `09-ci-verde.png` | Aba Checks de um PR com os dois jobs verdes |

## Antes de começar

- A API e o front precisam estar rodando, com o **banco vazio** (nenhum cliente, produto ou pedido). Siga o [guia de uso](../guia-de-uso.md) até a seção 6.
- Se o banco já tem dados, os prints 01 a 06 não saem como descritos. Para zerar o banco, veja "Recomeçar com o banco vazio" na seção 9 do guia.
- Tire os prints **na ordem da tabela**: cada um usa os dados criados no anterior. O 07 vem depois do 06 de propósito, para não sujar a tela de Clientes do print 02.

## Como capturar cada print

1. Deixe o navegador na tela descrita no passo.
2. Aperte `Win + Shift + S`, escolha o modo **Recorte de janela** (ícone de janela na barra que aparece no topo) e clique na janela do navegador.
3. Abra o Paint (tecla `Win`, digite `Paint`, `Enter`) e cole com `Ctrl+V`.
4. No Paint: **Arquivo → Salvar como → Imagem PNG**.
5. Navegue até esta pasta (`ecommerce-pedidos-swift\docs\prints`), digite o nome **exato** da tabela (por exemplo `01-dashboard-vazio.png`, tudo em minúsculas) e clique em **Salvar**.

Se der errado: se o recorte não vai para o Paint, a captura foi cancelada (`Esc`) ou o modo estava em outro tipo; aperte `Win + Shift + S` de novo. Se o arquivo saiu como `.jpg` ou `.bmp`, salve de novo escolhendo **Imagem PNG**.

### 01 — `01-dashboard-vazio.png`

1. Abra `http://localhost:5173`.
2. No menu lateral, clique em **Dashboard**.
3. Confira: o card **Receita total** mostra `R$ 0,00`, **Quantidade de pedidos** mostra `0`, e os painéis mostram "Nenhum pedido registrado ainda." e "Nenhum produto com estoque crítico.".
4. Capture.

### 02 — `02-cliente-cadastrado.png`

1. No menu lateral, clique em **Clientes**.
2. Em **Cadastro de cliente**, preencha:
   - **Nome:** `Maria Silva`
   - **CPF:** `52998224725`
   - **E-mail:** `maria.silva@email.com`
   - **Telefone:** `11987654321`
   - **Endereço:** `Rua das Flores, 123`
3. Clique em **Cadastrar cliente**.
4. Confira: aparece "Cliente cadastrado com sucesso." e a tabela **Clientes cadastrados** tem uma linha com Maria Silva.
5. Capture.

### 03 — `03-produto-cadastrado.png`

1. No menu lateral, clique em **Produtos**.
2. Em **Cadastro de produto**, preencha:
   - **Código:** `CAM-001`
   - **Nome:** `Camiseta Básica`
   - **Descrição:** `Algodão, tamanho M`
   - **Preço:** `49.90`
   - **Estoque:** `10`
   - Deixe marcado **Produto disponível para venda**.
3. Clique em **Salvar produto**.
4. Confira: aparece "Produto cadastrado com sucesso." e a tabela **Gerenciamento** mostra `CAM-001`, `R$ 49.90`, estoque `10` e o status **Disponível**.
5. Capture.

### 04 — `04-novo-pedido.png`

1. Clique no botão **Novo pedido**, no topo da tela.
2. Em **Cliente**, escolha `Maria Silva (52998224725)`.
3. Em **Produto**, escolha `Camiseta Básica — R$ 49,90 (10 em estoque)`.
4. Em **Quantidade**, digite `2` e clique em **Adicionar item**.
5. Confira: em **Resumo do pedido** aparece a linha da Camiseta Básica com subtotal `R$ 99,80`, e **Total do pedido** mostra `R$ 99,80`.
6. Capture **antes** de clicar em **Salvar pedido**.

### 05 — `05-pedido-pago.png`

1. Na mesma tela do print 04, clique em **Salvar pedido**. O sistema abre o detalhe do pedido (título `PED-` seguido do ano e do número, por exemplo `PED-2026-00001`) com a situação **ABERTO**.
2. No painel **Pagamento**, deixe **Forma de pagamento** em **Pix**, **Tipo da chave** em **E-mail** e preencha **Chave Pix** com `maria.silva@email.com`.
3. Clique em **Pagar R$ 99,80**.
4. Confira: aparece "Pagamento registrado com sucesso.", a etiqueta ao lado do número do pedido muda para **PAGO**, e o **Resumo** mostra **Forma de pagamento** `Pix` e **Situação do pagamento** `APROVADO`.
5. Capture.

### 06 — `06-dashboard-com-receita.png`

1. No menu lateral, clique em **Dashboard**.
2. Confira: **Receita total** mostra `R$ 99,80`, **Pedidos recentes** lista o pedido com status **PAGO** e **Estoque crítico** mostra "Camiseta Básica — 8 unidades restantes" (o limite do estoque crítico é abaixo de 15 unidades).
3. Se os números não mudaram, clique em **Atualizar**, no topo.
4. Capture.

### 07 — `07-erro-validacao.png`

Os campos do formulário são obrigatórios no navegador: com eles **totalmente** vazios, o próprio navegador barra o envio e mostra só o balão "Preencha este campo", sem chamar a API. Para a API recusar e as mensagens aparecerem embaixo dos campos, envie o formulário "vazio" com espaços:

1. No menu lateral, clique em **Clientes**.
2. Em **Cadastro de cliente**, preencha:
   - **Nome:** um espaço (aperte a barra de espaço uma vez)
   - **CPF:** um espaço
   - **E-mail:** `maria@email`
   - **Telefone:** `abc`
   - **Endereço:** um espaço
3. Clique em **Cadastrar cliente**.
4. Confira: em vermelho, "Há campos inválidos. Corrija-os e tente novamente."; embaixo de **Nome**, "Nome é obrigatório."; embaixo de **Telefone**, "Telefone deve conter apenas dígitos (8 a 20).".
5. Capture.

### 08 — `08-ci-vermelho.png`

1. Abra `https://github.com/lucaspatracao/ecommerce-pedidos-swift/pull/24/checks`.
2. Confira: na lista da esquerda, **Back-end (Maven verify)** (job `backend`) aparece com um X vermelho.
3. Clique nele para mostrar o passo **Testes, cobertura e build** com a falha.
4. Capture.

### 09 — `09-ci-verde.png`

1. Abra `https://github.com/lucaspatracao/ecommerce-pedidos-swift/pull/23/checks`.
2. Confira: **Back-end (Maven verify)** e **Front-end (lint e build)** aparecem os dois com o check verde.
3. Capture.

Se o GitHub pedir login, entre com a sua conta. Se a página dos checks estiver vazia, os registros do Actions podem ter expirado; nesse caso use qualquer outro PR com os dois jobs verdes (aba **Pull requests → Closed**).

## Checklist

- [ ] `01-dashboard-vazio.png`
- [ ] `02-cliente-cadastrado.png`
- [ ] `03-produto-cadastrado.png`
- [ ] `04-novo-pedido.png`
- [ ] `05-pedido-pago.png`
- [ ] `06-dashboard-com-receita.png`
- [ ] `07-erro-validacao.png`
- [ ] `08-ci-vermelho.png`
- [ ] `09-ci-verde.png`
