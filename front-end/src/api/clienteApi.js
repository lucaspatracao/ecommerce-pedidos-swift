import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

const tratarErro = (error, mensagemPadrao) => {
  if (error?.response?.data?.message) {
    return error.response.data.message
  }

  if (error?.message) {
    return error.message
  }

  return mensagemPadrao
}

export const listarClientes = async () => {
  const resposta = await api.get('/clientes')
  return resposta.data
}

export const cadastrarCliente = async (cliente) => {
  try {
    const resposta = await api.post('/clientes', cliente)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível cadastrar o cliente. Tente novamente.'))
  }
}

export const atualizarCliente = async (cpf, cliente) => {
  try {
    const resposta = await api.put(`/clientes/${cpf}`, cliente)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível atualizar o cliente. Tente novamente.'))
  }
}

export const excluirCliente = async (cpf) => {
  try {
    await api.delete(`/clientes/${cpf}`)
    return true
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível excluir o cliente. Tente novamente.'))
  }
}

export const listarProdutos = async () => {
  const resposta = await api.get('/produtos')
  return resposta.data
}

export const cadastrarProduto = async (produto) => {
  try {
    const resposta = await api.post('/produtos', produto)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível cadastrar o produto. Tente novamente.'))
  }
}

export const atualizarProduto = async (codigo, produto) => {
  try {
    const resposta = await api.put(`/produtos/${codigo}`, produto)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível atualizar o produto. Tente novamente.'))
  }
}

export const excluirProduto = async (codigo) => {
  try {
    await api.delete(`/produtos/${codigo}`)
    return true
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível excluir o produto. Tente novamente.'))
  }
}

export const listarPedidos = async () => {
  const resposta = await api.get('/pedidos')
  return resposta.data
}

export const criarPedido = async (pedido) => {
  try {
    const resposta = await api.post('/pedidos', pedido)
    return resposta.data
  } catch (error) {
    throw new Error(tratarErro(error, 'Não foi possível registrar o pedido. Tente novamente.'))
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
