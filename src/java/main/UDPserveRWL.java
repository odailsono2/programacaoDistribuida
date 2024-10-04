import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

import Protocolos.MeuProtocolo.Protocolo;

public class UDPserveRWL {
	String reply= "";
	// DatagramSocket serverSocket = null;
	List<ClientConnection> conexoes = new ArrayList<>();
	
	public UDPserveRWL(int porta) throws Exception{
	

		System.out.println("Servidor UDP inicializado ...");
		System.out.println("Ouvindo na porta: "+porta);

		try (DatagramSocket serverSocket = new DatagramSocket(porta);
		     ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();){

			//criarContasTeste();

			
			while (true) {

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);
				
				String message = new String(receivePacket.getData());

				System.out.println("Received from client: [" + message+ "]\nFrom: " + receivePacket.getAddress().toString()+":"+receivePacket.getPort());
				

				String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(message);

				executor.submit(()->{

					long threadName = Thread.currentThread().threadId();
					// Exibe a mensagem recebida e a thread que está processando
					System.out.println("Thread atual: " + threadName + " - Mensagem recebida de " + conexoes.get(0).getAddress() + ":" + conexoes.get(0).getPort() + " - " + message);

			
					//banco.getContas().values().stream().forEach(System.out::println); 

					byte[] replymsg = reply.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(replymsg,replymsg.length,receivePacket.getAddress(),receivePacket.getPort());
					
				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});	
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");	
				
		} 

	}


	public static void main(String[] args) { 

			try {
				new UDPserveRWL(8080);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
			
		}
}
