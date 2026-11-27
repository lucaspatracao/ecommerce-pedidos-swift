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

## Testar

```powershell
mvn clean test
```

* Perfil `test`: H2 em memória (`MODE=MySQL`) com as mesmas migrations do Flyway; não precisa de MySQL.
* O build **falha** se a cobertura de linhas do projeto (JaCoCo, BUNDLE) ficar abaixo de **70%**.
* Relatório: `target/site/jacoco/index.html`.

## Camadas

```text
controller → servico (interface + Impl, @Transactional) → repositorio (Spring Data JPA) → MySQL
                ↑ dto (requests com @Valid, responses com o contrato JSON)
                ↑ modelo (entidades com as regras de negócio)
```

Erros sempre no formato `{ "erro": "mensagem" }` (`controller/ApiExceptionHandler`).
