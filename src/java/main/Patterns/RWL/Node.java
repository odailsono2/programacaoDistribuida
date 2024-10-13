package Patterns.RWL;

import java.net.*;
import java.io.*;

public class Node implements ServerNode {
    private int Idservidor;
    private InetAddress address;
    private int port;
    private DatagramSocket nodeDatagramSocket;
    private Socket nodeSocket;
    private TypeConnection typeConnection;
    private Boolean lider = false;

    public void setLider(Boolean lider) {
        this.lider = lider;
    }
    
    public Boolean getLider() {
        return lider;
    }

    public Node() {
    }

    public Node(InetAddress address, int port, String typeConnection) {
        this.address = address;
        this.port = port;
        this.typeConnection = TypeConnection.valueOf(typeConnection);
    }

    public void setNodeDatagramSocket(DatagramSocket nodeDatagramSocket) {
        this.nodeDatagramSocket = nodeDatagramSocket;
    }

    public void setNodeSocket(Socket nodeSocket) {
        this.nodeSocket = nodeSocket;
    }

    public void setTypeConnection(TypeConnection typeConnection) {
        this.typeConnection = typeConnection;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getIdservidor() {
        return Idservidor;
    }

    public void setIdservidor(int idservidor) {
        Idservidor = idservidor;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void setAdress(String address) {

        try {
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    public void makeSocket() throws IOException{
        // System.out.println(address.getHostName());
        var novoSocket = new Socket(address.getHostName(),port);
        nodeSocket = novoSocket;
    }

    private void sendMessageViaUDP(byte [] reply) throws IOException{


		nodeDatagramSocket.send(new DatagramPacket(reply,reply.length,address,port));


	}

	private void sendMessageViaTCP(byte[] message) throws IOException{
        // System.out.println(new String(message));

		// ByteArrayOutputStream replyByteArrayOutputStream = new ByteArrayOutputStream();

		// replyByteArrayOutputStream.write(message);

		// BufferedOutputStream out = new BufferedOutputStream(nodeSocket.getOutputStream());
        // //PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);

     	// replyByteArrayOutputStream.writeTo(out);



		// out.flush();

        PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);

        out.println(new String(message));

	}

    public String receiveMessage() throws IOException{
         // Lê dados do cliente TCP
         if (nodeSocket.isConnected()){
            System.out.println("socket conectado");
         }

         BufferedReader inCliente = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

         StringBuilder mensagemRecebida = new StringBuilder(); 

         System.out.println("Aguardando mensagem...");

    
         //String linha;
        // linha = inCliente.readLine();

         inCliente.lines().forEach((linha)->{
            //System.out.println(linha);
            mensagemRecebida.append(linha).append(System.lineSeparator());
        });
         
         return mensagemRecebida.toString();
    }

    public void sendMessage(byte [] message) throws IOException{

		switch (typeConnection) {
			case TypeConnection.TCP:

                sendMessageViaTCP(message);
				
				break;

			case TypeConnection.UDP:

                sendMessageViaUDP(message);
				
				break;
		
			default:
				break;
		}
    }

    public void closeSocket() throws IOException {
        nodeSocket.close();
    }

    public static void main(String[] args) {
        var address = "localhost";
        try {
            Node node1 = new Node(InetAddress.getByName(address),8081,"TCP");

            node1.makeSocket();

            node1.sendMessage("criar;1".getBytes());

            var mensagem = node1.receiveMessage();

        

            node1.closeSocket();

            System.out.println(mensagem);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}
