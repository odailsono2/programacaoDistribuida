import Patterns.RWL.Node;
import Patterns.RWL.Nodemaker;
import Patterns.RWL.RequestOrResponse;
import Patterns.RWL.StringRequest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.*;
import java.util.*;
import java.net.*;

public class Gateway {

    DatagramSocket serverSocket = null;

	
    public Gateway(int porta, List<Node> servidores){

        System.out.println("Gatway iniciado na porta:" + porta);



        try {

			//criarContasTeste();

			serverSocket = new DatagramSocket(porta);

			ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

			while (true) {

				byte[] receiveMessage = new byte[1024];

				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

				serverSocket.receive(receivePacket);
				

				executor.submit(()->{
                    //long threadName = Thread.currentThread().threadId();

                    for (Node node : servidores) {
                        
                        try (DatagramSocket socket = new DatagramSocket()) {

                            InetAddress serverAddress = node.getAddress();
                            
                            int serverPort = node.getPort();

                            var msgInicialSerializar = new SerializaMensagem<RequestOrResponse>();
                            
                            var msg = (RequestOrResponse) msgInicialSerializar.deserializar(receivePacket.getData());
                            String messagemInicial = new String(msg.getRequest().getData());

                            System.out.println("Contatando Servidor porta :" + serverPort +" -- "+ messagemInicial);
                        
                            RequestOrResponse message = new RequestOrResponse(new StringRequest(1, messagemInicial.getBytes()), 2);


                            var msgSerializar = new SerializaMensagem<RequestOrResponse>();
                            byte[] sendData = msgSerializar.serializar(message).toByteArray();
                            
                            
                            // Cria um pacote UDP para enviar a mensagem ao servidor
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                            socket.send(sendPacket); // Envia o pacote

                            // Buffer para receber a resposta do servidor

                            byte[] receiveData = new byte[1024];
                            
                            // Pacote UDP para receber a resposta
                            DatagramPacket reponsePacket = new DatagramPacket(receiveData, receiveData.length);
                            socket.receive(reponsePacket); // Aguarda a resposta do servidor


                            RequestOrResponse req = (RequestOrResponse) msgSerializar.deserializar(reponsePacket.getData());

                            System.out.println("Resposta: "+req);

                            // Cria um pacote UDP para enviar a mensagem ao cliente
                            var sendDataCliente = req.getRequest().getData();
                            DatagramPacket sendPacketCliente = new DatagramPacket(sendDataCliente, sendDataCliente.length, receivePacket.getAddress(), receivePacket.getPort());
                    
                            serverSocket.send(sendPacketCliente); // Envia o pacote
                
                            System.out.println("Address: " + receivePacket.getAddress()+":"+ receivePacket.getPort());
                            
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
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

    public static void main(String[] args) {

        Node servedor1 = (new Nodemaker()).
                                    setAddress("localhost").
                                    setPort(8081).buideNode();
        
        Node servedor2 =  (new Nodemaker()).
                                    setAddress("localhost").
                                    setPort(8082).buideNode();
        
        Node servedor3 =  (new Nodemaker()).
                                    setAddress("localhost").
                                    setPort(8083).buideNode();

        List<Node> servidoresDisponiveis = new ArrayList<>();
        
        servidoresDisponiveis.add(servedor1);
        
        servidoresDisponiveis.add(servedor2);
        
   //     servidoresDisponiveis.add(servedor3);

        var gateway = new Gateway(8080,servidoresDisponiveis);


        return;
    }
}
