import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import logo from './assets/logo.png'
import './App.css'
import {
  buscarDashboard,
  excluirCliente,
  excluirProduto,
  listarClientes,
  listarHistorico,
  listarPagamentos,
  listarPedidos,
  listarProdutos,
} from './api/clienteApi'
import { DashboardPage } from './pages/DashboardPage'
import { ClientesPage } from './pages/ClientesPage'
import { ProdutosPage } from './pages/ProdutosPage'
import { PedidosPage } from './pages/PedidosPage'
import { PagamentosPage } from './pages/PagamentosPage'
import { DetalhePedidoPage } from './pages/DetalhePedidoPage'
import { NovoPedidoPage } from './pages/NovoPedidoPage'
import { HistoricoPage } from './pages/HistoricoPage'

const navItems = [
  { label: 'Dashboard', to: '/', exact: true },
  { label: 'Clientes', to: '/clientes' },
  { label: 'Produtos', to: '/produtos' },
  { label: 'Pedidos', to: '/pedidos' },
  { label: 'Pagamentos', to: '/pagamentos' },
  { label: 'Histórico de compras', to: '/historico' },
]

const estadoInicialDashboard = {
  totalClientes: 0,
  totalPedidos: 0,
  totalProdutos: 0,
  receitaTotal: 0,
  estoqueCritico: [],
  pedidosRecentes: [],
}

// Cada fonte de dados: [nome exibido em caso de erro, chamada à API].
const FONTES = {
  clientes: ['clientes', listarClientes],
  produtos: ['produtos', listarProdutos],
  pedidos: ['pedidos', listarPedidos],
  pagamentos: ['pagamentos', listarPagamentos],
  historico: ['histórico', listarHistorico],
  dashboard: ['resumo', buscarDashboard],
}

/**
 * Busca as fontes em paralelo com Promise.allSettled.
 * Diferente de Promise.all, uma chamada que falha NÃO descarta as outras:
 * devolve os dados de quem respondeu e os nomes de quem falhou.
 */
async function buscarFontes(chaves) {
  const resultados = await Promise.allSettled(chaves.map((chave) => FONTES[chave][1]()))
  const dados = {}
  const falhas = []
  resultados.forEach((resultado, indice) => {
    const chave = chaves[indice]
    if (resultado.status === 'fulfilled') {
      dados[chave] = resultado.value
    } else {
      falhas.push(FONTES[chave][0])
    }
  })
  return { dados, falhas }
}

const mensagemDeFalha = (falhas) =>
  falhas.length === 0 ? '' : `Não foi possível carregar: ${falhas.join(', ')}. Os demais dados foram atualizados.`

export default function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  )
}

