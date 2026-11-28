import { useState } from 'react'
import { registrarPagamento } from '../api/clienteApi'
import { ErroCampo } from './ErroCampo'

const formatCurrency = (valor) =>
  new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(valor || 0))

// Data de hoje no fuso LOCAL (AAAA-MM-DD). toISOString() usaria UTC e, à noite
// no Brasil, já devolveria o dia seguinte.
const hoje = () => {
  const agora = new Date()
  const doisDigitos = (n) => String(n).padStart(2, '0')
  return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`
}

/**
 * Campos que cada forma de pagamento exige no POST /api/pedidos/{numero}/pagamento.
 * Espelha a FabricaFormaPagamento do back-end: trocar o tipo troca os campos,
 * sem if/switch espalhado pelo formulário.
 */
const FORMAS = {
  PIX: {
    rotulo: 'Pix',
    inicial: { chave: '', tipoChave: 'EMAIL' },
    validar: (c) => (!c.chave.trim() ? 'Informe a chave Pix.' : ''),
    montar: (c) => ({ chave: c.chave.trim(), tipoChave: c.tipoChave }),
  },
  BOLETO: {
    rotulo: 'Boleto',
    inicial: { vencimento: '', codigo: '' },
    validar: (c) => {
      if (!c.vencimento) return 'Informe o vencimento do boleto.'
      if (c.vencimento < hoje()) return 'O vencimento não pode estar no passado.'
      if (!c.codigo.trim()) return 'Informe o código de barras.'
      return ''
    },
    montar: (c) => ({ vencimento: c.vencimento, codigo: c.codigo.trim() }),
  },
  CARTAO: {
    rotulo: 'Cartão de crédito',
    inicial: { finalCartao: '', bandeira: 'Visa', parcelas: '1' },
    validar: (c) => (!/^\d{4}$/.test(c.finalCartao) ? 'Informe os 4 últimos dígitos do cartão.' : ''),
    // Só o final do cartão sai do navegador, já mascarado.
    montar: (c) => ({ numero: `**** ${c.finalCartao}`, bandeira: c.bandeira, parcelas: Number(c.parcelas) }),
  },
}

export function PagamentoForm({ pedido, onPagamentoRegistrado }) {
  const [tipo, setTipo] = useState('PIX')
  const [campos, setCampos] = useState(FORMAS.PIX.inicial)
  const [erro, setErro] = useState('')
  // Mensagens por campo vindas do back-end (400 com "campos").
  const [errosCampos, setErrosCampos] = useState({})
  const [enviando, setEnviando] = useState(false)

  const handleTipo = (event) => {
    const novoTipo = event.target.value
    setTipo(novoTipo)
    setCampos(FORMAS[novoTipo].inicial)
    setErro('')
    setErrosCampos({})
  }

  const handleCampo = (event) => {
    const { name, value } = event.target
    setCampos((atual) => ({ ...atual, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const forma = FORMAS[tipo]
    const mensagem = forma.validar(campos)
    if (mensagem) {
      setErro(mensagem)
      return
    }

    setErro('')
    setErrosCampos({})
    setEnviando(true)
    try {
      await registrarPagamento(pedido.numero, {
        tipo,
        valor: String(pedido.valorTotal),
        ...forma.montar(campos),
      })
      onPagamentoRegistrado?.()
    } catch (error) {
      setErro(error.message)
      setErrosCampos(error.campos ?? {})
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Pagamento</h2>
      </div>

      <form className="form-grid" onSubmit={handleSubmit} noValidate>
        <label>
          Forma de pagamento
          <select name="tipo" value={tipo} onChange={handleTipo}>
            {Object.entries(FORMAS).map(([chave, forma]) => (
              <option key={chave} value={chave}>{forma.rotulo}</option>
            ))}
          </select>
          <ErroCampo mensagem={errosCampos.tipo} />
        </label>

        <label>
          Valor
          <input value={formatCurrency(pedido.valorTotal)} readOnly aria-readonly="true" />
          <ErroCampo mensagem={errosCampos.valor} />
        </label>

        {tipo === 'PIX' && (
          <>
            <label>
              Tipo da chave
              <select name="tipoChave" value={campos.tipoChave} onChange={handleCampo}>
                <option value="EMAIL">E-mail</option>
                <option value="CPF">CPF</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Aleatória</option>
              </select>
            </label>
            <label>
              Chave Pix
              <input name="chave" value={campos.chave} onChange={handleCampo} placeholder="cliente@email.com" />
            </label>
          </>
        )}

        {tipo === 'BOLETO' && (
          <>
            <label>
              Vencimento
              <input name="vencimento" type="date" min={hoje()} value={campos.vencimento} onChange={handleCampo} />
            </label>
            <label>
              Código de barras
              <input name="codigo" value={campos.codigo} onChange={handleCampo} placeholder="34191.79001 01043.510047" />
            </label>
          </>
        )}

        {tipo === 'CARTAO' && (
          <>
            <label>
              Bandeira
              <select name="bandeira" value={campos.bandeira} onChange={handleCampo}>
                <option>Visa</option>
                <option>Mastercard</option>
                <option>Elo</option>
                <option>Amex</option>
              </select>
            </label>
            <label>
              Últimos 4 dígitos
              <input name="finalCartao" inputMode="numeric" maxLength={4} value={campos.finalCartao} onChange={handleCampo} placeholder="4242" />
            </label>
            <label>
              Parcelas
              <select name="parcelas" value={campos.parcelas} onChange={handleCampo}>
                {Array.from({ length: 12 }, (_, i) => String(i + 1)).map((n) => (
                  <option key={n} value={n}>{n}x</option>
                ))}
              </select>
            </label>
          </>
        )}

        {erro && <div className="alert alert-error full-width" role="alert">{erro}</div>}

        <div className="full-width actions-row">
          <button type="submit" className="btn btn-primary" disabled={enviando}>
            {enviando ? 'Processando...' : `Pagar ${formatCurrency(pedido.valorTotal)}`}
          </button>
        </div>
      </form>
    </section>
  )
}
