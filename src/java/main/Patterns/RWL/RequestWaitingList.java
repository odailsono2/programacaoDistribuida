package Patterns.RWL;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.*;
import java.util.Map;

public class RequestWaitingList<TKey, TResponse> {
    
    private Map<TKey, CallbackDetails<TResponse>> pendingRequests = new ConcurrentHashMap<>();

    public void add(TKey key, RequestCallback<TResponse> callback) {
        //System.out.println("adicionando!");

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

        



        
        new Thread(()->{

        ServerSocket serverSocket;

        try {

            serverSocket = new ServerSocket(8082);


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

            //DatagramSocket clientSocket = new DatagramSocket(8082);
            Socket clientSocket = new Socket("localhost",8082);


        
            // var client = new Client(InetAddress.getByName("localhost"),8082);
            var client = new Client(clientSocket);

            client.setAdressByString("localhost");
            client.setPorta(8082);
            client.setTypeConnection("TCP");
            

            var callback = new WriteQuorumCallback(0, clientRequest, client);

            reList.add(clientRequest.getRequest().getRequestId(),callback);

            reList.handleResponse(clientRequest.getRequest().getRequestId(), new RequestOrResponse(new StringRequest(0, "sucess".getBytes()), 0));
            
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

       
    }
 

}


