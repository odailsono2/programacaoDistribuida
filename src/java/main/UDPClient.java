import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            // Cria um socket UDP para enviar e receber pacotes
            socket = new DatagramSocket();

            // Endereço do servidor (localhost no caso)
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 8080; // Porta que o servidor está escutando

            // Mensagem que o cliente deseja enviar
            String message = "criar;1";
            byte[] sendData = message.getBytes();

            // Cria um pacote UDP para enviar a mensagem ao servidor
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket); // Envia o pacote

            // Buffer para receber a resposta do servidor
            byte[] receiveData = new byte[1024];

            // Pacote UDP para receber a resposta
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket); // Aguarda a resposta do servidor

            // Converte a resposta em String e exibe
            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Resposta do servidor: " + serverResponse+" : porta "+ socket.getLocalPort());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fecha o socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
