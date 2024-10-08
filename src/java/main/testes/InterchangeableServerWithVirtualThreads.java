package testes;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;

public class InterchangeableServerWithVirtualThreads {

    public static void main(String[] args) throws IOException {
        int port = 9876;

        // Executor que usa virtual threads
        var virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Criar threads virtuais para TCP e UDP
        virtualThreadExecutor.execute(() -> startTCPServer(port));
        virtualThreadExecutor.execute(() -> startUDPServer(port));

        virtualThreadExecutor.shutdown(); // Fecha o executor quando terminar
    }

    // Iniciar o servidor TCP com Virtual Threads
    public static void startTCPServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor TCP esperando conexões na porta " + port);

            while (true) {
                // Aceitar uma conexão TCP
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente TCP conectado: " + clientSocket.getInetAddress());

                // Para cada conexão TCP, usar uma Virtual Thread
                Thread.ofVirtual().start(() -> handleTCPConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para lidar com a conexão TCP em uma Virtual Thread
    public static void handleTCPConnection(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String receivedMessage = in.readLine();
            System.out.println("Recebido do cliente TCP: " + receivedMessage);

            // Enviar uma resposta ao cliente TCP
            out.println("Mensagem recebida via TCP: " + receivedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Iniciar o servidor UDP com Virtual Threads
    public static void startUDPServer(int port) {
        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {
            System.out.println("Servidor UDP esperando pacotes na porta " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                datagramSocket.receive(receivePacket);

                // Para cada pacote UDP, usar uma Virtual Thread
                Thread.ofVirtual().start(() -> handleUDPPacket(datagramSocket, receivePacket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para lidar com o pacote UDP em uma Virtual Thread
    public static void handleUDPPacket(DatagramSocket datagramSocket, DatagramPacket receivePacket) {
        try {
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Recebido do cliente UDP: " + receivedMessage);

            // Enviar resposta de volta ao cliente UDP
            String responseMessage = "Mensagem recebida via UDP: " + receivedMessage;
            byte[] responseBuffer = responseMessage.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length,
                    receivePacket.getAddress(), receivePacket.getPort());

            datagramSocket.send(responsePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
