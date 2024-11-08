package services.banco;

import java.util.HashMap;

import java.net.Socket;

import Patterns.RWL.Connection;


public class Banco {

    private HashMap<String, Conta> contas = new HashMap<>();

    public String mensagemSaida = "";

    public HashMap<String, Conta> getContas() {
        return contas;
    }

    public void getSaldo(String id) throws Exception {

        contaValida(id);
        mensagemSaida = "Sucesso: Conta " + id + " saldo: " + contas.get(id).toString();
    }

    public void criar(String id) throws Exception {

        Contabuilder criadorContas = new Contabuilder();

        if (contas.containsKey(id)) {
            throw new Exception("Erro ao Criar Conta: a conta " + id + " já existe");
        }

        Conta novaConta = criadorContas
                .setId(id)
                .setSaldo(0)
                .criarConta();

        contas.put(id, novaConta);

        mensagemSaida = "Sucesso: Conta " + id + " criada!";

        salavaContaBD(novaConta, 9002);

    }

    public void transferir(String idContaOrigem, String idContaDestino, double valor) throws Exception {

        try {
            testaValorPositivo(valor);
            contaValida(idContaOrigem);
            contaValida(idContaDestino);
            testaSaldoSuficiente(idContaOrigem, valor);
            contas.get(idContaOrigem).sacar(valor);
            contas.get(idContaDestino).depositar(valor);
            mensagemSaida = "Sucesso: Transferência Realizada da Conta " + idContaOrigem + " para Conta " + idContaDestino
                    + ", Valor: " + valor;

            salavaContaBD(contas.get(idContaOrigem), 9002);
            salavaContaBD(contas.get(idContaDestino), 9002);

            

        } catch (Exception e) {
            throw new Exception("Erro de Transferencia: " + e.getMessage());
        }

    }

    public void depositar(String id, double valor) throws Exception {
        try {
            testaValorPositivo(valor);
            contaValida(id);
            contas.get(id).depositar(valor);
            mensagemSaida = "Sucesso: Deposito Realizado na Conta " + id + ", Saldo: " + contas.get(id).getSaldo();

            salavaContaBD(contas.get(id), 9002);

        } catch (Exception e) {
            throw new Exception("Erro de Deposito: " + e.getMessage());
        }
    }

    public void executarOperacao(String[] operacoBancaria) throws Exception {

        switch (operacoBancaria[0]) {

            case "criar":

                criar(operacoBancaria[1]);

                break;

            case "depositar":

                double valor = Double.parseDouble(operacoBancaria[2]);

                depositar(operacoBancaria[1], valor);

                break;

            case "transferir":

                valor = Double.parseDouble(operacoBancaria[3]);

                transferir(operacoBancaria[1], operacoBancaria[2], valor);

                break;
            case "saldo":

                getSaldo(operacoBancaria[1]);

                break;

            default:

                throw new Exception("Erro: comando inválido");
            // break;
        }


    }

    public void salavaContaBD(Conta conta, int porta){
        try (Socket cliente = new Socket("localhost", porta)) {

            // cliente.connect(new InetSocketAddress("localhost", porta));
            // --- teste de envio de objeto

            // System.out.println("TCP cliente: Envio do objeto: " + conta);

            Connection.sendObject(conta, cliente);

            // var mensagemRecebida = Connection.receiveData(cliente);

            // System.out.println("TCP- cliente - resposta servidor: " + mensagemRecebida);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void contaValida(String id) throws Exception {
        if (contas.get(id) != null) {
            return;
        } else {
            throw new Exception("Conta Inexistente");
        }
    }

    public void testaValorPositivo(double valor) throws Exception {
        if (valor > 0) {
            return;
        } else {
            throw new Exception("Valor deve ser maior que zero!");
        }
    }

    public void testaSaldoSuficiente(String id, double valor) throws Exception {
        if (contas.get(id).getSaldo() >= valor) {
            return;
        } else {
            throw new Exception("Saldo Insuficiente!");
        }
    }

    public String processarMensagemSaida(String mensagem) {
        return "";
    }

    public static void main(String[] args) {
        Banco banco = new Banco();

        // banco.criar("1");
        // banco.criar("2");
        // banco.criar("3");

        // String[] operacoBancaria =
        // Protocolo.getProtocolo().processarMensagem("criar;1");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria = Protocolo.getProtocolo().processarMensagem("criar;2");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria = Protocolo.getProtocolo().processarMensagem("criar;3");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria =
        // Protocolo.getProtocolo().processarMensagem("depositar;1;550");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria =
        // Protocolo.getProtocolo().processarMensagem("depositar;2;1550");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria =
        // Protocolo.getProtocolo().processarMensagem("depositar;3;150");
        // banco.executarOperacao(operacoBancaria);

        // operacoBancaria =
        // Protocolo.getProtocolo().processarMensagem("transferir;1;2;75.5");
        // banco.executarOperacao(operacoBancaria);

        // try {
        // banco.depositar("2", 400);
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // try {
        // banco.transferir("2","1", 125.55);
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // try {
        // banco.transferir("1","3", 25);
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        banco.contas.values().stream().forEach(System.out::println);
    }

}
