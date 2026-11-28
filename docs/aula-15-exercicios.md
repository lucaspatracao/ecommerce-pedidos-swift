# Aula 15: exercícios resolvidos (API REST e CI/CD)

**Squad Swift.** Cada resposta aponta onde a mesma ideia está no nosso projeto. Os caminhos Java são relativos a `back-end/ecommerce-pedidos-swift/src/main/java/com/ecommerce/pedidos/`. As rotas reais usam o prefixo `/api` e os identificadores naturais: `cpf` (cliente), `codigo` (produto) e `numero` (pedido).

Exemplos da aula citados: 1 e 3 (endereços REST), 2 (status HTTP), 4 (corpo de erro padronizado), 5 (tratador de exceções) e 6 (pipeline de CI).

---

## E1. Redesenhe os endereços da loja

### a) Método + endereço REST

| # | Legado | REST | No nosso projeto |
| :---: | :--- | :--- | :--- |
| 1 | `POST /cadastrarCliente` | `POST /clientes` → 201 + `Location` | `POST /api/clientes` |
| 2 | `GET /listarClientes` | `GET /clientes` | `GET /api/clientes` |
| 3 | `GET /buscarCliente?id=7` | `GET /clientes/7` | `GET /api/clientes/{cpf}` |
| 4 | `POST /atualizarEmailDoCliente?id=7` | `PATCH /clientes/7` com `{ "email": "..." }` | não temos PATCH de e-mail; o e-mail muda pelo `PUT /api/clientes/{cpf}` (veja c) |
| 5 | `GET /excluirCliente?id=7` | `DELETE /clientes/7` → 204 | `DELETE /api/clientes/{cpf}` |
| 6 | `GET /pedidosDoCliente?id=7` | `GET /clientes/7/pedidos` | `GET /api/clientes/{cpf}/pedidos` |
| 7 | `POST /cancelarPedido?id=1048` | `PATCH /pedidos/1048` com `{ "situacao": "CANCELADO" }` | `PATCH /api/pedidos/{numero}` |

A regra: o endereço é o **substantivo** (o recurso, no plural, com o identificador no caminho), e a ação fica no **método HTTP**. Em `controller/ClienteController.java` e `controller/PedidoController.java` não sobrou nenhum verbo no endereço.

### b) A linha perigosa

A **linha 5, `GET /excluirCliente?id=7`**. GET é, por contrato, *seguro*: só lê, não altera nada. Por isso qualquer coisa se sente livre para chamá-lo sozinha:

* o navegador, ao pré-carregar links;
* um rastreador de buscadores, seguindo links de uma página;
* um proxy ou cache, repetindo a requisição;
* o histórico ou o autocompletar da barra de endereço;
* uma tag `<img src="/excluirCliente?id=7">` numa página maliciosa, que dispara a requisição com o login de quem a abriu.

Basta um desses acessos para **apagar clientes sem ninguém clicar em "excluir"**. Exclusão é `DELETE`, que nenhum desses agentes envia sozinho.

### c) Linha 4: PUT ou PATCH?

**PATCH.** `PUT` substitui o recurso **inteiro**: o corpo deveria trazer todos os campos do cliente, e um campo omitido seria apagado. Para trocar **só** o e-mail, `PATCH /clientes/7` com `{ "email": "novo@x.com" }` deixa claro que é uma alteração parcial.

No nosso projeto, o `PUT /api/clientes/{cpf}` recebe o `ClienteRequest` completo (nome, e-mail, telefone e endereço; o CPF vem da rota) e atualiza o contato todo. O e-mail também passa pela regra de duplicidade: em uso → 409. O PATCH que temos em cliente é o de `{ "ativo": true|false }`, para inativar e reativar.

### d) Duas formas de modelar o cancelamento

1. **Alterar o estado do recurso:** `PATCH /pedidos/1048` com `{ "situacao": "CANCELADO" }`. O cancelamento é uma mudança no próprio pedido.
2. **Criar um sub-recurso que representa a ação:** `POST /pedidos/1048/cancelamento`, que cria "o cancelamento do pedido" (com motivo, data, quem cancelou) e responde 201.

