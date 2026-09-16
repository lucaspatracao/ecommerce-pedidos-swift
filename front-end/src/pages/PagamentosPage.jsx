const statusClassMap = {
  APROVADO: 'status status-success',
  PENDENTE: 'status status-pending',
  RECUSADO: 'status status-danger',
  ESTORNADO: 'status status-danger',
}

const formatCurrency = (valor) =>
  new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(valor || 0))

export function PagamentosPage({ pagamentos = [], loading, error }) {
  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Financeiro</p>
          <h1 className="page-title">Pagamentos</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="alert alert-info">Carregando pagamentos...</div>
      ) : pagamentos.length === 0 ? (
        <div className="empty-panel">Nenhum pagamento registrado ainda.</div>
      ) : (
        <div className="payments-grid">
          {pagamentos.map((pagamento, index) => (
            <article className="payment-card" key={`${pagamento.tipo}-${index}`}>
              <div className="payment-header">
                <div>
                  <p className="label">Método</p>
                  <h3>{pagamento.tipo}</h3>
                </div>
                <span className={statusClassMap[pagamento.status] || 'status'}>{pagamento.status}</span>
              </div>

              <div className="payment-body">
                <p>{pagamento.metodo}</p>
                <strong>{formatCurrency(pagamento.valor)}</strong>
              </div>

              <div className="payment-footer">
                <span>{pagamento.ultimaTransacao}</span>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
