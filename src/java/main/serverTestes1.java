import Patterns.RWL.Node;
import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.StringRequest;
import Protocolos.MeuProtocolo.Protocolo;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import services.banco.Banco;

public class serverTestes1 {

	DatagramSocket serverSocket = null;

	Banco banco = new Banco();

	private int porta;

	public serverTestes1(int porta) {
		this.porta = porta;
	}

	

	public void inicializarUDP() throws Exception {

		System.out.println("Servidor Teste 1 UDP inicializado ...");
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
						System.out.println(req);


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

                        String message = new String(receivedMessage.getBytes());

						String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(message);

						try {
							banco.executarOperacao(operacoBancaria);
						} catch (Exception e) {

							outCliente.println(e.getMessage()); // Envia o pacote

						}

                       // Enviar uma resposta ao cliente TCP
                        outCliente.println(banco.mensagemSaida);
                        
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


		ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Criar threads virtuais para TCP e UDP
	

		serverTestes1 server = new serverTestes1(8081);


		var udpFuture = virtualThreadExecutor.submit(() -> {
						try {
							server.inicializarUDP();
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
						}
					});


		var tcpFuture = virtualThreadExecutor.submit(() -> server.inicializarTCP());

        // Verificar se a tarefa foi concluída e capturar exceções
        try {
            udpFuture.get(); // Bloqueia até que a tarefa seja concluída
            tcpFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage()); // Captura e imprime qualquer erro
        } 

	}


}