**Adotamos a 1:** `PATCH /api/pedidos/{numero}` (`PedidoController.atualizarSituacao`, corpo `dto/AtualizarSituacaoRequest`). Motivos:

* No nosso domínio, cancelar é só uma transição da `situacao` do pedido (`ABERTO`/`PAGO` → `CANCELADO`). Não há dados próprios do cancelamento para guardar como recurso.
* O mesmo endereço serve para outras transições: `ABERTO` responde 200 sem mudança, e `PAGO` responde 409, porque pagar é pelo sub-recurso de pagamento.
* O comentário no controller registra a escolha: PATCH, e não PUT, porque só muda a situação.

O efeito colateral fica no serviço (`PedidoServiceImpl.cancelar`): devolve o estoque e estorna o pagamento. Cancelar de novo responde 409. A forma 2 faria sentido se um dia o cancelamento tiver motivo, histórico ou aprovação.

---

## E2. Escolha o status de cada situação

### a) Status

| # | Situação | Status | No nosso projeto |
| :---: | :--- | :---: | :--- |
| 1 | `GET /produtos/42`, existe | **200 OK** | `GET /api/produtos/{codigo}` |
| 2 | `POST /produtos`, criado | **201 Created** + `Location` | `ProdutoController.cadastrar` com `UriComponentsBuilder` |
| 3 | `DELETE /produtos/42`, removido | **204 No Content** | `DELETE /api/produtos/{codigo}` |
| 4 | `GET /produtos/999`, não existe | **404 Not Found** | `ProdutoNaoEncontradoException` |
| 5 | `POST /clientes`, e-mail sem @ | **400 Bad Request** | `@Email` no `ClienteRequest` → `campos` |
| 6 | `POST /clientes`, e-mail já cadastrado | **409 Conflict** | `RegistroDuplicadoException` |
| 7 | `POST /pedidos/7/pagamento`, já pago | **409 Conflict** | `PedidoJaPagoException` |
| 8 | `NullPointerException` no serviço | **500 Internal Server Error** | `ApiExceptionHandler.tratarErroInesperado` |

Os testes `integracao/StatusHttpApiTest.java` e `integracao/ErroPadronizadoApiTest.java` (em `src/test/java/...`) conferem esses status.

### b) Idempotência

* `PUT /produtos/42`: **idempotente.** Mandar o mesmo produto duas vezes deixa o mesmo estado final.
* `DELETE /produtos/42`: **idempotente.** Depois da primeira chamada, o produto não existe; a segunda não muda nada. A *resposta* pode mudar (no nosso projeto, 204 e depois 404), mas o **estado** é o mesmo.
* `POST /pedidos`: **não é idempotente.** Cada chamada cria um pedido novo, com número novo, e baixa o estoque de novo. Duas chamadas, dois pedidos.

Por isso um clique duplo em "Salvar pedido" é perigoso. O `NovoPedidoPage.jsx` desabilita o botão enquanto envia (`disabled={enviando}`).

### c) 200 OK com `{ "erro": "Cliente não encontrado" }`

O status mente. Para o HTTP, a operação deu certo, e quem consome confia no status, não no texto:

* o `axios` do front (e qualquer cliente HTTP) entra no caminho de **sucesso**; o `catch` nunca roda, e a tela trata `{ erro }` como se fosse um cliente;
* caches e proxies podem **guardar** essa resposta de "sucesso";
* monitoramento e logs contam como acerto, e o problema some das métricas;
* cada consumidor precisa inventar a regra "olhar se veio `erro` no corpo".

O certo é **404** com um corpo de erro padronizado. Nenhuma rota nossa responde 200 com erro: tudo passa pelo `ApiExceptionHandler`, que devolve 4xx/5xx com `dto/ErroResponse`.

### d) Situação 5 × situação 6

