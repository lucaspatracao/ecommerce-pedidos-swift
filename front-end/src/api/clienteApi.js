import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

/**
 * Erro vindo da API, já traduzido para a tela.
 * - message: o campo "mensagem" do corpo de erro padronizado (ErroResponse);
 * - campos: { nomeDoCampo: "mensagem" } quando o back-end recusou a validação (400),
 *   para cada formulário mostrar a mensagem junto do campo certo;
 * - status: código HTTP (undefined quando nem houve resposta, ex.: back-end fora do ar).
 */
export class ErroApi extends Error {
  constructor(mensagem, { campos = {}, status, cause } = {}) {
    super(mensagem, { cause })
    this.name = 'ErroApi'
    this.campos = campos
    this.status = status
  }
}

// Corpo de erro da API: { status, erro, mensagem, caminho, campos?: [{ campo, mensagem }] }
const erroDaApi = (error, mensagemPadrao) => {
  const corpo = error?.response?.data
  const campos = Object.fromEntries((corpo?.campos ?? []).map(({ campo, mensagem }) => [campo, mensagem]))
  const mensagem = corpo?.mensagem || error?.message || mensagemPadrao
  return new ErroApi(mensagem, { campos, status: error?.response?.status, cause: error })
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
    throw erroDaApi(error, 'Não foi possível cadastrar o cliente. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível atualizar o cliente. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível excluir o cliente. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível cadastrar o produto. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível atualizar o produto. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível excluir o produto. Tente novamente.')
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
    throw erroDaApi(error, 'Não foi possível registrar o pedido. Tente novamente.')
  }
}

export const buscarPedido = async (numero) => {
  try {
    const resposta = await api.get(`/pedidos/${encodeURIComponent(normalizarId(numero))}`)
    return resposta.data
  } catch (error) {
    throw erroDaApi(error, 'Não foi possível carregar o pedido. Tente novamente.')
  }
}

/**
 * POST /pedidos/{numero}/pagamento — o pagamento é sub-recurso do pedido.
 * Corpo (PagamentoRequest): { tipo, valor, ...campos do tipo }
 * PIX: chave, tipoChave | BOLETO: vencimento (AAAA-MM-DD), codigo | CARTAO: numero, bandeira, parcelas
 */
export const registrarPagamento = async (numeroPedido, pagamento) => {
  try {
    const resposta = await api.post(`/pedidos/${encodeURIComponent(normalizarId(numeroPedido))}/pagamento`, pagamento)
    return resposta.data
  } catch (error) {
    throw erroDaApi(error, 'Não foi possível registrar o pagamento. Tente novamente.')
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
