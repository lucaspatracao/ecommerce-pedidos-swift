-- =============================================================================
-- V3 - Número do pedido gerado pelo banco
--
-- Antes o número (PED-ano-NNNNN) era sorteado e o serviço sorteava de novo se
-- já existisse. Dois pedidos simultâneos ainda podiam sortear o mesmo número.
-- Agora cada ano tem um contador nesta tabela, lido COM BLOQUEIO de linha na
-- mesma transação do pedido: o próximo pedido espera e recebe o valor seguinte.
--
-- Por que uma tabela e não CREATE SEQUENCE? O MySQL 8 não tem sequences (o H2
-- dos testes tem). A tabela funciona igual nos dois.
-- =============================================================================

CREATE TABLE sequencia_pedido (
    ano     INT NOT NULL,
    ultimo  INT NOT NULL,
    CONSTRAINT pk_sequencia_pedido PRIMARY KEY (ano),
    CONSTRAINT ck_sequencia_pedido_ultimo CHECK (ultimo >= 0)
);

-- Os pedidos já gravados têm números sorteados, que poderiam colidir com a
-- sequência nova. Eles são renumerados no MESMO formato (PED-ano-NNNNN), na
-- ordem em que foram criados (id), e o contador de cada ano começa depois do
-- último número usado.
CREATE TABLE renumeracao_pedido AS
    SELECT id,
           YEAR(data_pedido) AS ano,
           ROW_NUMBER() OVER (PARTITION BY YEAR(data_pedido) ORDER BY id) AS seq
    FROM pedido;

-- Passo intermediário: valores únicos e fora do formato, para o UPDATE final
-- não esbarrar no UNIQUE de pedido.numero no meio da troca.
-- (CONCAT converte número em texto igual no MySQL e no H2; CAST AS CHAR
-- não: no H2 ele vira CHAR(1) e corta o valor.)
UPDATE pedido SET numero = CONCAT('TMP-', id);

UPDATE pedido
SET numero = (
    SELECT CONCAT('PED-', r.ano, '-', LPAD(CONCAT('', r.seq), 5, '0'))
    FROM renumeracao_pedido r
    WHERE r.id = pedido.id
);

INSERT INTO sequencia_pedido (ano, ultimo)
    SELECT ano, MAX(seq) FROM renumeracao_pedido GROUP BY ano;

DROP TABLE renumeracao_pedido;