* **5, e-mail sem @ → 400.** A requisição está **malformada em si mesma**. Dá para saber que está errada sem consultar nada; o problema é do dado enviado. No nosso projeto, o `@Email(message = "E-mail inválido.")` do `ClienteRequest` barra antes do serviço, e a resposta traz `campos: [{ "campo": "email", "mensagem": "E-mail inválido." }]`.
* **6, e-mail já cadastrado → 409.** A requisição está **correta**, mas **conflita com o estado atual** do servidor. Ontem seria aceita, e amanhã também, se o outro cliente trocar de e-mail. Só se descobre consultando o banco. No nosso projeto, o `ClienteServiceImpl` consulta o repositório e lança `RegistroDuplicadoException`.

Em resumo: o 400 diz "corrija o que você mandou", e o 409 diz "o que você mandou está certo, mas choca com os dados que já existem".

---

## E3. Conserte o controlador de clientes

### a) O que o cliente recebe

* **E-mail duplicado:** o método devolve `null`. O Spring responde **200 OK com corpo vazio**. Quem chamou acha que deu certo e não sabe o motivo.
* **id 999:** `findById(999)` devolve `Optional.empty()`, e o `.get()` lança `NoSuchElementException`. O `catch` captura e devolve a *String* `"Cliente não encontrado"`. O resultado é **200 OK** com o texto puro `Cliente não encontrado` no corpo, como `text/plain`, e não JSON.

### b) Defeitos

| Defeito | Por que é problema | Exemplo da aula |
| :--- | :--- | :--- |
| Verbo no endereço (`/cadastrarCliente`), com o recurso no singular e plural misturados (`/clientes/{id}`) | A ação é o método HTTP; o endereço é o recurso | Exemplos 1 e 3 |
| POST que cria responde 200, sem `Location` | Deveria ser 201 + `Location` + o recurso criado | Exemplo 2 |
| Duplicado vira 200 com `null` e "não encontrado" vira 200 com texto | Status mentindo; deveriam ser 409 e 404 | Exemplo 2 |
| Corpo de erro improvisado (`null`, String solta, `Object` como tipo de retorno) | Cada erro em um formato; o front não consegue tratar | Exemplo 4 |
| `try/catch (Exception e)` no controller | Engole **qualquer** erro (até um bug de banco vira "não encontrado") e espalha tratamento por todo controller | Exemplo 5 |
| Controller acessa o `ClienteRepository` e decide a regra (e-mail duplicado) | A regra de negócio fica na camada HTTP; não dá para reaproveitar nem testar isolada | Controller só traduz |
| Recebe e devolve a **entidade** `Cliente` | Expõe campos internos (id, senha, flags) e aceita campos que o cliente não deveria definir (`id`, `ativo`); muda o JSON a cada mudança da entidade | DTOs (veja o E4) |
| Sem `@Valid` | Nome vazio, e-mail inválido etc. chegam ao banco | Validação na entrada |
| `@Autowired` em campo | Dependência escondida; o teste unitário fica mais difícil (preferir injeção por construtor) | Boa prática |
| Sem `@RequestMapping("/api/...")` | Sem prefixo comum para a API | Convenção do projeto |

### c) Versão corrigida

Com `ServicoCliente`, DTOs e o tratador do Exemplo 5:

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ServicoCliente servico;

    public ClienteController(ServicoCliente servico) {   // injeção por construtor
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request,
                                                     UriComponentsBuilder uri) {
        ClienteResponse criado = servico.cadastrar(request);   // e-mail duplicado → EmailJaCadastradoException (409)
        URI endereco = uri.path("/api/clientes/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(endereco).body(criado);  // 201 + Location
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable("id") Long id) {
        return servico.buscar(id);                             // inexistente → ClienteNaoEncontradoException (404)
    }

    @DeleteMapping("/{id}")                                    // d)
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        servico.excluir(id);                                   // inexistente → 404
        return ResponseEntity.noContent().build();             // 204
    }
}

@Service
@Transactional(readOnly = true)
public class ServicoCliente {
    private final ClienteRepository repositorio;

