-- V4: controle de concorrência otimista (versao) e auditoria (atualizado_em).
-- atualizado_em é mantido pelo próprio MySQL a cada UPDATE na linha.
ALTER TABLE produto
    ADD COLUMN versao INT NOT NULL DEFAULT 0,
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE cliente
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE pedido
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX ix_pedido_situacao ON pedido (situacao);
