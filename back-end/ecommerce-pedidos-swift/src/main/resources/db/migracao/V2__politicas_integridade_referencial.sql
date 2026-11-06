-- V2: políticas explícitas de integridade referencial (Aula 13).
-- No MySQL não existe ALTER CONSTRAINT: a FK é removida e recriada com a
-- política desejada (em comandos separados para evitar conflito de nome).

-- Pedido -> ItemPedido: CASCADE. Composição: o item não existe sem o pedido.
ALTER TABLE item_pedido DROP FOREIGN KEY fk_item_pedido_pedido;
ALTER TABLE item_pedido
    ADD CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id)
        ON DELETE CASCADE ON UPDATE RESTRICT;

-- Cliente -> Pedido: RESTRICT. O histórico de vendas não pode sumir junto
-- com o cadastro; para "remover" um cliente com pedidos use o soft delete.
ALTER TABLE pedido DROP FOREIGN KEY fk_pedido_cliente;
ALTER TABLE pedido
    ADD CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Produto -> ItemPedido: RESTRICT + soft delete. Pedido antigo precisa do
-- produto vendido; produto nunca vendido pode ser excluído de fato.
ALTER TABLE item_pedido DROP FOREIGN KEY fk_item_pedido_produto;
ALTER TABLE item_pedido
    ADD CONSTRAINT fk_item_pedido_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
