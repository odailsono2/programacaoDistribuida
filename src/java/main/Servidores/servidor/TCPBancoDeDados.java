package servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

import Patterns.RWL.Connection;
import Patterns.RWL.ServerNode;
import services.banco.Conta;

// Classe para lidar com conexões TCP
public class TCPBancoDeDados {

    private Map<String, Conta> contas;
    int porta;

    public TCPBancoDeDados(int porta) {
        this.porta = porta;
        contas = new ConcurrentHashMap<>();
    }

    public Map<String, Conta> getContas() {
        return contas;
    }

    public void inicializarBancodDeDados() {
        try (ServerSocket serverSocket = new ServerSocket(porta, 20, InetAddress.getByName("127.0.0.1"))) {

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            System.out.println("Banco de Dados incializado TCP esperando conexões na porta " + porta);

            while (true) {

                // Aceita uma conexão TCP
                Socket clientSocket = serverSocket.accept();

                if (clientSocket == null) {

                    throw new Exception("clientSocket is null");
                }

                executor.submit(() -> {
                    System.out
                            .println("(Banco de Dados) Cliente TCP conectado: " + clientSocket.getInetAddress());

                    long threadName = Thread.currentThread().threadId();


                    Conta conta = null;

                    try {

                        conta = Connection.receiveObject(Conta.class, clientSocket);

                        System.out.println(
                                "(Banco de Dados) Thread: " + threadName + ", Mensagem do cliente: " + conta);

                        contas.put(conta.getId(), conta);

                        System.out.println("Conta " + conta.getId() + " atualizada");

                        contas.values().forEach((itemConta) -> {
                            System.out.println("conta " + itemConta.getId() + " saldo: " + itemConta.getSaldo());
                        });
                

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    } finally {
                        // Enviar uma resposta ao cliente TCP
                        try {

                            clientSocket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        
        final int porta;

        if (args.length == 1) {
            porta = Integer.parseInt(args[0]);
        } else {
            throw new Exception("Inclua a porta para comnucação do banco de dados");
        }


        var servidorBancoDeDados = new TCPBancoDeDados(porta);
         servidorBancoDeDados.inicializarBancodDeDados();



        // List<ServerNode> servidoresExternos = new ArrayList<>();

        // ServerNode nodeLider = new ServerNode();
        // nodeLider.setAddress("localhost");
        // nodeLider.setPorta(8086);
        // nodeLider.setNodeId("odailsonServer");
        // nodeLider.setLider(false);

        // servidoresExternos.add(nodeLider);

        // ServerNode node = new ServerNode();
        // node.setAddress("localhost");
        // node.setPorta(8086);
        // node.setNodeId("PandoraServer");
        // node.setLider(true);

        // servidoresExternos.add(node);

        // new Thread(() -> {

        //     servidorBancoDeDados.inicializarBancodDeDados(servidoresExternos);

        // }).start();

        // new Thread(() -> {

        //     Conta conta1 = new Conta("1", 1000.0);
        //     Conta conta2 = new Conta("2", 1500.0);
        //     Conta conta3 = new Conta("3", 2000.0);

        //     System.out.println("TCP : Abrindo conexão cliente");

        //     try (Socket cliente = new Socket("localhost", porta)) {

        //         // cliente.connect(new InetSocketAddress("localhost", porta));
        //         // --- teste de envio de objeto

        //         System.out.println("TCP cliente: Envio do objeto: " + conta1);

        //         Connection.sendObject(conta1, cliente);

        //         var mensagemRecebida = Connection.receiveData(cliente);

        //         System.out.println("TCP- cliente - resposta servidor: " + mensagemRecebida);

        //     } catch (Exception e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }

        //     try (Socket cliente = new Socket("localhost", porta)) {

        //         // cliente.connect(new InetSocketAddress("localhost", porta));
        //         // --- teste de envio de objeto

        //         System.out.println("TCP cliente: Envio do objeto: " + conta2);

        //         Connection.sendObject(conta2, cliente);

        //         var mensagemRecebida = Connection.receiveData(cliente);

        //         System.out.println("TCP- cliente - resposta servidor: " + mensagemRecebida);

        //     } catch (Exception e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }

        //     try (Socket cliente = new Socket("localhost", porta)) {

        //         // cliente.connect(new InetSocketAddress("localhost", porta));
        //         // --- teste de envio de objeto

        //         System.out.println("TCP cliente: Envio do objeto: " + conta3);

        //         Connection.sendObject(conta3, cliente);

        //         var mensagemRecebida = Connection.receiveData(cliente);

        //         System.out.println("TCP- cliente - resposta servidor: " + mensagemRecebida);

        //     } catch (Exception e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        // }).start();


    }
}