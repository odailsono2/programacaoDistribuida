package servidor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Patterns.RWL.Connection;
import Patterns.RWL.Beating;

public class UDPheartBeat {

    Map<String, Beating> listaServidores;

    public UDPheartBeat() {
        listaServidores = new ConcurrentHashMap<>();
    }

    public Map<String, Beating> getListaServidores() {
        return listaServidores;
    }

    public void removeNoBeatingServer(Long timeControlBeating) {

        while (true) {
            
            listaServidores.values().stream().forEach((beating) -> {
                var currentTime = System.currentTimeMillis();
                var duration = currentTime - beating.getTimestamp();
                if (duration > timeControlBeating) {
                    listaServidores.remove(beating.getNodeId());
                }
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void listenHeartBeat(int porta) {
        System.out.println("Servidor UDP HeartBeat na porta " + porta);

        try (var serverSocket = new DatagramSocket(porta);) {

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            while (true) {

                // byte[] receiveMessage = new byte[1024];

                // DatagramPacket receivePacket = new DatagramPacket(receiveMessage,
                // receiveMessage.length);

                // serverSocket.receive(receivePacket);

                var receivePacket = Connection.receiveUDP(serverSocket);

                var addressCliente = receivePacket.getAddress().getHostAddress();

                int portaCliente = receivePacket.getPort();

                var mensagemCliente = receivePacket.getData();

                var heartbeat = Connection.deserializarOBJ(Beating.class, mensagemCliente);

                System.out.println("(Servidor) Cliente UDP conectado: " + addressCliente);

                executor.submit(() -> {

                    long threadName = Thread.currentThread().threadId();

                    System.out.println(
                            "Thread: " + threadName + ", Mensagem do cliente: " + heartbeat);

                    heartbeat.setTimestamp(System.currentTimeMillis());

                    listaServidores.put(heartbeat.getNodeId(), heartbeat);
                }

                );
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("UDP Server Terminating");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final int porta;
        if (args.length == 1) {
            porta = Integer.parseInt(args[0]); // Converte o argumento para inteiro
        } else {
            System.out.println("Por favor, forneça a porta como argumento.");
            return; // Termina o programa caso o argumento não seja passado
        }

        var heartBeat = new UDPheartBeat();
        

        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Criar threads virtuais para TCP e UDP

        var udpFuture = virtualThreadExecutor.submit(() -> heartBeat.listenHeartBeat(porta));
        var tcpFuture = virtualThreadExecutor.submit(() -> heartBeat.removeNoBeatingServer((long) 3050));

        // Verificar se a tarefa foi concluída e capturar exceções
        try {
            udpFuture.get(); // Bloqueia até que a tarefa seja concluída
            tcpFuture.get();
        } catch (Exception e) {
            e.printStackTrace(); // Captura e imprime qualquer erro
        }
    }
}
