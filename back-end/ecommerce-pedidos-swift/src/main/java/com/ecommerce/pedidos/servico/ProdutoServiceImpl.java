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
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProdutoServiceImpl implements ProdutoService {

    static final String CODIGO_DUPLICADO = "Já existe um produto com este código.";

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Override
    public List<ProdutoResponse> listar(Visibilidade visibilidade, String nome, Pageable pagina) {
        String filtroNome = nome == null || nome.isBlank() ? null : nome.trim();
        return produtoRepository.listar(visibilidade.filtroAtivo(), filtroNome, pagina).stream()
            .map(ProdutoResponse::de)
            .toList();
    }

    @Override
    public ProdutoResponse buscarPorCodigo(String codigo) {
        return ProdutoResponse.de(buscarEntidade(codigo, Visibilidade.ATIVOS));
    }

    @Override
    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest request) {
        Produto produto = new Produto(request.codigo(), request.nome(), request.descricao(),
            preco(request), estoqueObrigatorio(request));
        produto.definirDisponibilidade(disponivel(request));
        if (produtoRepository.existsByCodigo(produto.getCodigo())) {
            throw new ConflitoException(CODIGO_DUPLICADO);
        }
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(String codigo, ProdutoRequest request) {
        Produto produto = buscarEntidade(codigo, Visibilidade.TODOS);
        if (request.versao() != null && !Objects.equals(request.versao(), produto.getVersao())) {
            throw new ConflitoException("O produto foi alterado(a) por outra operação. "
                + "Recarregue os dados e tente novamente.");
        }

        String novoCodigo = Utils.textoObrigatorio(request.codigo(), "Código do produto");
        if (!novoCodigo.equals(produto.getCodigo()) && produtoRepository.existsByCodigo(novoCodigo)) {
            throw new ConflitoException(CODIGO_DUPLICADO);
        }
        produto.alterarCodigo(novoCodigo);
        produto.atualizarDados(request.nome(), request.descricao(), preco(request));
        if (request.estoque() != null) {
            produto.definirEstoque(request.estoque());
        }
        produto.definirDisponibilidade(disponivel(request));
        // flush: grava agora para a resposta já trazer a versão nova (@Version).
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    @Override
    @Transactional
    public ProdutoResponse atualizarEstoque(String codigo, int quantidade) {
        Produto produto = buscarEntidade(codigo, Visibilidade.ATIVOS);
        produto.definirEstoque(quantidade);
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    @Override
    @Transactional
    public void excluir(String codigo, boolean definitivo) {
        if (!definitivo) {
            inativar(codigo);
            return;
        }
        Produto produto = buscarEntidade(codigo, Visibilidade.TODOS);
        if (itemPedidoRepository.existsByProduto(produto)) {
            throw new ConflitoException("Este produto já foi vendido e não pode ser excluído definitivamente. "
                + "Inative-o para tirá-lo do catálogo.");
        }
        produtoRepository.delete(produto);
    }

    @Override
    @Transactional
    public void inativar(String codigo) {
        buscarEntidade(codigo, Visibilidade.ATIVOS).desativar();
    }

    @Override
    @Transactional
    public ProdutoResponse reativar(String codigo) {
        Produto produto = buscarEntidade(codigo, Visibilidade.INATIVOS);
        produto.ativar();
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    @Override
    @Transactional
    public ProdutoResponse alterarAtivo(String codigo, boolean ativo) {
        if (ativo) {
            return reativar(codigo);
        }
        inativar(codigo);
        return ProdutoResponse.de(buscarEntidade(codigo, Visibilidade.TODOS));
    }

    @Override
    public long contar() {
        return produtoRepository.countByAtivoTrue();
    }

    @Override
    public List<ProdutoResponse> listarEstoqueCritico(int limite) {
        return produtoRepository.buscarComEstoqueAbaixoDe(limite).stream()
            .map(ProdutoResponse::de)
            .toList();
    }

    /** Busca pelo código respeitando o filtro do soft delete. */
    private Produto buscarEntidade(String codigo, Visibilidade visibilidade) {
        String chave = Utils.textoObrigatorio(codigo, "Código do produto");
        Boolean ativo = visibilidade.filtroAtivo();
        return produtoRepository.findByCodigo(chave)
            .filter(produto -> ativo == null || produto.isAtivo() == ativo)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", chave));
    }

    // Na API o @Valid já barra nulos; estas checagens protegem quem chama o serviço direto.
    private static BigDecimal preco(ProdutoRequest request) {
        if (request.preco() == null) {
            throw new IllegalArgumentException("Preço do produto é obrigatório.");
        }
        return request.preco();
    }

    private static int estoqueObrigatorio(ProdutoRequest request) {
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
