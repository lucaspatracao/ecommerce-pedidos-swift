-- =============================================================================
-- V2 - Soft delete, integridade referencial e auditoria
--
-- Porta para o Flyway as regras que a branch feature/crud-completo trouxe nas
-- migrations V2 a V4 dela. Mesmas regras de sempre: não edite a V1 já aplicada
-- e mantenha o SQL compatível com MySQL 8 (dev) e H2 em MODE=MySQL (testes).
-- =============================================================================

-- Soft delete de cliente. O produto já nasceu com "ativo" na V1.
-- Inativo sai das listagens padrão, mas continua no banco para os pedidos antigos.
ALTER TABLE cliente ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX ix_cliente_ativo ON cliente (ativo);
CREATE INDEX ix_produto_ativo ON produto (ativo);

-- Dois clientes não podem usar o mesmo e-mail.
ALTER TABLE cliente ADD CONSTRAINT uk_cliente_email UNIQUE (email);

-- Pedido -> ItemPedido: CASCADE. Composição: o item não existe sem o pedido.
-- Cliente -> Pedido e Produto -> ItemPedido continuam RESTRICT (padrão da V1):
-- o histórico de vendas não some junto com o cadastro; use o soft delete.
ALTER TABLE item_pedido DROP FOREIGN KEY fk_item_pedido_pedido;
ALTER TABLE item_pedido
    ADD CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id)
        ON DELETE CASCADE;

-- Um produto aparece uma vez por pedido: repetir o produto soma a quantidade
-- (regra de Pedido.adicionarItem).
ALTER TABLE item_pedido ADD CONSTRAINT uk_item_pedido_produto UNIQUE (pedido_id, produto_id);

-- Auditoria: o próprio banco marca a última alteração de cada linha.
ALTER TABLE cliente
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE produto
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE pedido
    ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX ix_pedido_situacao ON pedido (situacao);
