-- V1: esquema base de persistência (Aula 12).
-- As chaves estrangeiras são criadas com nome explícito para que a V2
-- possa redefinir as políticas de exclusão sem ambiguidade.

CREATE TABLE cliente (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(150) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL,
    email      VARCHAR(150) NOT NULL,
    telefone   VARCHAR(20)  NOT NULL,
    logradouro VARCHAR(200) NULL,
    cidade     VARCHAR(100) NULL,
    cep        VARCHAR(20)  NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uk_cliente_cpf UNIQUE (cpf),
    CONSTRAINT uk_cliente_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE produto (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    codigo                VARCHAR(50)   NOT NULL,
    nome                  VARCHAR(150)  NOT NULL,
    descricao             VARCHAR(500)  NULL,
    preco                 DECIMAL(10,2) NOT NULL,
    quantidade_em_estoque INT           NOT NULL,
    CONSTRAINT pk_produto PRIMARY KEY (id),
    CONSTRAINT uk_produto_codigo UNIQUE (codigo),
    CONSTRAINT ck_produto_preco CHECK (preco > 0),
    CONSTRAINT ck_produto_estoque CHECK (quantidade_em_estoque >= 0)
) ENGINE = InnoDB;

CREATE TABLE pedido (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    numero      VARCHAR(30) NOT NULL,
    cliente_id  BIGINT      NOT NULL,
    data_pedido DATE        NOT NULL,
    situacao    VARCHAR(20) NOT NULL,
    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) ENGINE = InnoDB;

-- preco_unitario guarda o preço praticado no momento da venda: o pedido
-- antigo continua exibindo o valor cobrado mesmo que o produto mude de preço.
CREATE TABLE item_pedido (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    pedido_id      BIGINT        NOT NULL,
    produto_id     BIGINT        NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_item_pedido PRIMARY KEY (id),
    CONSTRAINT uk_item_pedido_produto UNIQUE (pedido_id, produto_id),
    CONSTRAINT ck_item_pedido_quantidade CHECK (quantidade > 0),
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
) ENGINE = InnoDB;