    public ServicoCliente(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        if (repositorio.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }
        return ClienteResponse.de(repositorio.save(request.paraEntidade()));
    }

    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(repositorio.findById(id)
            .orElseThrow(() -> new ClienteNaoEncontradoException(id)));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = repositorio.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException(id));
        repositorio.delete(cliente);
    }
}
```

As exceções viram resposta no tratador (veja o E5), sem nenhum `try/catch` no controller.

**No nosso projeto:** `controller/ClienteController.java` segue exatamente esse formato.

* Delega a `servico/ClienteService` (interface) e `servico/ClienteServiceImpl`.
* Recebe `@Valid ClienteRequest` e devolve `ClienteResponse`.
* `POST` → 201 + `Location: /api/clientes/{cpf}`.
* O identificador é o **CPF**, e não o id do banco.
* E-mail ou CPF duplicado → `RegistroDuplicadoException` (409).
* Inexistente → `ClienteNaoEncontradoException` (404).

### d) `DELETE /clientes/{id}`

Está no código acima: 204 sem corpo, ou 404 se o cliente não existir. No nosso projeto, `DELETE /api/clientes/{cpf}` também responde **409** quando o cliente tem pedidos ("Este cliente possui pedidos e não pode ser excluído. Inative-o..."). O histórico de vendas não pode sumir, e a alternativa é `PATCH /api/clientes/{cpf}` com `{"ativo": false}` (soft delete).

---

## E4. Desenhe o contrato do cliente

### a) `ClienteRequest`

```java
public record ClienteRequest(
    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
    String nome,

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,

    @NotBlank(message = "CPF é obrigatório.")
    @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF deve ter 11 dígitos.")
    String documento,

    @NotBlank(message = "Senha é obrigatória.")
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres.")
    String senha
) {}
```

* Só entra o que **quem consome pode definir**.
* `senha` chega em texto e o serviço gera o `senhaHash`. O hash nunca vem de fora.
* Ficam de fora `id`, `ativo`, `dataCadastro` e `tentativasDeLogin`, que o servidor decide.
* Os dígitos verificadores do CPF são regra de domínio e ficam no serviço ou no domínio.

**No nosso projeto:** `dto/ClienteRequest.java` usa as mesmas anotações em `nome` e `email`, `@Pattern` no `telefone` e `@Size` no `endereco`. O CPF é validado pelos dígitos verificadores no domínio (`Utils.validarCpf`), porque a regra vale para qualquer origem, e não só para a API. Não temos senha (a API não tem login).

### b) `ClienteResponse`

```java
public record ClienteResponse(Long id, String nome, String email, String documento,
                              boolean ativo, LocalDateTime dataCadastro) {}
