package servidor;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.Node;
import Patterns.RWL.ServerNode;


public class Gateway {

    DatagramSocket serverSocket = null;

    private final int porta;

    List<ServerNode> servidores;

    ServerNode servidorLider;

    public Gateway(int porta, List<ServerNode> servidores) {
        this.porta = porta;
        this.servidores = servidores;
    }

    public void startUDPServer() {

        System.out.println("Gateway UDP na porta:" + porta);

        //new UDPserver(porta, servidores);

    }

    public void startTCPServer() {

        System.out.println("Gateway TCP na porta:" + porta);

        new TCPServer(porta, servidores);

    }

    // public void startHeartBeat(){
    //     new UDPheartBeat();
    // }

    public static void main(String[] args) {

        int porta = 8080;

        if (args.length == 1) {
            porta = Integer.parseInt(args[0]); // Converte o argumento para inteiro
        } else {
            System.out.println("Por favor, forneça a porta como argumento.");
            return; // Termina o programa caso o argumento não seja passado
        }
        
        List<ServerNode> servidoresExternos = new ArrayList<>();

        ServerNode nodeLider = new ServerNode();
        nodeLider.setAddress("localhost");
        nodeLider.setPorta(8081);
        nodeLider.setNodeId("odailsonServer");
        nodeLider.setLider(false);

        servidoresExternos.add(nodeLider);


        ServerNode node= new ServerNode();
        node.setAddress("localhost");
        node.setPorta(8082);
        node.setNodeId("PandoraServer");
        node.setLider(true);

        ServerNode node3= new ServerNode();
        node3.setAddress("localhost");
        node3.setPorta(8083);
        node3.setNodeId("PandoraServer");
        node3.setLider(true);

        servidoresExternos.add(node3);

        List<ServerNode> servidoresDisponiveis = new ArrayList<>();

        servidoresDisponiveis.add(node);

        servidoresDisponiveis.add(nodeLider);


        var gateway = new Gateway(porta, servidoresDisponiveis.stream().filter((server)->server.getLider()).toList());

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

        // executor.shutdown(); // Fecha o executor quando

    }
}
