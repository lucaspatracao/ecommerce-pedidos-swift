import { useEffect, useState } from 'react'
import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom'
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
  
  // Estado para gerenciar a minimização
  const [isCollapsed, setIsCollapsed] = useState(false)

  const carregarDados = async () => {
    setLoading(true)
    setError('')

    try {
      const [clientesCarregados, produtosCarregados, pedidosCarregados, pagamentosCarregados, historicoCarregado, dashboardCarregado] = await Promise.all([
        listarClientes(),
        listarProdutos(),
        listarPedidos(),
        listarPagamentos(),
        listarHistorico(),
        buscarDashboard(),
      ])

      setClientes(clientesCarregados)
      setProdutos(produtosCarregados)
      setPedidos(pedidosCarregados)
      setPagamentos(pagamentosCarregados)
      setHistorico(historicoCarregado)
      setDashboard(dashboardCarregado || estadoInicialDashboard)
    } catch (err) {
      setError('Não foi possível carregar os dados do sistema. Tente novamente.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregarDados()
  }, [])

  const handleClienteSalvo = (cliente, edicao = false) => {
    setClientes((atual) => {
      if (edicao) {
        return atual.map((item) => (item.cpf === cliente.cpf ? cliente : item))
      }
      return [cliente, ...atual]
    })
  }

  const handleClienteExcluido = async (cpf) => {
    try {
      await excluirCliente(cpf)
      setClientes((atual) => atual.filter((cliente) => cliente.cpf !== cpf))
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
  }

  const handleProdutoExcluido = async (codigo) => {
    try {
      await excluirProduto(codigo)
      setProdutos((atual) => atual.filter((produto) => produto.codigo !== codigo))
    } catch (error) {
      setError(error.message || 'Não foi possível excluir o produto. Tente novamente.')
    }
  }

  return (
    // Adicionada a classe condicional no shell para ajustar o layout principal do Grid
    <div className={`app-shell ${isCollapsed ? 'sidebar-collapsed' : ''}`}>
      
      {/* Correção crucial: Injetada a classe '.is-collapsed' exata esperada pelo seu CSS */}
      <aside className={`sidebar ${isCollapsed ? 'is-collapsed' : ''}`}>
        
        {/* Clique na brand-box inverte o estado. Adicionado o ponteiro via estilo inline */}
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
              title={item.label} // Adicionado conforme o comentário do seu CSS para gerar Tooltips nativos
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Mantido o HTML sem alterações de remoção do React para que a animação flua pelo CSS */}
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
            <button type="button" className="btn btn-primary">Novo pedido</button>
          </div>
        </header>

        <main className="main-panel">
          {error && <div className="alert alert-error">{error}</div>}

          <Routes>
            <Route path="/" element={<DashboardPage dashboard={dashboard} pedidos={pedidos} produtos={produtos} loading={loading} error={error} />} />
            <Route path="/clientes" element={<ClientesPage clientes={clientes} loading={loading} error={error} onClienteSalvo={handleClienteSalvo} onClienteDelete={handleClienteExcluido} />} />
            <Route path="/produtos" element={<ProdutosPage produtos={produtos} loading={loading} error={error} onProdutoSalvo={handleProdutoSalvo} onProdutoDelete={handleProdutoExcluido} />} />
            <Route path="/pedidos" element={<PedidosPage pedidos={pedidos} loading={loading} error={error} />} />
            <Route path="/pagamentos" element={<PagamentosPage pagamentos={pagamentos} loading={loading} error={error} />} />
            <Route path="/pedido/:id" element={<DetalhePedidoPage pedidos={pedidos} />} />
            <Route path="/historico" element={<HistoricoPage historico={historico} loading={loading} error={error} />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}
