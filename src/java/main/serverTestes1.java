import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.StringRequest;

public class serverTestes1 {

	DatagramSocket serverSocket = null;

	private int porta;

	public serverTestes1(int porta) {
		this.porta = porta;
	}

	public void inicializar() throws Exception {

		System.out.println("Servidor Teste 1 UDP inicializado ...");
		System.out.println("Ouvindo na porta: " + porta);

		try {

			// criarContasTeste();

			serverSocket = new DatagramSocket(porta);

			@SuppressWarnings("preview")
			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			while (true) {

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);

				// String message = new String(receivePacket.getData());

				// String[] operacoBancaria =
				// Protocolo.getProtocolo().processarMensagem(message);

				executor.submit(() -> {
					// long threadName = Thread.currentThread().threadId();

					try {
						// var msgSerializar = new SerializaMensagem<RequestOrResponse>();
						RequestOrResponse req = new RequestOrResponse(new StringRequest(1, receivePacket.getData()),
								11);

						System.out.println("Vindo do gateway:");
						System.out.println(req);

						String messagemReposta = ">>Servidor 1, recebi isso de vc: "
								+ new String(req.getRequest().getData());

						var sendData = messagemReposta.getBytes();

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

	public static void main(String[] args) {

		try {
			var server = new serverTestes1(8081);
			server.inicializar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
