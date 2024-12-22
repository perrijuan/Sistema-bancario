import java.io.Serializable;
import java.time.LocalDateTime;

public class ContaBancaria implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String numeroConta;
    private final String tipoUsuario;
    private final String login;
    private final String senha;
    private float saldo;
    private LocalDateTime ultimoSaqueNegativo;

    public ContaBancaria(String numeroConta, String tipoUsuario, String login, String senha) {
        if (numeroConta.length() != 5) {
            throw new IllegalArgumentException("Número da conta deve ter 5 dígitos!");
        }
        if (senha.length() != 4) {
            throw new IllegalArgumentException("Senha deve ter 4 dígitos!");
        }

        this.numeroConta = numeroConta;
        this.tipoUsuario = tipoUsuario.toUpperCase();
        this.login = login;
        this.senha = senha;
        this.saldo = 0.0f;
    }

    // Getters
    public String getNumeroConta() { return numeroConta; }
    public String getTipoUsuario() { return tipoUsuario; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public float getSaldo() { return saldo; }
    public LocalDateTime getUltimoSaqueNegativo() { return ultimoSaqueNegativo; }

    // Métodos de negócio
    public boolean isVip() {
        return "VIP".equals(tipoUsuario);
    }

    public void setSaldo(float novoSaldo) {
        this.saldo = novoSaldo;
        if (novoSaldo < 0 && isVip()) {
            this.ultimoSaqueNegativo = LocalDateTime.now();
        } else if (novoSaldo >= 0) {
            this.ultimoSaqueNegativo = null;
        }
    }

    public void aplicarJurosNegativo() {
        if (saldo < 0 && ultimoSaqueNegativo != null) {
            long minutosPassados = java.time.Duration.between(ultimoSaqueNegativo, LocalDateTime.now()).toMinutes();
            float juros = Math.abs(saldo) * 0.001f * minutosPassados; // 0.1% por minuto
            saldo -= juros;
        }
    }

    public boolean validarTransferencia(float valor) {
        if (!isVip() && valor > 1000) {
            return false;
        }
        return isVip() || saldo >= valor;
    }

    @Override
    public String toString() {
        return String.format("Conta: %s | Tipo: %s | Titular: %s | Saldo: R$ %.2f",
                numeroConta, tipoUsuario, login, saldo);
    }
}