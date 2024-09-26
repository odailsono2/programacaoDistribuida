import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
	
	public UDPServer(int porta) {
		System.out.println("UDP Server Started...");
		System.out.println("Ouvindo na porta:"+porta);
		try {
			DatagramSocket serverSocket = new DatagramSocket(porta);
			while (true) {
				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				System.out.println("Received from client: [" + message+ "]\nFrom: " + receivePacket.getAddress());
				
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");		
		}
	}
	public static void main(String[] args) { 
			new UDPServer(8080);    
		}
}
