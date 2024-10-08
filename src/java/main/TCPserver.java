
import Patterns.RWL.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Classe para lidar com conexões TCP
public class TCPserver{


    public TCPserver(int porta, List<Node> servidoresExternos) {

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                
            System.out.println("Servidor TCP esperando conexões na porta " + porta);
            
            while (true) {
            
                // Aceita uma conexão TCP
                Socket clientSocket = serverSocket.accept();

                executor.submit(()->{
                    System.out.println("Cliente TCP conectado: " + clientSocket.getInetAddress());

                    long threadName = Thread.currentThread().threadId();

                    try{
                        // Lê dados do cliente TCP
                        BufferedReader inCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter outCliente = new PrintWriter(clientSocket.getOutputStream(), true);

                        String receivedMessage = inCliente.readLine();

                        System.out.println("Thread: " + threadName + ", Mensagem do cliente: " + receivedMessage);

                        //encaminhando para servidores externos

                        String respServidores = "None";
                        
                        for (Node node : servidoresExternos) {

                            try {

                                Socket socket = new Socket(node.getAddress(), node.getPort());
                    
                                // Enviar mensagem ao servidor TCP
                                PrintWriter outServer = new PrintWriter(socket.getOutputStream(), true);
                                BufferedReader inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                                String message = receivedMessage;
                                outServer.println(message);
                                System.out.println("Mensagem enviada para o servidor TCP.");
                    
                                // Receber a resposta do servidor
                                String receivedMessageFromServer = inServer.readLine();
                                System.out.println("Resposta do servidor TCP: " + receivedMessageFromServer);

                                respServidores = receivedMessageFromServer;

                                socket.close();
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