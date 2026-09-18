import { useMemo, useState } from 'react'
import { atualizarProduto, cadastrarProduto } from '../api/clienteApi'

const estadoInicial = {
  codigo: '',
  nome: '',
  descricao: '',
  preco: '',
  estoque: '',
  disponivel: true,
}

export function ProdutosPage({ produtos = [], loading, error, onProdutoSalvo, onProdutoDelete }) {
  const [form, setForm] = useState(estadoInicial)
  const [produtoSelecionado, setProdutoSelecionado] = useState(null)
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')
  const [filtro, setFiltro] = useState('')

  const produtosFiltrados = useMemo(() => {
    const termo = filtro.trim().toLowerCase()
    if (!termo) return produtos
    return produtos.filter((produto) =>
      produto.nome?.toLowerCase().includes(termo) ||
      produto.codigo?.toLowerCase().includes(termo) ||
      produto.categoria?.toLowerCase().includes(termo),
    )
  }, [filtro, produtos])

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm((atual) => ({
      ...atual,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const resetForm = () => {
    setForm(estadoInicial)
    setProdutoSelecionado(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErro('')
    setMensagem('')

    const payload = {
      codigo: form.codigo,
      nome: form.nome,
      descricao: form.descricao,
      preco: String(form.preco),
      estoque: Number(form.estoque),
      disponivel: Boolean(form.disponivel),
    }

    try {
      const produtoProcessado = produtoSelecionado
        ? await atualizarProduto(produtoSelecionado.codigo, payload)
        : await cadastrarProduto(payload)

      setMensagem(produtoSelecionado ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.')
      onProdutoSalvo?.(produtoProcessado, Boolean(produtoSelecionado))
      resetForm()
    } catch (apiError) {
      setErro(apiError.message || 'Não foi possível salvar o produto. Tente novamente.')
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <div>
          <p className="eyebrow">Catálogo</p>
          <h1 className="page-title">Produtos</h1>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="content-grid double-grid">
        <section className="panel">
          <div className="panel-header">
            <h2>{produtoSelecionado ? 'Editar produto' : 'Cadastro de produto'}</h2>
          </div>

          <form className="form-grid" onSubmit={handleSubmit}>
            <label>
              Código
              <input name="codigo" value={form.codigo} onChange={handleChange} placeholder="PROD-001" required />
            </label>

            <label>
              Nome
              <input name="nome" value={form.nome} onChange={handleChange} placeholder="Ex.: Headset Premium" required />
            </label>

            <label className="full-width">
              Descrição
              <textarea name="descricao" value={form.descricao} onChange={handleChange} rows="3" placeholder="Descreva as características principais do item." />
            </label>

            <label>
              Preço
              <input name="preco" type="number" step="0.01" value={form.preco} onChange={handleChange} placeholder="0.00" required />
            </label>

            <label>
              Estoque
              <input name="estoque" type="number" value={form.estoque} onChange={handleChange} placeholder="0" required />
            </label>

            <label className="checkbox-row full-width">
              <input type="checkbox" name="disponivel" checked={form.disponivel} onChange={handleChange} />
              Produto disponível para venda
            </label>

            {mensagem && <div className="alert alert-success full-width">{mensagem}</div>}
            {erro && <div className="alert alert-error full-width">{erro}</div>}

            <div className="full-width actions-row">
              <button type="submit" className="btn btn-primary">
                {produtoSelecionado ? 'Salvar alterações' : 'Salvar produto'}
              </button>
              {produtoSelecionado && (
                <button type="button" className="btn btn-ghost" onClick={resetForm}>Cancelar</button>
              )}
            </div>
          </form>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h2>Gerenciamento</h2>
          </div>

          <div className="toolbar">
            <input
              type="text"
              value={filtro}
              onChange={(event) => setFiltro(event.target.value)}
              placeholder="Buscar por nome ou código"
            />
          </div>

          {loading ? (
            <div className="alert alert-info">Carregando produtos...</div>
          ) : produtosFiltrados.length === 0 ? (
            <div className="empty-panel">Nenhum produto cadastrado ainda.</div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Código</th>
                    <th>Nome</th>
                    <th>Preço</th>
                    <th>Estoque</th>
                    <th>Status</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {produtosFiltrados.map((produto) => (
                    <tr key={produto.codigo}>
                      <td>{produto.codigo}</td>
                      <td>{produto.nome}</td>
                      <td>R$ {Number(produto.preco || 0).toFixed(2)}</td>
                      <td>{produto.quantidadeEmEstoque ?? produto.estoque ?? 0}</td>
                      <td>
                        <span className={produto.ativo || produto.disponivel ? 'status status-success' : 'status status-danger'}>
                          {produto.ativo || produto.disponivel ? 'Disponível' : 'Indisponível'}
                        </span>
                      </td>
                      <td>
                        <div className="inline-actions">
                          <button type="button" className="btn btn-ghost small-btn" onClick={() => {
                            setProdutoSelecionado(produto)
                            setForm({
                              codigo: produto.codigo,
                              nome: produto.nome,
                              descricao: produto.descricao || '',
                              preco: Number(produto.preco || 0),
                              estoque: produto.quantidadeEmEstoque ?? produto.estoque ?? 0,
                              disponivel: produto.ativo || produto.disponivel,
                            })
                          }}>
                            Editar
                          </button>
                          <button type="button" className="btn btn-danger small-btn" onClick={() => onProdutoDelete?.(produto.codigo)}>
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
