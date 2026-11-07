-- V3: soft delete. Registros inativos saem das consultas padrão, mas
-- continuam no banco para preservar o histórico dos pedidos.
ALTER TABLE produto ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE cliente ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX ix_produto_ativo ON produto (ativo);
CREATE INDEX ix_cliente_ativo ON cliente (ativo);
