import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.Produto;
import java.math.BigDecimal;

public class App {
    public static void main(String[] args) {
        System.out.println("=== DEMONSTRAÇÃO DE ENCAPSULAMENTO E VALIDAÇÃO ===\n");

        System.out.println("--- PRODUTO VÁLIDO ---");
        Produto produtoValido = new Produto("PROD-001", "Teclado Mecânico", new BigDecimal("249.90"), 10);
        System.out.println(produtoValido);

        System.out.println("\n--- VALIDAÇÃO DE PREÇO INVÁLIDO ---");
        try {
            new Produto("PROD-002", "Mouse", new BigDecimal("-9.99"), 5);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro capturado: " + e.getMessage());
        }

        System.out.println("\n--- BAIXA DE ESTOQUE ---");
        produtoValido.baixarEstoque(2);
        System.out.println("Estoque após baixa: " + produtoValido.getQuantidadeEmEstoque());

        System.out.println("\n--- TENTATIVAS INVALIDAS DE ESTOQUE ---");
        try {
            produtoValido.baixarEstoque(0);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro capturado: " + e.getMessage());
        }

        try {
            produtoValido.baixarEstoque(999);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro capturado: " + e.getMessage());
        }

        System.out.println("\n--- CLIENTE VÁLIDO ---");
        Cliente cliente = new Cliente("Maria da Silva", "12345678909", "maria@ecommerce.com", "(11) 99999-9999", "Rua A, 123");
        System.out.println(cliente.getIdentificacao());

        System.out.println("\n--- CLIENTE INVÁLIDO ---");
        try {
            new Cliente("", "12345678909", "email-invalido", "(11) 99999-9999", "Rua B, 321");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro capturado: " + e.getMessage());
        }

        System.out.println("\n--- PEDIDO E ITEM ---");
        Pedido pedido = new Pedido(cliente);
        pedido.adicionarItem(produtoValido, 3);
        System.out.println("Subtotal: R$ " + pedido.getItens().get(0).calcularSubtotal());
        System.out.println("Total do pedido: R$ " + pedido.calcularValorTotal());

        System.out.println("\n--- LISTA DE ITENS SOMENTE LEITURA ---");
        try {
            pedido.getItens().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("Lista protegida: " + e.getClass().getSimpleName());
        }

        System.out.println("Pedido: " + pedido.getSituacao());
    }
}