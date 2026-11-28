# Guia de uso — do zero ao sistema rodando

Passo a passo para quem nunca viu o projeto, num PC com Windows. Todo comando está num bloco `powershell` completo: copie o bloco inteiro e cole no **PowerShell** (tecla `Win`, digite `PowerShell`, `Enter`). Os únicos valores que você troca são senhas, e cada uma vem avisada.

> A senha de exemplo do usuário do banco é `TroqueEstaSenha123`. Se quiser outra, troque-a **em todos os blocos** em que ela aparece (seções 3 e 4).

Sumário: [1. Pré-requisitos](#1-pré-requisitos) · [2. Baixar o projeto](#2-baixar-o-projeto) · [3. Banco de dados](#3-banco-de-dados) · [4. Variáveis de ambiente](#4-variáveis-de-ambiente) · [5. Subir a API](#5-subir-a-api) · [6. Subir o front](#6-subir-o-front) · [7. Usando o sistema](#7-usando-o-sistema) · [8. Rodar os testes](#8-rodar-os-testes) · [9. Problemas comuns](#9-problemas-comuns) · [10. Encerrar e sair do PC](#10-encerrar-e-sair-do-pc)

---

## 1. Pré-requisitos

Confira cada ferramenta:

```powershell
java -version
mvn -v
node -v
npm -v
git --version
```

| Ferramenta | Versão mínima | O que procurar na saída | Se faltar, baixe em |
| :--- | :--- | :--- | :--- |
| JDK (Java) | 17 | `java version "17...` ou maior | <https://adoptium.net/temurin/releases/?version=17> |
| Maven | 3.9 | `Apache Maven 3.9...` **e** a linha `Java version: 17...` ou maior | <https://maven.apache.org/download.cgi> |
| Node.js | 20.19 ou 22.13 | `v20.19...`, `v22.13...` ou maior (exigência do Vite 8 e do ESLint 10) | <https://nodejs.org/pt/download> |
| npm | vem com o Node | qualquer número (ex.: `11.6.2`) | instalado junto com o Node |
| Git | qualquer versão recente | `git version 2...` | <https://git-scm.com/downloads/win> |
| MySQL | 8.0 | veja a seção 3 | <https://dev.mysql.com/downloads/installer/> |

- **Deu certo:** cada comando imprime uma versão.
- **Deu errado:** `... não é reconhecido como nome de cmdlet` significa que a ferramenta não está instalada ou não está no `PATH`. Instale pelo link e **feche e abra o PowerShell de novo**.
- O Maven usa o Java indicado na linha `Java version` do `mvn -v` (vem do `JAVA_HOME`), que pode ser diferente do `java -version`. O que vale para o projeto é a linha do `mvn -v`.

## 2. Baixar o projeto

O projeto fica na Área de Trabalho. Todos os blocos deste guia usam esse caminho.

```powershell
Set-Location "$env:USERPROFILE\Desktop"
git clone https://github.com/lucaspatracao/ecommerce-pedidos-swift.git
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift"
git status
```

- **Deu certo:** `git status` mostra `On branch main` e `nothing to commit, working tree clean`.
- **Deu errado:** `destination path 'ecommerce-pedidos-swift' already exists` significa que o projeto já foi baixado; rode só as duas últimas linhas e depois `git pull` para atualizar.

## 3. Banco de dados

A API usa um banco MySQL chamado `ecommerce_swift` e um usuário próprio, `swift`. As tabelas **não** são criadas à mão: o Flyway cria tudo na primeira subida da API.

Escolha **um** cenário:

| Cenário | Quando usar | Porta |
| :--- | :--- | :--- |
| **A** | MySQL 8.0 instalado como serviço e você sabe a senha do `root` | 3306 |
| **B** | PC sem administrador (laboratório), senha do `root` desconhecida | 3307 |

### Cenário A — MySQL instalado e senha do root conhecida

Uma única vez. O comando pede a senha do `root` (digite e `Enter`; nada aparece enquanto você digita).

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'swift'@'%' IDENTIFIED BY 'TroqueEstaSenha123'; GRANT ALL PRIVILEGES ON ecommerce_swift.* TO 'swift'@'%'; FLUSH PRIVILEGES;"
```

> Troque `TroqueEstaSenha123` pela senha que o usuário `swift` vai ter (a mesma da seção 4).

Confira, entrando com o usuário novo (pede a senha do `swift`):

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u swift -p -e "SHOW DATABASES;"
```

- **Deu certo:** a lista mostra `ecommerce_swift`.
- **Deu errado:** `Access denied for user 'root'` = senha do root errada (se não souber, use o cenário B). `Can't connect to MySQL server on 'localhost:3306'` = o serviço está parado; veja `Start-Service` na [seção 9](#9-problemas-comuns). O arquivo não existe = o MySQL não está instalado nessa pasta; use o cenário B se o `mysqld.exe` existir em outro lugar, ou instale o MySQL 8.0.

### Cenário B — PC sem administrador, instância própria na porta 3307

Aqui você cria um MySQL **seu**, numa pasta do seu usuário, sem mexer no serviço do PC nem precisar da senha do root dele. O root da sua instância nasce **sem senha** (`--initialize-insecure`) e só aceita conexões desta máquina.

Requisito: o MySQL 8.0 estar instalado em `C:\Program Files\MySQL\MySQL Server 8.0` (só os arquivos; o serviço não importa). Confira:

```powershell
Test-Path "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe"
```

Deve aparecer `True`. Se aparecer `False`, este PC não tem o MySQL 8.0; peça a instalação ou use outro PC.

**B1 — Criar a pasta de dados (uma única vez por PC/usuário):**

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --no-defaults --initialize-insecure --datadir="$env:USERPROFILE\mysql-swift-data"
```

- **Deu certo:** termina sem mensagem e cria a pasta `C:\Users\<seu usuário>\mysql-swift-data`.
- **Deu errado:** `--initialize specified but the data directory has files in it` significa que a pasta já existe (você já fez este passo antes). Pule para o B2.

**B2 — Ligar a instância (a cada uso, toda vez que ligar o PC):**

```powershell
Start-Process -FilePath "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" -ArgumentList "--no-defaults", "--datadir=`"$env:USERPROFILE\mysql-swift-data`"", "--port=3307", "--mysqlx=0" -WindowStyle Hidden
```

Espere uns 5 segundos e confira:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h 127.0.0.1 -P 3307 -e "SELECT @@port, @@datadir;"
```

- **Deu certo:** mostra `3307` e o caminho da sua pasta `mysql-swift-data`.
- **Deu errado:** `Can't connect to MySQL server on '127.0.0.1:3307'` = a instância não subiu. Rode o B2 de novo e espere mais. Se continuar, veja o arquivo de erro na própria pasta de dados: `Get-Content "$env:USERPROFILE\mysql-swift-data\*.err" -Tail 20`. O `--mysqlx=0` evita o conflito com a porta 33060 do MySQL do serviço.

**B3 — Criar o banco e o usuário `swift` (uma única vez):**

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h 127.0.0.1 -P 3307 -e "CREATE DATABASE IF NOT EXISTS ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'swift'@'%' IDENTIFIED BY 'TroqueEstaSenha123'; GRANT ALL PRIVILEGES ON ecommerce_swift.* TO 'swift'@'%'; FLUSH PRIVILEGES; SELECT user, host FROM mysql.user WHERE user='swift';"
```

> Troque `TroqueEstaSenha123` pela senha que o usuário `swift` vai ter (a mesma da seção 4).

- **Deu certo:** a última consulta mostra uma linha `swift  %`.
- **Deu errado:** se aparecerem também `swift localhost` ou `swift 127.0.0.1`, eles têm prioridade sobre o `swift %` e podem ter outra senha; veja `Access denied` na [seção 9](#9-problemas-comuns).

| Passo | Frequência |
| :--- | :--- |
| B1 — criar a pasta de dados | uma única vez |
| B2 — ligar a instância | **a cada uso** |
| B3 — criar banco e usuário | uma única vez |
| Desligar (seção 10) | ao terminar |

## 4. Variáveis de ambiente

A API lê a conexão de três variáveis (veja `src/main/resources/application.properties`):

| Variável | Para quê | Obrigatória? |
| :--- | :--- | :--- |
| `DB_URL` | endereço do banco | No cenário A não (o padrão é `localhost:3306/ecommerce_swift`). No cenário B **sim** (porta 3307). |
| `DB_USERNAME` | usuário do banco | Sim: `swift` |
| `DB_PASSWORD` | senha do usuário | Sim. Não tem valor padrão de propósito: sem ela a API não sobe. |

Escolha **uma** das duas formas.

### Forma 1 — no terminal (valem só naquela janela)

Cole no **mesmo** PowerShell em que vai subir a API (seção 5). Fechou a janela, perdeu as variáveis: cole de novo.

Cenário A:

```powershell
$env:DB_USERNAME = "swift"
$env:DB_PASSWORD = "TroqueEstaSenha123"
Remove-Item Env:DB_URL -ErrorAction SilentlyContinue
```

Cenário B:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "swift"
$env:DB_PASSWORD = "TroqueEstaSenha123"
```

> Troque `TroqueEstaSenha123` pela senha do usuário `swift` que você criou na seção 3.

Confira com `Get-ChildItem Env:DB_*`: devem aparecer as variáveis com os valores acima.

### Forma 2 — arquivo `.env` (vale para todo terminal)

O `.env` fica em `back-end\ecommerce-pedidos-swift`, ao lado do `.env.example`, e **não vai para o Git** (está no `.gitignore`). A API o procura na **pasta em que ela é iniciada**, então ela precisa subir de `back-end\ecommerce-pedidos-swift`, como na seção 5. Se a mesma variável existir no terminal e no `.env`, vale a do terminal.

Cenário A:

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift"
Copy-Item .env.example .env
Set-Content -Path .env -Encoding ASCII -Value @'
DB_USERNAME=swift
DB_PASSWORD=TroqueEstaSenha123
'@
Get-Content .env
```

Cenário B:

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift"
Copy-Item .env.example .env
Set-Content -Path .env -Encoding ASCII -Value @'
DB_URL=jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=swift
DB_PASSWORD=TroqueEstaSenha123
'@
Get-Content .env
```

> Troque `TroqueEstaSenha123` pela senha do usuário `swift` que você criou na seção 3.

- **Deu certo:** `Get-Content .env` mostra as linhas `CHAVE=valor`, sem aspas.
- O `-Encoding ASCII` é de propósito: no Windows PowerShell 5.1, salvar como UTF-8 grava um caractere invisível (BOM) no começo do arquivo, e a primeira variável deixa de ser reconhecida. Se você editar o `.env` no Bloco de Notas, salve como **UTF-8** (sem BOM), não "UTF-8 com BOM".

## 5. Subir a API

Cenário B: ligue a instância antes (passo B2). Depois, no PowerShell com as variáveis (forma 1) ou com o `.env` criado (forma 2):

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift"
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

As aspas são obrigatórias no PowerShell por causa do `-D` com ponto. A primeira vez demora (o Maven baixa as dependências). **Deixe esta janela aberta**: fechar a janela desliga a API.

**Deu certo** quando o log mostrar, nesta ordem:

```text
The following 1 profile is active: "dev"
Successfully applied 3 migrations to schema `ecommerce_swift`, now at version v3
Tomcat started on port 8080 (http) with context path '/'
Started EcommercePedidosSwiftApplication in ... seconds
```

Da segunda subida em diante, no lugar do `Successfully applied 3 migrations` aparece `Schema `ecommerce_swift` is up to date. No migration necessary.`. O log também mostra o SQL de cada consulta (`show-sql` do perfil `dev`); isso é normal.

Teste rápido, num **segundo** PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/clientes
Invoke-RestMethod http://localhost:8080/api/dashboard
```

- **Deu certo:** o primeiro não mostra nada (lista vazia, sem erro) num banco novo; o segundo mostra `totalClientes : 0`, `receitaTotal : 0,00` etc.
- **Deu errado:** `Impossível conectar-se ao servidor remoto` = a API não está no ar; volte ao primeiro terminal e leia o erro. Os erros de subida mais comuns estão na [seção 9](#9-problemas-comuns).

## 6. Subir o front

Num **outro** PowerShell (a API continua rodando no primeiro):

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\front-end"
npm ci
npm run dev
```

- `npm ci` instala exatamente as versões do `package-lock.json`. Só precisa rodar de novo quando o `package-lock.json` mudar.
- **Deu certo:** o Vite mostra `Local:   http://localhost:5173/`. Abra esse endereço no navegador.
- **Deixe esta janela aberta** também.
- **Deu errado:** `EPERM: operation not permitted, unlink ...rolldown...` no `npm ci` = um `npm run dev` antigo ainda está rodando e trava os arquivos; pare-o com `Ctrl+C` na janela dele e rode o bloco de novo. Se o Vite mostrar `Port 5173 is in use, trying another one...` e subir em `5174`, as telas abrem mas ficam sem dados (a API só libera a origem `localhost:5173`); veja "porta ocupada" na [seção 9](#9-problemas-comuns).

## 7. Usando o sistema

O menu lateral tem: **Dashboard**, **Clientes**, **Produtos**, **Pedidos**, **Pagamentos** e **Histórico de compras**. No topo ficam **Atualizar** (recarrega tudo da API) e **Novo pedido**. Clicar no logo **Swift** recolhe o menu.

Os dados de exemplo abaixo são válidos e **sintéticos** (os CPFs têm dígitos verificadores corretos, mas não pertencem a ninguém). Siga na ordem: cada passo usa o anterior. Onde aparece 📸, tire o print com o nome indicado (como capturar: [`prints/README.md`](prints/README.md)).

Toda mensagem de erro vem da API num formato padronizado: a mensagem geral aparece numa faixa vermelha e, em erro de validação, cada campo recebe a sua mensagem embaixo dele.

### 7.1 Dashboard

1. Abra `http://localhost:5173`. A tela inicial é o **Dashboard** (rota `/`).
2. Cards: **Receita total**, **Quantidade de pedidos**, **Clientes ativos** e **Produtos em catálogo**. Painéis: **Pedidos recentes** e **Estoque crítico** (produtos ativos com menos de 15 unidades).
3. Banco vazio: `R$ 0,00`, zeros, "Nenhum pedido registrado ainda." e "Nenhum produto com estoque crítico.".

📸 `01-dashboard-vazio.png`

Se aparecer "Não foi possível carregar: clientes, produtos, ..." a API está desligada ou em outra porta (veja a seção 9).

### 7.2 Clientes (rota `/clientes`)

**Cadastrar.** Em **Cadastro de cliente**, preencha e clique em **Cadastrar cliente**:

| Campo | Cliente 1 | Cliente 2 | Cliente 3 (para excluir depois) |
| :--- | :--- | :--- | :--- |
| Nome | `Maria Silva` | `João Souza` | `Cliente Temporário` |
| CPF | `52998224725` | `11144477735` | `16899535009` |
| E-mail | `maria.silva@email.com` | `joao.souza@email.com` | `temp@email.com` |
| Telefone | `11987654321` | `11912345678` | `11900000000` |
| Endereço | `Rua das Flores, 123` | `Av. Brasil, 500` | `Rua A, 1` |

- **Resultado esperado:** "Cliente cadastrado com sucesso.", o formulário limpa e o cliente aparece em **Clientes cadastrados**. O campo de busca filtra por nome, e-mail ou CPF.
- Depois do primeiro cliente: 📸 `02-cliente-cadastrado.png` (tire com só a Maria na tabela).

Erros comuns:

| Mensagem | Significado |
| :--- | :--- |
| `CPF inválido: 12345678900` | Os dígitos verificadores não batem. Use só números, sem ponto nem traço. |
| `Já existe cliente cadastrado com o CPF 52998224725` | O CPF já está no sistema (inclusive em cliente inativo). |
| `Já existe cliente cadastrado com o e-mail maria.silva@email.com` | O e-mail é único por cliente. |
| `Telefone deve conter apenas dígitos (8 a 20).` (embaixo do campo) | Tire parênteses, espaço e traço: `11987654321`. |
| `Nome é obrigatório.` (embaixo do campo) | Nome em branco ou só com espaços. |
| Balão do navegador "Preencha este campo" | Campo vazio; o navegador nem chega a chamar a API. |

**Editar.** Na linha do João Souza, clique em **Editar**. O título muda para **Editar cliente**. Troque o **Nome** para `João Souza Lima` e clique em **Salvar alterações**. Resultado: "Cliente atualizado com sucesso." e a tabela mostra o nome novo. O CPF identifica o cliente e não muda na edição. **Cancelar** desfaz a edição.

**Inativar e reativar.** A tela não tem botão para isso; use a API (cliente inativo some das listagens, mas o histórico de pedidos dele continua). Com a API rodando, num PowerShell:

```powershell
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/clientes/11144477735" -ContentType "application/json" -Body '{"ativo": false}'
```

Resultado: a resposta mostra `ativo : False`. Clique em **Atualizar** no topo: o João some da tabela. Para ver os inativos e reativar:

```powershell
Invoke-RestMethod "http://localhost:8080/api/clientes?situacao=inativos"
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/clientes/11144477735" -ContentType "application/json" -Body '{"ativo": true}'
```

Inativar de novo quem já está inativo devolve 409 "Cliente já está inativo.".

**Excluir.** Na linha do **Cliente Temporário**, clique em **Excluir**. **Não há confirmação**: o cliente é apagado na hora e some da tabela. Só cliente **sem pedidos** pode ser excluído. Ao tentar excluir a Maria depois do pedido da seção 7.4, aparece no topo: "Este cliente possui pedidos e não pode ser excluído. Inative-o para tirá-lo das listagens sem perder o histórico.".

### 7.3 Produtos (rota `/produtos`)

**Cadastrar.** Em **Cadastro de produto**, preencha e clique em **Salvar produto**. Deixe marcado **Produto disponível para venda**.

| Campo | Produto 1 | Produto 2 |
| :--- | :--- | :--- |
| Código | `CAM-001` | `DEMO-01` |
| Nome | `Camiseta Básica` | `Caneca Swift` |
| Descrição | `Algodão, tamanho M` | `Caneca de porcelana 300 ml` |
| Preço | `49.90` | `29.90` |
| Estoque | `10` | `2` |

- **Resultado esperado:** "Produto cadastrado com sucesso." e o produto em **Gerenciamento**, com status **Disponível**.
- Depois do primeiro produto: 📸 `03-produto-cadastrado.png` (tire com só a Camiseta na tabela).

Erros comuns:

| Mensagem | Significado |
| :--- | :--- |
| `Já existe um produto com este código.` | O código é único. |
| `Código do produto deve ter entre 3 e 40 caracteres.` | Código curto demais (ex.: `A1`). |
| `Preço do produto deve ser maior que zero.` | Preço `0` ou negativo. Use ponto como separador: `49.90`. |
| `Estoque do produto não pode ser negativo.` | Estoque menor que zero. |

**Editar.** Na linha da Camiseta, clique em **Editar**; o título vira **Editar produto**. Troque o **Preço** para `59.90` e clique em **Salvar alterações**: "Produto atualizado com sucesso.". Para voltar ao preço do roteiro dos prints, edite de novo para `49.90`. **Cancelar** desfaz a edição.

**Inativar.** Duas formas, com o mesmo efeito (o produto sai do catálogo e do Novo pedido, mas continua nos pedidos antigos):

- Clicar em **Excluir** na linha do produto. Apesar do nome, é uma **inativação** (soft delete), sem confirmação.
- Editar e desmarcar **Produto disponível para venda**: o status vira **Indisponível** e o produto some da lista ao clicar em **Atualizar**.

Para ver os inativos e reativar a Caneca:

```powershell
Invoke-RestMethod "http://localhost:8080/api/produtos?situacao=inativos"
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/produtos/DEMO-01" -ContentType "application/json" -Body '{"ativo": true}'
```

Clique em **Atualizar**: a Caneca volta com status **Disponível**.

**Excluir definitivamente** (só pela API, e só produto **nunca vendido**):

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/produtos/DEMO-01?definitivo=true"
```

Sem vendas, não imprime nada (204). Se o produto já foi vendido, o PowerShell mostra erro `(409)`; o motivo é "Este produto já foi vendido e não pode ser excluído definitivamente. Inative-o para tirá-lo do catálogo.". Se você rodou este comando, cadastre a Caneca de novo para seguir o roteiro.

### 7.4 Novo pedido (rota `/pedidos/novo`)

1. Clique em **Novo pedido** no topo.
2. **Cliente:** `Maria Silva (52998224725)`.
3. **Produto:** `Camiseta Básica — R$ 49,90 (10 em estoque)`. Só aparecem produtos ativos e com estoque.
4. **Quantidade:** `2`. Clique em **Adicionar item**.
5. Em **Resumo do pedido**: a linha com subtotal `R$ 99,80` e **Total do pedido** `R$ 99,80`. **Remover** tira o item. Adicionar o mesmo produto de novo soma na mesma linha.

📸 `04-novo-pedido.png` (antes de salvar)

6. Clique em **Salvar pedido**. O sistema abre o detalhe do pedido, com número no formato `PED-<ano>-<sequencial>` (o primeiro do ano é `PED-2026-00001` em 2026) e situação **ABERTO**. O estoque da Camiseta cai para `8` na hora: o produto fica reservado.

Erros comuns:

| Mensagem | Significado |
| :--- | :--- |
| `Selecione o cliente do pedido.` | Faltou escolher o cliente. |
| `Adicione pelo menos um item ao pedido.` | Clicou em **Salvar pedido** sem itens. |
| `Selecione um produto.` | Clicou em **Adicionar item** sem produto. |
| `Estoque insuficiente: Caneca Swift tem 2 unidade(s).` | Pediu mais do que há (teste com 3 Canecas). |
| `Estoque insuficiente de Caneca Swift: disponível 1, solicitado 2` | Mesma regra, vinda da API: o estoque mudou desde que a tela abriu. A tela recarrega os produtos sozinha. |
| "Nenhum cliente cadastrado. Cadastre um em Clientes." / "Nenhum produto ativo com estoque." | Cadastre antes (seções 7.2 e 7.3). |

Para testar as três formas de pagamento, crie mais dois pedidos agora: **João Souza Lima** com 1 Caneca Swift (`R$ 29,90`) e **João Souza Lima** com 1 Camiseta Básica (`R$ 49,90`). Se o João estiver inativo, reative-o (7.2) e clique em **Atualizar**.

### 7.5 Detalhe do pedido e pagamento (rota `/pedido/<número>`)

O detalhe abre sozinho depois de salvar. Para chegar nele depois: menu **Pedidos** → link **Detalhes** na linha do pedido. Painéis: **Resumo** (Cliente, Data, Quantidade de itens, Valor total), **Itens** e, enquanto o pedido está **ABERTO**, **Pagamento**.

No painel **Pagamento**, **Valor** já vem com o total (não dá para editar). Escolha a **Forma de pagamento**:

**Pix** — pedido da Maria (`R$ 99,80`):

| Campo | Valor |
| :--- | :--- |
| Tipo da chave | `E-mail` (opções: E-mail, CPF, Telefone, Aleatória) |
| Chave Pix | `maria.silva@email.com` |

**Boleto** — pedido do João com a Caneca (`R$ 29,90`):

| Campo | Valor |
| :--- | :--- |
| Vencimento | uma data de hoje em diante, ex.: `31/12/2026` (o calendário não deixa escolher datas passadas) |
| Código de barras | `34191.79001 01043.510047` |

**Cartão de crédito** — pedido do João com a Camiseta (`R$ 49,90`):

| Campo | Valor |
| :--- | :--- |
| Bandeira | `Visa` (opções: Visa, Mastercard, Elo, Amex) |
| Últimos 4 dígitos | `4242` (só o final sai do navegador, já mascarado como `**** 4242`) |
| Parcelas | `3x` (de 1x a 12x) |

Clique em **Pagar R$ ...**.

- **Resultado esperado:** "Pagamento registrado com sucesso.", a etiqueta ao lado do número vira **PAGO**, o **Resumo** ganha **Forma de pagamento** e **Situação do pagamento** `APROVADO`, e o painel de pagamento some.
- Depois do Pix da Maria: 📸 `05-pedido-pago.png`

Erros comuns:

| Mensagem | Significado |
| :--- | :--- |
| `Informe a chave Pix.` | Chave Pix vazia. |
| `Informe o vencimento do boleto.` / `Informe o código de barras.` | Campo do boleto vazio. |
| `O vencimento não pode estar no passado.` | Data anterior a hoje. |
| `Informe os 4 últimos dígitos do cartão.` | Precisa ser exatamente 4 números. |
| `Pedido PED-2026-00001 já está pago e não pode ser pago de novo.` | O pedido já foi pago (por exemplo, em outra aba). Clique em **Atualizar**. |

### 7.6 Cancelamento

A tela não tem botão de cancelar; use a API. O bloco abaixo procura sozinho o pedido pago com cartão de crédito (o do João com a Camiseta), mostra o número e o cancela:

```powershell
$numero = ((Invoke-RestMethod "http://localhost:8080/api/pedidos?situacao=pago") | Where-Object { $_.formaPagamento.descricao -eq "CartaoCredito" } | Select-Object -First 1).numero
"Cancelando o pedido $numero"
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/pedidos/$numero" -ContentType "application/json" -Body '{"situacao": "CANCELADO"}' | Select-Object numero, situacao
```

- **Resultado esperado:** a resposta mostra o número e `CANCELADO`.
- **Deu errado:** `Cancelando o pedido` sem número = não há pedido pago com cartão; faça o pagamento com cartão da seção 7.5 antes. Clique em **Atualizar**: o pedido aparece como **CANCELADO**, o pagamento como **ESTORNADO** e o estoque da Camiseta volta.
- Cancelar de novo: erro `(409)`, motivo "Pedido já está cancelado".
- Pedido em aberto também pode ser cancelado (devolve o estoque, sem pagamento a estornar).

### 7.7 Dashboard com receita

Volte ao **Dashboard** (clique em **Atualizar** se os números não mudaram). A **Receita total** passa de `R$ 0,00`, **Pedidos recentes** lista os pedidos com o status de cada um e **Estoque crítico** mostra os produtos com menos de 15 unidades.

📸 `06-dashboard-com-receita.png` — no roteiro dos prints, tire logo depois do print 05 (só o pedido da Maria pago, receita `R$ 99,80`).

### 7.8 Pagamentos (rota `/pagamentos`)

Um cartão por pagamento: **Método** (Pix, Boleto, Cartão de crédito), etiqueta de situação (`APROVADO` ou `ESTORNADO`), o detalhe (`Chave: maria.silva@email.com`, `Código: 34191...`, `Visa •••• **** 4242`), o valor e a data/hora da transação. Sem pagamentos: "Nenhum pagamento registrado ainda.".

### 7.9 Histórico de compras (rota `/historico`)

**Atividades recentes**: os últimos 10 pedidos, do mais novo para o mais antigo, como "Pedido PED-2026-00001 criado", com o cliente, o status atual e a data. Vazio: "Nenhuma movimentação registrada no histórico.".

### 7.10 Print do erro de validação

Os campos do cliente são obrigatórios no navegador, então o envio 100% vazio é barrado antes de chegar à API. Para ver as mensagens da API embaixo dos campos, em **Clientes**, preencha **Nome**, **CPF** e **Endereço** com um espaço, **E-mail** com `maria@email` e **Telefone** com `abc`, e clique em **Cadastrar cliente**. Aparecem "Há campos inválidos. Corrija-os e tente novamente.", "Nome é obrigatório." e "Telefone deve conter apenas dígitos (8 a 20).".

📸 `07-erro-validacao.png`

Os prints `08-ci-vermelho.png` e `09-ci-verde.png` são do GitHub; o passo a passo está em [`prints/README.md`](prints/README.md).

## 8. Rodar os testes

**Back-end** (não precisa de MySQL nem de variáveis: os testes usam H2 em memória com as mesmas migrations):

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift"
mvn clean verify
```

- **Deu certo:** `All coverage checks have been met.` e `BUILD SUCCESS` no fim. O `verify` roda os testes de unidade, os de integração e as metas do JaCoCo (85% das linhas e 65% dos ramos); abaixo da meta, o build falha.
- **Deu errado:** `BUILD FAILURE` com `Tests run: ..., Failures: 1` indica qual teste falhou, logo acima; `Rule violated for bundle` indica cobertura abaixo da meta.
- Se a API estiver rodando no mesmo terminal, pare-a antes com `Ctrl+C` (o `clean` apaga a pasta `target` que ela usa).

Abrir o relatório de cobertura (unidade + integração somadas):

```powershell
Start-Process "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift\target\site\jacoco-total\index.html"
```

Há também `target\site\jacoco\` (só unidade) e `target\site\jacoco-it\` (só integração).

**Front-end:**

```powershell
Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\front-end"
npm run lint
npm run build
```

- **Deu certo:** o `lint` termina sem nenhuma linha de erro e o `build` termina com `✓ built in ...`.
- **Deu errado:** o `lint` lista `arquivo:linha` de cada problema; `'eslint' não é reconhecido` ou `'vite' não é reconhecido` = faltou o `npm ci` (seção 6).

Os mesmos comandos rodam no GitHub Actions em todo pull request (jobs `backend` e `frontend`).

## 9. Problemas comuns

| Erro | Causa | Solução |
| :--- | :--- | :--- |
| `mysql : O termo 'mysql' não é reconhecido...` | A pasta `bin` do MySQL não está no `PATH`. | Use o caminho completo, como nos blocos deste guia: `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" ...`. |
| `Access denied for user 'swift'@'localhost' (using password: YES)` | Senha do `DB_PASSWORD` diferente da criada na seção 3, ou existe um `swift@localhost` com outra senha (ele tem prioridade sobre o `swift@%`). | Confira com o passo de conferência do seu cenário (3A ou B3). Se houver um `swift localhost` sobrando, remova-o: no cenário B, `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h 127.0.0.1 -P 3307 -e "DROP USER 'swift'@'localhost';"`. |
| `Access denied for user '${DB_USERNAME}'@'localhost'` (o texto `${DB_USERNAME}` aparece literalmente) | A API não encontrou a variável: ela não foi definida neste terminal e não há `.env`, ou a API subiu de outra pasta e não achou o `.env`. | Defina as variáveis neste mesmo terminal (seção 4, forma 1) ou crie o `.env` e suba a API de `back-end\ecommerce-pedidos-swift` (seção 5). |
| `Plugin caching_sha2_password could not be loaded` | Você usou o `mysql` do XAMPP (cliente MariaDB), que não entende a autenticação padrão do MySQL 8. | Use o cliente do MySQL 8 pelo caminho completo: `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`. |
| `Communications link failure` na subida da API | O MySQL não está rodando ou o `DB_URL` aponta para a porta errada. | Cenário A: inicie o serviço. Cenário B: rode o passo B2 e confira que `DB_URL` tem `localhost:3307`. Cenário A não deve ter `DB_URL` de 3307 sobrando: `Remove-Item Env:DB_URL`. |
| `No plugin found for prefix 'spring-boot'` | O `mvn` foi rodado fora da pasta do back-end (onde está o `pom.xml`). | `Set-Location "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift"` e rode de novo. |
| `Web server failed to start. Port 8080 was already in use.` | Outra API (uma janela esquecida) já ocupa a porta. | Feche a outra janela ou pare o processo: `Get-NetTCPConnection -LocalPort 8080 -State Listen \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }`. Não suba a API em outra porta: o front chama sempre a 8080. |
| Vite: `Port 5173 is in use, trying another one...` (sobe na 5174) | Um `npm run dev` antigo ocupa a 5173. A API só libera CORS para `localhost:5173`, então as telas ficam sem dados. | Pare com `Ctrl+C` no `npm run dev` antigo e o novo também; depois: `Get-NetTCPConnection -LocalPort 5173 -State Listen \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` e `npm run dev` de novo. |
| `npm ci`: `EPERM: operation not permitted, unlink ...rolldown-binding...` | Um `npm run dev` rodando trava os arquivos do `node_modules`. | `Ctrl+C` no `npm run dev` e rode `npm ci` de novo. |
| `Start-Service : Falha ao iniciar o serviço 'MySQL80'` / acesso negado | Iniciar serviço exige administrador. | Peça a um administrador ou use o cenário B (instância própria na 3307, sem administrador). |
| Front abre, mas mostra "Não foi possível carregar: clientes, produtos, pedidos, pagamentos, histórico, resumo." e tudo zerado | A API está desligada (ou caiu). | Veja o terminal da API; suba de novo (seção 5). Teste com `Invoke-RestMethod http://localhost:8080/api/dashboard` e clique em **Atualizar** no front. |

### Recomeçar com o banco vazio

Útil para refazer os prints 01 a 06. Pare a API (`Ctrl+C`), apague e recrie o banco, e suba a API de novo (o Flyway recria as tabelas).

Cenário A (pede a senha do `swift`):

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u swift -p -e "DROP DATABASE ecommerce_swift; CREATE DATABASE ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Cenário B:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h 127.0.0.1 -P 3307 -e "DROP DATABASE ecommerce_swift; CREATE DATABASE ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

**Atenção:** apaga todos os clientes, produtos e pedidos. Na próxima subida, o log volta a mostrar `Successfully applied 3 migrations`.

## 10. Encerrar e sair do PC

1. **Parar o front:** na janela do `npm run dev`, aperte `Ctrl+C` (se perguntar `Deseja finalizar o arquivo em lotes (S/N)?`, responda `S`).
2. **Parar a API:** na janela do `mvn spring-boot:run`, aperte `Ctrl+C`. Deve aparecer `BUILD SUCCESS` ou o prompt de volta.
3. **Desligar a instância 3307 (só cenário B):**

   ```powershell
   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqladmin.exe" -u root -h 127.0.0.1 -P 3307 shutdown
   ```

   Não mostra nada quando dá certo. Confira com o comando de conferência do B2: deve dar `Can't connect`. Os dados continuam em `mysql-swift-data` para a próxima vez (comece pelo B2).

4. **PC compartilhado: apagar o seu login do GitHub** salvo pelo Git:

   ```powershell
   "protocol=https`nhost=github.com`n" | git credential-manager erase
   ```

   Não mostra nada quando dá certo. No próximo `git push` neste PC, o Git volta a pedir login.

5. Se criou o `.env` num PC compartilhado, apague-o (ele tem a senha do banco):

   ```powershell
   Remove-Item "$env:USERPROFILE\Desktop\ecommerce-pedidos-swift\back-end\ecommerce-pedidos-swift\.env"
   ```
