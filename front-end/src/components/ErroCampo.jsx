/**
 * Mensagem de validação que o back-end devolveu para um campo (ErroResponse.campos).
 * Não renderiza nada quando o campo está válido.
 */
export function ErroCampo({ mensagem }) {
  if (!mensagem) return null
  return <span className="field-error">{mensagem}</span>
}
