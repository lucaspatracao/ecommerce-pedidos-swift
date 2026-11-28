import { useState } from 'react'
import { atualizarCliente, cadastrarCliente } from '../api/clienteApi'
import { ErroCampo } from './ErroCampo'

const estadoInicial = {
  nome: '',
  cpf: '',
  email: '',
  telefone: '',
  endereco: '',
}

const extrairEnderecoTexto = (endereco) => {
  if (!endereco) return ''
  if (typeof endereco === 'string') return endereco.trim()
  if (typeof endereco === 'object') {
    return endereco.logradouro || endereco.rua || endereco.endereco || ''
  }
  return ''
}

const normalizarCliente = (cliente) => {
  if (!cliente) return estadoInicial

  return {
    ...cliente,
    cpf: cliente.cpf || '',
    endereco: extrairEnderecoTexto(cliente.endereco),
  }
}

export function ClienteForm({ cliente = null, onClienteSalvo, onCancelarEdicao }) {
  const [form, setForm] = useState(normalizarCliente(cliente))
  const [clienteCarregado, setClienteCarregado] = useState(cliente)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')
  // Mensagens por campo vindas do back-end (400 com "campos").
  const [errosCampos, setErrosCampos] = useState({})
  const isEdicao = Boolean(cliente)

  // Outro cliente escolhido para edição (ou edição cancelada): recarrega o
  // formulário durante a renderização, sem useEffect, como recomenda o React
  // ("ajustar estado quando uma prop muda").
  if (cliente !== clienteCarregado) {
    setClienteCarregado(cliente)
    setForm(normalizarCliente(cliente))
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((values) => ({ ...values, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErro('')
    setErrosCampos({})
    setMensagem('')

    try {
      const clienteSalvo = isEdicao
        ? await atualizarCliente(cliente.cpf, form)
        : await cadastrarCliente(form)

      setMensagem(isEdicao ? 'Cliente atualizado com sucesso.' : 'Cliente cadastrado com sucesso.')
      setForm(estadoInicial)
      onClienteSalvo?.(clienteSalvo, isEdicao)
      if (isEdicao) {
        onCancelarEdicao?.()
      }
    } catch (error) {
      setErro(error.message || 'Não foi possível salvar o cliente. Tente novamente.')
      setErrosCampos(error.campos ?? {})
    } finally {
      setIsSubmitting(false)
    }
  }

  const titulo = isEdicao ? 'Editar cliente' : 'Cadastro de cliente'
  const textoBotao = isEdicao ? 'Salvar alterações' : 'Cadastrar cliente'

  return (
    <div className="panel">
      <div className="panel-header">
        <h2>{titulo}</h2>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Nome
          <input name="nome" value={form.nome} onChange={handleChange} placeholder="Ex.: Maria Silva" required />
          <ErroCampo mensagem={errosCampos.nome} />
        </label>

        <label>
          CPF
          <input name="cpf" value={form.cpf} onChange={handleChange} placeholder="Apenas números" required />
          <ErroCampo mensagem={errosCampos.cpf} />
        </label>

        <label>
          E-mail
          <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="cliente@email.com" required />
          <ErroCampo mensagem={errosCampos.email} />
        </label>

        <label>
          Telefone
          <input name="telefone" value={form.telefone} onChange={handleChange} placeholder="11999999999" required />
          <ErroCampo mensagem={errosCampos.telefone} />
        </label>

        <label className="full-width">
          Endereço
          <input name="endereco" value={form.endereco} onChange={handleChange} placeholder="Rua das Flores, 123" required />
          <ErroCampo mensagem={errosCampos.endereco} />
        </label>

        {mensagem && <div className="alert alert-success full-width">{mensagem}</div>}
        {erro && <div className="alert alert-error full-width">{erro}</div>}

        <div className="full-width actions-row">
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando...' : textoBotao}
          </button>
          {isEdicao && (
            <button type="button" className="btn btn-ghost" onClick={onCancelarEdicao}>
              Cancelar
            </button>
          )}
        </div>
      </form>
    </div>
  )
}
