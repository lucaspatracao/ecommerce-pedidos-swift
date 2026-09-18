import { useMemo, useState } from 'react'
import { ClienteForm } from '../components/ClienteForm'

export function ClientesPage({ clientes = [], loading, error, onClienteSalvo, onClienteDelete }) {
  const [busca, setBusca] = useState('')
  const [clienteSelecionado, setClienteSelecionado] = useState(null)

  const clientesFiltrados = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    if (!termo) return clientes

    return clientes.filter((cliente) =>
      cliente.nome?.toLowerCase().includes(termo) ||
      cliente.email?.toLowerCase().includes(termo) ||
      cliente.cpf?.includes(termo),
    )
  }, [busca, clientes])

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Cadastro</p>
          <h1 className="page-title">Clientes</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="content-grid double-grid">
        <ClienteForm
          cliente={clienteSelecionado}
          onClienteSalvo={onClienteSalvo}
          onCancelarEdicao={() => setClienteSelecionado(null)}
        />

        <section className="panel">
          <div className="panel-header">
            <h2>Clientes cadastrados</h2>
          </div>

          <div className="toolbar">
            <input
              type="text"
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
              placeholder="Buscar por cliente, e-mail ou CPF"
            />
          </div>

          {loading ? (
            <div className="alert alert-info">Carregando clientes...</div>
          ) : clientesFiltrados.length === 0 ? (
            <div className="empty-panel">Nenhum cliente cadastrado ainda.</div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>E-mail</th>
                    <th>CPF</th>
                    <th>Telefone</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {clientesFiltrados.map((cliente) => (
                    <tr key={`${cliente.cpf}-${cliente.email}`}>
                      <td>{cliente.nome}</td>
                      <td>{cliente.email}</td>
                      <td>{cliente.cpf}</td>
                      <td>{cliente.telefone}</td>
                      <td>
                        <div className="inline-actions">
                          <button type="button" className="btn btn-ghost small-btn" onClick={() => setClienteSelecionado(cliente)}>
                            Editar
                          </button>
                          <button type="button" className="btn btn-danger small-btn" onClick={() => onClienteDelete?.(cliente.cpf)}>
                            Excluir
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
