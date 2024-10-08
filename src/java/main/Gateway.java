import Patterns.RWL.Node;
import Patterns.RWL.Nodemaker;
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

        UDPserver servidorUDP = new UDPserver(porta, servidores);


    }

    public void startTCPServer(){

        System.out.println("Gateway TCP na porta:" + porta);

        TCPserver servidorTCP = new TCPserver(porta, servidores);

    }


    public static void main(String[] args) {

        Node servedor1 = (new Nodemaker()).setAddress("localhost").setPort(8081).buideNode();

        // Node servedor2 = (new
        // Nodemaker()).setAddress("localhost").setPort(8082).buideNode();

        // Node servedor3 = (new
        // Nodemaker()).setAddress("localhost").setPort(8083).buideNode();

        List<Node> servidoresDisponiveis = new ArrayList<>();

        servidoresDisponiveis.add(servedor1);

        // servidoresDisponiveis.add(servedor2);

        // servidoresDisponiveis.add(servedor3);

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
