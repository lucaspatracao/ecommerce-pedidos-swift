package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Cliente responsável pela compra no e-commerce.
 *
 * <p>O CPF é a identidade de negócio do cliente: é validado pelos dígitos
 * verificadores e não muda depois do cadastro (por isso não há setter).
 * Nome e contato são alterados juntos por {@link #atualizarContato}.</p>
 *
 * <p>Na tabela, o campo herdado {@code documento} vira a coluna {@code cpf}
 * ({@code @AttributeOverride}).</p>
 */
@Entity
@AttributeOverride(name = "documento", column = @Column(name = "cpf", nullable = false, unique = true, length = 11))
public class Cliente extends Pessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Embedded
    private Endereco endereco;

    protected Cliente() {
        // exigido pelo JPA
    }

    /**
     * Construtor usado pela API, que recebe o endereço como texto livre.
     * Cidade e CEP ficam como "Não informado" porque o front só envia o logradouro.
     */
    public Cliente(String nome, String cpf, String email, String telefone, String endereco) {
        this(nome, cpf, email, telefone, Endereco.aPartirDoTexto(endereco));
    }

    public Cliente(String nome, String cpf, String email, String telefone, Endereco endereco) {
        // O CPF é validado e normalizado ANTES de chegar à superclasse.
        super(nome, Utils.validarCpf(cpf));
        definirContato(email, telefone, endereco);
    }

    public Long getId() {
        return id;
    }

    public String getCpf() {
        return getDocumento();
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    /**
     * Atualiza os dados editáveis do cliente de uma só vez.
     * Se algum valor for inválido, nada é alterado no nome (a validação do
     * contato acontece antes da troca do nome).
     */
    public void atualizarContato(String nome, String email, String telefone, Endereco endereco) {
        String emailValidado = Utils.validarEmail(email);
        String telefoneValidado = validarTelefone(telefone);
        alterarNome(nome);
        this.email = emailValidado;
        this.telefone = telefoneValidado;
        this.endereco = endereco;
    }

    private void definirContato(String email, String telefone, Endereco endereco) {
        this.email = Utils.validarEmail(email);
        this.telefone = validarTelefone(telefone);
        this.endereco = endereco;
    }

    private static String validarTelefone(String telefone) {
        String valor = Utils.textoObrigatorio(telefone, "Telefone");
        if (!valor.matches("\\d+")) {
            throw new IllegalArgumentException("Telefone deve conter apenas dígitos.");
        }
        return valor;
    }

    /** Identificação textual do cliente, usada em recibos e logs de pedido. */
    @Override
    public String getIdentificacao() {
        return String.format("%s (%s)", getNome(), getDocumento());
    }

    @Override
    public String toString() {
        return String.format("Cliente: %s | CPF: %s | E-mail: %s", getNome(), getDocumento(), email);
    }
}
