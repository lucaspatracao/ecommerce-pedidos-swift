# Back-end — API de Pedidos (Spring Boot)

Guia rápido do back-end. A visão geral do projeto, a equipe, o roadmap e a documentação completa estão no [README da raiz](../README.md).

## Stack

Java 17 · Spring Boot 3.3 (Web, Validation, Data JPA) · MySQL 8 · Flyway · JUnit 5 · Mockito · H2 · JaCoCo

## Rodar (PowerShell)

```powershell
# 1. Banco dedicado (uma vez); as tabelas são criadas pelo Flyway
mysql -u root -p -e "CREATE DATABASE ecommerce_swift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Credenciais (ou copie .env.example para .env)
cd back-end\ecommerce-pedidos-swift
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua-senha-local"
# Só se o MySQL não estiver em localhost:3306:
$env:DB_URL = "jdbc:mysql://localhost:3307/ecommerce_swift?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

# 3. Subir a API em http://localhost:8080/api
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

| Variável | Obrigatória? | Observação |
| :--- | :--- | :--- |
| `DB_URL` | Não no perfil `dev` | Padrão: `jdbc:mysql://localhost:3306/ecommerce_swift?...` |
| `DB_USERNAME` | Sim | |
| `DB_PASSWORD` | Sim | Sem valor padrão: nenhuma senha fica no repositório |

O `.env` (cópia do `.env.example`, formato `CHAVE=valor`) é lido da pasta em que a API é iniciada: rode os comandos a partir de `back-end\ecommerce-pedidos-swift`. Variável definida no terminal vale mais que a do `.env`.

Migrations do Flyway em `src/main/resources/db/migration` (V1 a V3). Nunca edite uma migration já aplicada: crie a próxima versão.

## Testar

```powershell
mvn clean test     # suíte de unidade
mvn clean verify   # unidade + integração (@Tag("integracao")) + regras de cobertura
```

* Perfil `test`: H2 em memória (`MODE=MySQL`) com as mesmas migrations do Flyway; não precisa de MySQL.
* O build **falha** se a cobertura ficar abaixo do mínimo: `modelo` e `excecao` com 70% das linhas na unidade (`test`); projeto inteiro com 85% das linhas e 65% dos ramos (`verify`).
* Relatórios: `target/site/jacoco/`, `jacoco-it/` e `jacoco-total/`.

CRUD completo, soft delete, políticas de exclusão e migrations: veja a seção "CRUD Completo, Integridade Referencial e Transações" no README da raiz.

## Camadas

```text
controller → servico (interface + Impl, @Transactional) → repositorio (Spring Data JPA) → MySQL
                ↑ dto (requests com @Valid, responses com o contrato JSON)
                ↑ modelo (entidades com as regras de negócio)
```

Erros sempre no corpo padronizado `{ status, erro, mensagem, caminho, campos }` (`dto/ErroResponse`, montado pelo `controller/ApiExceptionHandler`). CORS centralizado em `config/CorsConfig`. Tabela de rotas e exemplos no README da raiz.
