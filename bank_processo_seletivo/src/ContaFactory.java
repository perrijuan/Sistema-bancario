public class ContaFactory {
    //fabrica de contas que pode ser usada para criar contas
    //de acordo com os parametros passados podendo ser uma conta normal ou vip, interssante para a aplicacao
    //retorna uma nova conta
    public static ContaBancaria criarConta(String numeroConta, String tipo, String login, String senha) {
        // Validações
        if (numeroConta == null || numeroConta.length() != 5) {
            throw new IllegalArgumentException("Número da conta deve ter 5 dígitos!");
        }
        if (senha == null || senha.length() != 4) {
            throw new IllegalArgumentException("Senha deve ter 4 dígitos!");
        }
        if (tipo == null || (!tipo.equalsIgnoreCase("NORMAL") && !tipo.equalsIgnoreCase("VIP"))) {
            throw new IllegalArgumentException("Tipo de conta deve ser NORMAL ou VIP!");
        }

        return new ContaBancaria(numeroConta, tipo, login, senha);
    }
}