```

| Campo | Fica? | Por quê |
| :--- | :---: | :--- |
| `id`, `nome`, `email`, `documento` | ✅ | Quem consome precisa para exibir e identificar |
| `ativo`, `dataCadastro` | ✅ | Úteis na tela de administração; não são sensíveis |
| `senhaHash` | ❌ | **Nunca** sai: mesmo um hash ajuda ataque de força bruta offline |
| `tentativasDeLogin` | ❌ | Dado interno de segurança: revela quais contas estão sob ataque ou perto do bloqueio |

Pode-se discutir mascarar o `documento` (`111.***.***-44`) em listagens públicas.

**No nosso projeto:** `dto/ClienteResponse.java` devolve `nome`, `email`, `telefone`, `endereco` (como `EnderecoResponse`), `cpf`, `resumo` e `ativo`. O `id` do banco **não** sai, porque o CPF é o identificador da API.

### c) Status e corpo da requisição do enunciado

`{ "nome": "", "email": "ana@", "documento": "111.222.333-44", "senha": "123" }` falha na Bean Validation antes de chegar ao serviço: **400 Bad Request**. No formato do nosso `ErroResponse`:

```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Há campos inválidos. Corrija-os e tente novamente.",
  "caminho": "/clientes",
  "campos": [
    { "campo": "email", "mensagem": "E-mail inválido." },
    { "campo": "nome",  "mensagem": "Nome é obrigatório." },
    { "campo": "senha", "mensagem": "Senha deve ter entre 8 e 72 caracteres." }
  ]
}
```

* O `documento` passa no `@Pattern` (formato com 11 dígitos). Se os dígitos verificadores forem inválidos, o domínio recusa, mas só depois da validação de formato.
* No nosso projeto, um CPF com dígitos errados vira 400 com `mensagem: "CPF inválido: ..."` (por `IllegalArgumentException`), sem `campos`.
* A ordem de `campos` é alfabética porque o `ApiExceptionHandler` ordena por nome do campo.
* No front, cada mensagem aparece embaixo do campo correspondente (`components/ErroCampo.jsx`).

### d) Renomear `email` para `emailPrincipal` na entidade

* **Com DTO:** o aplicativo **não percebe nada**. O JSON continua `"email"`, porque o contrato é o `record`. Muda só uma linha no mapeamento (`ClienteResponse.de` e a conversão do request): `cliente.getEmailPrincipal()`. Os testes de API continuam verdes.
* **Sem DTO** (entidade serializada direto): o JSON passa a ter `"emailPrincipal"`. **Quebra o aplicativo**: a tela lê `cliente.email` e mostra vazio, e o cadastro envia `email`, que o Jackson ignora; o e-mail chega nulo. Cada consumidor (web, mobile, integrações) teria de mudar junto, no mesmo deploy.

Foi o que o nosso projeto evitou. Em `dto/ClienteResponse.java`, o comentário registra que o record "fixa o contrato".

---

## E5. Mapeie as exceções da loja

### a) Status

| Exceção | Status | No nosso projeto |
| :--- | :---: | :--- |
| `ClienteNaoEncontradoException` | **404** | `excecao/ClienteNaoEncontradoException` |
| `PedidoNaoEncontradoException` | **404** | `excecao/PedidoNaoEncontradoException` |
| `EmailJaCadastradoException` | **409** | equivalente: `excecao/RegistroDuplicadoException` (CPF, e-mail ou código) |
| `EstoqueInsuficienteException` | **409** | `excecao/EstoqueInsuficienteException` |
| `PedidoJaPagoException` | **409** | `excecao/PedidoJaPagoException` |
| `PagamentoRecusadoException` | **409** | `excecao/PagamentoRecusadoException` |
| `MethodArgumentNotValidException` | **400** (com `campos`) | `ApiExceptionHandler.handleMethodArgumentNotValid` |
| `NullPointerException` | **500** (mensagem genérica) | `ApiExceptionHandler.tratarErroInesperado` |

Pagamento recusado poderia ser 402 ou 422. Adotamos 409, como o restante dos conflitos de regra, e o `ErroPadronizadoApiTest` confere.

### b) `TratadorDeErros`

```java
public abstract class RecursoNaoEncontradoException extends RuntimeException {
    protected RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado: " + id);
    }
}
public class ClienteNaoEncontradoException extends RecursoNaoEncontradoException { /* super("Cliente", id) */ }
public class PedidoNaoEncontradoException  extends RecursoNaoEncontradoException { /* super("Pedido", id) */ }

public abstract class ConflitoException extends RuntimeException { /* ... */ }
// EmailJaCadastrado, EstoqueInsuficiente, PedidoJaPago e PagamentoRecusado estendem ConflitoException

