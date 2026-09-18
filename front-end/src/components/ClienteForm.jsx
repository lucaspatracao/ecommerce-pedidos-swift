import { useEffect, useState } from 'react'
import { atualizarCliente, cadastrarCliente } from '../api/clienteApi'

const estadoInicial = {
  nome: '',
  cpf: '',
  email: '',
  telefone: '',
  endereco: '',
}

export function ClienteForm({ cliente = null, onClienteSalvo, onCancelarEdicao }) {
  const [form, setForm] = useState(cliente ?? estadoInicial)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')
  const isEdicao = Boolean(cliente)

  useEffect(() => {
    setForm(cliente ?? estadoInicial)
  }, [cliente])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((values) => ({ ...values, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErro('')
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
        </label>

        <label>
          CPF
          <input name="cpf" value={form.cpf} onChange={handleChange} placeholder="Apenas números" required />
        </label>

        <label>
          E-mail
          <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="cliente@email.com" required />
        </label>

        <label>
          Telefone
          <input name="telefone" value={form.telefone} onChange={handleChange} placeholder="11999999999" required />
        </label>

        <label className="full-width">
          Endereço
          <input name="endereco" value={form.endereco} onChange={handleChange} placeholder="Rua das Flores, 123" required />
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
