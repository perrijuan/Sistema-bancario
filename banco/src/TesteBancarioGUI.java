import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

class TesteBancarioGUI {
    private BankGUI gui;
    private BankSingleton bank;
    private Robot robot;

    @BeforeEach
    void setUp() throws AWTException {
        // Inicializa o ambiente de teste
        bank = BankSingleton.getInstance();
        gui = new BankGUI();
        robot = new Robot();

        // Cria contas para teste
        bank.criarConta("11111", "NORMAL", "testUser1", "1234");
        bank.criarConta("22222", "VIP", "testUser2", "5678");

        gui.setVisible(true);
        robot.delay(500); // Aguarda interface carregar
    }

    @Test
    void testLoginInterface() {
        // Testa componentes da tela de login
        JTextField numeroContaField = findComponent(gui, JTextField.class, "numeroContaField");
        JPasswordField senhaField = findComponent(gui, JPasswordField.class, "senhaField");
        JButton loginButton = findComponent(gui, JButton.class, "Login");

        assertNotNull(numeroContaField, "Campo de número da conta deve existir");
        assertNotNull(senhaField, "Campo de senha deve existir");
        assertNotNull(loginButton, "Botão de login deve existir");
    }

    @Test
    void testLoginSucesso() {
        // Simula login com sucesso
        JTextField numeroContaField = findComponent(gui, JTextField.class, "numeroContaField");
        JPasswordField senhaField = findComponent(gui, JPasswordField.class, "senhaField");
        JButton loginButton = findComponent(gui, JButton.class, "Login");

        SwingUtilities.invokeLater(() -> {
            numeroContaField.setText("11111");
            senhaField.setText("1234");
            loginButton.doClick();
        });

        robot.delay(1000);

        // Verifica se painel de operações está visível
        JPanel operacoesPanel = findComponent(gui, JPanel.class, "OPERATIONS");
        assertTrue(operacoesPanel.isVisible(), "Painel de operações deve estar visível após login");
    }

    @Test
    void testDepositoInterface() {
        // Realiza login primeiro
        fazerLogin("11111", "1234");

        // Testa interface de depósito
        JButton depositoBtn = findComponent(gui, JButton.class, "Depósito");
        assertNotNull(depositoBtn, "Botão de depósito deve existir");

        SwingUtilities.invokeLater(() -> depositoBtn.doClick());
        robot.delay(500);

        // Verifica se diálogo de depósito aparece
        Window[] windows = Window.getWindows();
        boolean dialogoEncontrado = false;
        for (Window window : windows) {
            if (window instanceof JDialog && window.isVisible()) {
                dialogoEncontrado = true;
                break;
            }
        }
        assertTrue(dialogoEncontrado, "Diálogo de depósito deve ser exibido");
    }

    @Test
    void testSaqueInterface() {
        fazerLogin("11111", "1234");

        JButton saqueBtn = findComponent(gui, JButton.class, "Saque");
        assertNotNull(saqueBtn, "Botão de saque deve existir");

        float saldoInicial = bank.getContaAtual().getSaldo();

        SwingUtilities.invokeLater(() -> {
            saqueBtn.doClick();
            // Simula entrada de valor no diálogo
            JOptionPane.getRootFrame().setVisible(false);
        });

        robot.delay(500);
        assertEquals(saldoInicial, bank.getContaAtual().getSaldo(),
                "Saldo deve permanecer igual se operação for cancelada");
    }

    @Test
    void testLogoutFuncionalidade() {
        fazerLogin("11111", "1234");

        JButton logoutBtn = findComponent(gui, JButton.class, "Logout");
        SwingUtilities.invokeLater(() -> logoutBtn.doClick());

        robot.delay(500);

        // Verifica se voltou para tela de login
        JPanel loginPanel = findComponent(gui, JPanel.class, "LOGIN");
        assertTrue(loginPanel.isVisible(), "Deve voltar para tela de login após logout");
    }

    // Métodos auxiliares
    private void fazerLogin(String conta, String senha) {
        SwingUtilities.invokeLater(() -> {
            JTextField numeroContaField = findComponent(gui, JTextField.class, "numeroContaField");
            JPasswordField senhaField = findComponent(gui, JPasswordField.class, "senhaField");
            JButton loginButton = findComponent(gui, JButton.class, "Login");

            numeroContaField.setText(conta);
            senhaField.setText(senha);
            loginButton.doClick();
        });
        robot.delay(500);
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponent(Container container, Class<T> classe, String name) {
        for (Component comp : container.getComponents()) {
            if (classe.isInstance(comp) && (name == null || name.equals(comp.getName()))) {
                return (T) comp;
            }
            if (comp instanceof Container) {
                T found = findComponent((Container) comp, classe, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    @AfterEach
    void tearDown() {
        if (gui != null) {
            gui.dispose();
        }
    }
}