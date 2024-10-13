package Patterns.RWL;
import java.io.*;
import java.net.*;

public class Client{

	private int porta;
	private InetAddress address;
	private DatagramSocket clientDatagramSocket;
	private Socket clientSocket;
	private TypeConnection typeConnection;

	public Client(InetAddress address, int porta, String typeConnection){

		this.address = address;

		this.porta = porta;

		this.typeConnection = TypeConnection.valueOf(typeConnection);
	}
	public void setTypeConnection(String typeConnection) {
		this.typeConnection = TypeConnection.valueOf(typeConnection);
	}

	public void setPorta(int porta) {
		this.porta = porta;
	}

	public void setAdressByString(String address) throws UnknownHostException {
		this.address = InetAddress.getByName(address);
	}

	public Client(DatagramSocket clientDatagramSocket){
		this.clientDatagramSocket = clientDatagramSocket;


	}

	public Client(Socket clientSocket){
		this.clientSocket = clientSocket;


	}
	public int getPort() {
		return porta;
	}

	public InetAddress getAdress() {
		return address;
	}

	private void respondViaUDP(byte [] reply) throws IOException{

		clientDatagramSocket.send(new DatagramPacket(reply,reply.length,address,porta));

	}

	private void respondViaTCP(byte[] reply) throws IOException{


		// System.out.println(new String(reply));
		
		ByteArrayOutputStream replyByteArrayOutputStream = new ByteArrayOutputStream();

		replyByteArrayOutputStream.write(reply);


		BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

     	replyByteArrayOutputStream.writeTo(out);

		out.flush();

		//out.close();



	}

	public void respond(byte[] reply) throws IOException{

		switch (typeConnection) {
			case TypeConnection.TCP:

				respondViaTCP(reply);
				
				break;

			case TypeConnection.UDP:

				respondViaUDP(reply);
				
				break;
		
			default:
				break;
		}

	}

	public static void main(String[] args) {
		TypeConnection test = TypeConnection.valueOf("UDP");

		System.out.println(test.name());
		System.out.println(test.getValor());
	}
}

