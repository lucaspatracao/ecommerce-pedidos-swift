package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.dto.ProdutoResponse;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Override
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll().stream().map(ProdutoResponse::de).toList();
    }

    @Override
    public ProdutoResponse buscarPorCodigo(String codigo) {
        return ProdutoResponse.de(buscarEntidade(codigo));
    }

    @Override
    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest request) {
        Produto produto = new Produto(request.codigo(), request.nome(), request.descricao(),
            preco(request), estoque(request));
        produto.definirDisponibilidade(disponivel(request));
        if (produtoRepository.existsByCodigo(produto.getCodigo())) {
            throw new ConflitoException("Já existe produto com o código " + produto.getCodigo());
        }
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(String codigo, ProdutoRequest request) {
        Produto produto = buscarEntidade(codigo);

        String novoCodigo = Utils.textoObrigatorio(request.codigo(), "Código do produto");
        if (!novoCodigo.equals(produto.getCodigo()) && produtoRepository.existsByCodigo(novoCodigo)) {
            throw new ConflitoException("Já existe produto com o código " + novoCodigo);
        }
        produto.alterarCodigo(novoCodigo);
        produto.atualizarDados(request.nome(), request.descricao(), preco(request));
        produto.definirEstoque(estoque(request));
        produto.definirDisponibilidade(disponivel(request));
        return ProdutoResponse.de(produto);
    }

    @Override
    @Transactional
    public void excluir(String codigo) {
        Produto produto = buscarEntidade(codigo);
        if (itemPedidoRepository.existsByProduto(produto)) {
            throw new ConflitoException("Produto já foi vendido e não pode ser excluído. Desative-o para tirá-lo do catálogo.");
        }
        produtoRepository.delete(produto);
    }

    @Override
    public long contar() {
        return produtoRepository.count();
    }

    @Override
    public List<ProdutoResponse> listarEstoqueCritico(int limite) {
        return produtoRepository.buscarComEstoqueAbaixoDe(limite).stream()
            .map(ProdutoResponse::de)
            .toList();
    }

    private Produto buscarEntidade(String codigo) {
        String chave = Utils.textoObrigatorio(codigo, "Código do produto");
        return produtoRepository.findByCodigo(chave)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", chave));
    }

    // Na API o @Valid já barra nulos; estas checagens protegem quem chama o serviço direto.
    private static BigDecimal preco(ProdutoRequest request) {
        if (request.preco() == null) {
            throw new IllegalArgumentException("Preço do produto é obrigatório.");
        }
        return request.preco();
    }

    private static int estoque(ProdutoRequest request) {
        if (request.estoque() == null) {
            throw new IllegalArgumentException("Estoque do produto é obrigatório.");
        }
        return request.estoque();
    }

    // "disponivel" ausente no JSON = produto disponível.
    private static boolean disponivel(ProdutoRequest request) {
        return !Boolean.FALSE.equals(request.disponivel());
    }
}
