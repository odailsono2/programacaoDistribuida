import Patterns.RWL.Node;

import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gateway {

    DatagramSocket serverSocket = null;

    private final int porta;
    
    List<Node> servidores;

    public Gateway(int porta, List<Node> servidores) {
        this.porta = porta;
        this.servidores = servidores;
    }

    public void startUDPServer() {
        
        

        System.out.println("Gateway UDP na porta:" + porta);

        new UDPserver(porta, servidores);


    }

    public void startTCPServer(){

        System.out.println("Gateway TCP na porta:" + porta);

        new TCPServer(porta, servidores);

    }


    public static void main(String[] args) {

        //Node servedor1 = (new Nodemaker()).setAddress("localhost").setPort(8081).buildNode();

        var address = "localhost";
        Node node1 = new Node();
        Node node2 = new Node();

        Node node3 = new Node();

        try {
            node1.setAddress(InetAddress.getByName(address));
            node2.setAddress(InetAddress.getByName(address));
            node3.setAddress(InetAddress.getByName(address));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        node1.setPort(8081);
        node2.setPort(8082);

        node3.setPort(8083);
        node1.setLider(true);


        
            

        List<Node> servidoresDisponiveis = new ArrayList<>();

        servidoresDisponiveis.add(node1);

        servidoresDisponiveis.add(node2);

        servidoresDisponiveis.add(node3);

        var gateway = new Gateway(8080, servidoresDisponiveis);


        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Criar threads virtuais para TCP e UDP
        
        var udpFuture = virtualThreadExecutor.submit(() -> gateway.startUDPServer());
        var tcpFuture = virtualThreadExecutor.submit(() -> gateway.startTCPServer());

        // Verificar se a tarefa foi concluída e capturar exceções
        try {
            udpFuture.get(); // Bloqueia até que a tarefa seja concluída
            tcpFuture.get();
        } catch (Exception e) {
            e.printStackTrace(); // Captura e imprime qualquer erro
        } 

            //  executor.shutdown(); // Fecha o executor quando 

        
    }
}
