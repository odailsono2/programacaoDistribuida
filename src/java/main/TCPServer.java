
import Patterns.RWL.Node;
import Patterns.RWL.TypeConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Classe para lidar com conexões TCP
public class TCPServer{


    public TCPServer(int porta, List<Node> servidoresExternos) {

        try (ServerSocket serverSocket = new ServerSocket(porta,1,InetAddress.getByName("127.0.0.1"))) {
            
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                
            System.out.println("Gateway TCP esperando conexões na porta " + porta);
            
            while (true) {
            
                // Aceita uma conexão TCP
                Socket clientSocket = serverSocket.accept();

                executor.submit(()->{
                    System.out.println("(Gateway) Cliente TCP conectado: " + clientSocket.getInetAddress());
                    

                    long threadName = Thread.currentThread().threadId();

                    try{
                        // Lê dados do cliente TCP
                        BufferedReader inCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter outCliente = new PrintWriter(clientSocket.getOutputStream(), true);

                        String receivedMessage = inCliente.readLine();

                        System.out.println("(Gateway) Thread: " + threadName + ", Mensagem do cliente: " + receivedMessage);

                        //encaminhando para servidores externos

                        String respServidores = "None";

                        var servidorlider = servidoresExternos.stream().filter((node)->node.getLider()).toList();
                        
                        for (Node node : servidorlider) {

                            
                            try {

                                node.setTypeConnection(TypeConnection.TCP);

                                node.makeSocket();


                                //Socket socket = new Socket(node.getAddress(), node.getPort());
                    
                                // Enviar mensagem ao servidor TCP
                               // PrintWriter outServer = new PrintWriter(socket.getOutputStream(), true);
                                //BufferedReader inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                                String message = receivedMessage;
                               // outServer.println(message);

                                node.sendMessage(message.getBytes());

                                System.out.println("Mensagem enviada para o servidor TCP.");

                                String receivedMessageFromServer = node.receiveMessage();
                    
                                // Receber a resposta do servidor
                                // StringBuilder construirMensagem = new StringBuilder();
                                
                                // inServer.lines().forEach((linha)->{
                                //     construirMensagem.append(linha).append(System.lineSeparator());
                                // });

                                //String receivedMessageFromServer = construirMensagem.toString();

                                System.out.println("Resposta do servidor TCP: " + receivedMessageFromServer);

                                respServidores = receivedMessageFromServer;

                                //socket.close();
                                node.closeSocket();

                                } catch (IOException e) {
                                    e.printStackTrace();
                            }


                        }

                        // Enviar uma resposta ao cliente TCP
                        outCliente.println(respServidores);
                        
                        clientSocket.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                    
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   

    public static void main(String[] args) {
        // var tcp = new TCPserver(8082);
    }
}