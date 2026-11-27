package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.modelo.Boleto;
import com.ecommerce.pedidos.modelo.CartaoCredito;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.modelo.Pix;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Transforma o texto "CARTAO" / "BOLETO" / "PIX" da requisição no objeto
 * {@link FormaPagamento} certo.
 *
 * <p>Em vez de um {@code switch}, cada tipo é uma entrada num mapa
 * (tipo → função construtora). Para aceitar um novo meio de pagamento basta
 * registrar mais uma entrada; nenhum {@code if}/{@code case} precisa mudar.
 * Daqui em diante o sistema só enxerga a abstração {@code FormaPagamento}.</p>
 *
 * <p>Os campos obrigatórios de cada tipo são validados pelo construtor da
 * subclasse (ex.: {@code Pix} exige chave), que lança 400 com a mensagem.</p>
 */
@Component
public class FabricaFormaPagamento {

    private final Map<String, Function<PagamentoRequest, FormaPagamento>> construtores = Map.of(
        "CARTAO", req -> new CartaoCredito(req.valor(), req.numero(), req.bandeira(), parcelas(req)),
        "BOLETO", req -> new Boleto(req.valor(), req.vencimento(), req.codigo()),
        "PIX", req -> new Pix(req.valor(), req.chave(), req.tipoChave())
    );

    public FormaPagamento criar(PagamentoRequest request) {
        if (request == null || request.tipo() == null || request.tipo().isBlank()) {
            throw new IllegalArgumentException("Tipo de pagamento é obrigatório.");
        }
        Function<PagamentoRequest, FormaPagamento> construtor =
            construtores.get(request.tipo().trim().toUpperCase(Locale.ROOT));
        if (construtor == null) {
            throw new IllegalArgumentException("Tipo de pagamento inválido: " + request.tipo()
                + ". Use CARTAO, BOLETO ou PIX.");
        }
        return construtor.apply(request);
    }

    private static int parcelas(PagamentoRequest req) {
        if (req.parcelas() == null) {
            throw new IllegalArgumentException("Quantidade de parcelas é obrigatória para cartão.");
        }
        return req.parcelas();
    }
}
