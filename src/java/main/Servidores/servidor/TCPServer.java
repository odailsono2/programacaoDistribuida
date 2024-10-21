package servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.Connection;
import Patterns.RWL.ServerNode;

// Classe para lidar com conexões TCP
public class TCPServer {

    public TCPServer(int porta, List<ServerNode> servidoresExternos) {

        try (ServerSocket serverSocket = new ServerSocket(porta, 1, InetAddress.getByName("127.0.0.1"))) {

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            System.out.println("Servidor TCP esperando conexões na porta " + porta);

            while (true) {

                // Aceita uma conexão TCP
                Socket clientSocket = serverSocket.accept();

                if (clientSocket == null) {

                    throw new Exception("clientSocket is null");
                }

                executor.submit(() -> {
                    System.out
                            .println("(Gateway) Cliente TCP conectado: " + clientSocket.getInetAddress());

                    long threadName = Thread.currentThread().threadId();

                    String respServidores = "None";

                    String receivedMessage = null;

                    try {

                        receivedMessage = new String(Connection.receiveData(clientSocket));

                        System.out.println(
                                "(Gateway) Thread: " + threadName + ", Mensagem do cliente: " + receivedMessage);


                        for (ServerNode server : servidoresExternos) {
                            

                            String node_address = server.getAddress();
                            int node_porta = server.getPorta();

                            System.out.println("Comunicando com servidor: " + server.getNodeId());


                            try (var socketServidorLider = new Socket(node_address, node_porta);) {


                                String message = receivedMessage;

                                String receivedMessageFromServer = null;

                                Connection.send(message.getBytes(), socketServidorLider);

                                System.out.println(
                                        "Mensagem enviada para o servidor " + server.getNodeId());

                                receivedMessageFromServer = new String(Connection.receiveData(socketServidorLider));

                                System.out.println("Resposta do servidor: " + receivedMessageFromServer);

                                respServidores = receivedMessageFromServer;

                            } catch (IOException e) {
                                e.printStackTrace();
                                respServidores = e.getLocalizedMessage();

                            }

                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        respServidores = e.getLocalizedMessage();

                    } finally {
                        // Enviar uma resposta ao cliente TCP
                        try {
                            Connection.send(respServidores.getBytes(), clientSocket);

                            System.out.println("Resposta recebido do servidor: " + respServidores);
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

    public static void main(String[] args) {

        List<ServerNode> servidoresExternos = new ArrayList<>();

        ServerNode nodeLider = new ServerNode();
        nodeLider.setAddress("localhost");
        nodeLider.setPorta(8086);
        nodeLider.setNodeId("odailsonServer");
        nodeLider.setLider(false);

        servidoresExternos.add(nodeLider);


        ServerNode node= new ServerNode();
        node.setAddress("localhost");
        node.setPorta(8086);
        node.setNodeId("PandoraServer");
        node.setLider(true);

        servidoresExternos.add(node);

        int porta = 8085;

        if (args.length == 1) {
            porta = Integer.parseInt(args[0]);
        }

        new TCPServer(porta, servidoresExternos);
    }
}