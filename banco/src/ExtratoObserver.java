import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
//uso de observer para notificar o extrato sobre as transacoes realizadas

public class ExtratoObserver implements ContaObserver, Serializable {
    private static final long serialVersionUID = 1L;
    private final String numeroConta;
    private final List<Transacao> transacoes;

    public ExtratoObserver(String numeroConta) {
        this.numeroConta = numeroConta;
        this.transacoes = new ArrayList<>();
    }

    @Override
    public void onTransacao(Transacao transacao) {
        transacoes.add(transacao);
        salvarTransacao(transacao);
    }

    public void imprimirExtrato() {
        System.out.println("\n=== EXTRATO BANCÁRIO ===");
        System.out.println("Conta: " + numeroConta);
        System.out.println("Data/Hora: " + LocalDateTime.now());
        System.out.println("=====================");

        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação encontrada.");
        } else {
            transacoes.forEach(t -> {
                String valor = t.getValor() < 0 ?
                        String.format("(R$ %.2f)", Math.abs(t.getValor())) :
                        String.format("R$ %.2f", t.getValor());
                System.out.printf("%s - %s: %s%n",
                        t.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                        t.getDescricao(),
                        valor);
            });
        }
        System.out.println("=====================");
    }

    private void salvarTransacao(Transacao transacao) {
        FileManagerSingleton.getInstance().salvarTransacao(numeroConta, transacao);
    }
}