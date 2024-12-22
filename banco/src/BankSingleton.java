import java.util.HashMap;
import java.util.Map;
//singleton pois como cada instancia de banco deve ser unica, nao faz sentido ter mais de uma instancia de banco
public class BankSingleton {
    private static BankSingleton instance;
    private static final String CURRENT_DATE = "2024-12-22 00:34:37";
    private static final String CURRENT_USER = "perrijuan";

    // Constantes do sistema bancário
    private static final float TAXA_TRANSFERENCIA_NORMAL = 8.0f;
    private static final float TAXA_TRANSFERENCIA_VIP = 0.008f; // 0.8%
    private static final float TAXA_GERENTE = 50.0f;
    private static final float LIMITE_TRANSFERENCIA_NORMAL = 1000.0f;

    private ContaBancaria contaAtual;
    private final FileManagerSingleton fileManager;
    private final Map<String, ExtratoObserver> observers;

    private BankSingleton() {
        this.fileManager = FileManagerSingleton.getInstance();
        this.observers = new HashMap<>();
        System.out.println("Sistema Bancário iniciado em: " + CURRENT_DATE);
        System.out.println("Usuário do sistema: " + CURRENT_USER);
    }

    public static BankSingleton getInstance() {
        if (instance == null) {
            instance = new BankSingleton();
        }
        return instance;
    }

    // Métodos de autenticação
    public boolean login(String numeroConta, String senha) {
        ContaBancaria conta = fileManager.buscarConta(numeroConta);
        if (conta != null && conta.getSenha().equals(senha)) {
            contaAtual = conta;
            if (!observers.containsKey(numeroConta)) {
                observers.put(numeroConta, new ExtratoObserver(numeroConta));
            }
            atualizarSaldoNegativo();
            return true;
        }
        return false;
    }

    public void logout() {
        contaAtual = null;
    }

    // Operações bancárias
    public void deposito(float valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo!");
        }

        contaAtual.setSaldo(contaAtual.getSaldo() + valor);
        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.DEPOSITO,
                valor,
                "Depósito em conta",
                contaAtual.getNumeroConta(),
                null
        ));
        fileManager.salvarConta(contaAtual);
    }

    public void saque(float valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo!");
        }

        atualizarSaldoNegativo();

        if (!contaAtual.isVip() && contaAtual.getSaldo() < valor) {
            throw new IllegalStateException("Saldo insuficiente!");
        }

        contaAtual.setSaldo(contaAtual.getSaldo() - valor);
        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.SAQUE,
                -valor,
                "Saque em conta",
                contaAtual.getNumeroConta(),
                null
        ));
        fileManager.salvarConta(contaAtual);
    }

    public void transferencia(String contaDestino, float valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo!");
        }

        if (contaAtual.getNumeroConta().equals(contaDestino)) {
            throw new IllegalArgumentException("Não é possível transferir para a própria conta!");
        }

        ContaBancaria destino = fileManager.buscarConta(contaDestino);
        if (destino == null) {
            throw new IllegalArgumentException("Conta destino não encontrada!");
        }

        float taxa = contaAtual.isVip() ? valor * TAXA_TRANSFERENCIA_VIP : TAXA_TRANSFERENCIA_NORMAL;
        float valorTotal = valor + taxa;

        if (!contaAtual.isVip() && valor > LIMITE_TRANSFERENCIA_NORMAL) {
            throw new IllegalArgumentException("Limite de transferência excedido!");
        }

        if (!contaAtual.isVip() && contaAtual.getSaldo() < valorTotal) {
            throw new IllegalStateException("Saldo insuficiente!");
        }

        // Registra transferência e taxa
        contaAtual.setSaldo(contaAtual.getSaldo() - valorTotal);
        destino.setSaldo(destino.getSaldo() + valor);

        // Registra transações para ambas as contas
        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.TRANSFERENCIA_ENVIADA,
                -valor,
                "Transferência enviada para " + contaDestino,
                contaAtual.getNumeroConta(),
                contaDestino
        ));

        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.TAXA_TRANSFERENCIA,
                -taxa,
                "Taxa de transferência",
                contaAtual.getNumeroConta(),
                null
        ));

        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.TRANSFERENCIA_RECEBIDA,
                valor,
                "Transferência recebida de " + contaAtual.getNumeroConta(),
                contaDestino,
                contaAtual.getNumeroConta()
        ));

        fileManager.salvarConta(contaAtual);
        fileManager.salvarConta(destino);
    }

    public void solicitarGerente() {
        if (!contaAtual.isVip()) {
            throw new IllegalStateException("Apenas contas VIP podem solicitar gerente!");
        }

        contaAtual.setSaldo(contaAtual.getSaldo() - TAXA_GERENTE);
        registrarTransacao(new Transacao(
                Transacao.TipoTransacao.VISITA_GERENTE,
                -TAXA_GERENTE,
                "Solicitação de visita do gerente",
                contaAtual.getNumeroConta(),
                null
        ));
        fileManager.salvarConta(contaAtual);
    }

    // Métodos auxiliares
    private void registrarTransacao(Transacao transacao) {
        ExtratoObserver observer = observers.get(transacao.getContaOrigem());
        if (observer != null) {
            observer.onTransacao(transacao);
        }
        if (transacao.getContaDestino() != null) {
            ExtratoObserver observerDestino = observers.get(transacao.getContaDestino());
            if (observerDestino != null) {
                observerDestino.onTransacao(transacao);
            }
        }
    }

    private void atualizarSaldoNegativo() {
        if (contaAtual != null) {
            contaAtual.aplicarJurosNegativo();
            fileManager.salvarConta(contaAtual);
        }
    }

    // Getters
    public ContaBancaria getContaAtual() {
        return contaAtual;
    }

    public ExtratoObserver getExtratoObserver() {
        return observers.get(contaAtual.getNumeroConta());
    }

    // Método para criar contas (usado apenas na inicialização)
    public void criarConta(String numeroConta, String tipo, String login, String senha) {
        ContaBancaria novaConta = ContaFactory.criarConta(numeroConta, tipo, login, senha);
        fileManager.salvarConta(novaConta);
    }
}