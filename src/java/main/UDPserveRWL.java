import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class UDPserveRWL {

	String reply= "";
	
	List<ClientConnection> conexoes = new ArrayList<>();

	RequestWaitingList<Integer, RequestOrResponse> requestWaitingList = new RequestWaitingList<>();

	List<Node> clustersNodes = new ArrayList<>();

	private Integer correlationID = 0;
	private Integer requestID = 0;

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
				StringRequest requestComID = new StringRequest(requestID, receivePacket.getData());
				Client clientConnection = new Client(receivePacket);
				upRequestID();



				System.out.println("Received from client: [" + message+ "]\nFrom: " + receivePacket.getAddress().toString()+":"+receivePacket.getPort());
				


				executor.submit(()->{

					long threadName = Thread.currentThread().threadId();
					// Exibe a mensagem recebida e a thread que está processando
					System.out.println("Thread atual: " + threadName + " - Mensagem recebida de " + conexoes.get(0).getAddress() + ":" + conexoes.get(0).getPort() + " - " + message);

			
					clustersNodes.forEach(
						(node)->{
			
							int correlationID = nextCorrelationID();

							RequestOrResponse requestComCorrelationID = new RequestOrResponse(requestComID, correlationID);

							RequestCallback<RequestOrResponse> writeQuorumCallback = new WriteQuorumCallback(clustersNodes.size(), 
																												requestComCorrelationID, 
																												clientConnection);

							requestWaitingList.add(correlationID, writeQuorumCallback);
							
							// Cria um socket UDP para enviar e receber pacotes
							DatagramSocket socket = new DatagramSocket();

							// Endereço do servidor (localhost no caso)
							InetAddress serverAddress = node.getAddress();
							int serverPort = node.getPort(); // Porta que o servidor está escutando


							// Cria um pacote UDP para enviar a mensagem ao servidor
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
							socket.send(sendPacket); // Envia o pacote
							

						}
					);
				
			});	
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");	
				
		} 

	}

	public Integer nextCorrelationID(){
		return correlationID++;
	}

	public Integer upRequestID(){
		return requestID ++ ; 
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
