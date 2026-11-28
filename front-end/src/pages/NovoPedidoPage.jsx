import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { criarPedido } from '../api/clienteApi'
import { ErroCampo } from '../components/ErroCampo'

const formatCurrency = (valor) =>
  new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(valor || 0))

/**
 * Tela "Novo pedido" (/pedidos/novo).
 *
 * Monta o corpo do POST /api/pedidos: { clienteCpf, itens: [{ codigo, quantidade }] }.
 * Preço, subtotal e total exibidos aqui são só uma prévia: quem calcula e grava
 * o valor oficial é o back-end (que também revalida estoque e produto ativo).
 */
export function NovoPedidoPage({ clientes = [], produtos = [], loading, onPedidoCriado, onDadosDesatualizados }) {
  const [clienteCpf, setClienteCpf] = useState('')
  const [codigoSelecionado, setCodigoSelecionado] = useState('')
  const [quantidade, setQuantidade] = useState('1')
  const [itens, setItens] = useState([])
  const [erroItem, setErroItem] = useState('')
  const [erro, setErro] = useState('')
  // Mensagens por campo vindas do back-end (400 com "campos").
  const [errosCampos, setErrosCampos] = useState({})
  const [enviando, setEnviando] = useState(false)

  // Só produtos que podem ser vendidos: ativos e com estoque.
  const produtosDisponiveis = useMemo(
    () => produtos.filter((produto) => produto.ativo && produto.quantidadeEmEstoque > 0),
    [produtos],
  )

  const produtoPorCodigo = (codigo) => produtos.find((produto) => produto.codigo === codigo)
  const quantidadeNoPedido = (codigo) => itens.find((item) => item.codigo === codigo)?.quantidade || 0

  const itensDetalhados = itens.map((item) => {
    const produto = produtoPorCodigo(item.codigo)
    const preco = Number(produto?.preco || 0)
    return { ...item, nome: produto?.nome || item.codigo, preco, subtotal: preco * item.quantidade }
  })
  const total = itensDetalhados.reduce((soma, item) => soma + item.subtotal, 0)

  const handleAdicionarItem = () => {
    setErroItem('')
    setErro('')
    const produto = produtoPorCodigo(codigoSelecionado)
    const qtd = Number(quantidade)

    if (!produto) {
      setErroItem('Selecione um produto.')
      return
    }
    if (!Number.isInteger(qtd) || qtd <= 0) {
      setErroItem('A quantidade deve ser um número inteiro maior que zero.')
      return
    }
    const totalDesejado = quantidadeNoPedido(produto.codigo) + qtd
    if (totalDesejado > produto.quantidadeEmEstoque) {
      setErroItem(`Estoque insuficiente: ${produto.nome} tem ${produto.quantidadeEmEstoque} unidade(s).`)
      return
    }

    // Produto repetido soma a quantidade na mesma linha (o back-end faz o mesmo).
    setItens((atual) =>
      atual.some((item) => item.codigo === produto.codigo)
        ? atual.map((item) => (item.codigo === produto.codigo ? { ...item, quantidade: totalDesejado } : item))
        : [...atual, { codigo: produto.codigo, quantidade: qtd }],
    )
    setCodigoSelecionado('')
    setQuantidade('1')
  }

  const handleRemoverItem = (codigo) => {
    setItens((atual) => atual.filter((item) => item.codigo !== codigo))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErro('')

    if (!clienteCpf) {
      setErro('Selecione o cliente do pedido.')
      return
    }
    if (itens.length === 0) {
      setErro('Adicione pelo menos um item ao pedido.')
      return
    }

    setEnviando(true)
    try {
      setErrosCampos({})
      const pedido = await criarPedido({ clienteCpf, itens })
      onPedidoCriado?.(pedido)
    } catch (error) {
      // "mensagem" do corpo de erro padronizado, ex.: estoque insuficiente (409);
      // em 400 de validação, "campos" aparece junto do campo certo.
      setErro(error.message)
      setErrosCampos(error.campos ?? {})
      // O estoque pode ter mudado desde que a tela abriu: recarrega os produtos.
      onDadosDesatualizados?.()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Operação</p>
          <h1 className="page-title">Novo pedido</h1>
        </div>
        <Link to="/pedidos" className="btn btn-ghost">Voltar aos pedidos</Link>
      </div>

      {loading ? (
        <div className="alert alert-info">Carregando clientes e produtos...</div>
      ) : (
        <form className="content-grid double-grid" onSubmit={handleSubmit} noValidate>
          <section className="panel">
            <div className="panel-header">
              <h2>Cliente e itens</h2>
            </div>

            <div className="form-grid">
              <label className="full-width">
                Cliente
                <select name="cliente" value={clienteCpf} onChange={(event) => setClienteCpf(event.target.value)}>
                  <option value="">Selecione o cliente</option>
                  {clientes.map((cliente) => (
                    <option key={cliente.cpf} value={cliente.cpf}>
                      {cliente.nome} ({cliente.cpf})
                    </option>
                  ))}
                </select>
                <ErroCampo mensagem={errosCampos.clienteCpf} />
                {clientes.length === 0 && (
                  <span className="field-hint">Nenhum cliente cadastrado. Cadastre um em Clientes.</span>
                )}
              </label>

              <label>
                Produto
                <select name="produto" value={codigoSelecionado} onChange={(event) => setCodigoSelecionado(event.target.value)}>
                  <option value="">Selecione o produto</option>
                  {produtosDisponiveis.map((produto) => (
                    <option key={produto.codigo} value={produto.codigo}>
                      {produto.nome} — {formatCurrency(produto.preco)} ({produto.quantidadeEmEstoque} em estoque)
                    </option>
                  ))}
                </select>
                {produtosDisponiveis.length === 0 && (
                  <span className="field-hint">Nenhum produto ativo com estoque.</span>
                )}
              </label>

              <label>
                Quantidade
                <input
                  name="quantidade"
                  type="number"
                  min="1"
                  step="1"
                  value={quantidade}
                  onChange={(event) => setQuantidade(event.target.value)}
                  aria-invalid={Boolean(erroItem)}
                />
              </label>

              {erroItem && <span className="field-error full-width" role="alert">{erroItem}</span>}

              <div className="full-width actions-row">
                <button type="button" className="btn btn-secondary" onClick={handleAdicionarItem}>
                  Adicionar item
                </button>
              </div>
            </div>
          </section>

          <section className="panel">
            <div className="panel-header">
              <h2>Resumo do pedido</h2>
            </div>

            {itensDetalhados.length === 0 ? (
              <div className="empty-panel">Nenhum item adicionado ainda.</div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Produto</th>
                      <th className="num">Preço</th>
                      <th className="num">Qtd.</th>
                      <th className="num">Subtotal</th>
                      <th aria-label="Ações" />
                    </tr>
                  </thead>
                  <tbody>
                    {itensDetalhados.map((item) => (
                      <tr key={item.codigo}>
                        <td>{item.nome}</td>
                        <td className="num">{formatCurrency(item.preco)}</td>
                        <td className="num">{item.quantidade}</td>
                        <td className="num">{formatCurrency(item.subtotal)}</td>
                        <td>
                          <div className="inline-actions">
                            <button type="button" className="btn btn-danger small-btn" onClick={() => handleRemoverItem(item.codigo)}>
                              Remover
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="detail-list">
              <div>
                <span>Total do pedido</span>
                <strong data-testid="total-pedido">{formatCurrency(total)}</strong>
              </div>
            </div>

            <ErroCampo mensagem={errosCampos.itens} />
            {erro && <div className="alert alert-error" role="alert">{erro}</div>}

            <div className="actions-row">
              <button type="submit" className="btn btn-primary" disabled={enviando}>
                {enviando ? 'Salvando...' : 'Salvar pedido'}
              </button>
            </div>
          </section>
        </form>
      )}
    </div>
  )
}
