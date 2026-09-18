import { Link } from 'react-router-dom'

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

export function PedidosPage({ pedidos = [], loading, error }) {
  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Operação</p>
          <h1 className="page-title">Pedidos</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="panel">
        <div className="panel-header">
          <h2>Lista de pedidos</h2>
        </div>

        {loading ? (
          <div className="alert alert-info">Carregando pedidos...</div>
        ) : pedidos.length === 0 ? (
          <div className="empty-panel">Nenhum pedido registrado ainda.</div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Número</th>
                  <th>Cliente</th>
                  <th>Data</th>
                  <th>Itens</th>
                  <th>Valor</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {pedidos.map((pedido) => (
                  <tr key={pedido.numero}>
                    <td>{pedido.numero}</td>
                    <td>{pedido.cliente?.nome || 'Cliente não informado'}</td>
                    <td>{pedido.data}</td>
                    <td>{pedido.itens?.length || 0}</td>
                    <td>{formatCurrency(pedido.calcularValorTotal ? pedido.calcularValorTotal() : 0)}</td>
                    <td>
                      <span className={statusClassMap[pedido.situacao] || 'status'}>{pedido.situacao || 'Sem status'}</span>
                    </td>
                    <td>
                      <Link to={`/pedido/${pedido.numero}`} className="table-link">Detalhes</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
