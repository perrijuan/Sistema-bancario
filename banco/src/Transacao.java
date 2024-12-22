import java.io.Serializable;
import java.time.LocalDateTime;

public class Transacao implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TipoTransacao {
        DEPOSITO, SAQUE, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECEBIDA,
        TAXA_TRANSFERENCIA, VISITA_GERENTE
    }

    private final LocalDateTime data;
    private final TipoTransacao tipo;
    private final float valor;
    private final String descricao;
    private final String contaOrigem;
    private final String contaDestino;

    public Transacao(TipoTransacao tipo, float valor, String descricao,
                     String contaOrigem, String contaDestino) {
        this.data = LocalDateTime.now();
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
    }

    // Getters
    public LocalDateTime getData() { return data; }
    public TipoTransacao getTipo() { return tipo; }
    public float getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public String getContaOrigem() { return contaOrigem; }
    public String getContaDestino() { return contaDestino; }
}