function AppShell() {
  const [clientes, setClientes] = useState([])
  const [produtos, setProdutos] = useState([])
  const [pedidos, setPedidos] = useState([])
  const [pagamentos, setPagamentos] = useState([])
  const [historico, setHistorico] = useState([])
  const [dashboard, setDashboard] = useState(estadoInicialDashboard)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [isCollapsed, setIsCollapsed] = useState(false)
  const navigate = useNavigate()

  /**
   * Busca as fontes em paralelo e guarda cada resultado no seu estado.
   * Só usa setters do useState (estáveis), então a função é a mesma em
   * todas as renderizações e pode ser dependência do useEffect.
   * Devolve os nomes das fontes que falharam.
   */
  const carregarFontes = useCallback(async (chaves) => {
    const onde = {
      clientes: setClientes,
      produtos: setProdutos,
      pedidos: setPedidos,
      pagamentos: setPagamentos,
      historico: setHistorico,
      dashboard: (dados) => setDashboard(dados || estadoInicialDashboard),
    }
    const { dados, falhas } = await buscarFontes(chaves)
    Object.entries(dados).forEach(([chave, valor]) => onde[chave](valor))
    return falhas
  }, [])

  const carregarDados = async () => {
    setLoading(true)
    setError('')
    const falhas = await carregarFontes(Object.keys(FONTES))
    setError(mensagemDeFalha(falhas))
    setLoading(false)
  }

  /**
   * Recarrega o que é derivado dos cadastros (resumo lateral, dashboard,
   * pedidos, pagamentos e histórico) depois de qualquer alteração.
   * Antes, o resumo só era buscado ao abrir a página e ficava desatualizado.
   */
  const atualizarResumo = async () => {
    const falhas = await carregarFontes(['dashboard', 'pedidos', 'pagamentos', 'historico', 'produtos'])
    if (falhas.length > 0) {
      setError(mensagemDeFalha(falhas))
    }
  }

  // Carga inicial: "loading" já começa true, então o estado só muda quando as respostas chegam.
  useEffect(() => {
    carregarFontes(Object.keys(FONTES)).then((falhas) => {
      setError(mensagemDeFalha(falhas))
      setLoading(false)
    })
  }, [carregarFontes])

  const handleClienteSalvo = (cliente, edicao = false) => {
    setClientes((atual) => {
      if (edicao) {
        return atual.map((item) => (item.cpf === cliente.cpf ? cliente : item))
      }
      return [cliente, ...atual]
    })
    atualizarResumo()
  }

  const handleClienteExcluido = async (cpf) => {
    try {
      await excluirCliente(cpf)
      setClientes((atual) => atual.filter((cliente) => cliente.cpf !== cpf))
      atualizarResumo()
    } catch (error) {
      setError(error.message || 'Não foi possível excluir o cliente. Tente novamente.')
    }
  }

  const handleProdutoSalvo = (produto, edicao = false) => {
    setProdutos((atual) => {
      if (edicao) {
        return atual.map((item) => (item.codigo === produto.codigo ? produto : item))
      }
      return [produto, ...atual]
    })
    atualizarResumo()
  }

  const handleProdutoExcluido = async (codigo) => {
    try {
      await excluirProduto(codigo)
      setProdutos((atual) => atual.filter((produto) => produto.codigo !== codigo))
      atualizarResumo()
    } catch (error) {
      setError(error.message || 'Não foi possível excluir o produto. Tente novamente.')
    }
  }

  const handleNovoPedido = () => {
    navigate('/pedidos/novo')
  }

  // Pedido criado: abre o detalhe (onde ele pode ser pago) e atualiza resumo, estoque e listas.
  const handlePedidoCriado = (pedido) => {
    navigate(`/pedido/${encodeURIComponent(pedido.numero)}`)
    atualizarResumo()
  }

  return (
    <div className={`app-shell ${isCollapsed ? 'sidebar-collapsed' : ''}`}>
      <aside className={`sidebar ${isCollapsed ? 'is-collapsed' : ''}`}>
        <div className="brand-box" onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer' }}>
          <div className="brand-mark">
            <img src={logo} alt="Logotipo da Marca" />
          </div>
          <div>
            <span className="brand-name">Swift</span>
            <small>E-commerce</small>
          </div>
        </div>

        <nav className="nav-menu" aria-label="Navegação principal">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.exact}
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
              title={item.label}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-summary">
          <span className="label">Resumo rápido</span>
          <div>
            <strong>{dashboard.totalClientes}</strong>
            <small>clientes</small>
          </div>
          <div>
            <strong>{dashboard.totalPedidos}</strong>
            <small>pedidos</small>
          </div>
          <div>
            <strong>{dashboard.totalProdutos}</strong>
            <small>produtos</small>
          </div>
        </div>
      </aside>

      <div className="content-area">
        <header className="topbar">
          <div>
            <p className="eyebrow">Sistema administrativo</p>
            <h2>Gestão de pedidos</h2>
          </div>

          <div className="topbar-actions">
            <button type="button" className="btn btn-ghost" onClick={carregarDados}>Atualizar</button>
            <button type="button" className="btn btn-primary" onClick={handleNovoPedido}>Novo pedido</button>
          </div>
        </header>

        <main className="main-panel">
          {error && <div className="alert alert-error">{error}</div>}

          <Routes>
            <Route path="/" element={<DashboardPage dashboard={dashboard} pedidos={pedidos} produtos={produtos} loading={loading} error={error} />} />
            <Route path="/clientes" element={<ClientesPage clientes={clientes} loading={loading} error={error} onClienteSalvo={handleClienteSalvo} onClienteDelete={handleClienteExcluido} />} />
            <Route path="/produtos" element={<ProdutosPage produtos={produtos} loading={loading} error={error} onProdutoSalvo={handleProdutoSalvo} onProdutoDelete={handleProdutoExcluido} />} />
            <Route path="/pedidos" element={<PedidosPage pedidos={pedidos} loading={loading} error={error} />} />
            <Route path="/pedidos/novo" element={<NovoPedidoPage clientes={clientes} produtos={produtos} loading={loading} onPedidoCriado={handlePedidoCriado} onDadosDesatualizados={atualizarResumo} />} />
            <Route path="/pagamentos" element={<PagamentosPage pagamentos={pagamentos} loading={loading} error={error} />} />
            <Route path="/pedido/:id" element={<DetalhePedidoPage onPedidoAtualizado={atualizarResumo} />} />
            <Route path="/historico" element={<HistoricoPage historico={historico} loading={loading} error={error} />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}
