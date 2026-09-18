import { useParams } from 'react-router-dom'

const statusClassMap = {
  PAGO: 'status status-success',
  ABERTO: 'status status-pending',
  CANCELADO: 'status status-danger',
}

const formatCurrency = (valor) =>
  new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(valor || 0))

export function DetalhePedidoPage({ pedidos = [] }) {
  const { id } = useParams()
  const pedido = pedidos.find((item) => item.numero === id)

  if (!pedido) {
    return <div className="empty-panel">Pedido não encontrado.</div>
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Pedido</p>
          <h1 className="page-title">{pedido.numero}</h1>
        </div>
        <span className={statusClassMap[pedido.situacao] || 'status'}>{pedido.situacao || 'Sem status'}</span>
      </div>

      <div className="content-grid double-grid">
        <section className="panel">
          <div className="panel-header">
            <h2>Resumo</h2>
          </div>

          <div className="detail-list">
            <div><span>Cliente</span><strong>{pedido.cliente?.nome || 'Cliente não informado'}</strong></div>
            <div><span>Data</span><strong>{pedido.data}</strong></div>
            <div><span>Quantidade de itens</span><strong>{pedido.itens?.length || 0}</strong></div>
            <div><span>Valor total</span><strong>{formatCurrency(pedido.calcularValorTotal ? pedido.calcularValorTotal() : 0)}</strong></div>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h2>Itens</h2>
          </div>

          {pedido.itens?.length === 0 ? (
            <div className="empty-panel">Nenhum item cadastrado para este pedido.</div>
          ) : (
            <ul className="stack-list compact-list">
              {pedido.itens.map((item) => (
                <li key={`${pedido.numero}-${item.produto.codigo}`}>
                  <div>
                    <strong>{item.produto.nome}</strong>
                    <span>{item.quantidade} unidade(s)</span>
                  </div>
                  <strong>{formatCurrency(item.calcularSubtotal ? item.calcularSubtotal() : item.precoPraticado || 0)}</strong>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}
