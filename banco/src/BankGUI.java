import javax.swing.*;
import java.awt.*;

public class BankGUI extends JFrame {
    private final BankSingleton bank;
    private final JTextField numeroContaField;
    private final JPasswordField senhaField;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private final JLabel saldoLabel;

    public BankGUI() {
        // Inicialização dos componentes
        bank = BankSingleton.getInstance();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        numeroContaField = new JTextField(15);
        senhaField = new JPasswordField(15);
        saldoLabel = new JLabel("Saldo: R$ 0.00");

        // Configuração da janela
        configurarJanela();

        // Criação dos painéis
        mainPanel.add(criarPainelLogin(), "LOGIN");
        mainPanel.add(criarPainelOperacoes(), "OPERATIONS");

        // Exibir painel de login
        cardLayout.show(mainPanel, "LOGIN");
    }

    private void configurarJanela() {
        setTitle("Banco Digital");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        add(mainPanel);
    }

    private JPanel criarPainelLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Configuração dos componentes
        numeroContaField.setName("numeroContaField");
        senhaField.setName("senhaField");
        JButton loginButton = new JButton("Login");
        loginButton.setName("loginButton");

        // Adiciona componentes
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Número da Conta:"), gbc);

        gbc.gridx = 1;
        loginPanel.add(numeroContaField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        loginPanel.add(senhaField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> realizarLogin());

        return loginPanel;
    }

    private JPanel criarPainelOperacoes() {
        JPanel operacoesPanel = new JPanel(new BorderLayout());

        // Painel superior com saldo
        JPanel saldoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saldoPanel.add(saldoLabel);
        operacoesPanel.add(saldoPanel, BorderLayout.NORTH);

        // Painel central com botões
        JPanel botoesPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        botoesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Criação dos botões
        adicionarBotaoOperacao(botoesPanel, "Depósito", this::realizarDeposito);
        adicionarBotaoOperacao(botoesPanel, "Saque", this::realizarSaque);
        adicionarBotaoOperacao(botoesPanel, "Transferência", this::realizarTransferencia);
        adicionarBotaoOperacao(botoesPanel, "Extrato", this::mostrarExtrato);
        adicionarBotaoOperacao(botoesPanel, "Ver Saldo", this::atualizarSaldoExibido);
        adicionarBotaoOperacao(botoesPanel, "Logout", this::realizarLogout);

        operacoesPanel.add(botoesPanel, BorderLayout.CENTER);
        return operacoesPanel;
    }

    private void adicionarBotaoOperacao(JPanel panel, String texto, Runnable acao) {
        JButton botao = new JButton(texto);
        botao.setName(texto);
        botao.addActionListener(e -> acao.run());
        panel.add(botao);
    }

    private void realizarLogin() {
        try {
            String numeroConta = numeroContaField.getText();
            String senha = new String(senhaField.getPassword());

            if (bank.login(numeroConta, senha)) {
                atualizarSaldoExibido();
                cardLayout.show(mainPanel, "OPERATIONS");
                limparCamposLogin();
            } else {
                mostrarErro("Credenciais inválidas!");
            }
        } catch (Exception e) {
            mostrarErro("Erro ao realizar login: " + e.getMessage());
        }
    }

    private void realizarDeposito() {
        try {
            String valor = JOptionPane.showInputDialog(this,
                    "Digite o valor do depósito:",
                    "Depósito",
                    JOptionPane.PLAIN_MESSAGE);

            if (valor != null && !valor.isEmpty()) {
                float quantia = Float.parseFloat(valor);
                bank.deposito(quantia);
                atualizarSaldoExibido();
                mostrarSucesso("Depósito realizado com sucesso!");
            }
        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido!");
        } catch (Exception e) {
            mostrarErro("Erro ao realizar depósito: " + e.getMessage());
        }
    }

    private void realizarSaque() {
        try {
            String valor = JOptionPane.showInputDialog(this,
                    "Digite o valor do saque:",
                    "Saque",
                    JOptionPane.PLAIN_MESSAGE);

            if (valor != null && !valor.isEmpty()) {
                float quantia = Float.parseFloat(valor);
                bank.saque(quantia);
                atualizarSaldoExibido();
                mostrarSucesso("Saque realizado com sucesso!");
            }
        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido!");
        } catch (Exception e) {
            mostrarErro("Erro ao realizar saque: " + e.getMessage());
        }
    }

    private void realizarTransferencia() {
        JTextField contaDestinoField = new JTextField();
        JTextField valorField = new JTextField();
        Object[] message = {
                "Conta destino:", contaDestinoField,
                "Valor:", valorField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                "Transferência", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String contaDestino = contaDestinoField.getText();
                float valor = Float.parseFloat(valorField.getText());
                bank.transferencia(contaDestino, valor);
                atualizarSaldoExibido();
                mostrarSucesso("Transferência realizada com sucesso!");
            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido!");
            } catch (Exception e) {
                mostrarErro("Erro na transferência: " + e.getMessage());
            }
        }
    }

    private void mostrarExtrato() {
        try {
            ExtratoObserver extrato = bank.getExtratoObserver();
            StringBuilder extratoText = new StringBuilder();
            extratoText.append("=== EXTRATO ===\n\n");

            // Aqui você deve implementar a lógica para obter as transações do extrato
            // e adicionar ao StringBuilder

            JTextArea textArea = new JTextArea(extratoText.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 200));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Extrato", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            mostrarErro("Erro ao exibir extrato: " + e.getMessage());
        }
    }

    private void atualizarSaldoExibido() {
        try {
            float saldo = bank.getContaAtual().getSaldo();
            saldoLabel.setText(String.format("Saldo: R$ %.2f", saldo));
        } catch (Exception e) {
            mostrarErro("Erro ao atualizar saldo: " + e.getMessage());
        }
    }

    private void realizarLogout() {
        try {
            bank.logout();
            limparCamposLogin();
            cardLayout.show(mainPanel, "LOGIN");
            mostrarSucesso("Logout realizado com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao realizar logout: " + e.getMessage());
        }
    }

    private void limparCamposLogin() {
        numeroContaField.setText("");
        senhaField.setText("");
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this,
                mensagem,
                "Erro",
                JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this,
                mensagem,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new BankGUI().setVisible(true);
        });
    }
}