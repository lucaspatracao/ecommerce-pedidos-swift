package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.modelo.Boleto;
import com.ecommerce.pedidos.modelo.CartaoCredito;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.modelo.Pix;
import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
 */
@Component
public class FabricaFormaPagamento {

    private final Map<String, Function<PagamentoRequest, FormaPagamento>> construtores = Map.of(
        "CARTAO", req -> new CartaoCredito(valor(req), req.numero(), req.bandeira(), parcelas(req)),
        "BOLETO", req -> new Boleto(valor(req), vencimento(req), req.codigo()),
        "PIX", req -> new Pix(valor(req), req.chave(), req.tipoChave())
    );

    public FormaPagamento criar(PagamentoRequest request) {
        if (request == null || request.tipo() == null || request.tipo().isBlank()) {
            throw new IllegalArgumentException("Tipo de pagamento é obrigatório.");
        }
        Function<PagamentoRequest, FormaPagamento> construtor =
            construtores.get(request.tipo().trim().toUpperCase(Locale.ROOT));
        if (construtor == null) {
            throw new IllegalArgumentException("Tipo de pagamento inválido: " + request.tipo());
        }
        return construtor.apply(request);
    }

    // Os campos chegam como texto; a conversão falha com mensagem clara (400) em vez de erro 500.

    private static BigDecimal valor(PagamentoRequest req) {
        try {
            return new BigDecimal(Utils.textoObrigatorio(req.valor(), "Valor do pagamento"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor do pagamento inválido: " + req.valor());
        }
    }

    private static int parcelas(PagamentoRequest req) {
        try {
            return Integer.parseInt(Utils.textoObrigatorio(req.parcelas(), "Quantidade de parcelas"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantidade de parcelas inválida: " + req.parcelas());
        }
    }

    private static LocalDate vencimento(PagamentoRequest req) {
        try {
            return LocalDate.parse(Utils.textoObrigatorio(req.vencimento(), "Data de vencimento"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de vencimento inválida (use AAAA-MM-DD): " + req.vencimento());
        }
    }
}
