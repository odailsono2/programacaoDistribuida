package servidor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.Connection;
import Patterns.RWL.ServerNode;

public class UDPserver {

    Map<String, ServerNode> listaServidores = new ConcurrentHashMap<>();

    public UDPserver(int porta, List<ServerNode> servidoresExternos) {

        System.out.println("Servidor UDP esperando conexões na porta " + porta);

        try (var serverSocket = new DatagramSocket(porta);) {

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            while (true) {

                // byte[] receiveMessage = new byte[1024];

                // DatagramPacket receivePacket = new DatagramPacket(receiveMessage,
                // receiveMessage.length);

                // serverSocket.receive(receivePacket);

                var receivePacket = Connection.receiveUDP(serverSocket);

                var addressCliente = receivePacket.getAddress().getHostAddress();

                int portaCliente = receivePacket.getPort();

                var mensagemCliente = receivePacket.getData();

                System.out.println("(Servidor) Cliente UDP conectado: " + addressCliente);


                executor.submit(() -> {

                    long threadName = Thread.currentThread().threadId();

                    System.out.println(
                            "Thread: " + threadName + ", Mensagem do cliente: " + new String(mensagemCliente));

                    for (ServerNode node : servidoresExternos) {

                        try (DatagramSocket socket = new DatagramSocket()) {

                            String serverAddress = node.getAddress();

                            int serverPorta = node.getPorta();

                            byte[] sendData = mensagemCliente;

                            System.out.println(
                                    "Contatando Servidor: " + node.getNodeId());

                            // Cria um pacote UDP para enviar a mensagem ao servidor

                            Connection.sendUDP(sendData, socket, serverAddress, serverPorta);

                            // Buffer para receber a resposta do servidor

                            var reponsePacket = Connection.receiveUDP(socket);

                            // Cria um pacote UDP para enviar a mensagem ao cliente
                            var sendDataCliente = reponsePacket.getData();

                            Connection.sendUDP(sendDataCliente, serverSocket, addressCliente, portaCliente);

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

        }
    }

    public static void main(String[] args) {
        List<ServerNode> servidoresExternos = new ArrayList<>();

        ServerNode nodeLider = new ServerNode();
        nodeLider.setAddress("localhost");
        nodeLider.setPorta(8086);
        nodeLider.setNodeId("odailsonServer");
        nodeLider.setLider(false);

        servidoresExternos.add(nodeLider);

        ServerNode node = new ServerNode();
        node.setAddress("localhost");
        node.setPorta(8086);
        node.setNodeId("PandoraServer");
        node.setLider(true);

        servidoresExternos.add(node);

        new UDPserver(8080, servidoresExternos);
    }

}
