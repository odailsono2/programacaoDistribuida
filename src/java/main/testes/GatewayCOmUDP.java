package testes;
import Patterns.RWL.Node;
import Patterns.RWL.Nodemaker;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GatewayCOmUDP {

    DatagramSocket serverSocket = null;

    ServerSocket serverTCP = null;

    String TCP = "";

    private final int porta;
    
    List<Node> servidores;

    public GatewayCOmUDP(int porta, List<Node> servidores) {
        this.porta = porta;
        this.servidores = servidores;
    }

    public void inicializar() {
        

        System.out.println("Gateway iniciado na porta:" + porta);

        try {

            if (TCP.contentEquals("TCP")) {
                serverTCP = new ServerSocket(porta);
                Socket nextClient = serverTCP.accept();
                ObjectInputStream input = new ObjectInputStream(nextClient.getInputStream());
                try {
                    String msg = (String) input.readObject();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // criarContasTeste();

            serverSocket = new DatagramSocket(porta);

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            while (true) {

                byte[] receiveMessage = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

                serverSocket.receive(receivePacket);

                executor.submit(() -> {

                    long threadName = Thread.currentThread().threadId();

                    System.out.println(
                            "Thread: " + threadName + ", Mensagem do cliente: " + new String(receivePacket.getData()));

                    for (Node node : servidores) {

                        try (DatagramSocket socket = new DatagramSocket()) {

                            InetAddress serverAddress = node.getAddress();

                            int serverPort = node.getPort();

                            byte[] sendData = receivePacket.getData();

                            System.out.println(
                                    "Contatando Servidor:" + serverAddress.getHostAddress() + ":" + serverPort +
                                            "...");

                            // Cria um pacote UDP para enviar a mensagem ao servidor
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress,
                                    serverPort);

                            socket.send(sendPacket);

                            // Buffer para receber a resposta do servidor

                            byte[] receiveData = new byte[1024];

                            // Pacote UDP para receber a resposta do servidor
                            DatagramPacket reponsePacket = new DatagramPacket(receiveData, receiveData.length);
                            socket.receive(reponsePacket); // Aguarda a resposta do servidor

                            System.out.println(
                                    "Resposta servidor: " + node.getIdservidor() + ", dados:"
                                            + new String(reponsePacket.getData()));

                            // Cria um pacote UDP para enviar a mensagem ao cliente
                            var sendDataCliente = reponsePacket.getData();
                            DatagramPacket sendPacketCliente = new DatagramPacket(sendDataCliente,
                                    sendDataCliente.length, receivePacket.getAddress(), receivePacket.getPort());

                            serverSocket.send(sendPacketCliente); // Envia o pacote

                            System.out
                                    .println("Address: " + receivePacket.getAddress() + ":" + receivePacket.getPort());

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("UDP Server Terminating");

        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                System.out.println("Fechando o socket do servidor...");
                serverSocket.close();
            }
        }

    }

    public static void main(String[] args) {

        Node servedor1 = (new Nodemaker()).setAddress("localhost").setPort(8081).buideNode();

        // Node servedor2 = (new
        // Nodemaker()).setAddress("localhost").setPort(8082).buideNode();

        // Node servedor3 = (new
        // Nodemaker()).setAddress("localhost").setPort(8083).buideNode();

        List<Node> servidoresDisponiveis = new ArrayList<>();

        servidoresDisponiveis.add(servedor1);

        // servidoresDisponiveis.add(servedor2);

        // servidoresDisponiveis.add(servedor3);

        var gateway = new GatewayCOmUDP(8080, servidoresDisponiveis);

        gateway.inicializar();

        return;
    }
}
