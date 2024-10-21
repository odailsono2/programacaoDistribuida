package testes;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.Client;
import Patterns.RWL.ClientConnection;
import Patterns.RWL.Node;
import Patterns.RWL.RequestCallback;
import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.RequestWaitingList;
import Patterns.RWL.StringRequest;
import Patterns.RWL.WriteQuorumCallback;

import java.util.ArrayList;
import java.util.List;

public class UDPserveRWL {

	String reply= "";
	
	List<ClientConnection> conexoes = new ArrayList<>();

	RequestWaitingList<Integer, RequestOrResponse> requestWaitingList = new RequestWaitingList<>();

	List<Node> clustersNodes = new ArrayList<>();

	private Integer correlationID = 0;
	private volatile Integer requestID = 0;

	public UDPserveRWL(int porta) throws Exception{
	

		System.out.println("Servidor Handler inicializado ...");
		System.out.println("Ouvindo na porta: "+porta);

		try (DatagramSocket serverSocket = new DatagramSocket(porta);
		     ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();){

			//criarContasTeste();

			
			while (true) {

				//Atualiza o codigo ID para uso no proximo request do cliente
				incrementaRequestID();

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);
				

				//Encapsula Codigo Id e o request do cliente em StringRequest
				
				String message = new String(receivePacket.getData());
				StringRequest requestComID = new StringRequest(requestID, receivePacket.getData());
				Client clientConnection = new Client(receivePacket);

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

							sendRequestToNode(node, requestComCorrelationID, correlationID);
							
							

						}
					);
				
			});	
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");	
				
		} 

	}
	
	
	private void sendRequestToNode(Node node,
										RequestOrResponse requestComCorrelationID,
										int correlationId){

		// Cria um socket UDP para enviar e receber pacotes
		DatagramSocket socket=null;
		try {
			socket = new DatagramSocket();

			// Endereço do servidor (localhost no caso)
			InetAddress serverAddress = node.getAddress();
			int serverPort = node.getPort(); // Porta que o servidor está escutando


			// Cria um pacote UDP para enviar a mensagem ao servidor
			var req = serializar(requestComCorrelationID);

			DatagramPacket sendPacket = new DatagramPacket(req, req.length, serverAddress, serverPort);
	
			socket.send(sendPacket);

			 // Buffer para receber a resposta do servidor
			 byte[] receiveData = new byte[1024];

			 // Pacote UDP para receber a resposta
			 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			 socket.receive(receivePacket); // Aguarda a resposta do servidor
 
			 // Converte a resposta em String e exibe
			 String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
			 System.out.println("Resposta do servidor: " + serverResponse+" : porta "+ socket.getLocalPort());

			 handleSetValueResponse(requestComCorrelationID);

		} // Envia o pacote
		 catch (IOException e) {
			// TODO Auto-generated catch block
			requestWaitingList.handleError(correlationID, e);
		}
		finally{
			socket.close();
		}
	}

	private byte[] serializar(Object requestComCorrelationID) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream))) {
			objectOutputStream.writeObject(requestComCorrelationID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] replymsg = byteArrayOutputStream.toByteArray();

		return replymsg;
	}


	private void handleSetValueResponse(RequestOrResponse response) {
		requestWaitingList.handleResponse(response.getCorrelationId(), response);
	}
	


	public Integer nextCorrelationID(){
		return correlationID++;
	}

	public Integer incrementaRequestID(){
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
