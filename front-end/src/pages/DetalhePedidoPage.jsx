import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { buscarPedido } from '../api/clienteApi'
import { PagamentoForm } from '../components/PagamentoForm'

const statusClassMap = {
  PAGO: 'status status-success',
  ABERTO: 'status status-pending',
  CANCELADO: 'status status-danger',
  APROVADO: 'status status-success',
  ESTORNADO: 'status status-neutral',
}

// "descricao" vem da API com o nome da classe (ex.: CartaoCredito); aqui vira texto para o usuário.
const nomeDaForma = {
  Pix: 'Pix',
  Boleto: 'Boleto',
  CartaoCredito: 'Cartão de crédito',
  ValeCompras: 'Vale-compras',
}

const formatCurrency = (valor) =>
  new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(valor || 0))

/**
 * Detalhe do pedido (/pedido/:id). Busca o pedido direto na API
 * (GET /api/pedidos/{numero}) em vez de procurar na lista em memória: assim
 * funciona logo após criar o pedido, ao abrir o link direto e após pagar.
 */
export function DetalhePedidoPage({ onPedidoAtualizado }) {
  const { id } = useParams()
  const [pedido, setPedido] = useState(null)
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [mensagem, setMensagem] = useState('')

  useEffect(() => {
    let ativo = true
    buscarPedido(id)
      .then((dados) => ativo && setPedido(dados))
      .catch((error) => ativo && setErro(error.message))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [id])

  const handlePagamentoRegistrado = async () => {
    setPedido(await buscarPedido(id))
    setMensagem('Pagamento registrado com sucesso.')
    onPedidoAtualizado?.()
  }

  if (carregando) {
    return <div className="alert alert-info">Carregando pedido...</div>
  }
  if (!pedido) {
    return <div className="empty-panel">{erro || 'Pedido não encontrado.'}</div>
  }

  const pagamento = pedido.formaPagamento

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Pedido</p>
          <h1 className="page-title">{pedido.numero}</h1>
        </div>
        <div className="actions-row">
          <span className={statusClassMap[pedido.situacao] || 'status'}>{pedido.situacao || 'Sem status'}</span>
          <Link to="/pedidos" className="btn btn-ghost">Voltar aos pedidos</Link>
        </div>
      </div>

      {mensagem && <div className="alert alert-success" role="status">{mensagem}</div>}

      <div className="content-grid double-grid">
        <section className="panel">
          <div className="panel-header">
            <h2>Resumo</h2>
          </div>

          <div className="detail-list">
            <div><span>Cliente</span><strong>{pedido.cliente?.nome || 'Cliente não informado'}</strong></div>
            <div><span>Data</span><strong>{pedido.data}</strong></div>
            <div><span>Quantidade de itens</span><strong>{pedido.itens?.length || 0}</strong></div>
            <div><span>Valor total</span><strong data-testid="valor-total">{formatCurrency(pedido.valorTotal ?? 0)}</strong></div>
            {pagamento && (
              <>
                <div><span>Forma de pagamento</span><strong>{nomeDaForma[pagamento.descricao] || pagamento.descricao}</strong></div>
                <div>
                  <span>Situação do pagamento</span>
                  <span className={statusClassMap[pagamento.situacao] || 'status'}>{pagamento.situacao}</span>
                </div>
              </>
            )}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h2>Itens</h2>
          </div>

          {!pedido.itens?.length ? (
            <div className="empty-panel">Nenhum item cadastrado para este pedido.</div>
          ) : (
            <ul className="stack-list compact-list">
              {pedido.itens.map((item) => (
                <li key={`${pedido.numero}-${item.produto.codigo}`}>
                  <div>
                    <strong>{item.produto.nome}</strong>
                    <span>{item.quantidade} × {formatCurrency(item.precoPraticado)}</span>
                  </div>
                  <strong>{formatCurrency(item.subtotal ?? 0)}</strong>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      {pedido.situacao === 'ABERTO' && (
        <PagamentoForm pedido={pedido} onPagamentoRegistrado={handlePagamentoRegistrado} />
      )}
    </div>
  )
}
