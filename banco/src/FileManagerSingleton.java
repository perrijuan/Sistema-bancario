import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//preferir salvar o arquivo em .dat ao invés de .txt para evitar que o usuário possa alterar o conteúdo do arquivo
//gera uma maior seguranca para o sistema
//define o lugar onde os arquivos serao salvos
/**
 * Gerenciador de arquivos para persistência de dados
 * Implementa o padrão Singleton para gerenciamento centralizado
 */
public class FileManagerSingleton implements Serializable {
    private static final long serialVersionUID = 1L;
    private static FileManagerSingleton instance;
    private static final String CURRENT_DATE = "2024-12-22 02:17:08";
    private static final String CURRENT_USER = "billgates";

    // Diretórios e arquivos
    private static final String DATA_DIR = "bank_data";
    private static final String CONTAS_FILE = DATA_DIR + "/contas.dat";
    private static final String TRANSACOES_DIR = DATA_DIR + "/transacoes/";

    private final Map<String, ContaBancaria> contasCache;
    private final Map<String, List<Transacao>> transacoesCache;

    private FileManagerSingleton() {
        contasCache = new HashMap<>();
        transacoesCache = new HashMap<>();
        inicializarDiretorios();
        carregarDados();
    }

    public static synchronized FileManagerSingleton getInstance() {
        if (instance == null) {
            instance = new FileManagerSingleton();
        }
        return instance;
    }

    /**
     * Inicializa os diretórios necessários
     */
    private void inicializarDiretorios() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(TRANSACOES_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretórios: " + e.getMessage());
        }
    }

    /**
     * Salva uma conta no arquivo e no cache
     */
    public void salvarConta(ContaBancaria conta) {
        contasCache.put(conta.getNumeroConta(), conta);
        persistirContas();
        System.out.println("Conta salva com sucesso: " + conta.getNumeroConta());
    }

    /**
     * Salva uma transação no arquivo e no cache
     */
    public void salvarTransacao(String numeroConta, Transacao transacao) {
        // Adiciona a transação no cache
        transacoesCache.computeIfAbsent(numeroConta, k -> new ArrayList<>())
                .add(transacao);

        // Se for uma transferência, salva também para a conta destino
        if (transacao.getTipo() == Transacao.TipoTransacao.TRANSFERENCIA_ENVIADA) {
            String contaDestino = transacao.getContaDestino();
            Transacao transacaoDestino = new Transacao(
                    Transacao.TipoTransacao.TRANSFERENCIA_RECEBIDA,
                    Math.abs(transacao.getValor()),
                    "Transferência recebida de: " + transacao.getContaOrigem(),
                    transacao.getContaOrigem(),
                    contaDestino
            );

            transacoesCache.computeIfAbsent(contaDestino, k -> new ArrayList<>())
                    .add(transacaoDestino);
        }

        // Persiste as transações
        persistirTransacoes(numeroConta);

        // Se for transferência, persiste também para a conta destino
        if (transacao.getTipo() == Transacao.TipoTransacao.TRANSFERENCIA_ENVIADA) {
            persistirTransacoes(transacao.getContaDestino());
        }

        System.out.println("Transação salva com sucesso para conta: " + numeroConta);
    }

    /**
     * Persiste todas as contas em arquivo
     */
    private void persistirContas() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CONTAS_FILE))) {
            oos.writeObject(new ArrayList<>(contasCache.values()));
        } catch (IOException e) {
            System.err.println("Erro ao persistir contas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Persiste as transações de uma conta em arquivo
     */
    private void persistirTransacoes(String numeroConta) {
        String arquivo = TRANSACOES_DIR + numeroConta + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(arquivo))) {
            List<Transacao> transacoes = transacoesCache.get(numeroConta);
            if (transacoes != null) {
                oos.writeObject(new ArrayList<>(transacoes));
            }
        } catch (IOException e) {
            System.err.println("Erro ao persistir transações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carrega todas as contas e transações dos arquivos
     */
    private void carregarDados() {
        carregarContas();
        carregarTodasTransacoes();
    }

    @SuppressWarnings("unchecked")
    private void carregarContas() {
        File file = new File(CONTAS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            List<ContaBancaria> contas = (List<ContaBancaria>) ois.readObject();
            contas.forEach(c -> contasCache.put(c.getNumeroConta(), c));
            System.out.println("Contas carregadas: " + contasCache.size());
        } catch (Exception e) {
            System.err.println("Erro ao carregar contas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarTodasTransacoes() {
        File dir = new File(TRANSACOES_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files == null) return;

        for (File file : files) {
            String numeroConta = file.getName().replace(".dat", "");
            carregarTransacoes(numeroConta);
        }
        System.out.println("Transações carregadas para " + transacoesCache.size() + " contas");
    }

    @SuppressWarnings("unchecked")
    private void carregarTransacoes(String numeroConta) {
        String arquivo = TRANSACOES_DIR + numeroConta + ".dat";
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(arquivo))) {
            List<Transacao> transacoes = (List<Transacao>) ois.readObject();
            transacoesCache.put(numeroConta, transacoes);
        } catch (Exception e) {
            System.err.println("Erro ao carregar transações da conta " + numeroConta + ": " + e.getMessage());
        }
    }

    /**
     * Busca uma conta no cache
     */
    public ContaBancaria buscarConta(String numeroConta) {
        return contasCache.get(numeroConta);
    }

    /**
     * Busca transações de uma conta no cache
     */
    public List<Transacao> buscarTransacoes(String numeroConta) {
        return transacoesCache.getOrDefault(numeroConta, new ArrayList<>());
    }

    /**
     * Limpa os caches (útil para testes)
     */
    public void limparCaches() {
        contasCache.clear();
        transacoesCache.clear();
    }
}