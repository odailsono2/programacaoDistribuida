package testes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Classe para lidar com conexões TCP
public class TCPserverOld{
    private int port;

    public TCPserverOld(int port) {

        this.port = port;
    

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                
            System.out.println("Servidor TCP esperando conexões na porta " + port);
            
            while (true) {
            
                // Aceita uma conexão TCP
                Socket clientSocket = serverSocket.accept();

                executor.submit(()->{
                    System.out.println("Cliente TCP conectado: " + clientSocket.getInetAddress());

                    try{
                        // Lê dados do cliente TCP
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                        String receivedMessage = in.readLine();
                        System.out.println("Recebido do cliente TCP: " + receivedMessage);

                        // Enviar uma resposta ao cliente TCP
                        out.println("Mensagem recebida via TCP: " + receivedMessage);
                        
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
        var tcp = new TCPserverOld(8082);
    }
}