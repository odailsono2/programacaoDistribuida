package servidor;
import Patterns.RWL.Client;
import Patterns.RWL.Node;
import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.RequestWaitingList;
import Patterns.RWL.StringRequest;
import Patterns.RWL.TypeConnection;
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

public class Servidor {

	private DatagramSocket serverSocket = null;

	private volatile RequestWaitingList<Integer, RequestOrResponse> requestWaitingList = new RequestWaitingList<>();

	private volatile List<Node> servers = new ArrayList<>();

	Banco banco = new Banco();

	public int porta;

	private Boolean lider = true;

	private int requestID = 0;

	private volatile int correlationId = 0;

	public Servidor(int porta) {
		this.porta = porta;

		var address = "localhost";

		Node node1 = new Node();
		Node node2 = new Node();

		Node node3 = new Node();

		try {
			node1.setAddress(InetAddress.getByName(address));
			node2.setAddress(InetAddress.getByName(address));
			node3.setAddress(InetAddress.getByName(address));

		} catch (Exception e) {
			e.printStackTrace();
		}
		node1.setPort(8081);
		node2.setPort(8082);

		node3.setPort(8083);
		node1.setLider(true);
		servers.add(node1);

		servers.add(node2);

		servers.add(node3);

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

		// ServerSocket serverSocket = new ServerSocket(8081, 50,
		// InetAddress.getByName("127.0.0.1"));

		try (ServerSocket serverSocket = new ServerSocket(porta, 1, InetAddress.getByName("127.0.0.1"))) {

			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			System.out.println("Servidor TCP esperando conexões na porta " + porta);

			while (true) {

				// Aceita uma conexão TCP

				Socket clientSocket = serverSocket.accept();

				executor.submit(() -> {

					System.out.println("");
					System.out.println(
							"Cliente TCP conectado: " + clientSocket.getInetAddress() + ":"
									+ clientSocket.getLocalPort());

					long threadName = Thread.currentThread().threadId();

					String mensagemSaidaThread = new String();

					Boolean httpOn = false;

					try {
						// Lê dados do cliente TCP
						BufferedReader inCliente = new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()));
						PrintWriter outCliente = new PrintWriter(clientSocket.getOutputStream(),
								true);

						String receivedMessage = inCliente.readLine();

						System.out.println("Thread " + threadName +
								" lidando com: " + receivedMessage);

						// System.out.println("Thread " + threadName + " mensagem recebida " +
						// receivedMessage);

						if (receivedMessage != null && receivedMessage.startsWith("GET")) {

							httpOn = true;
							// System.out.println("http ON");

							var receivedMessageIsolada = receivedMessage.split(" ");
							receivedMessage = receivedMessageIsolada[1].substring(1);

						}

						if (receivedMessage != null && receivedMessage.contains("favicon.ico")) {
							System.out.println("Requisição para /favicon.ico ignorada.");
							clientSocket.close(); // Fechar a conexão e ignorar
							// Thread.currentThread().interrupt();
							return;
						}

						String tipoNodeMensagameRecebida = "servidor";

						// retira cabeçalho
						if (receivedMessage != null) {

							var receivedMessageIsolada = receivedMessage.split(":");
							receivedMessage = receivedMessageIsolada[1];
							tipoNodeMensagameRecebida = receivedMessageIsolada[0];
						}

						// -------------

						// ----Preparando para RequestWaitingList

						var clientConnection = new Client(clientSocket);

						clientConnection.setTypeConnection("TCP");

						// System.out.println("tipo de servidor" + tipoNodeMensagameRecebida);

						if (lider && tipoNodeMensagameRecebida.equals("Lider")
								|| lider && tipoNodeMensagameRecebida.equals("servidor")) {

							System.out.println("Mensagem Recebida " + receivedMessage + " do servidor "
									+ clientSocket.getInetAddress().getHostName());
							try {

								if (receivedMessage == null) {

									throw new Exception("receivedMessage is null");

								}
								mensagemSaidaThread = executarOperacaoBancaria(receivedMessage, httpOn);

							} catch (Exception e) {
								var erroBanco = e.getMessage();
								clientConnection.respond(erroBanco.getBytes());

							}

							clientConnection.respond(mensagemSaidaThread.getBytes());

						}

						if (lider && tipoNodeMensagameRecebida.equals("cliente")) {
							///- Request Waiting Lista TCP	
							System.out.println("--- Iniciando Request WaitingList");

							// "GET /cliente:criar;3 HTTP/1.1");
							// "Host: localhost");
							// "Connection: close");
							// "");

							// Linha em branco para indicar o fim dos cabeçalhos

							StringRequest requestWithID = createRequestsWithID(receivedMessage);

							var callback = new WriteQuorumCallback(servers.size(),
									new RequestOrResponse(requestWithID, 0), clientConnection);

							for (Node node : servers) {
								node.setTypeConnection(TypeConnection.TCP);
								System.out.println("Enviando mensagem para no: " + node.getPort());
								// System.out.println("No: " + node.getAddress().getHostName() + ":" +
								// node.getPort());

								nextCorrelationId();

								var requestToOtherServers = new RequestOrResponse(requestWithID, correlationId);
								requestToOtherServers.setOrigemAddress(clientSocket.getInetAddress().getHostName());
								requestToOtherServers.setOrigemPort(porta);

								requestWaitingList.add(correlationId, callback);

								try {
									var tipoNode = node.getLider() ? "Lider" : "servidor";

									var localReceivedMensage = tipoNode + ":" + receivedMessage;

									var httpCabecalho = "GET /" + localReceivedMensage
											+ " HTTP/1.1\r\n" +
											"Host: localhost\r\n" +
											"Connection: close\r\n" +
											"\r\n";

									System.out.println("\r\nCabecalho http\r\n" + httpCabecalho);

									var sendMessage = httpOn ? httpCabecalho : localReceivedMensage;

									System.out.println("mensagem a ser enviado\r\n" + sendMessage);

									var mensagemRecebida = sendAndReceiveRequestOtherServers(
											sendMessage.getBytes(),
											node);

									RequestOrResponse responseCurrenteNodeRoQ = new RequestOrResponse(
											new StringRequest(
													requestID,
													mensagemRecebida.getBytes()),
											correlationId);

									requestWaitingList.handleResponse(correlationId, responseCurrenteNodeRoQ);

								} catch (Exception e) {

									requestWaitingList.handleError(correlationId, e);

								}

							}

							// -----Fim RequestWaitingList
						}

						System.out.println(mensagemSaidaThread);

						clientSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
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

			if (httpOn) {

				mensagemSaida = handleHTTP(receivedMessage, banco.mensagemSaida);

			} else {
				mensagemSaida = banco.mensagemSaida + System.lineSeparator();
			}

		} catch (Exception e) {

			if (httpOn) {
				System.out.println(e.getMessage());

				mensagemSaida = handleHTTP(receivedMessage, e.getMessage());

				// outCliente.println(handleHTTP(receivedMessage, e.getMessage()));

			} else {
				System.out.println(e.getMessage());

				mensagemSaida = e.getMessage();

				// outCliente.println(e.getMessage()+System.lineSeparator());
			}

			throw new Exception(mensagemSaida);

		}
		return mensagemSaida;

	}

	private String handleHTTP(String requisicao, String respostaServico) {
		StringBuilder respostaHTML = new StringBuilder();

		// Usar CRLF como separador de linha, conforme exigido pelo protocolo HTTP
		String crlf = "\r\n";

		// Gera o corpo da resposta HTML
		String responseBody = "<html>" +
				"<head><title>Banco Metrópole</title></head>" +
				"<body>" +
				"<h1>Banco Metrópole</h1>" +
				"<p>Requisição: " + requisicao + "</p>" +
				"<p>Resposta: " + respostaServico + "</p>" +
				"</body>" +
				"</html>";

		// Construir a resposta HTTP
		respostaHTML.append("HTTP/1.1 200 OK").append(crlf);
		respostaHTML.append("Content-Type: text/html; charset=UTF-8").append(crlf);
		respostaHTML.append("Content-Length: " + responseBody.getBytes().length).append(crlf);
		respostaHTML.append("Connection: close").append(crlf);
		respostaHTML.append(crlf); // Linha em branco para separar cabeçalhos e corpo
		respostaHTML.append(responseBody); // Corpo da resposta (HTML)

		return respostaHTML.toString();
	}

	public StringRequest createRequestsWithID(String requestRecebida) {

		StringRequest novaRequest = new StringRequest(nextRequestId(), requestRecebida.getBytes());
		return novaRequest;

	}

	public RequestOrResponse createNewRequestWithCorrelationID(StringRequest novaRequest) {

		RequestOrResponse clientRequest = new RequestOrResponse(novaRequest, nextCorrelationId());

		return clientRequest;

	}

	public void handlRequestsWaitingList(RequestOrResponse novaRequestOrResponse, Client client, int quorum) {

		var callback = new WriteQuorumCallback(quorum, novaRequestOrResponse, client);

		requestWaitingList.add(requestID, callback);

	}

	public void handleResponsesFromServers(Integer key, RequestOrResponse responseFromServer) {

		requestWaitingList.handleResponse(key, responseFromServer);
	}

	private int nextCorrelationId() {

		return correlationId++;
	}

	private int nextRequestId() {
		return requestID++;
	}

	public String sendAndReceiveRequestOtherServers(byte[] request, Node node) throws IOException {

		node.makeSocket();

		node.sendMessage(request);

		var messageReceived = node.receiveMessage();

		node.closeSocket();

		return messageReceived;

	}

	public static void main(String[] args) {

		ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

		// Criar threads virtuais para TCP e UDP

		Servidor server = new Servidor(8081);

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
