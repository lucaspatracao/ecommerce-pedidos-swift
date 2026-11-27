import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

const tratarErro = (error, mensagemPadrao) => {
  if (error?.response?.data?.erro) {
    return error.response.data.erro
  }

  if (error?.response?.data?.message) {
    return error.response.data.message
  }

  if (error?.message) {
    return error.message
  }

  return mensagemPadrao
}

const normalizarId = (valor) => {
  if (valor === null || valor === undefined) return ''
  return String(valor).trim()
}

const normalizarPayloadCliente = (cliente = {}) => ({
  nome: cliente.nome || '',
  cpf: cliente.cpf || '',
  email: cliente.email || '',
  telefone: cliente.telefone || '',
  endereco: typeof cliente.endereco === 'string' ? cliente.endereco : cliente.endereco?.logradouro || '',
})

const normalizarPayloadProduto = (produto = {}) => ({
  codigo: produto.codigo || '',
  nome: produto.nome || '',
  descricao: produto.descricao || '',
  preco: String(produto.preco ?? 0),
  estoque: Number(produto.estoque ?? produto.quantidadeEmEstoque ?? 0),
  disponivel: Boolean(produto.disponivel ?? produto.ativo ?? true),
})

export const listarClientes = async () => {
  const resposta = await api.get('/clientes')
  return resposta.data
}

export const cadastrarCliente = async (cliente) => {
  try {
    const resposta = await api.post('/clientes', normalizarPayloadCliente(cliente))
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível cadastrar o cliente. Tente novamente.'), { cause: error })
  }
}

export const atualizarCliente = async (cpf, cliente) => {
  try {
    const cpfNormalizado = normalizarId(cpf)
    if (!cpfNormalizado) {
      throw new Error('CPF do cliente não foi informado.')
    }

    const resposta = await api.put(`/clientes/${encodeURIComponent(cpfNormalizado)}`, normalizarPayloadCliente(cliente))
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível atualizar o cliente. Tente novamente.'), { cause: error })
  }
}

export const excluirCliente = async (cpf) => {
  try {
    const cpfNormalizado = normalizarId(cpf)
    if (!cpfNormalizado) {
      throw new Error('CPF do cliente não foi informado.')
    }

    await api.delete(`/clientes/${encodeURIComponent(cpfNormalizado)}`)
    return true
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível excluir o cliente. Tente novamente.'), { cause: error })
  }
}

export const listarProdutos = async () => {
  const resposta = await api.get('/produtos')
  return resposta.data
}

export const cadastrarProduto = async (produto) => {
  try {
    const resposta = await api.post('/produtos', normalizarPayloadProduto(produto))
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível cadastrar o produto. Tente novamente.'), { cause: error })
  }
}

export const atualizarProduto = async (codigo, produto) => {
  try {
    const codigoNormalizado = normalizarId(codigo)
    if (!codigoNormalizado) {
      throw new Error('Código do produto não foi informado.')
    }

    const resposta = await api.put(`/produtos/${encodeURIComponent(codigoNormalizado)}`, normalizarPayloadProduto(produto))
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível atualizar o produto. Tente novamente.'), { cause: error })
  }
}

export const excluirProduto = async (codigo) => {
  try {
    const codigoNormalizado = normalizarId(codigo)
    if (!codigoNormalizado) {
      throw new Error('Código do produto não foi informado.')
    }

    await api.delete(`/produtos/${encodeURIComponent(codigoNormalizado)}`)
    return true
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível excluir o produto. Tente novamente.'), { cause: error })
  }
}

export const listarPedidos = async () => {
  const resposta = await api.get('/pedidos')
  return resposta.data
}

/**
 * POST /pedidos — contrato do back-end (PedidoRequest / ItemPedidoRequest):
 * { clienteCpf: "78617768916", itens: [ { codigo: "CAM-001", quantidade: 2 } ] }
 */
export const criarPedido = async ({ clienteCpf, itens = [] }) => {
  try {
    const resposta = await api.post('/pedidos', {
      clienteCpf: normalizarId(clienteCpf),
      itens: itens.map((item) => ({ codigo: normalizarId(item.codigo), quantidade: Number(item.quantidade) })),
    })
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível registrar o pedido. Tente novamente.'), { cause: error })
  }
}

export const buscarPedido = async (numero) => {
  try {
    const resposta = await api.get(`/pedidos/${encodeURIComponent(normalizarId(numero))}`)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível carregar o pedido. Tente novamente.'), { cause: error })
  }
}

/**
 * POST /pagamentos — contrato do back-end (PagamentoRequest). Campos por tipo:
 * PIX: chave, tipoChave | BOLETO: vencimento (AAAA-MM-DD), codigo | CARTAO: numero, bandeira, parcelas
 */
export const registrarPagamento = async (pagamento) => {
  try {
    const resposta = await api.post('/pagamentos', pagamento)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível registrar o pagamento. Tente novamente.'), { cause: error })
  }
}

export const buscarDashboard = async () => {
  const resposta = await api.get('/dashboard')
  return resposta.data
}

export const listarHistorico = async () => {
  const resposta = await api.get('/historico')
  return resposta.data
}

export const listarPagamentos = async () => {
  const resposta = await api.get('/pagamentos')
  return resposta.data
}
