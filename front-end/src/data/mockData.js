export const clientes = [
  {
    id: 1,
    nome: 'Maria Silva',
    cpf: '12345678900',
    email: 'maria.silva@email.com',
    telefone: '11999999999',
    endereco: 'Rua das Flores, 123 - São Paulo/SP',
  },
  {
    id: 2,
    nome: 'João Pereira',
    cpf: '98765432100',
    email: 'joao.pereira@email.com',
    telefone: '11988887777',
    endereco: 'Avenida Brasil, 456 - Campinas/SP',
  },
  {
    id: 3,
    nome: 'Ana Souza',
    cpf: '45678912300',
    email: 'ana.souza@email.com',
    telefone: '11977776666',
    endereco: 'Rua do Comércio, 89 - Ribeirão Preto/SP',
  },
]

export const produtos = [
  {
    id: 1,
    nome: 'Notebook Swift Pro',
    descricao: 'Ultrabook 14" com SSD 512GB e 16GB de RAM.',
    preco: 4899.9,
    estoque: 18,
    categoria: 'Eletrônicos',
    disponivel: true,
  },
  {
    id: 2,
    nome: 'Smart TV 55"',
    descricao: 'Tela 4K com sistema Android e áudio Dolby.',
    preco: 3499.9,
    estoque: 7,
    categoria: 'Eletrônicos',
    disponivel: true,
  },
  {
    id: 3,
    nome: 'Cafeteira Premium',
    descricao: 'Cafeteira automática com preparo programável.',
    preco: 699.0,
    estoque: 26,
    categoria: 'Casa',
    disponivel: true,
  },
  {
    id: 4,
    nome: 'Monitor 27"',
    descricao: 'Display Full HD com bordas finas e alto contraste.',
    preco: 1299.9,
    estoque: 12,
    categoria: 'Periféricos',
    disponivel: false,
  },
]

export const pedidos = [
  {
    id: 'SW-1042',
    cliente: 'Maria Silva',
    data: '2026-09-15',
    itens: 2,
    valor: 689.9,
    formaPagamento: 'Cartão de crédito',
    status: 'Em processamento',
  },
  {
    id: 'SW-1038',
    cliente: 'João Pereira',
    data: '2026-09-14',
    itens: 4,
    valor: 1825.5,
    formaPagamento: 'Pix',
    status: 'Pago',
  },
  {
    id: 'SW-1031',
    cliente: 'Ana Souza',
    data: '2026-09-12',
    itens: 1,
    valor: 699.0,
    formaPagamento: 'Boleto',
    status: 'Pendente',
  },
  {
    id: 'SW-1027',
    cliente: 'Maria Silva',
    data: '2026-09-08',
    itens: 3,
    valor: 2499.9,
    formaPagamento: 'Cartão de crédito',
    status: 'Concluído',
  },
]

export const pagamentos = [
  {
    tipo: 'Cartão de crédito',
    metodo: 'Visa •••• 2451',
    valor: 4199.9,
    status: 'Pago',
    ultimaTransacao: 'Hoje, 10:35',
  },
  {
    tipo: 'Boleto',
    metodo: 'Código: 6347-9001',
    valor: 699.0,
    status: 'Pendente',
    ultimaTransacao: 'Hoje, 08:10',
  },
  {
    tipo: 'Pix',
    metodo: 'Chave: cliente@email.com',
    valor: 1825.5,
    status: 'Concluído',
    ultimaTransacao: 'Ontem, 19:22',
  },
]

export const historicoCompras = [
  {
    titulo: 'Pedido SW-1042 entregue',
    descricao: 'Entrega confirmada para Maria Silva em São Paulo/SP.',
    data: '15 set 2026',
  },
  {
    titulo: 'Pagamento confirmado',
    descricao: 'Pagamento via Pix do pedido SW-1038 foi aprovado.',
    data: '14 set 2026',
  },
  {
    titulo: 'Novo cadastro de cliente',
    descricao: 'Ana Souza foi incluída no cadastro de clientes.',
    data: '12 set 2026',
  },
  {
    titulo: 'Estoque revisado',
    descricao: 'Produtos da categoria Eletrônicos tiveram inventário atualizado.',
    data: '08 set 2026',
  },
]

export const dashboardMetrics = [
  { titulo: 'Receita total', valor: 'R$ 89.4K', detalhe: '+12.4% vs. mês anterior' },
  { titulo: 'Pedidos', valor: '1.284', detalhe: '234 concluídos este mês' },
  { titulo: 'Clientes ativos', valor: '842', detalhe: '+7.8% no período' },
  { titulo: 'Taxa de conversão', valor: '4.6%', detalhe: 'Meta atual: 5.0%' },
]
