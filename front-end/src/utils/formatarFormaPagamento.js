/**
 * Nome legível de uma forma de pagamento, usado em todas as telas.
 *
 * A API devolve o nome da classe (Pix, Boleto, CartaoCredito, ValeCompras) e
 * o POST usa códigos (PIX, BOLETO, CARTAO). A chave é normalizada (sem
 * acento, "_", "-" ou espaço e em minúsculas), então todas essas grafias caem
 * no mesmo nome.
 */
const NOMES = {
  pix: 'Pix',
  boleto: 'Boleto',
  cartao: 'Cartão de crédito',
  cartaocredito: 'Cartão de crédito',
  valecompras: 'Vale-compras',
}

const normalizar = (valor) =>
  String(valor)
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-zA-Z]/g, '')
    .toLowerCase()

export function formatarFormaPagamento(forma) {
  if (forma === null || forma === undefined || String(forma).trim() === '') {
    return 'Não informado'
  }
  return NOMES[normalizar(forma)] ?? String(forma)
}
