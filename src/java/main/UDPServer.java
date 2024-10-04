import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Protocolos.MeuProtocolo.Protocolo;
import services.banco.Banco;

public class UDPServer {
	Banco banco = new Banco();
	String reply= "";
	DatagramSocket serverSocket = null;

	List<Client> connectedClients = new ArrayList<>(); 
	
	public UDPServer(int porta) throws Exception{
	

		System.out.println("Servidor UDP inicializado ...");
		System.out.println("Ouvindo na porta: "+porta);

		try {

			//criarContasTeste();

			serverSocket = new DatagramSocket(porta);

			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			while (true) {

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);
				
				String message = new String(receivePacket.getData());


				String[] operacoBancaria = Protocolo.getProtocolo().processarMensagem(message);

				executor.submit(()->{
				long threadName = Thread.currentThread().threadId();

				try {
					banco.executarOperacao(operacoBancaria);
					reply = banco.mensagemSaida == "" ? "Comando Não Reconhecido":"Thread:"+threadName+": "+banco.mensagemSaida;
				} catch (Exception e) {
					reply = "Thread:"+threadName+": "+e.getMessage();
				}

				//banco.getContas().values().stream().forEach(System.out::println); 

				// byte[] replymsg = reply.getBytes();
				// DatagramPacket sendPacket = new DatagramPacket(replymsg,replymsg.length,receivePacket.getAddress(),receivePacket.getPort());
				
				connectedClients.add(new Client(receivePacket));

				
				
				try {
					StringRequest request = new StringRequest(RequestId.GetValueRequest, reply.getBytes());
					RequestOrResponse req = new RequestOrResponse(request, 1);
					serverSocket.send(connectedClients.get(0).respond(req));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});	
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");	
				
		} finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                System.out.println("Fechando o socket do servidor...");
                serverSocket.close();
            }
        }
	}

	public void criarContasTeste() throws Exception{
		banco.criar("1");
        banco.criar("2");
        banco.criar("3");
	}
	public static void main(String[] args) { 

			try {
				new UDPServer(8080);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
			
		}
}

class Client{

	private int port;
	private InetAddress adress;

	Client(DatagramPacket clientReceivedPacket){
		port = clientReceivedPacket.getPort();
		adress = clientReceivedPacket.getAddress();
	}

	public int getPort() {
		return port;
	}

	public InetAddress getAdress() {
		return adress;
	}

	public DatagramPacket respond(RequestOrResponse reply){
		byte[] replymsg = reply.getRequest().getData();
		return new DatagramPacket(replymsg,replymsg.length,adress,port);
	}
}
