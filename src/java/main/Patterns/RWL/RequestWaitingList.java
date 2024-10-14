package Patterns.RWL;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.*;
import java.util.Map;

public class RequestWaitingList<TKey, TResponse> {

    private Map<TKey, CallbackDetails<TResponse>> pendingRequests = new ConcurrentHashMap<>();

    public void add(TKey key, RequestCallback<TResponse> callback) {
        // System.out.println("adicionando!");

        pendingRequests.put(key, new CallbackDetails<>(callback, System.nanoTime()));
    }

    public void handleResponse(TKey key, TResponse response) {

        if (!pendingRequests.containsKey(key)) {
            return;
        }

        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

        callbackDetails.getRequestCallback().onResponse(response);

    }

    public void handleError(TKey key, Throwable e) {

        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

        callbackDetails.getRequestCallback().onError(e);
    }

    public static void main(String[] args) {

        var reList = new RequestWaitingList<Integer, RequestOrResponse>();

        var clientRequest = new RequestOrResponse(new StringRequest(1, "testando".getBytes()), 0);

        new Thread(() -> {

            ServerSocket serverSocket;

            try {

                serverSocket = new ServerSocket(8081);

                System.out.println("Conexão Servidor 1 iniciada");

                Socket clientSocket = serverSocket.accept();

                // Lê dados do cliente TCP
                BufferedReader inCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String receivedMessage = inCliente.readLine();
                System.out.println("Servidor recebeu: " + receivedMessage);

                clientSocket.close();
                serverSocket.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }).start();

        new Thread(() -> {

            ServerSocket serverSocket;

            try {

                serverSocket = new ServerSocket(8082);

                System.out.println("Conexão Servidor 2 iniciada");

                Socket clientSocket = serverSocket.accept();

                // Lê dados do cliente TCP
                BufferedReader inCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String receivedMessage = inCliente.readLine();
                System.out.println("Servidor recebeu: " + receivedMessage);

                clientSocket.close();
                serverSocket.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }).start();

        try {

            // DatagramSocket clientSocket = new DatagramSocket(8082);
            Socket clientSocket1 = new Socket("localhost", 8081);
            //Socket clientSocket2 = new Socket("localhost", 8082);

            // var client = new Client(InetAddress.getByName("localhost"),8082, "TCP");
            var client = new Client(clientSocket1);
            client.setTypeConnection("TCP");

          //  var client2 = new Client(clientSocket2);
         //   client2.setTypeConnection("TCP");

            var callback = new WriteQuorumCallback(4, clientRequest, client);

            reList.add(1, callback);
            reList.add(2, callback);
            reList.add(3, callback);
            reList.add(4, callback);


         //   client.respond("mensagem para cliente 1".getBytes());
         //   client2.respond("mensagem para cliente 2".getBytes());

            // reList.handleResponse(1, new RequestOrResponse(new StringRequest(0, "success".getBytes()), 0));
            // reList.handleResponse(2, new RequestOrResponse(new StringRequest(0, "success".getBytes()), 0));
            reList.handleResponse(3, new RequestOrResponse(new StringRequest(0, "success".getBytes()), 0));
            reList.handleResponse(4, new RequestOrResponse(new StringRequest(0, "success".getBytes()), 0));
            reList.handleError(1, new Exception("Erro"));
            reList.handleError(2, new Exception("Erro"));
            //reList.handleError(3, new Exception("Erro"));

            clientSocket1.close();
          //  clientSocket2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
