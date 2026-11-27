-- =============================================================================
-- V1 - Esquema inicial do e-commerce
--
-- Regras do Flyway:
--   * NUNCA edite este arquivo depois de aplicado: o Flyway guarda o checksum
--     e recusa subir se ele mudar. Alterações vão em um novo V2__..., V3__...
--   * O SQL precisa rodar no MySQL 8 (dev) e no H2 em MODE=MySQL (testes).
--
-- Escolhas de modelagem:
--   * Toda tabela tem "id" numérico como chave primária (surrogate key).
--     CPF, código do produto e número do pedido são chaves de NEGÓCIO: ficam
--     com UNIQUE, mas podem ser corrigidos sem quebrar as chaves estrangeiras.
--   * Enums são gravados como texto (VARCHAR) para o banco ficar legível.
-- =============================================================================

CREATE TABLE cliente (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    cpf         VARCHAR(11)  NOT NULL,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    -- Endereço é um @Embeddable: suas colunas moram na própria tabela cliente.
    logradouro  VARCHAR(200),
    cidade      VARCHAR(100),
    cep         VARCHAR(20),
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uk_cliente_cpf UNIQUE (cpf)
);

CREATE TABLE produto (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    codigo                 VARCHAR(40)   NOT NULL,
    nome                   VARCHAR(120)  NOT NULL,
    descricao              VARCHAR(500)  NOT NULL,
    preco                  DECIMAL(12,2) NOT NULL,
    quantidade_em_estoque  INT           NOT NULL,
    ativo                  BOOLEAN       NOT NULL,
    -- Controle de concorrência otimista (@Version): duas vendas simultâneas
    -- do mesmo produto não conseguem sobrescrever o estoque uma da outra.
    versao                 BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_produto PRIMARY KEY (id),
    CONSTRAINT uk_produto_codigo UNIQUE (codigo),
    CONSTRAINT ck_produto_preco CHECK (preco > 0),
    CONSTRAINT ck_produto_estoque CHECK (quantidade_em_estoque >= 0)
);

-- -----------------------------------------------------------------------------
-- Herança FormaPagamento com estratégia JOINED:
--   forma_pagamento guarda o que é comum; cada subclasse tem sua tabela com o
--   MESMO id (chave primária que também é chave estrangeira para a mãe).
-- -----------------------------------------------------------------------------
CREATE TABLE forma_pagamento (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    valor              DECIMAL(12,2) NOT NULL,
    data_do_pagamento  DATETIME(6)   NOT NULL,
    situacao           VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_forma_pagamento PRIMARY KEY (id)
);

CREATE TABLE cartao_credito (
    id                      BIGINT        NOT NULL,
    numero_mascarado        VARCHAR(30)   NOT NULL,
    bandeira                VARCHAR(30)   NOT NULL,
    quantidade_de_parcelas  INT           NOT NULL,
    limite_disponivel       DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_cartao_credito PRIMARY KEY (id),
    CONSTRAINT fk_cartao_credito_forma FOREIGN KEY (id) REFERENCES forma_pagamento (id)
);

CREATE TABLE boleto (
    id                  BIGINT      NOT NULL,
    codigo_de_barras    VARCHAR(60) NOT NULL,
    data_de_vencimento  DATE        NOT NULL,
    CONSTRAINT pk_boleto PRIMARY KEY (id),
    CONSTRAINT fk_boleto_forma FOREIGN KEY (id) REFERENCES forma_pagamento (id)
);

CREATE TABLE pix (
    id            BIGINT       NOT NULL,
    chave         VARCHAR(140) NOT NULL,
    tipo_da_chave VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_pix PRIMARY KEY (id),
    CONSTRAINT fk_pix_forma FOREIGN KEY (id) REFERENCES forma_pagamento (id)
);

CREATE TABLE vale_compras (
    id       BIGINT      NOT NULL,
    codigo   VARCHAR(40) NOT NULL,
    emissor  VARCHAR(80) NOT NULL,
    CONSTRAINT pk_vale_compras PRIMARY KEY (id),
    CONSTRAINT fk_vale_compras_forma FOREIGN KEY (id) REFERENCES forma_pagamento (id)
);

-- Pedido N–1 Cliente e Pedido 1–1 FormaPagamento (UNIQUE garante o "1–1").
CREATE TABLE pedido (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    numero              VARCHAR(20) NOT NULL,
    cliente_id          BIGINT      NOT NULL,
    data_pedido         DATE        NOT NULL,
    situacao            VARCHAR(20) NOT NULL,
    forma_pagamento_id  BIGINT,
    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT uk_pedido_forma_pagamento UNIQUE (forma_pagamento_id),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_pedido_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento (id)
);

-- Pedido 1–N ItemPedido e ItemPedido N–1 Produto.
-- Sem ON DELETE CASCADE no produto: um produto já vendido não pode sumir do
-- histórico. O serviço responde 409 e sugere desativar o produto.
CREATE TABLE item_pedido (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    pedido_id        BIGINT        NOT NULL,
    produto_id       BIGINT        NOT NULL,
    quantidade       INT           NOT NULL,
    preco_praticado  DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_item_pedido PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT ck_item_pedido_quantidade CHECK (quantidade > 0)
);
