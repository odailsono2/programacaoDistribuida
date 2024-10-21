
package Patterns.RWL;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeartBeat implements Serializable{

    private Beating beating;

    private ConnectionUDP conexao;

    public HeartBeat(Beating beating, ConnectionUDP conexao) {
        this.beating = beating;
        this.conexao = conexao;
    }

    public void receiveBeating() throws Exception {
        beating = conexao.receiveObject(Beating.class);

    }

    public Beating getBeating() {
        return beating;
    }

    public void setBeating(String nodeId, long timestamp, String status, String address, int porta) {
        this.beating = new Beating(nodeId, timestamp, status, address, porta);
    }

    public void sendBeat(Beating beating, InetAddress serverAddress, int porta) throws Exception {

        if (conexao.getDatagramSocket() != null) {

            conexao.sendObject(beating, serverAddress, porta);

        } else {

            throw new Exception("HeartBeat.sendBeat: socket é nulo");
        }
    }

    public static void main(String[] args) {

        Map<String, Beating> heartBeatRegister = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        int portaServidor = 8080;

        executor.submit(() -> {

            try (var serverUDP = new ConnectionUDP.Builder().setSocketFromOtherSocket(new DatagramSocket()).build()) {

                var address = "localhost";

                var porta = portaServidor;

                var timestamp = System.currentTimeMillis();

                var status = "ok";

                Beating beat = new Beating("odailsonServer", timestamp, status, address, porta);

                HeartBeat heartBeat = new HeartBeat(beat, serverUDP);

                heartBeat.sendBeat(beat, InetAddress.getByName(address), porta);

                System.out.println("cliente: beat enviado");

            } catch (Exception e) {

                e.printStackTrace();

            }

        });
        
        executor.submit(() -> {

            try (var serverUDP = new ConnectionUDP.Builder().setSocketFromOtherSocket(new DatagramSocket()).build()) {

                var address = "localhost";

                var porta = portaServidor;

                var timestamp = System.currentTimeMillis();

                var status = "ok";

                Beating beat = new Beating("juliaServer", timestamp, status, address, porta);

                HeartBeat heartBeat = new HeartBeat(beat, serverUDP);

                heartBeat.sendBeat(beat, InetAddress.getByName(address), porta);

                System.out.println("cliente: beat enviado");

            } catch (Exception e) {

                e.printStackTrace();

            }

        });

        while (true) {

            

            try (DatagramSocket serverUDPsocket = new DatagramSocket(portaServidor)) {


                ConnectionUDP socketServer = new ConnectionUDP.Builder().setDatagramSocketFromOther(serverUDPsocket)
                        .build();

                Beating receiveBeating = socketServer.receiveObject(Beating.class);

                System.out.println("Servidor: Objeto recebido "+ receiveBeating);

                executor.submit(() -> {

                    heartBeatRegister.put(receiveBeating.getNodeId(), receiveBeating);

                });

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