@RestControllerAdvice
public class TratadorDeErros {
    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)        // UM handler para todos os 404
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ConflitoException.class)                    // UM handler para todos os 409
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest req) {
        return erro(HttpStatus.CONFLICT, "Conflito", ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> new CampoInvalido(f.getField(), f.getDefaultMessage())).toList();
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida",
            "Há campos inválidos. Corrija-os e tente novamente.", req, campos);
    }

    @ExceptionHandler(Exception.class)                            // rede de proteção (NPE e afins)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        LOG.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
            "Erro interno. Tente novamente mais tarde.", req, null);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus s, String rotulo, String msg,
                                              HttpServletRequest req, List<CampoInvalido> campos) {
        return ResponseEntity.status(s).body(new ErroResponse(s.value(), rotulo, msg, req.getRequestURI(), campos));
    }
}
```

O Spring escolhe o handler da exceção **mais específica**. Com a superclasse, cada "não encontrado" novo, como um `ProdutoNaoEncontradoException`, já cai no 404 sem mexer no tratador.

**No nosso projeto:** `controller/ApiExceptionHandler.java` faz isso.

* Um handler para `RecursoNaoEncontradoException`, superclasse de `Cliente`/`Produto`/`Pedido`/`PagamentoNaoEncontradoException`.
* Um para `ConflitoException` e subclasses.
* Ele estende `ResponseEntityExceptionHandler`, então os erros do próprio Spring (JSON malformado, 405, 415) também saem no formato `ErroResponse`.

### c) Mensagem do 500 e onde fica o detalhe

* **No corpo:** só a mensagem genérica, **"Erro interno. Tente novamente mais tarde."**. Nada de nome de classe, SQL, caminho de arquivo ou stack trace, que ajudariam um atacante e não ajudam o usuário.
* **O detalhe:** no **log do servidor**, com a exceção completa (`LOG.error(..., ex)`, que imprime o stack trace), junto do método e do endereço da requisição.

O `ErroPadronizadoApiTest.erroInternoNaoVaza` prova isso. Ele força uma exceção com SQL na mensagem e confere que o corpo não contém `Exception`, `org.hibernate`, `select` nem `at com.ecommerce`.

### d) Por que não try/catch em cada controlador

* **Repetição:** o mesmo bloco em todo método de todo controller, e um dia um deles responde diferente dos outros.
* **Inconsistência:** cada um inventa status e corpo (foi o que vimos no E3: 200 com texto).
* **Erros engolidos:** `catch (Exception e)` captura também bugs reais e os disfarça ("não encontrado"), e o problema some do log.
* **Transação:** se o controller captura a exceção antes, o serviço já fez rollback, mas a resposta diz que deu certo. Capturar dentro do serviço pode impedir o rollback do `@Transactional`, que só acontece quando a exceção *sai* do método.
* **Mistura de responsabilidades:** o controller deve só traduzir HTTP. A tradução de erro é uma regra só, num lugar só (`@RestControllerAdvice`), e dá para testar isolada (`ApiExceptionHandlerTest`).

Nos nossos controllers não há nenhum `try/catch` (`controller/*Controller.java`).

---

## E6. O pipeline sempre verde

### a) A linha que explica

```yaml
        run: mvn -B package -DskipTests
```

O **`-DskipTests`** pula todos os testes. O pipeline só confere se o código **compila**, e por isso nunca reprovou. Pior: o selo verde passava a impressão de que os testes passavam. O `package` também para antes da fase `verify`, onde ficam os testes de integração (Failsafe) e as metas de cobertura do JaCoCo.

### b) Demais defeitos e o arquivo reescrito

| Defeito | Problema |
| :--- | :--- |
| `on: push` só na `main` | Não roda em **pull request**: o erro só aparece **depois** do merge, quando já quebrou a `main` |
| `mvn package` em vez de `verify` | Pula os testes de integração e as metas de cobertura |
| `DB_PASSWORD: admin123` no YAML | **Segredo no repositório**: fica no histórico do Git para sempre, visível a quem tem acesso; precisa ser trocado. (Se um dia precisar de segredo, use `${{ secrets.NOME }}`) |
| Teste dependendo de um MySQL real | No runner não há MySQL (é o erro do item c) |
| Sem `cache: maven` | Baixa todas as dependências a cada execução: lento |
| Front-end fora do pipeline | Lint e build do React nunca são conferidos |
| Sem artefatos | Relatório de cobertura e `.jar` se perdem com o runner |
| Sem `working-directory` | Num monorepo como o nosso, o `pom.xml` não está na raiz |

O arquivo corrigido é o nosso [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) (resumo abaixo; o arquivo real também tem `permissions`, `concurrency` e nomes de passos):

```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:

jobs:
  backend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: back-end/ecommerce-pedidos-swift
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven
      - run: mvn -B verify          # unidade + integração + cobertura; sem -DskipTests
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: jacoco
          path: back-end/ecommerce-pedidos-swift/target/site/jacoco-total/
      - uses: actions/upload-artifact@v4
        with:
          name: aplicacao-jar
          path: back-end/ecommerce-pedidos-swift/target/*.jar

  frontend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: front-end
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: npm
          cache-dependency-path: front-end/package-lock.json
      - run: npm ci
      - run: npm run lint
      - run: npm run build
```

Sem nenhum segredo: os testes não precisam de banco externo (item c).

### c) `ProdutoRepositoryIT » Communications link failure`

Agora os testes rodam, e o `ProdutoRepositoryIT` tenta abrir conexão com um **MySQL que não existe no runner** do GitHub. O `localhost:3306` da máquina virtual está vazio. `Communications link failure` é a mensagem do driver MySQL quando nem consegue falar com o servidor. Não é senha errada: não há nada escutando. O teste "funcionava" na máquina do desenvolvedor só porque lá o MySQL estava ligado.

Três saídas:

1. **H2 em memória no perfil `test`.** Os testes sobem com `@ActiveProfiles("test")`, apontando para um H2 `MODE=MySQL`. O Flyway roda as mesmas migrations e o Hibernate valida o schema (`ddl-auto=validate`). Rápido e sem dependência externa. O custo: o H2 não é o MySQL, e uma diferença de dialeto pode escapar.
2. **Testcontainers.** O próprio teste sobe um MySQL 8 real em Docker (`@Testcontainers` + `MySQLContainer`), que o `ubuntu-latest` já suporta. É fiel ao banco de produção. Custa uma dependência a mais e testes mais lentos, e exige Docker na máquina de quem roda.
3. **Serviço MySQL no workflow.** Um bloco `services: mysql:` no job, com porta e healthcheck, e o `DB_URL` apontando para ele. É o MySQL real, mas só no CI. A senha, mesmo sendo de um banco descartável, deveria ir para `secrets` ou ser gerada no workflow, e localmente o desenvolvedor continua precisando do MySQL ligado.

**Adotamos a 1.** `src/test/resources/application-test.properties` usa `jdbc:h2:mem:ecommerce_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`.

* Todo teste com contexto Spring tem `@ActiveProfiles("test")`.
* O `MigracaoNumeroPedidoTest` abre o próprio H2.
* Nenhum teste depende de MySQL. Conferimos isso ao montar o pipeline.
* Resultado: o CI não tem nenhum segredo, e o `mvn clean verify` também roda sem banco na máquina de qualquer um.
* Testcontainers fica como próximo passo para ganhar fidelidade ao MySQL.

### d) Como provar que o pipeline corrigido funciona

Um pipeline que nunca fica vermelho não prova nada. A prova é **fazê-lo reprovar de propósito**:

1. criar uma branch só com um teste que falha de propósito;
2. abrir um PR e mostrar o check **vermelho**, com o log apontando o teste;
3. **fechar o PR sem merge** e mostrar que um PR normal fica verde.

**O que aconteceu no nosso projeto.** O histórico do Git registra o caso:

* Criamos a branch `test/ci-vermelho` com um único commit (`09dd793`), que adiciona `ProvaPipelineTest` com `assertEquals(1, 2)`, comentado como prova do pipeline.
* O PR #24 foi aberto, e o CI reprovou, como deveria. O pipeline reprova.
* Mas o PR #24 **foi mergeado por engano** (merge `8225b08`). A `main` passou a ter o teste quebrado, e o pipeline da `main` ficou vermelho.
* A correção não reescreveu o histórico, sem force push. O commit `4f6cc1c` (`revert: remove o teste de prova do CI mergeado por engano`) desfaz o merge com `git revert -m 1 8225b08`. Ele foi publicado na branch `fix/remove-teste-prova` para tirar a `main` do vermelho logo, e é o mesmo commit que abre a `docs/entrega-final`.

**Lição:** o CI **detecta**, mas não **impede**. A regra "PR com pipeline vermelho não é integrado" estava só no README. O passo que falta é a proteção da branch `main` no GitHub, exigindo o check `CI` aprovado (e uma revisão) antes de liberar o botão de merge. Assim o erro humano deixa de ser possível.
