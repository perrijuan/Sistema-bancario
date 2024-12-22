import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TesteBancario {
    private BankSingleton bank;

    @BeforeEach
    void setup() {
        bank = BankSingleton.getInstance();
        // criar contas padrão para testes
        bank.criarConta("11111", "NORMAL", "testUser1", "1234");
        bank.criarConta("22222", "VIP", "testUser2", "5678");
    }

    @Test
    void loginComCredenciaisCorretasDeveRetornarTrue() {
        assertTrue(bank.login("11111", "1234"),
                "Login deve ser bem-sucedido com credenciais corretas");
    }

    @Test
    void loginComCredenciaisIncorretasDeveRetornarFalse() {
        assertFalse(bank.login("11111", "wrong"),
                "Login deve falhar com credenciais incorretas");
    }

    @Test
    void depositoDeveSomarAoSaldo() {
        // Arrange
        bank.login("11111", "1234");
        float saldoInicial = bank.getContaAtual().getSaldo();
        float valorDeposito = 100.0f;

        // soma ao saldo
        bank.deposito(valorDeposito);

        // como credito
        assertEquals(saldoInicial + valorDeposito,
                bank.getContaAtual().getSaldo(),
                0.01,
                "Saldo deve ser atualizado após depósito");
    }

    @Test
    void depositoNegativoDeveLancarExcecao() {
        // Arrange
        bank.login("11111", "1234");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> bank.deposito(-100.0f),
                "Depósito com valor negativo deve lançar exceção");
    }

    @Test
    void saqueDeveSubtrairDoSaldo() {

        bank.login("11111", "1234");
        bank.deposito(200.0f);
        float saldoInicial = bank.getContaAtual().getSaldo();
        float valorSaque = 50.0f;

        // testa saque
        bank.saque(valorSaque);

        // espera o resultado
        assertEquals(saldoInicial - valorSaque,
                bank.getContaAtual().getSaldo(),
                0.01,
                "Saldo deve ser atualizado após saque");
    }

    @Test
    void transferenciaDeveAtualizarSaldosEAplicarTaxa() {

        bank.login("11111", "1234");
        bank.deposito(500.0f);
        float valorTransferencia = 100.0f;
        float taxaTransferencia = 8.0f; // Taxa para conta normal


        bank.transferencia("22222", valorTransferencia);


        float saldoEsperadoOrigem = 500.0f - valorTransferencia - taxaTransferencia;
        assertEquals(saldoEsperadoOrigem,
                bank.getContaAtual().getSaldo(),
                0.01,
                "Saldo da conta origem deve ser atualizado com valor e taxa");

        // Verificar conta destino
        bank.login("22222", "5678");
        assertEquals(valorTransferencia,
                bank.getContaAtual().getSaldo(),
                0.01,
                "Saldo da conta destino deve receber o valor da transferência");
    }

    @Test
    void contaVIPDeveSerIdentificadaCorretamente() {

        bank.login("22222", "5678");


        assertTrue(bank.getContaAtual().isVip(),
                "Conta deve ser identificada como VIP");
    }

    @Test
    void saqueComSaldoInsuficienteDeveGerarExcecao() {

        bank.login("11111", "1234"); // Conta normal
        bank.deposito(100.0f);


        assertThrows(IllegalStateException.class,
                () -> bank.saque(150.0f),
                "Saque com saldo insuficiente deve lançar exceção");
    }

    @Test
    void transferenciaParaPropriaContaDeveGerarExcecao() {
        // entrada
        bank.login("11111", "1234");
        bank.deposito(100.0f);

        // acao e verificacao usando excecao
        assertThrows(IllegalArgumentException.class,
                () -> bank.transferencia("11111", 50.0f),
                "Transferência para própria conta deve lançar exceção");
    }
}