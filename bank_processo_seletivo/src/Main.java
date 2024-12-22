import javax.swing.*;
import java.util.Scanner;

/**
 * Sistema Bancário - Main Class
 * Suporta modo Console (estável) e GUI (beta)
 *
 * @version 1.0.0
 * @since 2024-12-22
 * @author perrijuan
 */
public class Main {
    // Constantes do sistema
    private static final String VERSION = "1.0.0";
    private static final String CURRENT_DATE = "2024-12-22 18:23:58";
    private static final String CURRENT_USER = "perrijuan";

    // Componentes principais
    private static final Scanner scanner = new Scanner(System.in);
    private static final BankSingleton bank = BankSingleton.getInstance();

    public static void main(String[] args) {
        showWelcomeMessage();

        try {
            int mode = selectOperationMode();
            if (mode == 1) {
                startGUIMode();
            } else {
                startConsoleMode();
            }
        } catch (Exception e) {
            System.err.println("Erro crítico: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Exibe mensagem de boas-vindas com informações do sistema
     */
    private static void showWelcomeMessage() {
        System.out.println("=========================================");
        System.out.println("          BANCO DIGITAL v" + VERSION);
        System.out.println("=========================================");
        System.out.println("Data/Hora (UTC): " + CURRENT_DATE);
        System.out.println("Usuário: " + CURRENT_USER);
        System.out.println("=========================================");
    }

    /**
     * Permite ao usuário selecionar o modo de operação
     */
    private static int selectOperationMode() {
        while (true) {
            System.out.println("\nEscolha o modo de operação:");
            System.out.println("1. Interface Gráfica (Beta - Com avisos)");
            System.out.println("2. Console (Recomendado)");
            System.out.print("\nOpção: ");

            try {
                int option = Integer.parseInt(scanner.nextLine().trim());
                if (option == 1 || option == 2) {
                    if (option == 1) {
                        showGUIWarnings();
                    }
                    return option;
                }
            } catch (NumberFormatException e) {
                // Ignora entrada inválida
            }
            System.out.println("Opção inválida! Tente novamente.");
        }
    }

    /**
     * Exibe avisos sobre o modo GUI
     */
    private static void showGUIWarnings() {
        System.out.println("\n⚠️ AVISO: Interface Gráfica (Beta)");
        System.out.println("Este modo pode apresentar:");
        System.out.println("- Problemas de sincronização");
        System.out.println("- Consumo elevado de memória");
        System.out.println("- Falhas na interface");
        System.out.println("- Inconsistências nas operações");
        System.out.println("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }

    /**
     * Inicia o modo GUI (Interface Gráfica)
     */
    private static void startGUIMode() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(() -> {
                try {
                    new BankGUI().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Erro na interface gráfica: " + e.getMessage());
                    System.out.println("Iniciando modo console como fallback...");
                    startConsoleMode();
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao iniciar modo gráfico: " + e.getMessage());
            startConsoleMode();
        }
    }

    /**
     * Inicia o modo Console
     */
    private static void startConsoleMode() {
        createDefaultAccounts();
        runConsoleLoop();
    }

    /**
     * Cria contas padrão para teste
     */
    private static void createDefaultAccounts() {
        try {
            bank.criarConta("12345", "NORMAL", "usuario1", "1234");
            bank.criarConta("67890", "VIP", "usuario2", "5678");
        } catch (Exception e) {
            // Ignora se as contas já existem
        }
    }

    /**
     * Loop principal do modo console
     */
    private static void runConsoleLoop() {
        while (true) {
            if (performLogin()) {
                boolean keepRunning = true;
                while (keepRunning) {
                    showMenu();
                    int option = readOption();
                    keepRunning = processOption(option);
                }
            }
        }
    }

    /**
     * Realiza o login do usuário
     */
    private static boolean performLogin() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Número da Conta (5 dígitos): ");
        String account = scanner.nextLine();
        System.out.print("Senha (4 dígitos): ");
        String password = scanner.nextLine();

        if (bank.login(account, password)) {
            System.out.println("Login realizado com sucesso!");
            return true;
        } else {
            System.out.println("Credenciais inválidas!");
            return false;
        }
    }

    /**
     * Exibe o menu principal
     */
    private static void showMenu() {
        ContaBancaria account = bank.getContaAtual();
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.printf("Conta: %s (%s)%n",
                account.getNumeroConta(), account.getTipoUsuario());
        System.out.println("1. Consultar Saldo");
        System.out.println("2. Extrato");
        System.out.println("3. Saque");
        System.out.println("4. Depósito");
        System.out.println("5. Transferência");
        if (account.isVip()) {
            System.out.println("6. Solicitar Gerente");
        }
        System.out.println("7. Trocar Usuário");
        System.out.println("0. Sair");
    }

    /**
     * Lê a opção do usuário
     */
    private static int readOption() {
        System.out.print("\nEscolha uma opção: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Processa a opção escolhida
     */
    private static boolean processOption(int option) {
        try {
            return switch (option) {
                case 0 -> {
                    System.out.println("Encerrando sistema...");
                    System.exit(0);
                    yield false;
                }
                case 1 -> { showBalance(); yield true; }
                case 2 -> { showStatement(); yield true; }
                case 3 -> { performWithdrawal(); yield true; }
                case 4 -> { performDeposit(); yield true; }
                case 5 -> { performTransfer(); yield true; }
                case 6 -> { requestManager(); yield true; }
                case 7 -> {
                    bank.logout();
                    yield false;
                }
                default -> {
                    System.out.println("Opção inválida!");
                    yield true;
                }
            };
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return true;
        }
    }

    /**
     * Métodos para operações bancárias
     */
    private static void showBalance() {
        System.out.printf("%nSaldo atual: R$ %.2f%n",
                bank.getContaAtual().getSaldo());
    }

    private static void showStatement() {
        bank.getExtratoObserver().imprimirExtrato();
    }

    private static void performWithdrawal() {
        System.out.print("Valor para saque: R$ ");
        try {
            float amount = Float.parseFloat(scanner.nextLine().trim());
            bank.saque(amount);
            System.out.println("Saque realizado com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido!");
        }
    }

    private static void performDeposit() {
        System.out.print("Valor para depósito: R$ ");
        try {
            float amount = Float.parseFloat(scanner.nextLine().trim());
            bank.deposito(amount);
            System.out.println("Depósito realizado com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido!");
        }
    }

    private static void performTransfer() {
        try {
            System.out.print("Conta destino: ");
            String targetAccount = scanner.nextLine();
            System.out.print("Valor: R$ ");
            float amount = Float.parseFloat(scanner.nextLine().trim());

            bank.transferencia(targetAccount, amount);
            System.out.println("Transferência realizada com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido!");
        }
    }

    private static void requestManager() {
        if (!bank.getContaAtual().isVip()) {
            System.out.println("Apenas contas VIP podem solicitar gerente!");
            return;
        }

        System.out.println("Confirma solicitação do gerente? (S/N)");
        System.out.println("Taxa: R$ 50,00");

        if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
            try {
                bank.solicitarGerente();
                System.out.println("Gerente solicitado com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao solicitar gerente: " + e.getMessage());
            }
        }
    }
}