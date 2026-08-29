import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;

public class App {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE COMPROVACAO DE DOMINIO EM POO (AULA 04) ===\n");

        Produto p1 = new Produto("PER-001", "Teclado Mecanico RGB", 250.00, 15);
        Produto p2 = new Produto("MON-002", "Monitor Gamer UltraWide 29", 1200.00, 5);

        System.out.println("--- CATALOGO DE PRODUTOS CADASTRADOS ---");
        System.out.println(p1);
        System.out.println(p2);
        System.out.println("Total de instancias de produtos na memoria: " + Produto.getTotalDeProdutosCriados() + "\n");

        Cliente c1 = new Cliente("Lucas Nunes Patracao", "123.456.789-00", "lucas.n.patracao@gmail.com",
                "(16) 99999-9999", "Rua do E-commerce, 123 - Centro");

        System.out.println("--- CLIENTE LOCALIZADO ---");
        System.out.println(c1);
        System.out.println("Nome de Identificacao (Metodo de Negocio): " + c1.getIdentificacao() + "\n");

        Pedido pedido = new Pedido(c1);

        System.out.println("--- INICIALIZACAO DO NOVO PEDIDO ---");
        System.out.println("Codigo gerado pelo PedidoUtils: " + pedido.getNumero());
        System.out.println("Situacao inicial padrao: " + pedido.getSituacao() + "\n");

        System.out.println("--- ANALISANDO ESTOQUE DOS PRODUTOS ANTES DA COMPRA ---");
        System.out.println("Tem 2 Teclados disponiveis? " + p1.temEstoqueDisponivel(2));
        System.out.println("Tem 10 Monitores disponiveis? " + p2.temEstoqueDisponivel(10) + "\n");

        System.out.println("--- ADICIONANDO ITENS AO PEDIDO ---");
        ItemPedido item1 = new ItemPedido(p1, 2);
        ItemPedido item2 = new ItemPedido(p2, 1);
        boolean item1Adicionado = pedido.adicionarItem(item1);
        boolean item2Adicionado = pedido.adicionarItem(item2);

        System.out.println("Item 1 (2 teclados) adicionado? " + item1Adicionado);
        System.out.println("Item 2 (1 monitor) adicionado? " + item2Adicionado);
        System.out.println("Subtotal Item 1: R$ " + item1.calcularSubtotal());
        System.out.println("Subtotal Item 2: R$ " + item2.calcularSubtotal() + "\n");

        System.out.println("--- RESUMO DO PEDIDO CONSOLIDADO ---");
        System.out.println(pedido);
        System.out.println("Data de emissao: " + pedido.getData());
        System.out.println("Valor total do carrinho: R$ " + pedido.calcularValorTotal() + "\n");

        System.out.println("--- COMPROVACAO DE BAIXA DE ESTOQUE (PRODUTO SABE SE CUIDAR) ---");
        System.out.println("Estoque do Teclado pos-baixa: " + p1.getQuantidadeEmEstoque() + " unidades (inicio: 15)");
        System.out.println("Estoque do Monitor pos-baixa: " + p2.getQuantidadeEmEstoque() + " unidades (inicio: 5)\n");

        System.out.println("--- TENTATIVA DELIBERADA DE COMPRA ACIMA DO ESTOQUE ---");
        ItemPedido itemInvalido = new ItemPedido(p2, 999);
        boolean itemInvalidoAdicionado = pedido.adicionarItem(itemInvalido);

        System.out.println("Tentativa de comprar 999 monitores foi aceita? " + itemInvalidoAdicionado);
        System.out.println("Estoque do Monitor permanece em: " + p2.getQuantidadeEmEstoque() + " unidades\n");

        pedido.setSituacao(SituacaoPedido.PAGO);

        System.out.println("--- TRANSACAO AUTORIZADA (MUDANCA DE ESTADO) ---");
        System.out.println("Novo status operacional do pedido: " + pedido.getSituacao() + "\n");

        System.out.println("Total de pedidos emitidos no sistema: " + Pedido.getTotalDePedidosCriados());
    }
}