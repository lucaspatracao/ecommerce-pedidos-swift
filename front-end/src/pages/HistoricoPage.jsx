export function HistoricoPage({ historico = [], loading, error }) {
  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Operações</p>
          <h1 className="page-title">Histórico de compras</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="alert alert-info">Carregando histórico...</div>
      ) : historico.length === 0 ? (
        <div className="empty-panel">Nenhuma movimentação registrada no histórico.</div>
      ) : (
        <section className="panel">
          <div className="panel-header">
            <h2>Atividades recentes</h2>
          </div>

          <div className="timeline">
            {historico.map((item, index) => (
              <div className="timeline-item" key={`${item.titulo}-${index}`}>
                <span className="timeline-dot" />
                <div>
                  <strong>{item.titulo}</strong>
                  <p>{item.descricao}</p>
                  <small>{item.data}</small>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
