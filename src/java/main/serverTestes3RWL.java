import Patterns.RWL.Client;
import Patterns.RWL.Node;
import Patterns.RWL.Nodemaker;
import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.RequestWaitingList;
import Patterns.RWL.StringRequest;
import Patterns.RWL.WriteQuorumCallback;
import Protocolos.MeuProtocolo.Protocolo;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;


import services.banco.Banco;

public class serverTestes3RWL {

	private DatagramSocket serverSocket = null;

	private volatile RequestWaitingList<Integer, RequestOrResponse> requestWaitingList = new RequestWaitingList<>();

	private volatile List<Node> servers = new ArrayList<>();

	Banco banco = new Banco();

	public int porta;

	private Boolean lider = false;


	private int requestID = 0;

	private volatile int correlationId = 0;

	public serverTestes3RWL(int porta) {
		this.porta = porta;
	}

	

	public void inicializarUDP() throws Exception {
		System.out.println("");
		System.out.println("Servidor UDP inicializado ...");
		System.out.println("Ouvindo na porta: " + porta);

		try {

			// criarContasTeste();

			serverSocket = new DatagramSocket(porta);

			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			while (true) {

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);

				executor.submit(() -> {
					// long threadName = Thread.currentThread().threadId();


					try {
						// var msgSerializar = new SerializaMensagem<RequestOrResponse>();
						RequestOrResponse req = new RequestOrResponse(new StringRequest(1, receivePacket.getData()),
								11);

						System.out.println("Vindo do gateway:");
						System.out.println(new String(req.getRequest().getData()));


						String message = new String(receivePacket.getData());

						String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(message);

						try {
							banco.executarOperacao(operacoBancaria);
						} catch (Exception e) {
							var error = e.getMessage().getBytes();
							DatagramPacket sendPacket = new DatagramPacket(error, error.length,
									receivePacket.getAddress(), receivePacket.getPort());

							serverSocket.send(sendPacket); // Envia o pacote

						}

						var sendData = banco.mensagemSaida.getBytes(); // messagemReposta.getBytes();

						// Cria um pacote UDP para enviar a mensagem ao servidor
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
								receivePacket.getAddress(), receivePacket.getPort());

						serverSocket.send(sendPacket); // Envia o pacote

						// System.out.println("Address: " + receivePacket.getAddress() + ":" +
						// receivePacket.getPort());

					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("UDP Server Terminating");

		} finally {
			if (serverSocket != null && !serverSocket.isClosed()) {
				System.out.println("Fechando o socket do servidor...");
				serverSocket.close();
			}
		}
	}

	public void inicializarTCP() {


		try (ServerSocket serverSocket = new ServerSocket(porta,50,InetAddress.getByName("0.0.0.0"))) {
            
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                
            System.out.println("Servidor TCP esperando conexões na porta " + porta);
            
            while (true) {
            
                // Aceita uma conexão TCP

				Socket clientSocket = serverSocket.accept();


                executor.submit(()->{
					System.out.println("");
                    System.out.println("Cliente TCP conectado: " + clientSocket.getInetAddress()+":"+clientSocket.getPort());

                    long threadName = Thread.currentThread().threadId();

					String mensagemSaidaThread = new String();

					Boolean httpOn = false;


                    try{
                        // Lê dados do cliente TCP
                        BufferedReader inCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                       //  PrintWriter outCliente = new PrintWriter(clientSocket.getOutputStream(), true);

                        String receivedMessage = inCliente.readLine();
						System.out.println("Thread "+threadName+" mensagem recebida "+receivedMessage);

						if (receivedMessage != null && receivedMessage.startsWith("GET")){

							httpOn = true;

							var receivedMessageIsolada = receivedMessage.split(" ");
							receivedMessage = receivedMessageIsolada[1].substring(1);
						}



						//----Preparando para RequestWaitingList
						
						var clientConnection = new Client(clientSocket);

						clientConnection.setTypeConnection("TCP");

						if (!lider){


							try {
								mensagemSaidaThread = executarOperacaoBancaria(receivedMessage, httpOn)+System.lineSeparator();

								
							} catch (Exception e) {
								var erroBanco = e.getMessage()+System.lineSeparator();
								clientConnection.respond(erroBanco.getBytes());

							}

							


							clientConnection.respond(mensagemSaidaThread.getBytes());

							

							

							
						}

						StringRequest requestWithID = createRequestsWithID(receivedMessage);

						for (Node node : servers) {
							System.out.println("Laço requestwaitinglist");

							nextCorrelationId();

							var requestToOtherServers = new RequestOrResponse(requestWithID, correlationId);

							var callback = new WriteQuorumCallback(servers.size(),requestToOtherServers,clientConnection);

							requestWaitingList.add(correlationId, callback);

							try {

								var mensagemRecebida = sendAndReceiveRequestOtherServers(receivedMessage.getBytes(), node);

								RequestOrResponse responseCurrenteNodeRoQ = 
														new RequestOrResponse(
																new StringRequest(
																	requestID, 
																	mensagemRecebida.getBytes())
																,correlationId);

								requestWaitingList.handleResponse(correlationId, responseCurrenteNodeRoQ);

								
							} catch (Exception e) {

								requestWaitingList.handleError(correlationId, e);

							}		

							

							
						}
						
						//-----Fim RequestWaitingList
/* 
						var responseCurrentNode = executarOperacaoBancaria(receivedMessage,httpOn);
							
						RequestOrResponse responseCurrenteNodeRoQ= new RequestOrResponse(new StringRequest(requestID, responseCurrentNode.getBytes()),correlationId);

						requestWaitingList.handleResponse(correlationId, responseCurrenteNodeRoQ);

						System.out.println("Thread: " + threadName + ", Mensagem do cliente: " + receivedMessage);

						


						String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(receivedMessage);

						try {
							banco.executarOperacao(operacoBancaria);

							if (httpOn){

								mensagemSaidaThread =  handleHTTP(receivedMessage, banco.mensagemSaida);

							}
							else{
								mensagemSaidaThread = banco.mensagemSaida+System.lineSeparator();
							}

						} catch (Exception e) {


							if (httpOn){
								System.out.println(e.getMessage());


								outCliente.println(handleHTTP(receivedMessage, e.getMessage()));

							}
							else{
								System.out.println(e.getMessage());

								outCliente.println(e.getMessage()+System.lineSeparator());
							}


						}
							*/
						System.out.println(mensagemSaidaThread);
                       // Enviar uma resposta ao cliente TCP
                    //    outCliente.println(mensagemSaidaThread);
                        
                        clientSocket.close();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
					finally{
						try {
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
        }
    }

	private String executarOperacaoBancaria(String receivedMessage, Boolean httpOn) throws Exception {

		String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(receivedMessage);

		String mensagemSaida = new String(); 

		try {
			banco.executarOperacao(operacoBancaria);

			if (httpOn){

				mensagemSaida =  handleHTTP(receivedMessage, banco.mensagemSaida);

			}
			else{
				mensagemSaida = banco.mensagemSaida + System.lineSeparator();
			}


		} catch (Exception e) {


			if (httpOn){
				System.out.println(e.getMessage());

				mensagemSaida = handleHTTP(receivedMessage, e.getMessage());

				//outCliente.println(handleHTTP(receivedMessage, e.getMessage()));

			}
			else{
				System.out.println(e.getMessage());

				mensagemSaida = e.getMessage() + System.lineSeparator();

				//outCliente.println(e.getMessage()+System.lineSeparator());
			}

			throw new Exception(mensagemSaida);


		}
		return mensagemSaida;

	}



	private String handleHTTP(String requisicao, String respostaServico) {
		
		StringBuilder respostaHTML = new StringBuilder();

		var quebralinha = System.lineSeparator();

            // Gera a resposta HTML
            String responseBody =  "<html>" +
								"<head><title>Banco Metrópole</title></head>" +
								"<body>"+
								"<h1>Banco Metrópole</h1>"+
								"<p>Requisição: "+requisicao+"</p>" +
								"<p>Resposta: "+respostaServico+"</p>"+
								"</body>" +
								"</html>";

            // Envia a resposta HTTP com o código de status 200 OK
            respostaHTML.append("HTTP/1.1 200 OK").append(quebralinha);
            respostaHTML.append("Content-Type: text/html; charset=UTF-8").append(quebralinha);
            respostaHTML.append("Content-Length: " + responseBody.getBytes().length).append(quebralinha);
            respostaHTML.append("").append(quebralinha); // Linha em branco separa os cabeçalhos do corpo
            respostaHTML.append(responseBody).append(quebralinha); // Envia o corpo da resposta (HTML)

		return respostaHTML.toString();

    }

	public StringRequest createRequestsWithID(String requestRecebida){

		StringRequest novaRequest = new StringRequest(nextRequestId(), requestRecebida.getBytes());
		return novaRequest;
	
	}

	public RequestOrResponse createNewRequestWithCorrelationID(StringRequest novaRequest){

		RequestOrResponse clientRequest = new RequestOrResponse(novaRequest, nextCorrelationId());

		return clientRequest;
		
	}

	public void handlRequestsWaitingList(RequestOrResponse novaRequestOrResponse, Client client, int quorum){

		var callback = new WriteQuorumCallback(quorum, novaRequestOrResponse, client);

		requestWaitingList.add(requestID, callback);


	}

	public void handleResponsesFromServers(Integer key, RequestOrResponse responseFromServer){

		requestWaitingList.handleResponse(key, responseFromServer);
	}

	



	private int nextCorrelationId() {

		return correlationId ++;
	}



	private int nextRequestId() {
		return requestID ++ ;
	}

	public String sendAndReceiveRequestOtherServers(byte [] request, Node node) throws IOException{
		

			node.makeSocket();

			node.sendMessage(request);


			var messageReceived = node.receiveMessage();


			node.closeSocket();

			return messageReceived;

                                
	}



	public static void main(String[] args) {


		ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Criar threads virtuais para TCP e UDP
	

		serverTestes3RWL server = new serverTestes3RWL(8083);



		var udpFuture1 = virtualThreadExecutor.submit(() -> {
						try {
							server.inicializarUDP();
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
						}
					});


		var tcpFuture1 = virtualThreadExecutor.submit(() -> server.inicializarTCP());

        // Verificar se a tarefa foi concluída e capturar exceções
        try {
            udpFuture1.get(); // Bloqueia até que a tarefa seja concluída
            tcpFuture1.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage()); // Captura e imprime qualquer erro
        } 

	}


}
