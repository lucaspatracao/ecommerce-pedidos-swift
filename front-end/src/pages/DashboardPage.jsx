const formatCurrency = (valor) => {
  const numero = Number(valor || 0)
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(numero)
}

const statusClassMap = {
  PAGO: 'status status-success',
  ABERTO: 'status status-pending',
  CANCELADO: 'status status-danger',
  APROVADO: 'status status-success',
  PENDENTE: 'status status-pending',
  RECUSADO: 'status status-danger',
}

export function DashboardPage({ dashboard = {}, pedidos = [], produtos = [], loading, error }) {
  const totalClientes = Number(dashboard.totalClientes || 0)
  const totalPedidos = Number(dashboard.totalPedidos || pedidos.length || 0)
  const totalProdutos = Number(dashboard.totalProdutos || produtos.length || 0)
  const receitaTotal = Number(dashboard.receitaTotal || 0)
  const estoqueCritico = Array.isArray(dashboard.estoqueCritico) ? dashboard.estoqueCritico : []
  const pedidosRecentes = Array.isArray(dashboard.pedidosRecentes) && dashboard.pedidosRecentes.length > 0
    ? dashboard.pedidosRecentes
    : pedidos.slice(0, 5)

  const cards = [
    {
      titulo: 'Receita total',
      valor: formatCurrency(receitaTotal),
      detalhe: totalPedidos > 0 ? `${totalPedidos} pedidos registrados` : 'Nenhum pedido registrado',
    },
    {
      titulo: 'Quantidade de pedidos',
      valor: totalPedidos,
      detalhe: totalPedidos > 0 ? 'Pedidos ativos no sistema' : 'Sem pedidos no momento',
    },
    {
      titulo: 'Clientes ativos',
      valor: totalClientes,
      detalhe: totalClientes > 0 ? 'Clientes cadastrados' : 'Nenhum cliente cadastrado',
    },
    {
      titulo: 'Produtos em catálogo',
      valor: totalProdutos,
      detalhe: totalProdutos > 0 ? 'Produtos disponíveis' : 'Nenhum produto cadastrado',
    },
  ]

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Visão geral</p>
          <h1 className="page-title">Dashboard</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="alert alert-info">Carregando dados do dashboard...</div>
      ) : (
        <>
          <div className="metrics-grid">
            {cards.map((metric) => (
              <div className="metric-card" key={metric.titulo}>
                <span className="metric-label">{metric.titulo}</span>
                <strong className="metric-value">{metric.valor}</strong>
                <small>{metric.detalhe}</small>
              </div>
            ))}
          </div>

          <div className="content-grid dashboard-grid">
            <section className="panel panel-large">
              <div className="panel-header">
                <h2>Pedidos recentes</h2>
              </div>

              {pedidosRecentes.length === 0 ? (
                <div className="empty-panel">Nenhum pedido registrado ainda.</div>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Pedido</th>
                        <th>Cliente</th>
                        <th>Valor</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pedidosRecentes.map((pedido) => (
                        <tr key={pedido.numero || pedido.id}>
                          <td>{pedido.numero || pedido.id}</td>
                          <td>{pedido.cliente?.nome || pedido.cliente || 'Cliente não informado'}</td>
                          <td>{formatCurrency(pedido.calcularValorTotal ? pedido.calcularValorTotal() : pedido.valor || 0)}</td>
                          <td>
                            <span className={statusClassMap[pedido.situacao || pedido.status] || 'status'}>
                              {pedido.situacao || pedido.status || 'Sem status'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            <section className="panel">
              <div className="panel-header">
                <h2>Estoque crítico</h2>
              </div>

              {estoqueCritico.length === 0 ? (
                <div className="empty-panel">Nenhum produto com estoque crítico.</div>
              ) : (
                <ul className="stack-list">
                  {estoqueCritico.map((produto) => (
                    <li key={produto.codigo}>
                      <div>
                        <strong>{produto.nome}</strong>
                        <span>{produto.quantidadeEmEstoque} unidades restantes</span>
                      </div>
                      <span className="badge badge-soft">Baixo</span>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        </>
      )}
    </div>
  )
}
